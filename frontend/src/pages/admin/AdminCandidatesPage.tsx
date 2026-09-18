import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
    AlertCircle,
    Briefcase,
    CheckCircle,
    Code2,
    ExternalLink,
    Filter,
    Globe,
    Lock,
    Mail,
    MapPin,
    Phone,
    RefreshCw,
    Search,
    Share2,
    ShieldAlert,
    Sparkles,
    Unlock,
    UserCheck,
    Users,
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
import {
    AdminControlCenterIllustration,
    EmptyStateIllustration,
    Soft3DActiveBadge,
    Soft3DShieldBadge,
    Soft3DUserBadge,
    Soft3DVerifiedBadge,
} from '../../components/illustrations'

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
            queryClient.invalidateQueries({ queryKey: ['admin-dashboard-stats'] })
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

    function handleResetFilters() {
        setKeywordInput('')
        setActiveKeyword('')
        setStatusFilter('')
        setPage(1)
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

    // Metric calculations
    const totalCandidates = candidatesPage?.totalElements ?? 0
    const activeCandidatesOnPage = candidatesPage?.content.filter((c) => c.status === 'ACTIVE').length ?? 0
    const bannedCandidatesOnPage = candidatesPage?.content.filter((c) => c.status === 'BANNED').length ?? 0
    const experiencedCandidatesOnPage = candidatesPage?.content.filter((c) => (c.experienceYears ?? 0) > 0).length ?? 0

    return (
        <div className="space-y-6 sm:space-y-8 pb-12">
            {/* 1. Bento Hero Command Banner */}
            <div className="bento-card relative overflow-hidden border border-slate-200/90 bg-gradient-to-br from-white via-indigo-50/25 to-violet-50/35 p-6 sm:p-8 shadow-sm">
                {/* Ambient Decorative Accents */}
                <div className="pointer-events-none absolute -right-12 -top-12 size-64 rounded-full bg-indigo-200/30 blur-2xl" />
                <div className="pointer-events-none absolute bottom-0 right-1/4 size-48 rounded-full bg-amber-200/20 blur-2xl" />

                <div className="relative z-10 flex flex-col gap-6 lg:flex-row lg:items-center lg:justify-between">
                    <div className="max-w-2xl space-y-3">
                        <div className="inline-flex items-center gap-2 rounded-full border border-indigo-200/80 bg-indigo-50/90 px-3.5 py-1 text-xs font-bold uppercase tracking-wider text-indigo-700 shadow-2xs">
                            <Sparkles className="size-3.5 text-amber-500 animate-pulse" />
                            <span>Hệ Thống Quản Trị Nhân Tài ATS</span>
                            <span className="h-2 w-px bg-indigo-300" />
                            <span className="text-indigo-600 font-extrabold">TalentBridge 2026</span>
                        </div>

                        <h1 className="text-2xl sm:text-3xl lg:text-4xl font-black tracking-tight text-slate-900 leading-tight">
                            Quản lý hồ sơ & quyền truy cập ứng viên
                        </h1>

                        <p className="text-sm sm:text-base font-medium text-slate-600 leading-relaxed">
                            Tra cứu cơ sở dữ liệu nhân sự công nghệ số hóa, rà soát hồ sơ theo chuẩn TopCV, kiểm soát phân quyền tài khoản và đồng bộ an ninh hệ thống thời gian thực.
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
                                <span>Cổng kết nối CSDL 3NF: Hoạt động bình thường</span>
                            </div>
                        </div>
                    </div>

                    {/* Soft 3D Flat Vector Hero Graphic */}
                    <div className="hidden lg:flex items-center justify-end shrink-0">
                        <AdminControlCenterIllustration className="w-80 h-auto drop-shadow-md" />
                    </div>
                </div>
            </div>

            {/* Notification Alerts */}
            {successMessage && (
                <div className="bento-card flex items-center gap-3 rounded-2xl border border-emerald-300/80 bg-emerald-50/90 px-5 py-4 text-sm sm:text-base font-bold text-emerald-900 shadow-sm animate-in fade-in slide-in-from-top-2">
                    <CheckCircle className="size-5 shrink-0 text-emerald-600" />
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
                {/* Bento Stat Card 1: Tổng Ứng Viên */}
                <div className="bento-card group p-5 sm:p-6 border border-slate-200/90 bg-white hover:border-indigo-300/80 transition-all duration-300 shadow-sm">
                    <div className="flex items-start justify-between">
                        <div className="space-y-1">
                            <span className="text-xs sm:text-sm font-bold uppercase tracking-wider text-slate-500">
                                Tổng số ứng viên
                            </span>
                            <div className="text-3xl sm:text-4xl font-black tracking-tight text-slate-900">
                                {totalCandidates.toLocaleString('vi-VN')}
                            </div>
                        </div>
                        <Soft3DUserBadge className="size-12 shrink-0 group-hover:scale-105 transition-transform duration-300" />
                    </div>
                    <div className="mt-4 flex items-center gap-2 text-xs font-semibold text-slate-500 border-t border-slate-100 pt-3">
                        <Users size={14} className="text-indigo-600 shrink-0" />
                        <span>Toàn bộ người tìm việc đã đăng ký</span>
                    </div>
                </div>

                {/* Bento Stat Card 2: Đang Hoạt Động */}
                <div className="bento-card group p-5 sm:p-6 border border-slate-200/90 bg-white hover:border-emerald-300/80 transition-all duration-300 shadow-sm">
                    <div className="flex items-start justify-between">
                        <div className="space-y-1">
                            <span className="text-xs sm:text-sm font-bold uppercase tracking-wider text-slate-500">
                                Đang hoạt động
                            </span>
                            <div className="text-3xl sm:text-4xl font-black tracking-tight text-emerald-600">
                                {activeCandidatesOnPage}
                                <span className="text-base sm:text-lg font-bold text-slate-400"> / {candidatesPage?.content.length ?? 0}</span>
                            </div>
                        </div>
                        <Soft3DActiveBadge className="size-12 shrink-0 group-hover:scale-105 transition-transform duration-300" />
                    </div>
                    <div className="mt-4 flex items-center gap-2 text-xs font-semibold text-emerald-700 border-t border-slate-100 pt-3">
                        <UserCheck size={14} className="text-emerald-600 shrink-0" />
                        <span>Sẵn sàng kết nối cơ hội việc làm</span>
                    </div>
                </div>

                {/* Bento Stat Card 3: Bị Khóa / Giới Hạn */}
                <div className="bento-card group p-5 sm:p-6 border border-slate-200/90 bg-white hover:border-rose-300/80 transition-all duration-300 shadow-sm">
                    <div className="flex items-start justify-between">
                        <div className="space-y-1">
                            <span className="text-xs sm:text-sm font-bold uppercase tracking-wider text-slate-500">
                                Bị khóa truy cập
                            </span>
                            <div className="text-3xl sm:text-4xl font-black tracking-tight text-rose-600">
                                {bannedCandidatesOnPage}
                            </div>
                        </div>
                        <Soft3DShieldBadge className="size-12 shrink-0 group-hover:scale-105 transition-transform duration-300" />
                    </div>
                    <div className="mt-4 flex items-center gap-2 text-xs font-semibold text-slate-500 border-t border-slate-100 pt-3">
                        <ShieldAlert size={14} className="text-rose-500 shrink-0" />
                        <span>Hạn chế do vi phạm quy chế</span>
                    </div>
                </div>

                {/* Bento Stat Card 4: Hồ Sơ Có Kinh Nghiệm */}
                <div className="bento-card group p-5 sm:p-6 border border-slate-200/90 bg-white hover:border-amber-300/80 transition-all duration-300 shadow-sm">
                    <div className="flex items-start justify-between">
                        <div className="space-y-1">
                            <span className="text-xs sm:text-sm font-bold uppercase tracking-wider text-slate-500">
                                Có kinh nghiệm IT
                            </span>
                            <div className="text-3xl sm:text-4xl font-black tracking-tight text-amber-600">
                                {experiencedCandidatesOnPage}
                                <span className="text-base sm:text-lg font-bold text-slate-400"> hồ sơ</span>
                            </div>
                        </div>
                        <Soft3DVerifiedBadge className="size-12 shrink-0 group-hover:scale-105 transition-transform duration-300" />
                    </div>
                    <div className="mt-4 flex items-center gap-2 text-xs font-semibold text-amber-700 border-t border-slate-100 pt-3">
                        <Briefcase size={14} className="text-amber-600 shrink-0" />
                        <span>Đã xác minh năng lực chuyên môn</span>
                    </div>
                </div>
            </div>

            {/* 3. Bento Search & Quick Filter Capsule */}
            <div className="bento-card border border-slate-200/90 bg-white p-5 sm:p-6 shadow-sm">
                <form className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between" onSubmit={handleSearchSubmit}>
                    {/* Search Input Field */}
                    <div className="relative flex-1">
                        <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size={20} />
                        <input
                            className="w-full rounded-xl border border-slate-300 bg-slate-50/50 py-3 pl-12 pr-10 text-base font-medium text-slate-900 placeholder:text-slate-400 focus:border-indigo-500 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500/20 transition-all"
                            onChange={(e) => setKeywordInput(e.target.value)}
                            placeholder="Tìm kiếm theo họ tên, email, chức danh công nghệ hoặc thành phố..."
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

                    {/* Segmented Filter Pills & Submit Button */}
                    <div className="flex flex-wrap items-center gap-3">
                        {/* Status Tabs */}
                        <div className="inline-flex rounded-xl bg-slate-100/90 p-1 border border-slate-200/80">
                            <button
                                className={`rounded-lg px-3.5 py-2 text-xs sm:text-sm font-bold transition-all ${
                                    statusFilter === ''
                                        ? 'bg-white text-indigo-700 shadow-2xs font-extrabold'
                                        : 'text-slate-600 hover:text-slate-900'
                                }`}
                                onClick={() => {
                                    setStatusFilter('')
                                    setPage(1)
                                }}
                                type="button"
                            >
                                Tất cả
                            </button>
                            <button
                                className={`inline-flex items-center gap-1.5 rounded-lg px-3.5 py-2 text-xs sm:text-sm font-bold transition-all ${
                                    statusFilter === 'ACTIVE'
                                        ? 'bg-white text-emerald-700 shadow-2xs font-extrabold'
                                        : 'text-slate-600 hover:text-emerald-700'
                                }`}
                                onClick={() => {
                                    setStatusFilter('ACTIVE')
                                    setPage(1)
                                }}
                                type="button"
                            >
                                <span className="size-1.5 rounded-full bg-emerald-500" />
                                Hoạt động
                            </button>
                            <button
                                className={`inline-flex items-center gap-1.5 rounded-lg px-3.5 py-2 text-xs sm:text-sm font-bold transition-all ${
                                    statusFilter === 'BANNED'
                                        ? 'bg-white text-rose-700 shadow-2xs font-extrabold'
                                        : 'text-slate-600 hover:text-rose-700'
                                }`}
                                onClick={() => {
                                    setStatusFilter('BANNED')
                                    setPage(1)
                                }}
                                type="button"
                            >
                                <span className="size-1.5 rounded-full bg-rose-500" />
                                Bị khóa
                            </button>
                        </div>

                        {/* Search Submit Button */}
                        <button
                            className="btn-bento-primary px-5 py-2.5 text-xs sm:text-sm font-bold shadow-sm"
                            type="submit"
                        >
                            <Filter size={15} />
                            <span>Tìm kiếm</span>
                        </button>

                        {(activeKeyword || statusFilter) && (
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

                {/* Filter tags feedback */}
                {(activeKeyword || statusFilter) && (
                    <div className="mt-4 flex flex-wrap items-center gap-2 border-t border-slate-100 pt-3 text-xs text-slate-600">
                        <span className="font-bold text-slate-500">Bộ lọc đang áp dụng:</span>
                        {activeKeyword && (
                            <span className="inline-flex items-center gap-1.5 rounded-lg bg-indigo-50 border border-indigo-200 px-2.5 py-1 font-bold text-indigo-700">
                                Từ khóa: "{activeKeyword}"
                                <X
                                    className="cursor-pointer hover:text-indigo-900"
                                    onClick={() => {
                                        setActiveKeyword('')
                                        setKeywordInput('')
                                    }}
                                    size={13}
                                />
                            </span>
                        )}
                        {statusFilter && (
                            <span className="inline-flex items-center gap-1.5 rounded-lg bg-slate-100 border border-slate-300 px-2.5 py-1 font-bold text-slate-700">
                                Trạng thái: {statusFilter === 'ACTIVE' ? 'Đang hoạt động' : 'Bị khóa'}
                                <X
                                    className="cursor-pointer hover:text-slate-900"
                                    onClick={() => setStatusFilter('')}
                                    size={13}
                                />
                            </span>
                        )}
                    </div>
                )}
            </div>

            {/* 4. Bento Data Surface Table */}
            <div className="bento-card overflow-hidden border border-slate-200/90 bg-white shadow-sm">
                {/* Table Top Status Bar */}
                <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between border-b border-slate-200 bg-slate-50/75 px-6 py-4">
                    <div className="flex items-center gap-2.5">
                        <h2 className="text-base sm:text-lg font-black text-slate-900">
                            Danh sách hồ sơ ứng viên
                        </h2>
                        <span className="inline-flex items-center rounded-full bg-indigo-100 px-2.5 py-0.5 text-xs font-extrabold text-indigo-700">
                            {totalCandidates} kết quả
                        </span>
                    </div>

                    <div className="mt-1 sm:mt-0 text-xs font-semibold text-slate-500">
                        Trang {candidatesPage?.pageNumber ?? 1} / {candidatesPage?.totalPages || 1}
                    </div>
                </div>

                {isLoading ? (
                    <div className="flex h-72 items-center justify-center">
                        <div className="flex flex-col items-center gap-3 text-slate-500">
                            <RefreshCw className="animate-spin text-indigo-600" size={28} />
                            <span className="text-sm font-bold text-slate-700">Đang tải danh sách ứng viên từ hệ thống...</span>
                        </div>
                    </div>
                ) : candidatesPage?.content.length === 0 ? (
                    <div className="flex h-72 flex-col items-center justify-center p-8 text-center">
                        <EmptyStateIllustration className="w-32 h-auto mx-auto mb-4 drop-shadow-xs" />
                        <h3 className="text-lg font-black text-slate-800">Không tìm thấy ứng viên nào phù hợp</h3>
                        <p className="mt-1 max-w-md text-sm font-medium text-slate-500">
                            Thử điều chỉnh từ khóa tìm kiếm hoặc bấm nút "Đặt lại" để xem toàn bộ danh sách.
                        </p>
                        <button
                            className="mt-4 btn-bento-secondary text-xs font-bold"
                            onClick={handleResetFilters}
                            type="button"
                        >
                            Xóa bộ lọc tìm kiếm
                        </button>
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-left text-sm text-slate-600">
                            <thead className="border-b border-slate-200 bg-slate-100/90 text-xs sm:text-sm font-black uppercase tracking-wider text-slate-700">
                                <tr>
                                    <th className="px-6 py-4" scope="col">Ứng viên</th>
                                    <th className="px-6 py-4" scope="col">Thông tin liên hệ</th>
                                    <th className="px-6 py-4" scope="col">Địa điểm & Kinh nghiệm</th>
                                    <th className="px-6 py-4" scope="col">Trạng thái</th>
                                    <th className="px-6 py-4 text-right" scope="col">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-200/90">
                                {candidatesPage?.content.map((candidate) => (
                                    <tr
                                        className="group hover:bg-indigo-50/20 transition-colors duration-150"
                                        key={candidate.id}
                                    >
                                        {/* Candidate Profile */}
                                        <td className="px-6 py-4 sm:py-5">
                                            <div className="flex items-center gap-3.5">
                                                {candidate.avatarUrl ? (
                                                    <img
                                                        alt={candidate.fullName}
                                                        className="size-11 rounded-xl object-cover border border-slate-200/90 shadow-2xs group-hover:scale-105 transition-transform"
                                                        src={candidate.avatarUrl}
                                                    />
                                                ) : (
                                                    <div className="grid size-11 place-items-center rounded-xl bg-gradient-to-tr from-indigo-600 to-violet-500 text-white font-black text-sm shadow-2xs">
                                                        {candidate.fullName.charAt(0).toUpperCase()}
                                                    </div>
                                                )}
                                                <div>
                                                    <div className="text-base font-black text-slate-900 group-hover:text-indigo-600 transition-colors">
                                                        {candidate.fullName}
                                                    </div>
                                                    <div className="mt-0.5 inline-flex items-center gap-1 rounded-md bg-slate-100 px-2 py-0.5 text-xs font-semibold text-slate-700">
                                                        {candidate.title || 'Chưa cập nhật chức danh'}
                                                    </div>
                                                </div>
                                            </div>
                                        </td>

                                        {/* Contact Info */}
                                        <td className="px-6 py-4 sm:py-5">
                                            <div className="flex flex-col gap-1 text-xs sm:text-sm">
                                                <div className="flex items-center gap-2 font-medium text-slate-800">
                                                    <Mail size={14} className="text-indigo-600 shrink-0" />
                                                    <span className="truncate max-w-[200px]">{candidate.email}</span>
                                                </div>
                                                {candidate.phone && (
                                                    <div className="flex items-center gap-2 font-medium text-slate-600">
                                                        <Phone size={14} className="text-slate-400 shrink-0" />
                                                        <span>{candidate.phone}</span>
                                                    </div>
                                                )}
                                            </div>
                                        </td>

                                        {/* Location & Experience */}
                                        <td className="px-6 py-4 sm:py-5">
                                            <div className="flex flex-col gap-1 text-xs sm:text-sm">
                                                <div className="flex items-center gap-1.5 font-bold text-slate-900">
                                                    <MapPin size={14} className="text-rose-500 shrink-0" />
                                                    <span>{candidate.city || 'Chưa cập nhật'}</span>
                                                </div>
                                                <div className="inline-flex items-center gap-1 font-semibold text-slate-600">
                                                    <span className="rounded-md bg-amber-50 border border-amber-200 px-2 py-0.5 text-[11px] font-bold text-amber-800">
                                                        {candidate.experienceYears !== undefined && candidate.experienceYears !== null
                                                            ? `${candidate.experienceYears} năm kinh nghiệm`
                                                            : 'Mới tốt nghiệp / Chưa cập nhật'}
                                                    </span>
                                                </div>
                                            </div>
                                        </td>

                                        {/* Status */}
                                        <td className="px-6 py-4 sm:py-5">
                                            {candidate.status === 'ACTIVE' ? (
                                                <span className="inline-flex items-center gap-1.5 rounded-full bg-emerald-50 px-3 py-1.5 text-xs sm:text-sm font-bold text-emerald-700 border border-emerald-200 shadow-2xs">
                                                    <span className="size-2 rounded-full bg-emerald-500 animate-pulse" />
                                                    Hoạt động
                                                </span>
                                            ) : (
                                                <span className="inline-flex items-center gap-1.5 rounded-full bg-rose-50 px-3 py-1.5 text-xs sm:text-sm font-bold text-rose-700 border border-rose-200 shadow-2xs">
                                                    <span className="size-2 rounded-full bg-rose-500" />
                                                    Bị khóa
                                                </span>
                                            )}
                                        </td>

                                        {/* Actions */}
                                        <td className="px-6 py-4 sm:py-5 text-right">
                                            <div className="inline-flex items-center gap-2">
                                                <button
                                                    className="btn-bento-secondary px-3 py-1.5 text-xs sm:text-sm font-bold shadow-2xs hover:bg-indigo-50 hover:text-indigo-700 hover:border-indigo-300"
                                                    onClick={() => handleViewDetail(candidate.id)}
                                                    type="button"
                                                >
                                                    Chi tiết
                                                </button>

                                                {candidate.status === 'ACTIVE' ? (
                                                    <button
                                                        className="inline-flex items-center gap-1 rounded-xl border border-rose-200 bg-rose-50 px-3 py-1.5 text-xs sm:text-sm font-bold text-rose-700 transition hover:bg-rose-100 shadow-2xs"
                                                        onClick={() => handleOpenActionDialog(candidate, 'LOCK')}
                                                        title="Khóa tài khoản"
                                                        type="button"
                                                    >
                                                        <Lock size={14} />
                                                        <span>Khóa</span>
                                                    </button>
                                                ) : (
                                                    <button
                                                        className="inline-flex items-center gap-1 rounded-xl border border-emerald-200 bg-emerald-50 px-3 py-1.5 text-xs sm:text-sm font-bold text-emerald-700 transition hover:bg-emerald-100 shadow-2xs"
                                                        onClick={() => handleOpenActionDialog(candidate, 'UNLOCK')}
                                                        title="Mở khóa tài khoản"
                                                        type="button"
                                                    >
                                                        <Unlock size={14} />
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

                {/* 5. Bento Pagination Dock */}
                {candidatesPage && candidatesPage.totalPages > 0 && (
                    <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between border-t border-slate-200 bg-slate-50/90 px-6 py-4 text-xs sm:text-sm font-medium text-slate-600 gap-3">
                        <div>
                            Hiển thị trang <span className="font-extrabold text-slate-900">{candidatesPage.pageNumber}</span> trên tổng số{' '}
                            <span className="font-extrabold text-slate-900">{candidatesPage.totalPages}</span> trang (Tổng {candidatesPage.totalElements} ứng viên)
                        </div>
                        <div className="flex items-center gap-2">
                            <button
                                className="btn-bento-secondary px-3.5 py-1.5 text-xs sm:text-sm font-bold disabled:opacity-40 disabled:cursor-not-allowed"
                                disabled={candidatesPage.pageNumber <= 1}
                                onClick={() => setPage((p) => Math.max(1, p - 1))}
                                type="button"
                            >
                                Trang trước
                            </button>
                            <span className="px-3 py-1.5 rounded-lg bg-indigo-600 text-white font-extrabold text-xs sm:text-sm shadow-2xs">
                                {candidatesPage.pageNumber}
                            </span>
                            <button
                                className="btn-bento-secondary px-3.5 py-1.5 text-xs sm:text-sm font-bold disabled:opacity-40 disabled:cursor-not-allowed"
                                disabled={candidatesPage.isLast || candidatesPage.pageNumber >= candidatesPage.totalPages}
                                onClick={() => setPage((p) => p + 1)}
                                type="button"
                            >
                                Trang sau
                            </button>
                        </div>
                    </div>
                )}
            </div>

            {/* 6. Bento Candidate Detail Modal */}
            {selectedCandidate && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-sm p-4 animate-in fade-in">
                    <div className="bento-card relative w-full max-w-2xl border border-slate-200/90 bg-white p-6 sm:p-8 shadow-2xl max-h-[90vh] overflow-y-auto">
                        <button
                            className="absolute right-5 top-5 rounded-xl p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-700 transition"
                            onClick={() => setSelectedCandidate(null)}
                            type="button"
                        >
                            <X size={20} />
                        </button>

                        <div className="flex items-start gap-4 sm:gap-5">
                            {selectedCandidate.avatarUrl ? (
                                <img
                                    alt={selectedCandidate.fullName}
                                    className="size-16 sm:size-20 rounded-2xl object-cover border border-slate-200 shadow-sm"
                                    src={selectedCandidate.avatarUrl}
                                />
                            ) : (
                                <div className="grid size-16 sm:size-20 place-items-center rounded-2xl bg-gradient-to-tr from-indigo-600 to-violet-600 text-white font-black text-2xl shadow-sm">
                                    {selectedCandidate.fullName.charAt(0).toUpperCase()}
                                </div>
                            )}
                            <div>
                                <h3 className="text-xl sm:text-2xl font-black text-slate-900">
                                    {selectedCandidate.fullName}
                                </h3>
                                <p className="text-sm sm:text-base font-bold text-indigo-600 mt-0.5">
                                    {selectedCandidate.title || 'Chưa thiết lập chức danh công việc'}
                                </p>
                                <div className="mt-2 flex flex-wrap items-center gap-2">
                                    {selectedCandidate.status === 'ACTIVE' ? (
                                        <span className="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-bold text-emerald-700 border border-emerald-200">
                                            <span className="size-1.5 rounded-full bg-emerald-500" />
                                            Đang hoạt động
                                        </span>
                                    ) : (
                                        <span className="inline-flex items-center gap-1 rounded-full bg-rose-50 px-2.5 py-1 text-xs font-bold text-rose-700 border border-rose-200">
                                            <span className="size-1.5 rounded-full bg-rose-500" />
                                            Tài khoản bị khóa
                                        </span>
                                    )}
                                    <span className="text-xs font-semibold text-slate-500 bg-slate-100 px-2.5 py-1 rounded-md">
                                        Mã ứng viên: #{selectedCandidate.id} (User ID: #{selectedCandidate.userId})
                                    </span>
                                </div>
                            </div>
                        </div>

                        {/* Bento Details Grid */}
                        <div className="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-2 border-t border-slate-100 pt-5 text-sm">
                            <div className="space-y-3 rounded-xl bg-slate-50/70 p-4 border border-slate-200/60">
                                <h4 className="font-black text-xs uppercase tracking-wider text-slate-500">Thông tin liên hệ</h4>
                                <div>
                                    <span className="font-semibold text-slate-500">Email:</span>{' '}
                                    <span className="font-bold text-slate-900">{selectedCandidate.email}</span>
                                </div>
                                <div>
                                    <span className="font-semibold text-slate-500">Số điện thoại:</span>{' '}
                                    <span className="font-bold text-slate-900">{selectedCandidate.phone || 'Chưa cập nhật'}</span>
                                </div>
                                <div>
                                    <span className="font-semibold text-slate-500">Ngày sinh:</span>{' '}
                                    <span className="font-bold text-slate-900">{selectedCandidate.dob || 'Chưa cập nhật'}</span>
                                </div>
                                <div>
                                    <span className="font-semibold text-slate-500">Giới tính:</span>{' '}
                                    <span className="font-bold text-slate-900">{selectedCandidate.gender || 'Chưa cập nhật'}</span>
                                </div>
                            </div>

                            <div className="space-y-3 rounded-xl bg-slate-50/70 p-4 border border-slate-200/60">
                                <h4 className="font-black text-xs uppercase tracking-wider text-slate-500">Kinh nghiệm & Địa điểm</h4>
                                <div>
                                    <span className="font-semibold text-slate-500">Địa chỉ / Tỉnh thành:</span>{' '}
                                    <span className="font-bold text-slate-900">
                                        {[selectedCandidate.address, selectedCandidate.city].filter(Boolean).join(', ') || 'Chưa cập nhật'}
                                    </span>
                                </div>
                                <div>
                                    <span className="font-semibold text-slate-500">Kinh nghiệm:</span>{' '}
                                    <span className="font-bold text-slate-900">
                                        {selectedCandidate.experienceYears !== undefined && selectedCandidate.experienceYears !== null
                                            ? `${selectedCandidate.experienceYears} năm kinh nghiệm`
                                            : 'Chưa cập nhật'}
                                    </span>
                                </div>
                                <div>
                                    <span className="font-semibold text-slate-500">Mức lương kỳ vọng:</span>{' '}
                                    <span className="font-black text-emerald-600">
                                        {selectedCandidate.expectedSalary
                                            ? `${Number(selectedCandidate.expectedSalary).toLocaleString('vi-VN')} VNĐ`
                                            : 'Thương lượng'}
                                    </span>
                                </div>
                            </div>
                        </div>

                        {selectedCandidate.summary && (
                            <div className="mt-4 rounded-xl bg-indigo-50/40 p-4 text-sm border border-indigo-100">
                                <div className="font-black text-indigo-900 mb-1">Giới thiệu bản thân & Mục tiêu nghề nghiệp:</div>
                                <p className="leading-relaxed whitespace-pre-wrap font-medium text-slate-700">{selectedCandidate.summary}</p>
                            </div>
                        )}

                        <div className="mt-4 flex flex-wrap items-center gap-3 pt-2 text-xs sm:text-sm font-semibold">
                            {selectedCandidate.personalWebsite && (
                                <a
                                    className="inline-flex items-center gap-1.5 text-indigo-600 hover:text-indigo-800 bg-indigo-50 border border-indigo-200/80 rounded-lg px-3 py-1.5 transition"
                                    href={selectedCandidate.personalWebsite}
                                    rel="noreferrer"
                                    target="_blank"
                                >
                                    <Globe size={14} />
                                    <span>Website cá nhân</span>
                                    <ExternalLink size={12} />
                                </a>
                            )}
                            {selectedCandidate.linkedinUrl && (
                                <a
                                    className="inline-flex items-center gap-1.5 text-blue-700 hover:text-blue-900 bg-blue-50 border border-blue-200/80 rounded-lg px-3 py-1.5 transition"
                                    href={selectedCandidate.linkedinUrl}
                                    rel="noreferrer"
                                    target="_blank"
                                >
                                    <Share2 size={14} />
                                    <span>LinkedIn</span>
                                    <ExternalLink size={12} />
                                </a>
                            )}
                            {selectedCandidate.githubUrl && (
                                <a
                                    className="inline-flex items-center gap-1.5 text-slate-900 hover:text-indigo-700 bg-slate-100 border border-slate-300 rounded-lg px-3 py-1.5 transition"
                                    href={selectedCandidate.githubUrl}
                                    rel="noreferrer"
                                    target="_blank"
                                >
                                    <Code2 size={14} />
                                    <span>GitHub</span>
                                    <ExternalLink size={12} />
                                </a>
                            )}
                        </div>

                        <div className="mt-6 flex justify-end">
                            <button
                                className="btn-bento-secondary px-5 py-2 text-sm font-bold"
                                onClick={() => setSelectedCandidate(null)}
                                type="button"
                            >
                                Đóng cửa sổ
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* 7. Bento Lock / Unlock Confirmation Dialog */}
            {actionCandidate && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-sm p-4 animate-in fade-in">
                    <div className="bento-card w-full max-w-md border border-slate-200/90 bg-white p-6 sm:p-7 shadow-2xl">
                        <div className="flex items-center gap-3.5">
                            <div
                                className={`grid size-12 place-items-center rounded-2xl ${
                                    actionType === 'LOCK' ? 'bg-rose-100 text-rose-600' : 'bg-emerald-100 text-emerald-600'
                                }`}
                            >
                                {actionType === 'LOCK' ? <Lock size={22} /> : <Unlock size={22} />}
                            </div>
                            <div>
                                <h3 className="text-lg font-black text-slate-900">
                                    {actionType === 'LOCK' ? 'Khóa quyền truy cập ứng viên' : 'Mở khóa tài khoản ứng viên'}
                                </h3>
                                <p className="text-xs font-semibold text-slate-500">
                                    {actionCandidate.fullName} ({actionCandidate.email})
                                </p>
                            </div>
                        </div>

                        <div className="mt-4 text-xs sm:text-sm font-medium text-slate-600 leading-relaxed bg-slate-50 rounded-xl p-3.5 border border-slate-200/60">
                            {actionType === 'LOCK' ? (
                                <p>
                                    Khi kích hoạt khóa tài khoản, ứng viên sẽ ngay lập tức bị ngắt phiên đăng nhập và không thể nộp hồ sơ ứng tuyển việc làm trên TalentBridge.
                                </p>
                            ) : (
                                <p>
                                    Khi mở khóa tài khoản, ứng viên sẽ được khôi phục toàn bộ quyền truy cập và ứng tuyển việc làm bình thường.
                                </p>
                            )}
                        </div>

                        {actionType === 'LOCK' && (
                            <div className="mt-4">
                                <label className="block text-xs font-bold text-slate-700 mb-1.5" htmlFor="action-reason">
                                    Lý do khóa tài khoản (lưu nhật ký quản trị):
                                </label>
                                <textarea
                                    className="w-full rounded-xl border border-slate-300 p-3 text-xs sm:text-sm text-slate-900 focus:border-rose-500 focus:outline-none focus:ring-2 focus:ring-rose-500/20"
                                    id="action-reason"
                                    onChange={(e) => setActionReason(e.target.value)}
                                    placeholder="Nhập lý do kiểm duyệt vi phạm quy định nền tảng..."
                                    rows={3}
                                    value={actionReason}
                                />
                            </div>
                        )}

                        <div className="mt-6 flex justify-end gap-3">
                            <button
                                className="btn-bento-secondary px-4 py-2 text-xs sm:text-sm font-bold"
                                disabled={statusMutation.isPending}
                                onClick={() => setActionCandidate(null)}
                                type="button"
                            >
                                Hủy bỏ
                            </button>
                            <button
                                className={`rounded-xl px-5 py-2 text-xs sm:text-sm font-black text-white shadow-sm transition ${
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
                                    ? 'Xác nhận khóa tài khoản'
                                    : 'Xác nhận mở khóa'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    )
}
