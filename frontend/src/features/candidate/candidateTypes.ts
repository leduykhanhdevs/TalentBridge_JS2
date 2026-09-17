export interface CandidateProfileResponse {
    id: number
    userId: number
    fullName: string
    email: string
    phone: string | null
    avatarUrl: string | null
    status: string
    title: string | null
    dob: string | null
    gender: string | null
    summary: string | null
    experienceYears: number | null
    currentSalary: number | null
    expectedSalary: number | null
    city: string | null
    address: string | null
    personalWebsite: string | null
    linkedinUrl: string | null
    githubUrl: string | null
    createdAt: string | null
    updatedAt: string | null
}

export interface UpdateCandidateProfileRequest {
    fullName?: string
    phone?: string
    avatarUrl?: string
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
}

export interface CandidateProfileFormErrors {
    fullName?: string
    phone?: string
    experienceYears?: string
    currentSalary?: string
    expectedSalary?: string
    website?: string
    github?: string
    linkedin?: string
}
