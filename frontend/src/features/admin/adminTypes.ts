export type UserStatus = 'ACTIVE' | 'BANNED'
export type CompanyStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

export type PageResponse<T> = {
    content: T[]
    pageNumber: number
    pageSize: number
    totalElements: number
    totalPages: number
    isLast: boolean
}

export type ApiResponse<T> = {
    statusCode: number
    message: string
    data?: T
    timestamp?: string
}

export type CandidateAdminResponse = {
    id: number
    userId: number
    fullName: string
    email: string
    phone?: string
    avatarUrl?: string
    status: UserStatus
    title?: string
    dob?: string
    gender?: string
    summary?: string
    experienceYears?: number
    currentSalary?: number
    expectedSalary?: number
    city?: string
    address?: string
    personalWebsite?: string
    linkedinUrl?: string
    githubUrl?: string
    createdAt: string
    updatedAt?: string
}

export type RecruiterAdminResponse = {
    id: number
    userId: number
    fullName: string
    email: string
    phone?: string
    avatarUrl?: string
    status: UserStatus
    position?: string
    companyId?: number
    companyName?: string
    companyLogoUrl?: string
    companyStatus?: CompanyStatus
    createdAt: string
}

export type CompanyAdminResponse = {
    id: number
    name: string
    logoUrl?: string
    website?: string
    companySize?: string
    address?: string
    city?: string
    taxCode?: string
    description?: string
    status: CompanyStatus
    createdByUserId?: number
    createdAt?: string
}

export type AdminDashboardStatsResponse = {
    totalUsers: number
    totalCompanies: number
    pendingCompanies: number
    totalJobs: number
    activeJobs: number
    pendingJobs: number
}

export type UpdateUserStatusRequest = {
    status: UserStatus
    reason?: string
}

export type UpdateCompanyStatusRequest = {
    status: CompanyStatus
    reason?: string
}

export type CandidateFilterParams = {
    page?: number
    size?: number
    keyword?: string
    status?: UserStatus
}

export type RecruiterFilterParams = {
    page?: number
    size?: number
    keyword?: string
}

export type CompanyFilterParams = {
    page?: number
    size?: number
    keyword?: string
    status?: CompanyStatus
}
