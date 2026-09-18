import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
    AlertCircle,
    Building2,
    CheckCircle,
    Filter,
    Lock,
    Mail,
    Phone,
    RefreshCw,
    Search,
    ShieldAlert,
    Sparkles,
    Unlock,
    UserCheck,
    Users,
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
import {
    AdminControlCenterIllustration,
    EmptyStateIllustration,
    Soft3DActiveBadge,
    Soft3DBuildingBadge,
    Soft3DShieldBadge,
    Soft3DUserBadge,
} from '../../components/illustrations'

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

    function handleResetFilters() {
        setKeywordInput('')
        setActiveKeyword('')
        setPage(1)
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

    // Metric calculations
    const totalRecruiters = recruitersPage?.totalElements ?? 0
    const activeRecruitersOnPage = recruitersPage?.content.filter((r) => r.status === 'ACTIVE').length ?? 0
    const bannedRecruitersOnPage = recruitersPage?.content.filter((r) => r.status === 'BANNED').length ?? 0
    const affiliatedRecruitersOnPage = recruitersPage?.content.filter((r) => Boolean(r.companyName)).length ?? 0

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
                            <span>Mạng Lưới Nhà Tuyển Dụng & Đối Tác</span>
                            <span className="h-2 w-px bg-indigo-300" />
                            <span className="text-indigo-600 font-extrabold">TalentBridge HR</span>
                        </div>

                        <h1 className="text-2xl sm:text-3xl lg:text-4xl font-black tracking-tight text-slate-900 leading-tight">
                            Quản lý chuyên viên & nhà tuyển dụng (HR)
                        </h1>

                        <p className="text-sm sm:text-base font-medium text-slate-600 leading-relaxed">
                            Kiểm soát danh sách chuyên viên nhân sự, xác thực mối liên kết doanh nghiệp trực thuộc, theo dõi tiến độ săn đón nhân tài và duy trì uy tín thương hiệu tuyển dụng.
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
                                <span>Phân quyền RBAC HR: Hoạt động chuẩn xác</span>
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
                {/* Bento Stat Card 1: Tổng HR */}
                <div className="bento-card group p-5 sm:p-6 border border-slate-200/90 bg-white hover:border-indigo-300/80 transition-all duration-300 shadow-sm">
                    <div className="flex items-start justify-between">
                        <div className="space-y-1">
                            <span className="text-xs sm:text-sm font-bold uppercase tracking-wider text-slate-500">
                                Tổng chuyên viên HR
                            </span>
                            <div className="text-3xl sm:text-4xl font-black tracking-tight text-slate-900">
                                {totalRecruiters.toLocaleString('vi-VN')}
                            </div>
                        </div>
                        <Soft3DUserBadge className="size-12 shrink-0 group-hover:scale-105 transition-transform duration-300" />
                    </div>
                    <div className="mt-4 flex items-center gap-2 text-xs font-semibold text-slate-500 border-t border-slate-100 pt-3">
                        <Users size={14} className="text-indigo-600 shrink-0" />
                        <span>Tài khoản nhà tuyển dụng trong mạng lưới</span>
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
                                {activeRecruitersOnPage}
                                <span className="text-base sm:text-lg font-bold text-slate-400"> / {recruitersPage?.content.length ?? 0}</span>
                            </div>
                        </div>
                        <Soft3DActiveBadge className="size-12 shrink-0 group-hover:scale-105 transition-transform duration-300" />
                    </div>
                    <div className="mt-4 flex items-center gap-2 text-xs font-semibold text-emerald-700 border-t border-slate-100 pt-3">
                        <UserCheck size={14} className="text-emerald-600 shrink-0" />
                        <span>Có quyền đăng tin & lọc hồ sơ ứng viên</span>
                    </div>
                </div>

                {/* Bento Stat Card 3: Bị Khóa Quyền */}
                <div className="bento-card group p-5 sm:p-6 border border-slate-200/90 bg-white hover:border-rose-300/80 transition-all duration-300 shadow-sm">
                    <div className="flex items-start justify-between">
                        <div className="space-y-1">
                            <span className="text-xs sm:text-sm font-bold uppercase tracking-wider text-slate-500">
                                Tài khoản bị khóa
                            </span>
                            <div className="text-3xl sm:text-4xl font-black tracking-tight text-rose-600">
                                {bannedRecruitersOnPage}
                            </div>
                        </div>
                        <Soft3DShieldBadge className="size-12 shrink-0 group-hover:scale-105 transition-transform duration-300" />
                    </div>
                    <div className="mt-4 flex items-center gap-2 text-xs font-semibold text-slate-500 border-t border-slate-100 pt-3">
                        <ShieldAlert size={14} className="text-rose-500 shrink-0" />
                        <span>Tạm ngừng cấp quyền truy cập</span>
                    </div>
                </div>

                {/* Bento Stat Card 4: Đã Liên Kết Công Ty */}
                <div className="bento-card group p-5 sm:p-6 border border-slate-200/90 bg-white hover:border-sky-300/80 transition-all duration-300 shadow-sm">
                    <div className="flex items-start justify-between">
                        <div className="space-y-1">
                            <span className="text-xs sm:text-sm font-bold uppercase tracking-wider text-slate-500">
                                Đã liên kết công ty
                            </span>
                            <div className="text-3xl sm:text-4xl font-black tracking-tight text-sky-600">
                                {affiliatedRecruitersOnPage}
                                <span className="text-base sm:text-lg font-bold text-slate-400"> HR</span>
                            </div>
                        </div>
                        <Soft3DBuildingBadge className="size-12 shrink-0 group-hover:scale-105 transition-transform duration-300" />
                    </div>
                    <div className="mt-4 flex items-center gap-2 text-xs font-semibold text-sky-700 border-t border-slate-100 pt-3">
                        <Building2 size={14} className="text-sky-600 shrink-0" />
                        <span>Thuộc pháp nhân doanh nghiệp bảo chứng</span>
                    </div>
                </div>
            </div>

            {/* 3. Bento Search & Filter Capsule */}
            <div className="bento-card border border-slate-200/90 bg-white p-5 sm:p-6 shadow-sm">
                <form className="flex flex-col gap-4 sm:flex-row sm:items-center" onSubmit={handleSearchSubmit}>
                    <div className="relative flex-1">
                        <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size={20} />
                        <input
                            className="w-full rounded-xl border border-slate-300 bg-slate-50/50 py-3 pl-12 pr-10 text-base font-medium text-slate-900 placeholder:text-slate-400 focus:border-indigo-500 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500/20 transition-all"
                            onChange={(e) => setKeywordInput(e.target.value)}
                            placeholder="Tìm kiếm theo tên nhà tuyển dụng, email, hoặc tên công ty..."
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
                            <span>Tìm kiếm HR</span>
                        </button>

                        {activeKeyword && (
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
                    <div className="mt-4 flex items-center gap-2 border-t border-slate-100 pt-3 text-xs text-slate-600">
                        <span className="font-bold text-slate-500">Từ khóa lọc:</span>
                        <span className="inline-flex items-center gap-1.5 rounded-lg bg-indigo-50 border border-indigo-200 px-2.5 py-1 font-bold text-indigo-700">
                            "{activeKeyword}"
                            <X
                                className="cursor-pointer hover:text-indigo-900"
                                onClick={handleResetFilters}
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
                            Danh sách chuyên viên nhà tuyển dụng
                        </h2>
                        <span className="inline-flex items-center rounded-full bg-indigo-100 px-2.5 py-0.5 text-xs font-extrabold text-indigo-700">
                            {totalRecruiters} kết quả
                        </span>
                    </div>

                    <div className="mt-1 sm:mt-0 text-xs font-semibold text-slate-500">
                        Trang {recruitersPage?.pageNumber ?? 1} / {recruitersPage?.totalPages || 1}
                    </div>
                </div>

                {isLoading ? (
                    <div className="flex h-72 items-center justify-center">
                        <div className="flex flex-col items-center gap-3 text-slate-500">
                            <RefreshCw className="animate-spin text-indigo-600" size={28} />
                            <span className="text-sm font-bold text-slate-700">Đang tải danh sách nhà tuyển dụng...</span>
                        </div>
                    </div>
                ) : recruitersPage?.content.length === 0 ? (
                    <div className="flex h-72 flex-col items-center justify-center p-8 text-center">
                        <EmptyStateIllustration className="w-32 h-auto mx-auto mb-4 drop-shadow-xs" />
                        <h3 className="text-lg font-black text-slate-800">Không tìm thấy nhà tuyển dụng nào</h3>
                        <p className="mt-1 max-w-md text-sm font-medium text-slate-500">
                            Thử điều chỉnh từ khóa tìm kiếm để mở rộng kết quả hiển thị.
                        </p>
                        <button
                            className="mt-4 btn-bento-secondary text-xs font-bold"
                            onClick={handleResetFilters}
                            type="button"
                        >
                            Xóa bộ lọc
                        </button>
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-left text-sm text-slate-600">
                            <thead className="border-b border-slate-200 bg-slate-100/90 text-xs sm:text-sm font-black uppercase tracking-wider text-slate-700">
                                <tr>
                                    <th className="px-6 py-4" scope="col">Nhà tuyển dụng (HR)</th>
                                    <th className="px-6 py-4" scope="col">Thông tin liên hệ</th>
                                    <th className="px-6 py-4" scope="col">Doanh nghiệp trực thuộc</th>
                                    <th className="px-6 py-4" scope="col">Trạng thái tài khoản</th>
                                    <th className="px-6 py-4 text-right" scope="col">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-200/90">
                                {recruitersPage?.content.map((recruiter) => (
                                    <tr
                                        className="group hover:bg-indigo-50/20 transition-colors duration-150"
                                        key={recruiter.id}
                                    >
                                        <td className="px-6 py-4 sm:py-5">
                                            <div className="flex items-center gap-3.5">
                                                {recruiter.avatarUrl ? (
                                                    <img
                                                        alt={recruiter.fullName}
                                                        className="size-11 rounded-xl object-cover border border-slate-200/90 shadow-2xs group-hover:scale-105 transition-transform"
                                                        src={recruiter.avatarUrl}
                                                    />
                                                ) : (
                                                    <div className="grid size-11 place-items-center rounded-xl bg-gradient-to-tr from-indigo-600 to-violet-500 text-white font-black text-sm shadow-2xs">
                                                        {recruiter.fullName.charAt(0).toUpperCase()}
                                                    </div>
                                                )}
                                                <div>
                                                    <div className="text-base font-black text-slate-900 group-hover:text-indigo-600 transition-colors">
                                                        {recruiter.fullName}
                                                    </div>
                                                    <div className="mt-0.5 inline-flex items-center gap-1 rounded-md bg-slate-100 px-2 py-0.5 text-xs font-semibold text-slate-700">
                                                        {recruiter.position || 'Chuyên viên nhân sự'}
                                                    </div>
                                                </div>
                                            </div>
                                        </td>

                                        <td className="px-6 py-4 sm:py-5">
                                            <div className="flex flex-col gap-1 text-xs sm:text-sm">
                                                <div className="flex items-center gap-2 font-medium text-slate-800">
                                                    <Mail size={14} className="text-indigo-600 shrink-0" />
                                                    <span className="truncate max-w-[200px]">{recruiter.email}</span>
                                                </div>
                                                {recruiter.phone && (
                                                    <div className="flex items-center gap-2 font-medium text-slate-600">
                                                        <Phone size={14} className="text-slate-400 shrink-0" />
                                                        <span>{recruiter.phone}</span>
                                                    </div>
                                                )}
                                            </div>
                                        </td>

                                        <td className="px-6 py-4 sm:py-5">
                                            {recruiter.companyName ? (
                                                <div className="flex items-center gap-3">
                                                    <div className="grid size-10 place-items-center rounded-xl bg-slate-100 text-slate-600 border border-slate-200 overflow-hidden shadow-2xs">
                                                        {recruiter.companyLogoUrl ? (
                                                            <img
                                                                alt={recruiter.companyName}
                                                                className="size-10 object-cover"
                                                                src={recruiter.companyLogoUrl}
                                                            />
                                                        ) : (
                                                            <Building2 size={18} className="text-indigo-600" />
                                                        )}
                                                    </div>
                                                    <div>
                                                        <div className="font-bold text-slate-900 text-sm">
                                                            {recruiter.companyName}
                                                        </div>
                                                        {recruiter.companyStatus === 'APPROVED' && (
                                                            <span className="inline-flex items-center gap-1 text-[11px] font-bold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded-md border border-emerald-200">
                                                                <span className="size-1.5 rounded-full bg-emerald-500" />
                                                                Doanh nghiệp đã xác thực
                                                            </span>
                                                        )}
                                                        {recruiter.companyStatus === 'PENDING' && (
                                                            <span className="inline-flex items-center gap-1 text-[11px] font-bold text-amber-700 bg-amber-50 px-2 py-0.5 rounded-md border border-amber-200">
                                                                <span className="size-1.5 rounded-full bg-amber-500" />
                                                                Đang chờ duyệt cty
                                                            </span>
                                                        )}
                                                        {recruiter.companyStatus === 'REJECTED' && (
                                                            <span className="inline-flex items-center gap-1 text-[11px] font-bold text-rose-700 bg-rose-50 px-2 py-0.5 rounded-md border border-rose-200">
                                                                <span className="size-1.5 rounded-full bg-rose-500" />
                                                                Bị từ chối hồ sơ
                                                            </span>
                                                        )}
                                                    </div>
                                                </div>
                                            ) : (
                                                <span className="text-xs font-semibold text-slate-400 italic bg-slate-50 px-2.5 py-1 rounded-lg border border-slate-200">
                                                    Chưa liên kết công ty
                                                </span>
                                            )}
                                        </td>

                                        <td className="px-6 py-4 sm:py-5">
                                            {recruiter.status === 'ACTIVE' ? (
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

                                        <td className="px-6 py-4 sm:py-5 text-right">
                                            <div className="inline-flex items-center gap-2">
                                                <button
                                                    className="btn-bento-secondary px-3 py-1.5 text-xs sm:text-sm font-bold shadow-2xs hover:bg-indigo-50 hover:text-indigo-700 hover:border-indigo-300"
                                                    onClick={() => handleViewDetail(recruiter.id)}
                                                    type="button"
                                                >
                                                    Chi tiết
                                                </button>

                                                {recruiter.status === 'ACTIVE' ? (
                                                    <button
                                                        className="inline-flex items-center gap-1 rounded-xl border border-rose-200 bg-rose-50 px-3 py-1.5 text-xs sm:text-sm font-bold text-rose-700 transition hover:bg-rose-100 shadow-2xs"
                                                        onClick={() => handleOpenActionDialog(recruiter, 'LOCK')}
                                                        title="Khóa tài khoản HR"
                                                        type="button"
                                                    >
                                                        <Lock size={14} />
                                                        <span>Khóa</span>
                                                    </button>
                                                ) : (
                                                    <button
                                                        className="inline-flex items-center gap-1 rounded-xl border border-emerald-200 bg-emerald-50 px-3 py-1.5 text-xs sm:text-sm font-bold text-emerald-700 transition hover:bg-emerald-100 shadow-2xs"
                                                        onClick={() => handleOpenActionDialog(recruiter, 'UNLOCK')}
                                                        title="Mở khóa tài khoản HR"
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
                {recruitersPage && recruitersPage.totalPages > 0 && (
                    <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between border-t border-slate-200 bg-slate-50/90 px-6 py-4 text-xs sm:text-sm font-medium text-slate-600 gap-3">
                        <div>
                            Hiển thị trang <span className="font-extrabold text-slate-900">{recruitersPage.pageNumber}</span> trên tổng số{' '}
                            <span className="font-extrabold text-slate-900">{recruitersPage.totalPages}</span> trang (Tổng {recruitersPage.totalElements} chuyên viên HR)
                        </div>
                        <div className="flex items-center gap-2">
                            <button
                                className="btn-bento-secondary px-3.5 py-1.5 text-xs sm:text-sm font-bold disabled:opacity-40 disabled:cursor-not-allowed"
                                disabled={recruitersPage.pageNumber <= 1}
                                onClick={() => setPage((p) => Math.max(1, p - 1))}
                                type="button"
                            >
                                Trang trước
                            </button>
                            <span className="px-3 py-1.5 rounded-lg bg-indigo-600 text-white font-extrabold text-xs sm:text-sm shadow-2xs">
                                {recruitersPage.pageNumber}
                            </span>
                            <button
                                className="btn-bento-secondary px-3.5 py-1.5 text-xs sm:text-sm font-bold disabled:opacity-40 disabled:cursor-not-allowed"
                                disabled={recruitersPage.isLast || recruitersPage.pageNumber >= recruitersPage.totalPages}
                                onClick={() => setPage((p) => p + 1)}
                                type="button"
                            >
                                Trang sau
                            </button>
                        </div>
                    </div>
                )}
            </div>

            {/* 6. Bento Recruiter Detail Modal */}
            {selectedRecruiter && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-sm p-4 animate-in fade-in">
                    <div className="bento-card relative w-full max-w-lg border border-slate-200/90 bg-white p-6 sm:p-8 shadow-2xl max-h-[90vh] overflow-y-auto">
                        <button
                            className="absolute right-5 top-5 rounded-xl p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-700 transition"
                            onClick={() => setSelectedRecruiter(null)}
                            type="button"
                        >
                            <X size={20} />
                        </button>

                        <div className="flex items-start gap-4 sm:gap-5">
                            {selectedRecruiter.avatarUrl ? (
                                <img
                                    alt={selectedRecruiter.fullName}
                                    className="size-16 sm:size-20 rounded-2xl object-cover border border-slate-200 shadow-sm"
                                    src={selectedRecruiter.avatarUrl}
                                />
                            ) : (
                                <div className="grid size-16 sm:size-20 place-items-center rounded-2xl bg-gradient-to-tr from-indigo-600 to-violet-600 text-white font-black text-2xl shadow-sm">
                                    {selectedRecruiter.fullName.charAt(0).toUpperCase()}
                                </div>
                            )}
                            <div>
                                <h3 className="text-xl sm:text-2xl font-black text-slate-900">{selectedRecruiter.fullName}</h3>
                                <p className="text-sm sm:text-base font-bold text-indigo-600 mt-0.5">
                                    {selectedRecruiter.position || 'Chuyên viên tuyển dụng HR'}
                                </p>
                                <div className="mt-2 flex items-center gap-2">
                                    {selectedRecruiter.status === 'ACTIVE' ? (
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
                                </div>
                            </div>
                        </div>

                        <div className="mt-6 space-y-3.5 border-t border-slate-100 pt-5 text-sm bg-slate-50/70 p-4 rounded-xl border border-slate-200/60">
                            <div className="flex items-center justify-between py-1 border-b border-slate-200/50">
                                <span className="font-semibold text-slate-500">Mã định danh HR:</span>
                                <span className="font-mono font-bold text-slate-900">#{selectedRecruiter.id}</span>
                            </div>
                            <div className="flex items-center justify-between py-1 border-b border-slate-200/50">
                                <span className="font-semibold text-slate-500">Mã tài khoản User:</span>
                                <span className="font-mono font-bold text-slate-900">#{selectedRecruiter.userId}</span>
                            </div>
                            <div className="flex items-center justify-between py-1 border-b border-slate-200/50">
                                <span className="font-semibold text-slate-500">Địa chỉ Email:</span>
                                <span className="font-bold text-slate-900">{selectedRecruiter.email}</span>
                            </div>
                            <div className="flex items-center justify-between py-1 border-b border-slate-200/50">
                                <span className="font-semibold text-slate-500">Số điện thoại:</span>
                                <span className="font-bold text-slate-900">{selectedRecruiter.phone || 'Chưa cập nhật'}</span>
                            </div>
                            <div className="flex items-center justify-between py-1 border-b border-slate-200/50">
                                <span className="font-semibold text-slate-500">Doanh nghiệp trực thuộc:</span>
                                <span className="font-black text-indigo-700">
                                    {selectedRecruiter.companyName || 'Chưa liên kết pháp nhân'}
                                </span>
                            </div>
                            {selectedRecruiter.companyId && (
                                <div className="flex items-center justify-between py-1 border-b border-slate-200/50">
                                    <span className="font-semibold text-slate-500">Trạng thái xác thực công ty:</span>
                                    <span className="font-bold text-slate-900">
                                        {selectedRecruiter.companyStatus === 'APPROVED' ? 'Đã phê duyệt' : 'Đang xử lý'}
                                    </span>
                                </div>
                            )}
                            <div className="flex items-center justify-between py-1">
                                <span className="font-semibold text-slate-500">Thời điểm gia nhập:</span>
                                <span className="font-bold text-slate-900">
                                    {selectedRecruiter.createdAt
                                        ? new Date(selectedRecruiter.createdAt).toLocaleDateString('vi-VN')
                                        : 'N/A'}
                                </span>
                            </div>
                        </div>

                        <div className="mt-6 flex justify-end">
                            <button
                                className="btn-bento-secondary px-5 py-2 text-sm font-bold"
                                onClick={() => setSelectedRecruiter(null)}
                                type="button"
                            >
                                Đóng cửa sổ
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* 7. Bento Lock / Unlock Confirmation Dialog */}
            {actionRecruiter && (
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
                                    {actionType === 'LOCK' ? 'Khóa tài khoản HR' : 'Mở khóa tài khoản HR'}
                                </h3>
                                <p className="text-xs font-semibold text-slate-500">
                                    {actionRecruiter.fullName} ({actionRecruiter.email})
                                </p>
                            </div>
                        </div>

                        <div className="mt-4 text-xs sm:text-sm font-medium text-slate-600 leading-relaxed bg-slate-50 rounded-xl p-3.5 border border-slate-200/60">
                            {actionType === 'LOCK' ? (
                                <p>
                                    Khi bị khóa, chuyên viên nhân sự sẽ không thể đăng tin tuyển dụng mới hoặc kiểm tra danh sách hồ sơ ứng tuyển của doanh nghiệp.
                                </p>
                            ) : (
                                <p>
                                    Khi mở khóa tài khoản, quyền đăng tin và theo dõi ứng viên sẽ được khôi phục ngay lập tức.
                                </p>
                            )}
                        </div>

                        {actionType === 'LOCK' && (
                            <div className="mt-4">
                                <label className="block text-xs font-bold text-slate-700 mb-1.5" htmlFor="hr-lock-reason">
                                    Lý do khóa tài khoản (lưu nhật ký quản trị):
                                </label>
                                <textarea
                                    className="w-full rounded-xl border border-slate-300 p-3 text-xs sm:text-sm text-slate-900 focus:border-rose-500 focus:outline-none focus:ring-2 focus:ring-rose-500/20"
                                    id="hr-lock-reason"
                                    onChange={(e) => setActionReason(e.target.value)}
                                    placeholder="Nhập lý do kiểm soát vi phạm..."
                                    rows={3}
                                    value={actionReason}
                                />
                            </div>
                        )}

                        <div className="mt-6 flex justify-end gap-3">
                            <button
                                className="btn-bento-secondary px-4 py-2 text-xs sm:text-sm font-bold"
                                disabled={statusMutation.isPending}
                                onClick={() => setActionRecruiter(null)}
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
                                    ? 'Xác nhận khóa HR'
                                    : 'Xác nhận mở khóa'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    )
}
