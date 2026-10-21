import { useCallback, useEffect, useState } from 'react'
import { Activity, ArrowRight, BriefcaseBusiness, Building2, CircleAlert, RefreshCw, Users } from 'lucide-react'
import { Link } from 'react-router'
import { AssistantChatPanel } from '../../features/assistant/components/AssistantChatPanel'
import { getAdminApiMetrics, getAdminDashboardStats } from '../../features/admin/adminApi'
import type { AdminApiMetricsResponse, AdminDashboardStatsResponse } from '../../features/admin/adminTypes'

const numberFormat = new Intl.NumberFormat('vi-VN')

function formatNumber(value: number | undefined) {
    return value === undefined ? '—' : numberFormat.format(value)
}

function errorMessage(reason: unknown, fallback: string) {
    return reason instanceof Error ? reason.message : fallback
}

export function AdminAssistantPage() {
    const [stats, setStats] = useState<AdminDashboardStatsResponse | null>(null)
    const [metrics, setMetrics] = useState<AdminApiMetricsResponse | null>(null)
    const [statsError, setStatsError] = useState<string | null>(null)
    const [metricsError, setMetricsError] = useState<string | null>(null)
    const [isLoading, setIsLoading] = useState(true)
    const [updatedAt, setUpdatedAt] = useState<Date | null>(null)

    const refresh = useCallback(async () => {
        setIsLoading(true)
        const [statsResult, metricsResult] = await Promise.allSettled([
            getAdminDashboardStats(),
            getAdminApiMetrics(24),
        ])

        if (statsResult.status === 'fulfilled') {
            setStats(statsResult.value)
            setStatsError(null)
        } else {
            setStats(null)
            setStatsError(errorMessage(statsResult.reason, 'Không tải được số liệu hệ thống.'))
        }

        if (metricsResult.status === 'fulfilled') {
            setMetrics(metricsResult.value)
            setMetricsError(null)
        } else {
            setMetrics(null)
            setMetricsError(errorMessage(metricsResult.reason, 'Không tải được thống kê API.'))
        }

        if (statsResult.status === 'fulfilled' || metricsResult.status === 'fulfilled') {
            setUpdatedAt(new Date())
        }
        setIsLoading(false)
    }, [])

    useEffect(() => {
        void refresh()
    }, [refresh])

    const errors = [statsError, metricsError].filter((message): message is string => Boolean(message))
    const apiErrors = metrics ? metrics.clientErrorCount + metrics.serverErrorCount : undefined
    const cards = [
        {
            label: 'Tài khoản',
            value: formatNumber(stats?.totalUsers),
            detail: stats ? `${formatNumber(stats.totalCompanies)} doanh nghiệp` : 'Tổng số tài khoản hệ thống',
            icon: Users,
            tone: 'bg-blue-50 text-blue-700',
        },
        {
            label: 'Doanh nghiệp chờ duyệt',
            value: formatNumber(stats?.pendingCompanies),
            detail: stats ? `Trên ${formatNumber(stats.totalCompanies)} doanh nghiệp` : 'Hàng chờ phê duyệt',
            icon: Building2,
            tone: 'bg-amber-50 text-amber-700',
        },
        {
            label: 'Tin tuyển dụng',
            value: formatNumber(stats?.totalJobs),
            detail: stats ? `${formatNumber(stats.activeJobs)} đang hoạt động · ${formatNumber(stats.pendingJobs)} chờ duyệt` : 'Tổng số tin trong hệ thống',
            icon: BriefcaseBusiness,
            tone: 'bg-emerald-50 text-emerald-700',
        },
        {
            label: 'API trong 24 giờ',
            value: formatNumber(metrics?.requestCount),
            detail: metrics ? `${formatNumber(apiErrors)} lỗi · trung bình ${formatNumber(metrics.averageLatencyMs)} ms` : 'Lưu lượng và độ trễ máy chủ',
            icon: Activity,
            tone: 'bg-violet-50 text-violet-700',
        },
    ]
    const managementLinks = [
        { to: '/admin/candidates', label: 'Ứng viên', description: 'Tìm kiếm và quản lý tài khoản ứng viên', icon: Users },
        { to: '/admin/recruiters', label: 'Nhà tuyển dụng', description: 'Quản lý HR và thông tin liên quan', icon: Users },
        { to: '/admin/companies', label: 'Duyệt doanh nghiệp', description: 'Xem hồ sơ cần phê duyệt', icon: Building2, count: stats?.pendingCompanies },
        { to: '/admin/jobs', label: 'Kiểm duyệt tin', description: 'Xem tin tuyển dụng chờ duyệt', icon: BriefcaseBusiness, count: stats?.pendingJobs },
        { to: '/admin/metrics', label: 'Thống kê API', description: 'Theo dõi yêu cầu, lỗi và độ trễ', icon: Activity },
    ]

    return (
        <div className="space-y-6">
            <section className="flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
                <div>
                    <p className="text-xs font-bold uppercase tracking-[0.16em] text-blue-700">Trợ lý dành cho quản trị</p>
                    <h1 className="mt-1 text-2xl font-bold tracking-tight text-slate-950 sm:text-3xl">Trợ lý quản trị</h1>
                    <p className="mt-2 max-w-3xl text-sm leading-6 text-slate-600">
                        Theo dõi số liệu hệ thống, xác định việc cần xử lý và hỏi trợ lý về quy trình quản trị TalentBridge.
                    </p>
                </div>
                <div className="flex shrink-0 flex-col items-start gap-2 sm:items-end">
                    <button
                        type="button"
                        onClick={() => void refresh()}
                        disabled={isLoading}
                        className="inline-flex h-10 items-center gap-2 rounded-lg bg-blue-700 px-4 text-sm font-semibold text-white transition hover:bg-blue-800 disabled:cursor-wait disabled:opacity-70"
                    >
                        <RefreshCw aria-hidden="true" className={isLoading ? 'animate-spin' : ''} size={15} />
                        {isLoading ? 'Đang cập nhật…' : 'Cập nhật số liệu'}
                    </button>
                    <span className="text-xs text-slate-500" aria-live="polite">
                        {updatedAt ? `Cập nhật lúc ${updatedAt.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })}` : 'Đang tải dữ liệu từ máy chủ'}
                    </span>
                </div>
            </section>

            {errors.length > 0 && (
                <div className="flex items-start gap-2 rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-950" role="status">
                    <CircleAlert aria-hidden="true" className="mt-0.5 shrink-0" size={17} />
                    <div><p className="font-semibold">Một phần số liệu chưa tải được</p><p className="mt-1">{errors.join(' ')}</p></div>
                </div>
            )}

            <section aria-label="Số liệu quản trị" className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
                {cards.map((card) => {
                    const Icon = card.icon
                    return (
                        <article key={card.label} className="rounded-xl border border-slate-200 bg-white p-4 sm:p-5">
                            <div className="flex items-center justify-between gap-3">
                                <p className="text-sm font-semibold text-slate-600">{card.label}</p>
                                <span className={`grid size-9 place-items-center rounded-lg ${card.tone}`}><Icon aria-hidden="true" size={18} /></span>
                            </div>
                            <p className="mt-3 text-2xl font-bold tracking-tight text-slate-950" aria-live="polite">{isLoading && !stats && !metrics ? '…' : card.value}</p>
                            <p className="mt-1 text-xs leading-5 text-slate-500">{card.detail}</p>
                        </article>
                    )
                })}
            </section>

            <section className="grid items-start gap-5 xl:grid-cols-[minmax(0,1.3fr)_minmax(360px,0.9fr)]">
                <div className="rounded-xl border border-slate-200 bg-white p-4 sm:p-5">
                    <div className="mb-4">
                        <h2 className="text-base font-bold text-slate-950">Lối tắt quản lý</h2>
                        <p className="mt-1 text-sm text-slate-600">Mở khu vực cần xem hoặc xử lý.</p>
                    </div>
                    <div className="grid gap-2 sm:grid-cols-2">
                        {managementLinks.map((item) => {
                            const Icon = item.icon
                            return (
                                <Link key={item.to} to={item.to} className="group flex min-w-0 items-center gap-3 rounded-lg border border-slate-200 p-3 transition hover:border-blue-200 hover:bg-blue-50/50">
                                    <span className="grid size-9 shrink-0 place-items-center rounded-lg bg-slate-100 text-slate-700 group-hover:bg-white group-hover:text-blue-700"><Icon aria-hidden="true" size={17} /></span>
                                    <span className="min-w-0 flex-1">
                                        <span className="flex items-center gap-2 text-sm font-semibold text-slate-900">
                                            <span className="truncate">{item.label}</span>
                                            {item.count !== undefined && item.count > 0 && <span className="rounded-full bg-amber-100 px-2 py-0.5 text-[10px] font-bold text-amber-800">{formatNumber(item.count)} chờ</span>}
                                        </span>
                                        <span className="mt-0.5 block truncate text-xs text-slate-500">{item.description}</span>
                                    </span>
                                    <ArrowRight aria-hidden="true" className="shrink-0 text-slate-400 transition group-hover:translate-x-0.5 group-hover:text-blue-700" size={15} />
                                </Link>
                            )
                        })}
                    </div>
                </div>

                <AssistantChatPanel adminMode adminStats={stats} adminMetrics={metrics} />
            </section>
        </div>
    )
}
