import type { ApiResponse, JobDetail } from './jobTypes'

const API_BASE_URL = (
    import.meta.env.VITE_API_BASE_URL || '/api/v1'
).replace(/\/$/, '')

export class JobApiError extends Error {
    readonly status: number

    constructor(status: number, message: string) {
        super(message)
        this.name = 'JobApiError'
        this.status = status
    }
}

export async function getJobDetail(jobId: number): Promise<JobDetail> {
    const response = await fetch(`${API_BASE_URL}/jobs/${jobId}`, {
        method: 'GET',
        headers: {
            Accept: 'application/json',
        },
    })

    let body: ApiResponse<JobDetail> | undefined
    try {
        body = (await response.json()) as ApiResponse<JobDetail>
    } catch {
        body = undefined
    }

    if (!response.ok) {
        throw new JobApiError(
            response.status,
            body?.message || 'Không thể tải thông tin việc làm.',
        )
    }

    if (!body?.data) {
        throw new JobApiError(502, 'Phản hồi từ máy chủ không chứa dữ liệu việc làm.')
    }

    return body.data
}
