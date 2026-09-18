import { describe, it, expect } from 'vitest'

describe('Authentication Form Validation Rules', () => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    const phoneRegex = /^0\d{9}$/

    it('should correctly validate valid email addresses', () => {
        expect(emailRegex.test('candidate@gmail.com')).toBe(true)
        expect(emailRegex.test('user.test@company.vn')).toBe(true)
        expect(emailRegex.test('hello+test@domain.org')).toBe(true)
    })

    it('should reject malformed email formats', () => {
        expect(emailRegex.test('')).toBe(false)
        expect(emailRegex.test('not-an-email')).toBe(false)
        expect(emailRegex.test('missing-domain@')).toBe(false)
        expect(emailRegex.test('@missing-user.com')).toBe(false)
        expect(emailRegex.test('spaces in@email.com')).toBe(false)
    })

    it('should validate Vietnamese standard 10-digit mobile phones', () => {
        expect(phoneRegex.test('0901234567')).toBe(true)
        expect(phoneRegex.test('0388889999')).toBe(true)
        expect(phoneRegex.test('091234567')).toBe(false) // 9 digits
        expect(phoneRegex.test('09123456789')).toBe(false) // 11 digits
        expect(phoneRegex.test('1901234567')).toBe(false) // doesn't start with 0
        expect(phoneRegex.test('090abc1234')).toBe(false) // contains letters
    })

    it('should validate password minimum length constraints', () => {
        const isValidPassword = (p: string) => p.length >= 6

        expect(isValidPassword('123456')).toBe(true)
        expect(isValidPassword('strongP@ssw0rd')).toBe(true)
        expect(isValidPassword('12345')).toBe(false)
        expect(isValidPassword('')).toBe(false)
    })

    it('should verify that password confirmation matches original password', () => {
        const passwordsMatch = (p: string, cp: string) => p.length > 0 && p === cp

        expect(passwordsMatch('secret123', 'secret123')).toBe(true)
        expect(passwordsMatch('secret123', 'different123')).toBe(false)
        expect(passwordsMatch('', '')).toBe(false)
    })

    it('should validate change password rules', () => {
        const validateChangePassword = (current: string, next: string, confirm: string) => {
            if (!current.trim()) return 'Vui lòng nhập mật khẩu hiện tại.'
            if (next.length < 6) return 'Mật khẩu mới phải có ít nhất 6 ký tự.'
            if (next === current) return 'Mật khẩu mới không được trùng với mật khẩu hiện tại.'
            if (next !== confirm) return 'Mật khẩu xác nhận không khớp.'
            return null
        }

        expect(validateChangePassword('OldPass123!', 'NewPass456!', 'NewPass456!')).toBeNull()
        expect(validateChangePassword('', 'NewPass456!', 'NewPass456!')).toBe('Vui lòng nhập mật khẩu hiện tại.')
        expect(validateChangePassword('OldPass123!', '12345', '12345')).toBe('Mật khẩu mới phải có ít nhất 6 ký tự.')
        expect(validateChangePassword('OldPass123!', 'OldPass123!', 'OldPass123!')).toBe('Mật khẩu mới không được trùng với mật khẩu hiện tại.')
        expect(validateChangePassword('OldPass123!', 'NewPass456!', 'WrongConfirm!')).toBe('Mật khẩu xác nhận không khớp.')
    })
})

