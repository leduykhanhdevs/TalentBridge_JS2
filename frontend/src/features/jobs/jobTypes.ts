export interface ApiResponse<T> {
    statusCode: number
    message: string
    data?: T
    timestamp?: string
}

export interface JobDetail {
    id: number
    companyId: number
    companyName: string
    title: string
    description: string
    requirements: string | null
    benefits: string | null
    location: string | null
    jobType: string | null
    experienceLevel: string | null
    minSalary: number | null
    maxSalary: number | null
    deadline: string | null
    status: string
    createdAt: string | null
    updatedAt: string | null
}
