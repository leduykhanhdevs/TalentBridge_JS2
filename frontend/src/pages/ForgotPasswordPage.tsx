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
        <section className="relative overflow-hidden bg-slate-50 px-4 py-16 sm:px-6 lg:px-8">
            <div
                aria-hidden="true"
                className="absolute -left-32 top-20 size-80 rounded-full bg-indigo-100/70 blur-3xl"
            />
            <div
                aria-hidden="true"
                className="absolute -right-32 bottom-10 size-80 rounded-full bg-violet-100/70 blur-3xl"
            />

            <div className="relative mx-auto max-w-lg rounded-3xl border border-slate-200 bg-white p-8 shadow-xl shadow-slate-200/60 sm:p-10">
                <div className="mx-auto grid size-12 place-items-center rounded-2xl bg-indigo-600 text-white">
                    <KeyRound size={24} />
                </div>

                <div className="mt-6 text-center">
                    <h1 className="text-2xl font-bold tracking-tight text-slate-950 sm:text-3xl">
                        Quên mật khẩu?
                    </h1>
                    <p className="mt-2 text-sm leading-6 text-slate-600">
                        Nhập địa chỉ email đã đăng ký. Chúng tôi sẽ gửi cho bạn liên kết an toàn để đặt lại mật khẩu mới.
                    </p>
                </div>

                <form className="mt-8 space-y-5" onSubmit={handleSubmit} noValidate>
                    <div>
                        <label
                            className="mb-2 block text-sm font-medium text-slate-700"
                            htmlFor="forgot-email"
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
                                className={`h-12 w-full rounded-xl border bg-white pl-11 pr-4 text-slate-900 outline-none transition placeholder:text-slate-400 ${
                                    emailError
                                        ? 'border-red-300 focus:border-red-500 focus:ring-4 focus:ring-red-100'
                                        : 'border-slate-300 focus:border-indigo-500 focus:ring-4 focus:ring-indigo-100'
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
                            <p className="mt-1.5 text-xs text-red-600">{emailError}</p>
                        )}
                    </div>

                    {serverError && (
                        <div
                            className="flex gap-3 rounded-xl border border-red-200 bg-red-50 p-3.5 text-sm leading-6 text-red-700"
                            role="alert"
                        >
                            <AlertCircle aria-hidden="true" className="mt-0.5 shrink-0" size={18} />
                            <span>{serverError}</span>
                        </div>
                    )}

                    {successMessage && (
                        <div
                            className="flex gap-3 rounded-xl border border-emerald-200 bg-emerald-50 p-3.5 text-sm leading-6 text-emerald-700"
                            role="status"
                        >
                            <CheckCircle2 aria-hidden="true" className="mt-0.5 shrink-0" size={18} />
                            <span>{successMessage}</span>
                        </div>
                    )}

                    <button
                        className="inline-flex h-12 w-full items-center justify-center gap-2 rounded-xl bg-indigo-600 px-5 text-sm font-semibold text-white transition hover:bg-indigo-700 disabled:cursor-wait disabled:opacity-70"
                        disabled={forgotMutation.isPending}
                        type="submit"
                    >
                        {forgotMutation.isPending ? (
                            <LoaderCircle aria-hidden="true" className="animate-spin" size={18} />
                        ) : (
                            <Send aria-hidden="true" size={18} />
                        )}
                        {forgotMutation.isPending ? 'Đang gửi yêu cầu...' : 'Gửi liên kết đặt lại'}
                    </button>
                </form>

                <div className="mt-8 border-t border-slate-100 pt-6 text-center">
                    <Link
                        className="inline-flex items-center gap-2 text-sm font-semibold text-slate-600 transition hover:text-indigo-600"
                        to="/login"
                    >
                        <ArrowLeft size={16} /> Quay lại trang Đăng nhập
                    </Link>
                </div>
            </div>
        </section>
    )
}
