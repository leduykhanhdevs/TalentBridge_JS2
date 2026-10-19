import { afterEach, describe, expect, it, vi } from 'vitest'
import { AuthApiError, loginUser, logoutUser, LOGOUT_TIMEOUT_MS, registerCandidate } from '../authApi'
import { API_REQUEST_TIMEOUT_MESSAGE, API_REQUEST_TIMEOUT_MS } from '../../../app/fetchWithTimeout'

afterEach(() => {
    vi.useRealTimers()
    vi.unstubAllGlobals()
})

describe('auth API fallback errors', () => {
    it('shows the HTTP status when registration receives a non-JSON gateway error', async () => {
        vi.stubGlobal(
            'fetch',
            vi.fn().mockResolvedValue(
                new Response('<html>Bad Gateway</html>', {
                    status: 502,
                    headers: { 'Content-Type': 'text/html' },
                }),
            ),
        )

        await expect(
            registerCandidate({
                fullName: 'Local Diagnostic',
                email: 'local-diagnostic@example.invalid',
                password: 'not-a-real-account-password',
                role: 'ROLE_CANDIDATE',
            }),
        ).rejects.toMatchObject({
            name: 'AuthApiError',
            status: 502,
            message: expect.stringContaining('HTTP 502'),
        })
    })

    it('explains rate limiting for login when the response body has no message', async () => {
        vi.stubGlobal(
            'fetch',
            vi.fn().mockResolvedValue(
                new Response(JSON.stringify({ statusCode: 429 }), {
                    status: 429,
                    headers: { 'Content-Type': 'application/json' },
                }),
            ),
        )

        try {
            await loginUser({ email: 'local-diagnostic@example.invalid', password: 'unused' })
            throw new Error('Expected login to fail')
        } catch (error) {
            expect(error).toBeInstanceOf(AuthApiError)
            expect(error).toMatchObject({ status: 429 })
            expect((error as Error).message).toContain('HTTP 429')
        }
    })

    it('turns a stalled login request into a readable timeout error', async () => {
        vi.useFakeTimers()
        vi.stubGlobal('fetch', vi.fn((_input: RequestInfo | URL, init?: RequestInit) => new Promise<Response>((_resolve, reject) => {
            init?.signal?.addEventListener('abort', () => reject(new DOMException('Aborted', 'AbortError')), { once: true })
        })))

        const login = loginUser({ email: 'qa-user@example.invalid', password: 'fixture-only' })
        const assertion = expect(login).rejects.toMatchObject({
            name: 'AuthApiError',
            status: 0,
            message: API_REQUEST_TIMEOUT_MESSAGE,
        })
        await vi.advanceTimersByTimeAsync(API_REQUEST_TIMEOUT_MS)

        await assertion
    })

    it('reports a stalled login response body as a timeout instead of an invalid response', async () => {
        vi.useFakeTimers()
        vi.stubGlobal('fetch', vi.fn((_input: RequestInfo | URL, init?: RequestInit) => {
            const body = new ReadableStream<Uint8Array>({
                start(controller) {
                    init?.signal?.addEventListener('abort', () => {
                        controller.error(init?.signal?.reason)
                    }, { once: true })
                },
            })
            return Promise.resolve(new Response(body, { status: 200 }))
        }))

        const login = loginUser({ email: 'qa-user@example.invalid', password: 'fixture-only' })
        const assertion = expect(login).rejects.toMatchObject({
            name: 'AuthApiError',
            status: 0,
            message: API_REQUEST_TIMEOUT_MESSAGE,
        })
        await vi.advanceTimersByTimeAsync(API_REQUEST_TIMEOUT_MS)

        await assertion
    })

    it('clears the browser session after a stalled best-effort server logout', async () => {
        vi.useFakeTimers()
        const storage = new Map<string, string>([['talentbridge_access_token', 'qa-logout-token']])
        vi.stubGlobal('localStorage', {
            getItem: (key: string) => storage.get(key) ?? null,
            setItem: (key: string, value: string) => storage.set(key, value),
            removeItem: (key: string) => storage.delete(key),
            clear: () => storage.clear(),
            key: () => null,
            get length() { return storage.size },
        } as Storage)
        vi.stubGlobal('window', { dispatchEvent: () => true })
        const fetchMock = vi.fn((_input: RequestInfo | URL, init?: RequestInit) => new Promise<Response>((_resolve, reject) => {
            init?.signal?.addEventListener('abort', () => reject(new DOMException('Aborted', 'AbortError')), { once: true })
        }))
        vi.stubGlobal('fetch', fetchMock)

        const logout = logoutUser()
        await vi.advanceTimersByTimeAsync(LOGOUT_TIMEOUT_MS)
        await expect(logout).resolves.toBeUndefined()

        expect(storage.get('talentbridge_access_token')).toBeUndefined()
        expect(fetchMock).toHaveBeenCalledTimes(1)
        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/v1/auth/logout')
        expect(new Headers(fetchMock.mock.calls[0]?.[1]?.headers).get('Authorization')).toBe('Bearer qa-logout-token')
    })

    it('refreshes an expired access token before revoking the server session on logout', async () => {
        const storage = new Map<string, string>([
            ['talentbridge_access_token', 'qa-expired-access-token'],
            ['talentbridge_refresh_token', 'qa-refresh-token'],
        ])
        vi.stubGlobal('localStorage', {
            getItem: (key: string) => storage.get(key) ?? null,
            setItem: (key: string, value: string) => storage.set(key, value),
            removeItem: (key: string) => storage.delete(key),
            clear: () => storage.clear(),
            key: () => null,
            get length() { return storage.size },
        } as Storage)
        vi.stubGlobal('window', { dispatchEvent: () => true })

        const fetchMock = vi.spyOn(globalThis, 'fetch')
            .mockResolvedValueOnce(new Response('expired', { status: 401 }))
            .mockResolvedValueOnce(new Response(JSON.stringify({
                statusCode: 200,
                message: 'ok',
                data: {
                    accessToken: 'qa-fresh-access-token',
                    refreshToken: 'qa-rotated-refresh-token',
                    tokenType: 'Bearer',
                    expiresInMs: 60_000,
                    user: { id: 1, email: 'qa@example.invalid', fullName: 'QA', status: 'ACTIVE', roles: ['ROLE_CANDIDATE'] },
                },
            }), { status: 200, headers: { 'Content-Type': 'application/json' } }))
            .mockResolvedValueOnce(new Response(null, { status: 204 }))

        await expect(logoutUser()).resolves.toBeUndefined()

        expect(fetchMock).toHaveBeenCalledTimes(3)
        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/v1/auth/logout')
        expect(fetchMock.mock.calls[1]?.[0]).toBe('/api/v1/auth/refresh-token')
        expect(fetchMock.mock.calls[2]?.[0]).toBe('/api/v1/auth/logout')
        expect(new Headers(fetchMock.mock.calls[2]?.[1]?.headers).get('Authorization'))
            .toBe('Bearer qa-fresh-access-token')
        expect(storage.get('talentbridge_access_token')).toBeUndefined()
        expect(storage.get('talentbridge_refresh_token')).toBeUndefined()
    })
})
