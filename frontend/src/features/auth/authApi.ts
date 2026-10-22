import type {
    ApiResponse,
    ApiValidationErrors,
    AuthResponse,
    CandidateRegisterRequest,
    ChangePasswordRequest,
    ForgotPasswordRequest,
    LoginRequest,
    ResetPasswordRequest,
    PasswordResetVerificationResponse,
    VerifyPasswordResetOtpRequest,
} from './authTypes'
import {
    beginLogoutIntent,
    clearAuthTokens,
    getAccessToken,
    getLogoutIntentId,
    getStoredAccessToken,
    getStoredRefreshToken,
} from './tokenStorage'
import { authenticatedFetch } from './authenticatedFetch'
import { REFRESH_TIMEOUT_MS } from './sessionRefresh'
import { withAuthSessionLock } from './authSessionLock'
import { fetchWithTimeout, FetchTimeoutError } from '../../app/fetchWithTimeout'

export class AuthApiError extends Error {
    readonly status: number
    readonly fieldErrors?: ApiValidationErrors

    constructor(
        status: number,
        message: string,
        fieldErrors?: ApiValidationErrors,
    ) {
        super(message)
        this.name = 'AuthApiError'
        this.status = status
        this.fieldErrors = fieldErrors
    }
}

function isValidationErrors(value: unknown): value is ApiValidationErrors {
    return (
        typeof value === 'object' &&
        value !== null &&
        !Array.isArray(value) &&
        Object.values(value).every((message) => typeof message === 'string')
    )
}

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

const API_BASE_URL = (
    import.meta.env.VITE_API_BASE_URL || '/api/v1'
).replace(/\/$/, '')

export const LOGOUT_TIMEOUT_MS = 2_500
export const LOGIN_REQUEST_TIMEOUT_MS = 30_000

function connectionErrorMessage(error: unknown): string {
    return error instanceof FetchTimeoutError
        ? error.message
        : 'Không thể kết nối đến máy chủ. Vui lòng kiểm tra backend và thử lại.'
}

async function readAuthResponseBody<T>(response: Response): Promise<T | undefined> {
    try {
        return await response.json() as T
    } catch (error) {
        if (error instanceof FetchTimeoutError) {
            throw new AuthApiError(0, error.message)
        }
        return undefined
    }
}

function discardResponseBody(response: Response | undefined): void {
    if (response?.body) {
        void response.body.cancel().catch(() => undefined)
    }
}

function fallbackHttpErrorMessage(
    status: number,
    action: 'đăng ký' | 'đăng nhập',
): string {
    if (status === 401 && action === 'đăng nhập') {
        return 'Email hoặc mật khẩu không chính xác.'
    }
    if (status === 409 && action === 'đăng ký') {
        return 'Email này đã được sử dụng.'
    }
    if (status === 429) {
        return `Hệ thống đang giới hạn yêu cầu ${action} (HTTP 429). Vui lòng chờ một phút rồi thử lại.`
    }
    if (status === 404) {
        return `Không tìm thấy API ${action} (HTTP 404). Hãy kiểm tra backend đang chạy đúng phiên bản.`
    }
    if (status === 400) {
        return `Yêu cầu ${action} không hợp lệ (HTTP 400). Vui lòng kiểm tra lại thông tin.`
    }
    if (status >= 500) {
        return `Backend gặp lỗi HTTP ${status} khi xử lý yêu cầu ${action}. Hãy dùng mã này để tra log.`
    }
    return `Yêu cầu ${action} thất bại (HTTP ${status}). Hãy dùng mã này để kiểm tra phản hồi backend.`
}

