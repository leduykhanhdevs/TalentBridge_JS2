import type { AuthResponse } from './authTypes'

const ACCESS_TOKEN_KEY = 'talentbridge_access_token'
const REFRESH_TOKEN_KEY = 'talentbridge_refresh_token'

export function saveAuthTokens(authResponse: AuthResponse) {
    localStorage.setItem(ACCESS_TOKEN_KEY, authResponse.accessToken)
    localStorage.setItem(REFRESH_TOKEN_KEY, authResponse.refreshToken)
}
