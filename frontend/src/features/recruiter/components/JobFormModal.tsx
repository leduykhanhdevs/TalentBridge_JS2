import { useEffect, useState } from 'react'
import { Briefcase, Calendar, DollarSign, Plus, Sparkles, X } from 'lucide-react'
import type { CreateJobPayload, RecruiterJobItem } from '../recruiterJobTypes'
import { CustomSelect } from '../../../components/ui/CustomSelect'
import { DateInput } from '../../../components/ui/DateInput'
import {
    EXPERIENCE_LABELS,
    JOB_TYPE_LABELS,
    validateJobForm,
} from '../recruiterJobValidation'

interface JobFormModalProps {
    isOpen: boolean
    onClose: () => void
    onSubmit: (payload: CreateJobPayload) => Promise<void>
    initialData?: RecruiterJobItem | null
    isSubmitting: boolean
    submitError: string | null
    onClearSubmitError: () => void
}

export function JobFormModal({
    isOpen,
    onClose,
    onSubmit,
    initialData,
    isSubmitting,
    submitError,
    onClearSubmitError,
}: JobFormModalProps) {
    const isEdit = Boolean(initialData)

    const [title, setTitle] = useState('')
    const [jobType, setJobType] = useState('FULL_TIME')
    const [experienceLevel, setExperienceLevel] = useState('1_TO_3_YEARS')
    const [city, setCity] = useState('Hồ Chí Minh')
    const [address, setAddress] = useState('')
    const [location, setLocation] = useState('Tại văn phòng')
    const [minSalary, setMinSalary] = useState<string>('')
    const [maxSalary, setMaxSalary] = useState<string>('')
    const [isNegotiable, setIsNegotiable] = useState(false)
    const [deadline, setDeadline] = useState('')
    const [description, setDescription] = useState('')
    const [requirements, setRequirements] = useState('')
    const [benefits, setBenefits] = useState('')
    const [skillInput, setSkillInput] = useState('')
    const [skills, setSkills] = useState<string[]>([])
    const [errors, setErrors] = useState<Record<string, string>>({})

    useEffect(() => {
        if (initialData) {
            setTitle(initialData.title || '')
            setJobType(initialData.jobType || 'FULL_TIME')
            setExperienceLevel(initialData.experienceLevel || '1_TO_3_YEARS')
            setCity(initialData.city || 'Hồ Chí Minh')
            setAddress(initialData.address || '')
            setLocation(initialData.location || 'Tại văn phòng')
            setMinSalary(initialData.minSalary ? String(initialData.minSalary) : '')
            setMaxSalary(initialData.maxSalary ? String(initialData.maxSalary) : '')
            setIsNegotiable(Boolean(initialData.isNegotiable))
            setDeadline(initialData.deadline ? initialData.deadline.split('T')[0] : '')
            setDescription(initialData.description || '')
            setRequirements(initialData.requirements || '')
            setBenefits(initialData.benefits || '')
            setSkills(initialData.skills || [])
        } else {
            // Default 30 days in future for deadline
            const nextMonth = new Date()
            nextMonth.setDate(nextMonth.getDate() + 30)
            const dateStr = nextMonth.toISOString().split('T')[0]

            setTitle('')
            setJobType('FULL_TIME')
            setExperienceLevel('1_TO_3_YEARS')
            setCity('Hồ Chí Minh')
            setAddress('')
            setLocation('Tại văn phòng')
            setMinSalary('')
            setMaxSalary('')
            setIsNegotiable(false)
            setDeadline(dateStr)
            setDescription('')
            setRequirements('')
            setBenefits('')
            setSkills(['Java', 'Spring Boot', 'MySQL'])
        }
        setErrors({})
    }, [initialData, isOpen])

    if (!isOpen) return null

    function handleAddSkill() {
        const trimmed = skillInput.trim()
        if (trimmed && !skills.includes(trimmed)) {
            setSkills([...skills, trimmed])
            setSkillInput('')
        }
    }

    function handleRemoveSkill(skillToRemove: string) {
        setSkills(skills.filter((s) => s !== skillToRemove))
    }

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault()

        const payload: CreateJobPayload = {
            title: title.trim(),
            description: description.trim(),
            requirements: requirements.trim() || undefined,
            benefits: benefits.trim() || undefined,
            location: location.trim() || undefined,
            city: city.trim(),
            address: address.trim() || undefined,
            jobType,
            experienceLevel,
            minSalary: isNegotiable || !minSalary ? null : Number(minSalary),
            maxSalary: isNegotiable || !maxSalary ? null : Number(maxSalary),
            isNegotiable,
            deadline,
            skills: skills.length > 0 ? skills : undefined,
        }

        const validationErrors = validateJobForm(payload)
        if (validationErrors.length > 0) {
            const errMap: Record<string, string> = {}
            validationErrors.forEach((err) => {
                errMap[err.field] = err.message
            })
            setErrors(errMap)
            return
        }

        setErrors({})
        await onSubmit(payload)
    }

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-xs overflow-y-auto">
            <div aria-labelledby="job-form-title" aria-modal="true" className="relative w-full max-w-3xl my-8 rounded-3xl border border-slate-200 bg-white p-6 sm:p-8 shadow-2xl" role="dialog">
                {/* Header */}
                <div className="flex items-center justify-between border-b border-slate-100 pb-4">
                    <div className="flex items-center gap-3">
                        <div className="grid size-10 place-items-center rounded-2xl bg-blue-50 text-blue-700 border border-blue-100">
                            <Briefcase size={20} />
                        </div>
                        <div>
                            <h2 className="text-xl font-bold tracking-tight text-slate-900" id="job-form-title">
                                {isEdit ? 'Chỉnh sửa tin tuyển dụng' : 'Đăng tin tuyển dụng mới'}
                            </h2>
                            <p className="text-xs text-slate-500 mt-0.5">
                                {isEdit
                                    ? 'Cập nhật nội dung và yêu cầu tuyển dụng cho ứng viên'
                                    : 'Tạo cơ hội việc làm mới để thu hút nhân tài'}
                            </p>
                        </div>
                    </div>
                    <button
                        type="button"
                        onClick={onClose}
                        className="rounded-xl p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-600 transition"
                    >
                        <X size={20} />
                    </button>
                </div>

                {/* Form */}
                <form onChangeCapture={onClearSubmitError} onSubmit={handleSubmit} className="mt-6 space-y-6 max-h-[70vh] overflow-y-auto pr-1">
                    {submitError && (
                        <div aria-live="assertive" className="flex items-start gap-2 rounded-xl border border-rose-200 bg-rose-50 p-3 text-sm font-medium text-rose-800" role="alert">
                            <span>{submitError}</span>
                        </div>
                    )}
                    {/* Tiêu đề công việc */}
                    <div>
                        <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5" htmlFor="job-title">
                            Tiêu đề công việc <span className="text-rose-500">*</span>
                        </label>
                        <input
                            id="job-title"
                            type="text"
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                            placeholder="Ví dụ: Senior Java Spring Boot Developer..."
                            className="w-full rounded-xl border border-slate-200 bg-slate-50/50 px-3.5 py-2.5 text-sm text-slate-900 transition focus:border-blue-600 focus:bg-white focus:outline-none focus:ring-3 focus:ring-blue-100"
                        />
                        {errors.title && <p className="mt-1 text-xs text-rose-500">{errors.title}</p>}
                    </div>

                    {/* Hình thức & Kinh nghiệm */}
                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                        <div>
                            <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5" htmlFor="job-type">
                                Hình thức làm việc <span className="text-rose-500">*</span>
                            </label>
                            <CustomSelect
                                id="job-type"
                                aria-label="Hình thức làm việc"
                                value={jobType}
                                onChange={setJobType}
                                options={Object.entries(JOB_TYPE_LABELS).map(([value, label]) => ({ value, label }))}
                            />
                        </div>

                        <div>
                            <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5" htmlFor="job-experience">
                                Cấp bậc / Kinh nghiệm <span className="text-rose-500">*</span>
                            </label>
                            <CustomSelect
                                id="job-experience"
                                aria-label="Cấp bậc / Kinh nghiệm"
                                value={experienceLevel}
                                onChange={setExperienceLevel}
                                options={Object.entries(EXPERIENCE_LABELS).map(([value, label]) => ({ value, label }))}
                            />
                        </div>
                    </div>

                    {/* Địa điểm & Thành phố */}
                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
                        <div>
                            <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5" htmlFor="job-city">
                                Thành phố <span className="text-rose-500">*</span>
                            </label>
                            <input
                                id="job-city"
                                type="text"
                                value={city}
                                onChange={(e) => setCity(e.target.value)}
                                placeholder="Hồ Chí Minh, Hà Nội..."
                                className="w-full rounded-xl border border-slate-200 bg-slate-50/50 px-3.5 py-2.5 text-sm text-slate-900 transition focus:border-blue-600 focus:bg-white focus:outline-none focus:ring-3 focus:ring-blue-100"
                            />
                            {errors.city && <p className="mt-1 text-xs text-rose-500">{errors.city}</p>}
                        </div>

                        <div>
                            <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5" htmlFor="job-location">
                                Hình thức địa điểm
                            </label>
                            <input
                                id="job-location"
                                type="text"
                                value={location}
                                onChange={(e) => setLocation(e.target.value)}
                                placeholder="Tại văn phòng, Hybrid..."
                                className="w-full rounded-xl border border-slate-200 bg-slate-50/50 px-3.5 py-2.5 text-sm text-slate-900 transition focus:border-blue-600 focus:bg-white focus:outline-none focus:ring-3 focus:ring-blue-100"
                            />
                        </div>

                        <div>
                            <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5" htmlFor="job-address">
                                Địa chỉ cụ thể
                            </label>
                            <input
                                id="job-address"
                                type="text"
                                value={address}
                                onChange={(e) => setAddress(e.target.value)}
                                placeholder="Tòa nhà, số đường..."
                                className="w-full rounded-xl border border-slate-200 bg-slate-50/50 px-3.5 py-2.5 text-sm text-slate-900 transition focus:border-blue-600 focus:bg-white focus:outline-none focus:ring-3 focus:ring-blue-100"
                            />
                        </div>
                    </div>

                    {/* Mức lương */}
                    <div className="rounded-2xl border border-slate-200/80 bg-slate-50/50 p-4 space-y-3">
                        <div className="flex items-center justify-between">
                            <span className="text-xs font-bold uppercase tracking-wider text-slate-700 flex items-center gap-1.5">
                                <DollarSign size={14} className="text-emerald-600" /> Mức lương (VNĐ)
                            </span>
                            <label className="flex items-center gap-2 cursor-pointer text-xs font-semibold text-slate-700">
                                <input
                                    type="checkbox"
                                    checked={isNegotiable}
                                    onChange={(e) => setIsNegotiable(e.target.checked)}
                                    className="rounded border-slate-300 text-blue-700 focus:ring-blue-600"
                                />
                                Lương thỏa thuận
                            </label>
                        </div>

                        {!isNegotiable && (
                            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                                <div>
                                    <input
                                        aria-label="Lương tối thiểu (VNĐ)"
                                        type="number"
                                        min="0"
                                        step="500000"
                                        value={minSalary}
                                        onChange={(e) => setMinSalary(e.target.value)}
                                        placeholder="Lương tối thiểu (ví dụ: 15000000)"
                                        className="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2 text-sm text-slate-900 focus:border-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-100"
                                    />
                                    {errors.minSalary && <p className="mt-1 text-xs text-rose-500">{errors.minSalary}</p>}
                                </div>
                                <div>
                                    <input
                                        aria-label="Lương tối đa (VNĐ)"
                                        type="number"
                                        min="0"
                                        step="500000"
                                        value={maxSalary}
                                        onChange={(e) => setMaxSalary(e.target.value)}
                                        placeholder="Lương tối đa (ví dụ: 30000000)"
                                        className="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2 text-sm text-slate-900 focus:border-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-100"
                                    />
                                    {errors.maxSalary && <p className="mt-1 text-xs text-rose-500">{errors.maxSalary}</p>}
                                </div>
                            </div>
                        )}
                    </div>

                    {/* Hạn nộp hồ sơ */}
                    <div>
                        <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5 flex items-center gap-1.5" htmlFor="job-deadline">
                            <Calendar size={14} className="text-blue-700" /> Hạn nộp hồ sơ <span className="text-rose-500">*</span>
                        </label>
                        <DateInput
                            id="job-deadline"
                            value={deadline}
                            onChange={setDeadline}
                            aria-label="Hạn nộp hồ sơ"
                            className="sm:max-w-64"
                        />
                        {errors.deadline && <p className="mt-1 text-xs text-rose-500">{errors.deadline}</p>}
                    </div>

                    {/* Kỹ năng yêu cầu */}
                    <div>
                        <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5 flex items-center gap-1.5" htmlFor="job-skill-input">
                            <Sparkles size={14} className="text-amber-500" /> Kỹ năng yêu cầu
                        </label>
                        <div className="flex gap-2 mb-2">
                            <input
                                id="job-skill-input"
                                type="text"
                                value={skillInput}
                                onChange={(e) => setSkillInput(e.target.value)}
                                onKeyDown={(e) => {
                                    if (e.key === 'Enter') {
                                        e.preventDefault()
                                        handleAddSkill()
                                    }
                                }}
                                placeholder="Nhập tên kỹ năng (ví dụ: Docker, Redis) rồi bấm Thêm..."
                                className="flex-1 rounded-xl border border-slate-200 bg-slate-50/50 px-3.5 py-2 text-sm text-slate-900 focus:border-blue-600 focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-100"
                            />
                            <button
                                type="button"
                                onClick={handleAddSkill}
                                className="inline-flex items-center gap-1 rounded-xl bg-slate-900 px-4 py-2 text-xs font-bold text-white hover:bg-slate-800 transition"
                            >
                                <Plus size={14} /> Thêm
                            </button>
                        </div>
                        {skills.length > 0 && (
                            <div className="flex flex-wrap gap-1.5">
                                {skills.map((skill) => (
                                    <span
                                        key={skill}
                                        className="inline-flex items-center gap-1 rounded-lg bg-blue-50 border border-blue-100 px-2.5 py-1 text-xs font-semibold text-blue-800"
                                    >
                                        {skill}
                                        <button
                                            type="button"
                                            onClick={() => handleRemoveSkill(skill)}
                                            className="text-blue-400 hover:text-blue-950"
                                        >
                                            <X size={12} />
                                        </button>
                                    </span>
                                ))}
                            </div>
                        )}
                    </div>

                    {/* Mô tả công việc */}
                    <div>
                        <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5" htmlFor="job-description">
                            Mô tả công việc <span className="text-rose-500">*</span>
                        </label>
                        <textarea
                            id="job-description"
                            rows={4}
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            placeholder="Mô tả trách nhiệm, công việc hàng ngày của vị trí này..."
                            className="w-full rounded-xl border border-slate-200 bg-slate-50/50 p-3 text-sm text-slate-900 focus:border-blue-600 focus:bg-white focus:outline-none focus:ring-3 focus:ring-blue-100"
                        />
                        {errors.description && <p className="mt-1 text-xs text-rose-500">{errors.description}</p>}
                    </div>

                    {/* Yêu cầu ứng viên */}
                    <div>
                        <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5" htmlFor="job-requirements">
                            Yêu cầu ứng viên
                        </label>
                        <textarea
                            id="job-requirements"
                            rows={3}
                            value={requirements}
                            onChange={(e) => setRequirements(e.target.value)}
                            placeholder="- Tối thiểu 2 năm kinh nghiệm với Java...&#10;- Khả năng giải quyết vấn đề tốt..."
                            className="w-full rounded-xl border border-slate-200 bg-slate-50/50 p-3 text-sm text-slate-900 focus:border-blue-600 focus:bg-white focus:outline-none focus:ring-3 focus:ring-blue-100"
                        />
                    </div>

                    {/* Quyền lợi */}
                    <div>
                        <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5" htmlFor="job-benefits">
                            Quyền lợi được hưởng
                        </label>
                        <textarea
                            id="job-benefits"
                            rows={3}
                            value={benefits}
                            onChange={(e) => setBenefits(e.target.value)}
                            placeholder="- Lương tháng 13 + thưởng KPI...&#10;- Gói bảo hiểm sức khỏe cao cấp..."
                            className="w-full rounded-xl border border-slate-200 bg-slate-50/50 p-3 text-sm text-slate-900 focus:border-blue-600 focus:bg-white focus:outline-none focus:ring-3 focus:ring-blue-100"
                        />
                    </div>

                    {/* Submit Actions */}
                    <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-100">
                        <button
                            type="button"
                            onClick={onClose}
                            className="rounded-xl border border-slate-200 px-5 py-2.5 text-xs font-bold text-slate-700 hover:bg-slate-100 transition"
                        >
                            Hủy bỏ
                        </button>
                        <button
                            type="submit"
                            disabled={isSubmitting}
                            className="rounded-xl bg-blue-700 px-6 py-2.5 text-xs font-bold text-white hover:bg-blue-800 transition disabled:opacity-50 shadow-md shadow-blue-700/20"
                        >
                            {isSubmitting
                                ? 'Đang lưu...'
                                : isEdit
                                ? 'Cập nhật tin tuyển dụng'
                                : 'Đăng tin tuyển dụng'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    )
}
