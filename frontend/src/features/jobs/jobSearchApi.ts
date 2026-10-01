import type {
    ApiResponse,
    JobSearchParams,
    JobSummary,
    PageResponse,
} from './jobSearchTypes'

const API_BASE_URL = (
    import.meta.env.VITE_API_BASE_URL || '/api/v1'
).replace(/\/$/, '')

export class JobSearchApiError extends Error {
    readonly status: number

    constructor(status: number, message: string) {
        super(message)
        this.name = 'JobSearchApiError'
        this.status = status
    }
}

export function buildJobSearchQuery(params: JobSearchParams = {}): string {
    const query = new URLSearchParams()
    if (params.keyword?.trim()) query.set('keyword', params.keyword.trim())
    if (params.location?.trim()) query.set('location', params.location.trim())
    if (params.jobType) query.set('jobType', params.jobType)
    if (params.experienceLevel) query.set('experienceLevel', params.experienceLevel)
    if (params.minSalary !== undefined) query.set('minSalary', String(params.minSalary))
    if (params.maxSalary !== undefined) query.set('maxSalary', String(params.maxSalary))
    if (params.sort) query.set('sort', params.sort)
    if (params.sortBy) query.set('sortBy', params.sortBy)
    if (params.sortDirection) query.set('sortDirection', params.sortDirection)
    query.set('page', String(params.page ?? 1))
    query.set('size', String(params.size ?? 10))
    return query.toString()
}

export async function searchJobs(
    params: JobSearchParams = {},
): Promise<PageResponse<JobSummary>> {
    const response = await fetch(`${API_BASE_URL}/jobs?${buildJobSearchQuery(params)}`, {
        method: 'GET',
        headers: { Accept: 'application/json' },
    })

    let body: ApiResponse<PageResponse<JobSummary>> | undefined
    try {
        body = (await response.json()) as ApiResponse<PageResponse<JobSummary>>
    } catch {
        body = undefined
    }

    if (!response.ok) {
        throw new JobSearchApiError(
            response.status,
            body?.message || 'Không thể tải danh sách việc làm.',
        )
    }

    if (!body?.data) {
        throw new JobSearchApiError(502, 'Phản hồi từ máy chủ không chứa danh sách việc làm.')
    }

    return body.data
}
