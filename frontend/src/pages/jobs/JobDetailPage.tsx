import { useQuery } from '@tanstack/react-query'
import {
    ArrowLeft,
    Building2,
    CheckCircle2,
    ExternalLink,
    RefreshCw,
    Send,
    TriangleAlert,
} from 'lucide-react'
import { useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router'
import { getStoredUser, isAuthenticated } from '../../features/auth/tokenStorage'
import { getMyApplications } from '../../features/candidate/candidateApi'
import { getJobDetail, JobApiError } from '../../features/jobs/jobApi'
import { ApplyJobModal } from '../../features/jobs/components/ApplyJobModal'
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
        <section>
            <h2 className="text-base font-bold text-slate-900 sm:text-lg">{title}</h2>
            <div className="mt-3 whitespace-pre-line text-sm leading-6 text-slate-600">
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
        <div aria-label="Đang tải chi tiết việc làm" className="mx-auto w-full max-w-[1200px] animate-pulse px-4 py-8 sm:px-6 lg:px-8">
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
    const navigate = useNavigate()
    const location = useLocation()
    const { jobId: jobIdParam } = useParams<{ jobId: string }>()
    const jobId = Number(jobIdParam)
    const isValidJobId = Number.isInteger(jobId) && jobId > 0
    const [isApplyModalOpen, setIsApplyModalOpen] = useState(false)

    const user = getStoredUser()
    const authenticated = isAuthenticated()
    const isAdmin = authenticated && user?.roles?.includes('ROLE_ADMIN')
    const isRecruiter = authenticated && user?.roles?.includes('ROLE_RECRUITER')
    const isCandidate =
        authenticated && (user?.roles?.includes('ROLE_CANDIDATE') || (!isAdmin && !isRecruiter))

    const jobQuery = useQuery({
        queryKey: ['jobs', 'detail', jobId],
        queryFn: () => getJobDetail(jobId),
        enabled: isValidJobId,
    })

    const myApplicationsQuery = useQuery({
        queryKey: ['candidate-applications'],
        queryFn: getMyApplications,
        enabled: authenticated && isCandidate,
    })

    const existingApplication = myApplicationsQuery.data?.find((app) => app.jobId === jobId)

    function renderApplyAction(extraClass = '') {
        if (job.isDemo) {
            return (
                <div
                    className={`rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-center text-sm font-semibold text-amber-950 ${extraClass}`}
                    role="note"
                >
                    Tin mô phỏng chỉ để xem giao diện, không nhận hồ sơ ứng tuyển.
                </div>
            )
        }

        if (!authenticated) {
            return (
                <button
                    className={`btn-bento-primary inline-flex items-center justify-center gap-2 ${extraClass}`}
                    onClick={() => navigate('/login', { state: { from: location } })}
                    type="button"
                >
                    <Send size={16} />
                    <span>Đăng nhập để ứng tuyển</span>
                </button>
            )
        }

        if (isAdmin || isRecruiter) {
            return (
                <div
                    className={`rounded-xl border border-slate-200 bg-slate-50 px-3.5 py-2.5 text-center text-xs font-medium text-slate-500 ${extraClass}`}
                >
                    Tài khoản {isAdmin ? 'Quản trị viên' : 'Nhà tuyển dụng'} không thể nộp đơn
                </div>
            )
        }

        if (existingApplication) {
            return (
                <div
                    className={`rounded-2xl border border-emerald-200 bg-emerald-50/90 p-4 text-left ${extraClass}`}
                >
                    <div className="flex items-center gap-2 font-bold text-emerald-800 text-sm">
                        <CheckCircle2 className="text-emerald-600 shrink-0" size={18} />
                        <span>Đã nộp hồ sơ ứng tuyển</span>
                    </div>
                    <p className="mt-1 text-xs text-emerald-700">
                        Ngày nộp: {formatDate(existingApplication.appliedAt)} &bull; Giai đoạn:{' '}
                        <span className="font-semibold">{existingApplication.currentStage}</span>
                    </p>
                    <Link
                        className="mt-2.5 inline-flex items-center gap-1.5 text-xs font-bold text-emerald-800 hover:text-emerald-950 underline"
                        to="/candidate/applications"
                    >
                        <span>Xem trạng thái đơn ứng tuyển</span>
                        <ExternalLink size={13} />
                    </Link>
                </div>
            )
        }

        return (
            <button
                className={`btn-bento-primary inline-flex items-center justify-center gap-2 shadow-md hover:shadow-blue-600/20 ${extraClass}`}
                onClick={() => setIsApplyModalOpen(true)}
                type="button"
            >
                <Send size={16} />
                <span>Ứng tuyển ngay</span>
            </button>
        )
    }

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

    const companyInitials = (job.companyName || 'TB')
        .split(/\s+/)
        .filter(Boolean)
        .slice(0, 2)
        .map((part) => part[0]?.toUpperCase())
        .join('')
    const isActiveJob = job.status === 'ACTIVE'

    return (
        <div className="min-h-full bg-[#F5F7FB] py-7 sm:py-9">
            <div className="mx-auto w-full max-w-[1200px] px-4 sm:px-6 lg:px-8">
                <nav aria-label="Đường dẫn" className="text-xs text-slate-500 sm:text-sm">
                    <Link className="transition-colors hover:text-[#1D4ED8]" to="/jobs">Việc làm</Link>
                    <span aria-hidden="true" className="px-2">/</span>
                    <span className="text-slate-700">{job.title}</span>
                </nav>

                <header className="mt-4 rounded-xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
                    {job.isDemo && (
                        <div className="mb-4 rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm font-semibold leading-6 text-amber-950" role="note">
                            DỮ LIỆU MÔ PHỎNG — Doanh nghiệp và vị trí này không có thật; thông tin chỉ dùng để xem thử TalentBridge.
                        </div>
                    )}
                    <div className="flex items-start gap-4">
                        <span aria-hidden="true" className="grid size-12 shrink-0 place-items-center rounded-lg bg-[#EFF4FF] text-sm font-bold text-[#1D4ED8]">
                            {companyInitials || <Building2 size={20} />}
                        </span>
                        <div className="min-w-0 flex-1">
                            <h1 className="text-xl font-bold tracking-tight text-[#172033] sm:text-2xl">
                                {job.title}
                            </h1>
                            <p className="mt-1 text-sm font-medium text-slate-600">
                                {job.companyName || 'Doanh nghiệp chưa cập nhật'}
                            </p>
                            <div className="mt-3 flex flex-wrap items-center gap-2">
                                <span className={`inline-flex rounded-md px-2 py-1 text-[11px] font-semibold ${job.isDemo ? 'bg-amber-50 text-amber-900' : isActiveJob ? 'bg-emerald-50 text-emerald-800' : 'bg-slate-100 text-slate-700'}`}>
                                    {job.isDemo ? 'Tin mô phỏng' : isActiveJob ? 'Đang nhận hồ sơ' : 'Tin tuyển dụng'}
                                </span>
                                {!job.isDemo && <span className="text-xs text-slate-500">Thông tin tuyển dụng do doanh nghiệp cung cấp.</span>}
                            </div>
                        </div>
                    </div>
                </header>

                <div className="mt-4 grid items-start gap-4 lg:grid-cols-[minmax(0,1.7fr)_minmax(280px,0.85fr)]">
                    <article className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
                        <DetailSection content={job.description} title="Mô tả công việc" />
                        <div className="my-4 border-t border-slate-200" />
                        <DetailSection content={job.requirements} title="Bạn sẽ phù hợp nếu" />
                        <div className="my-4 border-t border-slate-200" />
                        <DetailSection content={job.benefits} title="Quyền lợi & quy trình tuyển dụng" />
                    </article>

                    <aside className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm lg:sticky lg:top-24">
                        <p className="text-lg font-bold text-[#1D4ED8] sm:text-xl">
                            {formatSalaryRange(job.minSalary, job.maxSalary)}
                        </p>
                        <p className="mt-1 text-xs text-slate-500">Mức lương trong tin tuyển dụng</p>
                        <dl className="mt-4 space-y-1 border-t border-slate-200 pt-4 text-sm leading-6 text-slate-600">
                            <div className="flex gap-1">
                                <dt className="shrink-0">Địa điểm</dt>
                                <dd className="min-w-0">{job.location || 'Chưa cập nhật'}</dd>
                            </div>
                            <div className="flex gap-1">
                                <dt className="shrink-0">Hình thức</dt>
                                <dd>{formatJobType(job.jobType)}</dd>
                            </div>
                            <div className="flex gap-1">
                                <dt className="shrink-0">Kinh nghiệm</dt>
                                <dd>{formatExperienceLevel(job.experienceLevel)}</dd>
                            </div>
                            <div className="flex gap-1">
                                <dt className="shrink-0">Hạn ứng tuyển</dt>
                                <dd>{formatDate(job.deadline)}</dd>
                            </div>
                        </dl>
                        <div className="mt-4 space-y-2">
                            {renderApplyAction('w-full py-3 text-sm')}
                            <p className="text-center text-xs leading-5 text-slate-500">
                                Chọn CV và xác nhận trước khi gửi.
                            </p>
                        </div>
                    </aside>
                </div>

                {/* Apply Modal */}
                <ApplyJobModal
                    companyName={job.companyName || 'Doanh nghiệp'}
                    isOpen={isApplyModalOpen && !job.isDemo}
                    jobId={job.id}
                    jobTitle={job.title}
                    onApplySuccess={() => {
                        void myApplicationsQuery.refetch()
                    }}
                    onClose={() => setIsApplyModalOpen(false)}
                />
            </div>
        </div>
    )
}
