import { useState, type FormEvent } from 'react'
import {
    AlertCircle,
    CheckCircle2,
    KeyRound,
    LoaderCircle,
    LockKeyhole,
    ShieldCheck,
} from 'lucide-react'
import { Link, useNavigate, useSearchParams } from 'react-router'
import { useMutation } from '@tanstack/react-query'
import { AuthApiError, resetPassword } from '../features/auth/authApi'

export function ResetPasswordPage() {
    const navigate = useNavigate()
    const [searchParams] = useSearchParams()
    const token = searchParams.get('token')

    const [newPassword, setNewPassword] = useState('')
    const [confirmPassword, setConfirmPassword] = useState('')
    const [passwordError, setPasswordError] = useState<string | null>(null)
    const [confirmError, setConfirmError] = useState<string | null>(null)
    const [serverError, setServerError] = useState<string | null>(null)
    const [successMessage, setSuccessMessage] = useState<string | null>(null)

    const resetMutation = useMutation({
        mutationFn: resetPassword,
        onSuccess: () => {
            setServerError(null)
            setSuccessMessage(
                'Đặt lại mật khẩu thành công! Bạn có thể đăng nhập bằng mật khẩu mới.'
            )
            setTimeout(() => {
                navigate('/login')
            }, 2500)
        },
        onError: (error) => {
            if (error instanceof AuthApiError) {
                setServerError(error.message)
            } else {
                setServerError('Đã có lỗi xảy ra. Vui lòng thử lại sau.')
            }
        },
    })

    function validate(): boolean {
        let valid = true

        if (!newPassword) {
            setPasswordError('Vui lòng nhập mật khẩu mới.')
            valid = false
        } else if (newPassword.length < 6) {
            setPasswordError('Mật khẩu phải có ít nhất 6 ký tự.')
            valid = false
        } else {
            setPasswordError(null)
        }

        if (!confirmPassword) {
            setConfirmError('Vui lòng xác nhận mật khẩu mới.')
            valid = false
        } else if (confirmPassword !== newPassword) {
            setConfirmError('Mật khẩu xác nhận không trùng khớp.')
            valid = false
        } else {
            setConfirmError(null)
        }

        return valid
    }

    function handleSubmit(event: FormEvent) {
        event.preventDefault()
        setServerError(null)

        if (!token) {
            setServerError('Mã đặt lại mật khẩu không hợp lệ hoặc bị thiếu trên URL.')
            return
        }

        if (!validate()) {
            return
        }

        resetMutation.mutate({
            token,
            newPassword,
        })
    }

    if (!token) {
        return (
            <section className="px-4 py-16 sm:px-6 lg:px-8">
                <div className="mx-auto max-w-md rounded-3xl border border-red-200 bg-white p-8 text-center shadow-lg">
                    <div className="mx-auto grid size-12 place-items-center rounded-2xl bg-red-100 text-red-600">
                        <AlertCircle size={24} />
                    </div>
                    <h2 className="mt-4 text-xl font-bold text-slate-900">Liên kết không hợp lệ</h2>
                    <p className="mt-2 text-sm text-slate-600">
                        Đường dẫn này thiếu mã đặt lại mật khẩu hoặc đã bị thay đổi.
                    </p>
                    <Link
                        to="/forgot-password"
                        className="mt-6 inline-block rounded-xl bg-indigo-600 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-indigo-700"
                    >
                        Yêu cầu liên kết mới
                    </Link>
                </div>
            </section>
        )
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
                        Thiết lập mật khẩu mới
                    </h1>
                    <p className="mt-2 text-sm leading-6 text-slate-600">
                        Vui lòng nhập mật khẩu mới cho tài khoản của bạn (tối thiểu 6 ký tự).
                    </p>
                </div>

                <form className="mt-8 space-y-5" onSubmit={handleSubmit} noValidate>
                    <div>
                        <label
                            className="mb-2 block text-sm font-medium text-slate-700"
                            htmlFor="reset-new-password"
                        >
                            Mật khẩu mới
                        </label>

                        <div className="relative">
                            <LockKeyhole
                                aria-hidden="true"
                                className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400"
                                size={19}
                            />
                            <input
                                autoComplete="new-password"
                                className={`h-12 w-full rounded-xl border bg-white pl-11 pr-4 text-slate-900 outline-none transition placeholder:text-slate-400 ${
                                    passwordError
                                        ? 'border-red-300 focus:border-red-500 focus:ring-4 focus:ring-red-100'
                                        : 'border-slate-300 focus:border-indigo-500 focus:ring-4 focus:ring-indigo-100'
                                }`}
                                id="reset-new-password"
                                name="newPassword"
                                placeholder="Tối thiểu 6 ký tự"
                                type="password"
                                value={newPassword}
                                onChange={(e) => {
                                    setNewPassword(e.target.value)
                                    if (passwordError) setPasswordError(null)
                                }}
                            />
                        </div>
                        {passwordError && (
                            <p className="mt-1.5 text-xs text-red-600">{passwordError}</p>
                        )}
                    </div>

                    <div>
                        <label
                            className="mb-2 block text-sm font-medium text-slate-700"
                            htmlFor="reset-confirm-password"
                        >
                            Xác nhận mật khẩu mới
                        </label>

                        <div className="relative">
                            <LockKeyhole
                                aria-hidden="true"
                                className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400"
                                size={19}
                            />
                            <input
                                autoComplete="new-password"
                                className={`h-12 w-full rounded-xl border bg-white pl-11 pr-4 text-slate-900 outline-none transition placeholder:text-slate-400 ${
                                    confirmError
                                        ? 'border-red-300 focus:border-red-500 focus:ring-4 focus:ring-red-100'
                                        : 'border-slate-300 focus:border-indigo-500 focus:ring-4 focus:ring-indigo-100'
                                }`}
                                id="reset-confirm-password"
                                name="confirmPassword"
                                placeholder="Nhập lại mật khẩu mới"
                                type="password"
                                value={confirmPassword}
                                onChange={(e) => {
                                    setConfirmPassword(e.target.value)
                                    if (confirmError) setConfirmError(null)
                                }}
                            />
                        </div>
                        {confirmError && (
                            <p className="mt-1.5 text-xs text-red-600">{confirmError}</p>
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
                        disabled={resetMutation.isPending}
                        type="submit"
                    >
                        {resetMutation.isPending ? (
                            <LoaderCircle aria-hidden="true" className="animate-spin" size={18} />
                        ) : (
                            <ShieldCheck aria-hidden="true" size={18} />
                        )}
                        {resetMutation.isPending ? 'Đang cập nhật mật khẩu...' : 'Lưu mật khẩu mới'}
                    </button>
                </form>
            </div>
        </section>
    )
}
