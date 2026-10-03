export interface JobApplicant {
    id: number
    jobId: number
    jobTitle?: string
    candidateId: number
    candidateFullName: string
    candidateEmail: string
    candidatePhone?: string
    candidateAvatarUrl?: string
    candidateTitle?: string
    candidateExperienceYears?: number
    candidateCity?: string
    resumeId?: number
    resumeUrl?: string
    resumeFileName?: string
    coverLetter?: string
    currentStage: string
    status: string
    aiMatchScore?: number
    appliedAt: string
    updatedAt?: string
    averageRating?: number | null
    notesCount: number
}

export interface ApplicationNote {
    id: number
    applicationId: number
    recruiterId: number
    recruiterName?: string
    rating?: number | null
    tag?: string | null
    comment: string
    createdAt: string
}

export interface ApplicationStage {
    id: number
    applicationId: number
    stage: string
    note?: string | null
    changedByUserId: number
    changedByUserName?: string
    changedAt: string
}

export interface ApplicantFilterParams {
    keyword?: string
    stage?: string
    minExperience?: number
    sortBy?: 'appliedDate' | 'score' | 'experience' | 'fullName' | string
    sortDirection?: 'ASC' | 'DESC' | string
}

export interface UpdateStagePayload {
    stage: string
    status?: string
    note?: string
}

export interface AddNotePayload {
    rating?: number
    tag?: string
    comment: string
}

export const APPLICANT_STAGE_CONFIG: Record<
    string,
    { label: string; badgeClass: string; desc: string }
> = {
    APPLIED: {
        label: 'Mới nộp',
        badgeClass: 'border-blue-200 bg-blue-50 text-blue-700',
        desc: 'Hồ sơ mới nộp vào hệ thống',
    },
    SCREENING: {
        label: 'Đang xem xét',
        badgeClass: 'border-indigo-200 bg-indigo-50 text-indigo-700',
        desc: 'Giai đoạn sàng lọc cũ; hệ thống quy đổi sang Đang xem xét',
    },
    REVIEWING: {
        label: 'Đang xem xét',
        badgeClass: 'border-indigo-200 bg-indigo-50 text-indigo-700',
        desc: 'HR đang xem xét và đánh giá hồ sơ',
    },
    SHORTLISTED: {
        label: 'Đạt sơ loại',
        badgeClass: 'border-purple-200 bg-purple-50 text-purple-700',
        desc: 'Đạt yêu cầu tiêu chí đầu vào',
    },
    INTERVIEW: {
        label: 'Phỏng vấn',
        badgeClass: 'border-amber-200 bg-amber-50 text-amber-700',
        desc: 'Được mời tham gia phỏng vấn',
    },
    OFFERED: {
        label: 'Đã gửi offer',
        badgeClass: 'border-teal-200 bg-teal-50 text-teal-700',
        desc: 'Đã gửi lời mời làm việc chính thức',
    },
    HIRED: {
        label: 'Đã tuyển dụng',
        badgeClass: 'border-emerald-200 bg-emerald-50 text-emerald-700',
        desc: 'Đã chấp nhận offer và gia nhập công ty',
    },
    REJECTED: {
        label: 'Từ chối',
        badgeClass: 'border-rose-200 bg-rose-50 text-rose-700',
        desc: 'Không phù hợp ở giai đoạn này',
    },
}

export interface InterviewItem {
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

export interface ScheduleInterviewPayload {
    interviewTime: string
    locationType: 'ONLINE' | 'OFFLINE' | string
    meetingLinkOrAddress: string
    notes?: string
}

