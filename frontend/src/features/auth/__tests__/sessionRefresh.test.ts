import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { refreshAuthSession } from '../sessionRefresh'
import { clearAuthTokens, getAccessToken, getRefreshToken, getStoredUser, saveAuthTokens } from '../tokenStorage'
import type { AuthResponse } from '../authTypes'

const storage = new Map<string, string>()

function authResponse(accessToken: string, refreshToken: string): AuthResponse {
    return {
        accessToken,
        refreshToken,
        tokenType: 'Bearer',
        expiresInMs: 60_000,
        user: {
            id: 7,
            email: 'qa-candidate@example.invalid',
            fullName: 'QA Candidate',
            status: 'ACTIVE',
            roles: ['ROLE_CANDIDATE'],
        },
    }
}

describe('session refresh race protection', () => {
    afterEach(() => {
        vi.unstubAllGlobals()
    })

    beforeEach(() => {
        storage.clear()
        globalThis.localStorage = {
            getItem: (key: string) => storage.get(key) ?? null,
            setItem: (key: string, value: string) => storage.set(key, value),
            removeItem: (key: string) => storage.delete(key),
            clear: () => storage.clear(),
            key: () => null,
            get length() { return storage.size },
        } as Storage
        globalThis.window = { dispatchEvent: () => true } as unknown as Window & typeof globalThis
        vi.stubGlobal('navigator', {
            locks: { request: (_name: string, _options: LockOptions, callback: (lock: Lock) => unknown) => Promise.resolve(callback({} as Lock)) },
        })
        vi.restoreAllMocks()
    })

    it('does not restore tokens when logout clears the session during a refresh', async () => {
        saveAuthTokens(authResponse('qa-old-access', 'qa-old-refresh'))
        let resolveRefresh!: (response: Response) => void
        const fetchMock = vi.spyOn(globalThis, 'fetch').mockImplementation(() => new Promise<Response>((resolve) => {
            resolveRefresh = resolve
        }))

        const refresh = refreshAuthSession()
        expect(fetchMock).toHaveBeenCalledTimes(1)
        clearAuthTokens()
        resolveRefresh(new Response(JSON.stringify({
            statusCode: 200,
            message: 'ok',
            data: authResponse('qa-stale-access', 'qa-stale-refresh'),
        }), { status: 200, headers: { 'Content-Type': 'application/json' } }))

        await expect(refresh).resolves.toBe(false)
        expect(getAccessToken()).toBeNull()
        expect(getRefreshToken()).toBeNull()
        expect(getStoredUser()).toBeNull()
    })

    it('revokes and clears the session instead of refreshing when cross-tab locks are unavailable', async () => {
        vi.stubGlobal('navigator', {})
        saveAuthTokens(authResponse('qa-old-access', 'qa-old-refresh'))
        const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response(null, { status: 200 }))

        await expect(refreshAuthSession()).resolves.toBe(false)

        expect(fetchMock).toHaveBeenCalledTimes(1)
        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/v1/auth/logout')
        expect(JSON.parse(String(fetchMock.mock.calls[0]?.[1]?.body))).toEqual({ refreshToken: 'qa-old-refresh' })
        expect(getRefreshToken()).toBeNull()
    })
})
