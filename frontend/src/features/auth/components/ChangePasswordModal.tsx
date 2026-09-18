import { useState } from 'react'
import { Eye, EyeOff, KeyRound, Lock, X } from 'lucide-react'
import { changePassword, AuthApiError } from '../authApi'

type ChangePasswordModalProps = {
    isOpen: boolean
    onClose: () => void
}

export function ChangePasswordModal({ isOpen, onClose }: ChangePasswordModalProps) {
    const [currentPassword, setCurrentPassword] = useState('')
    const [newPassword, setNewPassword] = useState('')
    const [confirmPassword, setConfirmPassword] = useState('')

    const [showCurrentPassword, setShowCurrentPassword] = useState(false)
    const [showNewPassword, setShowNewPassword] = useState(false)
    const [showConfirmPassword, setShowConfirmPassword] = useState(false)

    const [loading, setLoading] = useState(false)
    const [errorMessage, setErrorMessage] = useState<string | null>(null)
    const [successMessage, setSuccessMessage] = useState<string | null>(null)

    if (!isOpen) return null

    function handleClose() {
        if (loading) return
        setCurrentPassword('')
        setNewPassword('')
        setConfirmPassword('')
        setErrorMessage(null)
        setSuccessMessage(null)
        onClose()
    }

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault()
        setErrorMessage(null)
        setSuccessMessage(null)

        if (!currentPassword.trim()) {
            setErrorMessage('Vui lòng nhập mật khẩu hiện tại.')
            return
        }

        if (newPassword.length < 6) {
            setErrorMessage('Mật khẩu mới phải có ít nhất 6 ký tự.')
            return
        }

        if (newPassword === currentPassword) {
            setErrorMessage('Mật khẩu mới không được trùng với mật khẩu hiện tại.')
            return
        }

        if (newPassword !== confirmPassword) {
            setErrorMessage('Mật khẩu xác nhận không khớp.')
            return
        }

        setLoading(true)
        try {
            await changePassword({
                currentPassword,
                newPassword,
                confirmPassword,
            })
            setSuccessMessage('Đổi mật khẩu thành công!')
            setCurrentPassword('')
            setNewPassword('')
            setConfirmPassword('')
            setTimeout(() => {
                handleClose()
            }, 1500)
        } catch (error) {
            if (error instanceof AuthApiError) {
                setErrorMessage(error.message)
            } else {
                setErrorMessage('Đã xảy ra lỗi khi đổi mật khẩu. Vui lòng thử lại.')
            }
        } finally {
            setLoading(false)
        }
    }

    return (
        <div
            aria-modal="true"
            className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4 backdrop-blur-sm"
            role="dialog"
        >
            <div className="relative w-full max-w-md rounded-2xl bg-white p-6 shadow-2xl transition-all">
                <div className="flex items-center justify-between border-b border-slate-100 pb-4">
                    <div className="flex items-center gap-2 text-slate-800">
                        <div className="grid size-9 place-items-center rounded-xl bg-indigo-50 text-indigo-600">
                            <KeyRound size={20} />
                        </div>
                        <h2 className="text-lg font-bold">Đổi mật khẩu</h2>
                    </div>
                    <button
                        className="rounded-lg p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
                        disabled={loading}
                        onClick={handleClose}
                        type="button"
                    >
                        <X size={20} />
                    </button>
                </div>

                {errorMessage && (
                    <div className="mt-4 rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-600">
                        {errorMessage}
                    </div>
                )}

                {successMessage && (
                    <div className="mt-4 rounded-xl border border-emerald-200 bg-emerald-50 p-3 text-sm text-emerald-700">
                        {successMessage}
                    </div>
                )}

                <form className="mt-4 space-y-4" onSubmit={handleSubmit}>
                    <div>
                        <label className="block text-sm font-medium text-slate-700" htmlFor="current-password">
                            Mật khẩu hiện tại <span className="text-red-500">*</span>
                        </label>
                        <div className="relative mt-1">
                            <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400">
                                <Lock size={18} />
                            </div>
                            <input
                                autoComplete="current-password"
                                className="block w-full rounded-xl border border-slate-200 py-2.5 pl-10 pr-10 text-sm placeholder:text-slate-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-100"
                                disabled={loading}
                                id="current-password"
                                onChange={(e) => setCurrentPassword(e.target.value)}
                                placeholder="Nhập mật khẩu hiện tại"
                                required
                                type={showCurrentPassword ? 'text' : 'password'}
                                value={currentPassword}
                            />
                            <button
                                className="absolute inset-y-0 right-0 flex items-center pr-3 text-slate-400 hover:text-slate-600"
                                onClick={() => setShowCurrentPassword((prev) => !prev)}
                                tabIndex={-1}
                                type="button"
                            >
                                {showCurrentPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                            </button>
                        </div>
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-slate-700" htmlFor="new-password">
                            Mật khẩu mới <span className="text-red-500">*</span>
                        </label>
                        <div className="relative mt-1">
                            <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400">
                                <Lock size={18} />
                            </div>
                            <input
                                autoComplete="new-password"
                                className="block w-full rounded-xl border border-slate-200 py-2.5 pl-10 pr-10 text-sm placeholder:text-slate-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-100"
                                disabled={loading}
                                id="new-password"
                                onChange={(e) => setNewPassword(e.target.value)}
                                placeholder="Ít nhất 6 ký tự"
                                required
                                type={showNewPassword ? 'text' : 'password'}
                                value={newPassword}
                            />
                            <button
                                className="absolute inset-y-0 right-0 flex items-center pr-3 text-slate-400 hover:text-slate-600"
                                onClick={() => setShowNewPassword((prev) => !prev)}
                                tabIndex={-1}
                                type="button"
                            >
                                {showNewPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                            </button>
                        </div>
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-slate-700" htmlFor="confirm-password">
                            Xác nhận mật khẩu mới <span className="text-red-500">*</span>
                        </label>
                        <div className="relative mt-1">
                            <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400">
                                <Lock size={18} />
                            </div>
                            <input
                                autoComplete="new-password"
                                className="block w-full rounded-xl border border-slate-200 py-2.5 pl-10 pr-10 text-sm placeholder:text-slate-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-100"
                                disabled={loading}
                                id="confirm-password"
                                onChange={(e) => setConfirmPassword(e.target.value)}
                                placeholder="Nhập lại mật khẩu mới"
                                required
                                type={showConfirmPassword ? 'text' : 'password'}
                                value={confirmPassword}
                            />
                            <button
                                className="absolute inset-y-0 right-0 flex items-center pr-3 text-slate-400 hover:text-slate-600"
                                onClick={() => setShowConfirmPassword((prev) => !prev)}
                                tabIndex={-1}
                                type="button"
                            >
                                {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                            </button>
                        </div>
                    </div>

                    <div className="mt-6 flex justify-end gap-3 pt-2">
                        <button
                            className="rounded-xl border border-slate-200 px-4 py-2.5 text-sm font-medium text-slate-600 transition hover:bg-slate-50"
                            disabled={loading}
                            onClick={handleClose}
                            type="button"
                        >
                            Hủy
                        </button>
                        <button
                            className="inline-flex items-center gap-2 rounded-xl bg-indigo-600 px-5 py-2.5 text-sm font-medium text-white transition hover:bg-indigo-700 disabled:opacity-50"
                            disabled={loading}
                            type="submit"
                        >
                            {loading ? (
                                <>
                                    <span className="inline-block size-4 animate-spin rounded-full border-2 border-white border-t-transparent" />
                                    <span>Đang cập nhật...</span>
                                </>
                            ) : (
                                <span>Lưu thay đổi</span>
                            )}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    )
}
