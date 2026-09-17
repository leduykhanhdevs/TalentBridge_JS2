import type { AuthResponse, UserResponse } from './authTypes'

const ACCESS_TOKEN_KEY = 'talentbridge_access_token'
const REFRESH_TOKEN_KEY = 'talentbridge_refresh_token'
const USER_KEY = 'talentbridge_auth_user'

export function saveAuthTokens(authResponse: AuthResponse): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, authResponse.accessToken)
    localStorage.setItem(REFRESH_TOKEN_KEY, authResponse.refreshToken)
    localStorage.setItem(USER_KEY, JSON.stringify(authResponse.user))
    window.dispatchEvent(new Event('talentbridge_auth_change'))
}

export function getAccessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY)
}

export function getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY)
}

export function getStoredUser(): UserResponse | null {
    const raw = localStorage.getItem(USER_KEY)
    if (!raw) return null
    try {
        return JSON.parse(raw) as UserResponse
    } catch {
        return null
    }
}

export function clearAuthTokens(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY)
    localStorage.removeItem(REFRESH_TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
    window.dispatchEvent(new Event('talentbridge_auth_change'))
}

export function isAuthenticated(): boolean {
    return Boolean(getAccessToken())
}
