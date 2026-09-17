import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
    AlertCircle,
    Building2,
    Check,
    CheckCircle2,
    Clock,
    FileText,
    Filter,
    LoaderCircle,
    Mail,
    Phone,
    UserCheck,
    Users,
    X,
} from 'lucide-react'
import { useState } from 'react'
import { Link } from 'react-router'
import {
    getCompanyJoinRequests,
    getRecruiterProfile,
    RecruiterApiError,
    reviewJoinRequest,
} from '../../features/recruiter/recruiterApi'
import type {
    CompanyJoinRequestResponse,
    ReviewJoinRequest,
} from '../../features/recruiter/recruiterTypes'

type StatusFilter = 'ALL' | 'PENDING' | 'ACCEPTED' | 'REJECTED'

export function RecruiterPeerApprovalPage() {
    const queryClient = useQueryClient()
    const [selectedStatus, setSelectedStatus] = useState<StatusFilter>('PENDING')
    const [currentPage, setCurrentPage] = useState(1)
    const pageSize = 10

    const [rejectingRequest, setRejectingRequest] = useState<CompanyJoinRequestResponse | null>(null)
    const [rejectReason, setRejectReason] = useState('')
    const [rejectError, setRejectError] = useState('')
    const [actionMessage, setActionMessage] = useState('')
    const [serverError, setServerError] = useState('')

    const { data: profile } = useQuery({
        queryKey: ['recruiter-profile'],
        queryFn: getRecruiterProfile,
    })

    const statusParam = selectedStatus === 'ALL' ? undefined : selectedStatus

    const {
        data: requestsPage,
        isLoading,
        error: loadError,
        refetch,
    } = useQuery({
        queryKey: ['company-join-requests', statusParam, currentPage, pageSize],
        queryFn: () =>
            getCompanyJoinRequests({
                status: statusParam,
                page: currentPage,
                size: pageSize,
            }),
        enabled: Boolean(profile?.companyId && profile?.companyStatus === 'APPROVED'),
    })

    const reviewMutation = useMutation({
        mutationFn: ({ requestId, data }: { requestId: number; data: ReviewJoinRequest }) =>
            reviewJoinRequest(requestId, data),
        onSuccess: (_res, variables) => {
            queryClient.invalidateQueries({ queryKey: ['company-join-requests'] })
            const actionText = variables.data.status === 'ACCEPTED' ? 'Phê duyệt' : 'Từ chối'
            setActionMessage(`${actionText} yêu cầu gia nhập thành công!`)
            setRejectingRequest(null)
            setRejectReason('')
            setRejectError('')
            setServerError('')
        },
        onError: (err) => {
            if (err instanceof RecruiterApiError) {
                setServerError(err.message)
            } else {
                setServerError('Không thể xử lý yêu cầu. Vui lòng thử lại.')
            }
        },
    })

    function handleApprove(request: CompanyJoinRequestResponse) {
        if (!window.confirm(`Bạn có chắc chắn muốn phê duyệt HR "${request.applicantName}" vào ${profile?.companyName}?`)) {
            return
        }

        reviewMutation.mutate({
            requestId: request.id,
            data: {
                status: 'ACCEPTED',
                reason: 'Chào mừng gia nhập đội ngũ tuyển dụng!',
            },
        })
    }

    function openRejectModal(request: CompanyJoinRequestResponse) {
        setRejectingRequest(request)
        setRejectReason('')
        setRejectError('')
        setServerError('')
    }

    function handleRejectSubmit() {
        if (!rejectingRequest) return
        if (!rejectReason.trim()) {
            setRejectError('Vui lòng nhập lý do từ chối yêu cầu gia nhập.')
            return
        }

        reviewMutation.mutate({
            requestId: rejectingRequest.id,
            data: {
                status: 'REJECTED',
                reason: rejectReason.trim(),
            },
        })
    }

    const hasApprovedCompany = profile?.companyId && profile?.companyStatus === 'APPROVED'

    if (!hasApprovedCompany) {
        return (
            <div className="space-y-6">
                <div>
                    <h1 className="text-2xl font-bold text-slate-900">Duyệt thành viên nội bộ</h1>
                    <p className="text-sm text-slate-500">
                        Xét duyệt yêu cầu gia nhập của các nhân viên tuyển dụng khác vào doanh nghiệp của bạn.
                    </p>
                </div>

                <div className="rounded-2xl border border-amber-200 bg-amber-50 p-8 text-center shadow-sm">
                    <Building2 className="mx-auto text-amber-600 mb-3" size={40} />
                    <h2 className="text-lg font-bold text-amber-900">
                        Chưa liên kết Doanh nghiệp đã được phê duyệt
                    </h2>
                    <p className="mx-auto mt-2 max-w-lg text-sm text-amber-700">
                        Để thực hiện xét duyệt thành viên đồng nghiệp, tài khoản HR của bạn cần thuộc về một doanh nghiệp đã có trạng thái <strong>ĐÃ PHÊ DUYỆT (APPROVED)</strong>.
                    </p>
                    <div className="mt-5 flex justify-center gap-3">
                        <Link
                            className="rounded-xl bg-amber-600 px-4 py-2 text-xs font-semibold text-white shadow hover:bg-amber-700"
                            to="/recruiter/company"
                        >
                            Xem trạng thái doanh nghiệp
                        </Link>
                        <Link
                            className="rounded-xl border border-amber-300 bg-white px-4 py-2 text-xs font-semibold text-amber-800 hover:bg-amber-100"
                            to="/recruiter/join-company"
                        >
                            Tìm & gia nhập công ty
                        </Link>
                    </div>
                </div>
            </div>
        )
    }

    return (
        <div className="space-y-6">
            <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <h1 className="text-2xl font-bold text-slate-900">
                        Duyệt thành viên nội bộ: {profile.companyName}
                    </h1>
                    <p className="text-sm text-slate-500">
                        Danh sách các nhà tuyển dụng gửi yêu cầu gia nhập vào tổ chức của bạn.
                    </p>
                </div>

                <div className="flex items-center gap-2">
                    <span className="inline-flex items-center gap-1.5 rounded-xl border border-emerald-200 bg-emerald-50 px-3 py-1.5 text-xs font-semibold text-emerald-800">
                        <UserCheck size={14} />
                        <span>Người duyệt: {profile.fullName}</span>
                    </span>
                </div>
            </div>

            {actionMessage && (
                <div className="flex items-center justify-between rounded-xl border border-emerald-200 bg-emerald-50 p-4 text-sm text-emerald-800">
                    <div className="flex items-center gap-2">
                        <CheckCircle2 className="shrink-0 text-emerald-600" size={18} />
                        <span>{actionMessage}</span>
                    </div>
                    <button
                        className="text-xs text-emerald-700 underline"
                        onClick={() => setActionMessage('')}
                        type="button"
                    >
                        Đóng
                    </button>
                </div>
            )}

            {serverError && (
                <div className="flex gap-2 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
                    <AlertCircle className="shrink-0 mt-0.5" size={18} />
                    <span>{serverError}</span>
                </div>
            )}

            {/* Filter Tabs */}
            <div className="flex flex-wrap items-center gap-2 border-b border-slate-200 pb-3">
                <div className="flex items-center gap-1 text-xs text-slate-500 mr-2">
                    <Filter size={14} />
                    <span>Trạng thái:</span>
                </div>

                {(
                    [
                        { key: 'PENDING', label: 'Chờ xét duyệt' },
                        { key: 'ACCEPTED', label: 'Đã phê duyệt' },
                        { key: 'REJECTED', label: 'Đã từ chối' },
                        { key: 'ALL', label: 'Tất cả' },
                    ] as const
                ).map((tab) => (
                    <button
                        className={`rounded-xl px-3.5 py-1.5 text-xs font-semibold transition ${
                            selectedStatus === tab.key
                                ? 'bg-emerald-600 text-white shadow-sm'
                                : 'bg-white text-slate-600 hover:bg-slate-100 border border-slate-200'
                        }`}
                        key={tab.key}
                        onClick={() => {
                            setSelectedStatus(tab.key)
                            setCurrentPage(1)
                        }}
                        type="button"
                    >
                        {tab.label}
                    </button>
                ))}
            </div>

            {/* Requests List */}
            {isLoading ? (
                <div className="flex h-64 items-center justify-center">
                    <LoaderCircle className="animate-spin text-emerald-600" size={32} />
                </div>
            ) : loadError ? (
                <div className="rounded-xl border border-red-200 bg-red-50 p-6 text-center text-red-700">
                    <AlertCircle className="mx-auto mb-2" size={24} />
                    <p className="font-semibold">Lỗi khi tải danh sách yêu cầu gia nhập.</p>
                    <p className="mt-1 text-sm">{(loadError as Error).message}</p>
                    <button
                        className="mt-3 rounded-lg bg-red-600 px-3 py-1.5 text-xs font-medium text-white"
                        onClick={() => refetch()}
                        type="button"
                    >
                        Thử lại
                    </button>
                </div>
            ) : requestsPage?.content?.length === 0 ? (
                <div className="rounded-2xl border border-slate-200 bg-white p-12 text-center shadow-sm">
                    <Users className="mx-auto text-slate-300 mb-3" size={44} />
                    <h3 className="font-bold text-slate-800">Không có yêu cầu nào</h3>
                    <p className="mt-1 text-sm text-slate-500">
                        {selectedStatus === 'PENDING'
                            ? 'Hiện không có đơn xin gia nhập nào đang chờ phê duyệt.'
                            : 'Không có bản ghi yêu cầu gia nhập theo bộ lọc này.'}
                    </p>
                </div>
            ) : (
                <div className="space-y-4">
                    {requestsPage?.content.map((request) => (
                        <div
                            className="rounded-2xl border border-slate-200 bg-white p-5 sm:p-6 shadow-sm hover:border-emerald-200 transition"
                            key={request.id}
                        >
                            <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                                <div className="flex items-start gap-4">
                                    {request.applicantAvatarUrl ? (
                                        <img
                                            alt={request.applicantName}
                                            className="size-14 rounded-full border border-slate-200 object-cover shrink-0"
                                            src={request.applicantAvatarUrl}
                                        />
                                    ) : (
                                        <div className="grid size-14 place-items-center rounded-full bg-emerald-100 text-emerald-700 font-bold text-lg shrink-0">
                                            {request.applicantName ? request.applicantName.charAt(0).toUpperCase() : 'HR'}
                                        </div>
                                    )}

                                    <div className="min-w-0">
                                        <div className="flex items-center gap-2 flex-wrap">
                                            <h3 className="font-bold text-slate-900 text-base">
                                                {request.applicantName}
                                            </h3>
                                            <span
                                                className={`rounded-full px-2.5 py-0.5 text-[11px] font-semibold ${
                                                    request.status === 'ACCEPTED'
                                                        ? 'bg-emerald-100 text-emerald-800'
                                                        : request.status === 'PENDING'
                                                        ? 'bg-amber-100 text-amber-800'
                                                        : 'bg-red-100 text-red-800'
                                                }`}
                                            >
                                                {request.status === 'ACCEPTED'
                                                    ? 'ĐÃ DUYỆT'
                                                    : request.status === 'PENDING'
                                                    ? 'CHỜ DUYỆT'
                                                    : 'ĐÃ TỪ CHỐI'}
                                            </span>
                                        </div>

                                        <p className="text-xs font-semibold text-emerald-700 mt-0.5">
                                            Vị trí ứng tuyển: {request.position || 'Chuyên viên tuyển dụng'}
                                        </p>

                                        <div className="mt-2 flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-slate-500">
                                            <span className="flex items-center gap-1">
                                                <Mail size={13} />
                                                <span>{request.applicantEmail}</span>
                                            </span>
                                            {request.applicantPhone && (
                                                <span className="flex items-center gap-1">
                                                    <Phone size={13} />
                                                    <span>{request.applicantPhone}</span>
                                                </span>
                                            )}
                                            <span className="flex items-center gap-1 text-slate-400">
                                                <Clock size={13} />
                                                <span>Nộp ngày: {new Date(request.createdAt).toLocaleDateString('vi-VN')}</span>
                                            </span>
                                        </div>
                                    </div>
                                </div>

                                {/* Actions for PENDING status */}
                                {request.status === 'PENDING' && (
                                    <div className="flex items-center gap-2 self-end sm:self-center">
                                        <button
                                            className="inline-flex items-center gap-1 rounded-xl bg-emerald-600 px-3.5 py-2 text-xs font-semibold text-white shadow-sm transition hover:bg-emerald-700 disabled:opacity-70"
                                            disabled={reviewMutation.isPending}
                                            onClick={() => handleApprove(request)}
                                            type="button"
                                        >
                                            <Check size={14} />
                                            <span>Phê duyệt</span>
                                        </button>

                                        <button
                                            className="inline-flex items-center gap-1 rounded-xl border border-red-200 bg-red-50 px-3.5 py-2 text-xs font-semibold text-red-700 transition hover:bg-red-100 disabled:opacity-70"
                                            disabled={reviewMutation.isPending}
                                            onClick={() => openRejectModal(request)}
                                            type="button"
                                        >
                                            <X size={14} />
                                            <span>Từ chối</span>
                                        </button>
                                    </div>
                                )}
                            </div>

                            {/* Message / Reason */}
                            {request.message && (
                                <div className="mt-4 rounded-xl bg-slate-50 p-3.5 text-xs text-slate-700 border border-slate-100">
                                    <div className="flex items-start gap-2">
                                        <FileText className="text-slate-400 shrink-0 mt-0.5" size={14} />
                                        <div>
                                            <strong className="text-slate-900">Lời nhắn từ ứng viên:</strong>
                                            <p className="mt-0.5 text-slate-600 italic leading-relaxed">
                                                &ldquo;{request.message}&rdquo;
                                            </p>
                                        </div>
                                    </div>
                                </div>
                            )}

                            {request.reason && request.status !== 'PENDING' && (
                                <div className="mt-3 text-xs text-slate-500">
                                    <strong>Ghi chú xử lý:</strong> {request.reason}
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            )}

            {/* Pagination */}
            {requestsPage && requestsPage.totalPages > 1 && (
                <div className="flex items-center justify-between border-t border-slate-200 pt-4">
                    <p className="text-xs text-slate-500">
                        Trang {requestsPage.pageNumber} / {requestsPage.totalPages} ({requestsPage.totalElements} yêu cầu)
                    </p>
                    <div className="flex gap-2">
                        <button
                            className="rounded-lg border border-slate-300 px-3 py-1.5 text-xs font-medium text-slate-700 hover:bg-slate-50 disabled:opacity-40"
                            disabled={currentPage <= 1}
                            onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
                            type="button"
                        >
                            Trang trước
                        </button>
                        <button
                            className="rounded-lg border border-slate-300 px-3 py-1.5 text-xs font-medium text-slate-700 hover:bg-slate-50 disabled:opacity-40"
                            disabled={requestsPage.isLast}
                            onClick={() => setCurrentPage((p) => p + 1)}
                            type="button"
                        >
                            Trang kế
                        </button>
                    </div>
                </div>
            )}

            {/* Rejection Modal Dialog */}
            {rejectingRequest && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-sm p-4">
                    <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-2xl">
                        <div className="flex items-start justify-between border-b border-slate-100 pb-3 mb-4">
                            <div>
                                <h3 className="font-bold text-slate-900">Từ chối yêu cầu gia nhập</h3>
                                <p className="text-xs text-slate-500">
                                    Ứng viên: <strong className="text-slate-800">{rejectingRequest.applicantName}</strong>
                                </p>
                            </div>
                            <button
                                className="rounded-lg p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
                                onClick={() => setRejectingRequest(null)}
                                type="button"
                            >
                                <X size={18} />
                            </button>
                        </div>

                        <div className="space-y-4">
                            <div>
                                <label className="mb-1 block text-xs font-medium text-slate-700" htmlFor="reject-reason">
                                    Lý do từ chối <span className="text-red-500">*</span>
                                </label>
                                <textarea
                                    className="w-full rounded-xl border border-slate-300 bg-white p-3 text-sm text-slate-900 outline-none transition focus:border-red-500 focus:ring-4 focus:ring-red-100"
                                    id="reject-reason"
                                    onChange={(e) => {
                                        setRejectReason(e.target.value)
                                        setRejectError('')
                                    }}
                                    placeholder="Nhập lý do cụ thể gửi thông báo cho ứng viên..."
                                    rows={3}
                                    value={rejectReason}
                                />
                                {rejectError && (
                                    <p className="mt-1 text-xs text-red-600">{rejectError}</p>
                                )}
                            </div>

                            {serverError && (
                                <div className="flex gap-2 rounded-xl border border-red-200 bg-red-50 p-2.5 text-xs text-red-700">
                                    <AlertCircle className="shrink-0 mt-0.5" size={15} />
                                    <span>{serverError}</span>
                                </div>
                            )}

                            <div className="flex items-center justify-end gap-2 pt-2">
                                <button
                                    className="rounded-xl border border-slate-300 px-4 py-2 text-xs font-semibold text-slate-700 hover:bg-slate-50"
                                    onClick={() => setRejectingRequest(null)}
                                    type="button"
                                >
                                    Hủy
                                </button>
                                <button
                                    className="inline-flex items-center gap-1.5 rounded-xl bg-red-600 px-4 py-2 text-xs font-semibold text-white shadow transition hover:bg-red-700 disabled:opacity-70"
                                    disabled={reviewMutation.isPending}
                                    onClick={handleRejectSubmit}
                                    type="button"
                                >
                                    {reviewMutation.isPending && (
                                        <LoaderCircle className="animate-spin" size={14} />
                                    )}
                                    <span>Xác nhận từ chối</span>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    )
}
