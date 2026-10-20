import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { AuthApiError, loginUser, logoutUser, LOGOUT_TIMEOUT_MS, registerCandidate, requestForgotPassword, resetPassword, verifyPasswordResetOtp } from '../authApi'
import { refreshAuthSession } from '../sessionRefresh'
import { getAccessToken, saveAuthTokens } from '../tokenStorage'
import { AUTH_SESSION_LOCK_NAME } from '../authSessionLock'
import { API_REQUEST_TIMEOUT_MESSAGE, API_REQUEST_TIMEOUT_MS } from '../../../app/fetchWithTimeout'

afterEach(() => {
    vi.useRealTimers()
    vi.unstubAllGlobals()
})

function createSerializedLockManager(): LockManager {
    let previousRequest = Promise.resolve()

    return {
        request: vi.fn((_name, _options, callback) => {
            const previous = previousRequest
            let release!: () => void
            previousRequest = new Promise<void>((resolve) => { release = resolve })
            return previous.then(() => callback({} as Lock)).finally(release)
        }),
    } as unknown as LockManager
}

function createImmediateLockManager(): LockManager {
    return {
        request: vi.fn((_name, _options, callback) => Promise.resolve(callback({} as Lock))),
    } as unknown as LockManager
}

beforeEach(() => {
    vi.stubGlobal('navigator', { locks: createImmediateLockManager() })
})

