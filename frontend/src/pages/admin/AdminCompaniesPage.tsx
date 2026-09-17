import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
    AlertCircle,
    AlertTriangle,
    Building2,
    Check,
    CheckCircle2,
    Clock,
    ExternalLink,
    MapPin,
    RefreshCw,
    Search,
    ShieldAlert,
    X,
    XCircle,
} from 'lucide-react'
import {
    getAdminCompanies,
    getCompanyById,
    updateCompanyStatus,
} from '../../features/admin/adminApi'
import type {
    CompanyAdminResponse,
    CompanyStatus,
    UpdateCompanyStatusRequest,
} from '../../features/admin/adminTypes'

type TabType = 'PENDING' | 'APPROVED' | 'REJECTED' | 'ALL'

export function AdminCompaniesPage() {
    const queryClient = useQueryClient()

    // Filter states
    const [activeTab, setActiveTab] = useState<TabType>('PENDING')
    const [page, setPage] = useState(1)
    const [keywordInput, setKeywordInput] = useState('')
    const [activeKeyword, setActiveKeyword] = useState('')
    const [successMessage, setSuccessMessage] = useState<string | null>(null)
    const [actionError, setActionError] = useState<string | null>(null)

    // Status parameter for API
    const statusParam = activeTab === 'ALL' ? undefined : (activeTab as CompanyStatus)

    // Query companies list
    const {
        data: companiesPage,
        isLoading,
        isFetching,
        error: queryError,
        refetch,
    } = useQuery({
        queryKey: ['admin-companies', page, activeKeyword, statusParam],
        queryFn: () =>
            getAdminCompanies({
                page,
                size: 10,
                keyword: activeKeyword || undefined,
                status: statusParam,
            }),
    })

    // Detail modal state
    const [selectedCompany, setSelectedCompany] = useState<CompanyAdminResponse | null>(null)

    // Approve Dialog State
    const [approvingCompany, setApprovingCompany] = useState<CompanyAdminResponse | null>(null)

    // Reject Dialog State
    const [rejectingCompany, setRejectingCompany] = useState<CompanyAdminResponse | null>(null)
    const [rejectReason, setRejectReason] = useState('')
    const [rejectInputError, setRejectInputError] = useState<string | null>(null)

    // Mutation for updating company status
    const statusMutation = useMutation({
        mutationFn: async ({
            companyId,
            request,
        }: {
            companyId: number
            request: UpdateCompanyStatusRequest
        }) => {
            return await updateCompanyStatus(companyId, request)
        },
        onSuccess: (_data, variables) => {
            queryClient.invalidateQueries({ queryKey: ['admin-companies'] })
            if (variables.request.status === 'APPROVED') {
                setSuccessMessage(`Doanh nghiệp "${approvingCompany?.name}" đã được phê duyệt thành công.`)
                setApprovingCompany(null)
            } else {
                setSuccessMessage(`Đã từ chối doanh nghiệp "${rejectingCompany?.name}".`)
                setRejectingCompany(null)
                setRejectReason('')
                setRejectInputError(null)
            }
            setTimeout(() => setSuccessMessage(null), 4000)
        },
        onError: (err: unknown) => {
            const msg = err instanceof Error ? err.message : 'Thao tác không thành công'
            if (rejectingCompany) {
                setRejectInputError(msg)
            } else {
                setActionError(msg)
            }
        },
    })

    function handleTabChange(tab: TabType) {
        setActiveTab(tab)
        setPage(1)
    }

    function handleSearchSubmit(e: React.FormEvent) {
        e.preventDefault()
        setPage(1)
        setActiveKeyword(keywordInput)
    }

    async function handleViewDetail(companyId: number) {
        try {
            const detail = await getCompanyById(companyId)
            setSelectedCompany(detail)
        } catch (err: unknown) {
            const msg = err instanceof Error ? err.message : 'Không thể tải chi tiết doanh nghiệp'
            setActionError(msg)
        }
    }

    function handleConfirmApprove() {
        if (!approvingCompany) return
        statusMutation.mutate({
            companyId: approvingCompany.id,
            request: { status: 'APPROVED' },
        })
    }

    function handleConfirmReject() {
        if (!rejectingCompany) return
        if (!rejectReason.trim()) {
            setRejectInputError('Vui lòng nhập lý do từ chối để thông báo cho doanh nghiệp.')
            return
        }

        statusMutation.mutate({
            companyId: rejectingCompany.id,
            request: {
                status: 'REJECTED',
                reason: rejectReason.trim(),
            },
        })
    }

    const errorMessage = actionError || (queryError instanceof Error ? queryError.message : null)

    return (
        <div className="space-y-6">
            {/* Page Header */}
            <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-slate-900">
                        Phê duyệt & Quản lý doanh nghiệp
                    </h1>
                    <p className="text-sm text-slate-600">
                        Kiểm duyệt thông tin doanh nghiệp đăng ký, cấp phép đăng tin hoặc từ chối kèm lý do rõ ràng.
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
                    <CheckCircle2 className="shrink-0" size={18} />
                    <span>{successMessage}</span>
                </div>
            )}
            {errorMessage && (
                <div className="flex items-center gap-2 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">
                    <AlertCircle className="shrink-0" size={18} />
                    <span>{errorMessage}</span>
                </div>
            )}

            {/* Tab navigation */}
            <div className="flex border-b border-slate-200 gap-2">
                <button
                    className={`inline-flex items-center gap-2 border-b-2 px-4 py-2.5 text-sm font-semibold transition ${
                        activeTab === 'PENDING'
                            ? 'border-amber-500 text-amber-600'
                            : 'border-transparent text-slate-500 hover:border-slate-300 hover:text-slate-700'
                    }`}
                    onClick={() => handleTabChange('PENDING')}
                    type="button"
                >
                    <Clock size={16} />
                    <span>Chờ phê duyệt</span>
                    {activeTab === 'PENDING' && companiesPage && (
                        <span className="rounded-full bg-amber-100 px-2 py-0.5 text-xs font-bold text-amber-800">
                            {companiesPage.totalElements}
                        </span>
                    )}
                </button>

                <button
                    className={`inline-flex items-center gap-2 border-b-2 px-4 py-2.5 text-sm font-semibold transition ${
                        activeTab === 'APPROVED'
                            ? 'border-emerald-600 text-emerald-700'
                            : 'border-transparent text-slate-500 hover:border-slate-300 hover:text-slate-700'
                    }`}
                    onClick={() => handleTabChange('APPROVED')}
                    type="button"
                >
                    <Check size={16} />
                    <span>Đã phê duyệt</span>
                </button>

                <button
                    className={`inline-flex items-center gap-2 border-b-2 px-4 py-2.5 text-sm font-semibold transition ${
                        activeTab === 'REJECTED'
                            ? 'border-rose-600 text-rose-700'
                            : 'border-transparent text-slate-500 hover:border-slate-300 hover:text-slate-700'
                    }`}
                    onClick={() => handleTabChange('REJECTED')}
                    type="button"
                >
                    <XCircle size={16} />
                    <span>Bị từ chối</span>
                </button>

                <button
                    className={`inline-flex items-center gap-2 border-b-2 px-4 py-2.5 text-sm font-semibold transition ${
                        activeTab === 'ALL'
                            ? 'border-indigo-600 text-indigo-700'
                            : 'border-transparent text-slate-500 hover:border-slate-300 hover:text-slate-700'
                    }`}
                    onClick={() => handleTabChange('ALL')}
                    type="button"
                >
                    <Building2 size={16} />
                    <span>Tất cả</span>
                </button>
            </div>

            {/* Search Bar */}
            <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
                <form className="flex flex-col gap-3 sm:flex-row sm:items-center" onSubmit={handleSearchSubmit}>
                    <div className="relative flex-1">
                        <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                        <input
                            className="w-full rounded-lg border border-slate-300 py-2 pl-10 pr-4 text-sm text-slate-900 placeholder:text-slate-400 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
                            onChange={(e) => setKeywordInput(e.target.value)}
                            placeholder="Tìm kiếm theo tên công ty, mã số thuế, địa chỉ..."
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

            {/* Companies Table */}
            <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
                {isLoading ? (
                    <div className="flex h-64 items-center justify-center">
                        <div className="flex items-center gap-3 text-slate-500">
                            <RefreshCw className="animate-spin" size={20} />
                            <span>Đang tải danh sách doanh nghiệp...</span>
                        </div>
                    </div>
                ) : companiesPage?.content.length === 0 ? (
                    <div className="flex h-64 flex-col items-center justify-center p-6 text-center">
                        <div className="grid size-12 place-items-center rounded-full bg-slate-100 text-slate-400 mb-3">
                            <Building2 size={24} />
                        </div>
                        <h3 className="font-semibold text-slate-800">Không có doanh nghiệp nào</h3>
                        <p className="mt-1 text-sm text-slate-500">
                            {activeTab === 'PENDING'
                                ? 'Hiện không có yêu cầu phê duyệt doanh nghiệp nào đang chờ xử lý.'
                                : 'Không tìm thấy kết quả phù hợp với điều kiện lọc.'}
                        </p>
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-left text-sm text-slate-600">
                            <thead className="border-b border-slate-200 bg-slate-50/80 text-xs uppercase font-semibold text-slate-700">
                                <tr>
                                    <th className="px-4 py-3.5" scope="col">Doanh nghiệp</th>
                                    <th className="px-4 py-3.5" scope="col">Mã số thuế</th>
                                    <th className="px-4 py-3.5" scope="col">Địa điểm & Quy mô</th>
                                    <th className="px-4 py-3.5" scope="col">Trạng thái</th>
                                    <th className="px-4 py-3.5 text-right" scope="col">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-200">
                                {companiesPage?.content.map((company) => (
                                    <tr className="hover:bg-slate-50/70 transition" key={company.id}>
                                        <td className="px-4 py-3.5">
                                            <div className="flex items-center gap-3">
                                                {company.logoUrl ? (
                                                    <img
                                                        alt={company.name}
                                                        className="size-10 rounded-lg object-cover border border-slate-200"
                                                        src={company.logoUrl}
                                                    />
                                                ) : (
                                                    <div className="grid size-10 place-items-center rounded-lg bg-indigo-100 text-indigo-700 font-bold text-sm">
                                                        {company.name.charAt(0).toUpperCase()}
                                                    </div>
                                                )}
                                                <div>
                                                    <div className="font-semibold text-slate-900">
                                                        {company.name}
                                                    </div>
                                                    {company.website ? (
                                                        <a
                                                            className="inline-flex items-center gap-1 text-xs text-indigo-600 hover:underline"
                                                            href={company.website}
                                                            rel="noreferrer"
                                                            target="_blank"
                                                        >
                                                            <span>{company.website.replace(/^https?:\/\//, '')}</span>
                                                            <ExternalLink size={10} />
                                                        </a>
                                                    ) : (
                                                        <span className="text-xs text-slate-400">Chưa có website</span>
                                                    )}
                                                </div>
                                            </div>
                                        </td>
                                        <td className="px-4 py-3.5">
                                            <span className="font-mono text-xs font-semibold text-slate-800 bg-slate-100 px-2 py-1 rounded">
                                                {company.taxCode || 'Chưa cập nhật'}
                                            </span>
                                        </td>
                                        <td className="px-4 py-3.5">
                                            <div className="flex flex-col gap-0.5 text-xs">
                                                <div className="flex items-center gap-1 text-slate-700">
                                                    <MapPin size={13} className="text-slate-400 shrink-0" />
                                                    <span>{[company.address, company.city].filter(Boolean).join(', ') || 'Chưa cập nhật'}</span>
                                                </div>
                                                <div className="text-slate-500">
                                                    {company.companySize ? `Quy mô: ${company.companySize}` : 'Chưa cập nhật quy mô'}
                                                </div>
                                            </div>
                                        </td>
                                        <td className="px-4 py-3.5">
                                            {company.status === 'APPROVED' && (
                                                <span className="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-semibold text-emerald-700 border border-emerald-200">
                                                    <span className="size-1.5 rounded-full bg-emerald-500" />
                                                    Đã duyệt
                                                </span>
                                            )}
                                            {company.status === 'PENDING' && (
                                                <span className="inline-flex items-center gap-1 rounded-full bg-amber-50 px-2.5 py-1 text-xs font-semibold text-amber-700 border border-amber-200">
                                                    <span className="size-1.5 rounded-full bg-amber-500" />
                                                    Chờ duyệt
                                                </span>
                                            )}
                                            {company.status === 'REJECTED' && (
                                                <span className="inline-flex items-center gap-1 rounded-full bg-rose-50 px-2.5 py-1 text-xs font-semibold text-rose-700 border border-rose-200">
                                                    <span className="size-1.5 rounded-full bg-rose-500" />
                                                    Bị từ chối
                                                </span>
                                            )}
                                        </td>
                                        <td className="px-4 py-3.5 text-right">
                                            <div className="inline-flex items-center gap-1">
                                                <button
                                                    className="rounded-lg border border-slate-200 bg-white px-2.5 py-1.5 text-xs font-medium text-slate-700 shadow-sm transition hover:bg-slate-50"
                                                    onClick={() => handleViewDetail(company.id)}
                                                    type="button"
                                                >
                                                    Chi tiết
                                                </button>

                                                {company.status === 'PENDING' && (
                                                    <>
                                                        <button
                                                            className="inline-flex items-center gap-1 rounded-lg border border-emerald-200 bg-emerald-50 px-2.5 py-1.5 text-xs font-semibold text-emerald-700 transition hover:bg-emerald-100"
                                                            disabled={statusMutation.isPending}
                                                            onClick={() => setApprovingCompany(company)}
                                                            title="Phê duyệt doanh nghiệp"
                                                            type="button"
                                                        >
                                                            <Check size={13} />
                                                            <span>Duyệt</span>
                                                        </button>
                                                        <button
                                                            className="inline-flex items-center gap-1 rounded-lg border border-rose-200 bg-rose-50 px-2.5 py-1.5 text-xs font-semibold text-rose-700 transition hover:bg-rose-100"
                                                            disabled={statusMutation.isPending}
                                                            onClick={() => {
                                                                setRejectingCompany(company)
                                                                setRejectReason('')
                                                                setRejectInputError(null)
                                                            }}
                                                            title="Từ chối doanh nghiệp"
                                                            type="button"
                                                        >
                                                            <X size={13} />
                                                            <span>Từ chối</span>
                                                        </button>
                                                    </>
                                                )}

                                                {company.status === 'APPROVED' && (
                                                    <button
                                                        className="inline-flex items-center gap-1 rounded-lg border border-rose-200 bg-rose-50 px-2.5 py-1.5 text-xs font-semibold text-rose-700 transition hover:bg-rose-100"
                                                        disabled={statusMutation.isPending}
                                                        onClick={() => {
                                                            setRejectingCompany(company)
                                                            setRejectReason('')
                                                            setRejectInputError(null)
                                                        }}
                                                        title="Thu hồi phê duyệt / Từ chối"
                                                        type="button"
                                                    >
                                                        <span>Thu hồi</span>
                                                    </button>
                                                )}

                                                {company.status === 'REJECTED' && (
                                                    <button
                                                        className="inline-flex items-center gap-1 rounded-lg border border-emerald-200 bg-emerald-50 px-2.5 py-1.5 text-xs font-semibold text-emerald-700 transition hover:bg-emerald-100"
                                                        disabled={statusMutation.isPending}
                                                        onClick={() => setApprovingCompany(company)}
                                                        title="Phê duyệt lại"
                                                        type="button"
                                                    >
                                                        <span>Duyệt lại</span>
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
                {companiesPage && companiesPage.totalPages > 0 && (
                    <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between border-t border-slate-200 px-4 py-3 text-xs text-slate-600 gap-2">
                        <div>
                            Hiển thị trang <span className="font-semibold text-slate-900">{companiesPage.pageNumber}</span> /{' '}
                            <span className="font-semibold text-slate-900">{companiesPage.totalPages}</span> ({companiesPage.totalElements} doanh nghiệp)
                        </div>
                        <div className="flex items-center gap-1">
                            <button
                                className="rounded border border-slate-200 bg-white px-2.5 py-1 font-medium text-slate-700 disabled:opacity-40 hover:bg-slate-50"
                                disabled={companiesPage.pageNumber <= 1}
                                onClick={() => setPage((p) => Math.max(1, p - 1))}
                                type="button"
                            >
                                Trước
                            </button>
                            <button
                                className="rounded border border-slate-200 bg-white px-2.5 py-1 font-medium text-slate-700 disabled:opacity-40 hover:bg-slate-50"
                                disabled={companiesPage.isLast || companiesPage.pageNumber >= companiesPage.totalPages}
                                onClick={() => setPage((p) => p + 1)}
                                type="button"
                            >
                                Sau
                            </button>
                        </div>
                    </div>
                )}
            </div>

            {/* Company Detail Modal */}
            {selectedCompany && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-xs p-4">
                    <div className="relative w-full max-w-2xl rounded-2xl bg-white p-6 shadow-xl max-h-[90vh] overflow-y-auto">
                        <button
                            className="absolute right-4 top-4 rounded-lg p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
                            onClick={() => setSelectedCompany(null)}
                            type="button"
                        >
                            <X size={20} />
                        </button>

                        <div className="flex items-start gap-4">
                            {selectedCompany.logoUrl ? (
                                <img
                                    alt={selectedCompany.name}
                                    className="size-16 rounded-xl object-cover border border-slate-200"
                                    src={selectedCompany.logoUrl}
                                />
                            ) : (
                                <div className="grid size-16 place-items-center rounded-xl bg-indigo-100 text-indigo-700 font-bold text-xl">
                                    {selectedCompany.name.charAt(0).toUpperCase()}
                                </div>
                            )}
                            <div>
                                <h3 className="text-xl font-bold text-slate-900">{selectedCompany.name}</h3>
                                <div className="mt-1 flex items-center gap-2">
                                    {selectedCompany.status === 'APPROVED' && (
                                        <span className="inline-flex items-center rounded-full bg-emerald-50 px-2 py-0.5 text-xs font-semibold text-emerald-700 border border-emerald-200">
                                            Đã duyệt
                                        </span>
                                    )}
                                    {selectedCompany.status === 'PENDING' && (
                                        <span className="inline-flex items-center rounded-full bg-amber-50 px-2 py-0.5 text-xs font-semibold text-amber-700 border border-amber-200">
                                            Chờ duyệt
                                        </span>
                                    )}
                                    {selectedCompany.status === 'REJECTED' && (
                                        <span className="inline-flex items-center rounded-full bg-rose-50 px-2 py-0.5 text-xs font-semibold text-rose-700 border border-rose-200">
                                            Bị từ chối
                                        </span>
                                    )}
                                    <span className="text-xs text-slate-500">
                                        Mã công ty: #{selectedCompany.id}
                                    </span>
                                </div>
                            </div>
                        </div>

                        <div className="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-2 border-t border-slate-100 pt-4 text-xs">
                            <div className="space-y-2">
                                <div>
                                    <span className="font-semibold text-slate-500">Mã số thuế (MST):</span>{' '}
                                    <span className="font-mono font-semibold text-slate-900">{selectedCompany.taxCode || 'Chưa cập nhật'}</span>
                                </div>
                                <div>
                                    <span className="font-semibold text-slate-500">Website:</span>{' '}
                                    {selectedCompany.website ? (
                                        <a
                                            className="text-indigo-600 hover:underline inline-flex items-center gap-1"
                                            href={selectedCompany.website}
                                            rel="noreferrer"
                                            target="_blank"
                                        >
                                            <span>{selectedCompany.website}</span>
                                            <ExternalLink size={11} />
                                        </a>
                                    ) : (
                                        <span className="text-slate-900">Chưa cập nhật</span>
                                    )}
                                </div>
                                <div>
                                    <span className="font-semibold text-slate-500">Quy mô nhân sự:</span>{' '}
                                    <span className="text-slate-900">{selectedCompany.companySize || 'Chưa cập nhật'}</span>
                                </div>
                            </div>

                            <div className="space-y-2">
                                <div>
                                    <span className="font-semibold text-slate-500">Địa chỉ:</span>{' '}
                                    <span className="text-slate-900">{selectedCompany.address || 'Chưa cập nhật'}</span>
                                </div>
                                <div>
                                    <span className="font-semibold text-slate-500">Tỉnh / Thành phố:</span>{' '}
                                    <span className="text-slate-900">{selectedCompany.city || 'Chưa cập nhật'}</span>
                                </div>
                                <div>
                                    <span className="font-semibold text-slate-500">ID Người tạo:</span>{' '}
                                    <span className="font-mono text-slate-900">#{selectedCompany.createdByUserId || 'N/A'}</span>
                                </div>
                            </div>
                        </div>

                        {selectedCompany.description && (
                            <div className="mt-4 rounded-lg bg-slate-50 p-3 text-xs text-slate-700">
                                <div className="font-semibold text-slate-900 mb-1">Mô tả doanh nghiệp:</div>
                                <p className="leading-relaxed whitespace-pre-wrap">{selectedCompany.description}</p>
                            </div>
                        )}

                        <div className="mt-6 flex justify-end gap-2">
                            <button
                                className="rounded-lg bg-slate-100 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-200"
                                onClick={() => setSelectedCompany(null)}
                                type="button"
                            >
                                Đóng
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Approve Confirmation Modal */}
            {approvingCompany && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-xs p-4">
                    <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl">
                        <div className="flex items-center gap-3">
                            <div className="grid size-10 place-items-center rounded-xl bg-emerald-100 text-emerald-600">
                                <Check size={22} />
                            </div>
                            <div>
                                <h3 className="font-bold text-slate-900">Xác nhận phê duyệt doanh nghiệp</h3>
                                <p className="text-xs text-slate-500">{approvingCompany.name}</p>
                            </div>
                        </div>

                        <p className="mt-4 text-xs text-slate-600 leading-relaxed">
                            Khi phê duyệt, doanh nghiệp sẽ được cấp trạng thái <strong>APPROVED</strong>.
                            Các nhà tuyển dụng thuộc doanh nghiệp này sẽ có quyền đăng tin tuyển dụng và tìm kiếm ứng viên.
                        </p>

                        <div className="mt-6 flex justify-end gap-2">
                            <button
                                className="rounded-lg border border-slate-300 px-3 py-1.5 text-xs font-semibold text-slate-700 hover:bg-slate-50"
                                disabled={statusMutation.isPending}
                                onClick={() => setApprovingCompany(null)}
                                type="button"
                            >
                                Hủy bỏ
                            </button>
                            <button
                                className="rounded-lg bg-emerald-600 px-4 py-1.5 text-xs font-semibold text-white shadow-sm hover:bg-emerald-700 transition"
                                disabled={statusMutation.isPending}
                                onClick={handleConfirmApprove}
                                type="button"
                            >
                                {statusMutation.isPending ? 'Đang xử lý...' : 'Xác nhận duyệt'}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Reject Modal with Mandatory Reason Input */}
            {rejectingCompany && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-xs p-4">
                    <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl">
                        <div className="flex items-center gap-3">
                            <div className="grid size-10 place-items-center rounded-xl bg-rose-100 text-rose-600">
                                <ShieldAlert size={22} />
                            </div>
                            <div>
                                <h3 className="font-bold text-slate-900">Từ chối doanh nghiệp</h3>
                                <p className="text-xs text-slate-500">{rejectingCompany.name}</p>
                            </div>
                        </div>

                        <div className="mt-4">
                            <label className="block text-xs font-semibold text-slate-700 mb-1" htmlFor="company-reject-reason">
                                Lý do từ chối <span className="text-rose-600">*</span>
                            </label>
                            <textarea
                                className={`w-full rounded-lg border p-2 text-xs text-slate-900 focus:outline-none ${
                                    rejectInputError ? 'border-rose-500 ring-1 ring-rose-500' : 'border-slate-300 focus:border-rose-500'
                                }`}
                                id="company-reject-reason"
                                onChange={(e) => {
                                    setRejectReason(e.target.value)
                                    if (rejectInputError) setRejectInputError(null)
                                }}
                                placeholder="Ví dụ: Mã số thuế không trùng khớp với đăng ký kinh doanh, địa chỉ công ty chưa chính xác..."
                                rows={4}
                                value={rejectReason}
                            />
                            {rejectInputError && (
                                <p className="mt-1 flex items-center gap-1 text-xs text-rose-600">
                                    <AlertTriangle size={12} />
                                    <span>{rejectInputError}</span>
                                </p>
                            )}
                        </div>

                        <div className="mt-6 flex justify-end gap-2">
                            <button
                                className="rounded-lg border border-slate-300 px-3 py-1.5 text-xs font-semibold text-slate-700 hover:bg-slate-50"
                                disabled={statusMutation.isPending}
                                onClick={() => setRejectingCompany(null)}
                                type="button"
                            >
                                Hủy bỏ
                            </button>
                            <button
                                className="rounded-lg bg-rose-600 px-4 py-1.5 text-xs font-semibold text-white shadow-sm hover:bg-rose-700 transition"
                                disabled={statusMutation.isPending}
                                onClick={handleConfirmReject}
                                type="button"
                            >
                                {statusMutation.isPending ? 'Đang xử lý...' : 'Xác nhận từ chối'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    )
}
