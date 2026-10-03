import { useCallback, useEffect, useState } from 'react'
import { Check, Clock3, History, X, XCircle } from 'lucide-react'
import {
    getAdminJobs,
    getJobStatusHistory,
    updateAdminJobStatus,
} from '../../features/admin/adminApi'
import type {
    AdminJobResponse,
    AdminJobStatus,
    JobStatusHistoryResponse,
} from '../../features/admin/adminTypes'

const STATUS_LABELS: Record<AdminJobStatus, string> = {
    DRAFT: 'Bản nháp', PENDING: 'Chờ duyệt', ACTIVE: 'Đang tuyển',
    EXPIRED: 'Hết hạn', CLOSED: 'Đã đóng', REJECTED: 'Bị từ chối',
}

export function AdminJobsPage() {
    const [jobs, setJobs] = useState<AdminJobResponse[]>([])
    const [statusFilter, setStatusFilter] = useState<AdminJobStatus | ''>('PENDING')
    const [page, setPage] = useState(1)
    const [totalPages, setTotalPages] = useState(1)
    const [isLoading, setIsLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [action, setAction] = useState<{ job: AdminJobResponse; status: AdminJobStatus } | null>(null)
    const [reason, setReason] = useState('')
    const [busy, setBusy] = useState(false)
    const [historyJobId, setHistoryJobId] = useState<number | null>(null)
    const [history, setHistory] = useState<JobStatusHistoryResponse[]>([])

    const loadJobs = useCallback(async () => {
        setIsLoading(true)
        setError(null)
        try {
            const result = await getAdminJobs({ page, size: 10, status: statusFilter || undefined })
            setJobs(result.content ?? [])
            setTotalPages(Math.max(1, result.totalPages ?? 1))
        } catch (cause) {
            setError(cause instanceof Error ? cause.message : 'Không thể tải danh sách tin.')
        } finally {
            setIsLoading(false)
        }
    }, [page, statusFilter])

    useEffect(() => { void loadJobs() }, [loadJobs])

    async function openHistory(jobId: number) {
        setHistoryJobId(jobId)
        setHistory([])
        try {
            setHistory(await getJobStatusHistory(jobId))
        } catch (cause) {
            setError(cause instanceof Error ? cause.message : 'Không thể tải lịch sử kiểm duyệt.')
        }
    }

    async function submitAction() {
        if (!action) return
        if (action.status !== 'ACTIVE' && !reason.trim()) {
            setError('Vui lòng nhập lý do từ chối hoặc gỡ tin.')
            return
        }
        setBusy(true)
        setError(null)
        try {
            await updateAdminJobStatus(action.job.id, action.status, reason.trim() || undefined)
            setAction(null)
            setReason('')
            await loadJobs()
            if (historyJobId === action.job.id) await openHistory(action.job.id)
        } catch (cause) {
            setError(cause instanceof Error ? cause.message : 'Không thể kiểm duyệt tin.')
        } finally {
            setBusy(false)
        }
    }

    return (
        <section className="space-y-5">
            <header className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
                <div>
                    <span className="bento-badge border-indigo-200 bg-indigo-50 text-indigo-700">MODERATION</span>
                    <h1 className="mt-2 text-2xl font-black text-slate-900">Kiểm duyệt tin tuyển dụng</h1>
                    <p className="mt-1 text-sm text-slate-500">Chỉ tin được duyệt mới hiển thị công khai và nhận hồ sơ.</p>
                </div>
                <label className="flex items-center gap-2 text-sm font-semibold text-slate-600">
                    Trạng thái
                    <select value={statusFilter} onChange={(event) => { setStatusFilter(event.target.value as AdminJobStatus | ''); setPage(1) }} className="rounded-xl border border-slate-200 bg-white px-3 py-2">
                        <option value="">Tất cả</option>
                        {Object.entries(STATUS_LABELS).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
                    </select>
                </label>
            </header>

            {error && <div role="alert" className="rounded-xl border border-rose-200 bg-rose-50 p-3 text-sm text-rose-800">{error}</div>}

            {isLoading ? <div className="bento-card p-10 text-center text-sm text-slate-500">Đang tải tin tuyển dụng…</div> : jobs.length === 0 ? (
                <div className="bento-card p-10 text-center text-sm text-slate-500">Không có tin ở trạng thái này.</div>
            ) : <div className="space-y-3">
                {jobs.map((job) => <article key={job.id} className="bento-card flex flex-col gap-4 p-4 sm:p-5 lg:flex-row lg:items-center lg:justify-between">
                    <div className="min-w-0 space-y-1">
                        <div className="flex flex-wrap items-center gap-2 text-xs">
                            <span className="rounded-full border border-amber-200 bg-amber-50 px-2.5 py-1 font-bold text-amber-800">{STATUS_LABELS[job.status]}</span>
                            <span className="text-slate-500">Tin #{job.id}</span>
                        </div>
                        <h2 className="break-words text-base font-bold text-slate-900">{job.title}</h2>
                        <p className="text-sm text-slate-600">{job.companyName || 'Chưa có doanh nghiệp'} · {job.city || 'Chưa có địa điểm'}</p>
                        <p className="text-xs text-slate-500">Hạn nộp: {job.deadline || 'Chưa thiết lập'}</p>
                        <details className="pt-2 text-sm">
                            <summary className="cursor-pointer font-semibold text-indigo-700">Xem nội dung để kiểm duyệt</summary>
                            <div className="mt-3 space-y-3 rounded-xl bg-slate-50 p-3 text-slate-700">
                                <div><h3 className="text-xs font-bold uppercase text-slate-500">Mô tả</h3><p className="mt-1 whitespace-pre-wrap">{job.description || 'Chưa có mô tả.'}</p></div>
                                <div><h3 className="text-xs font-bold uppercase text-slate-500">Yêu cầu</h3><p className="mt-1 whitespace-pre-wrap">{job.requirements || 'Chưa có yêu cầu.'}</p></div>
                                <div><h3 className="text-xs font-bold uppercase text-slate-500">Quyền lợi</h3><p className="mt-1 whitespace-pre-wrap">{job.benefits || 'Chưa có thông tin quyền lợi.'}</p></div>
                                <div><h3 className="text-xs font-bold uppercase text-slate-500">Kỹ năng</h3><p className="mt-1">{job.skills?.join(', ') || 'Chưa chỉ định kỹ năng.'}</p></div>
                                <p className="text-xs text-slate-500">Nhà tuyển dụng #{job.recruiterUserId ?? 'Chưa có thông tin'} · Loại tin {job.jobType || 'Chưa xác định'} · Cấp độ {job.experienceLevel || 'Chưa xác định'}</p>
                            </div>
                        </details>
                    </div>
                    <div className="flex flex-wrap items-center gap-2">
                        {job.status === 'PENDING' && <>
                            <button type="button" onClick={() => { setAction({ job, status: 'ACTIVE' }); setReason('') }} className="inline-flex items-center gap-1.5 rounded-lg bg-emerald-600 px-3 py-2 text-xs font-bold text-white"><Check size={14} />Duyệt</button>
                            <button type="button" onClick={() => { setAction({ job, status: 'REJECTED' }); setReason('') }} className="inline-flex items-center gap-1.5 rounded-lg bg-rose-600 px-3 py-2 text-xs font-bold text-white"><XCircle size={14} />Từ chối</button>
                        </>}
                        {job.status === 'ACTIVE' && <button type="button" onClick={() => { setAction({ job, status: 'CLOSED' }); setReason('') }} className="inline-flex items-center gap-1.5 rounded-lg border border-rose-200 px-3 py-2 text-xs font-bold text-rose-700"><X size={14} />Gỡ tin</button>}
                        <button type="button" onClick={() => void openHistory(job.id)} className="inline-flex items-center gap-1.5 rounded-lg border border-slate-200 px-3 py-2 text-xs font-bold text-slate-700"><History size={14} />Lịch sử</button>
                    </div>
                </article>)}
            </div>}

            <div className="flex items-center justify-between text-sm text-slate-600">
                <span>Trang {page} / {totalPages}</span>
                <div className="flex gap-2">
                    <button disabled={page <= 1} onClick={() => setPage((value) => Math.max(1, value - 1))} className="rounded-lg border border-slate-200 px-3 py-1.5 disabled:opacity-40">Trước</button>
                    <button disabled={page >= totalPages} onClick={() => setPage((value) => Math.min(totalPages, value + 1))} className="rounded-lg border border-slate-200 px-3 py-1.5 disabled:opacity-40">Sau</button>
                </div>
            </div>

            {action && <div className="fixed inset-0 z-50 grid place-items-center bg-slate-950/50 p-4" role="dialog" aria-modal="true" aria-labelledby="moderation-title">
                <div className="w-full max-w-lg rounded-2xl bg-white p-5 shadow-2xl">
                    <h2 id="moderation-title" className="text-lg font-black text-slate-900">{action.status === 'ACTIVE' ? 'Duyệt tin tuyển dụng' : action.status === 'REJECTED' ? 'Từ chối tin tuyển dụng' : 'Gỡ tin tuyển dụng'}</h2>
                    <p className="mt-1 text-sm text-slate-600">{action.job.title} · #{action.job.id}</p>
                    {action.status !== 'ACTIVE' && <label className="mt-4 block text-sm font-semibold text-slate-700">Lý do bắt buộc<textarea value={reason} onChange={(event) => setReason(event.target.value)} maxLength={1000} rows={4} className="mt-2 w-full rounded-xl border border-slate-200 p-3 text-sm" /></label>}
                    <div className="mt-5 flex justify-end gap-2">
                        <button type="button" disabled={busy} onClick={() => setAction(null)} className="rounded-lg border border-slate-200 px-4 py-2 text-sm font-semibold">Hủy</button>
                        <button type="button" disabled={busy} onClick={() => void submitAction()} className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-bold text-white">{busy ? 'Đang lưu…' : 'Xác nhận'}</button>
                    </div>
                </div>
            </div>}

            {historyJobId !== null && <div className="fixed inset-0 z-40 grid place-items-center bg-slate-950/50 p-4" role="dialog" aria-modal="true" aria-labelledby="history-title">
                <div className="max-h-[85vh] w-full max-w-2xl overflow-y-auto rounded-2xl bg-white p-5 shadow-2xl">
                    <div className="flex items-center justify-between"><h2 id="history-title" className="text-lg font-black">Lịch sử tin #{historyJobId}</h2><button type="button" onClick={() => setHistoryJobId(null)} aria-label="Đóng lịch sử"><X size={18} /></button></div>
                    {history.length === 0 ? <p className="py-8 text-center text-sm text-slate-500">Chưa có lịch sử kiểm duyệt.</p> : <ol className="mt-4 space-y-3">
                        {history.map((item) => <li key={item.id} className="rounded-xl border border-slate-200 p-3">
                            <div className="flex flex-wrap items-center gap-2 text-sm font-bold"><Clock3 size={14} />{item.fromStatus ? STATUS_LABELS[item.fromStatus] : 'Mới tạo'} → {STATUS_LABELS[item.toStatus]}</div>
                            <p className="mt-1 text-xs text-slate-600">Người thao tác #{item.changedByUserId} · {new Date(item.changedAt).toLocaleString('vi-VN')}</p>
                            {item.reason && <p className="mt-1 text-sm text-slate-700">Lý do: {item.reason}</p>}
                        </li>)}
                    </ol>}
                </div>
            </div>}
        </section>
    )
}
