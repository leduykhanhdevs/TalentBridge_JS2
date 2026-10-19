import { refreshAuthSession } from './sessionRefresh'
import { getAccessToken } from './tokenStorage'
import { API_REQUEST_TIMEOUT_MS, fetchWithTimeout } from '../../app/fetchWithTimeout'

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
    const response = await fetchWithTimeout(input, withBearerToken(init, getAccessToken()), timeoutMs)
    if (response.status !== 401) return response

    const refreshed = await refreshAuthSession()
    if (!refreshed || init.signal?.aborted) return response

    return fetchWithTimeout(input, withBearerToken(init, getAccessToken()), timeoutMs)
}
