import { useState, type FormEvent } from 'react'
import {
    AlertCircle,
    CheckCircle2,
    KeyRound,
    LoaderCircle,
    LockKeyhole,
    ShieldCheck,
    ArrowLeft,
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
                'Đặt lại mật khẩu thành công! Đang chuyển hướng đến trang đăng nhập...'
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
            <section className="px-4 py-12 sm:px-6 lg:px-8">
                <div className="bento-card mx-auto max-w-md p-6 sm:p-8 text-center">
                    {/* Modern Bento Header */}
                    <div className="mb-6 flex items-center justify-between border-b border-slate-100 pb-3 text-left">
                        <div className="flex items-center gap-2">
                            <div className="size-2.5 rounded-full bg-rose-400" />
                            <div className="size-2.5 rounded-full bg-amber-400" />
                            <div className="size-2.5 rounded-full bg-emerald-400" />
                            <span className="text-xs font-bold text-slate-700 ml-1">Lỗi liên kết</span>
                        </div>
                        <span className="bento-badge border-rose-200 bg-rose-50 text-rose-700 text-[10px]">
                            INVALID_TOKEN
                        </span>
                    </div>

                    <div className="mx-auto grid size-12 place-items-center rounded-xl border border-rose-200 bg-rose-50 text-rose-600 shadow-sm">
                        <AlertCircle size={24} />
                    </div>
                    <h2 className="mt-4 text-xl font-bold tracking-tight text-slate-900">
                        Liên kết không hợp lệ
                    </h2>
                    <p className="mt-2 text-xs font-medium text-slate-600">
                        Đường dẫn này thiếu mã đặt lại mật khẩu hoặc đã hết hạn.
                    </p>
                    <Link
                        to="/forgot-password"
                        className="btn-bento-primary inline-flex mt-6 px-5 py-2.5 text-xs font-bold"
                    >
                        Yêu cầu liên kết mới
                    </Link>
                </div>
            </section>
        )
    }

    return (
        <section className="px-4 py-12 sm:px-6 lg:px-8">
            <div className="bento-card mx-auto max-w-md p-6 sm:p-8">
                {/* Modern Bento Header */}
                <div className="mb-6 flex items-center justify-between border-b border-slate-100 pb-3">
                    <div className="flex items-center gap-2">
                        <div className="size-2.5 rounded-full bg-rose-400" />
                        <div className="size-2.5 rounded-full bg-amber-400" />
                        <div className="size-2.5 rounded-full bg-emerald-400" />
                        <span className="text-xs font-bold text-slate-700 ml-1">Đặt lại mật khẩu</span>
                    </div>
                    <span className="bento-badge border-emerald-200 bg-emerald-50 text-emerald-700 text-[10px]">
                        TOKEN VERIFIED
                    </span>
                </div>

                <div className="text-center">
                    <div className="mx-auto inline-flex p-2.5 border border-slate-200 bg-amber-300 text-slate-950 shadow-xs mb-3">
                        <KeyRound size={22} />
                    </div>
                    <h1 className="text-xl sm:text-2xl font-black uppercase tracking-tight text-slate-950 font-mono">
                        Thiết lập mật khẩu mới
                    </h1>
                    <p className="mt-2 text-xs font-semibold leading-relaxed text-slate-600">
                        Nhập mật khẩu mới cho tài khoản TalentBridge của bạn (tối thiểu 6 ký tự).
                    </p>
                </div>

                <form className="mt-6 space-y-4" onSubmit={handleSubmit} noValidate>
                    <div>
                        <label
                            className="mb-1.5 block text-xs font-bold uppercase tracking-wider text-slate-800 font-mono"
                            htmlFor="reset-new-password"
                        >
                            Mật khẩu mới
                        </label>

                        <div className="relative">
                            <LockKeyhole
                                aria-hidden="true"
                                className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-500"
                                size={17}
                            />
                            <input
                                autoComplete="new-password"
                                className="h-11 w-full rounded-none border border-slate-200 bg-slate-50 pl-10 pr-3 font-mono text-xs font-semibold text-slate-900 placeholder:text-slate-400 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500 shadow-xs"
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
                            <p className="mt-1 text-[11px] font-bold text-rose-600">{passwordError}</p>
                        )}
                    </div>

                    <div>
                        <label
                            className="mb-1.5 block text-xs font-bold uppercase tracking-wider text-slate-800 font-mono"
                            htmlFor="reset-confirm-password"
                        >
                            Xác nhận mật khẩu mới
                        </label>

                        <div className="relative">
                            <LockKeyhole
                                aria-hidden="true"
                                className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-500"
                                size={17}
                            />
                            <input
                                autoComplete="new-password"
                                className="h-11 w-full rounded-none border border-slate-200 bg-slate-50 pl-10 pr-3 font-mono text-xs font-semibold text-slate-900 placeholder:text-slate-400 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500 shadow-xs"
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
                            <p className="mt-1 text-[11px] font-bold text-rose-600">{confirmError}</p>
                        )}
                    </div>

                    {serverError && (
                        <div
                            className="flex gap-2.5 rounded-lg border-2 border-rose-950 bg-rose-100 p-3 text-xs font-bold text-rose-950 shadow-xs"
                            role="alert"
                        >
                            <AlertCircle aria-hidden="true" className="mt-0.5 shrink-0 text-rose-700" size={16} />
                            <span>{serverError}</span>
                        </div>
                    )}

                    {successMessage && (
                        <div
                            className="flex gap-2.5 rounded-lg border-2 border-emerald-950 bg-emerald-100 p-3 text-xs font-bold text-emerald-950 shadow-xs"
                            role="status"
                        >
                            <CheckCircle2 aria-hidden="true" className="mt-0.5 shrink-0 text-emerald-700" size={16} />
                            <span>{successMessage}</span>
                        </div>
                    )}

                    <button
                        className="btn-bento-primary w-full h-12 text-sm mt-2 font-bold"
                        disabled={resetMutation.isPending}
                        type="submit"
                    >
                        {resetMutation.isPending ? (
                            <LoaderCircle aria-hidden="true" className="animate-spin" size={18} />
                        ) : (
                            <ShieldCheck aria-hidden="true" size={18} />
                        )}
                        <span>{resetMutation.isPending ? 'Đang cập nhật mật khẩu...' : 'Lưu mật khẩu mới'}</span>
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
