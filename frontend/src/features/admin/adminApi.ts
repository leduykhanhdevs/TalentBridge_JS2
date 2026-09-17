import type {
    AdminDashboardStatsResponse,
    ApiResponse,
    CandidateAdminResponse,
    CandidateFilterParams,
    CompanyAdminResponse,
    CompanyFilterParams,
    PageResponse,
    RecruiterAdminResponse,
    RecruiterFilterParams,
    UpdateCompanyStatusRequest,
    UpdateUserStatusRequest,
} from './adminTypes'
import { getAccessToken } from '../auth/tokenStorage'

export class AdminApiError extends Error {
    readonly status: number

    constructor(status: number, message: string) {
        super(message)
        this.name = 'AdminApiError'
        this.status = status
    }
}

const API_BASE_URL = (
    import.meta.env.VITE_API_BASE_URL || '/api/v1'
).replace(/\/$/, '')

function getAuthHeaders(): HeadersInit {
    const token = getAccessToken()
    if (!token) {
        throw new AdminApiError(401, 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.')
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
        throw new AdminApiError(response.status, errorMsg)
    }

    if (body?.data !== undefined) {
        return body.data
    }

    return {} as T
}

export async function getAdminCandidates(
    params: CandidateFilterParams = {},
): Promise<PageResponse<CandidateAdminResponse>> {
    const query = new URLSearchParams()
    if (params.page !== undefined) query.set('page', String(params.page))
    if (params.size !== undefined) query.set('size', String(params.size))
    if (params.keyword?.trim()) query.set('keyword', params.keyword.trim())
    if (params.status) query.set('status', params.status)

    const url = `${API_BASE_URL}/admin/candidates?${query.toString()}`
    const response = await fetch(url, {
        method: 'GET',
        headers: getAuthHeaders(),
    })

    return handleResponse<PageResponse<CandidateAdminResponse>>(
        response,
        'Không thể tải danh sách ứng viên.',
    )
}

export async function getCandidateById(id: number): Promise<CandidateAdminResponse> {
    const url = `${API_BASE_URL}/admin/candidates/${id}`
    const response = await fetch(url, {
        method: 'GET',
        headers: getAuthHeaders(),
    })

    return handleResponse<CandidateAdminResponse>(
        response,
        'Không thể tải thông tin chi tiết ứng viên.',
    )
}

export async function updateUserStatus(
    userId: number,
    request: UpdateUserStatusRequest,
): Promise<void> {
    const url = `${API_BASE_URL}/admin/users/${userId}/status`
    const response = await fetch(url, {
        method: 'PATCH',
        headers: getAuthHeaders(),
        body: JSON.stringify(request),
    })

    await handleResponse(response, 'Không thể cập nhật trạng thái tài khoản.')
}

export async function getAdminRecruiters(
    params: RecruiterFilterParams = {},
): Promise<PageResponse<RecruiterAdminResponse>> {
    const query = new URLSearchParams()
    if (params.page !== undefined) query.set('page', String(params.page))
    if (params.size !== undefined) query.set('size', String(params.size))
    if (params.keyword?.trim()) query.set('keyword', params.keyword.trim())

    const url = `${API_BASE_URL}/admin/recruiters?${query.toString()}`
    const response = await fetch(url, {
        method: 'GET',
        headers: getAuthHeaders(),
    })

    return handleResponse<PageResponse<RecruiterAdminResponse>>(
        response,
        'Không thể tải danh sách nhà tuyển dụng.',
    )
}

export async function getRecruiterById(id: number): Promise<RecruiterAdminResponse> {
    const url = `${API_BASE_URL}/admin/recruiters/${id}`
    const response = await fetch(url, {
        method: 'GET',
        headers: getAuthHeaders(),
    })

    return handleResponse<RecruiterAdminResponse>(
        response,
        'Không thể tải thông tin chi tiết nhà tuyển dụng.',
    )
}

export async function getAdminCompanies(
    params: CompanyFilterParams = {},
): Promise<PageResponse<CompanyAdminResponse>> {
    const query = new URLSearchParams()
    if (params.page !== undefined) query.set('page', String(params.page))
    if (params.size !== undefined) query.set('size', String(params.size))
    if (params.keyword?.trim()) query.set('keyword', params.keyword.trim())
    if (params.status) query.set('status', params.status)

    const url = `${API_BASE_URL}/admin/companies?${query.toString()}`
    const response = await fetch(url, {
        method: 'GET',
        headers: getAuthHeaders(),
    })

    return handleResponse<PageResponse<CompanyAdminResponse>>(
        response,
        'Không thể tải danh sách doanh nghiệp.',
    )
}

export async function getCompanyById(id: number): Promise<CompanyAdminResponse> {
    const url = `${API_BASE_URL}/admin/companies/${id}`
    const response = await fetch(url, {
        method: 'GET',
        headers: getAuthHeaders(),
    })

    return handleResponse<CompanyAdminResponse>(
        response,
        'Không thể tải thông tin chi tiết doanh nghiệp.',
    )
}

export async function updateCompanyStatus(
    companyId: number,
    request: UpdateCompanyStatusRequest,
): Promise<CompanyAdminResponse> {
    const url = `${API_BASE_URL}/admin/companies/${companyId}/status`
    const response = await fetch(url, {
        method: 'PATCH',
        headers: getAuthHeaders(),
        body: JSON.stringify(request),
    })

    return handleResponse<CompanyAdminResponse>(
        response,
        'Không thể cập nhật trạng thái doanh nghiệp.',
    )
}

export async function getAdminDashboardStats(): Promise<AdminDashboardStatsResponse> {
    const url = `${API_BASE_URL}/admin/dashboard/stats`
    const response = await fetch(url, {
        method: 'GET',
        headers: getAuthHeaders(),
    })

    return handleResponse<AdminDashboardStatsResponse>(
        response,
        'Không thể tải thống kê bảng điều khiển quản trị.',
    )
}
