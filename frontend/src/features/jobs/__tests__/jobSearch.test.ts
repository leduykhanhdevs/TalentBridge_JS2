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

import { formatJobLocation } from '../jobSearchFormatters'

describe('TB-002 location display', () => {
    it('deduplicates city while preserving the simulation label and street address', () => {
        expect(formatJobLocation('Địa điểm mô phỏng · Hà Nội', 'Hà Nội')).toBe('Địa điểm mô phỏng · Hà Nội')
        expect(formatJobLocation('  Hà Nội ', 'hà nội')).toBe('Hà Nội')
        expect(formatJobLocation('123 Đường A', 'Hà Nội')).toBe('123 Đường A · Hà Nội')
        expect(formatJobLocation(null, null)).toBe('Chưa cập nhật')
    })
})

describe('TB-003 experience filters', () => {
    it.each(['NO_EXPERIENCE', 'UNDER_1_YEAR'] as const)('sends %s without mapping it to a different level', (experienceLevel) => {
        const params = toJobSearchParams({ ...EMPTY_JOB_FILTERS, experienceLevel })
        expect(new URLSearchParams(buildJobSearchQuery(params)).get('experienceLevel')).toBe(experienceLevel)
    })
})

import { readJobSearchParams, toJobFilterFormValues } from '../jobSearchFilters'

describe('TB-006 search URL state', () => {
    it('restores page, filters, salary, size and sorting', () => {
        const original = { page: 3, size: 20, sort: 'SALARY_DESC', keyword: 'Java', location: 'Hà Nội',
            jobType: 'FULL_TIME' as const, experienceLevel: 'UNDER_1_YEAR' as const,
            minSalary: 15_000_000, maxSalary: 30_000_000 }
        const restored = readJobSearchParams(new URLSearchParams(buildJobSearchQuery(original)))
        expect(restored).toEqual(original)
        expect(toJobFilterFormValues(restored).minSalaryMillions).toBe('15')
    })
    it('rejects invalid URL values', () => {
        const restored = readJobSearchParams(new URLSearchParams('page=-1&size=999&minSalary=NaN&maxSalary=-1&sort=invalid&jobType=invalid'))
        expect(restored).toMatchObject({page: 1, size: 10, sort: 'NEWEST'})
        expect(restored.minSalary).toBeUndefined()
        expect(restored.maxSalary).toBeUndefined()
        expect(restored.jobType).toBeUndefined()
    })
})
