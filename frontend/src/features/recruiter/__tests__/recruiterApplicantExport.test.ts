import { describe, expect, it } from 'vitest'
import { buildRecruiterApplicantsCsv } from '../recruiterApplicantExport'
import type { JobApplicant } from '../recruiterApplicantTypes'

const applicant: JobApplicant = {
    id: 10,
    jobId: 4,
    candidateId: 2,
    candidateFullName: 'Nguyễn, "An"',
    candidateEmail: 'an@example.com',
    candidatePhone: '0900000000',
    candidateTitle: '=HYPERLINK("https://bad.example")',
    candidateExperienceYears: 3,
    candidateCity: 'Hồ Chí Minh',
    currentStage: 'INTERVIEW',
    status: 'ACTIVE',
    appliedAt: '2026-10-23T08:00:00',
    notesCount: 0,
}

describe('recruiter applicant CSV export', () => {
    it('preserves Vietnamese and CSV punctuation while neutralizing spreadsheet formulas', () => {
        const csv = buildRecruiterApplicantsCsv([applicant])
        expect(csv.startsWith('\uFEFF')).toBe(true)
        expect(csv).toContain('"Nguyễn, ""An"""')
        expect(csv).toContain("'=HYPERLINK")
        expect(csv).toContain('"Hồ Chí Minh"')
    })
})
