import type { ApiResponse, AuthResponse } from './authTypes'
import {
    clearAuthTokens,
    getAccessToken,
    getRefreshToken,
    saveAuthTokens,
} from './tokenStorage'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || '/api/v1').replace(/\/$/, '')
const REFRESH_TIMEOUT_MS = 8_000

let refreshInFlight: Promise<boolean> | null = null

function isAuthResponse(value: unknown): value is AuthResponse {
    if (typeof value !== 'object' || value === null || Array.isArray(value)) {
        return false
    }

    const authResponse = value as Partial<AuthResponse>
    return (
        typeof authResponse.accessToken === 'string' &&
        typeof authResponse.refreshToken === 'string' &&
        typeof authResponse.tokenType === 'string' &&
        typeof authResponse.expiresInMs === 'number' &&
        typeof authResponse.user === 'object' &&
        authResponse.user !== null
    )
}

async function refreshSession(): Promise<boolean> {
    const refreshToken = getRefreshToken()
    if (!refreshToken) {
        clearAuthTokens()
        return false
    }

    const controller = new AbortController()
    const timeoutId = window.setTimeout(() => controller.abort(), REFRESH_TIMEOUT_MS)

    try {
        const response = await fetch(`${API_BASE_URL}/auth/refresh-token`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json; charset=UTF-8' },
            body: JSON.stringify({ refreshToken }),
            signal: controller.signal,
        })

        if (response.ok) {
            const body = await response.json() as ApiResponse<AuthResponse>
            if (!isAuthResponse(body?.data)) return false
            saveAuthTokens(body.data)
            return true
        }

        if (response.status === 400 || response.status === 401) {
            // Another tab may have rotated the refresh token while this request was in flight.
            const currentRefreshToken = getRefreshToken()
            if (currentRefreshToken && currentRefreshToken !== refreshToken && getAccessToken()) {
                return true
            }
            clearAuthTokens()
        }

        return false
    } catch {
        return false
    } finally {
        window.clearTimeout(timeoutId)
    }
}

export function refreshAuthSession(): Promise<boolean> {
    if (refreshInFlight) return refreshInFlight

    const operation = refreshSession()
    refreshInFlight = operation
    void operation.finally(() => {
        if (refreshInFlight === operation) refreshInFlight = null
    })
    return operation
}
