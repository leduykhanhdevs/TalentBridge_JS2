import { describe, it, expect } from 'vitest'
import {
    formatJobSalary,
    validateJobForm,
} from '../recruiterJobValidation'
import { RecruiterJobApiError } from '../recruiterJobApi'
import type { CreateJobPayload } from '../recruiterJobTypes'

describe('Recruiter Job Validation and Formatters (HRPM-51, HRPM-52, HRPM-53, HRPM-54)', () => {
    describe('RecruiterJobApiError', () => {
        it('should correctly capture error properties', () => {
            const error = new RecruiterJobApiError(400, 'Mức lương không hợp lệ')
            expect(error.name).toBe('RecruiterJobApiError')
            expect(error.status).toBe(400)
            expect(error.message).toBe('Mức lương không hợp lệ')
        })
    })

    describe('validateJobForm', () => {
        const futureDate = new Date()
        futureDate.setDate(futureDate.getDate() + 15)
        const validDeadline = futureDate.toISOString().split('T')[0]

        const validJobPayload: CreateJobPayload = {
            title: 'Senior Java Backend Engineer',
            description: 'Phát triển hệ thống microservices với Spring Boot...',
            city: 'Hồ Chí Minh',
            jobType: 'FULL_TIME',
            experienceLevel: '1_TO_3_YEARS',
            deadline: validDeadline,
            minSalary: 20000000,
            maxSalary: 35000000,
            isNegotiable: false,
        }

        it('should pass validation when all required fields are valid', () => {
            const errors = validateJobForm(validJobPayload)
            expect(errors).toHaveLength(0)
        })

        it('should reject when title is missing or empty', () => {
            const errors = validateJobForm({ ...validJobPayload, title: '' })
            expect(errors.some((e) => e.field === 'title')).toBe(true)
            expect(errors.find((e) => e.field === 'title')?.message).toBe('Tiêu đề công việc không được để trống')
        })

        it('should reject when title exceeds 200 characters', () => {
            const errors = validateJobForm({ ...validJobPayload, title: 'a'.repeat(201) })
            expect(errors.some((e) => e.field === 'title')).toBe(true)
            expect(errors.find((e) => e.field === 'title')?.message).toBe('Tiêu đề công việc không được vượt quá 200 ký tự')
        })

        it('should reject when description is empty', () => {
            const errors = validateJobForm({ ...validJobPayload, description: '   ' })
            expect(errors.some((e) => e.field === 'description')).toBe(true)
        })

        it('should reject when city is empty', () => {
            const errors = validateJobForm({ ...validJobPayload, city: '' })
            expect(errors.some((e) => e.field === 'city')).toBe(true)
        })

        it('should reject when deadline is in the past or today', () => {
            const pastDate = new Date()
            pastDate.setDate(pastDate.getDate() - 1)
            const errors = validateJobForm({
                ...validJobPayload,
                deadline: pastDate.toISOString().split('T')[0],
            })
            expect(errors.some((e) => e.field === 'deadline')).toBe(true)
            expect(errors.find((e) => e.field === 'deadline')?.message).toBe('Hạn nộp hồ sơ phải sau ngày hiện tại')
        })

        it('should reject when minSalary is negative', () => {
            const errors = validateJobForm({
                ...validJobPayload,
                minSalary: -5000000,
            })
            expect(errors.some((e) => e.field === 'minSalary')).toBe(true)
        })

        it('should reject when maxSalary is negative', () => {
            const errors = validateJobForm({
                ...validJobPayload,
                maxSalary: -1000,
            })
            expect(errors.some((e) => e.field === 'maxSalary')).toBe(true)
        })

        it('should reject when minSalary is greater than maxSalary', () => {
            const errors = validateJobForm({
                ...validJobPayload,
                minSalary: 40000000,
                maxSalary: 20000000,
            })
            expect(errors.some((e) => e.field === 'maxSalary')).toBe(true)
            expect(errors.find((e) => e.field === 'maxSalary')?.message).toBe(
                'Mức lương tối thiểu không được lớn hơn mức lương tối đa',
            )
        })
    })

    describe('formatJobSalary', () => {
        it('should format negotiable salary', () => {
            expect(formatJobSalary(null, null, true)).toBe('Thỏa thuận')
            expect(formatJobSalary(10000000, 20000000, true)).toBe('Thỏa thuận')
        })

        it('should format salary range in millions', () => {
            expect(formatJobSalary(15000000, 25000000, false)).toBe('15 tr - 25 tr')
        })

        it('should format min salary only', () => {
            expect(formatJobSalary(20000000, null, false)).toBe('Từ 20 tr')
        })

        it('should format max salary only', () => {
            expect(formatJobSalary(null, 30000000, false)).toBe('Lên đến 30 tr')
        })

        it('should return Thỏa thuận if both are null', () => {
            expect(formatJobSalary(null, null, false)).toBe('Thỏa thuận')
        })
    })
})
