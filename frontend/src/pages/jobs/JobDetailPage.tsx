import { useQuery } from '@tanstack/react-query'
import {
    ArrowLeft,
    BriefcaseBusiness,
    Building2,
    CalendarDays,
    CircleDollarSign,
    Clock3,
    MapPin,
    RefreshCw,
    TriangleAlert,
    UserRoundCheck,
} from 'lucide-react'
import { Link, useParams } from 'react-router'
import { getJobDetail, JobApiError } from '../../features/jobs/jobApi'
import {
    formatDate,
    formatExperienceLevel,
    formatJobType,
    formatSalaryRange,
} from '../../features/jobs/jobFormatters'

interface DetailSectionProps {
    title: string
    content: string | null
}

function DetailSection({ title, content }: DetailSectionProps) {
    return (
        <section className="rounded-2xl border border-slate-200/80 bg-white p-6 shadow-2xs sm:p-8">
            <h2 className="text-lg font-bold text-slate-900 sm:text-xl">{title}</h2>
            <div className="mt-4 whitespace-pre-line text-sm leading-7 text-slate-600 sm:text-base">
                {content?.trim() || 'Thông tin đang được nhà tuyển dụng cập nhật.'}
            </div>
        </section>
    )
}

function JobNotFoundState() {
    return (
        <section className="mx-auto flex min-h-[60vh] w-full max-w-3xl items-center px-4 py-16 sm:px-6">
            <div className="w-full rounded-3xl border border-slate-200 bg-white p-8 text-center shadow-sm sm:p-12">
                <span className="mx-auto grid size-14 place-items-center rounded-2xl bg-amber-50 text-amber-600">
                    <TriangleAlert aria-hidden="true" size={28} />
                </span>
                <h1 className="mt-5 text-2xl font-bold text-slate-900">Tin tuyển dụng không khả dụng</h1>
                <p className="mx-auto mt-3 max-w-xl text-sm leading-6 text-slate-600 sm:text-base">
                    Tin tuyển dụng có thể không tồn tại, đã hết hạn hoặc không còn được công khai.
                </p>
                <Link className="btn-bento-primary mt-7" to="/">
                    <ArrowLeft aria-hidden="true" size={17} />
                    <span>Về trang chủ</span>
                </Link>
            </div>
        </section>
    )
}

function JobDetailSkeleton() {
    return (
        <div aria-label="Đang tải chi tiết việc làm" className="mx-auto w-full max-w-7xl animate-pulse px-4 py-8 sm:px-6 lg:px-8">
            <div className="h-5 w-40 rounded bg-slate-200" />
            <div className="mt-5 rounded-3xl border border-slate-200 bg-white p-8">
                <div className="h-7 w-2/3 rounded bg-slate-200" />
                <div className="mt-4 h-5 w-1/3 rounded bg-slate-100" />
                <div className="mt-8 grid gap-3 sm:grid-cols-3">
                    <div className="h-20 rounded-2xl bg-slate-100" />
                    <div className="h-20 rounded-2xl bg-slate-100" />
                    <div className="h-20 rounded-2xl bg-slate-100" />
                </div>
            </div>
            <div className="mt-6 grid gap-6 lg:grid-cols-3">
                <div className="h-80 rounded-3xl bg-white lg:col-span-2" />
                <div className="h-80 rounded-3xl bg-white" />
            </div>
        </div>
    )
}

