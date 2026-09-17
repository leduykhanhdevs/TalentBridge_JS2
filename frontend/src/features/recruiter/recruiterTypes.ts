export type RecruiterProfile = {
    id: number
    userId: number
    fullName: string
    email: string
    phone?: string
    avatarUrl?: string
    status: 'ACTIVE' | 'BANNED'
    position?: string
    companyId?: number
    companyName?: string
    companyLogoUrl?: string
    companyStatus?: 'PENDING' | 'APPROVED' | 'REJECTED'
    createdAt: string
}

export type UpdateRecruiterProfileRequest = {
    fullName?: string
    phone?: string
    avatarUrl?: string
    position?: string
}

export type CompanyResponse = {
    id: number
    name: string
    logoUrl?: string
    website?: string
    companySize?: string
    address?: string
    city?: string
    taxCode?: string
    description?: string
    status: 'PENDING' | 'APPROVED' | 'REJECTED'
    createdByUserId?: number
    createdAt?: string
}

export type RequestCreateCompanyRequest = {
    name: string
    taxCode?: string
    website?: string
    companySize?: string
    address?: string
    city?: string
    description?: string
    logoUrl?: string
}

export type CompanyFilterParams = {
    keyword?: string
    page?: number
    size?: number
    sortBy?: string
    sortDirection?: 'ASC' | 'DESC'
}

export type CompanyJoinRequestResponse = {
    id: number
    userId: number
    applicantName: string
    applicantEmail: string
    applicantPhone?: string
    applicantAvatarUrl?: string
    companyId: number
    companyName: string
    companyLogoUrl?: string
    position?: string
    message?: string
    status: 'PENDING' | 'ACCEPTED' | 'REJECTED'
    reason?: string
    approvedByUserId?: number
    createdAt: string
    updatedAt: string
}

export type SubmitJoinCompanyRequest = {
    position?: string
    message?: string
}

export type ReviewJoinRequest = {
    status: 'ACCEPTED' | 'REJECTED'
    reason?: string
}

export type CompanyJoinRequestFilterParams = {
    status?: 'PENDING' | 'ACCEPTED' | 'REJECTED'
    page?: number
    size?: number
}

export type ApiResponse<T> = {
    statusCode: number
    message: string
    data?: T
    timestamp?: string
}

export type PageResponse<T> = {
    content: T[]
    pageNumber: number
    pageSize: number
    totalElements: number
    totalPages: number
    isLast: boolean
}
