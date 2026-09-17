import type { CandidateProfileFormErrors } from './candidateTypes'

export interface CandidateProfileFormData {
    fullName: string
    phone: string
    title: string
    dob: string
    gender: string
    summary: string
    experienceYears: string | number
    currentSalary: string | number
    expectedSalary: string | number
    city: string
    address: string
    personalWebsite: string
    linkedinUrl: string
    githubUrl: string
}

const PHONE_REGEX = /^(0|\+84)[0-9]{9,10}$/
const URL_REGEX = /^(https?:\/\/)?([\da-z.-]+)\.([a-z.]{2,6})([/\w .-]*)*\/?$/

export function validateCandidateProfile(data: Partial<CandidateProfileFormData>): CandidateProfileFormErrors {
    const errors: CandidateProfileFormErrors = {}

    if (data.fullName !== undefined) {
        const trimmed = data.fullName.trim()
        if (!trimmed) {
            errors.fullName = 'Họ và tên không được để trống'
        } else if (trimmed.length < 2) {
            errors.fullName = 'Họ và tên phải có ít nhất 2 ký tự'
        } else if (trimmed.length > 100) {
            errors.fullName = 'Họ và tên không được vượt quá 100 ký tự'
        }
    }

    if (data.phone && data.phone.trim()) {
        if (!PHONE_REGEX.test(data.phone.trim())) {
            errors.phone = 'Số điện thoại không đúng định dạng (VD: 0912345678)'
        }
    }

    if (data.experienceYears !== undefined && data.experienceYears !== '') {
        const num = Number(data.experienceYears)
        if (isNaN(num) || num < 0) {
            errors.experienceYears = 'Số năm kinh nghiệm không được nhỏ hơn 0'
        }
    }

    if (data.currentSalary !== undefined && data.currentSalary !== '') {
        const num = Number(data.currentSalary)
        if (isNaN(num) || num < 0) {
            errors.currentSalary = 'Mức lương hiện tại không được nhỏ hơn 0'
        }
    }

    if (data.expectedSalary !== undefined && data.expectedSalary !== '') {
        const num = Number(data.expectedSalary)
        if (isNaN(num) || num < 0) {
            errors.expectedSalary = 'Mức lương mong muốn không được nhỏ hơn 0'
        }
    }

    if (data.personalWebsite && data.personalWebsite.trim()) {
        if (!URL_REGEX.test(data.personalWebsite.trim())) {
            errors.website = 'Địa chỉ website không hợp lệ'
        }
    }

    if (data.githubUrl && data.githubUrl.trim()) {
        if (!URL_REGEX.test(data.githubUrl.trim())) {
            errors.github = 'Đường dẫn GitHub không hợp lệ'
        }
    }

    if (data.linkedinUrl && data.linkedinUrl.trim()) {
        if (!URL_REGEX.test(data.linkedinUrl.trim())) {
            errors.linkedin = 'Đường dẫn LinkedIn không hợp lệ'
        }
    }

    return errors
}
