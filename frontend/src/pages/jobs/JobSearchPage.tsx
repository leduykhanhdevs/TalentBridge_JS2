import { useQuery } from '@tanstack/react-query'
import {
    ArrowUpDown,
    BriefcaseBusiness,
    Building2,
    ChevronLeft,
    ChevronRight,
    CircleDollarSign,
    ExternalLink,
    Filter,
    MapPin,
    RefreshCw,
    SlidersHorizontal,
    TriangleAlert,
} from 'lucide-react'
import { type FormEvent, useState } from 'react'
import { Link } from 'react-router'
import { searchJobs } from '../../features/jobs/jobSearchApi'
import {
    EMPTY_JOB_FILTERS,
    type JobFilterFormValues,
    toJobSearchParams,
    validateJobFilters,
} from '../../features/jobs/jobSearchFilters'
import {
    formatExperienceLevel,
    formatJobType,
    formatSalary,
} from '../../features/jobs/jobSearchFormatters'
import type { JobSearchParams } from '../../features/jobs/jobSearchTypes'
import { CustomSelect, type SelectOption } from '../../components/ui/CustomSelect'

const JOB_TYPE_OPTIONS: SelectOption[] = [
    { value: '', label: 'Tất cả hình thức' },
    { value: 'FULL_TIME', label: 'Toàn thời gian' },
    { value: 'PART_TIME', label: 'Bán thời gian' },
    { value: 'REMOTE', label: 'Làm việc từ xa' },
    { value: 'HYBRID', label: 'Làm việc kết hợp' },
]

const EXPERIENCE_LEVEL_OPTIONS: SelectOption[] = [
    { value: '', label: 'Tất cả kinh nghiệm' },
    { value: 'INTERN', label: 'Thực tập sinh' },
    { value: 'FRESHER', label: 'Fresher' },
    { value: 'JUNIOR', label: 'Junior' },
    { value: 'MIDDLE', label: 'Middle' },
    { value: 'SENIOR', label: 'Senior' },
]

const SORT_OPTIONS: SelectOption[] = [
    { value: 'NEWEST', label: 'Mới nhất' },
    { value: 'SALARY_DESC', label: 'Lương cao đến thấp' },
    { value: 'SALARY_ASC', label: 'Lương thấp đến cao' },
    { value: 'TITLE_ASC', label: 'Tên việc làm (A-Z)' },
]

const PAGE_SIZE_OPTIONS: SelectOption[] = [
    { value: '10', label: '10 việc làm / trang' },
    { value: '20', label: '20 việc làm / trang' },
    { value: '50', label: '50 việc làm / trang' },
]

const inputClassName = 'mt-1.5 w-full rounded-lg border border-slate-200 bg-white px-3 py-2.5 text-sm text-slate-900 outline-none transition focus:border-blue-600 focus:ring-3 focus:ring-blue-100 hover:border-slate-300'

