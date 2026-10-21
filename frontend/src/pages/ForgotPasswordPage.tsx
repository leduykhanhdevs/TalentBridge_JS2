import { useEffect, useState, type FormEvent } from 'react'
import {
    AlertCircle,
    ArrowLeft,
    CheckCircle2,
    Eye,
    EyeOff,
    KeyRound,
    LoaderCircle,
    LockKeyhole,
    Mail,
    RotateCw,
    Send,
    ShieldCheck,
} from 'lucide-react'
import { Link } from 'react-router'
import { useMutation } from '@tanstack/react-query'
import {
    AuthApiError,
    requestForgotPassword,
    resetPassword,
    verifyPasswordResetOtp,
} from '../features/auth/authApi'

type ResetStep = 'email' | 'otp' | 'password' | 'done'

const OTP_RESEND_SECONDS = 60

export function ForgotPasswordPage() {
    const [step, setStep] = useState<ResetStep>('email')
    const [email, setEmail] = useState('')
    const [otp, setOtp] = useState('')
    const [resetToken, setResetToken] = useState<string | null>(null)
    const [newPassword, setNewPassword] = useState('')
    const [confirmPassword, setConfirmPassword] = useState('')
    const [showNewPassword, setShowNewPassword] = useState(false)
    const [showConfirmPassword, setShowConfirmPassword] = useState(false)
    const [emailError, setEmailError] = useState<string | null>(null)
    const [otpError, setOtpError] = useState<string | null>(null)
    const [passwordError, setPasswordError] = useState<string | null>(null)
    const [confirmError, setConfirmError] = useState<string | null>(null)
    const [serverError, setServerError] = useState<string | null>(null)
    const [successMessage, setSuccessMessage] = useState<string | null>(null)
    const [resendSeconds, setResendSeconds] = useState(0)

    const forgotMutation = useMutation({
        mutationFn: requestForgotPassword,
        onSuccess: () => {
            setStep('otp')
            setOtp('')
            setOtpError(null)
            setServerError(null)
            setSuccessMessage(
                'Nếu email tồn tại trong hệ thống, mã OTP sẽ được gửi. Vui lòng kiểm tra hộp thư và Spam; nếu chưa nhận sau vài phút, hãy kiểm tra lại địa chỉ email hoặc liên hệ quản trị viên.',
            )
            setResendSeconds(OTP_RESEND_SECONDS)
        },
        onError: (error) => {
            setSuccessMessage(null)
            setServerError(errorMessage(error, 'Không thể gửi mã OTP. Vui lòng thử lại sau.'))
        },
    })

    const verifyMutation = useMutation({
        mutationFn: verifyPasswordResetOtp,
        onSuccess: ({ resetToken: token }) => {
            setResetToken(token)
            setStep('password')
            setServerError(null)
            setSuccessMessage(null)
        },
        onError: (error) => {
            setServerError(errorMessage(
                error,
                'Mã OTP không chính xác hoặc đã hết hạn. Bạn có thể nhập lại hoặc yêu cầu mã mới.',
            ))
        },
    })

    const resetMutation = useMutation({
        mutationFn: resetPassword,
        onSuccess: () => {
            setStep('done')
            setResetToken(null)
            setNewPassword('')
            setConfirmPassword('')
            setShowNewPassword(false)
            setShowConfirmPassword(false)
            setServerError(null)
            setSuccessMessage('Mật khẩu đã được cập nhật. Bạn có thể đăng nhập bằng mật khẩu mới.')
        },
        onError: (error) => {
            setServerError(errorMessage(error, 'Không thể đặt lại mật khẩu. Vui lòng thử lại.'))
        },
    })

    useEffect(() => {
        if (step !== 'otp' || resendSeconds <= 0) return

        const timer = window.setTimeout(() => {
            setResendSeconds((seconds) => Math.max(0, seconds - 1))
        }, 1_000)
        return () => window.clearTimeout(timer)
    }, [step, resendSeconds])

    function sendOtp() {
        setEmailError(null)
        setServerError(null)
        setSuccessMessage(null)

        const trimmedEmail = email.trim()
        if (!trimmedEmail) {
            setEmailError('Vui lòng nhập địa chỉ email.')
            return
        }
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(trimmedEmail)) {
            setEmailError('Địa chỉ email không đúng định dạng.')
            return
        }

        setEmail(trimmedEmail)
        forgotMutation.mutate({ email: trimmedEmail })
    }

    function handleRequestOtp(event: FormEvent) {
        event.preventDefault()
        sendOtp()
    }

    function handleVerifyOtp(event: FormEvent) {
        event.preventDefault()
        setServerError(null)
        if (!/^\d{6}$/.test(otp)) {
            setOtpError('Vui lòng nhập đủ 6 chữ số trong email.')
            return
        }
        setOtpError(null)
        verifyMutation.mutate({ email, otp })
    }

    function handleResetPassword(event: FormEvent) {
        event.preventDefault()
        setServerError(null)
        if (newPassword.length < 6) {
            setPasswordError('Mật khẩu mới cần có ít nhất 6 ký tự.')
            return
        }
        if (confirmPassword !== newPassword) {
            setConfirmError('Mật khẩu xác nhận chưa khớp.')
            return
        }
        if (!resetToken) {
            setServerError('Phiên xác minh đã hết hạn. Vui lòng yêu cầu mã OTP mới.')
            setStep('email')
            return
        }

        setPasswordError(null)
        setConfirmError(null)
        resetMutation.mutate({ token: resetToken, newPassword })
    }

    const heading = {
        email: 'Quên mật khẩu?',
        otp: 'Nhập mã xác minh',
        password: 'Tạo mật khẩu mới',
        done: 'Đổi mật khẩu thành công',
    }[step]

    const description = {
        email: 'Nhập địa chỉ email đã đăng ký để nhận mã OTP đặt lại mật khẩu.',
        otp: `Nhập mã OTP 6 chữ số nếu bạn nhận được email tại ${email}.`,
        password: 'Mã OTP đã được xác minh. Hãy chọn mật khẩu mới cho tài khoản của bạn.',
        done: 'Mật khẩu của bạn đã được thay đổi an toàn.',
    }[step]

    return (
        <section className="bg-bento-canvas min-h-[calc(100dvh-4rem)] flex items-center justify-center px-4 py-12 sm:px-6 lg:px-8">
            <div className="bento-card relative mx-auto w-full max-w-lg rounded-3xl border border-slate-200/80 bg-white p-6 shadow-xl sm:p-10">
                <div className="mb-6 flex items-center gap-2 border-b border-slate-100 pb-4">
                    <span className="size-2.5 rounded-full bg-rose-400" />
                    <span className="size-2.5 rounded-full bg-amber-400" />
                    <span className="size-2.5 rounded-full bg-emerald-400" />
                    <span className="ml-2 text-xs font-semibold text-slate-600">Khôi phục mật khẩu</span>
                </div>

                <div className="mx-auto grid size-12 place-items-center rounded-xl border border-blue-100 bg-blue-50 text-blue-700">
                    {step === 'done' ? <CheckCircle2 size={22} /> : step === 'password' ? <ShieldCheck size={22} /> : <KeyRound size={22} />}
                </div>

                <div className="mt-4 text-center">
                    <h1 className="text-2xl font-bold tracking-tight text-slate-900 sm:text-3xl">{heading}</h1>
                    <p className="mt-2 text-sm text-slate-600">{description}</p>
                </div>

                {step === 'email' && (
                    <form className="mt-6 space-y-4" onSubmit={handleRequestOtp} noValidate>
                        <div>
                            <label className="mb-1.5 block text-sm font-semibold text-slate-800" htmlFor="forgot-email">
                                Địa chỉ email
                            </label>
                            <div className="relative">
                                <Mail aria-hidden="true" className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500" size={18} />
                                <input
                                    autoComplete="email"
                                    className="h-12 w-full rounded-xl border border-slate-300 bg-white pl-11 pr-4 text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-blue-700 focus:ring-2 focus:ring-blue-100"
                                    id="forgot-email"
                                    name="email"
                                    placeholder="ban@example.com"
                                    type="email"
                                    value={email}
                                    onChange={(event) => {
                                        setEmail(event.target.value)
                                        setEmailError(null)
                                        setServerError(null)
                                    }}
                                />
                            </div>
                            {emailError && <p className="mt-1 text-xs font-semibold text-rose-700">{emailError}</p>}
                        </div>
                        <Feedback error={serverError} success={successMessage} />
                        <button className="btn-bento-primary mt-2 h-12 w-full text-sm font-semibold" disabled={forgotMutation.isPending} type="submit">
                            {forgotMutation.isPending ? <LoaderCircle aria-hidden="true" className="animate-spin" size={18} /> : <Send aria-hidden="true" size={18} />}
                            <span>{forgotMutation.isPending ? 'Đang gửi mã OTP...' : 'Gửi mã OTP'}</span>
                        </button>
                    </form>
                )}

                {step === 'otp' && (
                    <form className="mt-6 space-y-4" onSubmit={handleVerifyOtp} noValidate>
                        <div>
                            <label className="mb-1.5 block text-sm font-semibold text-slate-800" htmlFor="forgot-otp">Mã OTP</label>
                            <input
                                autoComplete="one-time-code"
                                className="h-14 w-full rounded-xl border border-slate-300 bg-white px-4 text-center font-mono text-2xl font-bold tracking-[0.45em] text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-blue-700 focus:ring-2 focus:ring-blue-100"
                                id="forgot-otp"
                                inputMode="numeric"
                                maxLength={6}
                                name="otp"
                                placeholder="••••••"
                                value={otp}
                                onChange={(event) => {
                                    setOtp(event.target.value.replace(/\D/g, '').slice(0, 6))
                                    setOtpError(null)
                                    setServerError(null)
                                }}
                            />
                            {otpError && <p className="mt-1 text-xs font-semibold text-rose-700">{otpError}</p>}
                        </div>
                        <Feedback error={serverError} success={successMessage} />
                        <button className="btn-bento-primary h-12 w-full text-sm font-semibold" disabled={verifyMutation.isPending} type="submit">
                            {verifyMutation.isPending ? <LoaderCircle aria-hidden="true" className="animate-spin" size={18} /> : <KeyRound aria-hidden="true" size={18} />}
                            <span>{verifyMutation.isPending ? 'Đang xác minh...' : 'Xác minh OTP'}</span>
                        </button>
                        <div className="flex flex-wrap items-center justify-between gap-3 text-sm">
                            <button className="font-semibold text-slate-600 hover:text-blue-700" onClick={() => {
                                setStep('email')
                                setServerError(null)
                                setSuccessMessage(null)
                            }} type="button">
                                Sửa email
                            </button>
                            <button
                                className="inline-flex items-center gap-2 font-semibold text-blue-700 disabled:cursor-not-allowed disabled:text-slate-400"
                                disabled={resendSeconds > 0 || forgotMutation.isPending}
                                onClick={sendOtp}
                                type="button"
                            >
                                {forgotMutation.isPending ? <LoaderCircle className="animate-spin" size={15} /> : <RotateCw size={15} />}
                                {resendSeconds > 0 ? `Gửi lại sau ${resendSeconds}s` : 'Gửi mã mới'}
                            </button>
                        </div>
                    </form>
                )}

                {step === 'password' && (
                    <form className="mt-6 space-y-4" onSubmit={handleResetPassword} noValidate>
                        <div>
                            <label className="mb-1.5 block text-sm font-semibold text-slate-800" htmlFor="new-password">Mật khẩu mới</label>
                            <div className="relative">
                                <LockKeyhole aria-hidden="true" className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500" size={18} />
                                <input
                                    autoComplete="new-password"
                                    className="h-12 w-full rounded-xl border border-slate-300 bg-white pl-11 pr-12 text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-blue-700 focus:ring-2 focus:ring-blue-100"
                                    id="new-password"
                                    minLength={6}
                                    name="newPassword"
                                    placeholder="Tối thiểu 6 ký tự"
                                    type={showNewPassword ? 'text' : 'password'}
                                    value={newPassword}
                                    onChange={(event) => {
                                        setNewPassword(event.target.value)
                                        setPasswordError(null)
                                        setServerError(null)
                                    }}
                                />
                                <button
                                    aria-label={showNewPassword ? 'Ẩn mật khẩu mới' : 'Hiện mật khẩu mới'}
                                    aria-pressed={showNewPassword}
                                    className="absolute inset-y-0 right-0 flex items-center px-3 text-slate-500 hover:text-blue-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-blue-600"
                                    onClick={() => setShowNewPassword((visible) => !visible)}
                                    type="button"
                                >
                                    {showNewPassword ? <EyeOff aria-hidden="true" size={18} /> : <Eye aria-hidden="true" size={18} />}
                                </button>
                            </div>
                            {passwordError && <p className="mt-1 text-xs font-semibold text-rose-700">{passwordError}</p>}
                        </div>
                        <div>
                            <label className="mb-1.5 block text-sm font-semibold text-slate-800" htmlFor="confirm-new-password">Xác nhận mật khẩu mới</label>
                            <div className="relative">
                                <LockKeyhole aria-hidden="true" className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500" size={18} />
                                <input
                                    autoComplete="new-password"
                                    className="h-12 w-full rounded-xl border border-slate-300 bg-white pl-11 pr-12 text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-blue-700 focus:ring-2 focus:ring-blue-100"
                                    id="confirm-new-password"
                                    name="confirmPassword"
                                    placeholder="Nhập lại mật khẩu mới"
                                    type={showConfirmPassword ? 'text' : 'password'}
                                    value={confirmPassword}
                                    onChange={(event) => {
                                        setConfirmPassword(event.target.value)
                                        setConfirmError(null)
                                        setServerError(null)
                                    }}
                                />
                                <button
                                    aria-label={showConfirmPassword ? 'Ẩn mật khẩu xác nhận' : 'Hiện mật khẩu xác nhận'}
                                    aria-pressed={showConfirmPassword}
                                    className="absolute inset-y-0 right-0 flex items-center px-3 text-slate-500 hover:text-blue-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-blue-600"
                                    onClick={() => setShowConfirmPassword((visible) => !visible)}
                                    type="button"
                                >
                                    {showConfirmPassword ? <EyeOff aria-hidden="true" size={18} /> : <Eye aria-hidden="true" size={18} />}
                                </button>
                            </div>
                            {confirmError && <p className="mt-1 text-xs font-semibold text-rose-700">{confirmError}</p>}
                        </div>
                        <Feedback error={serverError} success={successMessage} />
                        <button className="btn-bento-primary mt-2 h-12 w-full text-sm font-semibold" disabled={resetMutation.isPending} type="submit">
                            {resetMutation.isPending ? <LoaderCircle aria-hidden="true" className="animate-spin" size={18} /> : <ShieldCheck aria-hidden="true" size={18} />}
                            <span>{resetMutation.isPending ? 'Đang cập nhật mật khẩu...' : 'Lưu mật khẩu mới'}</span>
                        </button>
                        <button className="mx-auto block text-sm font-semibold text-slate-600 hover:text-blue-700" onClick={() => {
                            setStep('otp')
                            setResetToken(null)
                            setServerError(null)
                        }} type="button">
                            Quay lại nhập mã OTP
                        </button>
                    </form>
                )}

                {step === 'done' && (
                    <div className="mt-6 space-y-4">
                        <Feedback success={successMessage} />
                        <Link className="btn-bento-primary flex h-12 w-full items-center justify-center gap-2 text-sm font-semibold" to="/login">
                            <ArrowLeft aria-hidden="true" size={18} />
                            <span>Quay lại đăng nhập</span>
                        </Link>
                    </div>
                )}

                {step !== 'done' && (
                    <div className="mt-6 border-t border-slate-100 pt-4 text-center">
                        <Link className="inline-flex items-center gap-2 text-sm font-semibold text-slate-700 hover:text-blue-700" to="/login">
                            <ArrowLeft size={14} /> Quay lại trang Đăng nhập
                        </Link>
                    </div>
                )}
            </div>
        </section>
    )
}

function Feedback({ error, success }: { error?: string | null; success?: string | null }) {
    if (error) {
        return (
            <div className="flex gap-2.5 rounded-xl border border-rose-200 bg-rose-50 p-3 text-sm font-semibold text-rose-800" role="alert">
                <AlertCircle aria-hidden="true" className="mt-0.5 shrink-0 text-rose-700" size={17} />
                <span>{error}</span>
            </div>
        )
    }
    if (success) {
        return (
            <div className="flex gap-2.5 rounded-xl border border-emerald-200 bg-emerald-50 p-3 text-sm font-semibold text-emerald-800" role="status">
                <CheckCircle2 aria-hidden="true" className="mt-0.5 shrink-0 text-emerald-700" size={17} />
                <span>{success}</span>
            </div>
        )
    }
    return null
}

function errorMessage(error: unknown, fallback: string): string {
    return error instanceof AuthApiError ? error.message : fallback
}
