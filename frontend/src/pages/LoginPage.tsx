import { useState, type FormEvent } from 'react'
import {
    AlertCircle,
    CheckCircle2,
    LoaderCircle,
    LockKeyhole,
    LogIn,
    Mail,
} from 'lucide-react'
import { Link, useNavigate } from 'react-router'
import { useMutation } from '@tanstack/react-query'
import { AuthApiError, loginUser } from '../features/auth/authApi'
import { saveAuthTokens } from '../features/auth/tokenStorage'
import { AuthIllustration } from '../components/illustrations'

type FormField = 'email' | 'password'

type FieldErrors = Partial<Record<FormField, string>>

export function LoginPage() {
    const navigate = useNavigate()
    const [formValues, setFormValues] = useState({
        email: '',
        password: '',
    })
    const [fieldErrors, setFieldErrors] = useState<FieldErrors>({})
    const [serverError, setServerError] = useState<string | null>(null)
    const [successMessage, setSuccessMessage] = useState<string | null>(null)

    const loginMutation = useMutation({
        mutationFn: loginUser,
        onSuccess: (data) => {
            saveAuthTokens(data)
            setServerError(null)
            setSuccessMessage(`Đăng nhập thành công! Chào mừng ${data.user.fullName}.`)
            setTimeout(() => {
                const roles = data.user.roles || []
                if (roles.includes('ROLE_RECRUITER')) {
                    navigate('/recruiter/profile')
                } else if (roles.includes('ROLE_ADMIN')) {
                    navigate('/admin/candidates')
                } else {
                    navigate('/')
                }
            }, 800)
        },
        onError: (error) => {
            if (error instanceof AuthApiError) {
                setServerError(error.message)
                if (error.fieldErrors) {
                    setFieldErrors(error.fieldErrors)
                }
            } else {
                setServerError('Đã có lỗi xảy ra. Vui lòng thử lại sau.')
            }
        },
    })

    function validate(): boolean {
        const errors: FieldErrors = {}
        const emailTrimmed = formValues.email.trim()

        if (!emailTrimmed) {
            errors.email = 'Vui lòng nhập địa chỉ email.'
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailTrimmed)) {
            errors.email = 'Địa chỉ email không đúng định dạng.'
        }

        if (!formValues.password) {
            errors.password = 'Vui lòng nhập mật khẩu.'
        }

        setFieldErrors(errors)
        return Object.keys(errors).length === 0
    }

    function handleSubmit(event: FormEvent) {
        event.preventDefault()
        setServerError(null)

        if (!validate()) {
            return
        }

        loginMutation.mutate({
            email: formValues.email.trim(),
            password: formValues.password,
        })
    }

    function updateField(field: FormField, value: string) {
        setFormValues((prev) => ({ ...prev, [field]: value }))
        setFieldErrors((prev) => ({ ...prev, [field]: undefined }))
        if (serverError) setServerError(null)
    }

    function getInputClassName(field: FormField) {
        const baseClass =
            'h-12 w-full rounded-xl border bg-white pl-11 pr-4 text-slate-900 outline-none transition placeholder:text-slate-400 text-sm'
        if (fieldErrors[field]) {
            return `${baseClass} border-rose-400 bg-rose-50/20 focus:border-rose-500 focus:ring-2 focus:ring-rose-100`
        }
        return `${baseClass} border-slate-200 focus:border-indigo-600 focus:ring-2 focus:ring-indigo-100`
    }

    function quickFill(email: string, pass: string) {
        setFormValues({ email, password: pass })
        setFieldErrors({})
        setServerError(null)
    }

    return (
        <section className="bg-bento-canvas min-h-[calc(100dvh-4rem)] flex items-center justify-center px-4 py-12 sm:px-6 lg:px-8">
            <div className="mx-auto grid w-full max-w-5xl overflow-hidden rounded-2xl bento-card border border-slate-200/80 bg-white shadow-xl lg:grid-cols-12 rounded-3xl">
                {/* Left Side: Modern Bento Showcase Banner */}
                <div className="border-b lg:border-b-0 lg:border-r border-indigo-700/30 bg-gradient-to-br from-indigo-900 via-indigo-800 to-slate-900 p-8 text-white sm:p-10 lg:col-span-5 lg:flex lg:flex-col lg:justify-between">
                    <div>
                        <div className="flex items-center gap-2">
                            <span className="size-2.5 rounded-full bg-rose-400" />
                            <span className="size-2.5 rounded-full bg-amber-400" />
                            <span className="size-2.5 rounded-full bg-emerald-400" />
                            <span className="ml-2 text-xs font-medium text-slate-300">Cổng đăng nhập</span>
                        </div>

                        <div className="mt-8">
                            <span className="bento-badge bg-white/15 text-white border-white/20">
                                TALENTBRIDGE ATS
                            </span>
                            <h1 className="mt-4 text-2xl sm:text-3xl font-black leading-tight text-slate-100">
                                Cổng đăng nhập hệ thống
                            </h1>
                            <p className="mt-3 text-sm text-slate-400 leading-relaxed">
                                Xác thực phiên làm việc bảo mật dành cho Ứng viên, Nhà tuyển dụng và Quản trị viên.
                            </p>
                        </div>

                        <div className="my-6">
                            <AuthIllustration className="w-full max-w-[280px] mx-auto drop-shadow-2xl" />
                        </div>
                    </div>

                    <div className="mt-8 pt-6 border-t border-indigo-700/40 space-y-3 text-xs text-indigo-200/90 font-medium">
                        <div className="flex items-center gap-2">
                            <span className="size-2 rounded-full bg-emerald-400" />
                            <span>Bảo mật JWT & Refresh Token</span>
                        </div>
                        <div className="flex items-center gap-2">
                            <span className="size-2 rounded-full bg-indigo-400" />
                            <span>Phân quyền 3 cấp độ RBAC</span>
                        </div>
                        <div className="flex items-center gap-2">
                            <span className="size-2 rounded-full bg-amber-400" />
                            <span>Chuẩn hóa CSDL 27 bảng 3NF</span>
                        </div>
                    </div>
                </div>

                {/* Right Side: Form Content */}
                <div className="p-6 sm:p-10 lg:col-span-7">
                    <div className="mx-auto w-full max-w-md">
                        <div className="flex items-center justify-between">
                            <span className="bento-badge bg-indigo-50 text-indigo-700 border-indigo-200/60">
                                Xác thực tài khoản
                            </span>
                            <span className="text-xs font-semibold text-slate-400">
                                Đăng nhập
                            </span>
                        </div>

                        <h2 className="mt-3 text-2xl font-black text-slate-950">
                            Đăng nhập tài khoản
                        </h2>
                        <p className="mt-1 text-sm text-slate-600">
                            Nhập email và mật khẩu để truy cập hệ thống.
                        </p>

                        {/* Quick-test credential helper */}
                        <div className="mt-5 rounded-2xl border border-slate-200/80 bg-slate-50/70 p-3.5">
                            <p className="text-xs font-bold text-slate-700">
                                Tài khoản mẫu thử nghiệm (Nhấn để điền):
                            </p>
                            <div className="mt-2 flex flex-wrap gap-2">
                                <button
                                    className="bento-badge bg-blue-50 text-blue-700 border-blue-200 cursor-pointer hover:bg-blue-100"
                                    onClick={() => quickFill('candidate@talentbridge.vn', 'Password123!')}
                                    type="button"
                                >
                                    Ứng viên
                                </button>
                                <button
                                    className="bento-badge bg-emerald-50 text-emerald-700 border-emerald-200 cursor-pointer hover:bg-emerald-100"
                                    onClick={() => quickFill('recruiter@fpt.com', 'Password123!')}
                                    type="button"
                                >
                                    Nhà tuyển dụng
                                </button>
                                <button
                                    className="bento-badge bg-rose-50 text-rose-700 border-rose-200 cursor-pointer hover:bg-rose-100"
                                    onClick={() => quickFill('admin@talentbridge.vn', 'AdminPassword123!')}
                                    type="button"
                                >
                                    Quản trị viên
                                </button>
                            </div>
                        </div>

                        <form className="mt-6 space-y-4" onSubmit={handleSubmit} noValidate>
                            <div>
                                <label
                                    className="mb-1.5 block text-xs font-bold text-slate-900 uppercase font-mono"
                                    htmlFor="email"
                                >
                                    Địa chỉ email
                                </label>

                                <div className="relative">
                                    <Mail
                                        aria-hidden="true"
                                        className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500"
                                        size={18}
                                    />
                                    <input
                                        autoComplete="email"
                                        className={getInputClassName('email')}
                                        id="email"
                                        name="email"
                                        placeholder="user@talentbridge.vn"
                                        type="email"
                                        value={formValues.email}
                                        onChange={(e) => updateField('email', e.target.value)}
                                    />
                                </div>
                                {fieldErrors.email && (
                                    <p className="mt-1 text-xs font-bold text-rose-600 font-mono">{fieldErrors.email}</p>
                                )}
                            </div>

                            <div>
                                <div className="mb-1.5 flex items-center justify-between gap-4">
                                    <label
                                        className="block text-xs font-bold text-slate-900 uppercase font-mono"
                                        htmlFor="password"
                                    >
                                        Mật khẩu
                                    </label>

                                    <Link
                                        to="/forgot-password"
                                        className="text-xs font-bold text-indigo-600 hover:text-indigo-800 underline"
                                    >
                                        Quên mật khẩu?
                                    </Link>
                                </div>

                                <div className="relative">
                                    <LockKeyhole
                                        aria-hidden="true"
                                        className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500"
                                        size={18}
                                    />
                                    <input
                                        autoComplete="current-password"
                                        className={getInputClassName('password')}
                                        id="password"
                                        name="password"
                                        placeholder="Nhập mật khẩu"
                                        type="password"
                                        value={formValues.password}
                                        onChange={(e) => updateField('password', e.target.value)}
                                    />
                                </div>
                                {fieldErrors.password && (
                                    <p className="mt-1 text-xs font-bold text-rose-600 font-mono">{fieldErrors.password}</p>
                                )}
                            </div>

                            {serverError && (
                                <div
                                    className="flex gap-2.5 rounded-xl border border-rose-200 bg-rose-50 p-3 text-xs font-semibold text-rose-800"
                                    role="alert"
                                >
                                    <AlertCircle aria-hidden="true" className="mt-0.5 shrink-0 text-rose-700" size={16} />
                                    <span>{serverError}</span>
                                </div>
                            )}

                            {successMessage && (
                                <div
                                    className="flex gap-2.5 rounded-xl border border-emerald-200 bg-emerald-50 p-3 text-xs font-semibold text-emerald-800"
                                    role="status"
                                >
                                    <CheckCircle2 aria-hidden="true" className="mt-0.5 shrink-0 text-emerald-700" size={16} />
                                    <span>{successMessage}</span>
                                </div>
                            )}

                            <button
                                className="btn-bento-primary w-full h-12 text-sm mt-2 font-semibold"
                                disabled={loginMutation.isPending}
                                type="submit"
                            >
                                {loginMutation.isPending ? (
                                    <LoaderCircle aria-hidden="true" className="animate-spin" size={18} />
                                ) : (
                                    <LogIn aria-hidden="true" size={18} />
                                )}
                                <span>{loginMutation.isPending ? 'Đang đăng nhập...' : 'Đăng nhập hệ thống'}</span>
                            </button>
                        </form>

                        <p className="mt-6 text-center text-xs font-semibold text-slate-700">
                            Chưa có tài khoản?{' '}
                            <Link
                                className="font-bold text-indigo-600 hover:text-indigo-800 underline"
                                to="/register"
                            >
                                Đăng ký ngay
                            </Link>
                        </p>
                    </div>
                </div>
            </div>
        </section>
    )
}