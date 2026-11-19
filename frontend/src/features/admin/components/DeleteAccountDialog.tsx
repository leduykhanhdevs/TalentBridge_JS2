import { ShieldAlert, Trash2, X } from 'lucide-react'

export interface DeletableAdminAccount {
    fullName: string
    email: string
    phone?: string
}

interface DeleteAccountDialogProps {
    account: DeletableAdminAccount
    accountType: 'ứng viên' | 'nhà tuyển dụng'
    isPending: boolean
    errorMessage?: string | null
    onCancel: () => void
    onDelete: (blockIdentifiers: boolean) => void
}

export function DeleteAccountDialog({
    account,
    accountType,
    isPending,
    errorMessage,
    onCancel,
    onDelete,
}: DeleteAccountDialogProps) {
    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/60 p-4 backdrop-blur-sm" role="presentation">
            <section
                aria-labelledby="delete-account-title"
                aria-modal="true"
                className="w-full max-w-lg rounded-3xl border border-slate-200 bg-white p-6 shadow-2xl sm:p-7"
                role="dialog"
            >
                <div className="flex items-start gap-3">
                    <div className="grid size-12 shrink-0 place-items-center rounded-2xl bg-rose-50 text-rose-600">
                        <ShieldAlert aria-hidden="true" size={23} />
                    </div>
                    <div className="min-w-0 flex-1">
                        <h2 className="text-lg font-black text-slate-900" id="delete-account-title">
                            Bạn có chắc muốn xóa tài khoản này?
                        </h2>
                        <p className="mt-1 break-words text-sm font-semibold text-slate-700">
                            {account.fullName} · {account.email}
                        </p>
                        {account.phone && <p className="mt-0.5 text-xs text-slate-500">Số điện thoại: {account.phone}</p>}
                    </div>
                    <button
                        aria-label="Đóng hộp thoại"
                        className="rounded-lg p-1.5 text-slate-400 transition hover:bg-slate-100 hover:text-slate-700 disabled:opacity-50"
                        disabled={isPending}
                        onClick={onCancel}
                        type="button"
                    >
                        <X size={18} />
                    </button>
                </div>

                <p className="mt-4 rounded-xl border border-amber-200 bg-amber-50 p-3.5 text-xs leading-relaxed text-amber-900">
                    {accountType === 'ứng viên'
                        ? 'Tài khoản, hồ sơ ứng viên, CV và các đơn ứng tuyển của ứng viên sẽ bị xóa.'
                        : 'Tài khoản và hồ sơ nhà tuyển dụng sẽ bị xóa. Hồ sơ doanh nghiệp và tin tuyển dụng của công ty vẫn được giữ lại.'}
                    {' '}Thao tác không thể hoàn tác. Nếu chọn chặn, hệ thống chỉ giữ mã băm email/số điện thoại để ngăn đăng ký lại; thông tin liên hệ gốc không được lưu trong danh sách chặn.
                </p>

                {errorMessage && (
                    <p className="mt-3 rounded-xl border border-rose-200 bg-rose-50 p-3 text-sm text-rose-700" role="alert">
                        {errorMessage}
                    </p>
                )}

                <div className="mt-6 flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
                    <button
                        className="rounded-xl border border-slate-200 px-4 py-2.5 text-sm font-bold text-slate-700 transition hover:bg-slate-50 disabled:opacity-50"
                        disabled={isPending}
                        onClick={onCancel}
                        type="button"
                    >
                        Hủy
                    </button>
                    <button
                        className="inline-flex items-center justify-center gap-2 rounded-xl border border-rose-200 bg-white px-4 py-2.5 text-sm font-bold text-rose-700 transition hover:bg-rose-50 disabled:cursor-not-allowed disabled:opacity-50"
                        disabled={isPending}
                        onClick={() => onDelete(false)}
                        type="button"
                    >
                        <Trash2 size={15} />
                        {isPending ? 'Đang xóa...' : 'Xóa'}
                    </button>
                    <button
                        className="inline-flex items-center justify-center gap-2 rounded-xl bg-rose-600 px-4 py-2.5 text-sm font-bold text-white shadow-sm transition hover:bg-rose-700 disabled:cursor-not-allowed disabled:opacity-50"
                        disabled={isPending}
                        onClick={() => onDelete(true)}
                        type="button"
                    >
                        <ShieldAlert size={15} />
                        {isPending ? 'Đang xóa và chặn...' : 'Xóa và chặn'}
                    </button>
                </div>
            </section>
        </div>
    )
}
