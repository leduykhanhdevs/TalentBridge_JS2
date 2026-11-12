import { describe, expect, it } from 'vitest'
import { validateRegisterForm, type RegisterFormValues } from '../registerValidation'

const validValues: RegisterFormValues = {
    fullName: 'Lê Duy Khánh',
    email: 'khanh@example.com',
    phone: '0912345678',
    password: 'password123',
    confirmPassword: 'password123',
    role: 'ROLE_CANDIDATE',
    position: '',
}

describe('validateRegisterForm', () => {
    it('requires a phone number', () => {
        expect(validateRegisterForm({ ...validValues, phone: '  ' }, true).phone).toBe(
            'Vui lòng nhập số điện thoại.',
        )
    })

    it('requires the same Vietnamese phone formats accepted by the backend', () => {
        expect(validateRegisterForm({ ...validValues, phone: '09123abc' }, true).phone).toBe(
            'Số điện thoại cần có dạng 0912345678 hoặc +84912345678.',
        )
        expect(validateRegisterForm({ ...validValues, phone: '+84912345678' }, true).phone).toBeUndefined()
    })

    it('accepts a valid local phone number', () => {
        expect(validateRegisterForm(validValues, true).phone).toBeUndefined()
    })

    it('requires a confirmation password', () => {
        expect(validateRegisterForm({ ...validValues, confirmPassword: '' }, true).confirmPassword).toBe(
            'Vui lòng nhập lại mật khẩu.',
        )
    })

    it('rejects a confirmation password that does not match', () => {
        expect(validateRegisterForm({ ...validValues, confirmPassword: 'different123' }, true).confirmPassword).toBe(
            'Mật khẩu xác nhận không khớp.',
        )
    })

    it('accepts matching password confirmation', () => {
        expect(validateRegisterForm(validValues, true).confirmPassword).toBeUndefined()
    })
})
