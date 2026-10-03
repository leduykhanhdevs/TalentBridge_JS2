import type { ApiResponse, PageResponse } from './recruiterTypes'
import type {
    CreateJobPayload,
    RecruiterJobFilterParams,
    RecruiterJobItem,
    UpdateJobPayload,
    MyJobStats,
} from './recruiterJobTypes'
import { getAccessToken } from '../auth/tokenStorage'

export class RecruiterJobApiError extends Error {
    readonly status: number

    constructor(status: number, message: string) {
        super(message)
        this.name = 'RecruiterJobApiError'
        this.status = status
    }
}

const API_BASE_URL = (
    import.meta.env.VITE_API_BASE_URL || '/api/v1'
).replace(/\/$/, '')

function getAuthHeaders(): HeadersInit {
    const token = getAccessToken()
    if (!token) {
        throw new RecruiterJobApiError(401, 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.')
    }
    return {
        'Content-Type': 'application/json; charset=UTF-8',
        Authorization: `Bearer ${token}`,
    }
}

async function handleResponse<T>(response: Response, defaultErrorMsg: string): Promise<T> {
    let body: ApiResponse<T> | undefined
    try {
        body = (await response.json()) as ApiResponse<T>
    } catch {
        body = undefined
    }

    if (!response.ok) {
        const errorMsg = body?.message || defaultErrorMsg
        throw new RecruiterJobApiError(response.status, errorMsg)
    }

    if (body?.data !== undefined) {
        return body.data
    }

    return {} as T
}

/**
 * HR lấy danh sách tin tuyển dụng của công ty mình (My Jobs)
 */
export async function getMyJobs(
    params: RecruiterJobFilterParams = {},
): Promise<PageResponse<RecruiterJobItem>> {
    const query = new URLSearchParams()
    if (params.page !== undefined) query.set('page', String(params.page))
    if (params.size !== undefined) query.set('size', String(params.size))
    if (params.status) query.set('status', params.status)

    const url = `${API_BASE_URL}/jobs/my-jobs?${query.toString()}`
    const response = await fetch(url, {
        method: 'GET',
        headers: getAuthHeaders(),
    })

    return handleResponse<PageResponse<RecruiterJobItem>>(
        response,
        'Không thể tải danh sách tin tuyển dụng.',
    )
}

export async function getMyJobStats(): Promise<MyJobStats> {
    const response = await fetch(`${API_BASE_URL}/recruiters/my-jobs/stats`, {
        method: 'GET',
        headers: getAuthHeaders(),
    })
    return handleResponse<MyJobStats>(response, 'Không thể tải thống kê tin tuyển dụng.')
}

/**
 * HR đăng tin tuyển dụng mới (HRPM-51)
 */
export async function createJob(
    payload: CreateJobPayload,
): Promise<RecruiterJobItem> {
    const url = `${API_BASE_URL}/jobs`
    const response = await fetch(url, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(payload),
    })

    return handleResponse<RecruiterJobItem>(
        response,
        'Không thể tạo tin tuyển dụng.',
    )
}

/**
 * HR cập nhật tin tuyển dụng (HRPM-52)
 */
export async function updateJob(
    id: number,
    payload: UpdateJobPayload,
): Promise<RecruiterJobItem> {
    const url = `${API_BASE_URL}/jobs/${id}`
    const response = await fetch(url, {
        method: 'PUT',
        headers: getAuthHeaders(),
        body: JSON.stringify(payload),
    })

    return handleResponse<RecruiterJobItem>(
        response,
        'Không thể cập nhật tin tuyển dụng.',
    )
}

/**
 * HR đóng tin tuyển dụng (HRPM-53)
 */
export async function closeJob(id: number): Promise<RecruiterJobItem> {
    const url = `${API_BASE_URL}/jobs/${id}/close`
    const response = await fetch(url, {
        method: 'PATCH',
        headers: getAuthHeaders(),
    })

    return handleResponse<RecruiterJobItem>(
        response,
        'Không thể đóng tin tuyển dụng.',
    )
}

/**
 * Lấy chi tiết tin tuyển dụng
 */
export async function getJobDetail(id: number): Promise<RecruiterJobItem> {
    const url = `${API_BASE_URL}/jobs/${id}`
    const response = await fetch(url, {
        method: 'GET',
        headers: getAuthHeaders(),
    })

    return handleResponse<RecruiterJobItem>(
        response,
        'Không thể tải chi tiết tin tuyển dụng.',
    )
}
