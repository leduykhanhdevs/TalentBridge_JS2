import {
    ArrowRight,
    BriefcaseBusiness,
    Building2,
    CheckCircle2,
    MapPin,
    RefreshCw,
    ShieldCheck,
    TriangleAlert,
} from 'lucide-react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router'
import { HeroTalentIllustration } from '../components/illustrations'
import { getStoredUser } from '../features/auth/tokenStorage'
import { searchJobs } from '../features/jobs/jobSearchApi'
import { formatExperienceLevel, formatJobType, formatSalary } from '../features/jobs/jobSearchFormatters'

export function HomePage() {
    const user = getStoredUser()
    const featuredJobsQuery = useQuery({
        queryKey: ['home-featured-jobs'],
        queryFn: () => searchJobs({ page: 1, size: 2, sort: 'NEWEST' }),
        staleTime: 60_000,
        retry: false,
    })
    const isCandidate = Boolean(user?.roles?.includes('ROLE_CANDIDATE'))
    const isRecruiter = Boolean(user?.roles?.includes('ROLE_RECRUITER'))
    const isAdmin = Boolean(user?.roles?.includes('ROLE_ADMIN'))

    const secondaryLink = isAdmin
        ? { to: '/admin/companies', label: 'Duyệt doanh nghiệp' }
        : isRecruiter
            ? { to: '/recruiter/jobs', label: 'Quản lý tin tuyển dụng' }
            : isCandidate
                ? { to: '/candidate/profile', label: 'Hoàn thiện hồ sơ' }
                : { to: '/register', label: 'Dành cho nhà tuyển dụng' }

    return (
        <div className="min-h-screen bg-bento-canvas text-slate-900">
            <section className="mx-auto grid w-full max-w-[1200px] items-center gap-8 px-4 py-12 sm:px-6 lg:min-h-[590px] lg:grid-cols-2 lg:gap-12 lg:px-8 lg:py-16">
                <div className="max-w-[590px]">
                    <p className="text-xs font-bold uppercase tracking-[0.14em] text-blue-700 sm:text-sm">
                        Kết nối đúng người, đúng cơ hội
                    </p>
                    <h1 className="mt-5 text-4xl font-bold leading-[1.2] tracking-tight text-slate-900 sm:text-5xl lg:text-[48px]">
                        Bước tiếp theo
                        <br className="hidden sm:block" />
                        {' '}cho sự nghiệp của bạn.
                    </h1>
                    <p className="mt-5 max-w-xl text-base leading-7 text-slate-600 sm:text-lg">
                        Khám phá việc làm với thông tin rõ ràng. Xây dựng hồ sơ để nhà tuyển dụng hiểu đúng năng lực của bạn.
                    </p>

                    <div className="mt-8 flex flex-col gap-3 sm:flex-row">
                        <Link className="btn-bento-primary min-h-11 text-sm" to="/jobs">
                            Khám phá việc làm
                            <ArrowRight aria-hidden="true" size={17} />
                        </Link>
                        <Link className="btn-bento-secondary min-h-11 text-sm" to={secondaryLink.to}>
                            {secondaryLink.label}
                        </Link>
                    </div>

                    <div className="mt-8 flex flex-wrap gap-x-6 gap-y-3 text-sm font-medium text-slate-600">
                        <span className="inline-flex items-center gap-2">
                            <CheckCircle2 aria-hidden="true" className="text-emerald-700" size={16} />
                            Thông tin tuyển dụng rõ ràng
                        </span>
                        <span className="inline-flex items-center gap-2">
                            <CheckCircle2 aria-hidden="true" className="text-emerald-700" size={16} />
                            Bạn kiểm soát hồ sơ
                        </span>
                    </div>
                </div>

                <div className="relative mx-auto flex w-full max-w-[520px] items-center justify-center py-5 lg:py-0">
                    <HeroTalentIllustration className="h-auto w-full" />
                </div>
            </section>

            <section className="border-y border-slate-200 bg-white/70">
                <div className="mx-auto w-full max-w-[1200px] px-4 py-14 sm:px-6 lg:px-8 lg:py-16">
                    <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
                        <div className="max-w-2xl">
                            <p className="text-xs font-bold uppercase tracking-[0.14em] text-blue-700">Cơ hội nổi bật</p>
                            <h2 className="mt-3 text-2xl font-bold tracking-tight text-slate-900 sm:text-3xl">Tìm một công việc đáng để bắt đầu</h2>
                            <p className="mt-3 text-base leading-7 text-slate-600">
                                Xem trước mức lương, địa điểm và yêu cầu trước khi mở tin tuyển dụng.
                            </p>
                        </div>
                        <Link className="inline-flex shrink-0 items-center gap-2 text-sm font-semibold text-blue-700 hover:text-blue-800" to="/jobs">
                            Xem tất cả việc làm <ArrowRight aria-hidden="true" size={16} />
                        </Link>
                    </div>

                    {featuredJobsQuery.isPending && (
                        <div aria-label="Đang tải việc làm" className="mt-8 grid gap-4 md:grid-cols-2" role="status">
                            {[0, 1].map((item) => (
                                <div className="bento-card h-44 animate-pulse p-5" key={item}>
                                    <div className="h-5 w-2/3 rounded bg-slate-100" />
                                    <div className="mt-4 h-4 w-1/3 rounded bg-slate-100" />
                                    <div className="mt-5 h-3 w-1/2 rounded bg-slate-100" />
                                </div>
                            ))}
                        </div>
                    )}

                    {featuredJobsQuery.isError && (
                        <div className="mt-8 flex flex-col gap-4 rounded-xl border border-rose-200 bg-rose-50 p-5 sm:flex-row sm:items-center sm:justify-between" role="alert">
                            <div className="flex items-start gap-3">
                                <TriangleAlert aria-hidden="true" className="mt-0.5 shrink-0 text-rose-700" size={19} />
                                <div>
                                    <h3 className="font-semibold text-rose-900">Chưa tải được danh sách việc làm</h3>
                                    <p className="mt-1 text-sm text-rose-800">{featuredJobsQuery.error.message}</p>
                                </div>
                            </div>
                            <button className="btn-bento-secondary shrink-0 text-sm" onClick={() => void featuredJobsQuery.refetch()} type="button">
                                <RefreshCw aria-hidden="true" size={15} /> Thử lại
                            </button>
                        </div>
                    )}

                    {featuredJobsQuery.isSuccess && featuredJobsQuery.data.totalElements === 0 && (
                        <div className="mt-8 rounded-xl border border-dashed border-slate-300 bg-white px-6 py-10 text-center">
                            <span className="mx-auto grid size-11 place-items-center rounded-lg bg-blue-50 text-blue-700">
                                <BriefcaseBusiness aria-hidden="true" size={21} />
                            </span>
                            <h3 className="mt-3 font-semibold text-slate-900">Chưa có tin tuyển dụng đang mở</h3>
                            <p className="mx-auto mt-1 max-w-lg text-sm leading-6 text-slate-600">Tin mới sẽ xuất hiện tại đây sau khi được đăng và mở nhận hồ sơ.</p>
                            <Link className="mt-4 inline-flex items-center gap-2 text-sm font-semibold text-blue-700 hover:text-blue-800" to="/jobs">
                                Mở trang tìm việc <ArrowRight aria-hidden="true" size={15} />
                            </Link>
                        </div>
                    )}

                    {featuredJobsQuery.isSuccess && featuredJobsQuery.data.content.length > 0 && (
                        <div className="mt-8 grid gap-4 md:grid-cols-2">
                            {featuredJobsQuery.data.content.map((job) => (
                                <article className="bento-card flex min-w-0 flex-col justify-between gap-5 p-5 sm:flex-row sm:p-6" key={job.id}>
                                    <div className="flex min-w-0 items-start gap-3">
                                        <span className="grid size-11 shrink-0 place-items-center rounded-lg bg-blue-50 text-blue-700">
                                            <Building2 aria-hidden="true" size={20} />
                                        </span>
                                        <div className="min-w-0">
                                            <h3 className="text-base font-semibold text-slate-900 sm:text-lg">
                                                <Link className="hover:text-blue-700" to={`/jobs/${job.id}`}>{job.title}</Link>
                                            </h3>
                                            <p className="mt-1 truncate text-sm font-medium text-slate-700">{job.companyName}</p>
                                            <div className="mt-3 flex flex-wrap gap-x-4 gap-y-2 text-xs font-medium text-slate-600">
                                                <span className="inline-flex items-center gap-1.5"><MapPin aria-hidden="true" size={14} />{job.city || job.location || 'Chưa cập nhật địa điểm'}</span>
                                                <span>{formatJobType(job.jobType)}</span>
                                                <span>{formatExperienceLevel(job.experienceLevel)}</span>
                                            </div>
                                            {job.skills && job.skills.length > 0 && (
                                                <div className="mt-3 flex flex-wrap gap-2">
                                                    {job.skills.slice(0, 3).map((skill) => (
                                                        <span className="rounded-md bg-slate-100 px-2 py-1 text-xs font-medium text-slate-700" key={skill}>{skill}</span>
                                                    ))}
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                    <div className="flex shrink-0 items-center justify-between gap-4 border-t border-slate-100 pt-4 sm:min-w-40 sm:flex-col sm:items-end sm:justify-between sm:border-0 sm:pt-0">
                                        <span className="text-sm font-bold text-blue-700">{formatSalary(job.minSalary, job.maxSalary, job.isNegotiable)}</span>
                                        <Link className="inline-flex items-center gap-1.5 text-sm font-semibold text-blue-700 hover:text-blue-800" to={`/jobs/${job.id}`}>
                                            Xem chi tiết <ArrowRight aria-hidden="true" size={15} />
                                        </Link>
                                    </div>
                                </article>
                            ))}
                        </div>
                    )}
                </div>
            </section>

            <section className="mx-auto w-full max-w-[1200px] px-4 py-12 sm:px-6 lg:px-8 lg:py-16">
                <div className="flex flex-col gap-6 rounded-xl border border-slate-200 bg-white p-6 shadow-sm sm:flex-row sm:items-center sm:justify-between sm:p-8">
                    <div className="flex items-start gap-4">
                        <span className="grid size-11 shrink-0 place-items-center rounded-lg bg-blue-50 text-blue-700">
                            {isAdmin ? <ShieldCheck aria-hidden="true" size={21} /> : <Building2 aria-hidden="true" size={21} />}
                        </span>
                        <div>
                            <h2 className="text-xl font-semibold text-slate-900">Quy trình tuyển dụng có kiểm soát</h2>
                            <p className="mt-2 max-w-2xl text-sm leading-6 text-slate-600">
                                Doanh nghiệp và tin tuyển dụng được quản lý theo trạng thái xét duyệt; nhà tuyển dụng có thể theo dõi hồ sơ trong cùng một quy trình.
                            </p>
                        </div>
                    </div>
                    <Link className="btn-bento-secondary shrink-0 text-sm" to={secondaryLink.to}>
                        {secondaryLink.label}
                    </Link>
                </div>
            </section>
        </div>
    )
}
