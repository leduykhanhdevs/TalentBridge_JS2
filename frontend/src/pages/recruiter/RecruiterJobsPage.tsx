import { useQuery } from '@tanstack/react-query'
import {
    AlertCircle,
    ArrowLeft,
    Briefcase,
    Building2,
    Calendar,
    ChevronRight,
    FileText,
    GraduationCap,
    LoaderCircle,
    Mail,
    MapPin,
    Phone,
    RefreshCw,
    User,
    Users,
} from 'lucide-react'
import { useState } from 'react'
import { Link, useSearchParams } from 'react-router'
import {
    getJobApplicants,
    getMyCompanyJobs,
    getRecruiterProfile,
} from '../../features/recruiter/recruiterApi'
import type { JobApplicant, RecruiterJob } from '../../features/recruiter/recruiterTypes'

export function RecruiterJobsPage() {
    const [searchParams, setSearchParams] = useSearchParams()
    const [selectedJob, setSelectedJob] = useState<RecruiterJob | null>(null)

    // 1. Fetch Recruiter Profile to know company status
    const {
        data: profile,
        isLoading: isProfileLoading,
        error: profileError,
        refetch: refetchProfile,
    } = useQuery({
        queryKey: ['recruiter-profile'],
        queryFn: getRecruiterProfile,
    })

    const hasCompany = Boolean(profile?.companyId && profile?.companyStatus === 'APPROVED')

    // 2. Fetch Company Jobs
    const {
        data: jobs = [],
        isLoading: isJobsLoading,
        error: jobsError,
        refetch: refetchJobs,
    } = useQuery({
        queryKey: ['my-company-jobs'],
        queryFn: getMyCompanyJobs,
        enabled: hasCompany,
    })

    // If query param ?jobId is present and jobs loaded, auto-select job if not selected
    const paramJobId = searchParams.get('jobId')
    const activeJob =
        selectedJob ||
        (paramJobId ? jobs.find((j) => String(j.id) === paramJobId) || null : null)

    // 3. Fetch Applicants for active job
    const {
        data: applicants = [],
        isLoading: isApplicantsLoading,
        error: applicantsError,
        refetch: refetchApplicants,
    } = useQuery({
        queryKey: ['job-applicants', activeJob?.id],
        queryFn: () => getJobApplicants(activeJob!.id),
        enabled: Boolean(activeJob?.id),
    })

    function handleSelectJob(job: RecruiterJob) {
        setSelectedJob(job)
        setSearchParams({ jobId: String(job.id) })
    }

    function handleBackToJobs() {
        setSelectedJob(null)
        setSearchParams({})
    }

    // State 1: Profile is loading -> show Loading UI
    if (isProfileLoading) {
        return (
            <div className="rounded-2xl border border-slate-200 bg-white p-12 text-center shadow-sm">
                <LoaderCircle className="animate-spin mx-auto text-emerald-600 mb-2" size={26} />
                <p className="text-sm font-semibold text-slate-700">Đang tải thông tin hồ sơ nhà tuyển dụng...</p>
                <p className="text-xs text-slate-400 mt-1">Vui lòng chờ trong giây lát</p>
            </div>
        )
    }

    // State 2: Profile error -> show Error UI with Retry button
    if (profileError) {
        return (
            <div className="rounded-2xl border border-red-200 bg-red-50/80 p-6 text-center shadow-sm">
                <div className="mx-auto mb-3 grid size-12 place-items-center rounded-full bg-red-100 text-red-600">
                    <AlertCircle size={24} />
                </div>
                <h2 className="text-base font-bold text-red-900">Không thể tải thông tin hồ sơ</h2>
                <p className="mt-1 text-xs sm:text-sm text-red-700">
                    {profileError instanceof Error
                        ? profileError.message
                        : 'Đã có lỗi xảy ra khi kết nối máy chủ.'}
                </p>
                <button
                    className="mt-4 inline-flex items-center gap-1.5 rounded-lg bg-red-600 px-4 py-2 text-xs font-bold text-white shadow hover:bg-red-700 transition"
                    onClick={() => refetchProfile()}
                    type="button"
                >
                    <RefreshCw size={13} />
                    <span>Thử lại</span>
                </button>
            </div>
        )
    }

    // State 3: Recruiter does not have approved company
    if (!hasCompany) {
        return (
            <div className="space-y-6">
                <div className="rounded-2xl border border-amber-200 bg-amber-50/70 p-6 text-center shadow-sm">
                    <div className="mx-auto mb-3 grid size-12 place-items-center rounded-full bg-amber-100 text-amber-600">
                        <Building2 size={24} />
                    </div>
                    <h2 className="text-lg font-bold text-slate-900">Chưa liên kết Doanh nghiệp</h2>
                    <p className="mt-1 text-sm text-slate-600">
                        Bạn cần thuộc về một doanh nghiệp đã được phê duyệt để quản lý tin tuyển dụng và xem ứng viên.
                    </p>
                    <div className="mt-4 flex justify-center gap-3">
                        <Link
                            className="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-semibold text-white shadow hover:bg-emerald-700"
                            to="/recruiter/company"
                        >
                            Đăng ký Doanh nghiệp
                        </Link>
                        <Link
                            className="rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50"
                            to="/recruiter/join-company"
                        >
                            Tìm & Xin gia nhập
                        </Link>
                    </div>
                </div>
            </div>
        )
    }

    // ==========================================
    // VIEW 1: APPLICANTS OF A SELECTED JOB (HRPM-49)
    // ==========================================
    if (activeJob) {
        return (
            <div className="space-y-6">
                {/* Header with Back button */}
                <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                    <div>
                        <button
                            className="group mb-2 inline-flex items-center gap-1.5 text-xs font-semibold text-emerald-700 hover:text-emerald-900"
                            onClick={handleBackToJobs}
                            type="button"
                        >
                            <ArrowLeft className="transition group-hover:-translate-x-0.5" size={15} />
                            <span>Quay lại danh sách tin tuyển dụng</span>
                        </button>
                        <div className="flex flex-wrap items-center gap-2">
                            <h1 className="text-xl sm:text-2xl font-black text-slate-900">
                                Ứng viên: {activeJob.title}
                            </h1>
                            <span className="rounded-full bg-emerald-100 px-2.5 py-0.5 text-xs font-bold text-emerald-800 border border-emerald-300">
                                Mã Job #{activeJob.id}
                            </span>
                        </div>
                        <p className="mt-1 flex flex-wrap items-center gap-4 text-xs sm:text-sm text-slate-600">
                            {activeJob.location && (
                                <span className="inline-flex items-center gap-1">
                                    <MapPin size={14} className="text-slate-400" />
                                    {activeJob.location}
                                </span>
                            )}
                            {activeJob.jobType && (
                                <span className="inline-flex items-center gap-1">
                                    <Briefcase size={14} className="text-slate-400" />
                                    {activeJob.jobType}
                                </span>
                            )}
                            {activeJob.deadline && (
                                <span className="inline-flex items-center gap-1">
                                    <Calendar size={14} className="text-slate-400" />
                                    Hạn nộp: {activeJob.deadline}
                                </span>
                            )}
                        </p>
                    </div>

                    <button
                        className="inline-flex items-center gap-1.5 self-start sm:self-auto rounded-lg border border-slate-200 bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 shadow-sm hover:bg-slate-50 transition"
                        onClick={() => refetchApplicants()}
                        title="Tải lại danh sách ứng viên"
                        type="button"
                    >
                        <RefreshCw size={13} />
                        <span>Làm mới</span>
                    </button>
                </div>

                {/* State 1: Loading */}
                {isApplicantsLoading && (
                    <div className="rounded-2xl border border-slate-200 bg-white p-12 text-center shadow-sm">
                        <div className="mx-auto mb-3 grid size-12 place-items-center rounded-full bg-emerald-50 text-emerald-600">
                            <LoaderCircle className="animate-spin" size={26} />
                        </div>
                        <p className="text-sm font-semibold text-slate-700">Đang tải danh sách ứng viên...</p>
                        <p className="text-xs text-slate-400 mt-1">Vui lòng chờ trong giây lát</p>
                    </div>
                )}

                {/* State 2: Error */}
                {!isApplicantsLoading && applicantsError && (
                    <div className="rounded-2xl border border-red-200 bg-red-50/80 p-6 text-center shadow-sm">
                        <div className="mx-auto mb-3 grid size-12 place-items-center rounded-full bg-red-100 text-red-600">
                            <AlertCircle size={24} />
                        </div>
                        <h2 className="text-base font-bold text-red-900">Không thể tải danh sách ứng viên</h2>
                        <p className="mt-1 text-xs sm:text-sm text-red-700">
                            {applicantsError instanceof Error
                                ? applicantsError.message
                                : 'Đã có lỗi xảy ra khi kết nối máy chủ.'}
                        </p>
                        <button
                            className="mt-4 inline-flex items-center gap-1.5 rounded-lg bg-red-600 px-4 py-2 text-xs font-bold text-white shadow hover:bg-red-700 transition"
                            onClick={() => refetchApplicants()}
                            type="button"
                        >
                            <RefreshCw size={13} />
                            <span>Thử lại</span>
                        </button>
                    </div>
                )}

                {/* State 3: Empty (Không có applicants) */}
                {!isApplicantsLoading && !applicantsError && applicants.length === 0 && (
                    <div className="rounded-2xl border border-dashed border-slate-300 bg-white p-12 text-center shadow-sm">
                        <div className="mx-auto mb-3 grid size-14 place-items-center rounded-full bg-slate-100 text-slate-400">
                            <Users size={28} />
                        </div>
                        <h2 className="text-base font-bold text-slate-800">
                            Chưa có ứng viên nào ứng tuyển vào vị trí này
                        </h2>
                        <p className="mt-1 text-xs sm:text-sm text-slate-500 max-w-md mx-auto">
                            Khi ứng viên nộp hồ sơ vào tin tuyển dụng này, danh sách hồ sơ và thông tin liên hệ sẽ xuất hiện tại đây.
                        </p>
                        <button
                            className="mt-4 inline-flex items-center gap-1.5 rounded-lg border border-slate-300 bg-white px-3.5 py-1.5 text-xs font-semibold text-slate-700 hover:bg-slate-50"
                            onClick={handleBackToJobs}
                            type="button"
                        >
                            <ArrowLeft size={13} />
                            <span>Xem tin tuyển dụng khác</span>
                        </button>
                    </div>
                )}

                {/* State 4: Has applicants (Có applicants) */}
                {!isApplicantsLoading && !applicantsError && applicants.length > 0 && (
                    <div className="space-y-4">
                        <div className="flex items-center justify-between px-1">
                            <span className="text-xs font-bold uppercase tracking-wider text-slate-500">
                                Tổng số: {applicants.length} ứng viên
                            </span>
                        </div>

                        <div className="grid gap-4">
                            {applicants.map((applicant) => (
                                <ApplicantCard applicant={applicant} key={applicant.id} />
                            ))}
                        </div>
                    </div>
                )}
            </div>
        )
    }

    // ==========================================
    // VIEW 2: LIST OF JOBS (Chọn Job)
    // ==========================================
    return (
        <div className="space-y-6">
            <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <h1 className="text-xl sm:text-2xl font-black text-slate-900">
                        Quản lý Tin tuyển dụng & Ứng viên
                    </h1>
                    <p className="mt-1 text-xs sm:text-sm text-slate-600">
                        Chọn một vị trí tuyển dụng để theo dõi và xem danh sách các ứng viên đã nộp đơn.
                    </p>
                </div>

                <button
                    className="inline-flex items-center gap-1.5 self-start sm:self-auto rounded-lg border border-slate-200 bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 shadow-sm hover:bg-slate-50 transition"
                    onClick={() => refetchJobs()}
                    title="Tải lại danh sách"
                    type="button"
                >
                    <RefreshCw size={13} />
                    <span>Làm mới</span>
                </button>
            </div>

            {/* Loading state for jobs */}
            {isJobsLoading && (
                <div className="rounded-2xl border border-slate-200 bg-white p-12 text-center shadow-sm">
                    <LoaderCircle className="animate-spin mx-auto text-emerald-600 mb-2" size={26} />
                    <p className="text-sm font-semibold text-slate-700">Đang tải danh sách tin tuyển dụng...</p>
                </div>
            )}

            {/* Error state for jobs */}
            {!isJobsLoading && jobsError && (
                <div className="rounded-2xl border border-red-200 bg-red-50/80 p-6 text-center shadow-sm">
                    <div className="mx-auto mb-3 grid size-12 place-items-center rounded-full bg-red-100 text-red-600">
                        <AlertCircle size={24} />
                    </div>
                    <h2 className="text-base font-bold text-red-900">Không thể tải danh sách công việc</h2>
                    <p className="mt-1 text-xs text-red-700">
                        {jobsError instanceof Error ? jobsError.message : 'Lỗi kết nối máy chủ.'}
                    </p>
                    <button
                        className="mt-4 rounded-lg bg-red-600 px-4 py-1.5 text-xs font-bold text-white shadow hover:bg-red-700"
                        onClick={() => refetchJobs()}
                        type="button"
                    >
                        Thử lại
                    </button>
                </div>
            )}

            {/* Empty state for jobs */}
            {!isJobsLoading && !jobsError && jobs.length === 0 && (
                <div className="rounded-2xl border border-dashed border-slate-300 bg-white p-12 text-center shadow-sm">
                    <div className="mx-auto mb-3 grid size-14 place-items-center rounded-full bg-slate-100 text-slate-400">
                        <Briefcase size={28} />
                    </div>
                    <h2 className="text-base font-bold text-slate-800">Doanh nghiệp chưa có tin tuyển dụng nào</h2>
                    <p className="mt-1 text-xs sm:text-sm text-slate-500 max-w-md mx-auto">
                        Hiện tại chưa có tin tuyển dụng nào được tạo cho doanh nghiệp của bạn.
                    </p>
                </div>
            )}

            {/* List of jobs */}
            {!isJobsLoading && !jobsError && jobs.length > 0 && (
                <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                    {jobs.map((job) => (
                        <div
                            className="flex flex-col justify-between rounded-xl border border-slate-200 bg-white p-5 shadow-sm transition hover:border-emerald-300 hover:shadow-md"
                            key={job.id}
                        >
                            <div>
                                <div className="flex items-start justify-between gap-2">
                                    <h2 className="font-bold text-slate-900 text-base line-clamp-2">
                                        {job.title}
                                    </h2>
                                    <span
                                        className={`shrink-0 rounded px-2 py-0.5 text-[11px] font-bold ${
                                            job.status === 'ACTIVE'
                                                ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                                                : job.status === 'CLOSED'
                                                ? 'bg-slate-100 text-slate-600 border border-slate-200'
                                                : 'bg-amber-50 text-amber-700 border border-amber-200'
                                        }`}
                                    >
                                        {job.status || 'ACTIVE'}
                                    </span>
                                </div>

                                <div className="mt-3 space-y-1.5 text-xs text-slate-600">
                                    {job.location && (
                                        <div className="flex items-center gap-1.5">
                                            <MapPin className="text-slate-400 shrink-0" size={13} />
                                            <span className="truncate">{job.location}</span>
                                        </div>
                                    )}
                                    {job.jobType && (
                                        <div className="flex items-center gap-1.5">
                                            <Briefcase className="text-slate-400 shrink-0" size={13} />
                                            <span>{job.jobType}</span>
                                        </div>
                                    )}
                                    {job.deadline && (
                                        <div className="flex items-center gap-1.5">
                                            <Calendar className="text-slate-400 shrink-0" size={13} />
                                            <span>Hạn nộp: {job.deadline}</span>
                                        </div>
                                    )}
                                </div>
                            </div>

                            <div className="mt-5 border-t border-slate-100 pt-3">
                                <button
                                    className="w-full inline-flex items-center justify-center gap-2 rounded-lg bg-emerald-600 px-3.5 py-2 text-xs font-bold text-white shadow-sm transition hover:bg-emerald-700 focus:outline-none focus:ring-2 focus:ring-emerald-500"
                                    onClick={() => handleSelectJob(job)}
                                    type="button"
                                >
                                    <Users size={14} />
                                    <span>Xem ứng viên</span>
                                    <ChevronRight size={14} />
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    )
}

function ApplicantCard({ applicant }: { applicant: JobApplicant }) {
    const formattedDate = applicant.appliedAt
        ? new Date(applicant.appliedAt).toLocaleDateString('vi-VN', {
              year: 'numeric',
              month: 'long',
              day: 'numeric',
          })
        : 'Gần đây'

    return (
        <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm transition hover:border-slate-300 hover:shadow-md">
            <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4">
                {/* Left: Avatar + Info */}
                <div className="flex items-start gap-4">
                    {applicant.avatar ? (
                        <img
                            alt={applicant.fullName}
                            className="size-12 rounded-full object-cover border border-slate-200"
                            src={applicant.avatar}
                        />
                    ) : (
                        <div className="grid size-12 place-items-center rounded-full bg-emerald-100 text-emerald-800 font-bold text-base shrink-0">
                            <User size={22} />
                        </div>
                    )}

                    <div>
                        <div className="flex flex-wrap items-center gap-2">
                            <h2 className="text-base font-bold text-slate-900">{applicant.fullName}</h2>
                            {applicant.currentStage && (
                                <span className="rounded-md bg-blue-50 px-2 py-0.5 text-[11px] font-semibold text-blue-700 border border-blue-200">
                                    {applicant.currentStage}
                                </span>
                            )}
                        </div>

                        {applicant.title && (
                            <p className="text-xs font-medium text-emerald-700 mt-0.5">
                                {applicant.title}
                            </p>
                        )}

                        <div className="mt-2 flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-slate-600">
                            <span className="inline-flex items-center gap-1">
                                <Mail className="text-slate-400" size={13} />
                                <a
                                    className="hover:underline hover:text-emerald-700"
                                    href={`mailto:${applicant.email}`}
                                >
                                    {applicant.email}
                                </a>
                            </span>

                            {applicant.phone && (
                                <span className="inline-flex items-center gap-1">
                                    <Phone className="text-slate-400" size={13} />
                                    <a
                                        className="hover:underline hover:text-emerald-700"
                                        href={`tel:${applicant.phone}`}
                                    >
                                        {applicant.phone}
                                    </a>
                                </span>
                            )}

                            {applicant.yearsOfExperience !== undefined && applicant.yearsOfExperience !== null && (
                                <span className="inline-flex items-center gap-1">
                                    <GraduationCap className="text-slate-400" size={13} />
                                    <span>{applicant.yearsOfExperience} năm KN</span>
                                </span>
                            )}

                            {applicant.city && (
                                <span className="inline-flex items-center gap-1">
                                    <MapPin className="text-slate-400" size={13} />
                                    <span>{applicant.city}</span>
                                </span>
                            )}
                        </div>
                    </div>
                </div>

                {/* Right: Date */}
                <div className="sm:text-right shrink-0 text-xs text-slate-500">
                    <span className="block font-medium">Nộp đơn ngày</span>
                    <span className="text-slate-700 font-semibold">{formattedDate}</span>
                </div>
            </div>

            {/* Cover letter section if present */}
            {applicant.coverLetter && (
                <div className="mt-4 rounded-lg bg-slate-50 p-3 text-xs text-slate-700 border border-slate-100">
                    <div className="flex items-center gap-1 font-semibold text-slate-800 mb-1">
                        <FileText size={13} className="text-emerald-600" />
                        <span>Thư giới thiệu (Cover Letter):</span>
                    </div>
                    <p className="whitespace-pre-line text-slate-600 leading-relaxed">
                        {applicant.coverLetter}
                    </p>
                </div>
            )}
        </div>
    )
}
