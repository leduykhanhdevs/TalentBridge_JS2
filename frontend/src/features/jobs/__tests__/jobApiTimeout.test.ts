import { afterEach, describe, expect, it, vi } from 'vitest'
import { API_REQUEST_TIMEOUT_MS, FetchTimeoutError } from '../../../app/fetchWithTimeout'
import { getJobDetail } from '../jobApi'
import { searchJobs } from '../jobSearchApi'

afterEach(() => {
    vi.useRealTimers()
    vi.unstubAllGlobals()
})

function stubStalledJsonResponse() {
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
}

describe('job API response-body timeouts', () => {
    it('preserves a search response timeout instead of reporting missing job data', async () => {
        vi.useFakeTimers()
        stubStalledJsonResponse()

        const request = searchJobs()
        const assertion = expect(request).rejects.toBeInstanceOf(FetchTimeoutError)
        await vi.advanceTimersByTimeAsync(API_REQUEST_TIMEOUT_MS)

        await assertion
    })

    it('preserves a job-detail response timeout instead of reporting missing job data', async () => {
        vi.useFakeTimers()
        stubStalledJsonResponse()

        const request = getJobDetail(42)
        const assertion = expect(request).rejects.toBeInstanceOf(FetchTimeoutError)
        await vi.advanceTimersByTimeAsync(API_REQUEST_TIMEOUT_MS)

        await assertion
    })
})
