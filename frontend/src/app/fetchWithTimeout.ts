export const API_REQUEST_TIMEOUT_MS = 10_000
export const API_REQUEST_TIMEOUT_MESSAGE = 'Máy chủ phản hồi quá lâu. Vui lòng thử lại.'

export class FetchTimeoutError extends Error {
    constructor() {
        super(API_REQUEST_TIMEOUT_MESSAGE)
        this.name = 'FetchTimeoutError'
    }
}

function cleanupDeadline(
    timeoutId: ReturnType<typeof globalThis.setTimeout>,
    sourceSignals: AbortSignal[],
    abortListeners: Map<AbortSignal, () => void>,
): void {
    globalThis.clearTimeout(timeoutId)
    for (const signal of sourceSignals) {
        const onAbort = abortListeners.get(signal)
        if (onAbort) signal.removeEventListener('abort', onAbort)
    }
}

function attachDeadlineToResponseBody(
    response: Response,
    sourceSignals: AbortSignal[],
    abortListeners: Map<AbortSignal, () => void>,
    timeoutId: ReturnType<typeof globalThis.setTimeout>,
    didTimeOut: () => boolean,
): Response {
    if (!response.body || [204, 205, 304].includes(response.status)) {
        cleanupDeadline(timeoutId, sourceSignals, abortListeners)
        return response
    }

    const reader = response.body.getReader()
    let finished = false

    const body = new ReadableStream<Uint8Array>({
        async pull(streamController) {
            try {
                if (didTimeOut()) throw new FetchTimeoutError()
                const { done, value } = await reader.read()
                if (done) {
                    if (didTimeOut()) throw new FetchTimeoutError()
                    finished = true
                    streamController.close()
                    cleanupDeadline(timeoutId, sourceSignals, abortListeners)
                    reader.releaseLock()
                    return
                }
                streamController.enqueue(value)
            } catch (error) {
                finished = true
                streamController.error(didTimeOut() ? new FetchTimeoutError() : error)
                cleanupDeadline(timeoutId, sourceSignals, abortListeners)
                try { reader.releaseLock() } catch { /* The underlying stream may already be errored. */ }
            }
        },
        async cancel(reason) {
            if (finished) return
            finished = true
            cleanupDeadline(timeoutId, sourceSignals, abortListeners)
            try {
                await reader.cancel(reason)
            } finally {
                try { reader.releaseLock() } catch { /* The reader may already be released. */ }
            }
        },
    }, { highWaterMark: 0 })

    const wrapped = new Response(body, {
        headers: response.headers,
        status: response.status,
        statusText: response.statusText,
    })
    Object.defineProperties(wrapped, {
        url: { configurable: true, value: response.url },
        redirected: { configurable: true, value: response.redirected },
        type: { configurable: true, value: response.type },
    })
    return wrapped
}

/** Bounds how long a browser API request can remain pending without discarding caller cancellation. */
export async function fetchWithTimeout(
    input: RequestInfo | URL,
    init: RequestInit = {},
    timeoutMs = API_REQUEST_TIMEOUT_MS,
): Promise<Response> {
    const controller = new AbortController()
    const sourceSignals = Array.from(new Set([
        init.signal,
        typeof Request !== 'undefined' && input instanceof Request ? input.signal : null,
    ].filter((signal): signal is AbortSignal => signal !== null && signal !== undefined)))
    const abortListeners = new Map<AbortSignal, () => void>()

    for (const signal of sourceSignals) {
        if (signal.aborted) {
            controller.abort(signal.reason)
            break
        }
        const onAbort = () => controller.abort(signal.reason)
        abortListeners.set(signal, onAbort)
        signal.addEventListener('abort', onAbort, { once: true })
    }

    let timedOut = false
    const timeoutId = globalThis.setTimeout(() => {
        timedOut = true
        controller.abort(new FetchTimeoutError())
        cleanupDeadline(timeoutId, sourceSignals, abortListeners)
    }, timeoutMs)

    try {
        const response = await fetch(input, { ...init, signal: controller.signal })
        return attachDeadlineToResponseBody(
            response,
            sourceSignals,
            abortListeners,
            timeoutId,
            () => timedOut,
        )
    } catch (error) {
        cleanupDeadline(timeoutId, sourceSignals, abortListeners)
        if (timedOut) throw new FetchTimeoutError()
        throw error
    }
}
