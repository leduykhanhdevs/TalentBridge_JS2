import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
    AlertCircle,
    ArrowLeft,
    Briefcase,
    Building2,
    Calendar,
    CheckCircle2,
    Clock,
    ExternalLink,
    FileText,
    MapPin,
    RefreshCw,
    Search,
    Send,
    Sparkles,
    Trash2,
    X,
    XCircle,
} from 'lucide-react'
import { useState } from 'react'
import { Link, useNavigate } from 'react-router'
import {
    CandidateApiError,
    downloadResumeFile,
    getMyApplications,
    withdrawApplication,
} from '../../features/candidate/candidateApi'
import type {
    ApplicationStage,
    CandidateApplicationItem,
} from '../../features/candidate/candidateTypes'
import { CandidateInterviewCard } from '../../features/candidate/components/CandidateInterviewCard'

const STAGE_LABELS: Record<ApplicationStage, { label: string; color: string; bg: string; border: string }> = {
    APPLIED: {
        label: 'Đã nộp hồ sơ',
        color: 'text-blue-700',
        bg: 'bg-blue-50',
        border: 'border-blue-200',
    },
    SCREENING: {
        label: 'Đang sàng lọc CV',
        color: 'text-amber-700',
        bg: 'bg-amber-50',
        border: 'border-amber-200',
    },
    INTERVIEW: {
        label: 'Mời phỏng vấn',
        color: 'text-purple-700',
        bg: 'bg-purple-50',
        border: 'border-purple-200',
    },
    OFFERED: {
        label: 'Đề nghị nhận việc (Offer)',
        color: 'text-emerald-700',
        bg: 'bg-emerald-50',
        border: 'border-emerald-200',
    },
    REJECTED: {
        label: 'Chưa phù hợp',
        color: 'text-rose-700',
        bg: 'bg-rose-50',
        border: 'border-rose-200',
    },
}

