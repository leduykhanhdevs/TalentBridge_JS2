import { useEffect, useState, useMemo, useId, useCallback } from 'react'
import {
    AlertCircle,
    ArrowLeft,
    Briefcase,
    Calendar,
    CheckCircle2,
    FileText,
    History,
    Mail,
    MapPin,
    MessageSquare,
    Phone,
    RefreshCw,
    Search,
    Send,
    Sparkles,
    Star,
    Tag,
    UserCheck,
    X,
    CalendarCheck2,
} from 'lucide-react'
import { Link, useParams } from 'react-router'
import {
    addApplicantNote,
    getApplicantNotes,
    getApplicantStageHistory,
    getJobApplicants,
    updateApplicantStage,
} from '../../features/recruiter/recruiterApplicantApi'
import {
    APPLICANT_STAGE_CONFIG,
    type ApplicationNote,
    type ApplicationStage,
    type JobApplicant,
} from '../../features/recruiter/recruiterApplicantTypes'
import { ScheduleInterviewModal } from '../../features/recruiter/components/ScheduleInterviewModal'

export function RecruiterJobApplicantsPage() {
    const { jobId } = useParams<{ jobId: string }>()
    const numericJobId = Number(jobId)

    const [applicants, setApplicants] = useState<JobApplicant[]>([])
    const [isLoading, setIsLoading] = useState(true)
    const [errorMsg, setErrorMsg] = useState<string | null>(null)
    const [successMsg, setSuccessMsg] = useState<string | null>(null)

    // Filter and Sort states
    const [searchTerm, setSearchTerm] = useState('')
    const [stageFilter, setStageFilter] = useState('')
    const [minExpFilter, setMinExpFilter] = useState<number | ''>('')
    const [sortBy, setSortBy] = useState('appliedDate')
    const [sortDirection, setSortDirection] = useState<'ASC' | 'DESC'>('DESC')

    // Modals
    const [interviewModalApplicant, setInterviewModalApplicant] = useState<JobApplicant | null>(null)
    const [stageModalApplicant, setStageModalApplicant] = useState<JobApplicant | null>(null)
    const [targetStage, setTargetStage] = useState('REVIEWING')
    const [stageNote, setStageNote] = useState('')
    const [isUpdatingStage, setIsUpdatingStage] = useState(false)

    const [notesModalApplicant, setNotesModalApplicant] = useState<JobApplicant | null>(null)
    const [notesList, setNotesList] = useState<ApplicationNote[]>([])
    const [stagesList, setStagesList] = useState<ApplicationStage[]>([])
    const [activeTab, setActiveTab] = useState<'notes' | 'history'>('notes')
    const [isLoadingNotes, setIsLoadingNotes] = useState(false)

    // Add note form
    const [newRating, setNewRating] = useState<number>(5)
    const [newTag, setNewTag] = useState('')
    const [newComment, setNewComment] = useState('')
    const [isSubmittingNote, setIsSubmittingNote] = useState(false)
    const [noteError, setNoteError] = useState<string | null>(null)

    // Cover letter preview modal
    const [previewCoverLetter, setPreviewCoverLetter] = useState<{ name: string; content: string } | null>(null)

    const searchInputId = useId()
    const stageSelectId = useId()
    const expSelectId = useId()
    const sortSelectId = useId()

    const fetchApplicants = useCallback(async () => {
        if (!numericJobId || isNaN(numericJobId)) return
        setIsLoading(true)
        setErrorMsg(null)
        try {
            const data = await getJobApplicants(numericJobId, {
                keyword: searchTerm.trim() || undefined,
                stage: stageFilter || undefined,
                minExperience: minExpFilter !== '' ? Number(minExpFilter) : undefined,
                sortBy,
                sortDirection,
            })
            setApplicants(data)
        } catch (err: unknown) {
            const msg = err instanceof Error ? err.message : 'Không thể tải danh sách ứng viên'
            setErrorMsg(msg)
        } finally {
            setIsLoading(false)
        }
    }, [numericJobId, stageFilter, minExpFilter, sortBy, sortDirection, searchTerm])

    useEffect(() => {
        fetchApplicants()
    }, [fetchApplicants])

    function handleSearchSubmit(e: React.FormEvent) {
        e.preventDefault()
        fetchApplicants()
    }

    async function handleUpdateStageSubmit(e: React.FormEvent) {
        e.preventDefault()
        if (!stageModalApplicant) return

        setIsUpdatingStage(true)
        setErrorMsg(null)
        try {
            const updated = await updateApplicantStage(numericJobId, stageModalApplicant.id, {
                stage: targetStage,
                note: stageNote.trim() || undefined,
            })

            setApplicants((prev) =>
                prev.map((app) => (app.id === updated.id ? { ...app, ...updated } : app))
            )
            setSuccessMsg(`Đã chuyển ứng viên ${stageModalApplicant.candidateFullName} sang vòng ${APPLICANT_STAGE_CONFIG[targetStage]?.label || targetStage}`)
            setStageModalApplicant(null)
            setStageNote('')
        } catch (err: unknown) {
            const msg = err instanceof Error ? err.message : 'Cập nhật trạng thái thất bại'
            setErrorMsg(msg)
        } finally {
            setIsUpdatingStage(false)
        }
    }

    async function openNotesModal(applicant: JobApplicant) {
        setNotesModalApplicant(applicant)
        setIsLoadingNotes(true)
        setNoteError(null)
        try {
            const [notes, stages] = await Promise.all([
                getApplicantNotes(numericJobId, applicant.id),
                getApplicantStageHistory(numericJobId, applicant.id),
            ])
            setNotesList(notes)
            setStagesList(stages)
        } catch (err: unknown) {
            const msg = err instanceof Error ? err.message : 'Không thể tải dữ liệu ghi chú'
            setNoteError(msg)
        } finally {
            setIsLoadingNotes(false)
        }
    }

    async function handleAddNoteSubmit(e: React.FormEvent) {
        e.preventDefault()
        if (!notesModalApplicant) return
        if (!newComment.trim()) {
            setNoteError('Vui lòng nhập nội dung ghi chú')
            return
        }

        setIsSubmittingNote(true)
        setNoteError(null)
        try {
            const created = await addApplicantNote(numericJobId, notesModalApplicant.id, {
                rating: newRating,
                tag: newTag.trim() || undefined,
                comment: newComment.trim(),
            })

            setNotesList((prev) => [created, ...prev])
            setNewComment('')
            setNewTag('')
            setNewRating(5)

            // Update notesCount in local applicants list
            setApplicants((prev) =>
                prev.map((app) =>
                    app.id === notesModalApplicant.id
                        ? { ...app, notesCount: (app.notesCount || 0) + 1 }
                        : app
                )
            )
        } catch (err: unknown) {
            const msg = err instanceof Error ? err.message : 'Lưu ghi chú thất bại'
            setNoteError(msg)
        } finally {
            setIsSubmittingNote(false)
        }
    }

    // Quick stage statistics
    const stageCounts = useMemo(() => {
        const counts: Record<string, number> = {}
        for (const app of applicants) {
            counts[app.currentStage] = (counts[app.currentStage] || 0) + 1
        }
        return counts
    }, [applicants])

    const jobTitleDisplay = applicants[0]?.jobTitle || `Tin tuyển dụng #${jobId}`

    return (
        <div className="space-y-6">
            {/* Top Navigation & Header */}
            <div>
                <Link
                    to="/recruiter/jobs"
                    className="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-500 hover:text-indigo-600 transition mb-3"
                >
                    <ArrowLeft size={14} />
                    <span>Quay lại Quản lý tin đăng</span>
                </Link>

                <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                    <div>
                        <div className="flex items-center gap-2">
                            <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900">
                                Sàng lọc ứng viên
                            </h1>
                            <span className="bento-badge border-indigo-200 bg-indigo-50 text-indigo-700 text-xs">
                                {applicants.length} ỨNG VIÊN
                            </span>
                        </div>
                        <p className="mt-1 text-xs sm:text-sm text-slate-500">
                            {jobTitleDisplay} • Xem xét hồ sơ, lọc tiêu chí, sắp xếp điểm AI và ghi chú phỏng vấn
                        </p>
                    </div>

                    <button
                        type="button"
                        onClick={fetchApplicants}
                        disabled={isLoading}
                        className="btn-bento-secondary self-start sm:self-auto h-9 px-3.5 text-xs font-bold inline-flex items-center gap-2"
                        title="Làm mới danh sách"
                    >
                        <RefreshCw size={14} className={isLoading ? 'animate-spin' : ''} />
                        <span>Làm mới</span>
                    </button>
                </div>
            </div>

            {/* Notification Alerts */}
            {successMsg && (
                <div className="flex items-center justify-between rounded-xl border border-emerald-200 bg-emerald-50/90 p-3.5 text-xs font-semibold text-emerald-800">
                    <div className="flex items-center gap-2">
                        <CheckCircle2 size={16} className="text-emerald-600 shrink-0" />
                        <span>{successMsg}</span>
                    </div>
                    <button
                        type="button"
                        onClick={() => setSuccessMsg(null)}
                        className="text-emerald-700 hover:text-emerald-900"
                    >
                        <X size={15} />
                    </button>
                </div>
            )}

            {errorMsg && (
                <div className="flex items-center justify-between rounded-xl border border-rose-200 bg-rose-50/90 p-3.5 text-xs font-semibold text-rose-800">
                    <div className="flex items-center gap-2">
                        <AlertCircle size={16} className="text-rose-600 shrink-0" />
                        <span>{errorMsg}</span>
                    </div>
                    <button
                        type="button"
                        onClick={() => setErrorMsg(null)}
                        className="text-rose-700 hover:text-rose-900"
                    >
                        <X size={15} />
                    </button>
                </div>
            )}

            {/* Quick Stage Tabs / Filter Pills */}
            <div className="flex items-center gap-2 overflow-x-auto pb-1 scrollbar-none">
                <button
                    type="button"
                    onClick={() => setStageFilter('')}
                    className={`rounded-xl px-3.5 py-1.5 text-xs font-bold transition whitespace-nowrap ${
                        stageFilter === ''
                            ? 'bg-slate-900 text-white shadow-xs'
                            : 'bg-white text-slate-600 border border-slate-200 hover:bg-slate-100'
                    }`}
                >
                    Tất cả ({applicants.length})
                </button>
                {Object.entries(APPLICANT_STAGE_CONFIG).map(([key, config]) => {
                    const count = stageCounts[key] || 0
                    const isActive = stageFilter === key
                    return (
                        <button
                            key={key}
                            type="button"
                            onClick={() => setStageFilter(key)}
                            className={`rounded-xl px-3 py-1.5 text-xs font-bold transition whitespace-nowrap flex items-center gap-1.5 ${
                                isActive
                                    ? 'bg-indigo-600 text-white shadow-xs'
                                    : 'bg-white text-slate-600 border border-slate-200 hover:bg-slate-50'
                            }`}
                        >
                            <span>{config.label}</span>
                            <span
                                className={`text-[10px] px-1.5 py-0.2 rounded-full font-bold ${
                                    isActive ? 'bg-indigo-700 text-white' : 'bg-slate-100 text-slate-700'
                                }`}
                            >
                                {count}
                            </span>
                        </button>
                    )
                })}
            </div>

            {/* Search and Filters Bento Toolbar */}
            <div className="bento-card p-4">
                <form
                    onSubmit={handleSearchSubmit}
                    className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-12 gap-3"
                >
                    {/* Search Keyword */}
                    <div className="lg:col-span-4">
                        <label htmlFor={searchInputId} className="block text-[11px] font-bold uppercase tracking-wider text-slate-500 mb-1">
                            Tìm kiếm ứng viên
                        </label>
                        <div className="relative">
                            <Search
                                size={15}
                                className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
                            />
                            <input
                                id={searchInputId}
                                type="text"
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                                placeholder="Họ tên, email, số điện thoại..."
                                className="w-full rounded-xl border border-slate-200 bg-slate-50/50 pl-9 pr-3 py-2 text-xs text-slate-800 placeholder-slate-400 focus:border-indigo-500 focus:bg-white focus:outline-none transition"
                            />
                        </div>
                    </div>

                    {/* Stage Filter */}
                    <div className="lg:col-span-3">
                        <label htmlFor={stageSelectId} className="block text-[11px] font-bold uppercase tracking-wider text-slate-500 mb-1">
                            Vòng tuyển dụng
                        </label>
                        <select
                            id={stageSelectId}
                            value={stageFilter}
                            onChange={(e) => setStageFilter(e.target.value)}
                            className="w-full rounded-xl border border-slate-200 bg-slate-50/50 px-3 py-2 text-xs text-slate-800 focus:border-indigo-500 focus:bg-white focus:outline-none transition"
                        >
                            <option value="">Tất cả các vòng</option>
                            {Object.entries(APPLICANT_STAGE_CONFIG).map(([val, conf]) => (
                                <option key={val} value={val}>
                                    {conf.label} ({val})
                                </option>
                            ))}
                        </select>
                    </div>

                    {/* Min Experience */}
                    <div className="lg:col-span-2">
                        <label htmlFor={expSelectId} className="block text-[11px] font-bold uppercase tracking-wider text-slate-500 mb-1">
                            Kinh nghiệm tối thiểu
                        </label>
                        <select
                            id={expSelectId}
                            value={minExpFilter}
                            onChange={(e) =>
                                setMinExpFilter(e.target.value === '' ? '' : Number(e.target.value))
                            }
                            className="w-full rounded-xl border border-slate-200 bg-slate-50/50 px-3 py-2 text-xs text-slate-800 focus:border-indigo-500 focus:bg-white focus:outline-none transition"
                        >
                            <option value="">Tất cả kinh nghiệm</option>
                            <option value="1">1+ năm</option>
                            <option value="2">2+ năm</option>
                            <option value="3">3+ năm</option>
                            <option value="5">5+ năm</option>
                            <option value="8">8+ năm</option>
                        </select>
                    </div>

                    {/* Sort By */}
                    <div className="lg:col-span-3">
                        <label htmlFor={sortSelectId} className="block text-[11px] font-bold uppercase tracking-wider text-slate-500 mb-1">
                            Sắp xếp theo
                        </label>
                        <div className="flex gap-2">
                            <select
                                id={sortSelectId}
                                value={sortBy}
                                onChange={(e) => setSortBy(e.target.value)}
                                className="w-full rounded-xl border border-slate-200 bg-slate-50/50 px-3 py-2 text-xs text-slate-800 focus:border-indigo-500 focus:bg-white focus:outline-none transition"
                            >
                                <option value="appliedDate">Ngày nộp</option>
                                <option value="score">Điểm AI Match</option>
                                <option value="experience">Năm kinh nghiệm</option>
                                <option value="fullName">Họ tên ứng viên</option>
                            </select>
                            <button
                                type="button"
                                onClick={() =>
                                    setSortDirection((prev) => (prev === 'ASC' ? 'DESC' : 'ASC'))
                                }
                                className="rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-bold text-slate-700 hover:bg-slate-50 transition shrink-0"
                                title="Đổi thứ tự tăng/giảm"
                            >
                                {sortDirection === 'DESC' ? 'Giảm' : 'Tăng'}
                            </button>
                        </div>
                    </div>
                </form>
            </div>

            {/* Applicant List View */}
            {isLoading ? (
                <div className="bento-card flex flex-col items-center justify-center p-12 text-center">
                    <RefreshCw size={28} className="animate-spin text-indigo-600 mb-3" />
                    <p className="text-sm font-bold text-slate-800">Đang tải danh sách hồ sơ ứng viên...</p>
                    <p className="text-xs text-slate-500 mt-1">Hệ thống đang đồng bộ dữ liệu và tính toán điểm phù hợp</p>
                </div>
            ) : applicants.length === 0 ? (
                <div className="bento-card flex flex-col items-center justify-center p-12 text-center">
                    <div className="grid size-12 place-items-center rounded-2xl bg-indigo-50 text-indigo-600 mb-3">
                        <UserCheck size={24} />
                    </div>
                    <h3 className="text-base font-bold text-slate-900">Không tìm thấy ứng viên nào</h3>
                    <p className="text-xs text-slate-500 max-w-sm mt-1">
                        Chưa có ứng viên nộp đơn phù hợp với bộ lọc hiện tại. Thử xóa bộ lọc hoặc tìm kiếm với từ khóa khác.
                    </p>
                    {(searchTerm || stageFilter || minExpFilter !== '') && (
                        <button
                            type="button"
                            onClick={() => {
                                setSearchTerm('')
                                setStageFilter('')
                                setMinExpFilter('')
                            }}
                            className="mt-4 rounded-xl border border-slate-200 bg-white px-4 py-2 text-xs font-bold text-slate-700 hover:bg-slate-50 transition shadow-2xs"
                        >
                            Xóa toàn bộ bộ lọc
                        </button>
                    )}
                </div>
            ) : (
                <div className="space-y-3">
                    {applicants.map((app) => {
                        const stageInfo =
                            APPLICANT_STAGE_CONFIG[app.currentStage] || {
                                label: app.currentStage,
                                badgeClass: 'border-slate-200 bg-slate-100 text-slate-700',
                                desc: '',
                            }

                        const matchScore = app.aiMatchScore ? Number(app.aiMatchScore) : 0
                        const isHighMatch = matchScore >= 80
                        const isMedMatch = matchScore >= 60

                        return (
                            <div
                                key={app.id}
                                className="bento-card p-5 transition hover:border-indigo-200 hover:shadow-md"
                            >
                                <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">
                                    {/* Left: Candidate Info */}
                                    <div className="flex items-start gap-4">
                                        {/* Avatar */}
                                        <div className="size-12 rounded-2xl bg-gradient-to-tr from-indigo-500 to-indigo-700 text-white font-bold flex items-center justify-center text-sm shadow-sm shrink-0">
                                            {app.candidateFullName
                                                .trim()
                                                .split(/\s+/)
                                                .slice(-2)
                                                .map((w) => w[0])
                                                .join('')
                                                .toUpperCase() || 'CV'}
                                        </div>

                                        <div className="space-y-1.5">
                                            <div className="flex flex-wrap items-center gap-2">
                                                <h3 className="text-base font-bold text-slate-900">
                                                    {app.candidateFullName}
                                                </h3>
                                                <span
                                                    className={`bento-badge text-[11px] font-bold ${stageInfo.badgeClass}`}
                                                    title={stageInfo.desc}
                                                >
                                                    {stageInfo.label}
                                                </span>
                                                {app.status && app.status !== 'SUBMITTED' && (
                                                    <span className="text-[10px] uppercase font-semibold text-slate-600 bg-slate-100 px-1.5 py-0.5 rounded">
                                                        {app.status}
                                                    </span>
                                                )}
                                            </div>

                                            {/* Candidate Title & Exp */}
                                            <div className="flex flex-wrap items-center gap-x-3 gap-y-1 text-xs text-slate-600 font-medium">
                                                <span className="text-indigo-600 font-bold">
                                                    {app.candidateTitle || 'Chưa cập nhật chức danh'}
                                                </span>
                                                <span className="text-slate-300">•</span>
                                                <span className="flex items-center gap-1">
                                                    <Briefcase size={12} className="text-slate-400" />
                                                    {app.candidateExperienceYears || 0} năm kinh nghiệm
                                                </span>
                                                {app.candidateCity && (
                                                    <>
                                                        <span className="text-slate-300">•</span>
                                                        <span className="flex items-center gap-1">
                                                            <MapPin size={12} className="text-slate-400" />
                                                            {app.candidateCity}
                                                        </span>
                                                    </>
                                                )}
                                            </div>

                                            {/* Contacts */}
                                            <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-slate-500">
                                                <span className="flex items-center gap-1 min-w-0">
                                                    <Mail size={12} className="text-slate-400 shrink-0" />
                                                    <span className="break-all select-all">{app.candidateEmail}</span>
                                                </span>
                                                {app.candidatePhone && (
                                                    <span className="flex items-center gap-1 shrink-0">
                                                        <Phone size={12} className="text-slate-400 shrink-0" />
                                                        <span>{app.candidatePhone}</span>
                                                    </span>
                                                )}
                                                <span className="flex items-center gap-1 shrink-0">
                                                    <Calendar size={12} className="text-slate-400 shrink-0" />
                                                    Nộp ngày:{' '}
                                                    {new Date(app.appliedAt).toLocaleDateString('vi-VN')}
                                                </span>
                                            </div>

                                            {/* AI Match & Cover letter snippet */}
                                            <div className="flex flex-wrap items-center gap-3 pt-1">
                                                {matchScore > 0 && (
                                                    <span
                                                        className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-lg text-xs font-bold border ${
                                                            isHighMatch
                                                                ? 'border-emerald-200 bg-emerald-50 text-emerald-700'
                                                                : isMedMatch
                                                                ? 'border-blue-200 bg-blue-50 text-blue-700'
                                                                : 'border-amber-200 bg-amber-50 text-amber-700'
                                                        }`}
                                                    >
                                                        <Sparkles size={12} />
                                                        AI Match: {matchScore.toFixed(0)}%
                                                    </span>
                                                )}

                                                {app.averageRating && app.averageRating > 0 && (
                                                    <span className="inline-flex items-center gap-1 text-xs font-bold text-amber-600 bg-amber-50 px-2 py-0.5 rounded-lg border border-amber-200">
                                                        <Star size={12} className="fill-amber-400 text-amber-400" />
                                                        {app.averageRating.toFixed(1)}/5
                                                    </span>
                                                )}

                                                {app.coverLetter && (
                                                    <button
                                                        type="button"
                                                        onClick={() =>
                                                            setPreviewCoverLetter({
                                                                name: app.candidateFullName,
                                                                content: app.coverLetter || '',
                                                            })
                                                        }
                                                        className="text-xs text-indigo-600 hover:text-indigo-800 underline font-medium"
                                                    >
                                                        Xem thư giới thiệu
                                                    </button>
                                                )}
                                            </div>
                                        </div>
                                    </div>

                                    {/* Right: Actions */}
                                    <div className="flex flex-wrap items-center gap-2 pt-3 border-t border-slate-100 lg:border-t-0 lg:pt-0 shrink-0">
                                        {/* Resume Link */}
                                        {app.resumeUrl ? (
                                            <a
                                                href={app.resumeUrl}
                                                target="_blank"
                                                rel="noreferrer"
                                                className="inline-flex items-center gap-1.5 rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-bold text-slate-700 hover:bg-slate-50 transition shadow-2xs"
                                                title="Tải hoặc xem file CV"
                                            >
                                                <FileText size={13} />
                                                <span>Xem CV</span>
                                            </a>
                                        ) : (
                                            <span className="inline-flex items-center gap-1.5 rounded-xl border border-slate-100 bg-slate-50 px-3 py-2 text-xs font-semibold text-slate-400">
                                                <FileText size={13} />
                                                <span>Chưa nộp file CV</span>
                                            </span>
                                        )}

                                        {/* Schedule Interview Button */}
                                        <button
                                            type="button"
                                            onClick={() => setInterviewModalApplicant(app)}
                                            className="inline-flex items-center gap-1.5 rounded-xl border border-purple-200 bg-purple-50 px-3.5 py-2 text-xs font-bold text-purple-700 hover:bg-purple-100 transition shadow-2xs"
                                            title="Lên lịch phỏng vấn và đồng bộ Google Calendar"
                                        >
                                            <CalendarCheck2 size={13} />
                                            <span>Lên lịch PV</span>
                                        </button>

                                        {/* Change Stage Button */}
                                        <button
                                            type="button"
                                            onClick={() => {
                                                setStageModalApplicant(app)
                                                setTargetStage(app.currentStage)
                                                setStageNote('')
                                            }}
                                            className="inline-flex items-center gap-1.5 rounded-xl border border-indigo-200 bg-indigo-50 px-3.5 py-2 text-xs font-bold text-indigo-700 hover:bg-indigo-100 transition shadow-2xs"
                                            title="Chuyển vòng tuyển dụng"
                                        >
                                            <RefreshCw size={13} />
                                            <span>Chuyển vòng</span>
                                        </button>

                                        {/* Notes and Rating Button */}
                                        <button
                                            type="button"
                                            onClick={() => openNotesModal(app)}
                                            className="inline-flex items-center gap-1.5 rounded-xl border border-slate-200 bg-white px-3.5 py-2 text-xs font-bold text-slate-700 hover:bg-slate-50 transition shadow-2xs"
                                            title="Đánh giá và ghi chú phỏng vấn"
                                        >
                                            <MessageSquare size={13} />
                                            <span>Đánh giá</span>
                                            {app.notesCount > 0 && (
                                                <span className="ml-0.5 rounded-full bg-indigo-100 px-1.5 py-0.2 text-[10px] font-bold text-indigo-700">
                                                    {app.notesCount}
                                                </span>
                                            )}
                                        </button>
                                    </div>
                                </div>
                            </div>
                        )
                    })}
                </div>
            )}

            {/* Modal 1: Update Stage */}
            {stageModalApplicant && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4 backdrop-blur-xs">
                    <div className="bento-card w-full max-w-lg p-6 shadow-xl animate-in fade-in zoom-in-95 duration-150">
                        <div className="flex items-center justify-between border-b border-slate-100 pb-3 mb-4">
                            <div>
                                <h3 className="text-base font-bold text-slate-900">
                                    Chuyển vòng tuyển dụng
                                </h3>
                                <p className="text-xs text-slate-500">
                                    Ứng viên: <strong className="text-slate-800">{stageModalApplicant.candidateFullName}</strong>
                                </p>
                            </div>
                            <button
                                type="button"
                                onClick={() => setStageModalApplicant(null)}
                                className="text-slate-400 hover:text-slate-600 transition"
                            >
                                <X size={18} />
                            </button>
                        </div>

                        <form onSubmit={handleUpdateStageSubmit} className="space-y-4">
                            <div>
                                <label className="block text-xs font-bold text-slate-700 mb-1">
                                    Chọn vòng tuyển dụng mục tiêu *
                                </label>
                                <div className="grid grid-cols-2 gap-2">
                                    {Object.entries(APPLICANT_STAGE_CONFIG).map(([key, conf]) => {
                                        const isSelected = targetStage === key
                                        return (
                                            <button
                                                key={key}
                                                type="button"
                                                onClick={() => setTargetStage(key)}
                                                className={`text-left p-2.5 rounded-xl border transition ${
                                                    isSelected
                                                        ? 'border-indigo-600 bg-indigo-50/70 ring-2 ring-indigo-500/20'
                                                        : 'border-slate-200 bg-white hover:bg-slate-50'
                                                }`}
                                            >
                                                <div className="text-xs font-bold text-slate-900">
                                                    {conf.label}
                                                </div>
                                                <div className="text-[10px] text-slate-500 truncate">
                                                    {conf.desc}
                                                </div>
                                            </button>
                                        )
                                    })}
                                </div>
                            </div>

                            <div>
                                <label className="block text-xs font-bold text-slate-700 mb-1">
                                    Ghi chú lý do chuyển vòng (lưu vào lịch sử audit)
                                </label>
                                <textarea
                                    rows={3}
                                    value={stageNote}
                                    onChange={(e) => setStageNote(e.target.value)}
                                    placeholder="Ví dụ: Đạt yêu cầu CV kỹ thuật, mời phỏng vấn vòng 1 ngày 05/10..."
                                    className="w-full rounded-xl border border-slate-200 p-2.5 text-xs text-slate-800 placeholder-slate-400 focus:border-indigo-500 focus:outline-none"
                                />
                            </div>

                            <div className="flex justify-end gap-2 pt-2 border-t border-slate-100">
                                <button
                                    type="button"
                                    onClick={() => setStageModalApplicant(null)}
                                    disabled={isUpdatingStage}
                                    className="btn-bento-secondary h-9 px-4 text-xs font-bold"
                                >
                                    Hủy
                                </button>
                                <button
                                    type="submit"
                                    disabled={isUpdatingStage}
                                    className="btn-bento-primary h-9 px-4 text-xs font-bold inline-flex items-center gap-2"
                                >
                                    {isUpdatingStage ? (
                                        <>
                                            <RefreshCw size={13} className="animate-spin" />
                                            <span>Đang cập nhật...</span>
                                        </>
                                    ) : (
                                        <span>Xác nhận chuyển vòng</span>
                                    )}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Modal 2: Rate & Notes & History */}
            {notesModalApplicant && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4 backdrop-blur-xs">
                    <div className="bento-card w-full max-w-2xl max-h-[90vh] flex flex-col p-6 shadow-xl animate-in fade-in zoom-in-95 duration-150">
                        {/* Modal Header */}
                        <div className="flex items-center justify-between border-b border-slate-100 pb-3 mb-4 shrink-0">
                            <div>
                                <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
                                    <span>Đánh giá & Ghi chú phỏng vấn</span>
                                </h3>
                                <p className="text-xs text-slate-500">
                                    Ứng viên: <strong className="text-slate-800">{notesModalApplicant.candidateFullName}</strong>
                                </p>
                            </div>
                            <button
                                type="button"
                                onClick={() => setNotesModalApplicant(null)}
                                className="text-slate-400 hover:text-slate-600 transition"
                            >
                                <X size={18} />
                            </button>
                        </div>

                        {/* Tabs */}
                        <div className="flex gap-2 border-b border-slate-100 pb-3 shrink-0">
                            <button
                                type="button"
                                onClick={() => setActiveTab('notes')}
                                className={`inline-flex items-center gap-1.5 rounded-xl px-3.5 py-1.5 text-xs font-bold transition ${
                                    activeTab === 'notes'
                                        ? 'bg-indigo-600 text-white shadow-xs'
                                        : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                                }`}
                            >
                                <MessageSquare size={13} />
                                <span>Ghi chú nội bộ ({notesList.length})</span>
                            </button>
                            <button
                                type="button"
                                onClick={() => setActiveTab('history')}
                                className={`inline-flex items-center gap-1.5 rounded-xl px-3.5 py-1.5 text-xs font-bold transition ${
                                    activeTab === 'history'
                                        ? 'bg-indigo-600 text-white shadow-xs'
                                        : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                                }`}
                            >
                                <History size={13} />
                                <span>Lịch sử chuyển vòng ({stagesList.length})</span>
                            </button>
                        </div>

                        {/* Scrollable Body */}
                        <div className="flex-1 overflow-y-auto py-4 space-y-4 pr-1">
                            {noteError && (
                                <div className="rounded-xl border border-rose-200 bg-rose-50 p-3 text-xs text-rose-700 font-semibold flex items-center gap-2">
                                    <AlertCircle size={15} />
                                    <span>{noteError}</span>
                                </div>
                            )}

                            {activeTab === 'notes' ? (
                                <div className="space-y-4">
                                    {/* Add Note Form */}
                                    <form
                                        onSubmit={handleAddNoteSubmit}
                                        className="rounded-2xl border border-indigo-100 bg-indigo-50/40 p-4 space-y-3"
                                    >
                                        <h4 className="text-xs font-bold uppercase tracking-wider text-indigo-900 flex items-center gap-1.5">
                                            <Star size={13} className="text-amber-500 fill-amber-500" />
                                            <span>Thêm đánh giá & Ghi chú mới</span>
                                        </h4>

                                        {/* Rating Stars & Tag */}
                                        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
                                            <div className="flex items-center gap-2">
                                                <span className="text-xs font-semibold text-slate-700">
                                                    Đánh giá sao:
                                                </span>
                                                <div className="flex items-center gap-1">
                                                    {[1, 2, 3, 4, 5].map((star) => (
                                                        <button
                                                            key={star}
                                                            type="button"
                                                            onClick={() => setNewRating(star)}
                                                            className="p-1 hover:scale-110 transition"
                                                        >
                                                            <Star
                                                                size={18}
                                                                className={
                                                                    star <= newRating
                                                                        ? 'text-amber-400 fill-amber-400'
                                                                        : 'text-slate-300'
                                                                }
                                                            />
                                                        </button>
                                                    ))}
                                                    <span className="text-xs font-bold text-slate-700 ml-1">
                                                        {newRating}/5 sao
                                                    </span>
                                                </div>
                                            </div>

                                            <div className="flex items-center gap-1.5 sm:w-48">
                                                <Tag size={13} className="text-slate-400 shrink-0" />
                                                <input
                                                    type="text"
                                                    value={newTag}
                                                    onChange={(e) => setNewTag(e.target.value)}
                                                    placeholder="Nhãn tag (ví dụ: Senior, Tiềm năng)"
                                                    maxLength={50}
                                                    className="w-full rounded-lg border border-slate-200 bg-white px-2.5 py-1 text-xs text-slate-800 placeholder-slate-400 focus:border-indigo-500 focus:outline-none"
                                                />
                                            </div>
                                        </div>

                                        {/* Comment */}
                                        <div>
                                            <textarea
                                                rows={2}
                                                value={newComment}
                                                onChange={(e) => setNewComment(e.target.value)}
                                                placeholder="Ghi nhận xét buổi phỏng vấn, điểm mạnh, điểm yếu, deal lương... *"
                                                className="w-full rounded-xl border border-slate-200 bg-white p-2.5 text-xs text-slate-800 placeholder-slate-400 focus:border-indigo-500 focus:outline-none"
                                                required
                                            />
                                        </div>

                                        <div className="flex justify-end">
                                            <button
                                                type="submit"
                                                disabled={isSubmittingNote}
                                                className="btn-bento-primary h-8 px-4 text-xs font-bold inline-flex items-center gap-1.5"
                                            >
                                                <Send size={12} />
                                                <span>{isSubmittingNote ? 'Đang lưu...' : 'Lưu ghi chú'}</span>
                                            </button>
                                        </div>
                                    </form>

                                    {/* Existing Notes List */}
                                    {isLoadingNotes ? (
                                        <div className="p-8 text-center text-xs text-slate-500 font-semibold">
                                            Đang tải lịch sử ghi chú...
                                        </div>
                                    ) : notesList.length === 0 ? (
                                        <div className="p-8 text-center text-xs text-slate-500 border border-dashed border-slate-200 rounded-2xl">
                                            Chưa có ghi chú nội bộ nào cho ứng viên này.
                                        </div>
                                    ) : (
                                        <div className="space-y-2.5">
                                            {notesList.map((note) => (
                                                <div
                                                    key={note.id}
                                                    className="rounded-xl border border-slate-100 bg-white p-3.5 shadow-2xs space-y-1.5"
                                                >
                                                    <div className="flex items-center justify-between text-xs">
                                                        <div className="flex items-center gap-2">
                                                            <strong className="text-slate-800">
                                                                {note.recruiterName || 'HR Nhà tuyển dụng'}
                                                            </strong>
                                                            {note.tag && (
                                                                <span className="rounded bg-indigo-50 px-1.5 py-0.5 text-[10px] font-bold text-indigo-700 border border-indigo-100">
                                                                    #{note.tag}
                                                                </span>
                                                            )}
                                                        </div>
                                                        <div className="flex items-center gap-2 text-slate-400 text-[11px]">
                                                            {note.rating && (
                                                                <span className="flex items-center gap-0.5 text-amber-500 font-bold">
                                                                    <Star size={11} className="fill-amber-400" />
                                                                    {note.rating}/5
                                                                </span>
                                                            )}
                                                            <span>•</span>
                                                            <span>
                                                                {new Date(note.createdAt).toLocaleString('vi-VN')}
                                                            </span>
                                                        </div>
                                                    </div>
                                                    <p className="text-xs text-slate-700 leading-relaxed whitespace-pre-line">
                                                        {note.comment}
                                                    </p>
                                                </div>
                                            ))}
                                        </div>
                                    )}
                                </div>
                            ) : (
                                /* History Tab */
                                <div className="space-y-3">
                                    {isLoadingNotes ? (
                                        <div className="p-8 text-center text-xs text-slate-500 font-semibold">
                                            Đang tải lịch sử chuyển vòng...
                                        </div>
                                    ) : stagesList.length === 0 ? (
                                        <div className="p-8 text-center text-xs text-slate-500 border border-dashed border-slate-200 rounded-2xl">
                                            Chưa có lịch sử chuyển vòng nào được ghi nhận.
                                        </div>
                                    ) : (
                                        <div className="relative border-l-2 border-slate-200 ml-4 space-y-6 py-2">
                                            {stagesList.map((stg) => {
                                                const stageConf =
                                                    APPLICANT_STAGE_CONFIG[stg.stage] || {
                                                        label: stg.stage,
                                                        badgeClass: 'border-slate-200 bg-slate-100 text-slate-700',
                                                    }
                                                return (
                                                    <div key={stg.id} className="relative pl-6">
                                                        <div className="absolute -left-2 top-0.5 size-3.5 rounded-full border-2 border-white bg-indigo-600 shadow-xs" />
                                                        <div className="space-y-1">
                                                            <div className="flex items-center gap-2">
                                                                <span
                                                                    className={`bento-badge text-[11px] font-bold ${stageConf.badgeClass}`}
                                                                >
                                                                    {stageConf.label}
                                                                </span>
                                                                <span className="text-[11px] text-slate-400">
                                                                    {new Date(stg.changedAt).toLocaleString('vi-VN')}
                                                                </span>
                                                            </div>
                                                            <div className="text-xs text-slate-500">
                                                                Người cập nhật:{' '}
                                                                <strong className="text-slate-700">
                                                                    {stg.changedByUserName || `User #${stg.changedByUserId}`}
                                                                </strong>
                                                            </div>
                                                            {stg.note && (
                                                                <p className="rounded-lg bg-slate-50 p-2 text-xs text-slate-700 border border-slate-100">
                                                                    {stg.note}
                                                                </p>
                                                            )}
                                                        </div>
                                                    </div>
                                                )
                                            })}
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>

                        {/* Modal Footer */}
                        <div className="flex justify-end pt-3 border-t border-slate-100 shrink-0">
                            <button
                                type="button"
                                onClick={() => setNotesModalApplicant(null)}
                                className="btn-bento-secondary h-9 px-4 text-xs font-bold"
                            >
                                Đóng
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Modal 3: Cover letter preview */}
            {previewCoverLetter && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4 backdrop-blur-xs">
                    <div className="bento-card w-full max-w-lg p-6 shadow-xl animate-in fade-in zoom-in-95 duration-150 space-y-4">
                        <div className="flex items-center justify-between border-b border-slate-100 pb-3">
                            <h3 className="text-base font-bold text-slate-900">
                                Thư giới thiệu của {previewCoverLetter.name}
                            </h3>
                            <button
                                type="button"
                                onClick={() => setPreviewCoverLetter(null)}
                                className="text-slate-400 hover:text-slate-600 transition"
                            >
                                <X size={18} />
                            </button>
                        </div>
                        <p className="rounded-xl bg-slate-50 p-4 text-xs text-slate-700 leading-relaxed whitespace-pre-line border border-slate-200/80">
                            {previewCoverLetter.content}
                        </p>
                        <div className="flex justify-end pt-2">
                            <button
                                type="button"
                                onClick={() => setPreviewCoverLetter(null)}
                                className="btn-bento-secondary h-9 px-4 text-xs font-bold"
                            >
                                Đóng
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Modal 4: Schedule Interview with Google Calendar */}
            {interviewModalApplicant && (
                <ScheduleInterviewModal
                    isOpen={Boolean(interviewModalApplicant)}
                    onClose={() => setInterviewModalApplicant(null)}
                    jobId={numericJobId}
                    applicant={interviewModalApplicant}
                    onSuccess={() => {
                        fetchApplicants()
                        setSuccessMsg('Đã lên lịch phỏng vấn và tự động cập nhật vòng tuyển dụng!')
                    }}
                />
            )}
        </div>
    )
}
