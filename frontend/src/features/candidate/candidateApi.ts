import type {
    CandidateProfileResponse,
    UpdateCandidateProfileRequest,
    WorkExperience,
    WorkExperienceRequest,
    CandidateSkill,
    CandidateSkillRequest,
    SkillItem,
    ResumeItem,
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

// ==========================================
// WORK EXPERIENCES
// ==========================================

export async function getWorkExperiences(): Promise<WorkExperience[]> {
    const res = await fetch(`${API_BASE_URL}/candidates/work-experiences`, {
        method: 'GET',
        headers: getAuthHeaders(),
    })

    if (!res.ok) {
        const errJson = await res.json().catch(() => null)
        throw new CandidateApiError(
            res.status,
            errJson?.message || 'Không thể lấy danh sách kinh nghiệm làm việc',
        )
    }

    const payload: ApiResponse<WorkExperience[]> = await res.json()
    return payload.data || []
}

export async function addWorkExperience(
    request: WorkExperienceRequest,
): Promise<WorkExperience> {
    const res = await fetch(`${API_BASE_URL}/candidates/work-experiences`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(request),
    })

    if (!res.ok) {
        const errJson = await res.json().catch(() => null)
        throw new CandidateApiError(
            res.status,
            errJson?.message || 'Không thể thêm kinh nghiệm làm việc',
        )
    }

    const payload: ApiResponse<WorkExperience> = await res.json()
    return payload.data
}

export async function updateWorkExperience(
    id: number,
    request: WorkExperienceRequest,
): Promise<WorkExperience> {
    const res = await fetch(`${API_BASE_URL}/candidates/work-experiences/${id}`, {
        method: 'PUT',
        headers: getAuthHeaders(),
        body: JSON.stringify(request),
    })

    if (!res.ok) {
        const errJson = await res.json().catch(() => null)
        throw new CandidateApiError(
            res.status,
            errJson?.message || 'Không thể cập nhật kinh nghiệm làm việc',
        )
    }

    const payload: ApiResponse<WorkExperience> = await res.json()
    return payload.data
}

export async function deleteWorkExperience(id: number): Promise<void> {
    const res = await fetch(`${API_BASE_URL}/candidates/work-experiences/${id}`, {
        method: 'DELETE',
        headers: getAuthHeaders(),
    })

    if (!res.ok) {
        const errJson = await res.json().catch(() => null)
        throw new CandidateApiError(
            res.status,
            errJson?.message || 'Không thể xóa kinh nghiệm làm việc',
        )
    }
}

// ==========================================
// CANDIDATE SKILLS
// ==========================================

export async function getCandidateSkills(): Promise<CandidateSkill[]> {
    const res = await fetch(`${API_BASE_URL}/candidates/skills`, {
        method: 'GET',
        headers: getAuthHeaders(),
    })

    if (!res.ok) {
        const errJson = await res.json().catch(() => null)
        throw new CandidateApiError(
            res.status,
            errJson?.message || 'Không thể lấy danh sách kỹ năng',
        )
    }

    const payload: ApiResponse<CandidateSkill[]> = await res.json()
    return payload.data || []
}

export async function addCandidateSkill(
    request: CandidateSkillRequest,
): Promise<CandidateSkill> {
    const res = await fetch(`${API_BASE_URL}/candidates/skills`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(request),
    })

    if (!res.ok) {
        const errJson = await res.json().catch(() => null)
        throw new CandidateApiError(
            res.status,
            errJson?.message || 'Không thể thêm kỹ năng',
        )
    }

    const payload: ApiResponse<CandidateSkill> = await res.json()
    return payload.data
}

export async function deleteCandidateSkill(id: number): Promise<void> {
    const res = await fetch(`${API_BASE_URL}/candidates/skills/${id}`, {
        method: 'DELETE',
        headers: getAuthHeaders(),
    })

    if (!res.ok) {
        const errJson = await res.json().catch(() => null)
        throw new CandidateApiError(
            res.status,
            errJson?.message || 'Không thể xóa kỹ năng',
        )
    }
}

// ==========================================
// MASTER SKILLS
// ==========================================

export async function getAllMasterSkills(): Promise<SkillItem[]> {
    const res = await fetch(`${API_BASE_URL}/skills`, {
        method: 'GET',
    })

    if (!res.ok) {
        return []
    }

    const payload: ApiResponse<SkillItem[]> = await res.json()
    return payload.data || []
}

// ==========================================
// RESUMES (CV UPLOAD & MANAGEMENT)
// ==========================================

export async function getCandidateResumes(): Promise<ResumeItem[]> {
    const res = await fetch(`${API_BASE_URL}/candidates/resumes`, {
        method: 'GET',
        headers: getAuthHeaders(),
    })

    if (!res.ok) {
        const errJson = await res.json().catch(() => null)
        throw new CandidateApiError(
            res.status,
            errJson?.message || 'Không thể lấy danh sách CV',
        )
    }

    const payload: ApiResponse<ResumeItem[]> = await res.json()
    return payload.data || []
}

export async function uploadResume(file: File, title?: string): Promise<ResumeItem> {
    const token = getAccessToken()
    if (!token) {
        throw new CandidateApiError(401, 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.')
    }

    const formData = new FormData()
    formData.append('file', file)
    if (title && title.trim()) {
        formData.append('title', title.trim())
    }

    const res = await fetch(`${API_BASE_URL}/candidates/resumes/upload`, {
        method: 'POST',
        headers: {
            Authorization: `Bearer ${token}`,
        },
        body: formData,
    })

    if (!res.ok) {
        const errJson = await res.json().catch(() => null)
        throw new CandidateApiError(
            res.status,
            errJson?.message || 'Không thể tải lên file CV.',
        )
    }

    const payload: ApiResponse<ResumeItem> = await res.json()
    return payload.data
}

export async function deleteResume(id: number): Promise<void> {
    const res = await fetch(`${API_BASE_URL}/candidates/resumes/${id}`, {
        method: 'DELETE',
        headers: getAuthHeaders(),
    })

    if (!res.ok) {
        const errJson = await res.json().catch(() => null)
        throw new CandidateApiError(
            res.status,
            errJson?.message || 'Không thể xóa CV',
        )
    }
}

export async function setDefaultResume(id: number): Promise<ResumeItem> {
    const res = await fetch(`${API_BASE_URL}/candidates/resumes/${id}/default`, {
        method: 'PUT',
        headers: getAuthHeaders(),
    })

    if (!res.ok) {
        const errJson = await res.json().catch(() => null)
        throw new CandidateApiError(
            res.status,
            errJson?.message || 'Không thể đặt làm CV mặc định',
        )
    }

    const payload: ApiResponse<ResumeItem> = await res.json()
    return payload.data
}

export async function downloadResumeFile(id: number, fileName: string): Promise<void> {
    const token = getAccessToken()
    if (!token) {
        throw new CandidateApiError(401, 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.')
    }

    const res = await fetch(`${API_BASE_URL}/candidates/resumes/${id}/download`, {
        method: 'GET',
        headers: {
            Authorization: `Bearer ${token}`,
        },
    })

    if (!res.ok) {
        throw new CandidateApiError(res.status, 'Không thể tải xuống file CV.')
    }

    const blob = await res.blob()
    const downloadUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    window.URL.revokeObjectURL(downloadUrl)
    document.body.removeChild(link)
}

