import { useState, type FormEvent } from 'react'
import {
    AlertCircle,
    ArrowLeft,
    CheckCircle2,
    KeyRound,
    LoaderCircle,
    Mail,
    Send,
} from 'lucide-react'
import { Link } from 'react-router'
import { useMutation } from '@tanstack/react-query'
import { AuthApiError, requestForgotPassword } from '../features/auth/authApi'

export function ForgotPasswordPage() {
    const [email, setEmail] = useState('')
    const [emailError, setEmailError] = useState<string | null>(null)
    const [serverError, setServerError] = useState<string | null>(null)
    const [successMessage, setSuccessMessage] = useState<string | null>(null)

    const forgotMutation = useMutation({
        mutationFn: requestForgotPassword,
        onSuccess: () => {
            setServerError(null)
            setSuccessMessage(
                'Nếu địa chỉ email tồn tại trong hệ thống, hướng dẫn đặt lại mật khẩu đã được gửi tới hộp thư của bạn. Vui lòng kiểm tra email (kể cả thư mục Spam).'
            )
        },
        onError: (error) => {
            if (error instanceof AuthApiError) {
                setServerError(error.message)
            } else {
                setServerError('Đã có lỗi xảy ra. Vui lòng thử lại sau.')
            }
        },
    })

    function handleSubmit(event: FormEvent) {
        event.preventDefault()
        setServerError(null)

        const trimmed = email.trim()
        if (!trimmed) {
            setEmailError('Vui lòng nhập địa chỉ email.')
            return
        }
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(trimmed)) {
            setEmailError('Địa chỉ email không đúng định dạng.')
            return
        }

        setEmailError(null)
        forgotMutation.mutate({ email: trimmed })
    }

    return (
        <section className="bg-bento-canvas min-h-[calc(100dvh-4rem)] flex items-center justify-center px-4 py-16 sm:px-6 lg:px-8">
            <div className="bento-card relative mx-auto max-w-lg w-full rounded-3xl border border-slate-200/80 bg-white p-6 sm:p-10 shadow-xl">
                <div className="flex items-center justify-between border-b border-slate-100 pb-4 mb-6">
                    <div className="flex items-center gap-2">
                        <span className="size-2.5 rounded-full bg-rose-400" />
                        <span className="size-2.5 rounded-full bg-amber-400" />
                        <span className="size-2.5 rounded-full bg-emerald-400" />
                        <span className="ml-2 text-xs font-semibold text-slate-500">Khôi phục mật khẩu</span>
                    </div>
                    <span className="bento-badge bg-amber-50 text-amber-700 border-amber-200/60 text-[11px]">
                        Bảo mật tài khoản
                    </span>
                </div>

                <div className="mx-auto grid size-12 place-items-center rounded-xl bg-indigo-50 border border-indigo-100 text-indigo-600">
                    <KeyRound size={22} />
                </div>

                <div className="mt-4 text-center">
                    <h1 className="text-2xl font-bold tracking-tight text-slate-900 sm:text-3xl">
                        Quên mật khẩu?
                    </h1>
                    <p className="mt-2 text-xs sm:text-sm text-slate-600">
                        Nhập địa chỉ email đã đăng ký để nhận liên kết an toàn đặt lại mật khẩu.
                    </p>
                </div>

                <form className="mt-6 space-y-4" onSubmit={handleSubmit} noValidate>
                    <div>
                        <label
                            className="mb-1.5 block text-xs font-semibold text-slate-700"
                            htmlFor="forgot-email"
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
                                className={`h-12 w-full rounded-xl border-2 bg-white pl-11 pr-4 text-slate-900 outline-none font-mono text-sm transition placeholder:text-slate-400 ${
                                    emailError
                                        ? 'border-rose-500 bg-rose-50/20 focus:border-rose-600 focus:shadow-[2px_2px_0px_0px_#f43f5e]'
                                        : 'border-slate-200 focus:border-indigo-600 focus:ring-2 focus:ring-indigo-100'
                                }`}
                                id="forgot-email"
                                name="email"
                                placeholder="ban@example.com"
                                type="email"
                                value={email}
                                onChange={(e) => {
                                    setEmail(e.target.value)
                                    if (emailError) setEmailError(null)
                                    if (serverError) setServerError(null)
                                }}
                            />
                        </div>
                        {emailError && (
                            <p className="mt-1 text-xs font-bold text-rose-600 font-mono">{emailError}</p>
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
                        disabled={forgotMutation.isPending}
                        type="submit"
                    >
                        {forgotMutation.isPending ? (
                            <LoaderCircle aria-hidden="true" className="animate-spin" size={18} />
                        ) : (
                            <Send aria-hidden="true" size={18} />
                        )}
                        <span>{forgotMutation.isPending ? 'Đang gửi yêu cầu...' : 'Gửi liên kết đặt lại'}</span>
                    </button>
                </form>

                <div className="mt-6 border-t border-slate-100 pt-4 text-center">
                    <Link
                        className="inline-flex items-center gap-2 text-xs font-bold text-slate-800 hover:text-indigo-600 underline"
                        to="/login"
                    >
                        <ArrowLeft size={14} /> Quay lại trang Đăng nhập
                    </Link>
                </div>
            </div>
        </section>
    )
}
