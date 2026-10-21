import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { authenticatedFetch } from '../authenticatedFetch'
import { getAccessToken, getRefreshToken, saveAuthTokens } from '../tokenStorage'
import type { AuthResponse } from '../authTypes'
import { FetchTimeoutError } from '../../../app/fetchWithTimeout'

const storage = new Map<string, string>()

function authResponse(accessToken: string, refreshToken: string): AuthResponse {
    return {
        accessToken,
        refreshToken,
        tokenType: 'Bearer',
        expiresInMs: 60_000,
        user: {
            id: 1,
            email: 'candidate@example.com',
            fullName: 'Test Candidate',
            status: 'ACTIVE',
            roles: ['ROLE_CANDIDATE'],
        },
    }
}

function apiResponse(data: AuthResponse): Response {
    return new Response(JSON.stringify({ statusCode: 200, message: 'ok', data }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
    })
}

describe('authenticatedFetch', () => {
    afterEach(() => {
        vi.useRealTimers()
        vi.unstubAllGlobals()
    })

    beforeEach(() => {
        storage.clear()
        globalThis.localStorage = {
            getItem: (key: string) => storage.get(key) ?? null,
            setItem: (key: string, value: string) => storage.set(key, value),
            removeItem: (key: string) => storage.delete(key),
            clear: () => storage.clear(),
            length: storage.size,
            key: () => null,
        } as Storage
        globalThis.window = {
            dispatchEvent: () => true,
            setTimeout,
            clearTimeout,
        } as unknown as Window & typeof globalThis
        vi.stubGlobal('navigator', {
            locks: { request: (_name: string, _options: LockOptions, callback: (lock: Lock) => unknown) => Promise.resolve(callback({} as Lock)) },
        })
        vi.restoreAllMocks()
    })

    it('refreshes an expired session and retries the original request once', async () => {
        saveAuthTokens(authResponse('expired-access', 'current-refresh'))
        const fetchMock = vi.spyOn(globalThis, 'fetch')
            .mockResolvedValueOnce(new Response('unauthorized', { status: 401 }))
            .mockResolvedValueOnce(apiResponse(authResponse('fresh-access', 'rotated-refresh')))
            .mockResolvedValueOnce(new Response('ok', { status: 200 }))

        const response = await authenticatedFetch('/api/v1/admin/candidates', {
            method: 'GET',
            headers: { Accept: 'application/json' },
        })

        expect(response.status).toBe(200)
        expect(fetchMock).toHaveBeenCalledTimes(3)
        expect(new Headers(fetchMock.mock.calls[0]?.[1]?.headers).get('Authorization'))
            .toBe('Bearer expired-access')
        expect(fetchMock.mock.calls[1]?.[0]).toBe('/api/v1/auth/refresh-token')
        expect(fetchMock.mock.calls[1]?.[1]?.body).toBe(JSON.stringify({ refreshToken: 'current-refresh' }))
        expect(new Headers(fetchMock.mock.calls[2]?.[1]?.headers).get('Authorization'))
            .toBe('Bearer fresh-access')
        expect(getAccessToken()).toBe('fresh-access')
        expect(getRefreshToken()).toBe('rotated-refresh')
    })

    it('clears an invalid refresh session and returns the original unauthorized response', async () => {
        saveAuthTokens(authResponse('expired-access', 'invalid-refresh'))
        const fetchMock = vi.spyOn(globalThis, 'fetch')
            .mockResolvedValueOnce(new Response('unauthorized', { status: 401 }))
            .mockResolvedValueOnce(new Response('invalid refresh', { status: 401 }))

        const response = await authenticatedFetch('/api/v1/admin/candidates')

        expect(response.status).toBe(401)
        expect(fetchMock).toHaveBeenCalledTimes(2)
        expect(getAccessToken()).toBeNull()
        expect(getRefreshToken()).toBeNull()
    })

    it('does not refresh requests that do not return unauthorized', async () => {
        saveAuthTokens(authResponse('valid-access', 'current-refresh'))
        const fetchMock = vi.spyOn(globalThis, 'fetch')
            .mockResolvedValueOnce(new Response('ok', { status: 200 }))

        const response = await authenticatedFetch('/api/v1/jobs')

        expect(response.status).toBe(200)
        expect(fetchMock).toHaveBeenCalledTimes(1)
    })

    it('applies one deadline across the initial request, refresh, and retry', async () => {
        vi.useFakeTimers()
        saveAuthTokens(authResponse('expired-access', 'current-refresh'))
        const fetchMock = vi.spyOn(globalThis, 'fetch')
            .mockImplementationOnce(() => new Promise<Response>((resolve) => {
                globalThis.setTimeout(() => resolve(new Response(null, { status: 401 })), 10)
            }))
            .mockImplementationOnce(() => new Promise<Response>((resolve) => {
                globalThis.setTimeout(() => resolve(apiResponse(authResponse('fresh-access', 'rotated-refresh'))), 25)
            }))
            .mockImplementationOnce((_input, init) => new Promise<Response>((_resolve, reject) => {
                init?.signal?.addEventListener('abort', () => reject(new DOMException('Aborted', 'AbortError')), { once: true })
            }))

        const request = authenticatedFetch('/api/v1/assistant/chat', {}, 40)
        const assertion = expect(request).rejects.toBeInstanceOf(FetchTimeoutError)

        await vi.advanceTimersByTimeAsync(10)
        expect(fetchMock).toHaveBeenCalledTimes(2)
        await vi.advanceTimersByTimeAsync(25)
        expect(fetchMock).toHaveBeenCalledTimes(3)
        await vi.advanceTimersByTimeAsync(4)
        await vi.advanceTimersByTimeAsync(1)

        await assertion
    })
})
