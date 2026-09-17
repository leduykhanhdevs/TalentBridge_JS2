import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
    AlertCircle,
    CheckCircle,
    Code2,
    ExternalLink,
    Globe,
    Lock,
    Mail,
    MapPin,
    Phone,
    RefreshCw,
    Search,
    Share2,
    Unlock,
    User,
    X,
} from 'lucide-react'
import {
    getAdminCandidates,
    getCandidateById,
    updateUserStatus,
} from '../../features/admin/adminApi'
import type {
    CandidateAdminResponse,
    UserStatus,
} from '../../features/admin/adminTypes'

export function AdminCandidatesPage() {
    const queryClient = useQueryClient()

    // Filter states
    const [page, setPage] = useState(1)
    const [keywordInput, setKeywordInput] = useState('')
    const [activeKeyword, setActiveKeyword] = useState('')
    const [statusFilter, setStatusFilter] = useState<UserStatus | ''>('')
    const [successMessage, setSuccessMessage] = useState<string | null>(null)
    const [actionError, setActionError] = useState<string | null>(null)

    // Query candidates list
    const {
        data: candidatesPage,
        isLoading,
        isFetching,
        error: queryError,
        refetch,
    } = useQuery({
        queryKey: ['admin-candidates', page, activeKeyword, statusFilter],
        queryFn: () =>
            getAdminCandidates({
                page,
                size: 10,
                keyword: activeKeyword || undefined,
                status: statusFilter || undefined,
            }),
    })

    // Detail modal state
    const [selectedCandidate, setSelectedCandidate] = useState<CandidateAdminResponse | null>(null)

    // Action dialog state (Lock / Unlock)
    const [actionCandidate, setActionCandidate] = useState<CandidateAdminResponse | null>(null)
    const [actionType, setActionType] = useState<'LOCK' | 'UNLOCK'>('LOCK')
    const [actionReason, setActionReason] = useState('')

    // Mutation to lock/unlock candidate account
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
            queryClient.invalidateQueries({ queryKey: ['admin-candidates'] })
            setSuccessMessage(
                actionType === 'LOCK'
                    ? `Đã khóa tài khoản của ứng viên ${actionCandidate?.fullName}.`
                    : `Đã mở khóa tài khoản của ứng viên ${actionCandidate?.fullName}.`,
            )
            setActionCandidate(null)
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

    async function handleViewDetail(candidateId: number) {
        try {
            const detail = await getCandidateById(candidateId)
            setSelectedCandidate(detail)
        } catch (err: unknown) {
            const msg = err instanceof Error ? err.message : 'Không thể tải chi tiết hồ sơ'
            setActionError(msg)
        }
    }

    function handleOpenActionDialog(candidate: CandidateAdminResponse, type: 'LOCK' | 'UNLOCK') {
        setActionCandidate(candidate)
        setActionType(type)
        setActionReason('')
        setActionError(null)
    }

    function handleConfirmAction() {
        if (!actionCandidate) return
        const newStatus: UserStatus = actionType === 'LOCK' ? 'BANNED' : 'ACTIVE'
        statusMutation.mutate({
            userId: actionCandidate.userId,
            status: newStatus,
            reason: actionReason.trim() || undefined,
        })
    }

    const errorMessage = actionError || (queryError instanceof Error ? queryError.message : null)

    return (
        <div className="space-y-6">
            {/* Page Title & Controls */}
            <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-slate-900">Quản lý ứng viên</h1>
                    <p className="text-sm text-slate-600">
                        Danh sách tất cả người tìm việc, tra cứu hồ sơ và kiểm soát quyền truy cập hệ thống.
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

            {/* Notification alerts */}
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

            {/* Filter & Search Bar */}
            <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
                <form className="flex flex-col gap-3 sm:flex-row sm:items-center" onSubmit={handleSearchSubmit}>
                    <div className="relative flex-1">
                        <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                        <input
                            className="w-full rounded-lg border border-slate-300 py-2 pl-10 pr-4 text-sm text-slate-900 placeholder:text-slate-400 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
                            onChange={(e) => setKeywordInput(e.target.value)}
                            placeholder="Tìm kiếm theo tên, email, vị trí hoặc thành phố..."
                            type="text"
                            value={keywordInput}
                        />
                    </div>

                    <div className="flex items-center gap-2">
                        <select
                            className="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-700 focus:border-indigo-500 focus:outline-none"
                            onChange={(e) => {
                                setStatusFilter(e.target.value as UserStatus | '')
                                setPage(1)
                            }}
                            value={statusFilter}
                        >
                            <option value="">Tất cả trạng thái</option>
                            <option value="ACTIVE">Đang hoạt động (ACTIVE)</option>
                            <option value="BANNED">Bị khóa (BANNED)</option>
                        </select>

                        <button
                            className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-indigo-700"
                            type="submit"
                        >
                            Tìm kiếm
                        </button>
                    </div>
                </form>
            </div>

            {/* Candidate List Table */}
            <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
                {isLoading ? (
                    <div className="flex h-64 items-center justify-center">
                        <div className="flex items-center gap-3 text-slate-500">
                            <RefreshCw className="animate-spin" size={20} />
                            <span>Đang tải danh sách ứng viên...</span>
                        </div>
                    </div>
                ) : candidatesPage?.content.length === 0 ? (
                    <div className="flex h-64 flex-col items-center justify-center p-6 text-center">
                        <div className="grid size-12 place-items-center rounded-full bg-slate-100 text-slate-400 mb-3">
                            <User size={24} />
                        </div>
                        <h3 className="font-semibold text-slate-800">Không tìm thấy ứng viên nào</h3>
                        <p className="mt-1 text-sm text-slate-500">
                            Thử điều chỉnh từ khóa tìm kiếm hoặc bỏ bộ lọc trạng thái.
                        </p>
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-left text-sm text-slate-600">
                            <thead className="border-b border-slate-200 bg-slate-50/80 text-xs uppercase font-semibold text-slate-700">
                                <tr>
                                    <th className="px-4 py-3.5" scope="col">Ứng viên</th>
                                    <th className="px-4 py-3.5" scope="col">Liên hệ</th>
                                    <th className="px-4 py-3.5" scope="col">Địa điểm & Kinh nghiệm</th>
                                    <th className="px-4 py-3.5" scope="col">Trạng thái</th>
                                    <th className="px-4 py-3.5 text-right" scope="col">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-200">
                                {candidatesPage?.content.map((candidate) => (
                                    <tr className="hover:bg-slate-50/70 transition" key={candidate.id}>
                                        <td className="px-4 py-3.5">
                                            <div className="flex items-center gap-3">
                                                {candidate.avatarUrl ? (
                                                    <img
                                                        alt={candidate.fullName}
                                                        className="size-10 rounded-full object-cover border border-slate-200"
                                                        src={candidate.avatarUrl}
                                                    />
                                                ) : (
                                                    <div className="grid size-10 place-items-center rounded-full bg-indigo-100 text-indigo-700 font-semibold text-sm">
                                                        {candidate.fullName.charAt(0).toUpperCase()}
                                                    </div>
                                                )}
                                                <div>
                                                    <div className="font-semibold text-slate-900">
                                                        {candidate.fullName}
                                                    </div>
                                                    <div className="text-xs text-slate-500">
                                                        {candidate.title || 'Chưa cập nhật chức danh'}
                                                    </div>
                                                </div>
                                            </div>
                                        </td>
                                        <td className="px-4 py-3.5">
                                            <div className="flex flex-col gap-0.5 text-xs">
                                                <div className="flex items-center gap-1.5 text-slate-700">
                                                    <Mail size={13} className="text-slate-400 shrink-0" />
                                                    <span>{candidate.email}</span>
                                                </div>
                                                {candidate.phone && (
                                                    <div className="flex items-center gap-1.5 text-slate-500">
                                                        <Phone size={13} className="text-slate-400 shrink-0" />
                                                        <span>{candidate.phone}</span>
                                                    </div>
                                                )}
                                            </div>
                                        </td>
                                        <td className="px-4 py-3.5">
                                            <div className="flex flex-col gap-0.5 text-xs">
                                                <div className="flex items-center gap-1 text-slate-700">
                                                    <MapPin size={13} className="text-slate-400 shrink-0" />
                                                    <span>{candidate.city || 'Chưa rõ'}</span>
                                                </div>
                                                <div className="text-slate-500">
                                                    {candidate.experienceYears !== undefined && candidate.experienceYears !== null
                                                        ? `${candidate.experienceYears} năm kinh nghiệm`
                                                        : 'Chưa cập nhật KN'}
                                                </div>
                                            </div>
                                        </td>
                                        <td className="px-4 py-3.5">
                                            {candidate.status === 'ACTIVE' ? (
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
                                                    onClick={() => handleViewDetail(candidate.id)}
                                                    type="button"
                                                >
                                                    Chi tiết
                                                </button>

                                                {candidate.status === 'ACTIVE' ? (
                                                    <button
                                                        className="inline-flex items-center gap-1 rounded-lg border border-rose-200 bg-rose-50 px-2.5 py-1.5 text-xs font-semibold text-rose-700 transition hover:bg-rose-100"
                                                        onClick={() => handleOpenActionDialog(candidate, 'LOCK')}
                                                        title="Khóa tài khoản"
                                                        type="button"
                                                    >
                                                        <Lock size={13} />
                                                        <span>Khóa</span>
                                                    </button>
                                                ) : (
                                                    <button
                                                        className="inline-flex items-center gap-1 rounded-lg border border-emerald-200 bg-emerald-50 px-2.5 py-1.5 text-xs font-semibold text-emerald-700 transition hover:bg-emerald-100"
                                                        onClick={() => handleOpenActionDialog(candidate, 'UNLOCK')}
                                                        title="Mở khóa tài khoản"
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

                {/* Pagination footer */}
                {candidatesPage && candidatesPage.totalPages > 0 && (
                    <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between border-t border-slate-200 px-4 py-3 text-xs text-slate-600 gap-2">
                        <div>
                            Hiển thị trang <span className="font-semibold text-slate-900">{candidatesPage.pageNumber}</span> /{' '}
                            <span className="font-semibold text-slate-900">{candidatesPage.totalPages}</span> ({candidatesPage.totalElements} ứng viên)
                        </div>
                        <div className="flex items-center gap-1">
                            <button
                                className="rounded border border-slate-200 bg-white px-2.5 py-1 font-medium text-slate-700 disabled:opacity-40 hover:bg-slate-50"
                                disabled={candidatesPage.pageNumber <= 1}
                                onClick={() => setPage((p) => Math.max(1, p - 1))}
                                type="button"
                            >
                                Trước
                            </button>
                            <button
                                className="rounded border border-slate-200 bg-white px-2.5 py-1 font-medium text-slate-700 disabled:opacity-40 hover:bg-slate-50"
                                disabled={candidatesPage.isLast || candidatesPage.pageNumber >= candidatesPage.totalPages}
                                onClick={() => setPage((p) => p + 1)}
                                type="button"
                            >
                                Sau
                            </button>
                        </div>
                    </div>
                )}
            </div>

            {/* Candidate Detail Modal */}
            {selectedCandidate && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-xs p-4">
                    <div className="relative w-full max-w-2xl rounded-2xl bg-white p-6 shadow-xl max-h-[90vh] overflow-y-auto">
                        <button
                            className="absolute right-4 top-4 rounded-lg p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
                            onClick={() => setSelectedCandidate(null)}
                            type="button"
                        >
                            <X size={20} />
                        </button>

                        <div className="flex items-start gap-4">
                            {selectedCandidate.avatarUrl ? (
                                <img
                                    alt={selectedCandidate.fullName}
                                    className="size-16 rounded-xl object-cover border border-slate-200"
                                    src={selectedCandidate.avatarUrl}
                                />
                            ) : (
                                <div className="grid size-16 place-items-center rounded-xl bg-indigo-100 text-indigo-700 font-bold text-xl">
                                    {selectedCandidate.fullName.charAt(0).toUpperCase()}
                                </div>
                            )}
                            <div>
                                <h3 className="text-xl font-bold text-slate-900">{selectedCandidate.fullName}</h3>
                                <p className="text-sm font-medium text-indigo-600">
                                    {selectedCandidate.title || 'Chưa thiết lập chức danh'}
                                </p>
                                <div className="mt-1 flex items-center gap-2">
                                    {selectedCandidate.status === 'ACTIVE' ? (
                                        <span className="inline-flex items-center rounded-full bg-emerald-50 px-2 py-0.5 text-xs font-semibold text-emerald-700 border border-emerald-200">
                                            Hoạt động
                                        </span>
                                    ) : (
                                        <span className="inline-flex items-center rounded-full bg-rose-50 px-2 py-0.5 text-xs font-semibold text-rose-700 border border-rose-200">
                                            Bị khóa
                                        </span>
                                    )}
                                    <span className="text-xs text-slate-500">
                                        Mã ứng viên: #{selectedCandidate.id} (User ID: #{selectedCandidate.userId})
                                    </span>
                                </div>
                            </div>
                        </div>

                        <div className="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-2 border-t border-slate-100 pt-4 text-xs">
                            <div className="space-y-2">
                                <div>
                                    <span className="font-semibold text-slate-500">Email:</span>{' '}
                                    <span className="text-slate-900 font-medium">{selectedCandidate.email}</span>
                                </div>
                                <div>
                                    <span className="font-semibold text-slate-500">Số điện thoại:</span>{' '}
                                    <span className="text-slate-900">{selectedCandidate.phone || 'Chưa cập nhật'}</span>
                                </div>
                                <div>
                                    <span className="font-semibold text-slate-500">Ngày sinh:</span>{' '}
                                    <span className="text-slate-900">{selectedCandidate.dob || 'Chưa cập nhật'}</span>
                                </div>
                                <div>
                                    <span className="font-semibold text-slate-500">Giới tính:</span>{' '}
                                    <span className="text-slate-900">{selectedCandidate.gender || 'Chưa cập nhật'}</span>
                                </div>
                            </div>

                            <div className="space-y-2">
                                <div>
                                    <span className="font-semibold text-slate-500">Địa chỉ / Tỉnh thành:</span>{' '}
                                    <span className="text-slate-900">
                                        {[selectedCandidate.address, selectedCandidate.city].filter(Boolean).join(', ') || 'Chưa cập nhật'}
                                    </span>
                                </div>
                                <div>
                                    <span className="font-semibold text-slate-500">Kinh nghiệm:</span>{' '}
                                    <span className="text-slate-900">
                                        {selectedCandidate.experienceYears !== undefined && selectedCandidate.experienceYears !== null
                                            ? `${selectedCandidate.experienceYears} năm`
                                            : 'Chưa cập nhật'}
                                    </span>
                                </div>
                                <div>
                                    <span className="font-semibold text-slate-500">Mức lương kỳ vọng:</span>{' '}
                                    <span className="text-slate-900 font-semibold text-emerald-600">
                                        {selectedCandidate.expectedSalary
                                            ? `${Number(selectedCandidate.expectedSalary).toLocaleString('vi-VN')} VNĐ`
                                            : 'Thương lượng'}
                                    </span>
                                </div>
                            </div>
                        </div>

                        {selectedCandidate.summary && (
                            <div className="mt-4 rounded-lg bg-slate-50 p-3 text-xs text-slate-700">
                                <div className="font-semibold text-slate-900 mb-1">Giới thiệu bản thân:</div>
                                <p className="leading-relaxed whitespace-pre-wrap">{selectedCandidate.summary}</p>
                            </div>
                        )}

                        <div className="mt-4 flex flex-wrap items-center gap-3 pt-2 text-xs">
                            {selectedCandidate.personalWebsite && (
                                <a
                                    className="inline-flex items-center gap-1 text-indigo-600 hover:underline"
                                    href={selectedCandidate.personalWebsite}
                                    rel="noreferrer"
                                    target="_blank"
                                >
                                    <Globe size={13} />
                                    <span>Website cá nhân</span>
                                    <ExternalLink size={11} />
                                </a>
                            )}
                            {selectedCandidate.linkedinUrl && (
                                <a
                                    className="inline-flex items-center gap-1 text-blue-600 hover:underline"
                                    href={selectedCandidate.linkedinUrl}
                                    rel="noreferrer"
                                    target="_blank"
                                >
                                    <Share2 size={13} />
                                    <span>LinkedIn</span>
                                    <ExternalLink size={11} />
                                </a>
                            )}
                            {selectedCandidate.githubUrl && (
                                <a
                                    className="inline-flex items-center gap-1 text-slate-800 hover:underline"
                                    href={selectedCandidate.githubUrl}
                                    rel="noreferrer"
                                    target="_blank"
                                >
                                    <Code2 size={13} />
                                    <span>GitHub</span>
                                    <ExternalLink size={11} />
                                </a>
                            )}
                        </div>

                        <div className="mt-6 flex justify-end">
                            <button
                                className="rounded-lg bg-slate-100 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-200"
                                onClick={() => setSelectedCandidate(null)}
                                type="button"
                            >
                                Đóng
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Lock / Unlock Confirmation Dialog */}
            {actionCandidate && (
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
                                    {actionType === 'LOCK' ? 'Khóa tài khoản ứng viên' : 'Mở khóa tài khoản ứng viên'}
                                </h3>
                                <p className="text-xs text-slate-500">{actionCandidate.fullName} ({actionCandidate.email})</p>
                            </div>
                        </div>

                        <div className="mt-4 text-xs text-slate-600">
                            {actionType === 'LOCK' ? (
                                <p>
                                    Khi khóa tài khoản, ứng viên sẽ không thể đăng nhập hoặc ứng tuyển việc làm trên hệ thống.
                                </p>
                            ) : (
                                <p>
                                    Khi mở khóa, ứng viên sẽ được khôi phục toàn bộ quyền sử dụng hệ thống như bình thường.
                                </p>
                            )}
                        </div>

                        {actionType === 'LOCK' && (
                            <div className="mt-4">
                                <label className="block text-xs font-semibold text-slate-700 mb-1" htmlFor="action-reason">
                                    Lý do khóa tài khoản (tùy chọn):
                                </label>
                                <textarea
                                    className="w-full rounded-lg border border-slate-300 p-2 text-xs text-slate-900 focus:border-rose-500 focus:outline-none"
                                    id="action-reason"
                                    onChange={(e) => setActionReason(e.target.value)}
                                    placeholder="Nhập lý do khóa tài khoản vi phạm..."
                                    rows={3}
                                    value={actionReason}
                                />
                            </div>
                        )}

                        <div className="mt-6 flex justify-end gap-2">
                            <button
                                className="rounded-lg border border-slate-300 px-3 py-1.5 text-xs font-semibold text-slate-700 hover:bg-slate-50"
                                disabled={statusMutation.isPending}
                                onClick={() => setActionCandidate(null)}
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
