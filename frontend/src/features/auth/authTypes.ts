export type CandidateRegisterRequest = {
    email: string
    password: string
    fullName: string
    phone?: string
    role: 'ROLE_CANDIDATE'
}

export type UserRole = 'ROLE_CANDIDATE' | 'ROLE_RECRUITER' | 'ROLE_ADMIN'

export type UserResponse = {
    id: number
    email: string
    fullName: string
    phone?: string
    status: 'ACTIVE' | 'INACTIVE' | 'BANNED' | 'LOCKED' | 'PENDING'
    roles: UserRole[]
}

export type AuthResponse = {
    accessToken: string
    refreshToken: string
    tokenType: string
    expiresInMs: number
    user: UserResponse
}

export type ApiResponse<T> = {
    statusCode: number
    message: string
    data?: T
    timestamp?: string
}

export type ApiValidationErrors = Record<string, string>

export type LoginRequest = {
    email: string
    password: string
}

export type ForgotPasswordRequest = {
    email: string
}

export type ResetPasswordRequest = {
    token: string
    newPassword: string
}
