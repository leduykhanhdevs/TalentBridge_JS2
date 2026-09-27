export type JobType = 'FULL_TIME' | 'PART_TIME' | 'REMOTE' | 'HYBRID'
export type ExperienceLevel = 'INTERN' | 'FRESHER' | 'JUNIOR' | 'MIDDLE' | 'SENIOR'

export interface ApiResponse<T> {
    statusCode: number
    message: string
    data?: T
    timestamp?: string
}

export interface PageResponse<T> {
    content: T[]
    pageNumber: number
    pageSize: number
    totalElements: number
    totalPages: number
    isLast: boolean
}

export interface JobSummary {
    id: number
    companyId: number
    companyName: string
    title: string
    description: string
    location: string | null
    city: string | null
    jobType: string | null
    experienceLevel: string | null
    minSalary: number | null
    maxSalary: number | null
    isNegotiable: boolean | null
    deadline: string | null
    status: string
    skills: string[] | null
    createdAt: string | null
}

export interface JobSearchParams {
    keyword?: string
    location?: string
    jobType?: JobType
    experienceLevel?: ExperienceLevel
    minSalary?: number
    maxSalary?: number
    page?: number
    size?: number
}
