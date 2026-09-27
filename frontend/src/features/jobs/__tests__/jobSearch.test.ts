import { describe, expect, it } from 'vitest'
import { buildJobSearchQuery } from '../jobSearchApi'
import {
    EMPTY_JOB_FILTERS,
    toJobSearchParams,
    validateJobFilters,
} from '../jobSearchFilters'

describe('HRPM-35 job filters', () => {
    it('builds a trimmed API query containing every selected filter', () => {
        const query = new URLSearchParams(buildJobSearchQuery({
            keyword: '  Spring Boot  ',
            location: '  Hồ Chí Minh ',
            jobType: 'FULL_TIME',
            experienceLevel: 'SENIOR',
            minSalary: 30_000_000,
            maxSalary: 50_000_000,
        }))

        expect(Object.fromEntries(query)).toEqual({
            keyword: 'Spring Boot',
            location: 'Hồ Chí Minh',
            jobType: 'FULL_TIME',
            experienceLevel: 'SENIOR',
            minSalary: '30000000',
            maxSalary: '50000000',
            page: '1',
            size: '10',
        })
    })

    it('converts salary input from millions to VND', () => {
        const params = toJobSearchParams({
            ...EMPTY_JOB_FILTERS,
            minSalaryMillions: '20',
            maxSalaryMillions: '35.5',
        })

        expect(params.minSalary).toBe(20_000_000)
        expect(params.maxSalary).toBe(35_500_000)
    })

    it('rejects negative and reversed salary ranges', () => {
        expect(validateJobFilters({
            ...EMPTY_JOB_FILTERS,
            minSalaryMillions: '-1',
        })).toBe('Mức lương phải là số không âm.')

        expect(validateJobFilters({
            ...EMPTY_JOB_FILTERS,
            minSalaryMillions: '50',
            maxSalaryMillions: '30',
        })).toBe('Mức lương tối thiểu không được lớn hơn mức lương tối đa.')
    })
})
