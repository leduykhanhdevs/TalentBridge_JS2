import { describe, it, expect } from 'vitest'
import {
    formatMatchScore,
    validateApplicantNote,
    validateStageTransition,
    VALID_STAGES,
} from '../recruiterApplicantValidation'
import { RecruiterApplicantApiError } from '../recruiterApplicantApi'
import { APPLICANT_STAGE_CONFIG } from '../recruiterApplicantTypes'

describe('Recruiter Applicant Screening Validation & Error Handling (HRPM-45, 46, 47, 48, 49)', () => {
    describe('RecruiterApplicantApiError', () => {
        it('should correctly capture status and message properties', () => {
            const error = new RecruiterApplicantApiError(403, 'Bạn không có quyền thao tác trên ứng viên này')
            expect(error.name).toBe('RecruiterApplicantApiError')
            expect(error.status).toBe(403)
            expect(error.message).toBe('Bạn không có quyền thao tác trên ứng viên này')
        })
    })

    describe('validateStageTransition (HRPM-48)', () => {
        it('should pass validation for valid stages', () => {
            for (const stage of VALID_STAGES) {
                const errors = validateStageTransition({ stage, note: 'Passed interview' })
                expect(errors).toHaveLength(0)
            }
        })

        it('should reject empty or whitespace stage', () => {
            const errors = validateStageTransition({ stage: '   ' })
            expect(errors.some((e) => e.field === 'stage')).toBe(true)
            expect(errors[0].message).toContain('không được để trống')
        })

        it('should reject invalid stage value', () => {
            const errors = validateStageTransition({ stage: 'INVALID_STAGE_XYZ' })
            expect(errors.some((e) => e.field === 'stage')).toBe(true)
            expect(errors[0].message).toContain('không hợp lệ')
        })

        it('should reject note exceeding 1000 characters', () => {
            const errors = validateStageTransition({
                stage: 'SHORTLISTED',
                note: 'a'.repeat(1001),
            })
            expect(errors.some((e) => e.field === 'note')).toBe(true)
        })
    })

    describe('validateApplicantNote (HRPM-49)', () => {
        it('should pass for valid note with rating and tag', () => {
            const errors = validateApplicantNote({
                rating: 5,
                tag: 'Potential',
                comment: 'Phỏng vấn xuất sắc, kiến thức chuyên sâu',
            })
            expect(errors).toHaveLength(0)
        })

        it('should pass for note without rating or tag', () => {
            const errors = validateApplicantNote({
                comment: 'Đã hẹn phỏng vấn thứ 6',
            })
            expect(errors).toHaveLength(0)
        })

        it('should reject empty comment', () => {
            const errors = validateApplicantNote({ comment: '   ' })
            expect(errors.some((e) => e.field === 'comment')).toBe(true)
            expect(errors[0].message).toContain('không được để trống')
        })

        it('should reject rating below 1 or above 5', () => {
            const errorLow = validateApplicantNote({ rating: 0, comment: 'test' })
            expect(errorLow.some((e) => e.field === 'rating')).toBe(true)

            const errorHigh = validateApplicantNote({ rating: 6, comment: 'test' })
            expect(errorHigh.some((e) => e.field === 'rating')).toBe(true)
        })

        it('should reject non-integer rating', () => {
            const errors = validateApplicantNote({ rating: 3.5, comment: 'test' })
            expect(errors.some((e) => e.field === 'rating')).toBe(true)
        })

        it('should reject tag exceeding 50 characters', () => {
            const errors = validateApplicantNote({
                tag: 'a'.repeat(51),
                comment: 'test',
            })
            expect(errors.some((e) => e.field === 'tag')).toBe(true)
        })
    })

    describe('formatMatchScore', () => {
        it('should format high match score (>=80) as emerald', () => {
            const res = formatMatchScore(85.5)
            expect(res.percentage).toBe(86)
            expect(res.label).toBe('Rất phù hợp')
            expect(res.colorClass).toContain('emerald')
        })

        it('should format medium match score (>=60) as blue', () => {
            const res = formatMatchScore(65)
            expect(res.percentage).toBe(65)
            expect(res.label).toBe('Phù hợp')
            expect(res.colorClass).toContain('blue')
        })

        it('should format undefined or null score gracefully', () => {
            const res = formatMatchScore(undefined)
            expect(res.percentage).toBe(0)
            expect(res.label).toBe('Chưa tính toán')
        })
    })

    describe('APPLICANT_STAGE_CONFIG (HRPM-45)', () => {
        it('should define all 7 standard ATS recruitment stages', () => {
            expect(Object.keys(APPLICANT_STAGE_CONFIG)).toEqual(
                expect.arrayContaining([
                    'APPLIED',
                    'REVIEWING',
                    'SHORTLISTED',
                    'INTERVIEW',
                    'OFFERED',
                    'HIRED',
                    'REJECTED',
                ])
            )
        })
    })
})
