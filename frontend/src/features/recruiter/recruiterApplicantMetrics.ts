import type { JobApplicant } from './recruiterApplicantTypes'

export function countApplicantsByStage(applicants: Pick<JobApplicant, 'currentStage' | 'status'>[]) {
    const counts: Record<string, number> = {}
    for (const applicant of applicants) {
        if (applicant.status === 'WITHDRAWN') continue
        const stage = applicant.currentStage === 'SCREENING' ? 'REVIEWING' : applicant.currentStage
        counts[stage] = (counts[stage] || 0) + 1
    }
    return counts
}
