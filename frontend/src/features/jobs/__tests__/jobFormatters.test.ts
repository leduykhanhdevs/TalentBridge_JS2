import { describe, expect, it } from 'vitest'
import {
    formatDate,
    formatExperienceLevel,
    formatJobType,
    formatSalaryRange,
} from '../jobFormatters'

describe('jobFormatters', () => {
    it('formats known job type and experience values', () => {
        expect(formatJobType('FULL_TIME')).toBe('Toàn thời gian')
        expect(formatExperienceLevel('SENIOR')).toBe('Senior')
    })

    it('formats salary ranges in millions of VND', () => {
        expect(formatSalaryRange(20_000_000, 40_000_000)).toBe('20 - 40 triệu')
        expect(formatSalaryRange(20_000_000, null)).toBe('Từ 20 triệu')
        expect(formatSalaryRange(null, 40_000_000)).toBe('Đến 40 triệu')
        expect(formatSalaryRange(null, null)).toBe('Thỏa thuận')
    })

    it('formats ISO dates and handles missing dates', () => {
        expect(formatDate('2026-10-24')).toBe('24/10/2026')
        expect(formatDate(null)).toBe('Chưa cập nhật')
    })
})
