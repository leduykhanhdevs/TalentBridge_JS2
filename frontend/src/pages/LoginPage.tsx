import { useState, type FormEvent } from 'react'
import { AlertCircle, CircleHelp, Eye, EyeOff, LoaderCircle, LockKeyhole, LogIn, Mail } from 'lucide-react'
import { Link, useLocation, useNavigate } from 'react-router'
import { useMutation } from '@tanstack/react-query'
import { HeroTalentIllustration } from '../components/illustrations'
import { AuthApiError, loginUser, LOGOUT_TIMEOUT_MS } from '../features/auth/authApi'
import { warmUpBackend } from '../app/backendWarmup'
import { getCandidatePostLoginPath } from '../features/auth/loginRedirect'
import { saveAuthTokens } from '../features/auth/tokenStorage'
import { REFRESH_TIMEOUT_MS } from '../features/auth/sessionRefresh'
import { withAuthSessionLock } from '../features/auth/authSessionLock'

type FormField = 'email' | 'password'
type FieldErrors = Partial<Record<FormField, string>>

export function LoginPage() {
    const navigate = useNavigate()
    const location = useLocation()
    const [formValues, setFormValues] = useState({ email: '', password: '' })
    const [fieldErrors, setFieldErrors] = useState<FieldErrors>({})
    const [serverError, setServerError] = useState<string | null>(null)
    const [isConnecting, setIsConnecting] = useState(false)
    const [showPassword, setShowPassword] = useState(false)

    const loginMutation = useMutation({
        mutationFn: async (request: Parameters<typeof loginUser>[0]) => {
            setIsConnecting(true)
            let backendReady: boolean
            try {
                backendReady = await warmUpBackend()
            } finally {
                setIsConnecting(false)
            }
            if (!backendReady) {
                throw new AuthApiError(0, 'Máy chủ chưa sẵn sàng sau thời gian chờ. Vui lòng thử lại sau ít phút.')
            }
            return loginUser(request)
        },
        onSuccess: async (data) => {
            await withAuthSessionLock(() => saveAuthTokens(data), LOGOUT_TIMEOUT_MS + REFRESH_TIMEOUT_MS)
            const roles = data.user.roles ?? []
            if (roles.includes('ROLE_ADMIN')) {
                navigate('/admin/candidates')
            } else if (roles.includes('ROLE_RECRUITER')) {
                navigate('/recruiter/profile')
            } else if (roles.includes('ROLE_CANDIDATE')) {
                navigate(getCandidatePostLoginPath(location.state))
            } else {
                navigate('/')
            }
        },
        onError: (error) => {
            if (error instanceof AuthApiError) {
                setServerError(error.message)
                if (error.fieldErrors) setFieldErrors(error.fieldErrors)
            } else {
                setServerError('Đã có lỗi xảy ra. Vui lòng thử lại sau.')
            }
        },
    })

    function validate(): boolean {
        const errors: FieldErrors = {}
        const email = formValues.email.trim()
        if (!email) errors.email = 'Vui lòng nhập địa chỉ email.'
        else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) errors.email = 'Địa chỉ email không đúng định dạng.'
        if (!formValues.password) errors.password = 'Vui lòng nhập mật khẩu.'
        setFieldErrors(errors)
        return Object.keys(errors).length === 0
    }

    function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()
        setServerError(null)
        if (!validate()) return
        loginMutation.mutate({ email: formValues.email.trim(), password: formValues.password })
    }

    function updateField(field: FormField, value: string) {
        setFormValues((current) => ({ ...current, [field]: value }))
        setFieldErrors((current) => ({ ...current, [field]: undefined }))
        if (serverError) setServerError(null)
    }

    function inputClassName(field: FormField) {
        const base = 'h-12 w-full rounded-lg border bg-white pl-11 pr-4 text-sm text-slate-900 outline-none transition placeholder:text-slate-500'
        return fieldErrors[field]
            ? `${base} border-rose-400 bg-rose-50/30 focus:border-rose-500 focus:ring-2 focus:ring-rose-100`
            : `${base} border-slate-300 focus:border-blue-700 focus:ring-2 focus:ring-blue-100`
    }

    return (
        <section className="bg-bento-canvas flex min-h-[calc(100dvh-4rem)] items-center justify-center px-4 py-10 sm:px-6 lg:px-8">
            <div className="mx-auto grid w-full max-w-6xl items-stretch gap-8 lg:grid-cols-2 lg:gap-12">
                <div className="flex flex-col justify-center py-4 lg:py-10">
                    <p className="text-xs font-bold uppercase tracking-[0.14em] text-blue-700">Mối kết nối có ý nghĩa</p>
                    <h1 className="mt-5 max-w-xl text-3xl font-bold leading-tight tracking-tight text-slate-900 sm:text-4xl">
                        Năng lực của bạn.
                        <br />
                        Cơ hội xứng đáng.
                    </h1>
                    <p className="mt-4 max-w-lg text-base leading-7 text-slate-600">
                        Đăng nhập để tiếp tục hành trình nghề nghiệp và quản lý hồ sơ tuyển dụng của bạn.
                    </p>
                    <div className="mt-5 w-full max-w-[500px] self-center lg:mt-8">
                        <HeroTalentIllustration className="h-auto w-full" />
                    </div>
                    <div className="mt-5 flex flex-wrap gap-x-6 gap-y-2 text-sm font-medium text-slate-600">
                        <span className="inline-flex items-center gap-2"><span className="size-2 rounded-full bg-blue-700" />Bảo vệ thông tin tài khoản</span>
                        <span className="inline-flex items-center gap-2"><span className="size-2 rounded-full bg-emerald-700" />Phân quyền theo vai trò</span>
                    </div>
                </div>

                <div className="flex items-center justify-center">
                    <div className="bento-card w-full max-w-[520px] p-6 sm:p-9 lg:p-10">
                        <div className="flex items-center justify-between gap-4">
                            <span className="bento-badge border border-blue-100 bg-blue-50 px-3 py-1 text-xs font-semibold text-blue-800">
                                Xác thực tài khoản
                            </span>
                            <span className="text-xs font-semibold text-slate-500">Đăng nhập</span>
                        </div>

                        <h2 className="mt-5 text-2xl font-bold tracking-tight text-slate-900">Chào mừng bạn trở lại</h2>
                        <p className="mt-2 text-sm leading-6 text-slate-600">Đăng nhập để tiếp tục hành trình nghề nghiệp.</p>

                        <form className="mt-7 space-y-5" onSubmit={handleSubmit} noValidate>
                            <div>
                                <label className="mb-2 block text-sm font-semibold text-slate-800" htmlFor="email">Email</label>
                                <div className="relative">
                                    <Mail aria-hidden="true" className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500" size={18} />
                                    <input
                                        autoComplete="email"
                                        className={inputClassName('email')}
                                        id="email"
                                        name="email"
                                        placeholder="ban@example.com"
                                        type="email"
                                        value={formValues.email}
                                        onChange={(event) => updateField('email', event.target.value)}
                                    />
                                </div>
                                {fieldErrors.email && <p className="mt-1.5 text-xs font-medium text-rose-700" role="alert">{fieldErrors.email}</p>}
                            </div>

                            <div>
                                <div className="mb-2 flex items-center justify-between gap-4">
                                    <label className="block text-sm font-semibold text-slate-800" htmlFor="password">Mật khẩu</label>
                                    <Link className="inline-flex items-center gap-1 text-sm font-semibold text-blue-700 underline-offset-4 hover:underline" to="/forgot-password">
                                        <CircleHelp aria-hidden="true" size={15} /> Quên mật khẩu?
                                    </Link>
                                </div>
                                <div className="relative">
                                    <LockKeyhole aria-hidden="true" className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500" size={18} />
                                    <input
                                        autoComplete="current-password"
                                        className={`${inputClassName('password')} pr-11`}
                                        id="password"
                                        name="password"
                                        placeholder="Nhập mật khẩu"
                                        type={showPassword ? 'text' : 'password'}
                                        value={formValues.password}
                                        onChange={(event) => updateField('password', event.target.value)}
                                    />
                                    <button
                                        aria-label={showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                                        aria-pressed={showPassword}
                                        className="absolute inset-y-0 right-0 flex items-center px-3 text-slate-500 hover:text-blue-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-blue-600"
                                        onClick={() => setShowPassword((visible) => !visible)}
                                        type="button"
                                    >
                                        {showPassword ? <EyeOff aria-hidden="true" size={18} /> : <Eye aria-hidden="true" size={18} />}
                                    </button>
                                </div>
                                {fieldErrors.password && <p className="mt-1.5 text-xs font-medium text-rose-700" role="alert">{fieldErrors.password}</p>}
                            </div>

                            {serverError && (
                                <div className="flex gap-2.5 rounded-lg border border-rose-200 bg-rose-50 p-3.5 text-sm font-medium text-rose-800" role="alert">
                                    <AlertCircle aria-hidden="true" className="mt-0.5 shrink-0 text-rose-700" size={17} />
                                    <span>{serverError}</span>
                                </div>
                            )}

                            <button className="btn-bento-primary mt-2 h-12 w-full text-sm font-semibold" disabled={loginMutation.isPending} type="submit">
                                {loginMutation.isPending ? <LoaderCircle aria-hidden="true" className="animate-spin" size={18} /> : <LogIn aria-hidden="true" size={18} />}
                                <span>{loginMutation.isPending ? (isConnecting ? 'Đang kết nối máy chủ...' : 'Đang đăng nhập...') : 'Đăng nhập'}</span>
                            </button>
                            {isConnecting && <p className="text-center text-xs text-slate-500" role="status">Máy chủ miễn phí có thể cần đến khoảng 1 phút để khởi động sau khi không hoạt động.</p>}
                        </form>

                        <p className="mt-6 text-center text-sm text-slate-600">
                            Chưa có tài khoản? <Link className="font-semibold text-blue-700 underline-offset-4 hover:underline" to="/register">Đăng ký ngay</Link>
                        </p>
                    </div>
                </div>
            </div>
        </section>
    )
}
