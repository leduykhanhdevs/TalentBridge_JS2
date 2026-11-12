import { describe, expect, it } from 'vitest'
import { isPendingAdminQuestion } from '../adminQuestionIntent'

describe('isPendingAdminQuestion', () => {
    it('routes company approval count questions to queue statistics', () => {
        expect(isPendingAdminQuestion('co bao nhieu cong ty cho phe duyet')).toBe(true)
    })

    it('recognizes other pending moderation questions', () => {
        expect(isPendingAdminQuestion('tin tuyen dung cho duyet')).toBe(true)
    })

    it('does not classify general approval how-to questions as queue counts', () => {
        expect(isPendingAdminQuestion('huong dan phe duyet doanh nghiep')).toBe(false)
    })
})
