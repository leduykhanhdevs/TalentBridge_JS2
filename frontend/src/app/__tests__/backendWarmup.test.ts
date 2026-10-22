import { afterEach, describe, expect, it, vi } from 'vitest'

afterEach(() => {
    vi.useRealTimers()
    vi.unstubAllGlobals()
    vi.resetModules()
})

describe('warmUpBackend', () => {
    it('retries sleeping responses, deduplicates concurrent checks, and expires readiness', async () => {
        vi.useFakeTimers()
        const fetchMock = vi.fn()
            .mockResolvedValueOnce(new Response(null, { status: 503 }))
            .mockImplementation(() => Promise.resolve(new Response(null, { status: 200 })))
        vi.stubGlobal('fetch', fetchMock)
        const { BACKEND_WARMUP_CACHE_TTL_MS, BACKEND_WARMUP_TIMEOUT_MS, warmUpBackend } = await import('../backendWarmup')

        const firstWarmup = warmUpBackend()
        const concurrentWarmup = warmUpBackend()
        const readyAssertion = expect(firstWarmup).resolves.toBe(true)
        await vi.advanceTimersByTimeAsync(BACKEND_WARMUP_TIMEOUT_MS)
        await readyAssertion
        await expect(concurrentWarmup).resolves.toBe(true)
        expect(fetchMock).toHaveBeenCalledTimes(2)

        await warmUpBackend()
        expect(fetchMock).toHaveBeenCalledTimes(2)

        await vi.advanceTimersByTimeAsync(BACKEND_WARMUP_CACHE_TTL_MS)
        await warmUpBackend()
        expect(fetchMock).toHaveBeenCalledTimes(3)
    })

    it('returns false after the backend stays unavailable for the warm-up window', async () => {
        vi.useFakeTimers()
        const fetchMock = vi.fn().mockResolvedValue(new Response(null, { status: 503 }))
        vi.stubGlobal('fetch', fetchMock)
        const { BACKEND_WARMUP_TIMEOUT_MS, warmUpBackend } = await import('../backendWarmup')

        const warmup = warmUpBackend()
        const assertion = expect(warmup).resolves.toBe(false)
        await vi.advanceTimersByTimeAsync(BACKEND_WARMUP_TIMEOUT_MS)
        await assertion
        expect(fetchMock.mock.calls.length).toBeGreaterThan(1)
    })
})
