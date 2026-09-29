import type { CreateJobPayload } from './recruiterJobTypes'

export interface JobValidationError {
    field: string
    message: string
}

export function validateJobForm(payload: Partial<CreateJobPayload>): JobValidationError[] {
    const errors: JobValidationError[] = []

    if (!payload.title || !payload.title.trim()) {
        errors.push({ field: 'title', message: 'Tiêu đề công việc không được để trống' })
    } else if (payload.title.trim().length > 200) {
        errors.push({ field: 'title', message: 'Tiêu đề công việc không được vượt quá 200 ký tự' })
    }

    if (!payload.description || !payload.description.trim()) {
        errors.push({ field: 'description', message: 'Mô tả công việc không được để trống' })
    }

    if (!payload.city || !payload.city.trim()) {
        errors.push({ field: 'city', message: 'Thành phố làm việc không được để trống' })
    }

    if (!payload.jobType || !payload.jobType.trim()) {
        errors.push({ field: 'jobType', message: 'Hình thức làm việc không được để trống' })
    }

    if (!payload.experienceLevel || !payload.experienceLevel.trim()) {
        errors.push({ field: 'experienceLevel', message: 'Yêu cầu kinh nghiệm không được để trống' })
    }

    if (!payload.deadline) {
        errors.push({ field: 'deadline', message: 'Hạn nộp hồ sơ không được để trống' })
    } else {
        const deadlineDate = new Date(payload.deadline)
        const today = new Date()
        today.setHours(0, 0, 0, 0)
        if (deadlineDate <= today) {
            errors.push({ field: 'deadline', message: 'Hạn nộp hồ sơ phải sau ngày hiện tại' })
        }
    }

    if (payload.minSalary !== undefined && payload.minSalary !== null && payload.minSalary < 0) {
        errors.push({ field: 'minSalary', message: 'Mức lương tối thiểu không được âm' })
    }

    if (payload.maxSalary !== undefined && payload.maxSalary !== null && payload.maxSalary < 0) {
        errors.push({ field: 'maxSalary', message: 'Mức lương tối đa không được âm' })
    }

    if (
        payload.minSalary !== undefined &&
        payload.minSalary !== null &&
        payload.maxSalary !== undefined &&
        payload.maxSalary !== null &&
        payload.minSalary > payload.maxSalary
    ) {
        errors.push({
            field: 'maxSalary',
            message: 'Mức lương tối thiểu không được lớn hơn mức lương tối đa',
        })
    }

    return errors
}

export const JOB_TYPE_LABELS: Record<string, string> = {
    FULL_TIME: 'Toàn thời gian',
    PART_TIME: 'Bán thời gian',
    REMOTE: 'Làm việc từ xa',
    INTERNSHIP: 'Thực tập',
}

export const EXPERIENCE_LABELS: Record<string, string> = {
    NO_EXPERIENCE: 'Không yêu cầu',
    UNDER_1_YEAR: 'Dưới 1 năm',
    '1_TO_3_YEARS': '1 - 3 năm',
    '3_TO_5_YEARS': '3 - 5 năm',
    OVER_5_YEARS: 'Trên 5 năm',
    INTERN: 'Thực tập sinh',
    FRESHER: 'Mới tốt nghiệp (Fresher)',
    JUNIOR: 'Junior (1-2 năm)',
    MIDDLE: 'Middle (2-4 năm)',
    SENIOR: 'Senior (5+ năm)',
    LEAD: 'Trưởng nhóm (Lead)',
}

export function formatJobSalary(
    minSalary?: number | null,
    maxSalary?: number | null,
    isNegotiable?: boolean | null,
): string {
    if (isNegotiable) {
        return 'Thỏa thuận'
    }
    if (!minSalary && !maxSalary) {
        return 'Thỏa thuận'
    }
    const formatNumber = (num: number) => {
        if (num >= 1_000_000) {
            const millions = num / 1_000_000
            return `${millions % 1 === 0 ? millions : millions.toFixed(1)} tr`
        }
        return num.toLocaleString('vi-VN') + ' đ'
    }

    if (minSalary && maxSalary) {
        return `${formatNumber(minSalary)} - ${formatNumber(maxSalary)}`
    }
    if (minSalary) {
        return `Từ ${formatNumber(minSalary)}`
    }
    if (maxSalary) {
        return `Lên đến ${formatNumber(maxSalary)}`
    }
    return 'Thỏa thuận'
}
