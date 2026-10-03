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
    /** @deprecated Derived from work experience; retained for backward request compatibility and ignored by the server. */
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

export interface WorkExperience {
    id: number
    candidateId: number
    companyName: string
    position: string
    startDate: string
    endDate: string | null
    isCurrent: boolean
    description: string | null
    achievements: string | null
    createdAt?: string
    updatedAt?: string
}

export interface WorkExperienceRequest {
    companyName: string
    position: string
    startDate: string
    endDate?: string | null
    isCurrent?: boolean
    description?: string
    achievements?: string
}

export interface CandidateSkill {
    id: number
    candidateId: number
    skillId: number
    skillName: string
    proficiencyLevel: string
    rating: number
    yearsOfExperience: number
}

export interface CandidateSkillRequest {
    skillId?: number
    skillName?: string
    proficiencyLevel?: string
    rating: number
    yearsOfExperience?: number
}

export interface SkillItem {
    id: number
    name: string
}

export interface ResumeItem {
    id: number
    candidateId: number
    title: string
    fileName: string
    fileUrl: string
    fileType: string
    resumeType: string
    isDefault: boolean
    createdAt: string
}

export type ApplicationStage = 'APPLIED' | 'REVIEWING' | 'SCREENING' | 'SHORTLISTED' | 'INTERVIEW' | 'OFFERED' | 'HIRED' | 'REJECTED'
export type ApplicationStatus = 'SUBMITTED' | 'ACTIVE' | 'ACCEPTED' | 'REJECTED' | 'WITHDRAWN'

export interface CandidateApplicationItem {
    id: number
    jobId: number
    jobTitle: string
    companyId: number | null
    companyName: string
    companyLogo: string | null
    location: string | null
    city: string | null
    jobType: string | null
    experienceLevel: string | null
    minSalary: number | null
    maxSalary: number | null
    isNegotiable: boolean | null
    resumeId: number | null
    resumeFileName: string | null
    resumeFileUrl: string | null
    coverLetter: string | null
    currentStage: ApplicationStage
    status: ApplicationStatus
    aiMatchScore: number | null
    appliedAt: string
}

export interface CvTemplate {
    id: number
    name: string
    templateCode: string
    thumbnailUrl: string | null
    description: string | null
    defaultConfig: string | null
    isActive: boolean
}

export interface GenerateResumePayload {
    templateCode: string
    title: string
    primaryColor?: string
    customizationJson?: string
    htmlContent?: string
}

export interface ParsedCvExperience {
    companyName: string
    position: string
    startDate: string
    endDate: string | null
    isCurrent: boolean
    description: string
}

export interface ParsedCvResult {
    fullName: string
    email: string
    phone: string
    title: string
    city: string
    summary: string
    skills: string[]
    experiences: ParsedCvExperience[]
    rawText: string
}

export interface ApplyParsedCvPayload {
    fullName?: string
    phone?: string
    title?: string
    city?: string
    summary?: string
    skills?: string[]
    experiences?: {
        companyName: string
        position: string
        startDate: string
        endDate?: string | null
        isCurrent?: boolean
        description?: string
    }[]
}

export interface CandidateInterviewItem {
    id: number
    applicationId: number
    jobId: number
    jobTitle: string
    companyName: string
    candidateName: string
    candidateEmail: string
    interviewTime: string
    locationType: 'ONLINE' | 'OFFLINE' | string
    meetingLinkOrAddress: string
    notes?: string
    status: 'SCHEDULED' | 'COMPLETED' | 'CANCELLED' | string
    googleCalendarUrl: string
    createdAt: string
}


