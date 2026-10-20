import { afterEach, describe, expect, it, vi } from 'vitest'
import { askTalentBridge, AssistantApiError } from '../assistantApi'

afterEach(() => {
    vi.useRealTimers()
    vi.unstubAllGlobals()
})

describe('assistant API request deadlines', () => {
    it('reports a stalled response body as a timeout instead of malformed JSON', async () => {
        vi.useFakeTimers()
        vi.stubGlobal('localStorage', {
            getItem: (key: string) => key === 'talentbridge_access_token' ? 'qa-assistant-token' : null,
        } as Storage)
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

        const request = askTalentBridge('Cách ứng tuyển?', [])
        const assertion = expect(request).rejects.toMatchObject({
            name: 'AssistantApiError',
            status: 408,
            message: 'Trợ lý phản hồi quá chậm. Vui lòng thử lại sau một lát.',
        } satisfies Partial<AssistantApiError>)
        await vi.advanceTimersByTimeAsync(15_000)

        await assertion
    })
})
