import { useState, type FormEvent } from 'react'
import {
    AlertCircle,
    CheckCircle2,
    LoaderCircle,
    LockKeyhole,
    LogIn,
    Mail,
    ShieldCheck,
} from 'lucide-react'
import { Link, useNavigate } from 'react-router'
import { useMutation } from '@tanstack/react-query'
import { AuthApiError, loginUser } from '../features/auth/authApi'
import { saveAuthTokens } from '../features/auth/tokenStorage'

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
                navigate('/')
            }, 1000)
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
            'h-12 w-full rounded-xl border bg-white pl-11 pr-4 text-slate-900 outline-none transition placeholder:text-slate-400'
        if (fieldErrors[field]) {
            return `${baseClass} border-red-300 focus:border-red-500 focus:ring-4 focus:ring-red-100`
        }
        return `${baseClass} border-slate-300 focus:border-indigo-500 focus:ring-4 focus:ring-indigo-100`
    }

    return (
        <section className="relative overflow-hidden bg-slate-50 px-4 py-12 sm:px-6 lg:px-8">
            <div
                aria-hidden="true"
                className="absolute -left-32 top-20 size-80 rounded-full bg-indigo-100/70 blur-3xl"
            />
            <div
                aria-hidden="true"
                className="absolute -right-32 bottom-10 size-80 rounded-full bg-violet-100/70 blur-3xl"
            />

            <div className="relative mx-auto grid w-full max-w-6xl overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-xl shadow-slate-200/60 lg:grid-cols-[1.05fr_0.95fr]">
                <div className="relative hidden overflow-hidden bg-slate-950 p-12 text-white lg:flex lg:flex-col lg:justify-between">
                    <div
                        aria-hidden="true"
                        className="absolute -right-20 -top-20 size-64 rounded-full bg-indigo-500/30 blur-3xl"
                    />
                    <div
                        aria-hidden="true"
                        className="absolute -bottom-24 -left-16 size-72 rounded-full bg-violet-500/20 blur-3xl"
                    />

                    <div className="relative">
                        <div className="grid size-12 place-items-center rounded-2xl bg-indigo-500">
                            <ShieldCheck aria-hidden="true" size={25} />
                        </div>

                        <p className="mt-10 text-sm font-semibold uppercase tracking-wider text-indigo-300">
                            TalentBridge ATS
                        </p>

                        <h1 className="mt-4 text-4xl font-bold leading-tight">
                            Tiếp tục hành trình nghề nghiệp của bạn
                        </h1>

                        <p className="mt-5 max-w-md leading-7 text-slate-300">
                            Đăng nhập để quản lý hồ sơ, theo dõi trạng thái ứng tuyển và khám
                            phá những cơ hội phù hợp.
                        </p>
                    </div>

                    <div className="relative mt-12 space-y-4 text-sm text-slate-300">
                        <p className="flex items-center gap-3">
                            <CheckCircle2
                                aria-hidden="true"
                                className="text-emerald-400"
                                size={19}
                            />
                            Quản lý hồ sơ ứng viên tập trung
                        </p>

                        <p className="flex items-center gap-3">
                            <CheckCircle2
                                aria-hidden="true"
                                className="text-emerald-400"
                                size={19}
                            />
                            Theo dõi tiến trình ứng tuyển
                        </p>

                        <p className="flex items-center gap-3">
                            <CheckCircle2
                                aria-hidden="true"
                                className="text-emerald-400"
                                size={19}
                            />
                            Bảo vệ thông tin tài khoản và phiên đăng nhập
                        </p>
                    </div>
                </div>

                <div className="p-6 sm:p-10 lg:p-12">
                    <div className="mx-auto w-full max-w-md">
                        <p className="text-sm font-semibold text-indigo-600">
                            Chào mừng trở lại
                        </p>

                        <h2 className="mt-2 text-3xl font-bold tracking-tight text-slate-950">
                            Đăng nhập tài khoản
                        </h2>

                        <p className="mt-3 leading-7 text-slate-600">
                            Nhập thông tin tài khoản TalentBridge của bạn.
                        </p>

                        <form className="mt-8 space-y-5" onSubmit={handleSubmit} noValidate>
                            <div>
                                <label
                                    className="mb-2 block text-sm font-medium text-slate-700"
                                    htmlFor="email"
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
                                        autoComplete="email"
                                        className={getInputClassName('email')}
                                        id="email"
                                        name="email"
                                        placeholder="ban@example.com"
                                        type="email"
                                        value={formValues.email}
                                        onChange={(e) => updateField('email', e.target.value)}
                                    />
                                </div>
                                {fieldErrors.email && (
                                    <p className="mt-1.5 text-xs text-red-600">{fieldErrors.email}</p>
                                )}
                            </div>

                            <div>
                                <div className="mb-2 flex items-center justify-between gap-4">
                                    <label
                                        className="block text-sm font-medium text-slate-700"
                                        htmlFor="password"
                                    >
                                        Mật khẩu
                                    </label>

                                    <Link
                                        to="/forgot-password"
                                        className="text-sm font-medium text-indigo-600 hover:text-indigo-700"
                                    >
                                        Quên mật khẩu?
                                    </Link>
                                </div>

                                <div className="relative">
                                    <LockKeyhole
                                        aria-hidden="true"
                                        className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400"
                                        size={19}
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
                                    <p className="mt-1.5 text-xs text-red-600">{fieldErrors.password}</p>
                                )}
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
                                disabled={loginMutation.isPending}
                                type="submit"
                            >
                                {loginMutation.isPending ? (
                                    <LoaderCircle aria-hidden="true" className="animate-spin" size={18} />
                                ) : (
                                    <LogIn aria-hidden="true" size={18} />
                                )}
                                {loginMutation.isPending ? 'Đang đăng nhập...' : 'Đăng nhập'}
                            </button>
                        </form>

                        <p className="mt-7 text-center text-sm text-slate-600">
                            Chưa có tài khoản?{' '}
                            <Link
                                className="font-semibold text-indigo-600 hover:text-indigo-700"
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