export function CandidateApplicationsPage() {
    const navigate = useNavigate()
    const queryClient = useQueryClient()

    const [stageFilter, setStageFilter] = useState<string>('ALL')
    const [searchKeyword, setSearchKeyword] = useState<string>('')
    const [withdrawingApp, setWithdrawingApp] = useState<CandidateApplicationItem | null>(null)
    const [actionError, setActionError] = useState<string | null>(null)
    const [actionSuccess, setActionSuccess] = useState<string | null>(null)

    const {
        data: applications = [],
        isLoading,
        isError,
        error,
        refetch,
    } = useQuery({
        queryKey: ['candidate-my-applications'],
        queryFn: getMyApplications,
    })

    const withdrawMutation = useMutation({
        mutationFn: (id: number) => withdrawApplication(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['candidate-my-applications'] })
            setActionSuccess('Đã rút đơn ứng tuyển thành công.')
            setActionError(null)
            setWithdrawingApp(null)
        },
        onError: (err) => {
            if (err instanceof CandidateApiError) {
                setActionError(err.message)
            } else {
                setActionError('Không thể rút đơn ứng tuyển. Vui lòng thử lại.')
            }
        },
    })

    // Filter calculations
    const filteredApps = applications.filter((app) => {
        if (stageFilter !== 'ALL') {
            if (stageFilter === 'WITHDRAWN') {
                if (app.status !== 'WITHDRAWN') return false
            } else if (app.currentStage !== stageFilter || app.status === 'WITHDRAWN') {
                return false
            }
        }
        if (searchKeyword.trim()) {
            const kw = searchKeyword.toLowerCase()
            const matchTitle = app.jobTitle.toLowerCase().includes(kw)
            const matchCompany = app.companyName.toLowerCase().includes(kw)
            const matchCity = app.city?.toLowerCase().includes(kw)
            if (!matchTitle && !matchCompany && !matchCity) return false
        }
        return true
    })

    // Metrics
    const totalApps = applications.length
    const activeProcessingCount = applications.filter(
        (a) => a.status === 'SUBMITTED' && (a.currentStage === 'APPLIED' || a.currentStage === 'SCREENING'),
    ).length
    const interviewCount = applications.filter(
        (a) => a.status === 'SUBMITTED' && a.currentStage === 'INTERVIEW',
    ).length
    const offerCount = applications.filter(
        (a) => a.status === 'SUBMITTED' && a.currentStage === 'OFFERED',
    ).length
    const withdrawnCount = applications.filter((a) => a.status === 'WITHDRAWN').length

    return (
        <div className="space-y-6">
            {/* Header section */}
            <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <div className="flex items-center gap-2">
                        <Link
                            to="/candidate/profile"
                            className="inline-flex items-center gap-1 text-xs font-semibold text-slate-500 hover:text-slate-800 transition"
                        >
                            <ArrowLeft size={14} />
                            Hồ sơ cá nhân
                        </Link>
                        <span className="text-slate-300">•</span>
                        <span className="text-xs font-bold uppercase tracking-wider text-indigo-600">
                            HRPM-42 • ATS Candidate Tracking
                        </span>
                    </div>
                    <h1 className="mt-1 text-2xl sm:text-3xl font-black tracking-tight text-slate-900">
                        Lịch sử ứng tuyển & Theo dõi hồ sơ
                    </h1>
                    <p className="mt-1 text-xs sm:text-sm font-medium text-slate-500">
                        Theo dõi tiến trình xét duyệt hồ sơ, các vòng phỏng vấn và kết quả tuyển dụng từ các doanh nghiệp.
                    </p>
                </div>

                <div className="flex items-center gap-2.5 shrink-0">
                    <button
                        onClick={() => refetch()}
                        className="btn-bento-secondary h-10 px-3.5 text-xs font-bold inline-flex items-center gap-1.5"
                        type="button"
                    >
                        <RefreshCw size={14} />
                        Làm mới
                    </button>
                    <button
                        onClick={() => navigate('/jobs')}
                        className="btn-bento-primary h-10 px-4 text-xs font-bold inline-flex items-center gap-1.5"
                        type="button"
                    >
                        <Search size={14} />
                        Tìm việc làm mới
                    </button>
                </div>
            </div>

            {/* Notification messages */}
            {actionSuccess && (
                <div className="flex items-center justify-between rounded-2xl border border-emerald-200 bg-emerald-50/90 p-4 text-xs font-bold text-emerald-800 animate-in fade-in">
                    <div className="flex items-center gap-2">
                        <CheckCircle2 size={16} className="text-emerald-600 shrink-0" />
                        <span>{actionSuccess}</span>
                    </div>
                    <button onClick={() => setActionSuccess(null)} className="text-emerald-600 hover:text-emerald-800">
                        <X size={15} />
                    </button>
                </div>
            )}

            {actionError && (
                <div className="flex items-center justify-between rounded-2xl border border-rose-200 bg-rose-50/90 p-4 text-xs font-bold text-rose-800 animate-in fade-in">
                    <div className="flex items-center gap-2">
                        <AlertCircle size={16} className="text-rose-600 shrink-0" />
                        <span>{actionError}</span>
                    </div>
                    <button onClick={() => setActionError(null)} className="text-rose-600 hover:text-rose-800">
                        <X size={15} />
                    </button>
                </div>
            )}

            {/* Metric Bento Summary Cards */}
            <div className="grid grid-cols-2 gap-3 sm:grid-cols-5 sm:gap-4">
                <div className="bento-card p-4 sm:p-5">
                    <span className="text-[11px] font-bold uppercase tracking-wider text-slate-500">Tổng đơn đã nộp</span>
                    <div className="mt-2 flex items-baseline justify-between">
                        <span className="text-2xl sm:text-3xl font-black text-slate-900">{totalApps}</span>
                        <Send size={18} className="text-slate-400" />
                    </div>
                </div>

                <div className="bento-card p-4 sm:p-5">
                    <span className="text-[11px] font-bold uppercase tracking-wider text-amber-600">Đang sàng lọc</span>
                    <div className="mt-2 flex items-baseline justify-between">
                        <span className="text-2xl sm:text-3xl font-black text-amber-700">{activeProcessingCount}</span>
                        <Clock size={18} className="text-amber-500" />
                    </div>
                </div>

                <div className="bento-card p-4 sm:p-5">
                    <span className="text-[11px] font-bold uppercase tracking-wider text-purple-600">Mời phỏng vấn</span>
                    <div className="mt-2 flex items-baseline justify-between">
                        <span className="text-2xl sm:text-3xl font-black text-purple-700">{interviewCount}</span>
                        <Sparkles size={18} className="text-purple-500" />
                    </div>
                </div>

                <div className="bento-card p-4 sm:p-5">
                    <span className="text-[11px] font-bold uppercase tracking-wider text-emerald-600">Nhận đề nghị</span>
                    <div className="mt-2 flex items-baseline justify-between">
                        <span className="text-2xl sm:text-3xl font-black text-emerald-700">{offerCount}</span>
                        <CheckCircle2 size={18} className="text-emerald-500" />
                    </div>
                </div>

                <div className="bento-card p-4 sm:p-5 col-span-2 sm:col-span-1">
                    <span className="text-[11px] font-bold uppercase tracking-wider text-slate-500">Đã rút đơn</span>
                    <div className="mt-2 flex items-baseline justify-between">
                        <span className="text-2xl sm:text-3xl font-black text-slate-700">{withdrawnCount}</span>
                        <XCircle size={18} className="text-slate-400" />
                    </div>
                </div>
            </div>

            {/* Filter and search bar */}
            <div className="bento-card p-4 sm:p-5 space-y-4">
                <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                    {/* Stage tabs */}
                    <div className="flex items-center gap-1.5 overflow-x-auto pb-1 sm:pb-0 scrollbar-none">
                        {[
                            { key: 'ALL', label: `Tất cả (${totalApps})` },
                            { key: 'APPLIED', label: 'Đã nộp' },
                            { key: 'SCREENING', label: 'Sàng lọc' },
                            { key: 'INTERVIEW', label: 'Phỏng vấn' },
                            { key: 'OFFERED', label: 'Đề nghị' },
                            { key: 'REJECTED', label: 'Từ chối' },
                            { key: 'WITHDRAWN', label: 'Đã rút' },
                        ].map((tab) => (
                            <button
                                key={tab.key}
                                onClick={() => setStageFilter(tab.key)}
                                className={`rounded-xl px-3 py-1.5 text-xs font-bold transition whitespace-nowrap ${
                                    stageFilter === tab.key
                                        ? 'bg-indigo-600 text-white shadow-xs'
                                        : 'bg-slate-100 text-slate-600 hover:bg-slate-200/80 hover:text-slate-900'
                                }`}
                                type="button"
                            >
                                {tab.label}
                            </button>
                        ))}
                    </div>

                    {/* Search box */}
                    <div className="relative w-full sm:w-72 shrink-0">
                        <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
                        <input
                            type="text"
                            value={searchKeyword}
                            onChange={(e) => setSearchKeyword(e.target.value)}
                            placeholder="Tìm việc làm, công ty..."
                            className="w-full rounded-xl border border-slate-200 bg-white pl-9 pr-3 py-1.5 text-xs text-slate-800 placeholder-slate-400 focus:border-indigo-500 focus:outline-none"
                        />
                    </div>
                </div>
            </div>

            {/* Loading state */}
            {isLoading && (
                <div className="bento-card p-12 text-center">
                    <RefreshCw size={24} className="mx-auto text-indigo-600 animate-spin" />
                    <p className="mt-3 text-xs font-bold text-slate-600">Đang tải lịch sử ứng tuyển của bạn...</p>
                </div>
            )}

            {/* Error state */}
            {isError && (
                <div className="bento-card p-8 border-rose-200 bg-rose-50/50 text-center">
                    <AlertCircle size={28} className="mx-auto text-rose-600" />
                    <h3 className="mt-2 text-sm font-bold text-rose-900">Không thể tải danh sách đơn ứng tuyển</h3>
                    <p className="mt-1 text-xs text-rose-700">{(error as Error)?.message || 'Vui lòng thử lại sau.'}</p>
                    <button
                        onClick={() => refetch()}
                        className="mt-4 btn-bento-secondary text-xs font-bold px-4 py-2"
                        type="button"
                    >
                        Thử lại
                    </button>
                </div>
            )}

            {/* Empty state */}
            {!isLoading && !isError && filteredApps.length === 0 && (
                <div className="bento-card p-12 text-center border-dashed">
                    <div className="mx-auto grid size-14 place-items-center rounded-2xl bg-slate-100 text-slate-400">
                        <Briefcase size={26} />
                    </div>
                    <h3 className="mt-4 text-base font-bold text-slate-900">Chưa tìm thấy đơn ứng tuyển nào</h3>
                    <p className="mt-1 max-w-sm mx-auto text-xs text-slate-500">
                        {searchKeyword || stageFilter !== 'ALL'
                            ? 'Không có đơn ứng tuyển nào phù hợp với bộ lọc hiện tại. Thử nới rộng bộ lọc tìm kiếm.'
                            : 'Bạn chưa nộp đơn ứng tuyển việc làm nào trên hệ thống. Hãy khám phá ngay hàng trăm cơ hội việc làm hấp dẫn!'}
                    </p>
                    <button
                        onClick={() => navigate('/jobs')}
                        className="mt-5 btn-bento-primary px-5 py-2 text-xs font-bold"
                        type="button"
                    >
                        Khám phá việc làm ngay
                    </button>
                </div>
            )}

            {/* Applications List */}
            {!isLoading && !isError && filteredApps.length > 0 && (
                <div className="space-y-4">
                    {filteredApps.map((app) => {
                        const stageInfo = STAGE_LABELS[app.currentStage] || STAGE_LABELS.APPLIED
                        const isWithdrawn = app.status === 'WITHDRAWN'
                        const canWithdraw =
                            !isWithdrawn &&
                            app.currentStage !== 'OFFERED' &&
                            app.currentStage !== 'REJECTED'

                        return (
                            <div
                                key={app.id}
                                className={`bento-card p-5 sm:p-6 transition-all hover:border-slate-300 ${
                                    isWithdrawn ? 'opacity-70 bg-slate-50/50' : ''
                                }`}
                            >
                                <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                                    {/* Company & Job info */}
                                    <div className="flex items-start gap-4 min-w-0">
                                        {app.companyLogo ? (
                                            <img
                                                src={app.companyLogo}
                                                alt={app.companyName}
                                                className="size-12 rounded-xl object-cover border border-slate-200 shrink-0 shadow-2xs"
                                            />
                                        ) : (
                                            <div className="grid size-12 place-items-center rounded-xl bg-gradient-to-tr from-indigo-600 to-violet-500 text-white font-black text-base shadow-2xs shrink-0">
                                                {app.companyName ? app.companyName.charAt(0).toUpperCase() : 'C'}
                                            </div>
                                        )}

                                        <div className="min-w-0">
                                            <div className="flex flex-wrap items-center gap-2">
                                                <Link
                                                    to={`/jobs/${app.jobId}`}
                                                    className="text-base sm:text-lg font-black text-slate-900 hover:text-indigo-600 transition truncate"
                                                >
                                                    {app.jobTitle}
                                                </Link>
                                                {app.aiMatchScore && (
                                                    <span className="inline-flex items-center gap-1 rounded-full bg-indigo-50 border border-indigo-200/80 px-2.5 py-0.5 text-[11px] font-bold text-indigo-700">
                                                        <Sparkles size={11} className="text-indigo-600" />
                                                        Độ khớp CV: {Math.round(Number(app.aiMatchScore))}%
                                                    </span>
                                                )}
                                            </div>

                                            <p className="mt-1 flex items-center gap-1.5 text-xs sm:text-sm font-semibold text-slate-700">
                                                <Building2 size={14} className="text-slate-400 shrink-0" />
                                                <span>{app.companyName}</span>
                                            </p>

                                            <div className="mt-2.5 flex flex-wrap items-center gap-x-4 gap-y-1.5 text-xs text-slate-500 font-medium">
                                                <span className="flex items-center gap-1">
                                                    <MapPin size={13} className="text-slate-400" />
                                                    {app.city || app.location || 'Toàn quốc'}
                                                </span>
                                                <span className="flex items-center gap-1">
                                                    <Briefcase size={13} className="text-slate-400" />
                                                    {app.jobType || 'Toàn thời gian'}
                                                </span>
                                                <span className="flex items-center gap-1">
                                                    <Calendar size={13} className="text-slate-400" />
                                                    Nộp ngày: {new Date(app.appliedAt).toLocaleDateString('vi-VN')}
                                                </span>
                                            </div>
                                        </div>
                                    </div>

                                    {/* Status & Stage Badges */}
                                    <div className="flex flex-wrap sm:flex-col items-start sm:items-end gap-2 shrink-0">
                                        {isWithdrawn ? (
                                            <span className="inline-flex items-center gap-1 rounded-full bg-slate-100 border border-slate-200 px-3 py-1 text-xs font-bold text-slate-600">
                                                <XCircle size={13} className="text-slate-500" />
                                                Đã rút đơn ứng tuyển
                                            </span>
                                        ) : (
                                            <span
                                                className={`inline-flex items-center gap-1 rounded-full px-3 py-1 text-xs font-bold border ${stageInfo.bg} ${stageInfo.color} ${stageInfo.border}`}
                                            >
                                                <span className="size-1.5 rounded-full bg-current animate-pulse" />
                                                {stageInfo.label}
                                            </span>
                                        )}

                                        <span className="text-[11px] font-semibold text-slate-400">
                                            Mã đơn: #{app.id}
                                        </span>
                                    </div>
                                </div>

                                {app.currentStage === 'INTERVIEW' && (
                                    <CandidateInterviewCard applicationId={app.id} />
                                )}

                                {/* Attached CV & Cover letter snippet */}
                                <div className="mt-4 pt-3 border-t border-slate-100 flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs">
                                    <div className="flex flex-wrap items-center gap-3">
                                        {app.resumeFileName && (
                                            <div className="inline-flex items-center gap-1.5 bg-slate-100/80 px-2.5 py-1 rounded-lg text-slate-700 font-medium">
                                                <FileText size={13} className="text-indigo-600" />
                                                <span className="truncate max-w-[200px]">{app.resumeFileName}</span>
                                                {app.resumeId && (
                                                    <button
                                                        type="button"
                                                        onClick={() => downloadResumeFile(app.resumeId!, app.resumeFileName!)}
                                                        className="text-indigo-600 font-bold hover:underline ml-1"
                                                    >
                                                        Tải CV
                                                    </button>
                                                )}
                                            </div>
                                        )}

                                        {app.coverLetter && (
                                            <span className="text-slate-500 italic truncate max-w-md">
                                                &quot;{app.coverLetter}&quot;
                                            </span>
                                        )}
                                    </div>

                                    {/* Actions */}
                                    <div className="flex items-center gap-2 shrink-0">
                                        <Link
                                            to={`/jobs/${app.jobId}`}
                                            className="btn-bento-secondary h-8 px-3 text-xs font-bold inline-flex items-center gap-1"
                                        >
                                            <ExternalLink size={12} />
                                            Xem tin tuyển dụng
                                        </Link>

                                        {canWithdraw && (
                                            <button
                                                type="button"
                                                onClick={() => setWithdrawingApp(app)}
                                                className="inline-flex items-center gap-1 rounded-xl border border-rose-200 bg-rose-50/70 px-3 py-1.5 text-xs font-bold text-rose-700 hover:bg-rose-100 hover:border-rose-300 transition"
                                            >
                                                <Trash2 size={12} />
                                                Rút đơn
                                            </button>
                                        )}
                                    </div>
                                </div>
                            </div>
                        )
                    })}
                </div>
            )}

            {/* Modal Confirm Withdraw */}
            {withdrawingApp && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-xs animate-in fade-in">
                    <div className="bento-card relative w-full max-w-md p-6 bg-white shadow-2xl">
                        <div className="mx-auto mb-4 grid size-12 place-items-center rounded-2xl border border-rose-200 bg-rose-50 text-rose-700">
                            <Trash2 size={24} />
                        </div>

                        <h3 className="text-center text-lg font-black text-slate-900">
                            Xác nhận rút đơn ứng tuyển?
                        </h3>
                        <p className="mt-2 text-center text-xs font-medium text-slate-600 leading-relaxed">
                            Bạn đang chuẩn bị rút đơn ứng tuyển cho vị trí{' '}
                            <strong className="text-slate-900">{withdrawingApp.jobTitle}</strong> tại{' '}
                            <strong className="text-slate-900">{withdrawingApp.companyName}</strong>.
                        </p>
                        <p className="mt-1 text-center text-[11px] font-semibold text-rose-600">
                            * Sau khi rút đơn, nhà tuyển dụng sẽ nhận thông báo và không còn tiếp tục quy trình sàng lọc cho đơn này.
                        </p>

                        <div className="mt-6 flex items-center justify-end gap-2.5 pt-3 border-t border-slate-100">
                            <button
                                type="button"
                                disabled={withdrawMutation.isPending}
                                onClick={() => setWithdrawingApp(null)}
                                className="btn-bento-secondary h-9 px-4 text-xs font-bold"
                            >
                                Hủy bỏ
                            </button>
                            <button
                                type="button"
                                disabled={withdrawMutation.isPending}
                                onClick={() => withdrawMutation.mutate(withdrawingApp.id)}
                                className="inline-flex items-center gap-1.5 rounded-xl bg-rose-600 px-4 py-2 text-xs font-bold text-white hover:bg-rose-700 transition disabled:opacity-50"
                            >
                                {withdrawMutation.isPending ? (
                                    <>
                                        <RefreshCw size={13} className="animate-spin" />
                                        Đang xử lý...
                                    </>
                                ) : (
                                    'Xác nhận rút đơn'
                                )}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    )
}
