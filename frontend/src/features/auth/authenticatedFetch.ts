import { refreshAuthSession } from './sessionRefresh'
import { getAccessToken } from './tokenStorage'
import { API_REQUEST_TIMEOUT_MS, FetchTimeoutError, fetchWithTimeout } from '../../app/fetchWithTimeout'

function withBearerToken(init: RequestInit, token: string | null): RequestInit {
    const headers = new Headers(init.headers)
    if (token) headers.set('Authorization', `Bearer ${token}`)
    return { ...init, headers }
}

/** Sends an authenticated request and retries once after rotating an expired access token. */
export async function authenticatedFetch(
    input: RequestInfo | URL,
    init: RequestInit = {},
    timeoutMs = API_REQUEST_TIMEOUT_MS,
): Promise<Response> {
    const deadlineAt = Date.now() + timeoutMs
    const getRemainingTimeoutMs = () => {
        const remainingMs = deadlineAt - Date.now()
        if (remainingMs <= 0) throw new FetchTimeoutError()
        return remainingMs
    }

    const response = await fetchWithTimeout(
        input,
        withBearerToken(init, getAccessToken()),
        getRemainingTimeoutMs(),
    )
    if (response.status !== 401) return response

    const remainingMs = getRemainingTimeoutMs()
    const refresh = refreshAuthSession()
    const refreshed = await new Promise<boolean>((resolve, reject) => {
        let settled = false
        const timeoutId = globalThis.setTimeout(() => {
            settled = true
            reject(new FetchTimeoutError())
        }, remainingMs)

        refresh.then(
            (value) => {
                if (settled) return
                settled = true
                globalThis.clearTimeout(timeoutId)
                resolve(value)
            },
            (error: unknown) => {
                if (settled) return
                settled = true
                globalThis.clearTimeout(timeoutId)
                reject(error)
            },
        )
    })
    if (!refreshed || init.signal?.aborted) return response

    return fetchWithTimeout(input, withBearerToken(init, getAccessToken()), getRemainingTimeoutMs())
}
