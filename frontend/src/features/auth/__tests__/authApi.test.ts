import { afterEach, describe, expect, it, vi } from 'vitest'
import { AuthApiError, loginUser, registerCandidate } from '../authApi'

afterEach(() => {
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
})
