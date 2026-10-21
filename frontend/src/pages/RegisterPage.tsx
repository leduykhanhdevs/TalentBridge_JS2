import { useMutation } from '@tanstack/react-query'
import {
    AlertCircle,
    BriefcaseBusiness,
    Building2,
    CheckCircle2,
    ChevronDown,
    LoaderCircle,
    LockKeyhole,
    Mail,
    Phone,
    UserPlus,
    UserRound,
} from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router'
import { HeroTalentIllustration } from '../components/illustrations'
import { AuthApiError, registerCandidate, LOGOUT_TIMEOUT_MS } from '../features/auth/authApi'
import { saveAuthTokens } from '../features/auth/tokenStorage'
import { REFRESH_TIMEOUT_MS } from '../features/auth/sessionRefresh'
import { withAuthSessionLock } from '../features/auth/authSessionLock'

type RegisterRole = 'ROLE_CANDIDATE' | 'ROLE_RECRUITER'
type RegisterFormValues = {
    fullName: string
    email: string
    phone: string
    password: string
    role: RegisterRole
    position: string
}
type RegisterField = keyof RegisterFormValues | 'terms'
type RegisterFormErrors = Partial<Record<RegisterField, string>>

const initialFormValues: RegisterFormValues = {
    fullName: '',
    email: '',
    phone: '',
    password: '',
    role: 'ROLE_CANDIDATE',
    position: '',
}

function validateForm(values: RegisterFormValues, acceptedTerms: boolean): RegisterFormErrors {
    const errors: RegisterFormErrors = {}
    if (!values.fullName.trim()) errors.fullName = 'Vui lòng nhập họ và tên.'
    if (!values.email.trim()) errors.email = 'Vui lòng nhập địa chỉ email.'
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(values.email.trim())) {
        errors.email = 'Địa chỉ email không đúng định dạng.'
    }
    if (values.phone.trim() && !/^\d{10,11}$/.test(values.phone.trim())) {
        errors.phone = 'Số điện thoại phải gồm 10–11 chữ số.'
    }
    if (!values.password) errors.password = 'Vui lòng nhập mật khẩu.'
    else if (values.password.length < 6) errors.password = 'Mật khẩu phải có ít nhất 6 ký tự.'
    if (!acceptedTerms) errors.terms = 'Bạn cần đồng ý với điều khoản sử dụng.'
    if (values.role === 'ROLE_RECRUITER' && values.position.trim().length > 100) {
        errors.position = 'Chức danh không được vượt quá 100 ký tự.'
    }
    return errors
}

function FieldError({ id, message }: { id: string; message?: string }) {
    if (!message) return null
    return <p className="mt-1.5 text-xs leading-5 text-rose-700" id={id} role="alert">{message}</p>
}

