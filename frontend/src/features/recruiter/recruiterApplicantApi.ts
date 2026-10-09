import { getAccessToken } from '../auth/tokenStorage'
import type {
    AddNotePayload,
    AiMatchResult,
    ApplicantFilterParams,
    ApplicationNote,
    ApplicationStage,
    JobApplicant,
    UpdateStagePayload,
    InterviewItem,
    ScheduleInterviewPayload,
} from './recruiterApplicantTypes'

export class RecruiterApplicantApiError extends Error {
    readonly status: number

    constructor(status: number, message: string) {
        super(message)
        this.name = 'RecruiterApplicantApiError'
        this.status = status
    }
}

const API_BASE_URL = (
    import.meta.env.VITE_API_BASE_URL || '/api/v1'
).replace(/\/$/, '')

function getAuthHeaders(): HeadersInit {
    const token = getAccessToken()
    if (!token) {
        throw new RecruiterApplicantApiError(401, 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.')
    }
    return {
        'Content-Type': 'application/json; charset=UTF-8',
        Authorization: `Bearer ${token}`,
    }
}

interface ApiResponseEnvelope<T> {
    statusCode: number
    message: string
    data: T
    timestamp?: string
}

async function handleResponse<T>(response: Response, defaultErrorMsg: string): Promise<T> {
    let body: ApiResponseEnvelope<T> | undefined
    try {
        body = (await response.json()) as ApiResponseEnvelope<T>
    } catch {
        body = undefined
    }

    if (!response.ok) {
        const errorMsg = body?.message || defaultErrorMsg
        throw new RecruiterApplicantApiError(response.status, errorMsg)
    }

    if (body?.data !== undefined) {
        return body.data
    }

    return [] as unknown as T
}

export async function getJobApplicants(
    jobId: number,
    params?: ApplicantFilterParams
): Promise<JobApplicant[]> {
    const query = new URLSearchParams()
    if (params?.keyword) query.set('keyword', params.keyword)
    if (params?.stage) query.set('stage', params.stage)
    if (params?.minExperience !== undefined && params.minExperience !== null) {
        query.set('minExperience', String(params.minExperience))
    }
    if (params?.sortBy) query.set('sortBy', params.sortBy)
    if (params?.sortDirection) query.set('sortDirection', params.sortDirection)

    const queryString = query.toString()
    const url = `${API_BASE_URL}/recruiters/jobs/${jobId}/applicants${queryString ? `?${queryString}` : ''}`

    const res = await fetch(url, {
        method: 'GET',
        headers: getAuthHeaders(),
    })

    return handleResponse<JobApplicant[]>(res, 'Không thể tải danh sách ứng viên')
}

export async function matchApplicantWithAi(jobId: number, candidateId: number): Promise<AiMatchResult> {
    const response = await fetch(`${API_BASE_URL}/ai/match`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify({ jobId, candidateId }),
    })
    return handleResponse<AiMatchResult>(response, 'Không thể phân tích mức độ phù hợp bằng AI')
}

export async function updateApplicantStage(
    jobId: number,
    applicationId: number,
    payload: UpdateStagePayload
): Promise<JobApplicant> {
    const url = `${API_BASE_URL}/recruiters/jobs/${jobId}/applicants/${applicationId}/stage`

    const res = await fetch(url, {
        method: 'PATCH',
        headers: getAuthHeaders(),
        body: JSON.stringify(payload),
    })

    return handleResponse<JobApplicant>(res, 'Không thể cập nhật trạng thái ứng viên')
}

export async function reopenApplicantApplication(
    jobId: number,
    applicationId: number,
    reason: string,
): Promise<JobApplicant> {
    const res = await fetch(`${API_BASE_URL}/recruiters/jobs/${jobId}/applicants/${applicationId}/reopen`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify({ reason }),
    })
    return handleResponse<JobApplicant>(res, 'Không thể mở lại hồ sơ ứng tuyển.')
}

export async function addApplicantNote(
    jobId: number,
    applicationId: number,
    payload: AddNotePayload
): Promise<ApplicationNote> {
    const url = `${API_BASE_URL}/recruiters/jobs/${jobId}/applicants/${applicationId}/notes`

    const res = await fetch(url, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(payload),
    })

    return handleResponse<ApplicationNote>(res, 'Không thể lưu ghi chú đánh giá')
}

export async function getApplicantNotes(
    jobId: number,
    applicationId: number
): Promise<ApplicationNote[]> {
    const url = `${API_BASE_URL}/recruiters/jobs/${jobId}/applicants/${applicationId}/notes`

    const res = await fetch(url, {
        method: 'GET',
        headers: getAuthHeaders(),
    })

    return handleResponse<ApplicationNote[]>(res, 'Không thể tải lịch sử ghi chú')
}

export async function getApplicantStageHistory(
    jobId: number,
    applicationId: number
): Promise<ApplicationStage[]> {
    const url = `${API_BASE_URL}/recruiters/jobs/${jobId}/applicants/${applicationId}/stages`

    const res = await fetch(url, {
        method: 'GET',
        headers: getAuthHeaders(),
    })

    return handleResponse<ApplicationStage[]>(res, 'Không thể tải lịch sử vòng tuyển dụng')
}

// ==========================================
// INTERVIEW SCHEDULING (GOOGLE CALENDAR)
// ==========================================

export async function scheduleApplicantInterview(
    jobId: number,
    applicationId: number,
    payload: ScheduleInterviewPayload
): Promise<InterviewItem> {
    const url = `${API_BASE_URL}/recruiters/jobs/${jobId}/applicants/${applicationId}/interviews`

    const res = await fetch(url, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(payload),
    })

    return handleResponse<InterviewItem>(res, 'Không thể lên lịch phỏng vấn')
}

export async function getApplicantInterviews(
    jobId: number,
    applicationId: number
): Promise<InterviewItem[]> {
    const url = `${API_BASE_URL}/recruiters/jobs/${jobId}/applicants/${applicationId}/interviews`

    const res = await fetch(url, {
        method: 'GET',
        headers: getAuthHeaders(),
    })

    return handleResponse<InterviewItem[]>(res, 'Không thể tải danh sách lịch phỏng vấn')
}

