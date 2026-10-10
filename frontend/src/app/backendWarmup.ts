const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || '/api/v1').replace(/\/$/, '')

let warmupRequest: Promise<void> | undefined

/** Wakes the API in the background without querying business data. */
export function warmUpBackend(): Promise<void> {
    if (warmupRequest) return warmupRequest

    warmupRequest = fetch(`${API_BASE_URL}/health`, {
        headers: { Accept: 'application/json' },
        cache: 'no-store',
    })
        .then((response) => {
            if (!response.ok) throw new Error(`Backend warm-up returned ${response.status}`)
        })
        .catch(() => {
            warmupRequest = undefined
        })

    return warmupRequest
}