export function RegisterPage() {
    const navigate = useNavigate()
    const [formValues, setFormValues] = useState<RegisterFormValues>(initialFormValues)
    const [acceptedTerms, setAcceptedTerms] = useState(false)
    const [fieldErrors, setFieldErrors] = useState<RegisterFormErrors>({})
    const [serverError, setServerError] = useState('')
    const [successMessage, setSuccessMessage] = useState('')

    const registerMutation = useMutation({
        mutationFn: registerCandidate,
        onSuccess: async (authResponse) => {
            await withAuthSessionLock(() => saveAuthTokens(authResponse), LOGOUT_TIMEOUT_MS + REFRESH_TIMEOUT_MS)
            setFieldErrors({})
            setServerError('')
            setSuccessMessage(`Đăng ký thành công. Đang chuyển hướng... Chào mừng ${authResponse.user.fullName}!`)
            setFormValues(initialFormValues)
            setAcceptedTerms(false)
            window.setTimeout(() => {
                const roles = authResponse.user.roles || []
                if (roles.includes('ROLE_RECRUITER')) navigate('/recruiter/profile')
                else if (roles.includes('ROLE_ADMIN')) navigate('/admin/candidates')
                else navigate('/')
            }, 800)
        },
        onError: (error) => {
            setSuccessMessage('')
            if (!(error instanceof AuthApiError)) {
                setServerError('Đã xảy ra lỗi không xác định. Vui lòng thử lại.')
                return
            }
            if (error.status === 409) {
                setFieldErrors((current) => ({ ...current, email: 'Email này đã được sử dụng.' }))
                setServerError('')
                return
            }
            if (error.fieldErrors) {
                setFieldErrors((current) => ({
                    ...current,
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
        setFormValues((current) => ({ ...current, [field]: value }))
        setFieldErrors((current) => ({ ...current, [field]: undefined }))
        setServerError('')
        setSuccessMessage('')
    }

    function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()
        const errors = validateForm(formValues, acceptedTerms)
        setFieldErrors(errors)
        setServerError('')
        setSuccessMessage('')
        if (Object.keys(errors).length > 0) return

        registerMutation.mutate({
            fullName: formValues.fullName.trim(),
            email: formValues.email.trim().toLowerCase(),
            phone: formValues.phone.trim() || undefined,
            password: formValues.password,
            role: formValues.role,
            position: formValues.role === 'ROLE_RECRUITER' ? formValues.position.trim() || undefined : undefined,
        })
    }

    const inputBase = 'h-11 w-full rounded-lg border bg-white pl-10 pr-3 text-sm text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-blue-600 focus:ring-4 focus:ring-blue-100/70'
    const inputClass = (field: keyof RegisterFormValues) => `${inputBase} ${fieldErrors[field] ? 'border-rose-400 focus:border-rose-500 focus:ring-rose-100' : 'border-slate-300'}`

    return (
        <section className="relative isolate overflow-hidden bg-[#F5F7FB] px-4 py-8 sm:px-6 lg:min-h-[calc(100dvh-4rem)] lg:px-8 lg:py-10">
            <div aria-hidden="true" className="pointer-events-none absolute -left-40 top-28 -z-10 size-80 rounded-full bg-blue-100/40 blur-3xl" />
            <div aria-hidden="true" className="pointer-events-none absolute -right-40 bottom-8 -z-10 size-96 rounded-full bg-sky-100/40 blur-3xl" />

            <div className="mx-auto grid w-full max-w-[1120px] items-center gap-8 lg:min-h-[680px] lg:grid-cols-[minmax(0,1.05fr)_minmax(390px,0.8fr)] lg:gap-12">
                <div className="flex min-w-0 flex-col justify-center py-2 sm:py-5 lg:py-10">
                    <p className="text-[11px] font-bold uppercase tracking-[0.14em] text-blue-700">Mối kết nối đều có ý nghĩa</p>
                    <h1 className="mt-4 max-w-[540px] text-[32px] font-bold leading-[1.18] tracking-tight text-[#172033] sm:text-[40px]">
                        Năng lực của bạn.<br />Cơ hội xứng đáng.
                    </h1>
                    <p className="mt-4 max-w-[490px] text-sm leading-6 text-[#475467] sm:text-base sm:leading-7">
                        Tạo tài khoản để tìm cơ hội phù hợp, xây dựng hồ sơ nghề nghiệp và kết nối với doanh nghiệp.
                    </p>

                    <div className="mx-auto mt-5 w-full max-w-[480px] sm:mt-2 lg:mt-7">
                        <HeroTalentIllustration className="h-auto w-full" />
                    </div>

                    <div className="flex flex-wrap gap-x-6 gap-y-2 text-xs font-medium text-slate-600 sm:text-sm">
                        <span className="inline-flex items-center gap-2"><CheckCircle2 aria-hidden="true" className="text-emerald-700" size={16} />Bảo vệ thông tin tài khoản</span>
                        <span className="inline-flex items-center gap-2"><CheckCircle2 aria-hidden="true" className="text-emerald-700" size={16} />Phân quyền theo vai trò</span>
                    </div>
                </div>

                <div className="flex min-w-0 items-center justify-center lg:py-6">
                    <div className="bento-card w-full max-w-[440px] rounded-xl p-5 sm:p-7 lg:p-8">
                        <div>
                            <h2 className="text-[23px] font-bold leading-tight tracking-tight text-[#172033] sm:text-[25px]">Bắt đầu với TalentBridge</h2>
                            <p className="mt-2 text-xs leading-5 text-slate-600 sm:text-sm">Tạo tài khoản và chọn vai trò phù hợp.</p>
                        </div>

                        <form className="mt-5 space-y-4" noValidate onSubmit={handleSubmit}>
                            <div>
                                <label className="mb-1.5 block text-xs font-semibold text-slate-800" htmlFor="full-name">Họ và tên</label>
                                <div className="relative">
                                    <UserRound aria-hidden="true" className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={16} />
                                    <input aria-describedby={fieldErrors.fullName ? 'full-name-error' : undefined} aria-invalid={Boolean(fieldErrors.fullName)} autoComplete="name" className={inputClass('fullName')} id="full-name" name="fullName" onChange={(event) => updateField('fullName', event.target.value)} placeholder="Nguyễn Minh Anh" type="text" value={formValues.fullName} />
                                </div>
                                <FieldError id="full-name-error" message={fieldErrors.fullName} />
                            </div>

                            <div>
                                <label className="mb-1.5 block text-xs font-semibold text-slate-800" htmlFor="register-email">Email</label>
                                <div className="relative">
                                    <Mail aria-hidden="true" className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={16} />
                                    <input aria-describedby={fieldErrors.email ? 'register-email-error' : undefined} aria-invalid={Boolean(fieldErrors.email)} autoComplete="email" className={inputClass('email')} id="register-email" name="email" onChange={(event) => updateField('email', event.target.value)} placeholder="ban@example.com" type="email" value={formValues.email} />
                                </div>
                                <FieldError id="register-email-error" message={fieldErrors.email} />
                            </div>

                            <div>
                                <label className="mb-1.5 block text-xs font-semibold text-slate-800" htmlFor="register-password">Mật khẩu</label>
                                <div className="relative">
                                    <LockKeyhole aria-hidden="true" className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={16} />
                                    <input aria-describedby={fieldErrors.password ? 'register-password-error' : 'register-password-help'} aria-invalid={Boolean(fieldErrors.password)} autoComplete="new-password" className={inputClass('password')} id="register-password" name="password" onChange={(event) => updateField('password', event.target.value)} placeholder="Tối thiểu 6 ký tự" type="password" value={formValues.password} />
                                </div>
                                <FieldError id="register-password-error" message={fieldErrors.password} />
                                {!fieldErrors.password && <p className="mt-1 text-[11px] text-slate-500" id="register-password-help">Sử dụng ít nhất 6 ký tự.</p>}
                            </div>

                            <fieldset>
                                <legend className="sr-only">Chọn vai trò tài khoản</legend>
                                <div className="grid grid-cols-2 gap-2 rounded-lg bg-slate-100 p-1">
                                    <button aria-pressed={formValues.role === 'ROLE_CANDIDATE'} className={`flex min-h-9 items-center justify-center gap-1 whitespace-nowrap rounded-md px-1.5 text-[11px] font-semibold transition ${formValues.role === 'ROLE_CANDIDATE' ? 'bg-white text-blue-700 shadow-sm ring-1 ring-slate-200' : 'text-slate-600 hover:text-slate-900'}`} onClick={() => { updateField('role', 'ROLE_CANDIDATE'); updateField('position', '') }} type="button">
                                        <UserRound aria-hidden="true" size={14} />Ứng viên
                                    </button>
                                    <button aria-pressed={formValues.role === 'ROLE_RECRUITER'} className={`flex min-h-9 items-center justify-center gap-1 whitespace-nowrap rounded-md px-1.5 text-[11px] font-semibold transition ${formValues.role === 'ROLE_RECRUITER' ? 'bg-white text-blue-700 shadow-sm ring-1 ring-slate-200' : 'text-slate-600 hover:text-slate-900'}`} onClick={() => updateField('role', 'ROLE_RECRUITER')} type="button">
                                        <Building2 aria-hidden="true" size={14} />Nhà tuyển dụng
                                    </button>
                                </div>
                            </fieldset>

                            {formValues.role === 'ROLE_RECRUITER' && (
                                <div>
                                    <label className="mb-1.5 block text-xs font-semibold text-slate-800" htmlFor="register-position">Chức danh tuyển dụng <span className="font-normal text-slate-500">(không bắt buộc)</span></label>
                                    <div className="relative">
                                        <BriefcaseBusiness aria-hidden="true" className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={16} />
                                        <input aria-describedby={fieldErrors.position ? 'register-position-error' : undefined} aria-invalid={Boolean(fieldErrors.position)} className={inputClass('position')} id="register-position" name="position" onChange={(event) => updateField('position', event.target.value)} placeholder="Ví dụ: Chuyên viên tuyển dụng" type="text" value={formValues.position} />
                                    </div>
                                    <FieldError id="register-position-error" message={fieldErrors.position} />
                                </div>
                            )}

                            <details className="group rounded-lg border border-slate-200 px-3 py-2.5">
                                <summary className="flex cursor-pointer list-none items-center justify-between gap-3 text-xs font-semibold text-slate-600 [&::-webkit-details-marker]:hidden">
                                    Thông tin bổ sung <span className="font-normal text-slate-500">(không bắt buộc)</span>
                                    <ChevronDown aria-hidden="true" className="shrink-0 transition group-open:rotate-180" size={15} />
                                </summary>
                                <div className="mt-3">
                                    <label className="mb-1.5 block text-xs font-semibold text-slate-800" htmlFor="register-phone">Số điện thoại</label>
                                    <div className="relative">
                                        <Phone aria-hidden="true" className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={16} />
                                        <input aria-describedby={fieldErrors.phone ? 'register-phone-error' : undefined} aria-invalid={Boolean(fieldErrors.phone)} autoComplete="tel" className={inputClass('phone')} id="register-phone" inputMode="numeric" name="phone" onChange={(event) => updateField('phone', event.target.value)} placeholder="Số điện thoại liên hệ" type="tel" value={formValues.phone} />
                                    </div>
                                    <FieldError id="register-phone-error" message={fieldErrors.phone} />
                                </div>
                            </details>

                            <div>
                                <label className="flex items-start gap-2.5 text-[11px] leading-5 text-slate-600">
                                    <input aria-describedby={fieldErrors.terms ? 'terms-error' : undefined} aria-invalid={Boolean(fieldErrors.terms)} checked={acceptedTerms} className="mt-1 size-3.5 shrink-0 accent-blue-700" onChange={(event) => { setAcceptedTerms(event.target.checked); setFieldErrors((current) => ({ ...current, terms: undefined })) }} type="checkbox" />
                                    <span>Tôi đồng ý với điều khoản sử dụng và chính sách bảo mật của TalentBridge.</span>
                                </label>
                                <FieldError id="terms-error" message={fieldErrors.terms} />
                            </div>

                            {serverError && <div className="flex gap-2.5 rounded-lg border border-rose-200 bg-rose-50 px-3 py-2.5 text-xs leading-5 text-rose-800" role="alert"><AlertCircle aria-hidden="true" className="mt-0.5 shrink-0 text-rose-700" size={16} /><span>{serverError}</span></div>}
                            {successMessage && <div className="flex gap-2.5 rounded-lg border border-emerald-200 bg-emerald-50 px-3 py-2.5 text-xs leading-5 text-emerald-800" role="status"><CheckCircle2 aria-hidden="true" className="mt-0.5 shrink-0 text-emerald-700" size={16} /><span>{successMessage}</span></div>}

                            <button className="btn-bento-primary h-11 w-full text-sm" disabled={registerMutation.isPending} type="submit">
                                {registerMutation.isPending ? <LoaderCircle aria-hidden="true" className="animate-spin" size={17} /> : <UserPlus aria-hidden="true" size={17} />}
                                {registerMutation.isPending ? 'Đang tạo tài khoản...' : 'Tạo tài khoản'}
                            </button>
                        </form>

                        <p className="mt-4 text-center text-xs text-slate-600">Đã có tài khoản? <Link className="font-semibold text-blue-700 hover:text-blue-800" to="/login">Đăng nhập</Link></p>
                    </div>
                </div>
            </div>
        </section>
    )
}
