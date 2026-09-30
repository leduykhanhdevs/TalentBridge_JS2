import { useEffect, useState } from 'react'
import {
    AlertCircle,
    Archive,
    Briefcase,
    Calendar,
    CheckCircle2,
    Clock,
    DollarSign,
    Edit3,
    ExternalLink,
    MapPin,
    Plus,
    Search,
    Users,
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
    EXPERIENCE_LABELS,
    JOB_TYPE_LABELS,
    formatJobSalary,
} from '../../features/recruiter/recruiterJobValidation'
import { JobFormModal } from '../../features/recruiter/components/JobFormModal'

export function RecruiterJobsPage() {
    const [jobs, setJobs] = useState<RecruiterJobItem[]>([])
    const [isLoading, setIsLoading] = useState(true)
    const [errorMsg, setErrorMsg] = useState<string | null>(null)
    const [successMsg, setSuccessMsg] = useState<string | null>(null)

    // Filter states
    const [statusFilter, setStatusFilter] = useState<JobStatus | ''>('')
    const [searchTerm, setSearchTerm] = useState('')
    const [page, setPage] = useState(1)
    const [pageSize] = useState(10)
    const [totalPages, setTotalPages] = useState(1)
    const [totalJobs, setTotalJobs] = useState(0)

    // Modal state
    const [isModalOpen, setIsModalOpen] = useState(false)
    const [editingJob, setEditingJob] = useState<RecruiterJobItem | null>(null)
    const [isSubmitting, setIsSubmitting] = useState(false)

    // Confirm close modal
    const [confirmCloseId, setConfirmCloseId] = useState<number | null>(null)

    async function loadJobs() {
        setIsLoading(true)
        setErrorMsg(null)
        try {
            const res = await getMyJobs({
                page,
                size: pageSize,
                status: statusFilter,
            })
            setJobs(res.content || [])
            setTotalPages(res.totalPages || 1)
            setTotalJobs(res.totalElements || 0)
        } catch (err: unknown) {
            const msg = err instanceof Error ? err.message : 'Không thể tải danh sách tin tuyển dụng.'
            setErrorMsg(msg)
        } finally {
            setIsLoading(false)
        }
    }

    useEffect(() => {
        loadJobs()
    }, [page, statusFilter])

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

    const publishedCount = jobs.filter((j) => j.status === 'PUBLISHED').length
    const closedCount = jobs.filter((j) => j.status === 'CLOSED').length

    async function handleFormSubmit(payload: CreateJobPayload) {
        setIsSubmitting(true)
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
            setErrorMsg(msg)
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
        <div className="space-y-6">
            {/* Header Title & CTA */}
            <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <div className="flex items-center gap-2">
                        <span className="bento-badge border-indigo-200 bg-indigo-50 text-indigo-700 text-[10px]">
                            JOB_MANAGEMENT
                        </span>
                        <span className="text-xs font-semibold text-slate-500">• My Jobs Dashboard</span>
                    </div>
                    <h1 className="mt-1 text-2xl font-bold tracking-tight text-slate-900">
                        Quản lý tin tuyển dụng
                    </h1>
                    <p className="mt-1 text-xs text-slate-500">
                        Đăng tuyển, theo dõi trạng thái và quản trị các cơ hội việc làm của doanh nghiệp
                    </p>
                </div>

                <button
                    type="button"
                    onClick={() => {
                        setEditingJob(null)
                        setIsModalOpen(true)
                    }}
                    className="inline-flex items-center justify-center gap-2 rounded-xl bg-indigo-600 px-5 py-2.5 text-xs font-bold text-white shadow-md shadow-indigo-600/20 hover:bg-indigo-700 transition"
                >
                    <Plus size={16} />
                    <span>Đăng tin tuyển dụng mới</span>
                </button>
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

            {/* Top Stat Cards */}
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
                <div className="bento-card p-5">
                    <div className="flex items-center justify-between">
                        <span className="text-xs font-bold uppercase tracking-wider text-slate-500">
                            Tổng tin tuyển dụng
                        </span>
                        <div className="grid size-8 place-items-center rounded-xl bg-slate-100 text-slate-700">
                            <Briefcase size={16} />
                        </div>
                    </div>
                    <p className="mt-2 text-2xl font-black text-slate-900">{totalJobs}</p>
                    <span className="text-[11px] text-slate-500">Tất cả bài đăng của công ty</span>
                </div>

                <div className="bento-card p-5">
                    <div className="flex items-center justify-between">
                        <span className="text-xs font-bold uppercase tracking-wider text-emerald-700">
                            Đang tuyển dụng
                        </span>
                        <div className="grid size-8 place-items-center rounded-xl bg-emerald-50 text-emerald-600 border border-emerald-100">
                            <Clock size={16} />
                        </div>
                    </div>
                    <p className="mt-2 text-2xl font-black text-emerald-700">{publishedCount}</p>
                    <span className="text-[11px] text-emerald-600">Đang hiển thị công khai</span>
                </div>

                <div className="bento-card p-5">
                    <div className="flex items-center justify-between">
                        <span className="text-xs font-bold uppercase tracking-wider text-slate-500">
                            Đã đóng / Hết hạn
                        </span>
                        <div className="grid size-8 place-items-center rounded-xl bg-slate-100 text-slate-500 border border-slate-200">
                            <Archive size={16} />
                        </div>
                    </div>
                    <p className="mt-2 text-2xl font-black text-slate-600">{closedCount}</p>
                    <span className="text-[11px] text-slate-500">Ngừng tiếp nhận hồ sơ</span>
                </div>
            </div>

            {/* Filter & Search Bar */}
            <div className="bento-card p-4">
                <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                    {/* Status Tabs */}
                    <div className="flex items-center gap-1.5 p-1 rounded-xl bg-slate-100/80 border border-slate-200/60 w-fit">
                        <button
                            type="button"
                            onClick={() => {
                                setStatusFilter('')
                                setPage(1)
                            }}
                            className={`px-3 py-1.5 rounded-lg text-xs font-bold transition ${
                                statusFilter === ''
                                    ? 'bg-white text-slate-900 shadow-2xs'
                                    : 'text-slate-600 hover:text-slate-900'
                            }`}
                        >
                            Tất cả ({totalJobs})
                        </button>
                        <button
                            type="button"
                            onClick={() => {
                                setStatusFilter('PUBLISHED')
                                setPage(1)
                            }}
                            className={`px-3 py-1.5 rounded-lg text-xs font-bold transition ${
                                statusFilter === 'PUBLISHED'
                                    ? 'bg-emerald-600 text-white shadow-2xs'
                                    : 'text-slate-600 hover:text-slate-900'
                            }`}
                        >
                            Đang tuyển
                        </button>
                        <button
                            type="button"
                            onClick={() => {
                                setStatusFilter('CLOSED')
                                setPage(1)
                            }}
                            className={`px-3 py-1.5 rounded-lg text-xs font-bold transition ${
                                statusFilter === 'CLOSED'
                                    ? 'bg-slate-700 text-white shadow-2xs'
                                    : 'text-slate-600 hover:text-slate-900'
                            }`}
                        >
                            Đã đóng
                        </button>
                    </div>

                    {/* Search Input */}
                    <div className="relative w-full sm:w-72">
                        <Search size={15} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
                        <input
                            type="text"
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            placeholder="Tìm theo tiêu đề, kỹ năng..."
                            className="w-full rounded-xl border border-slate-200 bg-white pl-9 pr-3.5 py-2 text-xs text-slate-900 placeholder:text-slate-400 focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-100"
                        />
                    </div>
                </div>
            </div>

            {/* Job List Table / Cards */}
            {isLoading ? (
                <div className="bento-card p-12 text-center">
                    <div className="inline-block size-8 animate-spin rounded-full border-3 border-indigo-600 border-t-transparent" />
                    <p className="mt-3 text-xs font-semibold text-slate-500">Đang tải danh sách tin tuyển dụng...</p>
                </div>
            ) : filteredJobs.length === 0 ? (
                <div className="bento-card p-12 text-center">
                    <div className="mx-auto grid size-12 place-items-center rounded-2xl bg-indigo-50 text-indigo-600 border border-indigo-100">
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
                            setIsModalOpen(true)
                        }}
                        className="mt-5 inline-flex items-center gap-2 rounded-xl bg-indigo-600 px-4 py-2 text-xs font-bold text-white hover:bg-indigo-700 transition shadow-sm"
                    >
                        <Plus size={14} />
                        <span>Đăng tin tuyển dụng ngay</span>
                    </button>
                </div>
            ) : (
                <div className="space-y-3">
                    {filteredJobs.map((job) => {
                        const isPublished = job.status === 'PUBLISHED'

                        return (
                            <div
                                key={job.id}
                                className="bento-card p-5 transition hover:border-indigo-200 hover:shadow-md"
                            >
                                <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
                                    {/* Left Info */}
                                    <div className="space-y-2">
                                        <div className="flex flex-wrap items-center gap-2">
                                            <span
                                                className={`bento-badge text-[10px] ${
                                                    isPublished
                                                        ? 'border-emerald-200 bg-emerald-50 text-emerald-700'
                                                        : 'border-slate-200 bg-slate-100 text-slate-600'
                                                }`}
                                            >
                                                {isPublished ? 'ĐANG TUYỂN DỤNG' : 'ĐÃ ĐÓNG'}
                                            </span>
                                            <span className="text-xs font-bold text-indigo-600">
                                                {JOB_TYPE_LABELS[job.jobType] || job.jobType}
                                            </span>
                                            <span className="text-slate-300">•</span>
                                            <span className="text-xs text-slate-600 font-medium">
                                                {EXPERIENCE_LABELS[job.experienceLevel] || job.experienceLevel}
                                            </span>
                                        </div>

                                        <h3 className="text-base font-bold text-slate-900 hover:text-indigo-600 transition">
                                            <Link to={`/jobs/${job.id}`} target="_blank">
                                                {job.title}
                                            </Link>
                                        </h3>

                                        <div className="flex flex-wrap items-center gap-x-4 gap-y-1.5 text-xs text-slate-500">
                                            <span className="flex items-center gap-1">
                                                <MapPin size={13} className="text-slate-400" />
                                                {job.city} {job.location ? `(${job.location})` : ''}
                                            </span>
                                            <span className="flex items-center gap-1 font-semibold text-emerald-700">
                                                <DollarSign size={13} />
                                                {formatJobSalary(job.minSalary, job.maxSalary, job.isNegotiable)}
                                            </span>
                                            <span className="flex items-center gap-1">
                                                <Calendar size={13} className="text-slate-400" />
                                                Hạn nộp:{' '}
                                                {job.deadline
                                                    ? new Date(job.deadline).toLocaleDateString('vi-VN')
                                                    : 'Chưa đặt'}
                                            </span>
                                        </div>

                                        {/* Skills Tag */}
                                        {job.skills && job.skills.length > 0 && (
                                            <div className="flex flex-wrap gap-1.5 pt-1">
                                                {job.skills.map((skill) => (
                                                    <span
                                                        key={skill}
                                                        className="rounded-md bg-slate-100 px-2 py-0.5 text-[11px] font-medium text-slate-700"
                                                    >
                                                        {skill}
                                                    </span>
                                                ))}
                                            </div>
                                        )}
                                    </div>

                                    {/* Right Actions */}
                                    <div className="flex items-center gap-2 pt-3 border-t border-slate-100 lg:border-t-0 lg:pt-0 shrink-0">
                                        <Link
                                            to={`/recruiter/jobs/${job.id}/applicants`}
                                            className="inline-flex items-center gap-1.5 rounded-xl border border-indigo-600 bg-indigo-600 px-3.5 py-2 text-xs font-bold text-white hover:bg-indigo-700 shadow-2xs transition"
                                            title="Xem và sàng lọc hồ sơ ứng viên"
                                        >
                                            <Users size={13} />
                                            <span>Xem ứng viên</span>
                                        </Link>

                                        <Link
                                            to={`/jobs/${job.id}`}
                                            target="_blank"
                                            className="inline-flex items-center gap-1.5 rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-bold text-slate-700 hover:bg-slate-50 transition"
                                            title="Xem trang công khai"
                                        >
                                            <ExternalLink size={13} />
                                            <span>Xem trang</span>
                                        </Link>

                                        <button
                                            type="button"
                                            onClick={() => {
                                                setEditingJob(job)
                                                setIsModalOpen(true)
                                            }}
                                            className="inline-flex items-center gap-1.5 rounded-xl border border-indigo-200 bg-indigo-50 px-3 py-2 text-xs font-bold text-indigo-700 hover:bg-indigo-100 transition"
                                            title="Chỉnh sửa tin"
                                        >
                                            <Edit3 size={13} />
                                            <span>Chỉnh sửa</span>
                                        </button>

                                        {isPublished && (
                                            <button
                                                type="button"
                                                onClick={() => setConfirmCloseId(job.id)}
                                                className="inline-flex items-center gap-1.5 rounded-xl border border-rose-200 bg-rose-50 px-3 py-2 text-xs font-bold text-rose-700 hover:bg-rose-100 transition"
                                                title="Đóng tin tuyển dụng"
                                            >
                                                <Archive size={13} />
                                                <span>Đóng tin</span>
                                            </button>
                                        )}
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
                }}
                onSubmit={handleFormSubmit}
                initialData={editingJob}
                isSubmitting={isSubmitting}
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
