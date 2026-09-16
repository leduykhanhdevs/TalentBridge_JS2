import { useMutation } from '@tanstack/react-query'
import {
    AlertCircle,
    CheckCircle2,
    LoaderCircle,
    LockKeyhole,
    Mail,
    Phone,
    UserPlus,
    UserRound,
} from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { Link } from 'react-router'
import { AuthApiError, registerCandidate } from '../features/auth/authApi'
import { saveAuthTokens } from '../features/auth/tokenStorage'

const inputClassName =
    'h-12 w-full rounded-xl border border-slate-300 bg-white pl-11 pr-4 text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-indigo-500 focus:ring-4 focus:ring-indigo-100'

const invalidInputClassName =
    'border-red-400 focus:border-red-500 focus:ring-red-100'

type RegisterFormValues = {
    fullName: string
    email: string
    phone: string
    password: string
    confirmPassword: string
}

type RegisterField = keyof RegisterFormValues | 'terms'
type RegisterFormErrors = Partial<Record<RegisterField, string>>

const initialFormValues: RegisterFormValues = {
    fullName: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: '',
}

function validateForm(
    values: RegisterFormValues,
    acceptedTerms: boolean,
): RegisterFormErrors {
    const errors: RegisterFormErrors = {}
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    const phonePattern = /^\d{10,11}$/

    if (!values.fullName.trim()) {
        errors.fullName = 'Vui lòng nhập họ và tên.'
    }

    if (!values.email.trim()) {
        errors.email = 'Vui lòng nhập địa chỉ email.'
    } else if (!emailPattern.test(values.email.trim())) {
        errors.email = 'Địa chỉ email không đúng định dạng.'
    }

    if (values.phone.trim() && !phonePattern.test(values.phone.trim())) {
        errors.phone = 'Số điện thoại phải gồm 10–11 chữ số.'
    }

    if (!values.password) {
        errors.password = 'Vui lòng nhập mật khẩu.'
    } else if (values.password.length < 6) {
        errors.password = 'Mật khẩu phải có ít nhất 6 ký tự.'
    }

    if (!values.confirmPassword) {
        errors.confirmPassword = 'Vui lòng nhập lại mật khẩu.'
    } else if (values.confirmPassword !== values.password) {
        errors.confirmPassword = 'Mật khẩu xác nhận không khớp.'
    }

    if (!acceptedTerms) {
        errors.terms = 'Bạn cần đồng ý với điều khoản sử dụng.'
    }

    return errors
}

function FieldError({ id, message }: { id: string; message?: string }) {
    if (!message) {
        return null
    }

    return (
        <p className="mt-1.5 text-sm text-red-600" id={id} role="alert">
            {message}
        </p>
    )
}

