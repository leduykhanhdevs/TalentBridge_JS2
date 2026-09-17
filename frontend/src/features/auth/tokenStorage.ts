import type { AuthResponse, UserResponse, UserRole } from './authTypes'

const ACCESS_TOKEN_KEY = 'talentbridge_access_token'
const REFRESH_TOKEN_KEY = 'talentbridge_refresh_token'
const AUTH_USER_KEY = 'talentbridge_auth_user'

const userRoles: UserRole[] = [
    'ROLE_CANDIDATE',
    'ROLE_RECRUITER',
    'ROLE_ADMIN',
]

function getSessionStorage(): Storage | null {
    return typeof globalThis.sessionStorage === 'undefined'
        ? null
        : globalThis.sessionStorage
}

function dispatchAuthChange(): void {
    if (typeof window !== 'undefined') {
        window.dispatchEvent(new Event('talentbridge_auth_change'))
    }
}

function isStoredUser(value: unknown): value is UserResponse {
    if (typeof value !== 'object' || value === null || Array.isArray(value)) {
        return false
    }

    const user = value as Partial<UserResponse>
    return (
        typeof user.id === 'number' &&
        typeof user.email === 'string' &&
        typeof user.fullName === 'string' &&
        Array.isArray(user.roles) &&
        user.roles.every((role) => userRoles.includes(role))
    )
}

function clearStorage(storage: Storage) {
    storage.removeItem(ACCESS_TOKEN_KEY)
    storage.removeItem(REFRESH_TOKEN_KEY)
    storage.removeItem(AUTH_USER_KEY)
}

export function saveAuthTokens(
    authResponse: AuthResponse,
    rememberMe = true,
) {
    const browserSessionStorage = getSessionStorage()
    const targetStorage =
        rememberMe || browserSessionStorage === null
            ? localStorage
            : browserSessionStorage
    const otherStorage =
        targetStorage === localStorage ? browserSessionStorage : localStorage

    if (otherStorage) {
        clearStorage(otherStorage)
    }
    targetStorage.setItem(ACCESS_TOKEN_KEY, authResponse.accessToken)
    targetStorage.setItem(REFRESH_TOKEN_KEY, authResponse.refreshToken)
    targetStorage.setItem(AUTH_USER_KEY, JSON.stringify(authResponse.user))
    dispatchAuthChange()
}

export function getAccessToken() {
    return (
        localStorage.getItem(ACCESS_TOKEN_KEY) ??
        getSessionStorage()?.getItem(ACCESS_TOKEN_KEY) ??
        null
    )
}

export function getRefreshToken() {
    return (
        localStorage.getItem(REFRESH_TOKEN_KEY) ??
        getSessionStorage()?.getItem(REFRESH_TOKEN_KEY) ??
        null
    )
}

export function getStoredUser(): UserResponse | null {
    const storedUser =
        localStorage.getItem(AUTH_USER_KEY) ??
        getSessionStorage()?.getItem(AUTH_USER_KEY)

    if (!storedUser) {
        return null
    }

    try {
        const parsedUser: unknown = JSON.parse(storedUser)
        if (isStoredUser(parsedUser)) {
            return parsedUser
        }
    } catch {
        // Invalid session data is cleared below.
    }

    clearAuthTokens()
    return null
}

export function clearAuthTokens() {
    clearStorage(localStorage)
    const browserSessionStorage = getSessionStorage()
    if (browserSessionStorage) {
        clearStorage(browserSessionStorage)
    }
    dispatchAuthChange()
}

export function isAuthenticated(): boolean {
    return Boolean(getAccessToken())
}
