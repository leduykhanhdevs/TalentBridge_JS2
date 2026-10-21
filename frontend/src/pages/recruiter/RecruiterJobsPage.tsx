import { useEffect, useState, useCallback } from 'react'
import {
    AlertCircle,
    Archive,
    BarChart3,
    Briefcase,
    CheckCircle2,
    Edit3,
    ExternalLink,
    MoreHorizontal,
    Plus,
    Search,
} from 'lucide-react'
import { Link } from 'react-router'
import {
    closeJob,
    createJob,
    getMyJobs,
    updateJob,
} from '../../features/recruiter/recruiterJobApi'
import type {
    CreateJobPayload,
    JobStatus,
    RecruiterJobItem,
} from '../../features/recruiter/recruiterJobTypes'
import {
    formatJobSalary,
} from '../../features/recruiter/recruiterJobValidation'
import { JobFormModal } from '../../features/recruiter/components/JobFormModal'

const JOB_STATUS_TABS: Array<{ label: string; value: JobStatus | '' }> = [
    { label: 'Tất cả', value: '' },
    { label: 'Đang mở', value: 'ACTIVE' },
    { label: 'Chờ duyệt', value: 'PENDING' },
    { label: 'Bản nháp', value: 'DRAFT' },
]

const JOB_STATUS_LABELS: Record<JobStatus, string> = {
    DRAFT: 'Bản nháp',
    PENDING: 'Chờ quản trị duyệt',
    ACTIVE: 'Đang nhận hồ sơ',
    EXPIRED: 'Đã hết hạn',
    CLOSED: 'Đã đóng',
    REJECTED: 'Bị từ chối',
}

const JOB_STATUS_STYLES: Record<JobStatus, string> = {
    DRAFT: 'bg-slate-100 text-slate-700',
    PENDING: 'bg-amber-50 text-amber-800',
    ACTIVE: 'bg-emerald-50 text-emerald-800',
    EXPIRED: 'bg-slate-100 text-slate-700',
    CLOSED: 'bg-slate-100 text-slate-700',
    REJECTED: 'bg-rose-50 text-rose-800',
}

