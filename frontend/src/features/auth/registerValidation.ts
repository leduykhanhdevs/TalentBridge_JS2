export type RegisterRole = 'ROLE_CANDIDATE' | 'ROLE_RECRUITER'

export type RegisterFormValues = {
    fullName: string
    email: string
    phone: string
    password: string
    role: RegisterRole
    position: string
}

type RegisterField = keyof RegisterFormValues | 'terms'
export type RegisterFormErrors = Partial<Record<RegisterField, string>>

const phonePattern = /^(0[0-9]{9,10}|\+84[0-9]{9,10})$/

export function validateRegisterForm(
    values: RegisterFormValues,
    acceptedTerms: boolean,
): RegisterFormErrors {
    const errors: RegisterFormErrors = {}

    if (!values.fullName.trim()) errors.fullName = 'Vui lòng nhập họ và tên.'
    if (!values.email.trim()) errors.email = 'Vui lòng nhập địa chỉ email.'
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(values.email.trim())) {
        errors.email = 'Địa chỉ email không đúng định dạng.'
    }

    if (!values.phone.trim()) {
        errors.phone = 'Vui lòng nhập số điện thoại.'
    } else if (!phonePattern.test(values.phone.trim())) {
        errors.phone = 'Số điện thoại cần có dạng 0912345678 hoặc +84912345678.'
    }

    if (!values.password) errors.password = 'Vui lòng nhập mật khẩu.'
    else if (values.password.length < 6) errors.password = 'Mật khẩu phải có ít nhất 6 ký tự.'
    if (!acceptedTerms) errors.terms = 'Bạn cần đồng ý với điều khoản sử dụng.'
    if (values.role === 'ROLE_RECRUITER' && values.position.trim().length > 100) {
        errors.position = 'Chức danh không được vượt quá 100 ký tự.'
    }

    return errors
}
