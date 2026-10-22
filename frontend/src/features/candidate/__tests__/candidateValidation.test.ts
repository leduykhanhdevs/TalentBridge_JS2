import { describe, it, expect } from 'vitest'
import { validateCandidateProfile } from '../candidateValidation'

describe('Candidate Profile Validation Tests (HRPM-14)', () => {
    it('passes validation for complete valid candidate profile data', () => {
        const errors = validateCandidateProfile({
            fullName: 'Nguyễn Văn Ứng Viên',
            phone: '0912345678',
            title: 'Senior React Developer',
            experienceYears: 4,
            currentSalary: 25000000,
            expectedSalary: 35000000,
            personalWebsite: 'https://ungvien.dev',
            githubUrl: 'https://github.com/ungvien',
            linkedinUrl: 'https://linkedin.com/in/ungvien',
        })

        expect(Object.keys(errors)).toHaveLength(0)
    })

    it('validates empty and short full name', () => {
        expect(validateCandidateProfile({ fullName: '' }).fullName).toBe('Họ và tên không được để trống')
        expect(validateCandidateProfile({ fullName: 'A' }).fullName).toBe('Họ và tên phải có ít nhất 2 ký tự')
        expect(validateCandidateProfile({ fullName: 'a'.repeat(101) }).fullName).toBe('Họ và tên không được vượt quá 100 ký tự')
    })

    it('validates invalid phone number format', () => {
        expect(validateCandidateProfile({ phone: '12345' }).phone).toBeDefined()
        expect(validateCandidateProfile({ phone: '0912345678' }).phone).toBeUndefined()
        expect(validateCandidateProfile({ phone: '+84912345678' }).phone).toBeUndefined()
    })

    it('rejects negative experience years or negative salaries', () => {
        expect(validateCandidateProfile({ experienceYears: -2 }).experienceYears).toBeDefined()
        expect(validateCandidateProfile({ currentSalary: -5000000 }).currentSalary).toBeDefined()
        expect(validateCandidateProfile({ expectedSalary: -1000 }).expectedSalary).toBeDefined()
    })

    it('validates website and social links formatting', () => {
        expect(validateCandidateProfile({ personalWebsite: 'not-a-url' }).website).toBeDefined()
        expect(validateCandidateProfile({ githubUrl: 'not-a-url' }).github).toBeDefined()
        expect(validateCandidateProfile({ linkedinUrl: 'not-a-url' }).linkedin).toBeDefined()
    })

    it('rejects future date of birth and accepts valid past or today date of birth (TB-03)', () => {
        // Future date must be rejected
        const futureErrors = validateCandidateProfile({ dob: '2030-02-01' })
        expect(futureErrors.dob).toBe('Ngày sinh không được ở trong tương lai')

        // Valid past date must be accepted
        const pastErrors = validateCandidateProfile({ dob: '2000-07-19' })
        expect(pastErrors.dob).toBeUndefined()

        // Today date in Vietnam timezone must be accepted
        const todayStr = new Intl.DateTimeFormat('en-CA', {
            timeZone: 'Asia/Ho_Chi_Minh',
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
        }).format(new Date())
        const todayErrors = validateCandidateProfile({ dob: todayStr })
        expect(todayErrors.dob).toBeUndefined()
    })
})
