import type {
    ApiResponse,
    ApiValidationErrors,
    AuthResponse,
    CandidateRegisterRequest,
    ForgotPasswordRequest,
    LoginRequest,
    ResetPasswordRequest,
} from './authTypes'
import { getAccessToken, clearAuthTokens } from './tokenStorage'

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

export async function registerCandidate(
    request: CandidateRegisterRequest,
): Promise<AuthResponse> {
    let response: Response

    try {
        response = await fetch(`${API_BASE_URL}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json; charset=UTF-8',
            },
            body: JSON.stringify(request),
        })
    } catch {
        throw new AuthApiError(
            0,
            'Không thể kết nối đến máy chủ. Vui lòng kiểm tra backend và thử lại.',
        )
    }

    let body: ApiResponse<AuthResponse | ApiValidationErrors> | undefined

    try {
        body = (await response.json()) as ApiResponse<
            AuthResponse | ApiValidationErrors
        >
    } catch {
        body = undefined
    }

    if (!response.ok) {
        const defaultMessage =
            response.status === 409
                ? 'Email này đã được sử dụng.'
                : 'Đăng ký không thành công. Vui lòng thử lại.'
        const fieldErrors =
            response.status === 400 && isValidationErrors(body?.data)
                ? body.data
                : undefined

        throw new AuthApiError(
            response.status,
            body?.message || defaultMessage,
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
        response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json; charset=UTF-8',
            },
            body: JSON.stringify(request),
        })
    } catch {
        throw new AuthApiError(
            0,
            'Không thể kết nối đến máy chủ. Vui lòng kiểm tra backend và thử lại.',
        )
    }

    let body: ApiResponse<AuthResponse | ApiValidationErrors> | undefined

    try {
        body = (await response.json()) as ApiResponse<
            AuthResponse | ApiValidationErrors
        >
    } catch {
        body = undefined
    }

    if (!response.ok) {
        const defaultMessage =
            response.status === 401
                ? 'Email hoặc mật khẩu không chính xác.'
                : 'Đăng nhập không thành công. Vui lòng thử lại.'
        const fieldErrors =
            response.status === 400 && isValidationErrors(body?.data)
                ? body.data
                : undefined

        throw new AuthApiError(
            response.status,
            body?.message || defaultMessage,
            fieldErrors,
        )
    }

    if (!isAuthResponse(body?.data)) {
        throw new AuthApiError(500, 'Phản hồi từ máy chủ không hợp lệ.')
    }

    return body.data
}

export async function logoutUser(): Promise<void> {
    const token = getAccessToken()
    try {
        if (token) {
            await fetch(`${API_BASE_URL}/auth/logout`, {
                method: 'POST',
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            })
        }
    } catch {
        // Silently ignore network failure on logout
    } finally {
        clearAuthTokens()
    }
}

export async function requestForgotPassword(
    request: ForgotPasswordRequest,
): Promise<void> {
    let response: Response

    try {
        response = await fetch(`${API_BASE_URL}/auth/forgot-password`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json; charset=UTF-8',
            },
            body: JSON.stringify(request),
        })
    } catch {
        throw new AuthApiError(
            0,
            'Không thể kết nối đến máy chủ. Vui lòng kiểm tra backend và thử lại.',
        )
    }

    let body: ApiResponse<void | ApiValidationErrors> | undefined

    try {
        body = (await response.json()) as ApiResponse<void | ApiValidationErrors>
    } catch {
        body = undefined
    }

    if (!response.ok) {
        const fieldErrors =
            response.status === 400 && isValidationErrors(body?.data)
                ? body.data
                : undefined

        throw new AuthApiError(
            response.status,
            body?.message || 'Không thể gửi yêu cầu đặt lại mật khẩu.',
            fieldErrors,
        )
    }
}

export async function resetPassword(
    request: ResetPasswordRequest,
): Promise<void> {
    let response: Response

    try {
        response = await fetch(`${API_BASE_URL}/auth/reset-password`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json; charset=UTF-8',
            },
            body: JSON.stringify(request),
        })
    } catch {
        throw new AuthApiError(
            0,
            'Không thể kết nối đến máy chủ. Vui lòng kiểm tra backend và thử lại.',
        )
    }

    let body: ApiResponse<void | ApiValidationErrors> | undefined

    try {
        body = (await response.json()) as ApiResponse<void | ApiValidationErrors>
    } catch {
        body = undefined
    }

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

