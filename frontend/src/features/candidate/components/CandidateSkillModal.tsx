import { useState } from 'react'
import { Award, Star, X } from 'lucide-react'
import { addCandidateSkill, CandidateApiError } from '../candidateApi'
import type { SkillItem } from '../candidateTypes'

interface CandidateSkillModalProps {
    isOpen: boolean
    onClose: () => void
    onSuccess: () => void
    availableSkills: SkillItem[]
}

const PROFICIENCY_OPTIONS = [
    { value: 'BEGINNER', label: 'Mới bắt đầu (Cơ bản)' },
    { value: 'ELEMENTARY', label: 'Sơ cấp (Đã làm quen)' },
    { value: 'INTERMEDIATE', label: 'Trung cấp (Làm việc độc lập)' },
    { value: 'ADVANCED', label: 'Cao cấp (Thành thạo)' },
    { value: 'EXPERT', label: 'Chuyên gia (Xuất sắc / Lead)' },
]

export function CandidateSkillModal({
    isOpen,
    onClose,
    onSuccess,
    availableSkills,
}: CandidateSkillModalProps) {
    const [selectedSkillName, setSelectedSkillName] = useState('')
    const [customSkillName, setCustomSkillName] = useState('')
    const [isCustom, setIsCustom] = useState(false)
    const [rating, setRating] = useState(3)
    const [hoverRating, setHoverRating] = useState(0)
    const [proficiencyLevel, setProficiencyLevel] = useState('INTERMEDIATE')
    const [yearsOfExperience, setYearsOfExperience] = useState('1.0')

    const [loading, setLoading] = useState(false)
    const [errorMsg, setErrorMsg] = useState<string | null>(null)

    if (!isOpen) return null

    function handleRatingChange(stars: number) {
        setRating(stars)
        if (stars === 1) setProficiencyLevel('BEGINNER')
        else if (stars === 2) setProficiencyLevel('ELEMENTARY')
        else if (stars === 3) setProficiencyLevel('INTERMEDIATE')
        else if (stars === 4) setProficiencyLevel('ADVANCED')
        else if (stars === 5) setProficiencyLevel('EXPERT')
    }

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault()
        setErrorMsg(null)

        const skillName = isCustom ? customSkillName.trim() : selectedSkillName.trim()
        if (!skillName) {
            setErrorMsg('Vui lòng chọn hoặc nhập tên kỹ năng.')
            return
        }

        const matchedMaster = availableSkills.find(
            (s) => s.name.toLowerCase() === skillName.toLowerCase(),
        )

        setLoading(true)
        try {
            await addCandidateSkill({
                skillId: matchedMaster ? matchedMaster.id : undefined,
                skillName: matchedMaster ? undefined : skillName,
                proficiencyLevel,
                rating,
                yearsOfExperience: parseFloat(yearsOfExperience) || 1.0,
            })
            onSuccess()
            onClose()
            setSelectedSkillName('')
            setCustomSkillName('')
            setIsCustom(false)
            setRating(3)
            setYearsOfExperience('1.0')
        } catch (err) {
            if (err instanceof CandidateApiError) {
                setErrorMsg(err.message)
            } else {
                setErrorMsg('Đã có lỗi xảy ra khi lưu kỹ năng.')
            }
        } finally {
            setLoading(false)
        }
    }

    return (
        <div
            aria-modal="true"
            className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4 backdrop-blur-sm"
            role="dialog"
        >
            <div className="relative w-full max-w-md rounded-2xl bg-white p-6 shadow-2xl transition-all">
                <div className="flex items-center justify-between border-b border-slate-100 pb-4">
                    <div className="flex items-center gap-2 text-slate-800">
                        <div className="grid size-9 place-items-center rounded-xl bg-amber-50 text-amber-600">
                            <Award size={20} />
                        </div>
                        <h2 className="text-lg font-bold">Thêm kỹ năng chuyên môn</h2>
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
                        <div className="flex items-center justify-between mb-1">
                            <label className="block text-xs font-semibold text-slate-700">
                                Kỹ năng <span className="text-rose-500">*</span>
                            </label>
                            <button
                                className="text-xs text-blue-600 hover:underline"
                                onClick={() => setIsCustom(!isCustom)}
                                type="button"
                            >
                                {isCustom ? 'Chọn từ danh mục' : 'Nhập kỹ năng khác'}
                            </button>
                        </div>

                        {isCustom ? (
                            <input
                                className="w-full rounded-xl border border-slate-200 px-3.5 py-2 text-sm text-slate-800 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-100"
                                disabled={loading}
                                onChange={(e) => setCustomSkillName(e.target.value)}
                                placeholder="VD: Kubernetes, GraphQL, Next.js..."
                                required
                                type="text"
                                value={customSkillName}
                            />
                        ) : (
                            <select
                                className="w-full rounded-xl border border-slate-200 px-3.5 py-2 text-sm text-slate-800 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-100"
                                disabled={loading}
                                onChange={(e) => setSelectedSkillName(e.target.value)}
                                required
                                value={selectedSkillName}
                            >
                                <option value="">-- Chọn kỹ năng tiêu chuẩn --</option>
                                {availableSkills.map((s) => (
                                    <option key={s.id} value={s.name}>
                                        {s.name}
                                    </option>
                                ))}
                            </select>
                        )}
                    </div>

                    {/* Star Rating Selector */}
                    <div>
                        <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                            Mức độ thành thạo ({rating}/5 sao) <span className="text-rose-500">*</span>
                        </label>
                        <div className="flex items-center gap-1.5">
                            {[1, 2, 3, 4, 5].map((star) => {
                                const active = (hoverRating || rating) >= star
                                return (
                                    <button
                                        className="p-1 text-slate-300 transition hover:scale-110 focus:outline-none"
                                        key={star}
                                        onClick={() => handleRatingChange(star)}
                                        onMouseEnter={() => setHoverRating(star)}
                                        onMouseLeave={() => setHoverRating(0)}
                                        type="button"
                                    >
                                        <Star
                                            className={`size-6 ${
                                                active
                                                    ? 'fill-amber-400 text-amber-400'
                                                    : 'text-slate-300'
                                            }`}
                                        />
                                    </button>
                                )
                            })}
                            <span className="ml-2 text-xs font-semibold text-amber-700 bg-amber-50 px-2 py-0.5 rounded-md border border-amber-200">
                                {rating === 1 && 'Cơ bản (1 sao)'}
                                {rating === 2 && 'Sơ cấp (2 sao)'}
                                {rating === 3 && 'Trung cấp (3 sao)'}
                                {rating === 4 && 'Thành thạo (4 sao)'}
                                {rating === 5 && 'Chuyên gia (5 sao)'}
                            </span>
                        </div>
                    </div>

                    <div>
                        <label className="block text-xs font-semibold text-slate-700 mb-1" htmlFor="proficiency-level">
                            Cấp độ đánh giá
                        </label>
                        <select
                            className="w-full rounded-xl border border-slate-200 px-3.5 py-2 text-sm text-slate-800 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-100"
                            disabled={loading}
                            id="proficiency-level"
                            onChange={(e) => setProficiencyLevel(e.target.value)}
                            value={proficiencyLevel}
                        >
                            {PROFICIENCY_OPTIONS.map((opt) => (
                                <option key={opt.value} value={opt.value}>
                                    {opt.label}
                                </option>
                            ))}
                        </select>
                    </div>

                    <div>
                        <label className="block text-xs font-semibold text-slate-700 mb-1" htmlFor="years-exp">
                            Số năm kinh nghiệm với kỹ năng này
                        </label>
                        <input
                            className="w-full rounded-xl border border-slate-200 px-3.5 py-2 text-sm text-slate-800 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-100"
                            disabled={loading}
                            id="years-exp"
                            min="0.1"
                            onChange={(e) => setYearsOfExperience(e.target.value)}
                            placeholder="VD: 2.5"
                            step="0.5"
                            type="number"
                            value={yearsOfExperience}
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
                                <span>Thêm kỹ năng</span>
                            )}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    )
}
