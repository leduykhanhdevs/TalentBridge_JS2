import type { AuthResponse, UserResponse } from './authTypes'

const ACCESS_TOKEN_KEY = 'talentbridge_access_token'
const REFRESH_TOKEN_KEY = 'talentbridge_refresh_token'
const USER_KEY = 'talentbridge_auth_user'
const LOGOUT_INTENT_KEY = 'talentbridge_logout_intent'
let authMutationVersion = 0

function notifyAuthChange(): void {
    window.dispatchEvent(new Event('talentbridge_auth_change'))
}

export function getLogoutIntentId(): string | null {
    return localStorage.getItem(LOGOUT_INTENT_KEY)
}

export function isLogoutPending(): boolean {
    return getLogoutIntentId() !== null
}

/** Masks the shared session in every tab while logout waits for refresh to finish. */
export function beginLogoutIntent(): string {
    const logoutIntentId = globalThis.crypto?.randomUUID?.()
        ?? `${Date.now()}-${Math.random().toString(36).slice(2)}`
    localStorage.setItem(LOGOUT_INTENT_KEY, logoutIntentId)
    notifyAuthChange()
    return logoutIntentId
}

/** Identifies same-tab login/logout mutations so stale async refreshes cannot win. */
export function getAuthMutationVersion(): number {
    return authMutationVersion
}

export function saveAuthTokens(
    authResponse: AuthResponse,
    options: { preserveLogoutIntent?: boolean } = {},
): void {
    authMutationVersion += 1
    localStorage.setItem(ACCESS_TOKEN_KEY, authResponse.accessToken)
    localStorage.setItem(REFRESH_TOKEN_KEY, authResponse.refreshToken)
    localStorage.setItem(USER_KEY, JSON.stringify(authResponse.user))
    if (!options.preserveLogoutIntent) localStorage.removeItem(LOGOUT_INTENT_KEY)
    notifyAuthChange()
}

export function getStoredAccessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY)
}

export function getStoredRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY)
}

export function getAccessToken(): string | null {
    return isLogoutPending() ? null : getStoredAccessToken()
}

export function getRefreshToken(): string | null {
    return isLogoutPending() ? null : getStoredRefreshToken()
}

export function getStoredUser(): UserResponse | null {
    if (isLogoutPending()) return null
    const raw = localStorage.getItem(USER_KEY)
    if (!raw) return null
    try {
        return JSON.parse(raw) as UserResponse
    } catch {
        return null
    }
}

export function clearAuthTokens(expectedLogoutIntentId?: string): boolean {
    if (expectedLogoutIntentId && getLogoutIntentId() !== expectedLogoutIntentId) return false
    authMutationVersion += 1
    localStorage.removeItem(ACCESS_TOKEN_KEY)
    localStorage.removeItem(REFRESH_TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
    localStorage.removeItem(LOGOUT_INTENT_KEY)
    notifyAuthChange()
    return true
}

export function isAuthenticated(): boolean {
    return Boolean(getAccessToken())
}