export function JobSearchPage() {
    const [formValues, setFormValues] = useState<JobFilterFormValues>(EMPTY_JOB_FILTERS)
    const [sortBy, setSortBy] = useState<string>('NEWEST')
    const [appliedFilters, setAppliedFilters] = useState<JobSearchParams>({ page: 1, size: 10, sort: 'NEWEST' })
    const [validationError, setValidationError] = useState<string | null>(null)

    const jobsQuery = useQuery({
        queryKey: ['public-jobs', appliedFilters],
        queryFn: () => searchJobs(appliedFilters),
        staleTime: 60_000,
        gcTime: 5 * 60_000,
    })

    function updateField(field: keyof JobFilterFormValues, value: string) {
        setFormValues((current) => ({ ...current, [field]: value }))
        setValidationError(null)
    }

    function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()
        const error = validateJobFilters(formValues)
        if (error) {
            setValidationError(error)
            return
        }
        setAppliedFilters({
            ...toJobSearchParams(formValues),
            page: 1,
            size: appliedFilters.size ?? 10,
            sort: sortBy,
        })
    }

    function handleReset() {
        setFormValues(EMPTY_JOB_FILTERS)
        setValidationError(null)
        setSortBy('NEWEST')
        setAppliedFilters({ page: 1, size: 10, sort: 'NEWEST' })
    }

    function handleSortChange(newSort: string) {
        setSortBy(newSort)
        setAppliedFilters((current) => ({ ...current, page: 1, sort: newSort }))
    }

    function handlePageChange(newPage: number) {
        setAppliedFilters((current) => ({ ...current, page: newPage }))
        window.scrollTo({ top: 0, behavior: 'smooth' })
    }

    function handlePageSizeChange(newSize: string) {
        const size = parseInt(newSize, 10) || 10
        setAppliedFilters((current) => ({ ...current, page: 1, size }))
    }

    const jobs = jobsQuery.data?.content ?? []

    return (
        <main className="min-h-[calc(100vh-5.5rem)] bg-[#F5F7FB]">
            <section>
                <div className="mx-auto w-full max-w-[1760px] px-4 pb-1 pt-7 sm:px-6 lg:px-8 lg:pt-8">
                    <p className="text-[11px] font-bold uppercase tracking-[0.12em] text-[#1D4ED8]">Cơ hội nghề nghiệp</p>
                    <h1 className="mt-1 text-2xl font-bold tracking-tight text-[#172033] sm:text-[28px]">Tìm việc làm phù hợp</h1>
                    <p className="mt-1 max-w-2xl text-sm leading-6 text-slate-600">
                        Lọc cơ hội theo từ khóa, địa điểm, hình thức, kinh nghiệm và mức lương mong muốn.
                    </p>
                </div>
            </section>

            <div className="mx-auto grid min-w-0 w-full max-w-[1760px] grid-cols-1 gap-6 px-4 pb-8 pt-5 sm:px-6 lg:grid-cols-[300px_minmax(0,1fr)] lg:px-8">
                <aside className="h-fit min-w-0 rounded-xl border border-slate-200 bg-white p-5 shadow-sm lg:sticky lg:top-24">
                    <div className="flex items-center gap-2 text-slate-900">
                        <SlidersHorizontal aria-hidden="true" size={18} />
                        <h2 className="font-bold">Bộ lọc việc làm</h2>
                    </div>

                    <form className="mt-5 space-y-4" onSubmit={handleSubmit}>
                        <label className="block text-sm font-semibold text-slate-700">
                            Từ khóa
                            <input
                                className={inputClassName}
                                onChange={(event) => updateField('keyword', event.target.value)}
                                placeholder="Java, Spring Boot..."
                                type="search"
                                value={formValues.keyword}
                            />
                        </label>

                        <label className="block text-sm font-semibold text-slate-700">
                            Địa điểm
                            <input
                                className={inputClassName}
                                onChange={(event) => updateField('location', event.target.value)}
                                placeholder="Hà Nội, Hồ Chí Minh..."
                                type="search"
                                value={formValues.location}
                            />
                        </label>

                        <div>
                            <span className="block text-sm font-semibold text-slate-700">Hình thức làm việc</span>
                            <CustomSelect
                                options={JOB_TYPE_OPTIONS}
                                value={formValues.jobType}
                                onChange={(val) => updateField('jobType', val)}
                                placeholder="Tất cả hình thức"
                            />
                        </div>

                        <div>
                            <span className="block text-sm font-semibold text-slate-700">Kinh nghiệm</span>
                            <CustomSelect
                                options={EXPERIENCE_LEVEL_OPTIONS}
                                value={formValues.experienceLevel}
                                onChange={(val) => updateField('experienceLevel', val)}
                                placeholder="Tất cả kinh nghiệm"
                            />
                        </div>

                        <div>
                            <span className="text-sm font-semibold text-slate-700">Mức lương (triệu VNĐ)</span>
                            <div className="mt-1.5 grid grid-cols-2 gap-2">
                                <input
                                    aria-label="Lương tối thiểu"
                                    className={inputClassName.replace('mt-1.5 ', '')}
                                    min="0"
                                    onChange={(event) => updateField('minSalaryMillions', event.target.value)}
                                    placeholder="Từ"
                                    step="0.5"
                                    type="number"
                                    value={formValues.minSalaryMillions}
                                />
                                <input
                                    aria-label="Lương tối đa"
                                    className={inputClassName.replace('mt-1.5 ', '')}
                                    min="0"
                                    onChange={(event) => updateField('maxSalaryMillions', event.target.value)}
                                    placeholder="Đến"
                                    step="0.5"
                                    type="number"
                                    value={formValues.maxSalaryMillions}
                                />
                            </div>
                        </div>

                        {validationError && (
                            <p className="rounded-lg bg-rose-50 px-3 py-2 text-xs font-medium text-rose-700" role="alert">
                                {validationError}
                            </p>
                        )}

                        <button
                            className="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-blue-700 px-4 py-2.5 text-sm font-bold text-white transition hover:bg-blue-800 disabled:cursor-not-allowed disabled:opacity-60"
                            disabled={jobsQuery.isFetching}
                            type="submit"
                        >
                            <Filter aria-hidden="true" size={16} />
                            Áp dụng bộ lọc
                        </button>
                        <button
                            className="inline-flex w-full items-center justify-center gap-2 rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
                            onClick={handleReset}
                            type="button"
                        >
                            <RefreshCw aria-hidden="true" size={15} />
                            Đặt lại
                        </button>
                    </form>
                </aside>

                <section aria-live="polite" className="min-w-0">
                    {jobsQuery.isSuccess && jobs.some((job) => job.isDemo) && (
                        <p className="mb-4 rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm leading-6 text-amber-950" role="note">
                            Danh sách có tin tuyển dụng mô phỏng để xem thử giao diện. Tin được gắn nhãn “Mô phỏng” không đại diện vị trí thật và không nhận hồ sơ.
                        </p>
                    )}
                    <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                        <div>
                            <h2 className="text-xl font-bold text-slate-950">Danh sách việc làm</h2>
                            {jobsQuery.data && (
                                <p className="mt-1 text-sm text-slate-500">
                                    Tìm thấy <span className="font-semibold text-blue-700">{jobsQuery.data.totalElements}</span> việc làm
                                    {jobsQuery.data.totalPages > 1 && ` (Trang ${jobsQuery.data.pageNumber}/${jobsQuery.data.totalPages})`}
                                </p>
                            )}
                        </div>
                        <div className="flex items-center gap-2">
                            <ArrowUpDown className="text-slate-400" size={15} />
                            <span className="text-xs font-semibold text-slate-600">Sắp xếp:</span>
                            <div className="w-48">
                                <CustomSelect
                                    onChange={handleSortChange}
                                    options={SORT_OPTIONS}
                                    placeholder="Sắp xếp"
                                    value={sortBy}
                                />
                            </div>
                            {jobsQuery.isFetching && <span className="text-xs font-medium text-blue-700 animate-pulse ml-2">Đang tải...</span>}
                        </div>
                    </div>

                    {jobsQuery.isLoading && (
                        <div className="rounded-2xl border border-slate-200 bg-white p-10 text-center text-slate-600">
                            Đang tải danh sách việc làm...
                        </div>
                    )}

                    {jobsQuery.isError && (
                        <div className="rounded-2xl border border-rose-200 bg-rose-50 p-8 text-center">
                            <TriangleAlert className="mx-auto text-rose-600" aria-hidden="true" size={28} />
                            <p className="mt-3 font-semibold text-rose-900">Không thể tải danh sách việc làm</p>
                            <p className="mt-1 text-sm text-rose-700">{jobsQuery.error.message}</p>
                            <button
                                className="mt-4 rounded-xl bg-rose-600 px-4 py-2 text-sm font-bold text-white hover:bg-rose-700"
                                onClick={() => jobsQuery.refetch()}
                                type="button"
                            >
                                Thử lại
                            </button>
                        </div>
                    )}

                    {jobsQuery.isSuccess && jobs.length === 0 && (
                        <div className="rounded-2xl border border-dashed border-slate-300 bg-white p-10 text-center">
                            <BriefcaseBusiness className="mx-auto text-slate-400" aria-hidden="true" size={32} />
                            <p className="mt-3 font-semibold text-slate-900">Chưa tìm thấy việc làm phù hợp</p>
                            <p className="mt-1 text-sm text-slate-500">Hãy thử nới rộng hoặc đặt lại bộ lọc.</p>
                        </div>
                    )}

                    {jobsQuery.isSuccess && jobs.length > 0 && (
                        <div className="space-y-4">
                            {jobs.map((job) => (
                                <article className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm transition hover:border-slate-300 hover:shadow-md sm:p-6" key={job.id}>
                                    <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                                        <div>
                                            <Link className="group" to={`/jobs/${job.id}`}>
                                                <h3 className="text-lg font-bold text-slate-950 transition group-hover:text-blue-700">
                                                    {job.title}
                                                </h3>
                                            </Link>
                                            {job.isDemo && (
                                                <span className="mt-1 inline-flex rounded-full border border-amber-200 bg-amber-50 px-2.5 py-1 text-[11px] font-bold text-amber-900">
                                                    DEMO · Không nhận hồ sơ
                                                </span>
                                            )}
                                            <p className="mt-1 flex items-center gap-1.5 text-sm font-semibold text-blue-800">
                                                <Building2 aria-hidden="true" size={15} />
                                                {job.companyName}
                                            </p>
                                        </div>
                                        <div className="flex items-center gap-2">
                                            <span className={`w-fit rounded-full px-3 py-1 text-xs font-bold ${job.isDemo ? 'bg-amber-50 text-amber-900' : 'bg-emerald-50 text-emerald-700'}`}>
                                                {job.isDemo ? 'Tin mô phỏng' : 'Đang tuyển'}
                                            </span>
                                            <Link
                                                className="inline-flex items-center gap-1.5 rounded-xl bg-blue-50 px-3.5 py-1 text-xs font-bold text-blue-800 transition hover:bg-blue-700 hover:text-white"
                                                to={`/jobs/${job.id}`}
                                            >
                                                <span>{job.isDemo ? 'Xem mẫu' : 'Ứng tuyển'}</span>
                                                <ExternalLink size={13} />
                                            </Link>
                                        </div>
                                    </div>

                                    <div className="mt-4 flex flex-wrap gap-2 text-xs font-semibold text-slate-700">
                                        <span className="inline-flex items-center gap-1 rounded-lg bg-slate-100 px-2.5 py-1.5">
                                            <MapPin aria-hidden="true" size={14} />
                                            {[job.location, job.city].filter(Boolean).join(' · ') || 'Chưa cập nhật'}
                                        </span>
                                        <span className="inline-flex items-center gap-1 rounded-lg bg-slate-100 px-2.5 py-1.5">
                                            <BriefcaseBusiness aria-hidden="true" size={14} />
                                            {formatJobType(job.jobType)} · {formatExperienceLevel(job.experienceLevel)}
                                        </span>
                                        <span className="inline-flex items-center gap-1 rounded-lg bg-amber-50 px-2.5 py-1.5 text-amber-800">
                                            <CircleDollarSign aria-hidden="true" size={14} />
                                            {formatSalary(job.minSalary, job.maxSalary, job.isNegotiable)}
                                        </span>
                                    </div>

                                    <p className="mt-4 line-clamp-2 text-sm leading-6 text-slate-600">{job.description}</p>
                                    {job.skills && job.skills.length > 0 && (
                                        <div className="mt-4 flex flex-wrap gap-2">
                                            {job.skills.slice(0, 5).map((skill) => (
                                                <span className="rounded-md border border-blue-100 bg-blue-50 px-2 py-1 text-xs font-medium text-blue-800" key={skill}>{skill}</span>
                                            ))}
                                        </div>
                                    )}
                                </article>
                            ))}

                            {/* Pagination Controls */}
                            {jobsQuery.data && jobsQuery.data.totalPages > 1 && (
                                <div className="mt-8 flex flex-col items-center justify-between gap-4 rounded-2xl border border-slate-200 bg-white p-4 shadow-xs sm:flex-row">
                                    <div className="flex items-center gap-2 text-xs text-slate-600">
                                        <span>Hiển thị:</span>
                                        <div className="w-40">
                                            <CustomSelect
                                                onChange={handlePageSizeChange}
                                                options={PAGE_SIZE_OPTIONS}
                                                value={String(appliedFilters.size ?? 10)}
                                            />
                                        </div>
                                    </div>

                                    <div className="flex items-center gap-1.5">
                                        <button
                                            className="inline-flex items-center gap-1 rounded-xl border border-slate-200 bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40"
                                            disabled={(appliedFilters.page ?? 1) <= 1 || jobsQuery.isFetching}
                                            onClick={() => handlePageChange(Math.max(1, (appliedFilters.page ?? 1) - 1))}
                                            type="button"
                                        >
                                            <ChevronLeft size={14} />
                                            <span>Trước</span>
                                        </button>

                                        {Array.from({ length: jobsQuery.data.totalPages }, (_, i) => i + 1)
                                            .filter((p) => {
                                                const current = appliedFilters.page ?? 1
                                                return p === 1 || p === jobsQuery.data?.totalPages || Math.abs(p - current) <= 2
                                            })
                                            .map((pageNum, idx, arr) => {
                                                const prev = arr[idx - 1]
                                                const showEllipsis = prev && pageNum - prev > 1
                                                const isActive = pageNum === (appliedFilters.page ?? 1)

                                                return (
                                                    <div className="flex items-center" key={pageNum}>
                                                        {showEllipsis && <span className="px-1.5 text-xs text-slate-400">...</span>}
                                                        <button
                                                            className={`h-8 min-w-[32px] rounded-lg px-2 text-xs font-semibold transition ${
                                                                isActive
                                                                    ? 'bg-blue-700 text-white shadow-xs'
                                                                    : 'border border-slate-200 bg-white text-slate-700 hover:bg-slate-50'
                                                            }`}
                                                            disabled={jobsQuery.isFetching}
                                                            onClick={() => handlePageChange(pageNum)}
                                                            type="button"
                                                        >
                                                            {pageNum}
                                                        </button>
                                                    </div>
                                                )
                                            })}

                                        <button
                                            className="inline-flex items-center gap-1 rounded-xl border border-slate-200 bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40"
                                            disabled={(appliedFilters.page ?? 1) >= jobsQuery.data.totalPages || jobsQuery.isFetching}
                                            onClick={() => handlePageChange(Math.min(jobsQuery.data?.totalPages ?? 1, (appliedFilters.page ?? 1) + 1))}
                                            type="button"
                                        >
                                            <span>Sau</span>
                                            <ChevronRight size={14} />
                                        </button>
                                    </div>
                                </div>
                            )}
                        </div>
                    )}
                </section>
            </div>
        </main>
    )
}
