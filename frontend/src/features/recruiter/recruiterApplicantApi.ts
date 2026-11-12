import { getAccessToken } from '../auth/tokenStorage'
import { authenticatedFetch } from '../auth/authenticatedFetch'
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

    const res = await authenticatedFetch(url, {
        method: 'GET',
        headers: getAuthHeaders(),
    })

    return handleResponse<JobApplicant[]>(res, 'Không thể tải danh sách ứng viên')
}

export async function matchApplicantWithAi(jobId: number, candidateId: number): Promise<AiMatchResult> {
    const response = await authenticatedFetch(`${API_BASE_URL}/ai/match`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify({ jobId, candidateId }),
    })
    return handleResponse<AiMatchResult>(response, 'Không thể phân tích mức độ phù hợp bằng AI')
}

export async function openApplicantResume(jobId: number, resumeId: number, fileName = 'CV'): Promise<void> {
    const token = getAccessToken()
    if (!token) throw new RecruiterApplicantApiError(401, 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.')

    // Open synchronously from the user's click so popup blockers do not discard the preview.
    const previewWindow = window.open('about:blank', '_blank')
    try {
        const response = await authenticatedFetch(
            `${API_BASE_URL}/recruiters/jobs/${jobId}/resumes/${resumeId}/view`,
            { method: 'GET', headers: { Authorization: `Bearer ${token}` } },
        )
        if (!response.ok) {
            let message = 'Không thể mở CV ứng viên.'
            try {
                const body = await response.json() as { message?: string }
                message = body.message || message
            } catch {
                // Keep the safe message when the server does not return JSON.
            }
            throw new RecruiterApplicantApiError(response.status, message)
        }

        const objectUrl = URL.createObjectURL(await response.blob())
        if (previewWindow) {
            previewWindow.opener = null
            const previewDocument = previewWindow.document
            previewDocument.title = `Xem CV - ${fileName}`
            previewDocument.body.replaceChildren()
            previewDocument.body.style.cssText = 'margin:0;min-height:100vh;display:flex;flex-direction:column;background:#f1f5f9;font-family:system-ui,sans-serif'

            const toolbar = previewDocument.createElement('header')
            toolbar.style.cssText = 'display:flex;align-items:center;justify-content:space-between;gap:16px;padding:12px 20px;background:#fff;border-bottom:1px solid #e2e8f0;color:#0f172a;font-size:14px'
            const title = previewDocument.createElement('strong')
            title.textContent = fileName
            const downloadLink = previewDocument.createElement('a')
            downloadLink.href = objectUrl
            downloadLink.download = fileName
            downloadLink.textContent = 'Tải CV nếu không xem được'
            downloadLink.style.cssText = 'color:#1d4ed8;font-weight:600;text-decoration:none'
            toolbar.append(title, downloadLink)

            const preview = previewDocument.createElement('iframe')
            preview.title = 'Bản xem trước CV'
            preview.src = objectUrl
            preview.style.cssText = 'display:block;flex:1;width:100%;min-height:calc(100vh - 50px);border:0;background:#fff'
            previewDocument.body.append(toolbar, preview)
        } else {
            const link = document.createElement('a')
            link.href = objectUrl
            link.download = fileName
            document.body.appendChild(link)
            link.click()
            link.remove()
        }
        window.setTimeout(() => URL.revokeObjectURL(objectUrl), 60_000)
    } catch (error) {
        previewWindow?.close()
        throw error
    }
}

export async function updateApplicantStage(
    jobId: number,
    applicationId: number,
    payload: UpdateStagePayload
): Promise<JobApplicant> {
    const url = `${API_BASE_URL}/recruiters/jobs/${jobId}/applicants/${applicationId}/stage`

    const res = await authenticatedFetch(url, {
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
    const res = await authenticatedFetch(`${API_BASE_URL}/recruiters/jobs/${jobId}/applicants/${applicationId}/reopen`, {
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

    const res = await authenticatedFetch(url, {
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

    const res = await authenticatedFetch(url, {
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

    const res = await authenticatedFetch(url, {
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

    const res = await authenticatedFetch(url, {
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

    const res = await authenticatedFetch(url, {
        method: 'GET',
        headers: getAuthHeaders(),
    })

    return handleResponse<InterviewItem[]>(res, 'Không thể tải danh sách lịch phỏng vấn')
}

export async function downloadRecruiterInterviewCalendar(interviewId: number): Promise<void> {
    const token = getAccessToken()
    if (!token) throw new RecruiterApplicantApiError(401, 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.')
    const response = await authenticatedFetch(`${API_BASE_URL}/interviews/${interviewId}/calendar.ics`, {
        method: 'GET',
        headers: { Authorization: `Bearer ${token}` },
    })
    if (!response.ok) {
        let message = 'Không thể tải lịch phỏng vấn. Vui lòng thử lại.'
        try {
            const body = await response.json() as { message?: string }
            message = body.message || message
        } catch {
            // Keep the user-facing fallback when the server has no JSON error body.
        }
        throw new RecruiterApplicantApiError(response.status, message)
    }
    const objectUrl = URL.createObjectURL(await response.blob())
    const link = document.createElement('a')
    link.href = objectUrl
    link.download = `TalentBridge-phong-van-${interviewId}.ics`
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.setTimeout(() => URL.revokeObjectURL(objectUrl), 60_000)
}

