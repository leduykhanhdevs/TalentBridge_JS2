import type {
    CandidateProfileResponse,
    UpdateCandidateProfileRequest,
} from './candidateTypes'
import { getAccessToken } from '../auth/tokenStorage'

export class CandidateApiError extends Error {
    readonly status: number

    constructor(status: number, message: string) {
        super(message)
        this.name = 'CandidateApiError'
        this.status = status
    }
}

interface ApiResponse<T> {
    success: boolean
    message: string
    statusCode: number
    data: T
    timestamp: string
}

const API_BASE_URL = (
    import.meta.env.VITE_API_BASE_URL || '/api/v1'
).replace(/\/$/, '')

function getAuthHeaders(): HeadersInit {
    const token = getAccessToken()
    if (!token) {
        throw new CandidateApiError(401, 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.')
    }
    return {
        'Content-Type': 'application/json; charset=UTF-8',
        Authorization: `Bearer ${token}`,
    }
}

export async function getCandidateProfile(): Promise<CandidateProfileResponse> {
    const res = await fetch(`${API_BASE_URL}/candidates/profile`, {
        method: 'GET',
        headers: getAuthHeaders(),
    })

    if (!res.ok) {
        const errJson = await res.json().catch(() => null)
        throw new CandidateApiError(
            res.status,
            errJson?.message || 'Không thể lấy thông tin hồ sơ ứng viên',
        )
    }

    const payload: ApiResponse<CandidateProfileResponse> = await res.json()
    return payload.data
}

export async function updateCandidateProfile(
    request: UpdateCandidateProfileRequest,
): Promise<CandidateProfileResponse> {
    const res = await fetch(`${API_BASE_URL}/candidates/profile`, {
        method: 'PUT',
        headers: getAuthHeaders(),
        body: JSON.stringify(request),
    })

    if (!res.ok) {
        const errJson = await res.json().catch(() => null)
        throw new CandidateApiError(
            res.status,
            errJson?.message || 'Không thể cập nhật thông tin hồ sơ ứng viên',
        )
    }

    const payload: ApiResponse<CandidateProfileResponse> = await res.json()
    return payload.data
}
