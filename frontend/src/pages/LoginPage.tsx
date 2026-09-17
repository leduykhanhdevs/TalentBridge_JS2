import { useMutation } from '@tanstack/react-query'
import {
    AlertCircle,
    CheckCircle2,
    LoaderCircle,
    LockKeyhole,
    LogIn,
    Mail,
    ShieldCheck,
} from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router'
import { AuthApiError, login } from '../features/auth/authApi'
import { getRoleHomePath } from '../features/auth/authRoutes'
import { saveAuthTokens } from '../features/auth/tokenStorage'

const inputClassName =
    'h-12 w-full rounded-xl border border-slate-300 bg-white pl-11 pr-4 text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-indigo-500 focus:ring-4 focus:ring-indigo-100 disabled:cursor-not-allowed disabled:bg-slate-50'

const invalidInputClassName =
    'border-red-400 focus:border-red-500 focus:ring-red-100'

type LoginFormValues = {
    email: string
    password: string
}

type LoginFormErrors = Partial<Record<keyof LoginFormValues, string>>

const initialFormValues: LoginFormValues = {
    email: '',
    password: '',
}

function validateForm(values: LoginFormValues): LoginFormErrors {
    const errors: LoginFormErrors = {}
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

    if (!values.email.trim()) {
        errors.email = 'Vui lòng nhập địa chỉ email.'
    } else if (!emailPattern.test(values.email.trim())) {
        errors.email = 'Địa chỉ email không đúng định dạng.'
    }

    if (!values.password) {
        errors.password = 'Vui lòng nhập mật khẩu.'
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

export function LoginPage() {
    const navigate = useNavigate()
    const [formValues, setFormValues] =
        useState<LoginFormValues>(initialFormValues)
    const [rememberMe, setRememberMe] = useState(false)
    const [fieldErrors, setFieldErrors] = useState<LoginFormErrors>({})
    const [serverError, setServerError] = useState('')

    const loginMutation = useMutation({
        mutationFn: login,
        onSuccess: (authResponse) => {
            saveAuthTokens(authResponse, rememberMe)
            navigate(getRoleHomePath(authResponse.user.roles), { replace: true })
        },
        onError: (error) => {
            if (!(error instanceof AuthApiError)) {
                setServerError('Đã xảy ra lỗi không xác định. Vui lòng thử lại.')
                return
            }

            if (error.fieldErrors) {
                setFieldErrors({
                    email: error.fieldErrors.email,
                    password: error.fieldErrors.password,
                })
                setServerError('Vui lòng kiểm tra lại thông tin đăng nhập.')
                return
            }

            if (error.status === 401) {
                setServerError('Email hoặc mật khẩu không chính xác.')
                return
            }

            if (error.status === 403) {
                setServerError('Tài khoản chưa hoạt động hoặc đã bị khóa.')
                return
            }

            if (error.status === 429) {
                setServerError(
                    'Bạn đã đăng nhập sai quá nhiều lần. Vui lòng thử lại sau.',
                )
                return
            }

            setServerError(error.message)
        },
    })

    function updateField(field: keyof LoginFormValues, value: string) {
        setFormValues((currentValues) => ({
            ...currentValues,
            [field]: value,
        }))
        setFieldErrors((currentErrors) => ({
            ...currentErrors,
            [field]: undefined,
        }))
        setServerError('')
    }

    function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()

        const validationErrors = validateForm(formValues)
        setFieldErrors(validationErrors)
        setServerError('')

        if (Object.keys(validationErrors).length > 0) {
            return
        }

        loginMutation.mutate({
            email: formValues.email.trim().toLowerCase(),
            password: formValues.password,
        })
    }

    function getInputClassName(field: keyof LoginFormValues) {
        return `${inputClassName} ${fieldErrors[field] ? invalidInputClassName : ''}`
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
                            Một cổng đăng nhập an toàn cho Ứng viên, Nhà tuyển dụng và
                            Quản trị viên.
                        </p>
                    </div>

                    <div className="relative mt-12 space-y-4 text-sm text-slate-300">
                        <p className="flex items-center gap-3">
                            <CheckCircle2
                                aria-hidden="true"
                                className="text-emerald-400"
                                size={19}
                            />
                            Điều hướng đúng không gian theo vai trò
                        </p>
                        <p className="flex items-center gap-3">
                            <CheckCircle2
                                aria-hidden="true"
                                className="text-emerald-400"
                                size={19}
                            />
                            Phiên đăng nhập được bảo vệ bằng JWT
                        </p>
                        <p className="flex items-center gap-3">
                            <CheckCircle2
                                aria-hidden="true"
                                className="text-emerald-400"
                                size={19}
                            />
                            Thông báo lỗi rõ ràng và an toàn
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

                        <form className="mt-8 space-y-5" noValidate onSubmit={handleSubmit}>
                            <div>
                                <label
                                    className="mb-2 block text-sm font-medium text-slate-700"
                                    htmlFor="login-email"
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
                                        aria-describedby={fieldErrors.email ? 'login-email-error' : undefined}
                                        aria-invalid={Boolean(fieldErrors.email)}
                                        autoComplete="email"
                                        className={getInputClassName('email')}
                                        disabled={loginMutation.isPending}
                                        id="login-email"
                                        name="email"
                                        onChange={(event) => updateField('email', event.target.value)}
                                        placeholder="ban@example.com"
                                        type="email"
                                        value={formValues.email}
                                    />
                                </div>
                                <FieldError id="login-email-error" message={fieldErrors.email} />
                            </div>

                            <div>
                                <div className="mb-2 flex items-center justify-between gap-4">
                                    <label
                                        className="block text-sm font-medium text-slate-700"
                                        htmlFor="login-password"
                                    >
                                        Mật khẩu
                                    </label>
                                    <span className="text-sm text-slate-400">Quên mật khẩu?</span>
                                </div>
                                <div className="relative">
                                    <LockKeyhole
                                        aria-hidden="true"
                                        className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400"
                                        size={19}
                                    />
                                    <input
                                        aria-describedby={fieldErrors.password ? 'login-password-error' : undefined}
                                        aria-invalid={Boolean(fieldErrors.password)}
                                        autoComplete="current-password"
                                        className={getInputClassName('password')}
                                        disabled={loginMutation.isPending}
                                        id="login-password"
                                        name="password"
                                        onChange={(event) => updateField('password', event.target.value)}
                                        placeholder="Nhập mật khẩu"
                                        type="password"
                                        value={formValues.password}
                                    />
                                </div>
                                <FieldError id="login-password-error" message={fieldErrors.password} />
                            </div>

                            <label className="flex items-center gap-3 text-sm text-slate-600">
                                <input
                                    checked={rememberMe}
                                    className="size-4 rounded border-slate-300 text-indigo-600 accent-indigo-600"
                                    disabled={loginMutation.isPending}
                                    onChange={(event) => setRememberMe(event.target.checked)}
                                    type="checkbox"
                                />
                                Ghi nhớ đăng nhập trên thiết bị này
                            </label>

                            {serverError ? (
                                <div
                                    className="flex items-start gap-3 rounded-xl border border-red-200 bg-red-50 p-3 text-sm leading-6 text-red-700"
                                    role="alert"
                                >
                                    <AlertCircle
                                        aria-hidden="true"
                                        className="mt-0.5 shrink-0"
                                        size={18}
                                    />
                                    <span>{serverError}</span>
                                </div>
                            ) : null}

                            <button
                                className="inline-flex h-12 w-full items-center justify-center gap-2 rounded-xl bg-indigo-600 px-5 text-sm font-semibold text-white shadow-sm transition hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-70"
                                disabled={loginMutation.isPending}
                                type="submit"
                            >
                                {loginMutation.isPending ? (
                                    <LoaderCircle
                                        aria-hidden="true"
                                        className="animate-spin"
                                        size={19}
                                    />
                                ) : (
                                    <LogIn aria-hidden="true" size={19} />
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
