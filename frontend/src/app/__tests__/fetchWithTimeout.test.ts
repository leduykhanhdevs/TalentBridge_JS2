import { afterEach, describe, expect, it, vi } from 'vitest'
import { FetchTimeoutError, fetchWithTimeout } from '../fetchWithTimeout'

afterEach(() => {
    vi.useRealTimers()
    vi.unstubAllGlobals()
})

describe('fetchWithTimeout', () => {
    it('aborts a request that exceeds its deadline and reports a readable timeout', async () => {
        vi.useFakeTimers()
        const requestSignals: AbortSignal[] = []
        vi.stubGlobal('fetch', vi.fn((_input: RequestInfo | URL, init?: RequestInit) => {
            if (init?.signal) requestSignals.push(init.signal)
            return new Promise<Response>((_resolve, reject) => {
                init?.signal?.addEventListener('abort', () => reject(new DOMException('Aborted', 'AbortError')), { once: true })
            })
        }))

        const request = fetchWithTimeout('/api/v1/jobs', {}, 25)
        const assertion = expect(request).rejects.toMatchObject({
            name: 'FetchTimeoutError',
            message: 'Máy chủ phản hồi quá lâu. Vui lòng thử lại.',
        })
        await vi.advanceTimersByTimeAsync(25)

        await assertion
        expect(requestSignals[0]?.aborted).toBe(true)
    })

    it('preserves caller cancellation instead of converting it to a timeout', async () => {
        const caller = new AbortController()
        vi.stubGlobal('fetch', vi.fn((_input: RequestInfo | URL, init?: RequestInit) => new Promise<Response>((_resolve, reject) => {
            init?.signal?.addEventListener('abort', () => reject(new DOMException('Aborted', 'AbortError')), { once: true })
        })))

        const request = fetchWithTimeout('/api/v1/jobs', { signal: caller.signal }, 1000)
        caller.abort()

        await expect(request).rejects.not.toBeInstanceOf(FetchTimeoutError)
    })

    it('keeps the deadline active while a response body is stalled', async () => {
        vi.useFakeTimers()
        vi.stubGlobal('fetch', vi.fn((_input: RequestInfo | URL, init?: RequestInit) => {
            const body = new ReadableStream<Uint8Array>({
                start(controller) {
                    init?.signal?.addEventListener('abort', () => {
                        controller.error(init?.signal?.reason)
                    }, { once: true })
                },
            })
            return Promise.resolve(new Response(body, {
                status: 200,
                headers: { 'Content-Type': 'application/json' },
            }))
        }))

        const response = await fetchWithTimeout('/api/v1/jobs', {}, 25)
        const bodyAssertion = expect(response.json()).rejects.toBeInstanceOf(FetchTimeoutError)
        await vi.advanceTimersByTimeAsync(25)
        await bodyAssertion
    })

    it('clears the deadline after consuming a successful response body', async () => {
        vi.useFakeTimers()
        const requestSignals: AbortSignal[] = []
        vi.stubGlobal('fetch', vi.fn((_input: RequestInfo | URL, init?: RequestInit) => {
            if (init?.signal) requestSignals.push(init.signal)
            return Promise.resolve(new Response('{"ok":true}', {
                status: 200,
                headers: { 'Content-Type': 'application/json' },
            }))
        }))

        const response = await fetchWithTimeout('/api/v1/jobs', {}, 25)
        await expect(response.json()).resolves.toEqual({ ok: true })
        await vi.advanceTimersByTimeAsync(25)

        expect(requestSignals[0]?.aborted).toBe(false)
    })
})
