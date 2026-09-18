import { useState } from 'react'
import { Briefcase, X } from 'lucide-react'
import { addWorkExperience, updateWorkExperience, CandidateApiError } from '../candidateApi'
import type { WorkExperience, WorkExperienceRequest } from '../candidateTypes'

interface WorkExperienceModalProps {
    isOpen: boolean
    onClose: () => void
    onSuccess: () => void
    initialData?: WorkExperience | null
}

interface WorkExperienceFormProps {
    initialData?: WorkExperience | null
    onClose: () => void
    onSuccess: () => void
}

function WorkExperienceForm({ initialData, onClose, onSuccess }: WorkExperienceFormProps) {
    const [companyName, setCompanyName] = useState(initialData?.companyName || '')
    const [position, setPosition] = useState(initialData?.position || '')
    const [startDate, setStartDate] = useState(initialData?.startDate || '')
    const [endDate, setEndDate] = useState(initialData?.endDate || '')
    const [isCurrent, setIsCurrent] = useState(Boolean(initialData?.isCurrent))
    const [description, setDescription] = useState(initialData?.description || '')
    const [achievements, setAchievements] = useState(initialData?.achievements || '')

    const [loading, setLoading] = useState(false)
    const [errorMsg, setErrorMsg] = useState<string | null>(null)

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault()
        setErrorMsg(null)

        if (!companyName.trim()) {
            setErrorMsg('Vui lòng nhập tên công ty.')
            return
        }
        if (!position.trim()) {
            setErrorMsg('Vui lòng nhập vị trí làm việc.')
            return
        }
        if (!startDate) {
            setErrorMsg('Vui lòng chọn ngày bắt đầu.')
            return
        }
        if (!isCurrent && !endDate) {
            setErrorMsg('Vui lòng chọn ngày kết thúc hoặc đánh dấu là đang làm việc tại đây.')
            return
        }
        if (!isCurrent && endDate && endDate < startDate) {
            setErrorMsg('Ngày kết thúc không được trước ngày bắt đầu.')
            return
        }

        const payload: WorkExperienceRequest = {
            companyName: companyName.trim(),
            position: position.trim(),
            startDate,
            endDate: isCurrent ? null : endDate || null,
            isCurrent,
            description: description.trim() || undefined,
            achievements: achievements.trim() || undefined,
        }

        setLoading(true)
        try {
            if (initialData?.id) {
                await updateWorkExperience(initialData.id, payload)
            } else {
                await addWorkExperience(payload)
            }
            onSuccess()
            onClose()
        } catch (err) {
            if (err instanceof CandidateApiError) {
                setErrorMsg(err.message)
            } else {
                setErrorMsg('Đã có lỗi xảy ra khi lưu kinh nghiệm làm việc.')
            }
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="relative w-full max-w-lg rounded-2xl bg-white p-6 shadow-2xl transition-all max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between border-b border-slate-100 pb-4">
                <div className="flex items-center gap-2 text-slate-800">
                    <div className="grid size-9 place-items-center rounded-xl bg-blue-50 text-blue-600">
                        <Briefcase size={20} />
                    </div>
                    <h2 className="text-lg font-bold">
                        {initialData ? 'Chỉnh sửa kinh nghiệm làm việc' : 'Thêm kinh nghiệm làm việc'}
                    </h2>
                </div>
                <button
                    className="rounded-lg p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
                    disabled={loading}
                    onClick={onClose}
                    type="button"
                >
                    <X size={20} />
                </button>
            </div>

            {errorMsg && (
                <div className="mt-4 rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-600">
                    {errorMsg}
                </div>
            )}

            <form className="mt-4 space-y-4" onSubmit={handleSubmit}>
                <div>
                    <label className="block text-xs font-semibold text-slate-700 mb-1" htmlFor="company-name">
                        Tên doanh nghiệp / Công ty <span className="text-rose-500">*</span>
                    </label>
                    <input
                        className="w-full rounded-xl border border-slate-200 px-3.5 py-2 text-sm text-slate-800 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-100"
                        disabled={loading}
                        id="company-name"
                        onChange={(e) => setCompanyName(e.target.value)}
                        placeholder="VD: FPT Software, VNG Corporation..."
                        required
                        type="text"
                        value={companyName}
                    />
                </div>

                <div>
                    <label className="block text-xs font-semibold text-slate-700 mb-1" htmlFor="position">
                        Vị trí / Chức danh công việc <span className="text-rose-500">*</span>
                    </label>
                    <input
                        className="w-full rounded-xl border border-slate-200 px-3.5 py-2 text-sm text-slate-800 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-100"
                        disabled={loading}
                        id="position"
                        onChange={(e) => setPosition(e.target.value)}
                        placeholder="VD: Java Backend Developer, Fullstack Engineer..."
                        required
                        type="text"
                        value={position}
                    />
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div>
                        <label className="block text-xs font-semibold text-slate-700 mb-1" htmlFor="start-date">
                            Ngày bắt đầu <span className="text-rose-500">*</span>
                        </label>
                        <div className="relative">
                            <input
                                className="w-full rounded-xl border border-slate-200 px-3.5 py-2 text-sm text-slate-800 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-100"
                                disabled={loading}
                                id="start-date"
                                onChange={(e) => setStartDate(e.target.value)}
                                required
                                type="date"
                                value={startDate}
                            />
                        </div>
                    </div>

                    <div>
                        <label className="block text-xs font-semibold text-slate-700 mb-1" htmlFor="end-date">
                            Ngày kết thúc
                        </label>
                        <input
                            className="w-full rounded-xl border border-slate-200 px-3.5 py-2 text-sm text-slate-800 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-100 disabled:bg-slate-100 disabled:text-slate-400"
                            disabled={loading || isCurrent}
                            id="end-date"
                            onChange={(e) => setEndDate(e.target.value)}
                            type="date"
                            value={isCurrent ? '' : endDate}
                        />
                    </div>
                </div>

                <div className="flex items-center gap-2">
                    <input
                        checked={isCurrent}
                        className="size-4 rounded border-slate-300 text-blue-600 focus:ring-blue-500"
                        disabled={loading}
                        id="is-current"
                        onChange={(e) => setIsCurrent(e.target.checked)}
                        type="checkbox"
                    />
                    <label className="text-xs font-medium text-slate-700 select-none cursor-pointer" htmlFor="is-current">
                        Hiện tại tôi đang làm việc ở vị trí này
                    </label>
                </div>

                <div>
                    <label className="block text-xs font-semibold text-slate-700 mb-1" htmlFor="description">
                        Mô tả công việc & Trách nhiệm chính
                    </label>
                    <textarea
                        className="w-full rounded-xl border border-slate-200 px-3.5 py-2 text-sm text-slate-800 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-100"
                        disabled={loading}
                        id="description"
                        onChange={(e) => setDescription(e.target.value)}
                        placeholder="Mô tả các nhiệm vụ và công việc bạn phụ trách..."
                        rows={3}
                        value={description}
                    />
                </div>

                <div>
                    <label className="block text-xs font-semibold text-slate-700 mb-1" htmlFor="achievements">
                        Thành tích & Dự án tiêu biểu
                    </label>
                    <textarea
                        className="w-full rounded-xl border border-slate-200 px-3.5 py-2 text-sm text-slate-800 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-100"
                        disabled={loading}
                        id="achievements"
                        onChange={(e) => setAchievements(e.target.value)}
                        placeholder="VD: Giảm 25% thời gian phản hồi API, giải thưởng nhân viên xuất sắc..."
                        rows={2}
                        value={achievements}
                    />
                </div>

                <div className="mt-6 flex justify-end gap-3 pt-2">
                    <button
                        className="rounded-xl border border-slate-200 px-4 py-2 text-sm font-medium text-slate-600 transition hover:bg-slate-50"
                        disabled={loading}
                        onClick={onClose}
                        type="button"
                    >
                        Hủy
                    </button>
                    <button
                        className="inline-flex items-center gap-2 rounded-xl bg-blue-600 px-5 py-2 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:opacity-50"
                        disabled={loading}
                        type="submit"
                    >
                        {loading ? (
                            <>
                                <span className="inline-block size-4 animate-spin rounded-full border-2 border-white border-t-transparent" />
                                <span>Đang lưu...</span>
                            </>
                        ) : (
                            <span>{initialData ? 'Cập nhật' : 'Thêm kinh nghiệm'}</span>
                        )}
                    </button>
                </div>
            </form>
        </div>
    )
}

export function WorkExperienceModal({
    isOpen,
    onClose,
    onSuccess,
    initialData,
}: WorkExperienceModalProps) {
    if (!isOpen) return null

    return (
        <div
            aria-modal="true"
            className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4 backdrop-blur-sm"
            role="dialog"
        >
            <WorkExperienceForm
                key={initialData ? initialData.id : 'new'}
                initialData={initialData}
                onClose={onClose}
                onSuccess={onSuccess}
            />
        </div>
    )
}
