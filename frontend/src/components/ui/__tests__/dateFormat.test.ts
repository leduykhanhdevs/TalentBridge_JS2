import { describe, expect, it } from 'vitest'
import { formatDateDraft, formatIsoDate, parseVietnameseDate } from '../dateFormat'

describe('Vietnamese date formatting', () => {
    it('formats ISO date strings without shifting the calendar day', () => {
        expect(formatIsoDate('1999-10-23')).toBe('23/10/1999')
    })

    it('parses dd/mm/yyyy and compact pasted dates into ISO values', () => {
        expect(parseVietnameseDate('23/10/1999')).toBe('1999-10-23')
        expect(parseVietnameseDate('23101999')).toBe('1999-10-23')
    })

    it('rejects impossible and incomplete dates', () => {
        expect(parseVietnameseDate('31/02/2020')).toBeNull()
        expect(parseVietnameseDate('01/01/20')).toBeNull()
    })

    it('adds separators while the user types', () => {
        expect(formatDateDraft('23101999')).toBe('23/10/1999')
    })
})
