import { getAccessToken } from '../auth/tokenStorage'
import type { CandidateJobAssessment, JobQualityReport, RecruiterPipelineAnalytics } from './statisticsTypes'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || '/api/v1').replace(/\/$/, '')

export class StatisticsApiError extends Error {
    readonly status: number

    constructor(message: string, status: number) {
        super(message)
        this.name = 'StatisticsApiError'
        this.status = status
    }
}

interface ApiEnvelope<T> {
    message?: string
    data?: T
}

async function getData<T>(path: string): Promise<T> {
    const token = getAccessToken()
    if (!token) throw new StatisticsApiError('Phiên đăng nhập đã hết hạn.', 401)
    const response = await fetch(`${API_BASE_URL}${path}`, {
        headers: { Authorization: `Bearer ${token}` },
    })
    let body: ApiEnvelope<T> | undefined
    try {
        body = await response.json() as ApiEnvelope<T>
    } catch {
        body = undefined
    }
    if (!response.ok) throw new StatisticsApiError(body?.message || 'Không thể tải thống kê.', response.status)
    if (body?.data === undefined) throw new StatisticsApiError('Phản hồi thống kê không hợp lệ.', response.status)
    return body.data
}

export function getJobQualityReport(jobId: number): Promise<JobQualityReport> {
    return getData(`/statistics/recruiter/jobs/${jobId}/quality`)
}

export function getCandidateJobAssessment(jobId: number, candidateId: number): Promise<CandidateJobAssessment> {
    return getData(`/statistics/recruiter/jobs/${jobId}/candidates/${candidateId}`)
}

export function getRecruiterPipeline(jobId: number): Promise<RecruiterPipelineAnalytics> {
    return getData(`/statistics/recruiter/jobs/${jobId}/pipeline`)
}
