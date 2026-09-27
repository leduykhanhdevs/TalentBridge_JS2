import type { JobSearchParams } from './jobSearchTypes'

export interface JobFilterFormValues {
    keyword: string
    location: string
    jobType: string
    experienceLevel: string
    minSalaryMillions: string
    maxSalaryMillions: string
}

export const EMPTY_JOB_FILTERS: JobFilterFormValues = {
    keyword: '',
    location: '',
    jobType: '',
    experienceLevel: '',
    minSalaryMillions: '',
    maxSalaryMillions: '',
}

function parseMillions(value: string): number | undefined {
    if (!value.trim()) return undefined
    const amount = Number(value)
    return Number.isFinite(amount) ? amount * 1_000_000 : undefined
}

export function validateJobFilters(values: JobFilterFormValues): string | null {
    const min = values.minSalaryMillions.trim()
        ? Number(values.minSalaryMillions)
        : undefined
    const max = values.maxSalaryMillions.trim()
        ? Number(values.maxSalaryMillions)
        : undefined

    if ((min !== undefined && (!Number.isFinite(min) || min < 0))
        || (max !== undefined && (!Number.isFinite(max) || max < 0))) {
        return 'Mức lương phải là số không âm.'
    }
    if (min !== undefined && max !== undefined && min > max) {
        return 'Mức lương tối thiểu không được lớn hơn mức lương tối đa.'
    }
    return null
}

export function toJobSearchParams(values: JobFilterFormValues): JobSearchParams {
    return {
        keyword: values.keyword.trim() || undefined,
        location: values.location.trim() || undefined,
        jobType: values.jobType as JobSearchParams['jobType'] || undefined,
        experienceLevel: values.experienceLevel as JobSearchParams['experienceLevel'] || undefined,
        minSalary: parseMillions(values.minSalaryMillions),
        maxSalary: parseMillions(values.maxSalaryMillions),
        page: 1,
        size: 10,
    }
}
