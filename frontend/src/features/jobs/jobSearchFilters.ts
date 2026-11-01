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

// Only supported public search values are restored from shareable URLs.
export function readJobSearchParams(query: URLSearchParams): JobSearchParams {
    const positiveInt = (value: string | null, fallback: number) => {
        const number = Number(value)
        return Number.isSafeInteger(number) && number > 0 ? number : fallback
    }
    const salary = (name: string) => {
        const value = query.get(name)
        if (!value?.trim()) return undefined
        const number = Number(value)
        return Number.isFinite(number) && number >= 0 ? number : undefined
    }
    const type = query.get('jobType')
    const experience = query.get('experienceLevel')
    const sort = query.get('sort')
    const size = positiveInt(query.get('size'), 10)
    return {
        keyword: query.get('keyword')?.trim() || undefined,
        location: query.get('location')?.trim() || undefined,
        jobType: ['FULL_TIME', 'PART_TIME', 'REMOTE', 'HYBRID'].includes(type ?? '')
            ? type as JobSearchParams['jobType'] : undefined,
        experienceLevel: ['NO_EXPERIENCE', 'UNDER_1_YEAR', 'INTERN', 'FRESHER', 'JUNIOR', 'MIDDLE', 'SENIOR'].includes(experience ?? '')
            ? experience as JobSearchParams['experienceLevel'] : undefined,
        minSalary: salary('minSalary'), maxSalary: salary('maxSalary'),
        page: positiveInt(query.get('page'), 1),
        size: [10, 20, 50].includes(size) ? size : 10,
        sort: ['NEWEST', 'SALARY_ASC', 'SALARY_DESC', 'TITLE_ASC'].includes(sort ?? '') ? sort! : 'NEWEST',
    }
}

export function toJobFilterFormValues(params: JobSearchParams): JobFilterFormValues {
    return {
        keyword: params.keyword ?? '', location: params.location ?? '',
        jobType: params.jobType ?? '', experienceLevel: params.experienceLevel ?? '',
        minSalaryMillions: params.minSalary === undefined ? '' : String(params.minSalary / 1_000_000),
        maxSalaryMillions: params.maxSalary === undefined ? '' : String(params.maxSalary / 1_000_000),
    }
}
