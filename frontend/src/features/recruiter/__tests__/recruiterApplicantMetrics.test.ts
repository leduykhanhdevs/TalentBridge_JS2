import { describe, expect, it } from 'vitest'
import { countApplicantsByStage } from '../recruiterApplicantMetrics'

describe('countApplicantsByStage', () => {
    it('counts by pipeline stage across the mapped application statuses', () => {
        expect(countApplicantsByStage([
            { currentStage: 'APPLIED', status: 'SUBMITTED' },
            { currentStage: 'INTERVIEW', status: 'ACTIVE' },
            { currentStage: 'HIRED', status: 'ACCEPTED' },
            { currentStage: 'REJECTED', status: 'REJECTED' },
        ])).toEqual({ APPLIED: 1, INTERVIEW: 1, HIRED: 1, REJECTED: 1 })
    })

    it('normalizes the legacy screening stage and keeps withdrawn applications out of the pipeline', () => {
        expect(countApplicantsByStage([
            { currentStage: 'SCREENING', status: 'ACTIVE' },
            { currentStage: 'INTERVIEW', status: 'WITHDRAWN' },
        ])).toEqual({ REVIEWING: 1 })
    })
})
