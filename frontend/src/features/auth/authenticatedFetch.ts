import { refreshAuthSession } from './sessionRefresh'
import { getAccessToken } from './tokenStorage'

function withBearerToken(init: RequestInit, token: string | null): RequestInit {
    const headers = new Headers(init.headers)
    if (token) headers.set('Authorization', `Bearer ${token}`)
    return { ...init, headers }
}

/** Sends an authenticated request and retries once after rotating an expired access token. */
export async function authenticatedFetch(
    input: RequestInfo | URL,
    init: RequestInit = {},
): Promise<Response> {
    const response = await fetch(input, withBearerToken(init, getAccessToken()))
    if (response.status !== 401) return response

    const refreshed = await refreshAuthSession()
    if (!refreshed || init.signal?.aborted) return response

    return fetch(input, withBearerToken(init, getAccessToken()))
}
