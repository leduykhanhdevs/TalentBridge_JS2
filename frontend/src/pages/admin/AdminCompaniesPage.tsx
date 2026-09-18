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
    Filter,
    MapPin,
    RefreshCw,
    Search,
    ShieldAlert,
    Sparkles,
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
import {
    AdminControlCenterIllustration,
    EmptyStateIllustration,
    Soft3DActiveBadge,
    Soft3DBuildingBadge,
    Soft3DShieldBadge,
    Soft3DVerifiedBadge,
} from '../../components/illustrations'

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

    function handleResetFilters() {
        setKeywordInput('')
        setActiveKeyword('')
        setActiveTab('PENDING')
        setPage(1)
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

    // Metric counts
    const totalCompanies = companiesPage?.totalElements ?? 0
    const pendingCount = companiesPage?.content.filter((c) => c.status === 'PENDING').length ?? 0
    const approvedCount = companiesPage?.content.filter((c) => c.status === 'APPROVED').length ?? 0
    const rejectedCount = companiesPage?.content.filter((c) => c.status === 'REJECTED').length ?? 0

    return (
        <div className="space-y-6 sm:space-y-8 pb-12">
            {/* 1. Bento Hero Command Banner */}
            <div className="bento-card relative overflow-hidden border border-slate-200/90 bg-gradient-to-br from-white via-indigo-50/25 to-violet-50/35 p-6 sm:p-8 shadow-sm">
                <div className="pointer-events-none absolute -right-12 -top-12 size-64 rounded-full bg-indigo-200/30 blur-2xl" />
                <div className="pointer-events-none absolute bottom-0 right-1/4 size-48 rounded-full bg-amber-200/20 blur-2xl" />

                <div className="relative z-10 flex flex-col gap-6 lg:flex-row lg:items-center lg:justify-between">
                    <div className="max-w-2xl space-y-3">
                        <div className="inline-flex items-center gap-2 rounded-full border border-indigo-200/80 bg-indigo-50/90 px-3.5 py-1 text-xs font-bold uppercase tracking-wider text-indigo-700 shadow-2xs">
                            <Sparkles className="size-3.5 text-amber-500 animate-pulse" />
                            <span>Kiểm Duyệt & Bảo Chứng Pháp Nhân</span>
                            <span className="h-2 w-px bg-indigo-300" />
                            <span className="text-indigo-600 font-extrabold">TalentBridge Enterprise</span>
                        </div>

                        <h1 className="text-2xl sm:text-3xl lg:text-4xl font-black tracking-tight text-slate-900 leading-tight">
                            Thẩm định & phê duyệt doanh nghiệp
                        </h1>

                        <p className="text-sm sm:text-base font-medium text-slate-600 leading-relaxed">
                            Tra cứu mã số thuế (MST), kiểm định giấy tờ thành lập doanh nghiệp, bảo chứng uy tín thương hiệu tuyển dụng và phê chuẩn quyền truy cập cổng HR.
                        </p>

                        <div className="pt-1 flex flex-wrap items-center gap-3">
                            <button
                                className="btn-bento-secondary inline-flex items-center gap-2 text-xs sm:text-sm font-bold shadow-xs hover:border-indigo-300 hover:text-indigo-600 transition"
                                disabled={isFetching}
                                onClick={() => refetch()}
                                type="button"
                            >
                                <RefreshCw className={isFetching ? 'animate-spin text-indigo-600' : 'text-slate-500'} size={16} />
                                <span>{isFetching ? 'Đang cập nhật...' : 'Làm mới dữ liệu'}</span>
                            </button>

                            <div className="inline-flex items-center gap-2 text-xs font-semibold text-slate-500 bg-white/80 border border-slate-200 rounded-xl px-3 py-2">
                                <span className="size-2 rounded-full bg-emerald-500 animate-pulse" />
                                <span>Quy trình thẩm định: Tuân thủ 100% tiêu chuẩn pháp nhân</span>
                            </div>
                        </div>
                    </div>

                    <div className="hidden lg:flex items-center justify-end shrink-0">
                        <AdminControlCenterIllustration className="w-80 h-auto drop-shadow-md" />
                    </div>
                </div>
            </div>

            {/* Notification Messages */}
            {successMessage && (
                <div className="bento-card flex items-center gap-3 rounded-2xl border border-emerald-300/80 bg-emerald-50/90 px-5 py-4 text-sm sm:text-base font-bold text-emerald-900 shadow-sm animate-in fade-in slide-in-from-top-2">
                    <CheckCircle2 className="size-5 shrink-0 text-emerald-600" />
                    <span>{successMessage}</span>
                </div>
            )}
            {errorMessage && (
                <div className="bento-card flex items-center gap-3 rounded-2xl border border-rose-300/80 bg-rose-50/90 px-5 py-4 text-sm sm:text-base font-bold text-rose-900 shadow-sm animate-in fade-in slide-in-from-top-2">
                    <AlertCircle className="size-5 shrink-0 text-rose-600" />
                    <span>{errorMessage}</span>
                </div>
            )}

            {/* 2. Bento Stat Cards Grid (4 Thẻ Chỉ Số Bento với Soft 3D Badges) */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-5">
                {/* Bento Stat Card 1: Chờ Phê Duyệt */}
                <div
                    className={`bento-card group p-5 sm:p-6 border transition-all duration-300 shadow-sm cursor-pointer ${
                        activeTab === 'PENDING'
                            ? 'border-amber-400/90 bg-amber-50/20 ring-2 ring-amber-400/30'
                            : 'border-slate-200/90 bg-white hover:border-amber-300/80'
                    }`}
                    onClick={() => handleTabChange('PENDING')}
                >
                    <div className="flex items-start justify-between">
                        <div className="space-y-1">
                            <span className="text-xs sm:text-sm font-bold uppercase tracking-wider text-amber-700">
                                Chờ thẩm định
                            </span>
                            <div className="text-3xl sm:text-4xl font-black tracking-tight text-amber-600 flex items-center gap-2">
                                {pendingCount}
                                <span className="size-2 rounded-full bg-amber-500 animate-pulse" />
                            </div>
                        </div>
                        <Soft3DVerifiedBadge className="size-12 shrink-0 group-hover:scale-105 transition-transform duration-300" />
                    </div>
                    <div className="mt-4 flex items-center gap-2 text-xs font-semibold text-amber-800 border-t border-amber-100 pt-3">
                        <Clock size={14} className="text-amber-600 shrink-0" />
                        <span>Hồ sơ cần quản trị viên xem xét</span>
                    </div>
                </div>

                {/* Bento Stat Card 2: Đã Phê Duyệt */}
                <div
                    className={`bento-card group p-5 sm:p-6 border transition-all duration-300 shadow-sm cursor-pointer ${
                        activeTab === 'APPROVED'
                            ? 'border-emerald-400/90 bg-emerald-50/20 ring-2 ring-emerald-400/30'
                            : 'border-slate-200/90 bg-white hover:border-emerald-300/80'
                    }`}
                    onClick={() => handleTabChange('APPROVED')}
                >
                    <div className="flex items-start justify-between">
                        <div className="space-y-1">
                            <span className="text-xs sm:text-sm font-bold uppercase tracking-wider text-slate-500">
                                Đã xác thực
                            </span>
                            <div className="text-3xl sm:text-4xl font-black tracking-tight text-emerald-600">
                                {approvedCount}
                            </div>
                        </div>
                        <Soft3DActiveBadge className="size-12 shrink-0 group-hover:scale-105 transition-transform duration-300" />
                    </div>
                    <div className="mt-4 flex items-center gap-2 text-xs font-semibold text-emerald-700 border-t border-slate-100 pt-3">
                        <Check size={14} className="text-emerald-600 shrink-0" />
                        <span>Doanh nghiệp bảo chứng hoạt động</span>
                    </div>
                </div>

                {/* Bento Stat Card 3: Bị Từ Chối */}
                <div
                    className={`bento-card group p-5 sm:p-6 border transition-all duration-300 shadow-sm cursor-pointer ${
                        activeTab === 'REJECTED'
                            ? 'border-rose-400/90 bg-rose-50/20 ring-2 ring-rose-400/30'
                            : 'border-slate-200/90 bg-white hover:border-rose-300/80'
                    }`}
                    onClick={() => handleTabChange('REJECTED')}
                >
                    <div className="flex items-start justify-between">
                        <div className="space-y-1">
                            <span className="text-xs sm:text-sm font-bold uppercase tracking-wider text-slate-500">
                                Bị từ chối
                            </span>
                            <div className="text-3xl sm:text-4xl font-black tracking-tight text-rose-600">
                                {rejectedCount}
                            </div>
                        </div>
                        <Soft3DShieldBadge className="size-12 shrink-0 group-hover:scale-105 transition-transform duration-300" />
                    </div>
                    <div className="mt-4 flex items-center gap-2 text-xs font-semibold text-slate-500 border-t border-slate-100 pt-3">
                        <XCircle size={14} className="text-rose-500 shrink-0" />
                        <span>Hồ sơ chưa đạt tiêu chuẩn pháp lý</span>
                    </div>
                </div>

                {/* Bento Stat Card 4: Tổng Doanh Nghiệp */}
                <div
                    className={`bento-card group p-5 sm:p-6 border transition-all duration-300 shadow-sm cursor-pointer ${
                        activeTab === 'ALL'
                            ? 'border-indigo-400/90 bg-indigo-50/20 ring-2 ring-indigo-400/30'
                            : 'border-slate-200/90 bg-white hover:border-sky-300/80'
                    }`}
                    onClick={() => handleTabChange('ALL')}
                >
                    <div className="flex items-start justify-between">
                        <div className="space-y-1">
                            <span className="text-xs sm:text-sm font-bold uppercase tracking-wider text-slate-500">
                                Tổng doanh nghiệp
                            </span>
                            <div className="text-3xl sm:text-4xl font-black tracking-tight text-sky-600">
                                {totalCompanies.toLocaleString('vi-VN')}
                            </div>
                        </div>
                        <Soft3DBuildingBadge className="size-12 shrink-0 group-hover:scale-105 transition-transform duration-300" />
                    </div>
                    <div className="mt-4 flex items-center gap-2 text-xs font-semibold text-sky-700 border-t border-slate-100 pt-3">
                        <Building2 size={14} className="text-sky-600 shrink-0" />
                        <span>Toàn bộ hồ sơ trong cơ sở dữ liệu</span>
                    </div>
                </div>
            </div>

            {/* 3. Bento Status Tab Capsule & Search Bar */}
            <div className="bento-card border border-slate-200/90 bg-white p-5 sm:p-6 shadow-sm space-y-4">
                {/* Segmented Status Capsule Dock */}
                <div className="flex flex-wrap items-center gap-2">
                    <button
                        className={`inline-flex items-center gap-2 rounded-xl px-4 py-2.5 text-xs sm:text-sm font-bold transition-all ${
                            activeTab === 'PENDING'
                                ? 'bg-amber-500 text-white shadow-sm shadow-amber-900/30 font-black'
                                : 'bg-slate-100/90 text-slate-700 hover:bg-slate-200/80'
                        }`}
                        onClick={() => handleTabChange('PENDING')}
                        type="button"
                    >
                        <Clock size={16} />
                        <span>Chờ phê duyệt</span>
                        {companiesPage && (
                            <span className={`rounded-full px-2 py-0.5 text-xs font-extrabold ${
                                activeTab === 'PENDING' ? 'bg-white/25 text-white' : 'bg-amber-100 text-amber-800'
                            }`}>
                                {activeTab === 'PENDING' ? companiesPage.totalElements : pendingCount}
                            </span>
                        )}
                    </button>

                    <button
                        className={`inline-flex items-center gap-2 rounded-xl px-4 py-2.5 text-xs sm:text-sm font-bold transition-all ${
                            activeTab === 'APPROVED'
                                ? 'bg-emerald-600 text-white shadow-sm shadow-emerald-900/30 font-black'
                                : 'bg-slate-100/90 text-slate-700 hover:bg-slate-200/80'
                        }`}
                        onClick={() => handleTabChange('APPROVED')}
                        type="button"
                    >
                        <Check size={16} />
                        <span>Đã xác thực</span>
                    </button>

                    <button
                        className={`inline-flex items-center gap-2 rounded-xl px-4 py-2.5 text-xs sm:text-sm font-bold transition-all ${
                            activeTab === 'REJECTED'
                                ? 'bg-rose-600 text-white shadow-sm shadow-rose-900/30 font-black'
                                : 'bg-slate-100/90 text-slate-700 hover:bg-slate-200/80'
                        }`}
                        onClick={() => handleTabChange('REJECTED')}
                        type="button"
                    >
                        <XCircle size={16} />
                        <span>Bị từ chối</span>
                    </button>

                    <button
                        className={`inline-flex items-center gap-2 rounded-xl px-4 py-2.5 text-xs sm:text-sm font-bold transition-all ${
                            activeTab === 'ALL'
                                ? 'bg-indigo-600 text-white shadow-sm shadow-indigo-900/30 font-black'
                                : 'bg-slate-100/90 text-slate-700 hover:bg-slate-200/80'
                        }`}
                        onClick={() => handleTabChange('ALL')}
                        type="button"
                    >
                        <Building2 size={16} />
                        <span>Tất cả hồ sơ</span>
                    </button>
                </div>

                {/* Search Bar */}
                <form className="flex flex-col gap-4 sm:flex-row sm:items-center" onSubmit={handleSearchSubmit}>
                    <div className="relative flex-1">
                        <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size={20} />
                        <input
                            className="w-full rounded-xl border border-slate-300 bg-slate-50/50 py-3 pl-12 pr-10 text-base font-medium text-slate-900 placeholder:text-slate-400 focus:border-indigo-500 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500/20 transition-all"
                            onChange={(e) => setKeywordInput(e.target.value)}
                            placeholder="Tìm kiếm theo tên doanh nghiệp, mã số thuế (MST), địa chỉ trụ sở..."
                            type="text"
                            value={keywordInput}
                        />
                        {keywordInput && (
                            <button
                                className="absolute right-3 top-1/2 -translate-y-1/2 rounded-lg p-1 text-slate-400 hover:text-slate-600"
                                onClick={() => setKeywordInput('')}
                                type="button"
                            >
                                <X size={16} />
                            </button>
                        )}
                    </div>

                    <div className="flex items-center gap-3">
                        <button
                            className="btn-bento-primary px-5 py-2.5 text-xs sm:text-sm font-bold shadow-sm"
                            type="submit"
                        >
                            <Filter size={15} />
                            <span>Tìm kiếm</span>
                        </button>

                        {(activeKeyword || activeTab !== 'PENDING') && (
                            <button
                                className="rounded-xl border border-slate-200 px-3.5 py-2 text-xs sm:text-sm font-semibold text-slate-600 hover:bg-slate-50 transition"
                                onClick={handleResetFilters}
                                type="button"
                            >
                                Đặt lại
                            </button>
                        )}
                    </div>
                </form>

                {activeKeyword && (
                    <div className="flex items-center gap-2 border-t border-slate-100 pt-3 text-xs text-slate-600">
                        <span className="font-bold text-slate-500">Từ khóa tìm kiếm:</span>
                        <span className="inline-flex items-center gap-1.5 rounded-lg bg-indigo-50 border border-indigo-200 px-2.5 py-1 font-bold text-indigo-700">
                            "{activeKeyword}"
                            <X
                                className="cursor-pointer hover:text-indigo-900"
                                onClick={() => {
                                    setActiveKeyword('')
                                    setKeywordInput('')
                                }}
                                size={13}
                            />
                        </span>
                    </div>
                )}
            </div>

            {/* 4. Bento Data Surface Table */}
            <div className="bento-card overflow-hidden border border-slate-200/90 bg-white shadow-sm">
                <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between border-b border-slate-200 bg-slate-50/75 px-6 py-4">
                    <div className="flex items-center gap-2.5">
                        <h2 className="text-base sm:text-lg font-black text-slate-900">
                            {activeTab === 'PENDING' && 'Danh sách doanh nghiệp chờ phê duyệt'}
                            {activeTab === 'APPROVED' && 'Danh sách doanh nghiệp đã xác thực'}
                            {activeTab === 'REJECTED' && 'Danh sách hồ sơ doanh nghiệp bị từ chối'}
                            {activeTab === 'ALL' && 'Toàn bộ danh sách doanh nghiệp'}
                        </h2>
                        <span className="inline-flex items-center rounded-full bg-indigo-100 px-2.5 py-0.5 text-xs font-extrabold text-indigo-700">
                            {totalCompanies} kết quả
                        </span>
                    </div>

                    <div className="mt-1 sm:mt-0 text-xs font-semibold text-slate-500">
                        Trang {companiesPage?.pageNumber ?? 1} / {companiesPage?.totalPages || 1}
                    </div>
                </div>

                {isLoading ? (
                    <div className="flex h-72 items-center justify-center">
                        <div className="flex flex-col items-center gap-3 text-slate-500">
                            <RefreshCw className="animate-spin text-indigo-600" size={28} />
                            <span className="text-sm font-bold text-slate-700">Đang tải dữ liệu thẩm định doanh nghiệp...</span>
                        </div>
                    </div>
                ) : companiesPage?.content.length === 0 ? (
                    <div className="flex h-72 flex-col items-center justify-center p-8 text-center">
                        <EmptyStateIllustration className="w-32 h-auto mx-auto mb-4 drop-shadow-xs" />
                        <h3 className="text-lg font-black text-slate-800">Không có doanh nghiệp nào phù hợp</h3>
                        <p className="mt-1 max-w-md text-sm font-medium text-slate-500">
                            {activeTab === 'PENDING'
                                ? 'Hiện không có yêu cầu phê duyệt doanh nghiệp nào đang chờ xử lý.'
                                : 'Không tìm thấy kết quả phù hợp với điều kiện lọc.'}
                        </p>
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-left text-sm text-slate-600">
                            <thead className="border-b border-slate-200 bg-slate-100/90 text-xs sm:text-sm font-black uppercase tracking-wider text-slate-700">
                                <tr>
                                    <th className="px-6 py-4" scope="col">Doanh nghiệp</th>
                                    <th className="px-6 py-4" scope="col">Mã số thuế (MST)</th>
                                    <th className="px-6 py-4" scope="col">Địa chỉ & Quy mô</th>
                                    <th className="px-6 py-4" scope="col">Trạng thái thẩm định</th>
                                    <th className="px-6 py-4 text-right" scope="col">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-200/90">
                                {companiesPage?.content.map((company) => (
                                    <tr
                                        className="group hover:bg-indigo-50/20 transition-colors duration-150"
                                        key={company.id}
                                    >
                                        <td className="px-6 py-4 sm:py-5">
                                            <div className="flex items-center gap-3.5">
                                                {company.logoUrl ? (
                                                    <img
                                                        alt={company.name}
                                                        className="size-12 rounded-xl object-cover border border-slate-200/90 shadow-2xs group-hover:scale-105 transition-transform"
                                                        src={company.logoUrl}
                                                    />
                                                ) : (
                                                    <div className="grid size-12 place-items-center rounded-xl bg-gradient-to-tr from-indigo-600 to-violet-500 text-white font-black text-base shadow-2xs">
                                                        {company.name.charAt(0).toUpperCase()}
                                                    </div>
                                                )}
                                                <div>
                                                    <div className="text-base font-black text-slate-900 group-hover:text-indigo-600 transition-colors">
                                                        {company.name}
                                                    </div>
                                                    {company.website ? (
                                                        <a
                                                            className="inline-flex items-center gap-1 text-xs font-bold text-indigo-600 hover:underline mt-0.5"
                                                            href={company.website}
                                                            rel="noreferrer"
                                                            target="_blank"
                                                        >
                                                            <span>{company.website.replace(/^https?:\/\//, '')}</span>
                                                            <ExternalLink size={11} />
                                                        </a>
                                                    ) : (
                                                        <span className="text-xs font-semibold text-slate-400">Chưa thiết lập website</span>
                                                    )}
                                                </div>
                                            </div>
                                        </td>

                                        <td className="px-6 py-4 sm:py-5">
                                            <span className="font-mono text-xs sm:text-sm font-black text-slate-800 bg-slate-100 border border-slate-200 px-2.5 py-1 rounded-lg">
                                                {company.taxCode || 'Chưa cập nhật'}
                                            </span>
                                        </td>

                                        <td className="px-6 py-4 sm:py-5">
                                            <div className="flex flex-col gap-1 text-xs sm:text-sm">
                                                <div className="flex items-center gap-1.5 font-bold text-slate-800">
                                                    <MapPin size={14} className="text-rose-500 shrink-0" />
                                                    <span>{[company.address, company.city].filter(Boolean).join(', ') || 'Chưa cập nhật'}</span>
                                                </div>
                                                <div className="text-slate-500 font-semibold">
                                                    {company.companySize ? `Quy mô: ${company.companySize}` : 'Chưa cập nhật quy mô'}
                                                </div>
                                            </div>
                                        </td>

                                        <td className="px-6 py-4 sm:py-5">
                                            {company.status === 'APPROVED' && (
                                                <span className="inline-flex items-center gap-1.5 rounded-full bg-emerald-50 px-3 py-1.5 text-xs sm:text-sm font-bold text-emerald-700 border border-emerald-200 shadow-2xs">
                                                    <span className="size-2 rounded-full bg-emerald-500 animate-pulse" />
                                                    Đã xác thực
                                                </span>
                                            )}
                                            {company.status === 'PENDING' && (
                                                <span className="inline-flex items-center gap-1.5 rounded-full bg-amber-50 px-3 py-1.5 text-xs sm:text-sm font-bold text-amber-800 border border-amber-300 shadow-2xs">
                                                    <span className="size-2 rounded-full bg-amber-500 animate-pulse" />
                                                    Chờ phê duyệt
                                                </span>
                                            )}
                                            {company.status === 'REJECTED' && (
                                                <span className="inline-flex items-center gap-1.5 rounded-full bg-rose-50 px-3 py-1.5 text-xs sm:text-sm font-bold text-rose-700 border border-rose-200 shadow-2xs">
                                                    <span className="size-2 rounded-full bg-rose-500" />
                                                    Bị từ chối
                                                </span>
                                            )}
                                        </td>

                                        <td className="px-6 py-4 sm:py-5 text-right">
                                            <div className="inline-flex items-center gap-2">
                                                <button
                                                    className="btn-bento-secondary px-3 py-1.5 text-xs sm:text-sm font-bold shadow-2xs hover:bg-indigo-50 hover:text-indigo-700 hover:border-indigo-300"
                                                    onClick={() => handleViewDetail(company.id)}
                                                    type="button"
                                                >
                                                    Chi tiết
                                                </button>

                                                {company.status === 'PENDING' && (
                                                    <>
                                                        <button
                                                            className="inline-flex items-center gap-1 rounded-xl border border-emerald-200 bg-emerald-600 px-3 py-1.5 text-xs sm:text-sm font-black text-white shadow-2xs hover:bg-emerald-700 transition"
                                                            disabled={statusMutation.isPending}
                                                            onClick={() => setApprovingCompany(company)}
                                                            title="Phê duyệt doanh nghiệp"
                                                            type="button"
                                                        >
                                                            <Check size={14} />
                                                            <span>Duyệt</span>
                                                        </button>
                                                        <button
                                                            className="inline-flex items-center gap-1 rounded-xl border border-rose-200 bg-rose-50 px-3 py-1.5 text-xs sm:text-sm font-bold text-rose-700 transition hover:bg-rose-100 shadow-2xs"
                                                            disabled={statusMutation.isPending}
                                                            onClick={() => {
                                                                setRejectingCompany(company)
                                                                setRejectReason('')
                                                                setRejectInputError(null)
                                                            }}
                                                            title="Từ chối hồ sơ doanh nghiệp"
                                                            type="button"
                                                        >
                                                            <X size={14} />
                                                            <span>Từ chối</span>
                                                        </button>
                                                    </>
                                                )}

                                                {company.status === 'APPROVED' && (
                                                    <button
                                                        className="inline-flex items-center gap-1 rounded-xl border border-rose-200 bg-rose-50 px-3 py-1.5 text-xs sm:text-sm font-bold text-rose-700 transition hover:bg-rose-100 shadow-2xs"
                                                        disabled={statusMutation.isPending}
                                                        onClick={() => {
                                                            setRejectingCompany(company)
                                                            setRejectReason('')
                                                            setRejectInputError(null)
                                                        }}
                                                        title="Thu hồi quyết định duyệt"
                                                        type="button"
                                                    >
                                                        <span>Thu hồi</span>
                                                    </button>
                                                )}

                                                {company.status === 'REJECTED' && (
                                                    <button
                                                        className="inline-flex items-center gap-1 rounded-xl border border-emerald-200 bg-emerald-50 px-3 py-1.5 text-xs sm:text-sm font-bold text-emerald-700 transition hover:bg-emerald-100 shadow-2xs"
                                                        disabled={statusMutation.isPending}
                                                        onClick={() => setApprovingCompany(company)}
                                                        title="Phê duyệt lại hồ sơ"
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

                {/* 5. Bento Pagination Dock */}
                {companiesPage && companiesPage.totalPages > 0 && (
                    <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between border-t border-slate-200 bg-slate-50/90 px-6 py-4 text-xs sm:text-sm font-medium text-slate-600 gap-3">
                        <div>
                            Hiển thị trang <span className="font-extrabold text-slate-900">{companiesPage.pageNumber}</span> trên tổng số{' '}
                            <span className="font-extrabold text-slate-900">{companiesPage.totalPages}</span> trang (Tổng {companiesPage.totalElements} doanh nghiệp)
                        </div>
                        <div className="flex items-center gap-2">
                            <button
                                className="btn-bento-secondary px-3.5 py-1.5 text-xs sm:text-sm font-bold disabled:opacity-40 disabled:cursor-not-allowed"
                                disabled={companiesPage.pageNumber <= 1}
                                onClick={() => setPage((p) => Math.max(1, p - 1))}
                                type="button"
                            >
                                Trang trước
                            </button>
                            <span className="px-3 py-1.5 rounded-lg bg-indigo-600 text-white font-extrabold text-xs sm:text-sm shadow-2xs">
                                {companiesPage.pageNumber}
                            </span>
                            <button
                                className="btn-bento-secondary px-3.5 py-1.5 text-xs sm:text-sm font-bold disabled:opacity-40 disabled:cursor-not-allowed"
                                disabled={companiesPage.isLast || companiesPage.pageNumber >= companiesPage.totalPages}
                                onClick={() => setPage((p) => p + 1)}
                                type="button"
                            >
                                Trang sau
                            </button>
                        </div>
                    </div>
                )}
            </div>

            {/* 6. Bento Company Detail Modal */}
            {selectedCompany && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-sm p-4 animate-in fade-in">
                    <div className="bento-card relative w-full max-w-2xl border border-slate-200/90 bg-white p-6 sm:p-8 shadow-2xl max-h-[90vh] overflow-y-auto">
                        <button
                            className="absolute right-5 top-5 rounded-xl p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-700 transition"
                            onClick={() => setSelectedCompany(null)}
                            type="button"
                        >
                            <X size={20} />
                        </button>

                        <div className="flex items-start gap-4 sm:gap-5">
                            {selectedCompany.logoUrl ? (
                                <img
                                    alt={selectedCompany.name}
                                    className="size-16 sm:size-20 rounded-2xl object-cover border border-slate-200 shadow-sm"
                                    src={selectedCompany.logoUrl}
                                />
                            ) : (
                                <div className="grid size-16 sm:size-20 place-items-center rounded-2xl bg-gradient-to-tr from-indigo-600 to-violet-600 text-white font-black text-2xl shadow-sm">
                                    {selectedCompany.name.charAt(0).toUpperCase()}
                                </div>
                            )}
                            <div>
                                <h3 className="text-xl sm:text-2xl font-black text-slate-900">{selectedCompany.name}</h3>
                                <div className="mt-2 flex flex-wrap items-center gap-2">
                                    {selectedCompany.status === 'APPROVED' && (
                                        <span className="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-bold text-emerald-700 border border-emerald-200">
                                            <span className="size-1.5 rounded-full bg-emerald-500" />
                                            Đã xác thực pháp nhân
                                        </span>
                                    )}
                                    {selectedCompany.status === 'PENDING' && (
                                        <span className="inline-flex items-center gap-1 rounded-full bg-amber-50 px-2.5 py-1 text-xs font-bold text-amber-800 border border-amber-300">
                                            <span className="size-1.5 rounded-full bg-amber-500" />
                                            Đang chờ duyệt
                                        </span>
                                    )}
                                    {selectedCompany.status === 'REJECTED' && (
                                        <span className="inline-flex items-center gap-1 rounded-full bg-rose-50 px-2.5 py-1 text-xs font-bold text-rose-700 border border-rose-200">
                                            <span className="size-1.5 rounded-full bg-rose-500" />
                                            Bị từ chối hồ sơ
                                        </span>
                                    )}
                                    <span className="text-xs font-semibold text-slate-500 bg-slate-100 px-2.5 py-1 rounded-md">
                                        Mã công ty: #{selectedCompany.id}
                                    </span>
                                </div>
                            </div>
                        </div>

                        <div className="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-2 border-t border-slate-100 pt-5 text-sm bg-slate-50/70 p-4 rounded-xl border border-slate-200/60">
                            <div className="space-y-3">
                                <div>
                                    <span className="font-semibold text-slate-500">Mã số thuế (MST):</span>{' '}
                                    <span className="font-mono font-black text-slate-900">{selectedCompany.taxCode || 'Chưa cập nhật'}</span>
                                </div>
                                <div>
                                    <span className="font-semibold text-slate-500">Website:</span>{' '}
                                    {selectedCompany.website ? (
                                        <a
                                            className="text-indigo-600 font-bold hover:underline inline-flex items-center gap-1"
                                            href={selectedCompany.website}
                                            rel="noreferrer"
                                            target="_blank"
                                        >
                                            <span>{selectedCompany.website}</span>
                                            <ExternalLink size={12} />
                                        </a>
                                    ) : (
                                        <span className="text-slate-900">Chưa cập nhật</span>
                                    )}
                                </div>
                                <div>
                                    <span className="font-semibold text-slate-500">Quy mô nhân sự:</span>{' '}
                                    <span className="font-bold text-slate-900">{selectedCompany.companySize || 'Chưa cập nhật'}</span>
                                </div>
                            </div>

                            <div className="space-y-3">
                                <div>
                                    <span className="font-semibold text-slate-500">Địa chỉ:</span>{' '}
                                    <span className="font-bold text-slate-900">{selectedCompany.address || 'Chưa cập nhật'}</span>
                                </div>
                                <div>
                                    <span className="font-semibold text-slate-500">Tỉnh / Thành phố:</span>{' '}
                                    <span className="font-bold text-slate-900">{selectedCompany.city || 'Chưa cập nhật'}</span>
                                </div>
                                <div>
                                    <span className="font-semibold text-slate-500">ID Người đại diện:</span>{' '}
                                    <span className="font-mono font-bold text-slate-900">#{selectedCompany.createdByUserId || 'N/A'}</span>
                                </div>
                            </div>
                        </div>

                        {selectedCompany.description && (
                            <div className="mt-4 rounded-xl bg-indigo-50/40 p-4 text-sm border border-indigo-100">
                                <div className="font-black text-indigo-900 mb-1">Mô tả doanh nghiệp & Lĩnh vực hoạt động:</div>
                                <p className="leading-relaxed whitespace-pre-wrap font-medium text-slate-700">{selectedCompany.description}</p>
                            </div>
                        )}

                        <div className="mt-6 flex justify-end">
                            <button
                                className="btn-bento-secondary px-5 py-2 text-sm font-bold"
                                onClick={() => setSelectedCompany(null)}
                                type="button"
                            >
                                Đóng cửa sổ
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* 7. Bento Approve Confirmation Modal */}
            {approvingCompany && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-sm p-4 animate-in fade-in">
                    <div className="bento-card w-full max-w-md border border-slate-200/90 bg-white p-6 sm:p-7 shadow-2xl">
                        <div className="flex items-center gap-3.5">
                            <div className="grid size-12 place-items-center rounded-2xl bg-emerald-100 text-emerald-600">
                                <Check size={24} />
                            </div>
                            <div>
                                <h3 className="text-lg font-black text-slate-900">Xác nhận phê duyệt pháp nhân</h3>
                                <p className="text-xs font-semibold text-slate-500">{approvingCompany.name}</p>
                            </div>
                        </div>

                        <p className="mt-4 text-xs sm:text-sm text-slate-600 leading-relaxed bg-slate-50 rounded-xl p-3.5 border border-slate-200/60 font-medium">
                            Khi hoàn tất phê duyệt, doanh nghiệp sẽ được cấp trạng thái <strong>APPROVED</strong>.
                            Các chuyên viên nhân sự của công ty sẽ chính thức được kích hoạt quyền đăng tin tuyển dụng và tìm kiếm hồ sơ ứng viên TopCV trên toàn hệ thống TalentBridge.
                        </p>

                        <div className="mt-6 flex justify-end gap-3">
                            <button
                                className="btn-bento-secondary px-4 py-2 text-xs sm:text-sm font-bold"
                                disabled={statusMutation.isPending}
                                onClick={() => setApprovingCompany(null)}
                                type="button"
                            >
                                Hủy bỏ
                            </button>
                            <button
                                className="rounded-xl bg-emerald-600 px-5 py-2 text-xs sm:text-sm font-black text-white shadow-sm hover:bg-emerald-700 transition"
                                disabled={statusMutation.isPending}
                                onClick={handleConfirmApprove}
                                type="button"
                            >
                                {statusMutation.isPending ? 'Đang xử lý...' : 'Xác nhận phê duyệt'}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* 8. Bento Reject Modal with Reason Input */}
            {rejectingCompany && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-sm p-4 animate-in fade-in">
                    <div className="bento-card w-full max-w-md border border-slate-200/90 bg-white p-6 sm:p-7 shadow-2xl">
                        <div className="flex items-center gap-3.5">
                            <div className="grid size-12 place-items-center rounded-2xl bg-rose-100 text-rose-600">
                                <ShieldAlert size={24} />
                            </div>
                            <div>
                                <h3 className="text-lg font-black text-slate-900">Từ chối hồ sơ doanh nghiệp</h3>
                                <p className="text-xs font-semibold text-slate-500">{rejectingCompany.name}</p>
                            </div>
                        </div>

                        <div className="mt-4">
                            <label className="block text-xs font-bold text-slate-700 mb-1.5" htmlFor="company-reject-reason">
                                Lý do từ chối (bắt buộc) <span className="text-rose-600">*</span>
                            </label>
                            <textarea
                                className={`w-full rounded-xl border p-3 text-xs sm:text-sm text-slate-900 focus:outline-none focus:ring-2 ${
                                    rejectInputError
                                        ? 'border-rose-500 ring-rose-500/20'
                                        : 'border-slate-300 focus:border-rose-500 focus:ring-rose-500/20'
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
                                <p className="mt-1.5 flex items-center gap-1 text-xs font-bold text-rose-600">
                                    <AlertTriangle size={13} />
                                    <span>{rejectInputError}</span>
                                </p>
                            )}
                        </div>

                        <div className="mt-6 flex justify-end gap-3">
                            <button
                                className="btn-bento-secondary px-4 py-2 text-xs sm:text-sm font-bold"
                                disabled={statusMutation.isPending}
                                onClick={() => setRejectingCompany(null)}
                                type="button"
                            >
                                Hủy bỏ
                            </button>
                            <button
                                className="rounded-xl bg-rose-600 px-5 py-2 text-xs sm:text-sm font-black text-white shadow-sm hover:bg-rose-700 transition"
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