describe('auth API fallback errors', () => {
    it('verifies a six-digit reset OTP and returns the short-lived grant', async () => {
        const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({
            statusCode: 200,
            message: 'OTP verified',
            data: { resetToken: 'one-time-reset-grant' },
        }), {
            status: 200,
            headers: { 'Content-Type': 'application/json' },
        }))
        vi.stubGlobal('fetch', fetchMock)

        await expect(verifyPasswordResetOtp({ email: 'candidate@example.invalid', otp: '123456' }))
            .resolves.toEqual({ resetToken: 'one-time-reset-grant' })

        expect(fetchMock).toHaveBeenCalledWith('/api/v1/auth/verify-reset-otp', expect.objectContaining({
            method: 'POST',
            body: JSON.stringify({ email: 'candidate@example.invalid', otp: '123456' }),
        }))
    })

    it('keeps an incorrect OTP retryable and does not expose a reset grant', async () => {
        vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(JSON.stringify({
            statusCode: 40005,
            message: 'Mã OTP không chính xác hoặc đã hết hạn. Bạn có thể nhập lại hoặc yêu cầu mã mới.',
        }), {
            status: 400,
            headers: { 'Content-Type': 'application/json' },
        })))

        await expect(verifyPasswordResetOtp({ email: 'candidate@example.invalid', otp: '000000' }))
            .rejects.toMatchObject({
                name: 'AuthApiError',
                status: 400,
                message: expect.stringContaining('nhập lại'),
            })
    })

    it('only sends the verified grant when setting a new password', async () => {
        const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({ statusCode: 200, message: 'ok' }), {
            status: 200,
            headers: { 'Content-Type': 'application/json' },
        }))
        vi.stubGlobal('fetch', fetchMock)

        await expect(resetPassword({ token: 'one-time-reset-grant', newPassword: 'NewPass123' })).resolves.toBeUndefined()

        expect(fetchMock).toHaveBeenCalledWith('/api/v1/auth/reset-password', expect.objectContaining({
            method: 'POST',
            body: JSON.stringify({ token: 'one-time-reset-grant', newPassword: 'NewPass123' }),
        }))
    })

    it('explains when password-reset email delivery is not configured', async () => {
        vi.stubGlobal(
            'fetch',
            vi.fn().mockResolvedValue(
                new Response(JSON.stringify({ statusCode: 50301 }), {
                    status: 503,
                    headers: { 'Content-Type': 'application/json' },
                }),
            ),
        )

        await expect(
            requestForgotPassword({ email: 'local-diagnostic@example.invalid' }),
        ).rejects.toMatchObject({
            name: 'AuthApiError',
            status: 503,
            message: 'Dịch vụ gửi email khôi phục mật khẩu chưa được cấu hình. Vui lòng liên hệ quản trị viên.',
        })
    })

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

    it('sends both captured tokens to revoke the session without refreshing after logout', async () => {
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
            .mockResolvedValueOnce(new Response(null, { status: 204 }))

        await expect(logoutUser()).resolves.toBeUndefined()

        expect(fetchMock).toHaveBeenCalledTimes(1)
        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/v1/auth/logout')
        expect(JSON.parse(String(fetchMock.mock.calls[0]?.[1]?.body))).toEqual({ refreshToken: 'qa-refresh-token' })
        expect(new Headers(fetchMock.mock.calls[0]?.[1]?.headers).get('Authorization'))
            .toBe('Bearer qa-expired-access-token')
        expect(storage.get('talentbridge_access_token')).toBeUndefined()
        expect(storage.get('talentbridge_refresh_token')).toBeUndefined()
    })

    it('clears local auth immediately and bounds an expired-token logout by one deadline', async () => {
        vi.useFakeTimers()
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
        const fetchMock = vi.fn((input: RequestInfo | URL, init?: RequestInit) => {
            if (input === '/api/v1/auth/logout') {
                return Promise.resolve(new Response('expired', { status: 401 }))
            }
            return new Promise<Response>((_resolve, reject) => {
                init?.signal?.addEventListener('abort', () => reject(new DOMException('Aborted', 'AbortError')), { once: true })
            })
        })
        vi.stubGlobal('fetch', fetchMock)

        const logout = logoutUser()
        expect(storage.get('talentbridge_access_token')).toBeUndefined()
        expect(storage.get('talentbridge_refresh_token')).toBeUndefined()

        const assertion = expect(logout).resolves.toBeUndefined()
        await vi.advanceTimersByTimeAsync(LOGOUT_TIMEOUT_MS)
        await assertion

        expect(fetchMock).toHaveBeenCalledTimes(1)
        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/v1/auth/logout')
        expect(JSON.parse(String(fetchMock.mock.calls[0]?.[1]?.body))).toEqual({ refreshToken: 'qa-refresh-token' })
        expect(new Headers(fetchMock.mock.calls[0]?.[1]?.headers).get('Authorization'))
            .toBe('Bearer qa-expired-access-token')
    })

    it('revokes the stored session when acquiring the cross-tab logout lock rejects', async () => {
        const storage = new Map<string, string>([
            ['talentbridge_access_token', 'qa-lock-failure-access-token'],
            ['talentbridge_refresh_token', 'qa-lock-failure-refresh-token'],
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
        vi.stubGlobal('navigator', {
            locks: { request: vi.fn().mockRejectedValue(new DOMException('Lock unavailable', 'AbortError')) },
        })
        const fetchMock = vi.fn().mockResolvedValue(new Response(null, { status: 204 }))
        vi.stubGlobal('fetch', fetchMock)

        await expect(logoutUser()).resolves.toBeUndefined()

        expect(fetchMock).toHaveBeenCalledTimes(1)
        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/v1/auth/logout')
        expect(new Headers(fetchMock.mock.calls[0]?.[1]?.headers).get('Authorization'))
            .toBe('Bearer qa-lock-failure-access-token')
        expect(JSON.parse(String(fetchMock.mock.calls[0]?.[1]?.body))).toEqual({
            refreshToken: 'qa-lock-failure-refresh-token',
        })
        expect(storage.has('talentbridge_access_token')).toBe(false)
        expect(storage.has('talentbridge_refresh_token')).toBe(false)
    })

    it('bounds lock-acquisition timeout and still attempts server revocation', async () => {
        vi.useFakeTimers()
        const storage = new Map<string, string>([
            ['talentbridge_access_token', 'qa-lock-timeout-access-token'],
            ['talentbridge_refresh_token', 'qa-lock-timeout-refresh-token'],
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
        const lockManager = {
            request: vi.fn((_name, options) => new Promise((_resolve, reject) => {
                const signal = (options as { signal?: AbortSignal }).signal
                signal?.addEventListener('abort', () => reject(signal.reason), { once: true })
            })),
        }
        vi.stubGlobal('navigator', { locks: lockManager })
        const fetchMock = vi.fn().mockResolvedValue(new Response(null, { status: 204 }))
        vi.stubGlobal('fetch', fetchMock)

        const logout = logoutUser()
        const assertion = expect(logout).resolves.toBeUndefined()
        await vi.advanceTimersByTimeAsync(LOGOUT_TIMEOUT_MS + 8_000)
        await assertion

        expect(lockManager.request).toHaveBeenCalledTimes(1)
        expect(fetchMock).toHaveBeenCalledTimes(1)
        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/v1/auth/logout')
        expect(new Headers(fetchMock.mock.calls[0]?.[1]?.headers).get('Authorization'))
            .toBe('Bearer qa-lock-timeout-access-token')
        expect(storage.has('talentbridge_access_token')).toBe(false)
        expect(storage.has('talentbridge_refresh_token')).toBe(false)
    })

    it('serializes refresh and logout before revoking the rotated shared session pair', async () => {
        const storage = new Map<string, string>()
        vi.stubGlobal('localStorage', {
            getItem: (key: string) => storage.get(key) ?? null,
            setItem: (key: string, value: string) => storage.set(key, value),
            removeItem: (key: string) => storage.delete(key),
            clear: () => storage.clear(),
            key: () => null,
            get length() { return storage.size },
        } as Storage)
        vi.stubGlobal('window', { dispatchEvent: () => true })
        const lockManager = createSerializedLockManager()
        vi.stubGlobal('navigator', { locks: lockManager })
        saveAuthTokens({
            accessToken: 'qa-old-access-token',
            refreshToken: 'qa-old-refresh-token',
            tokenType: 'Bearer',
            expiresInMs: 60_000,
            user: { id: 1, email: 'qa@example.invalid', fullName: 'QA', status: 'ACTIVE', roles: ['ROLE_CANDIDATE'] },
        })

        let resolveRefresh!: (response: Response) => void
        let logoutCalls = 0
        const fetchMock = vi.fn((input: RequestInfo | URL, init?: RequestInit) => {
            void init
            if (input === '/api/v1/auth/refresh-token') {
                return new Promise<Response>((resolve) => { resolveRefresh = resolve })
            }
            if (input === '/api/v1/auth/logout') {
                logoutCalls += 1
                return Promise.resolve(new Response(null, { status: 204 }))
            }
            return Promise.reject(new Error('Unexpected request'))
        })
        vi.stubGlobal('fetch', fetchMock)

        const refresh = refreshAuthSession()
        await vi.waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(1))
        const logout = logoutUser()
        expect(getAccessToken()).toBeNull()
        expect(storage.get('talentbridge_access_token')).toBe('qa-old-access-token')
        expect(storage.get('talentbridge_refresh_token')).toBe('qa-old-refresh-token')
        expect(logoutCalls).toBe(0)

        resolveRefresh(new Response(JSON.stringify({
            statusCode: 200,
            message: 'ok',
            data: {
                accessToken: 'qa-new-access-token',
                refreshToken: 'qa-new-refresh-token',
                tokenType: 'Bearer',
                expiresInMs: 60_000,
                user: { id: 1, email: 'qa@example.invalid', fullName: 'QA', status: 'ACTIVE', roles: ['ROLE_CANDIDATE'] },
            },
        }), { status: 200, headers: { 'Content-Type': 'application/json' } }))

        await expect(refresh).resolves.toBe(false)
        await expect(logout).resolves.toBeUndefined()

        const logoutRequests = fetchMock.mock.calls.filter(([input]) => input === '/api/v1/auth/logout')
        expect(logoutRequests).toHaveLength(1)
        expect(new Headers(logoutRequests[0]?.[1]?.headers).get('Authorization')).toBe('Bearer qa-new-access-token')
        expect(JSON.parse(String(logoutRequests[0]?.[1]?.body))).toEqual({ refreshToken: 'qa-new-refresh-token' })
        expect(storage.get('talentbridge_access_token')).toBeUndefined()
        expect(storage.get('talentbridge_refresh_token')).toBeUndefined()
        expect(storage.get('talentbridge_logout_intent')).toBeUndefined()
        expect(lockManager.request).toHaveBeenCalledWith(
            AUTH_SESSION_LOCK_NAME,
            expect.objectContaining({ mode: 'exclusive' }),
            expect.any(Function),
        )
    })
})
