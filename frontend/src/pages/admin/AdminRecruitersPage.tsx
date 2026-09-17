import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
    AlertCircle,
    Building2,
    CheckCircle,
    Lock,
    Mail,
    Phone,
    RefreshCw,
    Search,
    Unlock,
    UserCheck,
    X,
} from 'lucide-react'
import {
    getAdminRecruiters,
    getRecruiterById,
    updateUserStatus,
} from '../../features/admin/adminApi'
import type {
    RecruiterAdminResponse,
    UserStatus,
} from '../../features/admin/adminTypes'

export function AdminRecruitersPage() {
    const queryClient = useQueryClient()

    // Filter states
    const [page, setPage] = useState(1)
    const [keywordInput, setKeywordInput] = useState('')
    const [activeKeyword, setActiveKeyword] = useState('')
    const [successMessage, setSuccessMessage] = useState<string | null>(null)
    const [actionError, setActionError] = useState<string | null>(null)

    // Query recruiter list
    const {
        data: recruitersPage,
        isLoading,
        isFetching,
        error: queryError,
        refetch,
    } = useQuery({
        queryKey: ['admin-recruiters', page, activeKeyword],
        queryFn: () =>
            getAdminRecruiters({
                page,
                size: 10,
                keyword: activeKeyword || undefined,
            }),
    })

    // Detail modal state
    const [selectedRecruiter, setSelectedRecruiter] = useState<RecruiterAdminResponse | null>(null)

    // Action dialog state (Lock / Unlock)
    const [actionRecruiter, setActionRecruiter] = useState<RecruiterAdminResponse | null>(null)
    const [actionType, setActionType] = useState<'LOCK' | 'UNLOCK'>('LOCK')
    const [actionReason, setActionReason] = useState('')

    // Mutation to lock/unlock recruiter
    const statusMutation = useMutation({
        mutationFn: async ({
            userId,
            status,
            reason,
        }: {
            userId: number
            status: UserStatus
            reason?: string
        }) => {
            await updateUserStatus(userId, { status, reason })
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-recruiters'] })
            setSuccessMessage(
                actionType === 'LOCK'
                    ? `Đã khóa tài khoản HR ${actionRecruiter?.fullName}.`
                    : `Đã mở khóa tài khoản HR ${actionRecruiter?.fullName}.`,
            )
            setActionRecruiter(null)
            setActionReason('')
            setTimeout(() => setSuccessMessage(null), 4000)
        },
        onError: (err: unknown) => {
            const msg = err instanceof Error ? err.message : 'Không thể cập nhật trạng thái'
            setActionError(msg)
        },
    })

    function handleSearchSubmit(e: React.FormEvent) {
        e.preventDefault()
        setPage(1)
        setActiveKeyword(keywordInput)
    }

    async function handleViewDetail(recruiterId: number) {
        try {
            const detail = await getRecruiterById(recruiterId)
            setSelectedRecruiter(detail)
        } catch (err: unknown) {
            const msg = err instanceof Error ? err.message : 'Không thể tải chi tiết nhà tuyển dụng'
            setActionError(msg)
        }
    }

    function handleOpenActionDialog(recruiter: RecruiterAdminResponse, type: 'LOCK' | 'UNLOCK') {
        setActionRecruiter(recruiter)
        setActionType(type)
        setActionReason('')
        setActionError(null)
    }

    function handleConfirmAction() {
        if (!actionRecruiter) return
        const newStatus: UserStatus = actionType === 'LOCK' ? 'BANNED' : 'ACTIVE'
        statusMutation.mutate({
            userId: actionRecruiter.userId,
            status: newStatus,
            reason: actionReason.trim() || undefined,
        })
    }

    const errorMessage = actionError || (queryError instanceof Error ? queryError.message : null)

    return (
        <div className="space-y-6">
            {/* Page Header */}
            <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-slate-900">
                        Quản lý nhà tuyển dụng (HR)
                    </h1>
                    <p className="text-sm text-slate-600">
                        Danh sách chuyên viên nhân sự, quản lý tài khoản và theo dõi doanh nghiệp trực thuộc.
                    </p>
                </div>
                <button
                    className="inline-flex items-center gap-2 rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-slate-700 shadow-sm transition hover:bg-slate-50"
                    disabled={isFetching}
                    onClick={() => refetch()}
                    type="button"
                >
                    <RefreshCw className={isFetching ? 'animate-spin' : ''} size={15} />
                    <span>Làm mới</span>
                </button>
            </div>

            {/* Notification messages */}
            {successMessage && (
                <div className="flex items-center gap-2 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800">
                    <CheckCircle className="shrink-0" size={18} />
                    <span>{successMessage}</span>
                </div>
            )}
            {errorMessage && (
                <div className="flex items-center gap-2 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">
                    <AlertCircle className="shrink-0" size={18} />
                    <span>{errorMessage}</span>
                </div>
            )}

            {/* Search Bar */}
            <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
                <form className="flex flex-col gap-3 sm:flex-row sm:items-center" onSubmit={handleSearchSubmit}>
                    <div className="relative flex-1">
                        <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                        <input
                            className="w-full rounded-lg border border-slate-300 py-2 pl-10 pr-4 text-sm text-slate-900 placeholder:text-slate-400 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
                            onChange={(e) => setKeywordInput(e.target.value)}
                            placeholder="Tìm kiếm theo tên nhà tuyển dụng, email, hoặc tên công ty..."
                            type="text"
                            value={keywordInput}
                        />
                    </div>
                    <button
                        className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-indigo-700"
                        type="submit"
                    >
                        Tìm kiếm
                    </button>
                </form>
            </div>

            {/* Recruiter Table */}
            <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
                {isLoading ? (
                    <div className="flex h-64 items-center justify-center">
                        <div className="flex items-center gap-3 text-slate-500">
                            <RefreshCw className="animate-spin" size={20} />
                            <span>Đang tải danh sách nhà tuyển dụng...</span>
                        </div>
                    </div>
                ) : recruitersPage?.content.length === 0 ? (
                    <div className="flex h-64 flex-col items-center justify-center p-6 text-center">
                        <div className="grid size-12 place-items-center rounded-full bg-slate-100 text-slate-400 mb-3">
                            <UserCheck size={24} />
                        </div>
                        <h3 className="font-semibold text-slate-800">Không tìm thấy nhà tuyển dụng nào</h3>
                        <p className="mt-1 text-sm text-slate-500">
                            Thử điều chỉnh từ khóa tìm kiếm để mở rộng kết quả.
                        </p>
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-left text-sm text-slate-600">
                            <thead className="border-b border-slate-200 bg-slate-50/80 text-xs uppercase font-semibold text-slate-700">
                                <tr>
                                    <th className="px-4 py-3.5" scope="col">Nhà tuyển dụng</th>
                                    <th className="px-4 py-3.5" scope="col">Liên hệ</th>
                                    <th className="px-4 py-3.5" scope="col">Doanh nghiệp trực thuộc</th>
                                    <th className="px-4 py-3.5" scope="col">Trạng thái tài khoản</th>
                                    <th className="px-4 py-3.5 text-right" scope="col">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-200">
                                {recruitersPage?.content.map((recruiter) => (
                                    <tr className="hover:bg-slate-50/70 transition" key={recruiter.id}>
                                        <td className="px-4 py-3.5">
                                            <div className="flex items-center gap-3">
                                                {recruiter.avatarUrl ? (
                                                    <img
                                                        alt={recruiter.fullName}
                                                        className="size-10 rounded-full object-cover border border-slate-200"
                                                        src={recruiter.avatarUrl}
                                                    />
                                                ) : (
                                                    <div className="grid size-10 place-items-center rounded-full bg-indigo-100 text-indigo-700 font-semibold text-sm">
                                                        {recruiter.fullName.charAt(0).toUpperCase()}
                                                    </div>
                                                )}
                                                <div>
                                                    <div className="font-semibold text-slate-900">
                                                        {recruiter.fullName}
                                                    </div>
                                                    <div className="text-xs text-slate-500">
                                                        {recruiter.position || 'Chuyên viên nhân sự'}
                                                    </div>
                                                </div>
                                            </div>
                                        </td>
                                        <td className="px-4 py-3.5">
                                            <div className="flex flex-col gap-0.5 text-xs">
                                                <div className="flex items-center gap-1.5 text-slate-700">
                                                    <Mail size={13} className="text-slate-400 shrink-0" />
                                                    <span>{recruiter.email}</span>
                                                </div>
                                                {recruiter.phone && (
                                                    <div className="flex items-center gap-1.5 text-slate-500">
                                                        <Phone size={13} className="text-slate-400 shrink-0" />
                                                        <span>{recruiter.phone}</span>
                                                    </div>
                                                )}
                                            </div>
                                        </td>
                                        <td className="px-4 py-3.5">
                                            {recruiter.companyName ? (
                                                <div className="flex items-center gap-2">
                                                    <div className="grid size-8 place-items-center rounded bg-slate-100 text-slate-600 border border-slate-200">
                                                        {recruiter.companyLogoUrl ? (
                                                            <img
                                                                alt={recruiter.companyName}
                                                                className="size-8 rounded object-cover"
                                                                src={recruiter.companyLogoUrl}
                                                            />
                                                        ) : (
                                                            <Building2 size={16} />
                                                        )}
                                                    </div>
                                                    <div>
                                                        <div className="font-medium text-slate-900 text-xs">
                                                            {recruiter.companyName}
                                                        </div>
                                                        {recruiter.companyStatus === 'APPROVED' && (
                                                            <span className="inline-block text-[10px] font-medium text-emerald-600">
                                                                Đã xác thực
                                                            </span>
                                                        )}
                                                        {recruiter.companyStatus === 'PENDING' && (
                                                            <span className="inline-block text-[10px] font-medium text-amber-600">
                                                                Chờ duyệt cty
                                                            </span>
                                                        )}
                                                        {recruiter.companyStatus === 'REJECTED' && (
                                                            <span className="inline-block text-[10px] font-medium text-rose-600">
                                                                Bị từ chối cty
                                                            </span>
                                                        )}
                                                    </div>
                                                </div>
                                            ) : (
                                                <span className="text-xs text-slate-400 italic">
                                                    Chưa liên kết công ty
                                                </span>
                                            )}
                                        </td>
                                        <td className="px-4 py-3.5">
                                            {recruiter.status === 'ACTIVE' ? (
                                                <span className="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-semibold text-emerald-700 border border-emerald-200">
                                                    <span className="size-1.5 rounded-full bg-emerald-500" />
                                                    Hoạt động
                                                </span>
                                            ) : (
                                                <span className="inline-flex items-center gap-1 rounded-full bg-rose-50 px-2.5 py-1 text-xs font-semibold text-rose-700 border border-rose-200">
                                                    <span className="size-1.5 rounded-full bg-rose-500" />
                                                    Bị khóa
                                                </span>
                                            )}
                                        </td>
                                        <td className="px-4 py-3.5 text-right">
                                            <div className="inline-flex items-center gap-1">
                                                <button
                                                    className="rounded-lg border border-slate-200 bg-white px-2.5 py-1.5 text-xs font-medium text-slate-700 shadow-sm transition hover:bg-slate-50"
                                                    onClick={() => handleViewDetail(recruiter.id)}
                                                    type="button"
                                                >
                                                    Chi tiết
                                                </button>

                                                {recruiter.status === 'ACTIVE' ? (
                                                    <button
                                                        className="inline-flex items-center gap-1 rounded-lg border border-rose-200 bg-rose-50 px-2.5 py-1.5 text-xs font-semibold text-rose-700 transition hover:bg-rose-100"
                                                        onClick={() => handleOpenActionDialog(recruiter, 'LOCK')}
                                                        title="Khóa tài khoản HR"
                                                        type="button"
                                                    >
                                                        <Lock size={13} />
                                                        <span>Khóa</span>
                                                    </button>
                                                ) : (
                                                    <button
                                                        className="inline-flex items-center gap-1 rounded-lg border border-emerald-200 bg-emerald-50 px-2.5 py-1.5 text-xs font-semibold text-emerald-700 transition hover:bg-emerald-100"
                                                        onClick={() => handleOpenActionDialog(recruiter, 'UNLOCK')}
                                                        title="Mở khóa tài khoản HR"
                                                        type="button"
                                                    >
                                                        <Unlock size={13} />
                                                        <span>Mở</span>
                                                    </button>
                                                )}
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}

                {/* Pagination */}
                {recruitersPage && recruitersPage.totalPages > 0 && (
                    <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between border-t border-slate-200 px-4 py-3 text-xs text-slate-600 gap-2">
                        <div>
                            Hiển thị trang <span className="font-semibold text-slate-900">{recruitersPage.pageNumber}</span> /{' '}
                            <span className="font-semibold text-slate-900">{recruitersPage.totalPages}</span> ({recruitersPage.totalElements} nhà tuyển dụng)
                        </div>
                        <div className="flex items-center gap-1">
                            <button
                                className="rounded border border-slate-200 bg-white px-2.5 py-1 font-medium text-slate-700 disabled:opacity-40 hover:bg-slate-50"
                                disabled={recruitersPage.pageNumber <= 1}
                                onClick={() => setPage((p) => Math.max(1, p - 1))}
                                type="button"
                            >
                                Trước
                            </button>
                            <button
                                className="rounded border border-slate-200 bg-white px-2.5 py-1 font-medium text-slate-700 disabled:opacity-40 hover:bg-slate-50"
                                disabled={recruitersPage.isLast || recruitersPage.pageNumber >= recruitersPage.totalPages}
                                onClick={() => setPage((p) => p + 1)}
                                type="button"
                            >
                                Sau
                            </button>
                        </div>
                    </div>
                )}
            </div>

            {/* Recruiter Detail Modal */}
            {selectedRecruiter && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-xs p-4">
                    <div className="relative w-full max-w-lg rounded-2xl bg-white p-6 shadow-xl max-h-[90vh] overflow-y-auto">
                        <button
                            className="absolute right-4 top-4 rounded-lg p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
                            onClick={() => setSelectedRecruiter(null)}
                            type="button"
                        >
                            <X size={20} />
                        </button>

                        <div className="flex items-start gap-4">
                            {selectedRecruiter.avatarUrl ? (
                                <img
                                    alt={selectedRecruiter.fullName}
                                    className="size-16 rounded-xl object-cover border border-slate-200"
                                    src={selectedRecruiter.avatarUrl}
                                />
                            ) : (
                                <div className="grid size-16 place-items-center rounded-xl bg-indigo-100 text-indigo-700 font-bold text-xl">
                                    {selectedRecruiter.fullName.charAt(0).toUpperCase()}
                                </div>
                            )}
                            <div>
                                <h3 className="text-xl font-bold text-slate-900">{selectedRecruiter.fullName}</h3>
                                <p className="text-sm font-medium text-indigo-600">
                                    {selectedRecruiter.position || 'Chuyên viên nhân sự'}
                                </p>
                                <div className="mt-1 flex items-center gap-2">
                                    {selectedRecruiter.status === 'ACTIVE' ? (
                                        <span className="inline-flex items-center rounded-full bg-emerald-50 px-2 py-0.5 text-xs font-semibold text-emerald-700 border border-emerald-200">
                                            Tài khoản hoạt động
                                        </span>
                                    ) : (
                                        <span className="inline-flex items-center rounded-full bg-rose-50 px-2 py-0.5 text-xs font-semibold text-rose-700 border border-rose-200">
                                            Tài khoản bị khóa
                                        </span>
                                    )}
                                </div>
                            </div>
                        </div>

                        <div className="mt-6 space-y-3 border-t border-slate-100 pt-4 text-xs">
                            <div className="flex items-center justify-between py-1 border-b border-slate-50">
                                <span className="font-semibold text-slate-500">Mã HR ID:</span>
                                <span className="font-mono text-slate-900">#{selectedRecruiter.id}</span>
                            </div>
                            <div className="flex items-center justify-between py-1 border-b border-slate-50">
                                <span className="font-semibold text-slate-500">Mã User ID:</span>
                                <span className="font-mono text-slate-900">#{selectedRecruiter.userId}</span>
                            </div>
                            <div className="flex items-center justify-between py-1 border-b border-slate-50">
                                <span className="font-semibold text-slate-500">Email:</span>
                                <span className="text-slate-900 font-medium">{selectedRecruiter.email}</span>
                            </div>
                            <div className="flex items-center justify-between py-1 border-b border-slate-50">
                                <span className="font-semibold text-slate-500">Số điện thoại:</span>
                                <span className="text-slate-900">{selectedRecruiter.phone || 'Chưa cập nhật'}</span>
                            </div>
                            <div className="flex items-center justify-between py-1 border-b border-slate-50">
                                <span className="font-semibold text-slate-500">Doanh nghiệp:</span>
                                <span className="font-semibold text-slate-900">
                                    {selectedRecruiter.companyName || 'Chưa liên kết'}
                                </span>
                            </div>
                            {selectedRecruiter.companyId && (
                                <div className="flex items-center justify-between py-1 border-b border-slate-50">
                                    <span className="font-semibold text-slate-500">Trạng thái duyệt công ty:</span>
                                    <span className="font-medium text-slate-900">
                                        {selectedRecruiter.companyStatus || 'N/A'}
                                    </span>
                                </div>
                            )}
                            <div className="flex items-center justify-between py-1">
                                <span className="font-semibold text-slate-500">Ngày tham gia:</span>
                                <span className="text-slate-900">
                                    {selectedRecruiter.createdAt
                                        ? new Date(selectedRecruiter.createdAt).toLocaleDateString('vi-VN')
                                        : 'N/A'}
                                </span>
                            </div>
                        </div>

                        <div className="mt-6 flex justify-end">
                            <button
                                className="rounded-lg bg-slate-100 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-200"
                                onClick={() => setSelectedRecruiter(null)}
                                type="button"
                            >
                                Đóng
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Lock / Unlock Confirmation Modal */}
            {actionRecruiter && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-xs p-4">
                    <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl">
                        <div className="flex items-center gap-3">
                            <div
                                className={`grid size-10 place-items-center rounded-xl ${
                                    actionType === 'LOCK' ? 'bg-rose-100 text-rose-600' : 'bg-emerald-100 text-emerald-600'
                                }`}
                            >
                                {actionType === 'LOCK' ? <Lock size={20} /> : <Unlock size={20} />}
                            </div>
                            <div>
                                <h3 className="font-bold text-slate-900">
                                    {actionType === 'LOCK' ? 'Khóa tài khoản HR' : 'Mở khóa tài khoản HR'}
                                </h3>
                                <p className="text-xs text-slate-500">{actionRecruiter.fullName} ({actionRecruiter.email})</p>
                            </div>
                        </div>

                        <div className="mt-4 text-xs text-slate-600">
                            {actionType === 'LOCK' ? (
                                <p>
                                    Khi bị khóa, HR sẽ không thể đăng tin tuyển dụng mới hoặc quản lý ứng viên của công ty.
                                </p>
                            ) : (
                                <p>
                                    Mở khóa tài khoản sẽ khôi phục quyền đăng tin và tuyển dụng của HR.
                                </p>
                            )}
                        </div>

                        {actionType === 'LOCK' && (
                            <div className="mt-4">
                                <label className="block text-xs font-semibold text-slate-700 mb-1" htmlFor="hr-lock-reason">
                                    Lý do khóa tài khoản:
                                </label>
                                <textarea
                                    className="w-full rounded-lg border border-slate-300 p-2 text-xs text-slate-900 focus:border-rose-500 focus:outline-none"
                                    id="hr-lock-reason"
                                    onChange={(e) => setActionReason(e.target.value)}
                                    placeholder="Nhập lý do khóa..."
                                    rows={3}
                                    value={actionReason}
                                />
                            </div>
                        )}

                        <div className="mt-6 flex justify-end gap-2">
                            <button
                                className="rounded-lg border border-slate-300 px-3 py-1.5 text-xs font-semibold text-slate-700 hover:bg-slate-50"
                                disabled={statusMutation.isPending}
                                onClick={() => setActionRecruiter(null)}
                                type="button"
                            >
                                Hủy bỏ
                            </button>
                            <button
                                className={`rounded-lg px-4 py-1.5 text-xs font-semibold text-white shadow-sm transition ${
                                    actionType === 'LOCK'
                                        ? 'bg-rose-600 hover:bg-rose-700'
                                        : 'bg-emerald-600 hover:bg-emerald-700'
                                }`}
                                disabled={statusMutation.isPending}
                                onClick={handleConfirmAction}
                                type="button"
                            >
                                {statusMutation.isPending
                                    ? 'Đang xử lý...'
                                    : actionType === 'LOCK'
                                    ? 'Xác nhận khóa'
                                    : 'Xác nhận mở khóa'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    )
}
