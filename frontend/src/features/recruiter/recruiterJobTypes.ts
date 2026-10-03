export type JobStatus = 'DRAFT' | 'PENDING' | 'ACTIVE' | 'EXPIRED' | 'CLOSED' | 'REJECTED'

export interface MyJobStats {
    total: number
    draft: number
    pending: number
    active: number
    rejected: number
    expired: number
    closed: number
}

export interface RecruiterJobItem {
    id: number
    companyId: number
    companyName: string
    title: string
    description: string
    requirements?: string | null
    benefits?: string | null
    location?: string | null
    city: string
    address?: string | null
    jobType: string
    experienceLevel: string
    minSalary?: number | null
    maxSalary?: number | null
    isNegotiable?: boolean | null
    deadline: string
    status: JobStatus
    recruiterUserId?: number
    skills?: string[]
    createdAt?: string | null
    updatedAt?: string | null
}

export interface CreateJobPayload {
    title: string
    description: string
    requirements?: string
    benefits?: string
    location?: string
    city: string
    address?: string
    jobType: string
    experienceLevel: string
    minSalary?: number | null
    maxSalary?: number | null
    isNegotiable?: boolean
    deadline: string
    skills?: string[]
}

export type UpdateJobPayload = CreateJobPayload

export interface RecruiterJobFilterParams {
    page?: number
    size?: number
    status?: JobStatus | ''
}