export function JobDetailPage() {
    const { jobId: jobIdParam } = useParams<{ jobId: string }>()
    const jobId = Number(jobIdParam)
    const isValidJobId = Number.isInteger(jobId) && jobId > 0

    const jobQuery = useQuery({
        queryKey: ['jobs', 'detail', jobId],
        queryFn: () => getJobDetail(jobId),
        enabled: isValidJobId,
    })

    if (!isValidJobId) {
        return <JobNotFoundState />
    }

    if (jobQuery.isPending) {
        return <JobDetailSkeleton />
    }

    if (jobQuery.isError) {
        if (jobQuery.error instanceof JobApiError && jobQuery.error.status === 404) {
            return <JobNotFoundState />
        }

        return (
            <section className="mx-auto flex min-h-[60vh] w-full max-w-3xl items-center px-4 py-16 sm:px-6">
                <div className="w-full rounded-3xl border border-rose-200 bg-white p-8 text-center shadow-sm sm:p-12">
                    <span className="mx-auto grid size-14 place-items-center rounded-2xl bg-rose-50 text-rose-600">
                        <TriangleAlert aria-hidden="true" size={28} />
                    </span>
                    <h1 className="mt-5 text-2xl font-bold text-slate-900">Không thể tải tin tuyển dụng</h1>
                    <p className="mx-auto mt-3 max-w-xl text-sm leading-6 text-slate-600 sm:text-base">
                        {jobQuery.error instanceof Error
                            ? jobQuery.error.message
                            : 'Đã xảy ra lỗi không xác định.'}
                    </p>
                    <button
                        className="btn-bento-primary mt-7"
                        onClick={() => void jobQuery.refetch()}
                        type="button"
                    >
                        <RefreshCw aria-hidden="true" size={17} />
                        <span>Thử lại</span>
                    </button>
                </div>
            </section>
        )
    }

    const job = jobQuery.data

    return (
        <div className="bg-bento-canvas min-h-full py-8 sm:py-10">
            <div className="mx-auto w-full max-w-7xl px-4 sm:px-6 lg:px-8">
                <Link
                    className="inline-flex items-center gap-2 text-sm font-semibold text-slate-600 transition-colors hover:text-indigo-700"
                    to="/"
                >
                    <ArrowLeft aria-hidden="true" size={17} />
                    <span>Về trang chủ</span>
                </Link>

                <header className="relative mt-5 overflow-hidden rounded-3xl border border-indigo-100 bg-white p-6 shadow-sm sm:p-8 lg:p-10">
                    <div aria-hidden="true" className="absolute -top-24 -right-20 size-72 rounded-full bg-indigo-100/70 blur-3xl" />
                    <div className="relative flex flex-col gap-7 lg:flex-row lg:items-start lg:justify-between">
                        <div className="flex min-w-0 gap-4 sm:gap-5">
                            <span className="grid size-14 shrink-0 place-items-center rounded-2xl bg-gradient-to-br from-indigo-600 to-violet-600 text-white shadow-md sm:size-16">
                                <Building2 aria-hidden="true" size={28} />
                            </span>
                            <div className="min-w-0">
                                <span className="inline-flex rounded-full border border-emerald-200 bg-emerald-50 px-2.5 py-1 text-xs font-bold text-emerald-700">
                                    Đang tuyển
                                </span>
                                <h1 className="mt-3 text-2xl font-extrabold tracking-tight text-slate-950 sm:text-3xl lg:text-4xl">
                                    {job.title}
                                </h1>
                                <p className="mt-3 flex items-center gap-2 text-base font-semibold text-slate-600 sm:text-lg">
                                    <Building2 aria-hidden="true" size={18} />
                                    <span>{job.companyName || 'Doanh nghiệp chưa cập nhật'}</span>
                                </p>
                            </div>
                        </div>

                        <div className="rounded-2xl border border-indigo-100 bg-indigo-50/70 px-5 py-4 lg:min-w-64">
                            <p className="text-xs font-bold tracking-wide text-indigo-700 uppercase">Mức lương</p>
                            <p className="mt-2 text-xl font-extrabold text-indigo-950">
                                {formatSalaryRange(job.minSalary, job.maxSalary)}
                            </p>
                        </div>
                    </div>

                    <div className="relative mt-8 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
                        <div className="flex items-center gap-3 rounded-2xl border border-slate-200 bg-slate-50/80 p-4">
                            <MapPin className="shrink-0 text-rose-500" aria-hidden="true" size={20} />
                            <div className="min-w-0">
                                <p className="text-xs font-medium text-slate-500">Địa điểm</p>
                                <p className="mt-1 truncate text-sm font-bold text-slate-800">{job.location || 'Chưa cập nhật'}</p>
                            </div>
                        </div>
                        <div className="flex items-center gap-3 rounded-2xl border border-slate-200 bg-slate-50/80 p-4">
                            <BriefcaseBusiness className="shrink-0 text-indigo-500" aria-hidden="true" size={20} />
                            <div>
                                <p className="text-xs font-medium text-slate-500">Hình thức</p>
                                <p className="mt-1 text-sm font-bold text-slate-800">{formatJobType(job.jobType)}</p>
                            </div>
                        </div>
                        <div className="flex items-center gap-3 rounded-2xl border border-slate-200 bg-slate-50/80 p-4">
                            <UserRoundCheck className="shrink-0 text-violet-500" aria-hidden="true" size={20} />
                            <div>
                                <p className="text-xs font-medium text-slate-500">Kinh nghiệm</p>
                                <p className="mt-1 text-sm font-bold text-slate-800">{formatExperienceLevel(job.experienceLevel)}</p>
                            </div>
                        </div>
                        <div className="flex items-center gap-3 rounded-2xl border border-slate-200 bg-slate-50/80 p-4">
                            <CalendarDays className="shrink-0 text-amber-500" aria-hidden="true" size={20} />
                            <div>
                                <p className="text-xs font-medium text-slate-500">Hạn nộp hồ sơ</p>
                                <p className="mt-1 text-sm font-bold text-slate-800">{formatDate(job.deadline)}</p>
                            </div>
                        </div>
                    </div>
                </header>

                <div className="mt-6 grid items-start gap-6 lg:grid-cols-3">
                    <div className="space-y-6 lg:col-span-2">
                        <DetailSection content={job.description} title="Mô tả công việc" />
                        <DetailSection content={job.requirements} title="Yêu cầu ứng viên" />
                        <DetailSection content={job.benefits} title="Quyền lợi" />
                    </div>

                    <aside className="rounded-2xl border border-slate-200/80 bg-white p-6 shadow-2xs lg:sticky lg:top-24">
                        <h2 className="text-lg font-bold text-slate-900">Thông tin chung</h2>
                        <dl className="mt-5 space-y-5">
                            <div className="flex gap-3">
                                <CircleDollarSign className="mt-0.5 shrink-0 text-emerald-600" aria-hidden="true" size={20} />
                                <div>
                                    <dt className="text-xs font-medium text-slate-500">Mức lương</dt>
                                    <dd className="mt-1 text-sm font-bold text-slate-800">{formatSalaryRange(job.minSalary, job.maxSalary)}</dd>
                                </div>
                            </div>
                            <div className="flex gap-3">
                                <Clock3 className="mt-0.5 shrink-0 text-indigo-600" aria-hidden="true" size={20} />
                                <div>
                                    <dt className="text-xs font-medium text-slate-500">Ngày đăng</dt>
                                    <dd className="mt-1 text-sm font-bold text-slate-800">{formatDate(job.createdAt)}</dd>
                                </div>
                            </div>
                            <div className="flex gap-3">
                                <CalendarDays className="mt-0.5 shrink-0 text-amber-600" aria-hidden="true" size={20} />
                                <div>
                                    <dt className="text-xs font-medium text-slate-500">Hạn nộp</dt>
                                    <dd className="mt-1 text-sm font-bold text-slate-800">{formatDate(job.deadline)}</dd>
                                </div>
                            </div>
                        </dl>

                        <div className="mt-6 border-t border-slate-200 pt-5">
                            <p className="text-xs leading-5 text-slate-500">
                                Mã tin tuyển dụng: <span className="font-mono font-semibold text-slate-700">#{job.id}</span>
                            </p>
                        </div>
                    </aside>
                </div>
            </div>
        </div>
    )
}