export async function registerCandidate(
    request: CandidateRegisterRequest,
): Promise<AuthResponse> {
    let response: Response

    try {
        response = await fetchWithTimeout(`${API_BASE_URL}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json; charset=UTF-8',
            },
            body: JSON.stringify(request),
        })
    } catch (error) {
        throw new AuthApiError(
            0,
            connectionErrorMessage(error),
        )
    }

    const body = await readAuthResponseBody<ApiResponse<AuthResponse | ApiValidationErrors>>(response)

    if (!response.ok) {
        const fieldErrors =
            response.status === 400 && isValidationErrors(body?.data)
                ? body.data
                : undefined

        throw new AuthApiError(
            response.status,
            body?.message || fallbackHttpErrorMessage(response.status, 'đăng ký'),
            fieldErrors,
        )
    }

    if (!isAuthResponse(body?.data)) {
        throw new AuthApiError(500, 'Phản hồi từ máy chủ không hợp lệ.')
    }

    return body.data
}

export async function loginUser(
    request: LoginRequest,
): Promise<AuthResponse> {
    let response: Response

    try {
        response = await fetchWithTimeout(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json; charset=UTF-8',
            },
            body: JSON.stringify(request),
        }, LOGIN_REQUEST_TIMEOUT_MS)
    } catch (error) {
        throw new AuthApiError(
            0,
            connectionErrorMessage(error),
        )
    }

    const body = await readAuthResponseBody<ApiResponse<AuthResponse | ApiValidationErrors>>(response)

    if (!response.ok) {
        const fieldErrors =
            response.status === 400 && isValidationErrors(body?.data)
                ? body.data
                : undefined

        throw new AuthApiError(
            response.status,
            body?.message || fallbackHttpErrorMessage(response.status, 'đăng nhập'),
            fieldErrors,
        )
    }

    if (!isAuthResponse(body?.data)) {
        throw new AuthApiError(500, 'Phản hồi từ máy chủ không hợp lệ.')
    }

    return body.data
}

export async function logoutUser(): Promise<void> {
    const logoutIntentId = beginLogoutIntent()
    const deadline = Date.now() + LOGOUT_TIMEOUT_MS + REFRESH_TIMEOUT_MS
    let revocationTokens: { accessToken: string | null; refreshToken: string | null } | null = null

    const remainingMs = () => Math.min(LOGOUT_TIMEOUT_MS, Math.max(0, deadline - Date.now()))
    const revoke = (
        token: string | null,
        sessionRefreshToken: string | null,
        timeoutMs = remainingMs(),
    ) => fetchWithTimeout(
        `${API_BASE_URL}/auth/logout`,
        {
            method: 'POST',
            headers: {
                ...(token ? { Authorization: `Bearer ${token}` } : {}),
                ...(sessionRefreshToken ? { 'Content-Type': 'application/json; charset=UTF-8' } : {}),
            },
            ...(sessionRefreshToken ? { body: JSON.stringify({ refreshToken: sessionRefreshToken }) } : {}),
            keepalive: true,
        },
        timeoutMs,
    )

    try {
        await withAuthSessionLock(async () => {
            if (getLogoutIntentId() !== logoutIntentId) return

            // The shared lock waits for a refresh in any same-origin tab to finish,
            // so read and revoke the latest rotated pair before clearing storage.
            const accessToken = getStoredAccessToken()
            const refreshToken = getStoredRefreshToken()
            if (!clearAuthTokens(logoutIntentId)) return
            revocationTokens = { accessToken, refreshToken }
            if ((!accessToken && !refreshToken) || remainingMs() <= 0) return

            const response = await revoke(accessToken, refreshToken)
            discardResponseBody(response)
        }, LOGOUT_TIMEOUT_MS + REFRESH_TIMEOUT_MS)
    } catch {
        // If acquiring the lock fails before the callback, revoke the best
        // available session pair. Local logout stays final and bounded.
        if (revocationTokens) return
        let tokens: { accessToken: string | null; refreshToken: string | null } | null = null
        if (getLogoutIntentId() === logoutIntentId) {
            tokens = { accessToken: getStoredAccessToken(), refreshToken: getStoredRefreshToken() }
            if (!clearAuthTokens(logoutIntentId)) tokens = null
        } else clearAuthTokens(logoutIntentId)

        if (!tokens || (!tokens.accessToken && !tokens.refreshToken)) return
        try {
            const response = await revoke(tokens.accessToken, tokens.refreshToken, LOGOUT_TIMEOUT_MS)
            discardResponseBody(response)
        } catch {
            // Server revocation is best-effort; local sign-out must not be undone.
        }
    }
}

export async function requestForgotPassword(
    request: ForgotPasswordRequest,
): Promise<void> {
    let response: Response

    try {
        response = await fetchWithTimeout(`${API_BASE_URL}/auth/forgot-password`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json; charset=UTF-8',
            },
            body: JSON.stringify(request),
        })
    } catch (error) {
        throw new AuthApiError(
            0,
            connectionErrorMessage(error),
        )
    }

    const body = await readAuthResponseBody<ApiResponse<void | ApiValidationErrors>>(response)

    if (!response.ok) {
        const fieldErrors =
            response.status === 400 && isValidationErrors(body?.data)
                ? body.data
                : undefined

        throw new AuthApiError(
            response.status,
            body?.message || (
                response.status === 503
                    ? 'Dịch vụ gửi email khôi phục mật khẩu hiện không khả dụng. Vui lòng thử lại sau hoặc liên hệ quản trị viên.'
                    : 'Không thể gửi yêu cầu đặt lại mật khẩu.'
            ),
            fieldErrors,
        )
    }
}

export async function verifyPasswordResetOtp(
    request: VerifyPasswordResetOtpRequest,
): Promise<PasswordResetVerificationResponse> {
    let response: Response

    try {
        response = await fetchWithTimeout(`${API_BASE_URL}/auth/verify-reset-otp`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json; charset=UTF-8',
            },
            body: JSON.stringify(request),
        })
    } catch (error) {
        throw new AuthApiError(0, connectionErrorMessage(error))
    }

    const body = await readAuthResponseBody<ApiResponse<PasswordResetVerificationResponse | ApiValidationErrors>>(response)

    if (!response.ok) {
        const fieldErrors = response.status === 400 && isValidationErrors(body?.data)
            ? body.data
            : undefined
        throw new AuthApiError(
            response.status,
            body?.message || 'Mã OTP không chính xác hoặc đã hết hạn. Bạn có thể nhập lại hoặc yêu cầu mã mới.',
            fieldErrors,
        )
    }

    if (
        typeof body?.data !== 'object' ||
        body.data === null ||
        !('resetToken' in body.data) ||
        typeof body.data.resetToken !== 'string' ||
        !body.data.resetToken
    ) {
        throw new AuthApiError(500, 'Phản hồi xác minh OTP từ máy chủ không hợp lệ.')
    }

    return body.data as PasswordResetVerificationResponse
}

export async function resetPassword(
    request: ResetPasswordRequest,
): Promise<void> {
    let response: Response

    try {
        response = await fetchWithTimeout(`${API_BASE_URL}/auth/reset-password`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json; charset=UTF-8',
            },
            body: JSON.stringify(request),
        })
    } catch (error) {
        throw new AuthApiError(
            0,
            connectionErrorMessage(error),
        )
    }

    const body = await readAuthResponseBody<ApiResponse<void | ApiValidationErrors>>(response)

    if (!response.ok) {
        const defaultMessage =
            response.status === 400
                ? 'Mã đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.'
                : 'Đặt lại mật khẩu không thành công. Vui lòng thử lại.'

        throw new AuthApiError(
            response.status,
            body?.message || defaultMessage,
        )
    }
}

export async function changePassword(
    request: ChangePasswordRequest,
): Promise<void> {
    const token = getAccessToken()
    if (!token) {
        throw new AuthApiError(401, 'Bạn chưa đăng nhập.')
    }

    let response: Response

    try {
        response = await authenticatedFetch(`${API_BASE_URL}/auth/change-password`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json; charset=UTF-8',
                Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify(request),
        })
    } catch (error) {
        throw new AuthApiError(
            0,
            connectionErrorMessage(error),
        )
    }

    const body = await readAuthResponseBody<ApiResponse<void | ApiValidationErrors>>(response)

    if (!response.ok) {
        const fieldErrors =
            response.status === 400 && isValidationErrors(body?.data)
                ? body.data
                : undefined

        throw new AuthApiError(
            response.status,
            body?.message || 'Đổi mật khẩu không thành công. Vui lòng thử lại.',
            fieldErrors,
        )
    }
}