export function RecruiterJobsPage() {
    const [jobs, setJobs] = useState<RecruiterJobItem[]>([])
    const [isLoading, setIsLoading] = useState(true)
    const [errorMsg, setErrorMsg] = useState<string | null>(null)
    const [successMsg, setSuccessMsg] = useState<string | null>(null)

    const [statusFilter, setStatusFilter] = useState<JobStatus | ''>('')
    const [searchTerm, setSearchTerm] = useState('')
    const [isSearchOpen, setIsSearchOpen] = useState(false)
    const [page, setPage] = useState(1)
    const [pageSize] = useState(10)
    const [totalPages, setTotalPages] = useState(1)
    const [totalJobs, setTotalJobs] = useState(0)

    // Modal state
    const [isModalOpen, setIsModalOpen] = useState(false)
    const [editingJob, setEditingJob] = useState<RecruiterJobItem | null>(null)
    const [isSubmitting, setIsSubmitting] = useState(false)
    const [submitError, setSubmitError] = useState<string | null>(null)

    // Confirm close modal
    const [confirmCloseId, setConfirmCloseId] = useState<number | null>(null)

    const loadJobs = useCallback(async () => {
        setIsLoading(true)
        setErrorMsg(null)
        try {
            const res = await getMyJobs({ page, size: pageSize, status: statusFilter })
            setJobs(res.content || [])
            setTotalPages(res.totalPages || 1)
            setTotalJobs(res.totalElements || 0)
        } catch (err: unknown) {
            const msg = err instanceof Error ? err.message : 'Không thể tải danh sách tin tuyển dụng.'
            setErrorMsg(msg)
        } finally {
            setIsLoading(false)
        }
    }, [page, pageSize, statusFilter])

    useEffect(() => {
        loadJobs()
    }, [loadJobs])

    // Filter jobs client-side by keyword
    const filteredJobs = jobs.filter((job) => {
        if (!searchTerm.trim()) return true
        const term = searchTerm.toLowerCase()
        return (
            job.title.toLowerCase().includes(term) ||
            job.city.toLowerCase().includes(term) ||
            job.skills?.some((s) => s.toLowerCase().includes(term))
        )
    })

    async function handleFormSubmit(payload: CreateJobPayload) {
        setIsSubmitting(true)
        setSubmitError(null)
        setErrorMsg(null)
        try {
            if (editingJob) {
                await updateJob(editingJob.id, payload)
                setSuccessMsg('Cập nhật tin tuyển dụng thành công!')
            } else {
                await createJob(payload)
                setSuccessMsg('Đăng tin tuyển dụng mới thành công!')
            }
            setIsModalOpen(false)
            setEditingJob(null)
            await loadJobs()
            setTimeout(() => setSuccessMsg(null), 4000)
        } catch (err: unknown) {
            const msg = err instanceof Error ? err.message : 'Thao tác không thành công.'
            setSubmitError(msg)
        } finally {
            setIsSubmitting(false)
        }
    }

    async function handleCloseJob(id: number) {
        try {
            await closeJob(id)
            setSuccessMsg('Đã đóng tin tuyển dụng thành công!')
            setConfirmCloseId(null)
            await loadJobs()
            setTimeout(() => setSuccessMsg(null), 4000)
        } catch (err: unknown) {
            const msg = err instanceof Error ? err.message : 'Không thể đóng tin tuyển dụng.'
            setErrorMsg(msg)
        }
    }

    return (
        <div className="space-y-5">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <p className="text-[11px] font-bold uppercase tracking-[0.12em] text-[#1D4ED8]">
                        Không gian nhà tuyển dụng
                    </p>
                    <h1 className="mt-1 text-2xl font-bold tracking-tight text-[#172033] sm:text-[28px]">
                        Tin tuyển dụng của doanh nghiệp
                    </h1>
                    <p className="mt-1 text-sm text-slate-600">
                        Tập trung vào chất lượng thông tin và trạng thái nhận hồ sơ.
                    </p>
                </div>

                <div className="flex shrink-0 items-center gap-2">
                    <button
                        aria-expanded={isSearchOpen}
                        aria-label={isSearchOpen ? 'Đóng tìm kiếm tin tuyển dụng' : 'Tìm tin tuyển dụng'}
                        className="grid size-10 place-items-center rounded-lg border border-slate-200 bg-white text-slate-600 transition hover:bg-slate-50 hover:text-[#1D4ED8]"
                        onClick={() => setIsSearchOpen((open) => !open)}
                        type="button"
                    >
                        <Search aria-hidden="true" size={17} />
                    </button>
                    <button
                        type="button"
                        onClick={() => {
                            setEditingJob(null)
                            setSubmitError(null)
                            setIsModalOpen(true)
                        }}
                        className="inline-flex min-h-10 items-center justify-center gap-2 rounded-lg bg-[#1D4ED8] px-4 py-2 text-sm font-semibold text-white transition hover:bg-blue-800"
                    >
                        <Plus aria-hidden="true" size={16} />
                        <span>Tạo tin tuyển dụng</span>
                    </button>
                </div>
            </div>

            {/* Notifications */}
            {successMsg && (
                <div className="flex items-center gap-2.5 rounded-2xl border border-emerald-200 bg-emerald-50/80 p-4 text-xs font-bold text-emerald-800">
                    <CheckCircle2 size={16} className="text-emerald-600 shrink-0" />
                    <span>{successMsg}</span>
                </div>
            )}

            {errorMsg && (
                <div className="flex items-center gap-2.5 rounded-2xl border border-rose-200 bg-rose-50/80 p-4 text-xs font-bold text-rose-800">
                    <AlertCircle size={16} className="text-rose-600 shrink-0" />
                    <span>{errorMsg}</span>
                </div>
            )}

            <div className="border-b border-slate-200 pb-3">
                <div className="flex flex-wrap items-center justify-between gap-3">
                    <nav aria-label="Lọc tin theo trạng thái" className="flex min-w-0 flex-wrap gap-1.5">
                        {JOB_STATUS_TABS.map(({ label, value }) => (
                            <button
                                aria-pressed={statusFilter === value}
                                className={`min-h-9 rounded-lg px-3 text-xs font-semibold transition-colors ${statusFilter === value ? 'bg-[#EFF4FF] text-[#1D4ED8]' : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'}`}
                                key={label}
                                onClick={() => {
                                    setStatusFilter(value)
                                    setPage(1)
                                }}
                                type="button"
                            >
                                {label}
                            </button>
                        ))}
                    </nav>
                    <p className="text-xs text-slate-500" aria-live="polite">
                        {isLoading ? 'Đang tải tin tuyển dụng…' : `${totalJobs} tin tuyển dụng`}
                    </p>
                </div>
                {isSearchOpen && (
                    <label className="relative mt-3 block w-full sm:max-w-sm">
                        <span className="sr-only">Tìm theo tiêu đề, địa điểm hoặc kỹ năng</span>
                        <Search aria-hidden="true" className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={15} />
                        <input
                            autoFocus
                            className="w-full rounded-lg border border-slate-200 bg-white py-2.5 pl-9 pr-3 text-sm text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-[#1D4ED8] focus:ring-2 focus:ring-blue-100"
                            onChange={(event) => setSearchTerm(event.target.value)}
                            placeholder="Tìm theo tiêu đề, địa điểm, kỹ năng"
                            type="search"
                            value={searchTerm}
                        />
                    </label>
                )}
            </div>

            {/* Job List Table / Cards */}
            {isLoading ? (
                <div className="bento-card p-12 text-center">
                    <div className="inline-block size-8 animate-spin rounded-full border-3 border-blue-700 border-t-transparent" />
                    <p className="mt-3 text-xs font-semibold text-slate-500">Đang tải danh sách tin tuyển dụng...</p>
                </div>
            ) : filteredJobs.length === 0 ? (
                <div className="bento-card p-12 text-center">
                    <div className="mx-auto grid size-12 place-items-center rounded-2xl bg-blue-50 text-blue-700 border border-blue-100">
                        <Briefcase size={24} />
                    </div>
                    <h3 className="mt-4 text-base font-bold text-slate-900">Chưa có tin tuyển dụng nào</h3>
                    <p className="mt-1 text-xs text-slate-500 max-w-sm mx-auto">
                        Doanh nghiệp của bạn chưa đăng tin tuyển dụng hoặc không tìm thấy bài đăng phù hợp với bộ lọc.
                    </p>
                    <button
                        type="button"
                        onClick={() => {
                            setEditingJob(null)
                            setSubmitError(null)
                            setIsModalOpen(true)
                        }}
                        className="mt-5 inline-flex items-center gap-2 rounded-xl bg-blue-700 px-4 py-2 text-xs font-bold text-white hover:bg-blue-800 transition shadow-sm"
                    >
                        <Plus size={14} />
                        <span>Đăng tin tuyển dụng ngay</span>
                    </button>
                </div>
            ) : (
                <div className="space-y-3">
                    {filteredJobs.map((job) => {
                        const isPublished = job.status === 'ACTIVE'

                        return (
                            <div
                                key={job.id}
                                className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm transition hover:border-blue-200 sm:p-5"
                            >
                                <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                                    <div className="min-w-0">
                                        <h2 className="truncate text-base font-bold text-[#172033] sm:text-lg">
                                            <Link className="transition-colors hover:text-[#1D4ED8]" to={`/jobs/${job.id}`}>
                                                {job.title}
                                            </Link>
                                        </h2>
                                        <p className="mt-1 text-sm text-slate-600">
                                            {formatJobSalary(job.minSalary, job.maxSalary, job.isNegotiable)}
                                            <span aria-hidden="true" className="px-2 text-slate-300">·</span>
                                            Hạn {job.deadline ? new Date(job.deadline).toLocaleDateString('vi-VN') : 'chưa đặt'}
                                        </p>
                                        <span className={`mt-2 inline-flex rounded-md px-2 py-1 text-[11px] font-semibold ${JOB_STATUS_STYLES[job.status]}`}>
                                            {JOB_STATUS_LABELS[job.status]}
                                        </span>
                                    </div>

                                    <div className="flex shrink-0 items-center gap-2 border-t border-slate-100 pt-3 sm:border-0 sm:pt-0">
                                        {isPublished ? (
                                            <Link
                                                className="inline-flex min-h-9 items-center justify-center rounded-lg bg-[#1D4ED8] px-4 text-xs font-semibold text-white transition hover:bg-blue-800"
                                                to={`/recruiter/jobs/${job.id}/applicants`}
                                            >
                                                Xem ứng viên
                                            </Link>
                                        ) : (
                                            <button
                                                className="inline-flex min-h-9 items-center justify-center rounded-lg border border-slate-300 bg-white px-4 text-xs font-semibold text-slate-800 transition hover:bg-slate-50"
                                                onClick={() => {
                                                    setEditingJob(job)
                                                    setIsModalOpen(true)
                                                }}
                                                type="button"
                                            >
                                                Chỉnh sửa tin
                                            </button>
                                        )}
                                        <details className="group relative">
                                            <summary aria-label={`Thao tác khác cho ${job.title}`} className="grid size-9 cursor-pointer list-none place-items-center rounded-lg border border-slate-200 bg-white text-slate-600 transition hover:bg-slate-50 hover:text-[#1D4ED8] [&::-webkit-details-marker]:hidden">
                                                <MoreHorizontal aria-hidden="true" size={18} />
                                            </summary>
                                            <div className="absolute right-0 top-10 z-20 grid min-w-44 gap-1 rounded-lg border border-slate-200 bg-white p-1.5 shadow-lg">
                                                <Link className="flex items-center gap-2 rounded-md px-2.5 py-2 text-xs font-medium text-slate-700 hover:bg-slate-50" to={`/jobs/${job.id}`} target="_blank">
                                                    <ExternalLink aria-hidden="true" size={14} /> Xem trang công khai
                                                </Link>
                                                <Link className="flex items-center gap-2 rounded-md px-2.5 py-2 text-xs font-medium text-slate-700 hover:bg-slate-50" to={`/recruiter/statistics?jobId=${job.id}`}>
                                                    <BarChart3 aria-hidden="true" size={14} /> Thống kê tin
                                                </Link>
                                                <button className="flex items-center gap-2 rounded-md px-2.5 py-2 text-left text-xs font-medium text-slate-700 hover:bg-slate-50" onClick={() => { setEditingJob(job); setIsModalOpen(true) }} type="button">
                                                    <Edit3 aria-hidden="true" size={14} /> Chỉnh sửa tin
                                                </button>
                                                {isPublished && (
                                                    <button className="flex items-center gap-2 rounded-md px-2.5 py-2 text-left text-xs font-medium text-rose-700 hover:bg-rose-50" onClick={() => setConfirmCloseId(job.id)} type="button">
                                                        <Archive aria-hidden="true" size={14} /> Đóng tin tuyển dụng
                                                    </button>
                                                )}
                                            </div>
                                        </details>
                                    </div>
                                </div>
                            </div>
                        )
                    })}

                    {/* Pagination */}
                    {totalPages > 1 && (
                        <div className="flex items-center justify-between pt-4">
                            <span className="text-xs text-slate-500">
                                Trang {page} / {totalPages} (Tổng số {totalJobs} tin)
                            </span>
                            <div className="flex gap-1.5">
                                <button
                                    type="button"
                                    disabled={page <= 1}
                                    onClick={() => setPage((p) => Math.max(1, p - 1))}
                                    className="rounded-lg border border-slate-200 px-3 py-1.5 text-xs font-bold text-slate-700 disabled:opacity-40 hover:bg-slate-100 transition"
                                >
                                    Trang trước
                                </button>
                                <button
                                    type="button"
                                    disabled={page >= totalPages}
                                    onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
                                    className="rounded-lg border border-slate-200 px-3 py-1.5 text-xs font-bold text-slate-700 disabled:opacity-40 hover:bg-slate-100 transition"
                                >
                                    Trang sau
                                </button>
                            </div>
                        </div>
                    )}
                </div>
            )}

            {/* Job Form Modal (Create & Edit) */}
            <JobFormModal
                isOpen={isModalOpen}
                onClose={() => {
                    setIsModalOpen(false)
                    setEditingJob(null)
                    setSubmitError(null)
                }}
                onSubmit={handleFormSubmit}
                initialData={editingJob}
                isSubmitting={isSubmitting}
                submitError={submitError}
                onClearSubmitError={() => setSubmitError(null)}
            />

            {/* Confirm Close Dialog */}
            {confirmCloseId !== null && (
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-xs">
                    <div className="w-full max-w-sm rounded-3xl border border-slate-200 bg-white p-6 shadow-2xl">
                        <div className="mx-auto grid size-12 place-items-center rounded-2xl bg-rose-50 text-rose-600 border border-rose-100">
                            <Archive size={24} />
                        </div>
                        <h3 className="mt-4 text-center text-base font-bold text-slate-900">
                            Đóng tin tuyển dụng?
                        </h3>
                        <p className="mt-2 text-center text-xs text-slate-500 leading-relaxed">
                            Sau khi đóng, ứng viên sẽ không thể gửi thêm hồ sơ ứng tuyển vào vị trí này nữa.
                        </p>
                        <div className="mt-6 flex gap-2">
                            <button
                                type="button"
                                onClick={() => setConfirmCloseId(null)}
                                className="flex-1 rounded-xl border border-slate-200 py-2.5 text-xs font-bold text-slate-700 hover:bg-slate-100 transition"
                            >
                                Hủy
                            </button>
                            <button
                                type="button"
                                onClick={() => handleCloseJob(confirmCloseId)}
                                className="flex-1 rounded-xl bg-rose-600 py-2.5 text-xs font-bold text-white hover:bg-rose-700 transition shadow-md shadow-rose-600/20"
                            >
                                Xác nhận đóng
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    )
}
