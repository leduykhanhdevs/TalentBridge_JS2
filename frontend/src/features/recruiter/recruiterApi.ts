import type {
    ApiResponse,
    CompanyFilterParams,
    CompanyJoinRequestFilterParams,
    CompanyJoinRequestResponse,
    CompanyResponse,
    PageResponse,
    RecruiterProfile,
    RequestCreateCompanyRequest,
    ReviewJoinRequest,
    SubmitJoinCompanyRequest,
    UpdateRecruiterProfileRequest,
} from './recruiterTypes'
import { getAccessToken } from '../auth/tokenStorage'

export class RecruiterApiError extends Error {
    readonly status: number

    constructor(status: number, message: string) {
        super(message)
        this.name = 'RecruiterApiError'
        this.status = status
    }
}

const API_BASE_URL = (
    import.meta.env.VITE_API_BASE_URL || '/api/v1'
).replace(/\/$/, '')

function getAuthHeaders(): HeadersInit {
    const token = getAccessToken()
    if (!token) {
        throw new RecruiterApiError(401, 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.')
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
        throw new RecruiterApiError(response.status, errorMsg)
    }

    if (body?.data !== undefined) {
        return body.data
    }

    return {} as T
}

export async function getRecruiterProfile(): Promise<RecruiterProfile> {
    const url = `${API_BASE_URL}/recruiters/profile`
    const response = await fetch(url, {
        method: 'GET',
        headers: getAuthHeaders(),
    })

    return handleResponse<RecruiterProfile>(
        response,
        'Không thể tải hồ sơ nhà tuyển dụng.',
    )
}

export async function updateRecruiterProfile(
    request: UpdateRecruiterProfileRequest,
): Promise<RecruiterProfile> {
    const url = `${API_BASE_URL}/recruiters/profile`
    const response = await fetch(url, {
        method: 'PUT',
        headers: getAuthHeaders(),
        body: JSON.stringify(request),
    })

    return handleResponse<RecruiterProfile>(
        response,
        'Không thể cập nhật hồ sơ nhà tuyển dụng.',
    )
}

export async function requestCreateCompany(
    request: RequestCreateCompanyRequest,
): Promise<CompanyResponse> {
    const url = `${API_BASE_URL}/recruiters/companies/request-create`
    const response = await fetch(url, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(request),
    })

    return handleResponse<CompanyResponse>(
        response,
        'Không thể gửi yêu cầu tạo doanh nghiệp.',
    )
}

export async function searchApprovedCompanies(
    params: CompanyFilterParams = {},
): Promise<PageResponse<CompanyResponse>> {
    const query = new URLSearchParams()
    if (params.page !== undefined) query.set('page', String(params.page))
    if (params.size !== undefined) query.set('size', String(params.size))
    if (params.keyword?.trim()) query.set('keyword', params.keyword.trim())
    if (params.sortBy) query.set('sortBy', params.sortBy)
    if (params.sortDirection) query.set('sortDirection', params.sortDirection)

    const url = `${API_BASE_URL}/recruiters/companies/search?${query.toString()}`
    const response = await fetch(url, {
        method: 'GET',
        headers: getAuthHeaders(),
    })

    return handleResponse<PageResponse<CompanyResponse>>(
        response,
        'Không thể tìm kiếm doanh nghiệp.',
    )
}

export async function submitJoinCompanyRequest(
    companyId: number,
    request: SubmitJoinCompanyRequest,
): Promise<CompanyJoinRequestResponse> {
    const url = `${API_BASE_URL}/recruiters/companies/${companyId}/join-request`
    const response = await fetch(url, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(request),
    })

    return handleResponse<CompanyJoinRequestResponse>(
        response,
        'Không thể gửi yêu cầu xin gia nhập công ty.',
    )
}

export async function getCompanyJoinRequests(
    params: CompanyJoinRequestFilterParams = {},
): Promise<PageResponse<CompanyJoinRequestResponse>> {
    const query = new URLSearchParams()
    if (params.page !== undefined) query.set('page', String(params.page))
    if (params.size !== undefined) query.set('size', String(params.size))
    if (params.status) query.set('status', params.status)

    const url = `${API_BASE_URL}/recruiters/companies/my-company/join-requests?${query.toString()}`
    const response = await fetch(url, {
        method: 'GET',
        headers: getAuthHeaders(),
    })

    return handleResponse<PageResponse<CompanyJoinRequestResponse>>(
        response,
        'Không thể tải danh sách yêu cầu gia nhập công ty.',
    )
}

export async function reviewJoinRequest(
    requestId: number,
    request: ReviewJoinRequest,
): Promise<CompanyJoinRequestResponse> {
    const url = `${API_BASE_URL}/recruiters/companies/join-requests/${requestId}`
    const response = await fetch(url, {
        method: 'PATCH',
        headers: getAuthHeaders(),
        body: JSON.stringify(request),
    })

    return handleResponse<CompanyJoinRequestResponse>(
        response,
        'Không thể xử lý yêu cầu gia nhập công ty.',
    )
}
