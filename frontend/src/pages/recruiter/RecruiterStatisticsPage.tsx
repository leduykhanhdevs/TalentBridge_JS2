import { useEffect, useMemo, useState } from 'react'
import { AlertCircle, BarChart3, BriefcaseBusiness, CheckCircle2, LoaderCircle, ShieldCheck, Sparkles } from 'lucide-react'
import { useSearchParams } from 'react-router'
import { getMyJobs } from '../../features/recruiter/recruiterJobApi'
import type { RecruiterJobItem } from '../../features/recruiter/recruiterJobTypes'
import { getJobQualityReport, getRecruiterPipeline } from '../../features/statistics/statisticsApi'
import type { JobQualityReport, RecruiterPipelineAnalytics } from '../../features/statistics/statisticsTypes'

const STAGE_LABELS: Record<string, string> = {
    APPLIED: 'Mới nộp', REVIEWING: 'Đang xem xét', SCREENING: 'Đang xem xét',
    SHORTLISTED: 'Đạt sơ loại', INTERVIEW: 'Phỏng vấn', OFFERED: 'Đã gửi offer',
    HIRED: 'Đã tuyển dụng', REJECTED: 'Từ chối', WITHDRAWN: 'Ứng viên rút hồ sơ',
}

export function RecruiterStatisticsPage() {
    const [, setSearchParams] = useSearchParams()
    const [jobs, setJobs] = useState<RecruiterJobItem[]>([])
    const [selectedJobId, setSelectedJobId] = useState<number | null>(null)
    const [quality, setQuality] = useState<JobQualityReport | null>(null)
    const [pipeline, setPipeline] = useState<RecruiterPipelineAnalytics | null>(null)
    const [isLoadingJobs, setIsLoadingJobs] = useState(true)
    const [isLoadingReport, setIsLoadingReport] = useState(false)
    const [error, setError] = useState<string | null>(null)

    useEffect(() => {
        let active = true
        getMyJobs({ page: 1, size: 100 })
            .then((result) => {
                if (!active) return
                const ownJobs = result.content || []
                setJobs(ownJobs)
                const requestedJobId = Number(new URLSearchParams(window.location.search).get('jobId'))
                const initialJobId = ownJobs.some((job) => job.id === requestedJobId)
                    ? requestedJobId
                    : (ownJobs[0]?.id ?? null)
                setSelectedJobId(initialJobId)
                if (initialJobId !== null && initialJobId !== requestedJobId) {
                    setSearchParams({ jobId: String(initialJobId) }, { replace: true })
                }
            })
            .catch((caught: unknown) => {
                if (active) setError(caught instanceof Error ? caught.message : 'Không thể tải tin tuyển dụng của bạn.')
            })
            .finally(() => { if (active) setIsLoadingJobs(false) })
        return () => { active = false }
    }, [setSearchParams])

    useEffect(() => {
        if (selectedJobId === null) {
            setQuality(null)
            setPipeline(null)
            return
        }
        let active = true
        setIsLoadingReport(true)
        setError(null)
        Promise.all([getJobQualityReport(selectedJobId), getRecruiterPipeline(selectedJobId)])
            .then(([qualityResult, pipelineResult]) => {
                if (!active) return
                setQuality(qualityResult)
                setPipeline(pipelineResult)
            })
            .catch((caught: unknown) => {
                if (active) setError(caught instanceof Error ? caught.message : 'Không thể tải báo cáo thống kê.')
            })
            .finally(() => { if (active) setIsLoadingReport(false) })
        return () => { active = false }
    }, [selectedJobId])

    const selectedJob = useMemo(() => jobs.find((job) => job.id === selectedJobId) || null, [jobs, selectedJobId])

    return (
        <div className="space-y-6">
            <header className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
                <div>
                    <p className="text-xs font-bold uppercase tracking-wider text-emerald-700">Recruiter Analytics</p>
                    <h1 className="mt-1 text-2xl font-black tracking-tight text-slate-900">Thống kê tin và pipeline</h1>
                    <p className="mt-1 text-sm text-slate-600">Đo độ đầy đủ của tin và xem phân bố hồ sơ theo vòng hiện tại.</p>
                </div>
                <label className="w-full text-xs font-bold text-slate-600 sm:w-80">
                    Chọn tin tuyển dụng
                    <select className="mt-1.5 w-full rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-sm font-medium text-slate-800" disabled={isLoadingJobs || jobs.length === 0} onChange={(event) => { const jobId = Number(event.target.value); setSelectedJobId(jobId); setSearchParams({ jobId: String(jobId) }) }} value={selectedJobId ?? ''}>
                        {jobs.length === 0 && <option value="">Chưa có tin</option>}
                        {jobs.map((job) => <option key={job.id} value={job.id}>{job.title} · {job.status}</option>)}
                    </select>
                </label>
            </header>

            {error && <div className="flex items-start gap-2 rounded-2xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-800" role="alert"><AlertCircle aria-hidden="true" className="mt-0.5 shrink-0" size={17} />{error}</div>}
            {isLoadingJobs && <div className="bento-card grid min-h-52 place-items-center text-sm text-slate-500"><LoaderCircle aria-hidden="true" className="animate-spin" size={21} />Đang tải dữ liệu...</div>}
            {!isLoadingJobs && jobs.length === 0 && (
                <div className="bento-card p-10 text-center">
                    <BriefcaseBusiness aria-hidden="true" className="mx-auto text-slate-400" size={30} />
                    <h2 className="mt-3 font-bold text-slate-900">Chưa có tin để phân tích</h2>
                    <p className="mt-1 text-sm text-slate-600">Tạo tin trong mục Tin tuyển dụng. Bạn chỉ xem được dữ liệu thuộc công ty mình.</p>
                </div>
            )}
            {isLoadingReport && <div className="bento-card flex items-center justify-center gap-2 p-10 text-sm text-slate-500" role="status"><LoaderCircle aria-hidden="true" className="animate-spin" size={18} />Đang phân tích tin và tải pipeline...</div>}

            {!isLoadingReport && selectedJob && quality && pipeline && (
                <div className="grid items-start gap-5 xl:grid-cols-2">
                    <section className="bento-card p-5 sm:p-6" aria-labelledby="job-quality-title">
                        <div className="flex items-start justify-between gap-3">
                            <div>
                                <p className="text-xs font-bold uppercase tracking-wider text-indigo-700">Job Quality</p>
                                <h2 className="mt-1 text-lg font-bold text-slate-900" id="job-quality-title">Độ rõ ràng và minh bạch của tin</h2>
                                <p className="mt-1 line-clamp-2 text-sm text-slate-600">{selectedJob.title}</p>
                            </div>
                            <div className="shrink-0 rounded-2xl bg-indigo-50 px-3 py-2 text-center text-indigo-800">
                                <p className="text-2xl font-black">{quality.qualityScore}<span className="text-sm">/100</span></p>
                                <p className="text-[11px] font-bold">{quality.qualityLabel}</p>
                            </div>
                        </div>
                        <div className="mt-5 space-y-3">
                            {quality.criteria.map((criterion) => (
                                <div key={criterion.name}>
                                    <div className="flex items-baseline justify-between gap-3 text-xs">
                                        <span className="font-semibold text-slate-700">{criterion.name}</span>
                                        <span className="shrink-0 font-bold text-slate-600">{criterion.points}/{criterion.maximumPoints}</span>
                                    </div>
                                    <div aria-label={`${criterion.name}: ${criterion.points} trên ${criterion.maximumPoints}`} className="mt-1 h-1.5 overflow-hidden rounded-full bg-slate-100" role="img">
                                        <div className="h-full rounded-full bg-indigo-500" style={{ width: `${criterion.maximumPoints ? (criterion.points / criterion.maximumPoints) * 100 : 0}%` }} />
                                    </div>
                                    <p className="mt-1 text-[11px] leading-5 text-slate-500">{criterion.evidence}</p>
                                </div>
                            ))}
                        </div>
                        {quality.trustSignals.length > 0 && <div className="mt-5 rounded-xl border border-emerald-100 bg-emerald-50 p-3"><h3 className="flex items-center gap-1.5 text-xs font-bold text-emerald-800"><ShieldCheck aria-hidden="true" size={14} />Tín hiệu kiểm chứng trong TalentBridge</h3><ul className="mt-2 space-y-1 text-xs text-emerald-900">{quality.trustSignals.map((signal) => <li key={signal}>• {signal}</li>)}</ul></div>}
                        {quality.improvementSuggestions.length > 0 && <div className="mt-4 rounded-xl border border-amber-100 bg-amber-50 p-3"><h3 className="flex items-center gap-1.5 text-xs font-bold text-amber-900"><Sparkles aria-hidden="true" size={14} />Gợi ý cải thiện</h3><ul className="mt-2 space-y-1 text-xs text-amber-950">{quality.improvementSuggestions.map((suggestion) => <li key={suggestion}>• {suggestion}</li>)}</ul></div>}
                        <p className="mt-4 text-[11px] leading-5 text-slate-500">{quality.limitation}</p>
                    </section>

                    <section className="bento-card p-5 sm:p-6" aria-labelledby="pipeline-title">
                        <p className="text-xs font-bold uppercase tracking-wider text-emerald-700">ATS Pipeline Snapshot</p>
                        <div className="mt-1 flex items-end justify-between gap-3">
                            <div>
                                <h2 className="text-lg font-bold text-slate-900" id="pipeline-title">Phân bố hồ sơ hiện tại</h2>
                                <p className="mt-1 line-clamp-2 text-sm text-slate-600">{pipeline.jobTitle}</p>
                            </div>
                            <p className="shrink-0 text-2xl font-black text-emerald-800">{pipeline.totalApplications}<span className="ml-1 text-xs font-semibold">hồ sơ</span></p>
                        </div>
                        <div className="mt-5 space-y-4">
                            {pipeline.stages.map((stage) => (
                                <div key={stage.stage}>
                                    <div className="mb-1 flex justify-between gap-3 text-xs">
                                        <span className="font-semibold text-slate-700">{STAGE_LABELS[stage.stage] || stage.stage}</span>
                                        <span className="font-bold tabular-nums text-slate-600">{stage.applicantCount} · {stage.sharePercentage}%</span>
                                    </div>
                                    <div aria-label={`${STAGE_LABELS[stage.stage] || stage.stage}: ${stage.sharePercentage}%`} className="h-2 overflow-hidden rounded-full bg-slate-100" role="img">
                                        <div className="h-full rounded-full bg-emerald-500 transition-all" style={{ width: `${stage.sharePercentage}%` }} />
                                    </div>
                                </div>
                            ))}
                        </div>
                        {pipeline.totalApplications === 0 && <div className="mt-4 flex items-center gap-2 rounded-xl border border-slate-200 bg-slate-50 p-3 text-xs text-slate-600"><CheckCircle2 aria-hidden="true" size={15} />Chưa có đơn ứng tuyển ở tin này.</div>}
                        <p className="mt-5 flex gap-2 border-t border-slate-100 pt-4 text-xs leading-5 text-slate-500"><BarChart3 aria-hidden="true" className="mt-0.5 shrink-0" size={15} />{pipeline.interpretation}</p>
                    </section>
                </div>
            )}
        </div>
    )
}
