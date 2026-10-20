import type { ApiResponse, AuthResponse } from './authTypes'
import {
    clearAuthTokens,
    getAuthMutationVersion,
    getStoredAccessToken,
    getStoredRefreshToken,
    isLogoutPending,
    saveAuthTokens,
} from './tokenStorage'
import { fetchWithTimeout } from '../../app/fetchWithTimeout'
import { isAuthSessionLockAvailable, withAuthSessionLock } from './authSessionLock'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || '/api/v1').replace(/\/$/, '')
export const REFRESH_TIMEOUT_MS = 8_000

let refreshInFlight: Promise<RefreshResult> | null = null

interface RefreshResult { refreshed: boolean }

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

async function refreshSession(deadlineAt: number): Promise<RefreshResult> {
    const authMutationVersion = getAuthMutationVersion()
    const refreshToken = getStoredRefreshToken()
    if (!refreshToken) {
        if (
            !isLogoutPending() &&
            getAuthMutationVersion() === authMutationVersion &&
            getStoredRefreshToken() === null
        ) {
            clearAuthTokens()
        }
        return { refreshed: false }
    }

    try {
        const remainingMs = Math.max(0, deadlineAt - Date.now())
        if (remainingMs <= 0) return { refreshed: false }
        const response = await fetchWithTimeout(`${API_BASE_URL}/auth/refresh-token`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json; charset=UTF-8' },
            body: JSON.stringify({ refreshToken }),
        }, remainingMs)

        if (response.ok) {
            const body = await response.json() as ApiResponse<AuthResponse>
            if (!isAuthResponse(body?.data)) return { refreshed: false }
            const mayPersist =
                getAuthMutationVersion() === authMutationVersion &&
                getStoredRefreshToken() === refreshToken
            if (mayPersist) saveAuthTokens(body.data, { preserveLogoutIntent: true })
            return { refreshed: mayPersist && !isLogoutPending() }
        }

        if (response.status === 400 || response.status === 401) {
            // Another tab may have rotated the refresh token before this tab acquired the lock.
            const currentRefreshToken = getStoredRefreshToken()
            if (currentRefreshToken && currentRefreshToken !== refreshToken && getStoredAccessToken()) {
                return { refreshed: true }
            }
            if (
                !isLogoutPending() &&
                getAuthMutationVersion() === authMutationVersion &&
                currentRefreshToken === refreshToken
            ) {
                clearAuthTokens()
            }
        }

        return { refreshed: false }
    } catch {
        return { refreshed: false }
    }
}

export function refreshAuthSession(): Promise<boolean> {
    // Without Web Locks this browser cannot prevent a refresh/logout race across tabs.
    // Revoke the existing session best-effort and require a fresh sign-in instead of
    // rotating tokens unsafely. The server verifies the refresh token before revocation.
    if (!isAuthSessionLockAvailable()) {
        const accessToken = getStoredAccessToken()
        const refreshToken = getStoredRefreshToken()
        clearAuthTokens()
        if (accessToken || refreshToken) {
            void fetchWithTimeout(`${API_BASE_URL}/auth/logout`, {
                method: 'POST',
                headers: {
                    ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
                    ...(refreshToken ? { 'Content-Type': 'application/json; charset=UTF-8' } : {}),
                },
                ...(refreshToken ? { body: JSON.stringify({ refreshToken }) } : {}),
                keepalive: true,
            }, 2_500).then((response) => response.body?.cancel()).catch(() => undefined)
        }
        return Promise.resolve(false)
    }
    if (refreshInFlight) return refreshInFlight.then(({ refreshed }) => refreshed)

    const deadlineAt = Date.now() + REFRESH_TIMEOUT_MS
    const operation = withAuthSessionLock(async () => {
        if (isLogoutPending()) return { refreshed: false }
        return refreshSession(deadlineAt)
    }, REFRESH_TIMEOUT_MS).catch(() => ({ refreshed: false }))
    refreshInFlight = operation
    void operation.finally(() => {
        if (refreshInFlight === operation) refreshInFlight = null
    })
    return operation.then(({ refreshed }) => refreshed && !isLogoutPending())
}