export function RegisterPage() {
    const [formValues, setFormValues] =
        useState<RegisterFormValues>(initialFormValues)
    const [acceptedTerms, setAcceptedTerms] = useState(false)
    const [fieldErrors, setFieldErrors] = useState<RegisterFormErrors>({})
    const [serverError, setServerError] = useState('')
    const [successMessage, setSuccessMessage] = useState('')

    const registerMutation = useMutation({
        mutationFn: registerCandidate,
        onSuccess: (authResponse) => {
            saveAuthTokens(authResponse)
            setFieldErrors({})
            setServerError('')
            setSuccessMessage(
                `Đăng ký thành công. Chào mừng ${authResponse.user.fullName}!`,
            )
            setFormValues(initialFormValues)
            setAcceptedTerms(false)
        },
        onError: (error) => {
            setSuccessMessage('')

            if (!(error instanceof AuthApiError)) {
                setServerError('Đã xảy ra lỗi không xác định. Vui lòng thử lại.')
                return
            }

            if (error.status === 409) {
                setFieldErrors((currentErrors) => ({
                    ...currentErrors,
                    email: 'Email này đã được sử dụng.',
                }))
                setServerError('')
                return
            }

            if (error.fieldErrors) {
                setFieldErrors((currentErrors) => ({
                    ...currentErrors,
                    fullName: error.fieldErrors?.fullName,
                    email: error.fieldErrors?.email,
                    phone: error.fieldErrors?.phone,
                    password: error.fieldErrors?.password,
                }))
                setServerError('Vui lòng kiểm tra lại các trường thông tin.')
                return
            }

            setServerError(error.message)
        },
    })

    function updateField(field: keyof RegisterFormValues, value: string) {
        setFormValues((currentValues) => ({
            ...currentValues,
            [field]: value,
        }))
        setFieldErrors((currentErrors) => ({
            ...currentErrors,
            [field]: undefined,
        }))
        setServerError('')
        setSuccessMessage('')
    }

    function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()

        const validationErrors = validateForm(formValues, acceptedTerms)
        setFieldErrors(validationErrors)
        setServerError('')
        setSuccessMessage('')

        if (Object.keys(validationErrors).length > 0) {
            return
        }

        registerMutation.mutate({
            fullName: formValues.fullName.trim(),
            email: formValues.email.trim().toLowerCase(),
            phone: formValues.phone.trim() || undefined,
            password: formValues.password,
            role: 'ROLE_CANDIDATE',
        })
    }

    function getInputClassName(field: keyof RegisterFormValues) {
        return `${inputClassName} ${fieldErrors[field] ? invalidInputClassName : ''}`
    }

    return (
        <section className="relative overflow-hidden bg-slate-50 px-4 py-12 sm:px-6 lg:px-8">
            <div
                aria-hidden="true"
                className="absolute -left-32 bottom-10 size-80 rounded-full bg-violet-100/70 blur-3xl"
            />
            <div
                aria-hidden="true"
                className="absolute -right-32 top-20 size-80 rounded-full bg-indigo-100/70 blur-3xl"
            />

            <div className="relative mx-auto grid w-full max-w-6xl overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-xl shadow-slate-200/60 lg:grid-cols-[0.95fr_1.05fr]">
                <div className="relative hidden overflow-hidden bg-indigo-600 p-12 text-white lg:flex lg:flex-col lg:justify-between">
                    <div
                        aria-hidden="true"
                        className="absolute -right-20 -top-20 size-64 rounded-full bg-white/15 blur-3xl"
                    />
                    <div
                        aria-hidden="true"
                        className="absolute -bottom-24 -left-16 size-72 rounded-full bg-violet-950/30 blur-3xl"
                    />

                    <div className="relative">
                        <div className="grid size-12 place-items-center rounded-2xl bg-white/15 ring-1 ring-white/20">
                            <UserPlus aria-hidden="true" size={25} />
                        </div>

                        <p className="mt-10 text-sm font-semibold uppercase tracking-wider text-indigo-100">
                            Tài khoản ứng viên
                        </p>

                        <h1 className="mt-4 text-4xl font-bold leading-tight">
                            Khởi đầu hành trình mới cùng TalentBridge
                        </h1>

                        <p className="mt-5 max-w-md leading-7 text-indigo-100">
                            Tạo hồ sơ chuyên nghiệp, khám phá việc làm phù hợp và kết nối với
                            những doanh nghiệp uy tín.
                        </p>
                    </div>

                    <div className="relative mt-12 space-y-4 text-sm text-indigo-50">
                        <p className="flex items-center gap-3">
                            <CheckCircle2 aria-hidden="true" size={19} />
                            Tạo và quản lý hồ sơ cá nhân
                        </p>

                        <p className="flex items-center gap-3">
                            <CheckCircle2 aria-hidden="true" size={19} />
                            Nhận gợi ý công việc phù hợp
                        </p>

                        <p className="flex items-center gap-3">
                            <CheckCircle2 aria-hidden="true" size={19} />
                            Theo dõi trạng thái ứng tuyển
                        </p>
                    </div>
                </div>

                <div className="p-6 sm:p-10 lg:p-12">
                    <div className="mx-auto w-full max-w-lg">
                        <p className="text-sm font-semibold text-indigo-600">
                            Candidate account
                        </p>

                        <h2 className="mt-2 text-3xl font-bold tracking-tight text-slate-950">
                            Đăng ký ứng viên
                        </h2>

                        <p className="mt-3 leading-7 text-slate-600">
                            Điền thông tin bên dưới để tạo tài khoản TalentBridge.
                        </p>

                        <form className="mt-8 space-y-5" noValidate onSubmit={handleSubmit}>
                            <div>
                                <label
                                    className="mb-2 block text-sm font-medium text-slate-700"
                                    htmlFor="full-name"
                                >
                                    Họ và tên
                                </label>

                                <div className="relative">
                                    <UserRound
                                        aria-hidden="true"
                                        className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400"
                                        size={19}
                                    />
                                    <input
                                        aria-describedby={fieldErrors.fullName ? 'full-name-error' : undefined}
                                        aria-invalid={Boolean(fieldErrors.fullName)}
                                        autoComplete="name"
                                        className={getInputClassName('fullName')}
                                        id="full-name"
                                        name="fullName"
                                        onChange={(event) => updateField('fullName', event.target.value)}
                                        placeholder="Nguyễn Văn A"
                                        type="text"
                                        value={formValues.fullName}
                                    />
                                </div>
                                <FieldError id="full-name-error" message={fieldErrors.fullName} />
                            </div>

                            <div>
                                <label
                                    className="mb-2 block text-sm font-medium text-slate-700"
                                    htmlFor="register-email"
                                >
                                    Địa chỉ email
                                </label>

                                <div className="relative">
                                    <Mail
                                        aria-hidden="true"
                                        className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400"
                                        size={19}
                                    />
                                    <input
                                        aria-describedby={fieldErrors.email ? 'register-email-error' : undefined}
                                        aria-invalid={Boolean(fieldErrors.email)}
                                        autoComplete="email"
                                        className={getInputClassName('email')}
                                        id="register-email"
                                        name="email"
                                        onChange={(event) => updateField('email', event.target.value)}
                                        placeholder="ban@example.com"
                                        type="email"
                                        value={formValues.email}
                                    />
                                </div>
                                <FieldError id="register-email-error" message={fieldErrors.email} />
                            </div>

                            <div>
                                <label
                                    className="mb-2 block text-sm font-medium text-slate-700"
                                    htmlFor="register-phone"
                                >
                                    Số điện thoại <span className="text-slate-400">(không bắt buộc)</span>
                                </label>

                                <div className="relative">
                                    <Phone
                                        aria-hidden="true"
                                        className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400"
                                        size={19}
                                    />
                                    <input
                                        aria-describedby={fieldErrors.phone ? 'register-phone-error' : undefined}
                                        aria-invalid={Boolean(fieldErrors.phone)}
                                        autoComplete="tel"
                                        className={getInputClassName('phone')}
                                        id="register-phone"
                                        inputMode="numeric"
                                        name="phone"
                                        onChange={(event) => updateField('phone', event.target.value)}
                                        placeholder="0912345678"
                                        type="tel"
                                        value={formValues.phone}
                                    />
                                </div>
                                <FieldError id="register-phone-error" message={fieldErrors.phone} />
                            </div>

                            <div className="grid gap-5 sm:grid-cols-2">
                                <div>
                                    <label
                                        className="mb-2 block text-sm font-medium text-slate-700"
                                        htmlFor="register-password"
                                    >
                                        Mật khẩu
                                    </label>

                                    <div className="relative">
                                        <LockKeyhole
                                            aria-hidden="true"
                                            className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400"
                                            size={19}
                                        />
                                        <input
                                            aria-describedby={fieldErrors.password ? 'register-password-error' : undefined}
                                            aria-invalid={Boolean(fieldErrors.password)}
                                            autoComplete="new-password"
                                            className={getInputClassName('password')}
                                            id="register-password"
                                            name="password"
                                            onChange={(event) => updateField('password', event.target.value)}
                                            placeholder="Tối thiểu 6 ký tự"
                                            type="password"
                                            value={formValues.password}
                                        />
                                    </div>
                                    <FieldError id="register-password-error" message={fieldErrors.password} />
                                </div>

                                <div>
                                    <label
                                        className="mb-2 block text-sm font-medium text-slate-700"
                                        htmlFor="confirm-password"
                                    >
                                        Xác nhận mật khẩu
                                    </label>

                                    <div className="relative">
                                        <LockKeyhole
                                            aria-hidden="true"
                                            className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400"
                                            size={19}
                                        />
                                        <input
                                            aria-describedby={fieldErrors.confirmPassword ? 'confirm-password-error' : undefined}
                                            aria-invalid={Boolean(fieldErrors.confirmPassword)}
                                            autoComplete="new-password"
                                            className={getInputClassName('confirmPassword')}
                                            id="confirm-password"
                                            name="confirmPassword"
                                            onChange={(event) => updateField('confirmPassword', event.target.value)}
                                            placeholder="Nhập lại mật khẩu"
                                            type="password"
                                            value={formValues.confirmPassword}
                                        />
                                    </div>
                                    <FieldError id="confirm-password-error" message={fieldErrors.confirmPassword} />
                                </div>
                            </div>

                            <div>
                                <label className="flex items-start gap-3 text-sm leading-6 text-slate-600">
                                    <input
                                        aria-describedby={fieldErrors.terms ? 'terms-error' : undefined}
                                        aria-invalid={Boolean(fieldErrors.terms)}
                                        checked={acceptedTerms}
                                        className="mt-1 size-4 shrink-0 rounded border-slate-300 accent-indigo-600"
                                        onChange={(event) => {
                                            setAcceptedTerms(event.target.checked)
                                            setFieldErrors((currentErrors) => ({
                                                ...currentErrors,
                                                terms: undefined,
                                            }))
                                        }}
                                        type="checkbox"
                                    />
                                    <span>
                                        Tôi đồng ý với điều khoản sử dụng và chính sách bảo mật của
                                        TalentBridge.
                                    </span>
                                </label>
                                <FieldError id="terms-error" message={fieldErrors.terms} />
                            </div>

                            {serverError && (
                                <div
                                    className="flex gap-3 rounded-xl border border-red-200 bg-red-50 p-3 text-sm leading-6 text-red-700"
                                    role="alert"
                                >
                                    <AlertCircle aria-hidden="true" className="mt-0.5 shrink-0" size={18} />
                                    <span>{serverError}</span>
                                </div>
                            )}

                            {successMessage && (
                                <div
                                    className="flex gap-3 rounded-xl border border-emerald-200 bg-emerald-50 p-3 text-sm leading-6 text-emerald-700"
                                    role="status"
                                >
                                    <CheckCircle2 aria-hidden="true" className="mt-0.5 shrink-0" size={18} />
                                    <span>{successMessage}</span>
                                </div>
                            )}

                            <button
                                className="inline-flex h-12 w-full items-center justify-center gap-2 rounded-xl bg-indigo-600 px-5 text-sm font-semibold text-white transition hover:bg-indigo-700 disabled:cursor-wait disabled:opacity-70"
                                disabled={registerMutation.isPending}
                                type="submit"
                            >
                                {registerMutation.isPending ? (
                                    <LoaderCircle aria-hidden="true" className="animate-spin" size={18} />
                                ) : (
                                    <UserPlus aria-hidden="true" size={18} />
                                )}
                                {registerMutation.isPending ? 'Đang tạo tài khoản...' : 'Tạo tài khoản'}
                            </button>
                        </form>

                        <p className="mt-7 text-center text-sm text-slate-600">
                            Đã có tài khoản?{' '}
                            <Link
                                className="font-semibold text-indigo-600 hover:text-indigo-700"
                                to="/login"
                            >
                                Đăng nhập
                            </Link>
                        </p>
                    </div>
                </div>
            </div>
        </section>
    )
}
