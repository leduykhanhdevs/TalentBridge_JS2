import { describe, expect, it } from 'vitest'
import { getAdminReportFormat } from '../adminReportIntent'

describe('getAdminReportFormat', () => {
    it('does not interpret the Vietnamese pronoun "anh" as an image export request', () => {
        expect(getAdminReportFormat('Anh hãy thống kê lỗi API', true)).toBeNull()
    })

    it('recognizes an explicitly requested image using Vietnamese diacritics', () => {
        expect(getAdminReportFormat('Xuất ảnh thống kê API', true)).toBe('png')
    })

    it('recognizes an explicit PNG export request', () => {
        expect(getAdminReportFormat('Tạo file PNG biểu đồ thống kê', true)).toBe('png')
    })

    it('keeps spreadsheet and document export requests available', () => {
        expect(getAdminReportFormat('Tạo bảng tính Excel', true)).toBe('xlsx')
        expect(getAdminReportFormat('Tạo báo cáo Word', true)).toBe('docx')
    })

    it('does not treat the word inside password as a Word export request', () => {
        expect(getAdminReportFormat('Hướng dẫn đặt lại password cho tài khoản TalentBridge', true)).toBeNull()
    })

    it('does not activate admin exports in the regular assistant', () => {
        expect(getAdminReportFormat('Tạo ảnh thống kê', false)).toBeNull()
    })
})
