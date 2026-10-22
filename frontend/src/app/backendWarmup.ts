import { fetchWithTimeout } from './fetchWithTimeout'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || '/api/v1').replace(/\/$/, '')
export const BACKEND_WARMUP_TIMEOUT_MS = 70_000
export const BACKEND_WARMUP_CACHE_TTL_MS = 12 * 60_000
const BACKEND_WARMUP_ATTEMPT_TIMEOUT_MS = 12_000
const BACKEND_WARMUP_RETRY_DELAY_MS = 8_000

let warmupRequest: Promise<boolean> | undefined
let warmupCompletedAt: number | undefined

/** Wakes the API without querying business data; Render Free can need about a minute. */
export function warmUpBackend(): Promise<boolean> {
    if (warmupRequest) return warmupRequest
    if (warmupCompletedAt !== undefined && Date.now() - warmupCompletedAt < BACKEND_WARMUP_CACHE_TTL_MS) {
        return Promise.resolve(true)
    }

    warmupRequest = waitForBackend()
        .finally(() => { warmupRequest = undefined })

    return warmupRequest
}

async function waitForBackend(): Promise<boolean> {
    const deadline = Date.now() + BACKEND_WARMUP_TIMEOUT_MS

    while (Date.now() < deadline) {
        const remainingMs = deadline - Date.now()
        try {
            const response = await fetchWithTimeout(`${API_BASE_URL}/health`, {
                headers: { Accept: 'application/json' },
                cache: 'no-store',
            }, Math.min(BACKEND_WARMUP_ATTEMPT_TIMEOUT_MS, remainingMs))

            if (response.ok) {
                await response.body?.cancel().catch(() => undefined)
                warmupCompletedAt = Date.now()
                return true
            }

            await response.body?.cancel().catch(() => undefined)
        } catch {
            // A sleeping Render Free service can reject health checks while it starts.
        }

        const retryDelayMs = Math.min(BACKEND_WARMUP_RETRY_DELAY_MS, deadline - Date.now())
        if (retryDelayMs > 0) {
            await new Promise<void>((resolve) => globalThis.setTimeout(resolve, retryDelayMs))
        }
    }

    warmupCompletedAt = undefined
    return false
}
