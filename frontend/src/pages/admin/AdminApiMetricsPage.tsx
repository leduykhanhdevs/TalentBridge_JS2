import { useEffect, useState } from 'react'
import { Activity, AlertTriangle, Clock3, RefreshCw, Server, ShieldAlert } from 'lucide-react'
import { AdminApiError, getAdminApiMetrics } from '../../features/admin/adminApi'
import type { AdminApiMetricsResponse, ApiMetricsBucketResponse, ApiMetricsEndpointResponse } from '../../features/admin/adminTypes'

const numberFormat = new Intl.NumberFormat('vi-VN')

function formatLatency(value: number): string {
    return `${new Intl.NumberFormat('vi-VN', { maximumFractionDigits: 1 }).format(value)} ms`
}

function formatTime(value: string): string {
    return new Intl.DateTimeFormat('vi-VN', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit' }).format(new Date(value))
}

function EndpointTimeline({ buckets, hours, endpoint, to }: { buckets: ApiMetricsBucketResponse[]; hours: number; endpoint: string; to: string }) {
    const totalSlots = Math.max(1, Math.ceil(hours * 60 / 5))
    const slots = new Map(buckets.map((bucket) => [Math.floor(new Date(bucket.startsAt).getTime() / 300_000), bucket]))
    const nowSlot = Math.floor(new Date(to).getTime() / 300_000)
    const values = Array.from({ length: totalSlots }, (_, index) => slots.get(nowSlot - totalSlots + index + 1)?.requestCount ?? 0)
    const maxValue = Math.max(1, ...values)
    const points = values.map((value, index) => {
        const x = values.length === 1 ? 0 : index * 240 / (values.length - 1)
        const y = 32 - (value / maxValue) * 28
        return `${x},${y}`
    }).join(' ')

    return (
        <svg aria-label={`Lưu lượng ${endpoint} theo từng khoảng 5 phút`} className="h-10 w-44 overflow-visible" role="img" viewBox="0 0 240 36">
            <line x1="0" x2="240" y1="33" y2="33" stroke="#cbd5e1" strokeWidth="1" />
            <polyline fill="none" points={points} stroke="#1d4ed8" strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" />
        </svg>
    )
}

function MetricCard({ icon: Icon, label, value, detail }: { icon: typeof Activity; label: string; value: string; detail: string }) {
    return (
        <article className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
            <div className="flex items-center justify-between gap-3">
                <p className="text-sm font-semibold text-slate-600">{label}</p>
                <span className="grid size-9 place-items-center rounded-xl bg-blue-50 text-blue-700"><Icon aria-hidden="true" size={18} /></span>
            </div>
            <p className="mt-4 text-2xl font-bold tracking-tight text-slate-950">{value}</p>
            <p className="mt-1 text-xs text-slate-500">{detail}</p>
        </article>
    )
}

function EndpointRow({ endpoint, hours, to }: { endpoint: ApiMetricsEndpointResponse; hours: number; to: string }) {
    const errors = endpoint.clientErrorCount + endpoint.serverErrorCount
    return (
        <tr className="border-t border-slate-100 align-middle">
            <th className="max-w-[360px] px-4 py-4 text-left font-medium text-slate-800" scope="row">
                <span className="block truncate font-mono text-xs" title={endpoint.endpoint}>{endpoint.endpoint}</span>
                <span className="mt-1 block text-xs font-normal text-slate-500">{numberFormat.format(endpoint.requestCount)} request</span>
            </th>
            <td className="px-4 py-4 text-sm text-slate-700">{numberFormat.format(errors)} <span className="text-xs text-slate-500">(4xx {endpoint.clientErrorCount} · 5xx {endpoint.serverErrorCount})</span></td>
            <td className="px-4 py-4 text-sm font-semibold text-slate-700">{endpoint.errorRatePercent}%</td>
            <td className="px-4 py-4 text-sm text-slate-700">{formatLatency(endpoint.averageLatencyMs)}</td>
            <td className="px-4 py-4 text-sm text-slate-700">{formatLatency(endpoint.maxLatencyMs)}</td>
            <td className="px-4 py-4"><EndpointTimeline buckets={endpoint.buckets} endpoint={endpoint.endpoint} hours={hours} to={to} /></td>
        </tr>
    )
}

export function AdminApiMetricsPage() {
    const [hours, setHours] = useState(24)
    const [refreshVersion, setRefreshVersion] = useState(0)
    const [metrics, setMetrics] = useState<AdminApiMetricsResponse | null>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    useEffect(() => {
        let active = true
        setLoading(true)
        setError('')
        getAdminApiMetrics(hours)
            .then((result) => { if (active) setMetrics(result) })
            .catch((reason: unknown) => {
                if (!active) return
                setError(reason instanceof AdminApiError ? reason.message : 'Không thể kết nối tới API thống kê.')
            })
            .finally(() => { if (active) setLoading(false) })
        return () => { active = false }
    }, [hours, refreshVersion])

    const errors = metrics ? metrics.clientErrorCount + metrics.serverErrorCount : 0
    const sortedEndpoints = [...(metrics?.endpoints ?? [])].sort((left, right) => right.requestCount - left.requestCount)

    return (
        <main className="mx-auto w-full max-w-[1200px] px-4 py-8 sm:px-6 lg:px-8">
            <div className="flex flex-wrap items-end justify-between gap-4">
                <div>
                    <p className="text-xs font-bold uppercase tracking-[0.16em] text-blue-700">Vận hành hệ thống</p>
                    <h1 className="mt-2 text-2xl font-bold tracking-tight text-slate-950 sm:text-3xl">Thống kê API</h1>
                    <p className="mt-2 max-w-2xl text-sm text-slate-600">Lưu lượng, độ trễ và lỗi HTTP được gom theo endpoint, theo từng khoảng 5 phút.</p>
                </div>
                <div className="flex items-center gap-2">
                    <label className="sr-only" htmlFor="metrics-range">Khoảng thời gian</label>
                    <select id="metrics-range" className="h-10 rounded-xl border border-slate-200 bg-white px-3 text-sm font-medium text-slate-700" onChange={(event) => setHours(Number(event.target.value))} value={hours}>
                        <option value={1}>1 giờ</option>
                        <option value={6}>6 giờ</option>
                        <option value={24}>24 giờ</option>
                    </select>
                    <button className="inline-flex h-10 items-center gap-2 rounded-xl border border-slate-200 bg-white px-3 text-sm font-semibold text-slate-700 hover:bg-slate-50" onClick={() => setRefreshVersion((value) => value + 1)} type="button">
                        <RefreshCw aria-hidden="true" className={loading ? 'animate-spin' : ''} size={15} /> Làm mới
                    </button>
                </div>
            </div>

            {error && <div className="mt-6 flex items-start gap-3 rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-800" role="alert"><AlertTriangle aria-hidden="true" className="mt-0.5 shrink-0" size={18} /><span>{error}</span></div>}

            {metrics && <>
                <section aria-label="Tổng quan API" className="mt-7 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
                    <MetricCard icon={Activity} label="Tổng request" value={numberFormat.format(metrics.requestCount)} detail={`trong ${hours} giờ qua`} />
                    <MetricCard icon={AlertTriangle} label="Request lỗi" value={`${numberFormat.format(errors)} · ${metrics.errorRatePercent}%`} detail="HTTP 4xx và 5xx" />
                    <MetricCard icon={Clock3} label="Độ trễ trung bình" value={formatLatency(metrics.averageLatencyMs)} detail={`cao nhất ${formatLatency(metrics.maxLatencyMs)}`} />
                    <MetricCard icon={Server} label="Endpoint ghi nhận" value={numberFormat.format(metrics.endpoints.length)} detail={`bucket ${metrics.bucketMinutes} phút`} />
                </section>

                <div className="mt-5 flex items-start gap-3 rounded-xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-950">
                    <ShieldAlert aria-hidden="true" className="mt-0.5 shrink-0" size={18} />
                    <p>Dữ liệu được giữ trong bộ nhớ tiến trình tối đa {metrics.retentionHours} giờ và sẽ được làm mới sau khi backend khởi động lại. URL và dữ liệu request không được lưu.</p>
                </div>

                <section aria-label="Metrics theo endpoint" className="mt-5 overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
                    <div className="flex flex-wrap items-center justify-between gap-2 border-b border-slate-100 px-5 py-4">
                        <div>
                            <h2 className="font-bold text-slate-900">Chi tiết theo endpoint</h2>
                            <p className="mt-1 text-xs text-slate-500">Tính cả request nội bộ và request bị từ chối; route dùng mẫu đường dẫn, không lưu ID.</p>
                        </div>
                        <p className="text-xs text-slate-500">Cập nhật {formatTime(metrics.generatedAt)}</p>
                    </div>
                    {loading && <p className="px-5 py-3 text-xs text-blue-700" role="status">Đang cập nhật số liệu...</p>}
                    {sortedEndpoints.length === 0 ? (
                        <div className="px-5 py-14 text-center text-sm text-slate-500">Chưa có request API trong tiến trình hiện tại.</div>
                    ) : (
                        <div className="overflow-x-auto">
                            <table className="w-full min-w-[1040px] text-left">
                                <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                                    <tr><th className="px-4 py-3 font-semibold">Endpoint</th><th className="px-4 py-3 font-semibold">Lỗi</th><th className="px-4 py-3 font-semibold">Tỷ lệ lỗi</th><th className="px-4 py-3 font-semibold">Trung bình</th><th className="px-4 py-3 font-semibold">Cao nhất</th><th className="px-4 py-3 font-semibold">Lưu lượng theo thời gian</th></tr>
                                </thead>
                                <tbody>{sortedEndpoints.map((endpoint) => <EndpointRow endpoint={endpoint} hours={hours} key={endpoint.endpoint} to={metrics.to} />)}</tbody>
                            </table>
                        </div>
                    )}
                </section>
            </>}
            {!metrics && loading && <div className="mt-7 rounded-2xl border border-slate-200 bg-white p-12 text-center text-sm text-slate-600" role="status">Đang tải thống kê API...</div>}
        </main>
    )
}
