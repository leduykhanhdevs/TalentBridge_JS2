import { useRef, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
    AlertCircle,
    Award,
    Briefcase,
    Building2,
    Calendar,
    CheckCircle2,
    Code2,
    Edit3,
    ExternalLink,
    Globe,
    Mail,
    MapPin,
    Phone,
    Plus,
    RefreshCw,
    Save,
    Share2,
    Sparkles,
    Star,
    Trash2,
    User,
    X,
    FileText,
    UploadCloud,
} from 'lucide-react'
import {
    deleteCandidateSkill,
    deleteWorkExperience,
    getAllMasterSkills,
    getCandidateProfile,
    getCandidateResumes,
    getCandidateSkills,
    getWorkExperiences,
    updateCandidateProfile,
    uploadCandidateAvatar,
} from '../../features/candidate/candidateApi'
import type {
    CandidateSkill,
    UpdateCandidateProfileRequest,
    WorkExperience,
} from '../../features/candidate/candidateTypes'
import { validateCandidateProfile, getVietnamTodayString } from '../../features/candidate/candidateValidation'
import { WorkExperienceModal } from '../../features/candidate/components/WorkExperienceModal'
import { CandidateSkillModal } from '../../features/candidate/components/CandidateSkillModal'
import { ResumeUploadSection } from '../../features/candidate/components/ResumeUploadSection'
import { CvBuilderModal } from '../../features/candidate/components/CvBuilderModal'
import { CvImportModal } from '../../features/candidate/components/CvImportModal'
import { EmptyStateIllustration } from '../../components/illustrations'
import { CustomSelect } from '../../components/ui/CustomSelect'
import { DateInput } from '../../components/ui/DateInput'
import { formatIsoDate } from '../../components/ui/dateFormat'

export function CandidateProfilePage() {
    const queryClient = useQueryClient()
    const [isEditing, setIsEditing] = useState(false)
    const [successMessage, setSuccessMessage] = useState<string | null>(null)
    const [errorMessage, setErrorMessage] = useState<string | null>(null)

    // Modals state for Work Experience, Skills, CV Builder & CV Import
    const [isExpModalOpen, setIsExpModalOpen] = useState(false)
    const [editingExp, setEditingExp] = useState<WorkExperience | null>(null)
    const [isSkillModalOpen, setIsSkillModalOpen] = useState(false)
    const [isCvBuilderOpen, setIsCvBuilderOpen] = useState(false)
    const [isCvImportOpen, setIsCvImportOpen] = useState(false)

    const {
        data: profile,
        isLoading,
        isError,
        error,
        refetch,
    } = useQuery({
        queryKey: ['candidate-profile'],
        queryFn: getCandidateProfile,
    })

    const { data: experiences = [], refetch: refetchExperiences } = useQuery({
        queryKey: ['work-experiences'],
        queryFn: getWorkExperiences,
    })

    const { data: skills = [], refetch: refetchSkills } = useQuery({
        queryKey: ['candidate-skills'],
        queryFn: getCandidateSkills,
    })

    const { data: masterSkills = [] } = useQuery({
        queryKey: ['master-skills'],
        queryFn: getAllMasterSkills,
    })

    const { data: resumes = [], refetch: refetchResumes } = useQuery({
        queryKey: ['candidate-resumes'],
        queryFn: getCandidateResumes,
    })

    async function handleDeleteExp(id: number) {
        if (window.confirm('Bạn có chắc chắn muốn xóa kinh nghiệm làm việc này không?')) {
            try {
                await deleteWorkExperience(id)
                refetchExperiences()
                refetch()
            } catch {
                setErrorMessage('Không thể xóa kinh nghiệm làm việc.')
            }
        }
    }

    async function handleDeleteSkill(id: number) {
        if (window.confirm('Bạn có chắc chắn muốn xóa kỹ năng này không?')) {
            try {
                await deleteCandidateSkill(id)
                refetchSkills()
            } catch {
                setErrorMessage('Không thể xóa kỹ năng.')
            }
        }
    }

    const [formData, setFormData] = useState<UpdateCandidateProfileRequest>({})
    const [formErrors, setFormErrors] = useState<Record<string, string>>({})
    const avatarInputRef = useRef<HTMLInputElement>(null)
    const [isAvatarDragging, setIsAvatarDragging] = useState(false)
    const [isAvatarUploading, setIsAvatarUploading] = useState(false)

    async function handleAvatarFile(file?: File) {
        if (!file) return
        if (!['image/jpeg', 'image/png'].includes(file.type)) {
            setErrorMessage('Ảnh đại diện chỉ hỗ trợ định dạng JPG hoặc PNG.')
            return
        }
        if (file.size > 5 * 1024 * 1024) {
            setErrorMessage('Ảnh đại diện tối đa 5 MB.')
            return
        }

        setIsAvatarUploading(true)
        setErrorMessage(null)
        try {
            const avatarUrl = await uploadCandidateAvatar(file)
            setFormData((current) => ({ ...current, avatarUrl }))
        } catch (err) {
            setErrorMessage(err instanceof Error ? err.message : 'Không thể tải ảnh đại diện lên.')
        } finally {
            setIsAvatarUploading(false)
            if (avatarInputRef.current) avatarInputRef.current.value = ''
        }
    }

    const handleStartEditing = () => {
        if (!profile) return
        setFormData({
            fullName: profile.fullName || '',
            phone: profile.phone || '',
            avatarUrl: profile.avatarUrl || '',
            title: profile.title || '',
            dob: profile.dob || '',
            gender: profile.gender || 'OTHER',
            summary: profile.summary || '',
            currentSalary: profile.currentSalary ?? undefined,
            expectedSalary: profile.expectedSalary ?? undefined,
            city: profile.city || '',
            address: profile.address || '',
            personalWebsite: profile.personalWebsite || '',
            linkedinUrl: profile.linkedinUrl || '',
            githubUrl: profile.githubUrl || '',
        })
        setFormErrors({})
        setSuccessMessage(null)
        setErrorMessage(null)
        setIsEditing(true)
    }

    const handleCancel = () => {
        setIsEditing(false)
        setFormErrors({})
        setErrorMessage(null)
    }

    const mutation = useMutation({
        mutationFn: updateCandidateProfile,
        onSuccess: (updated) => {
            queryClient.setQueryData(['candidate-profile'], updated)
            setIsEditing(false)
            setSuccessMessage('Hồ sơ ứng viên đã được cập nhật thành công!')
            setErrorMessage(null)
            setTimeout(() => setSuccessMessage(null), 5000)
        },
        onError: (err: Error) => {
            setErrorMessage(err.message || 'Đã xảy ra lỗi khi lưu hồ sơ.')
        },
    })

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault()
        const errors = validateCandidateProfile({
            fullName: formData.fullName,
            phone: formData.phone,
            dob: formData.dob,
            currentSalary: formData.currentSalary,
            expectedSalary: formData.expectedSalary,
            personalWebsite: formData.personalWebsite,
            linkedinUrl: formData.linkedinUrl,
            githubUrl: formData.githubUrl,
        })

        if (Object.keys(errors).length > 0) {
            setFormErrors(errors as Record<string, string>)
            return
        }

        setFormErrors({})
        mutation.mutate(formData)
    }

    if (isLoading) {
        return (
            <div className="flex min-h-[500px] items-center justify-center">
                <div className="flex flex-col items-center gap-3">
                    <RefreshCw className="h-8 w-8 animate-spin text-blue-600" />
                    <p className="text-sm font-medium text-slate-500">Đang tải hồ sơ ứng viên...</p>
                </div>
            </div>
        )
    }

    if (isError) {
        return (
            <div className="mx-auto max-w-4xl py-12 px-4">
                <div className="rounded-2xl border border-rose-200 bg-rose-50 p-6 text-center">
                    <AlertCircle className="mx-auto h-12 w-12 text-rose-500 mb-3" />
                    <h2 className="text-lg font-bold text-rose-800">Không thể tải hồ sơ</h2>
                    <p className="mt-1 text-sm text-rose-600">{(error as Error)?.message || 'Vui lòng kiểm tra kết nối và thử lại.'}</p>
                    <button
                        onClick={() => refetch()}
                        className="mt-4 inline-flex items-center gap-2 rounded-xl bg-rose-600 px-4 py-2 text-sm font-semibold text-white shadow hover:bg-rose-700 transition"
                    >
                        <RefreshCw className="h-4 w-4" /> Thử lại
                    </button>
                </div>
            </div>
        )
    }

    if (!profile) return null

    const todayDate = getVietnamTodayString()
    const experienceDisplayLabel = experiences.length === 0
        ? 'Chưa có kinh nghiệm (0 năm)'
        : profile.experienceYears
            ? `${profile.experienceYears} năm (tự động tính)`
            : 'Dưới 1 năm (tự động tính)'
    const hasSkillExperienceExceeding = skills.some(
        (skill) => (skill.yearsOfExperience ?? 0) > (profile.experienceYears ?? 0),
    )

    const formatCurrency = (val: number | null | undefined) => {
        if (!val || val <= 0) return 'Thỏa thuận'
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
    }

    return (
        <div className="min-h-screen bg-slate-50 px-4 py-6 sm:px-6 lg:px-8">
            <div className="mx-auto w-full max-w-[1760px] space-y-5">
                <header className="space-y-1">
                    <p className="text-xs font-semibold uppercase tracking-[0.16em] text-blue-700">Hồ sơ ứng viên</p>
                    <h1 className="text-2xl font-bold tracking-tight text-slate-900 sm:text-[28px]">
                        {isEditing ? 'Hoàn thiện hồ sơ của bạn' : 'Thể hiện năng lực bằng trải nghiệm'}
                    </h1>
                    <p className="max-w-2xl text-sm leading-6 text-slate-600">
                        Quản lý thông tin nghề nghiệp, kinh nghiệm và CV tại một nơi.
                    </p>
                </header>

                {/* Header Actions & Notifications */}
                {successMessage && (
                    <div className="flex items-center gap-3 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-emerald-800 shadow-sm animate-in fade-in">
                        <CheckCircle2 className="h-5 w-5 shrink-0 text-emerald-600" />
                        <p className="text-sm font-medium">{successMessage}</p>
                    </div>
                )}

                {errorMessage && (
                    <div className="flex items-center gap-3 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-rose-800 shadow-sm">
                        <AlertCircle className="h-5 w-5 shrink-0 text-rose-600" />
                        <p className="text-sm font-medium">{errorMessage}</p>
                    </div>
                )}

                <div className={isEditing ? 'space-y-5' : 'grid grid-cols-1 items-start gap-5 lg:grid-cols-[280px_minmax(0,1fr)]'}>
                    <aside className={isEditing ? 'contents' : 'min-w-0 lg:sticky lg:top-24'}>
                        {/* Profile summary follows the compact left rail in the V2 profile layout. */}
                        <div className={`rounded-xl border border-slate-200 bg-white p-5 shadow-sm ${isEditing ? '' : 'space-y-5'}`}>
                    <div className={`flex flex-col justify-between gap-5 ${isEditing ? 'sm:flex-row sm:items-center' : 'items-start'}`}>
                        <div className={`flex min-w-0 ${isEditing ? 'items-center gap-5' : 'w-full flex-col items-start gap-4'}`}>
                            <div className="flex h-[72px] w-[72px] shrink-0 items-center justify-center overflow-hidden rounded-xl bg-blue-50 text-2xl font-bold text-blue-700 ring-1 ring-blue-100">
                                {profile.avatarUrl ? (
                                    <img
                                        src={profile.avatarUrl}
                                        alt={profile.fullName}
                                        className="h-full w-full object-cover"
                                    />
                                ) : (
                                    profile.fullName?.charAt(0)?.toUpperCase() || 'U'
                                )}
                            </div>

                            <div className="min-w-0">
                                <div className="flex flex-wrap items-center gap-2">
                                    <h2 className="break-words text-xl font-bold tracking-tight text-slate-900">
                                        {profile.fullName || 'Chưa cập nhật tên'}
                                    </h2>
                                    <span className="rounded-full bg-blue-50 px-3 py-1 text-xs font-semibold text-blue-700 ring-1 ring-inset ring-blue-700/10">
                                        Ứng viên
                                    </span>
                                </div>
                                <p className="mt-1 text-sm font-medium text-slate-600 flex items-center gap-1.5">
                                    <Briefcase className="h-4 w-4 text-slate-400" />
                                    {profile.title || 'Chưa có chức danh nghề nghiệp'}
                                </p>
                                <div className={`mt-3 flex flex-wrap gap-x-4 gap-y-2 text-xs text-slate-600 ${isEditing ? 'items-center' : 'flex-col'}`}>
                                    <span className="flex min-w-0 items-center gap-1.5 break-all">
                                        <Mail className="h-3.5 w-3.5 text-slate-400" />
                                        {profile.email}
                                    </span>
                                    {profile.phone && (
                                        <span className="flex items-center gap-1.5">
                                            <Phone className="h-3.5 w-3.5 text-slate-400" />
                                            {profile.phone}
                                        </span>
                                    )}
                                    {profile.city && (
                                        <span className="flex items-center gap-1.5">
                                            <MapPin className="h-3.5 w-3.5 text-slate-400" />
                                            {profile.city}
                                        </span>
                                    )}
                                </div>
                            </div>
                        </div>

                        {!isEditing ? (
                            <div className={`flex gap-2.5 ${isEditing ? 'flex-wrap items-center' : 'w-full flex-col'}`}>
                                <button
                                    type="button"
                                    onClick={() => setIsCvImportOpen(true)}
                                    className="inline-flex items-center gap-2 rounded-lg border border-slate-200 bg-white px-3 py-2.5 text-left text-xs font-semibold text-slate-700 transition hover:border-blue-200 hover:bg-blue-50"
                                >
                                    <Sparkles className="h-4 w-4 text-blue-700" /> Nhập nhanh từ CV
                                </button>
                                <button
                                    type="button"
                                    onClick={() => setIsCvBuilderOpen(true)}
                                    className="inline-flex items-center gap-2 rounded-lg border border-slate-200 bg-white px-3 py-2.5 text-left text-xs font-semibold text-slate-700 transition hover:border-blue-200 hover:bg-blue-50"
                                >
                                    <FileText className="h-4 w-4 text-blue-600" /> Tạo CV từ Hồ sơ
                                </button>
                                <button
                                    type="button"
                                    onClick={handleStartEditing}
                                    className="inline-flex items-center gap-2 rounded-lg bg-blue-700 px-3 py-2.5 text-left text-xs font-semibold text-white transition hover:bg-blue-800"
                                >
                                    <Edit3 className="h-4 w-4" /> Chỉnh sửa hồ sơ
                                </button>
                            </div>
                        ) : (
                            <div className="flex items-center gap-2">
                                <button
                                    type="button"
                                    onClick={handleCancel}
                                    className="inline-flex items-center gap-1.5 rounded-xl border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50 transition"
                                >
                                    <X className="h-4 w-4" /> Hủy
                                </button>
                                <button
                                    form="candidate-profile-form"
                                    type="submit"
                                    disabled={mutation.isPending}
                                    className="inline-flex items-center gap-1.5 rounded-xl bg-emerald-600 px-5 py-2 text-sm font-semibold text-white shadow hover:bg-emerald-700 disabled:opacity-50 transition"
                                >
                                    {mutation.isPending ? (
                                        <RefreshCw className="h-4 w-4 animate-spin" />
                                    ) : (
                                        <Save className="h-4 w-4" />
                                    )}
                                    Lưu thay đổi
                                </button>
                            </div>
                        )}
                    </div>
                        </div>
                    </aside>

                {/* Main Content Area */}
                    <section aria-label="Nội dung hồ sơ" className={isEditing ? 'contents' : 'min-w-0 space-y-5'}>
                {isEditing ? (
                    /* EDIT FORM */
                    <form
                        id="candidate-profile-form"
                        onSubmit={handleSubmit}
                        className="space-y-5"
                    >
                        {/* Personal Info Group */}
                        <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
                            <h2 className="text-base font-bold text-slate-900 mb-5 flex items-center gap-2">
                                <User className="h-5 w-5 text-blue-600" /> Thông tin cá nhân
                            </h2>
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
                                <div className="sm:col-span-2">
                                    <label className="mb-1.5 block text-xs font-semibold text-slate-700" htmlFor="candidate-avatar-url">
                                        Ảnh đại diện
                                    </label>
                                    <div className="flex flex-col gap-4 rounded-2xl border border-dashed border-slate-300 bg-slate-50/70 p-4 sm:flex-row sm:items-center">
                                        <div className="grid size-20 shrink-0 place-items-center overflow-hidden rounded-2xl bg-blue-100 text-2xl font-bold text-blue-700">
                                            {formData.avatarUrl ? (
                                                <img alt="Xem trước ảnh đại diện" className="size-full object-cover" src={formData.avatarUrl} />
                                            ) : (
                                                formData.fullName?.charAt(0)?.toUpperCase() || 'U'
                                            )}
                                        </div>
                                        <div className="min-w-0 flex-1">
                                            <label
                                                className={`flex min-h-20 cursor-pointer flex-col justify-center rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm transition ${isAvatarDragging ? 'border-blue-500 bg-blue-50' : 'hover:border-blue-300 hover:bg-blue-50/50'} ${isAvatarUploading ? 'pointer-events-none opacity-60' : ''}`}
                                                htmlFor="candidate-avatar-file"
                                                onDragEnter={(event) => { event.preventDefault(); setIsAvatarDragging(true) }}
                                                onDragOver={(event) => event.preventDefault()}
                                                onDragLeave={(event) => {
                                                    if (!event.currentTarget.contains(event.relatedTarget as Node | null)) setIsAvatarDragging(false)
                                                }}
                                                onDrop={(event) => {
                                                    event.preventDefault()
                                                    setIsAvatarDragging(false)
                                                    void handleAvatarFile(event.dataTransfer.files[0])
                                                }}
                                            >
                                                <input
                                                    accept="image/png,image/jpeg,.png,.jpg,.jpeg"
                                                    className="sr-only"
                                                    disabled={isAvatarUploading}
                                                    id="candidate-avatar-file"
                                                    onChange={(event) => void handleAvatarFile(event.target.files?.[0])}
                                                    ref={avatarInputRef}
                                                    type="file"
                                                />
                                                <span className="flex items-center gap-2 font-semibold text-slate-800">
                                                    <UploadCloud aria-hidden="true" className="size-4 text-blue-700" />
                                                    {isAvatarUploading ? 'Đang tải ảnh lên...' : 'Thả ảnh vào đây hoặc bấm để chọn từ thiết bị'}
                                                </span>
                                                <span className="mt-1 text-xs text-slate-500">JPG hoặc PNG, tối đa 5 MB.</span>
                                            </label>
                                            <div className="mt-3 flex flex-col gap-2 sm:flex-row sm:items-center">
                                                <input
                                                    className="min-w-0 flex-1 rounded-xl border border-slate-200 bg-white px-3.5 py-2 text-sm text-slate-800 shadow-sm outline-none focus:border-blue-500 focus:ring-3 focus:ring-blue-100"
                                                    id="candidate-avatar-url"
                                                    maxLength={500}
                                                    onChange={(event) => setFormData({ ...formData, avatarUrl: event.target.value })}
                                                    placeholder="Hoặc dán đường dẫn https://..."
                                                    type="url"
                                                    value={formData.avatarUrl || ''}
                                                />
                                                {formData.avatarUrl && (
                                                    <button
                                                        className="shrink-0 rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100"
                                                        onClick={() => setFormData({ ...formData, avatarUrl: '' })}
                                                        type="button"
                                                    >
                                                        Xóa ảnh
                                                    </button>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div>
                                    <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                                        Họ và tên <span className="text-rose-500">*</span>
                                    </label>
                                    <input
                                        type="text"
                                        value={formData.fullName || ''}
                                        onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                                        className={`w-full rounded-xl border px-3.5 py-2 text-sm text-slate-800 shadow-sm focus:outline-none focus:ring-2 ${
                                            formErrors.fullName
                                                ? 'border-rose-300 focus:ring-rose-400'
                                                : 'border-slate-200 focus:ring-blue-500'
                                        }`}
                                        placeholder="VD: Trần Ứng Viên"
                                    />
                                    {formErrors.fullName && (
                                        <p className="mt-1 text-xs text-rose-500">{formErrors.fullName}</p>
                                    )}
                                </div>

                                <div>
                                    <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                                        Số điện thoại
                                    </label>
                                    <input
                                        type="text"
                                        value={formData.phone || ''}
                                        onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                                        className={`w-full rounded-xl border px-3.5 py-2 text-sm text-slate-800 shadow-sm focus:outline-none focus:ring-2 ${
                                            formErrors.phone
                                                ? 'border-rose-300 focus:ring-rose-400'
                                                : 'border-slate-200 focus:ring-blue-500'
                                        }`}
                                        placeholder="VD: 0912345678"
                                    />
                                    {formErrors.phone && (
                                        <p className="mt-1 text-xs text-rose-500">{formErrors.phone}</p>
                                    )}
                                </div>

                                <div>
                                    <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                                        Ngày sinh
                                    </label>
                                    <DateInput
                                        id="candidate-dob"
                                        aria-label="Ngày sinh"
                                        max={todayDate}
                                        onChange={(dob) => {
                                            setFormData({ ...formData, dob })
                                            setFormErrors((prev) => {
                                                const next = { ...prev }
                                                if (dob && dob > getVietnamTodayString()) {
                                                    next.dob = 'Ngày sinh không được ở trong tương lai'
                                                } else {
                                                    delete next.dob
                                                }
                                                return next
                                            })
                                        }}
                                        value={formData.dob || ''}
                                        className={formErrors.dob ? 'border-rose-300 focus:border-rose-400 focus:ring-rose-100' : ''}
                                    />
                                    {formErrors.dob && (
                                        <p className="mt-1 text-xs text-rose-500">{formErrors.dob}</p>
                                    )}
                                </div>

                                <div>
                                    <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                                        Giới tính
                                    </label>
                                    <CustomSelect
                                        id="candidate-gender"
                                        aria-label="Giới tính"
                                        value={formData.gender || 'OTHER'}
                                        onChange={(gender) => setFormData({ ...formData, gender })}
                                        options={[
                                            { value: 'NAM', label: 'Nam' },
                                            { value: 'NU', label: 'Nữ' },
                                            { value: 'OTHER', label: 'Khác' },
                                        ]}
                                    />
                                </div>

                                <div>
                                    <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                                        Tỉnh / Thành phố
                                    </label>
                                    <input
                                        type="text"
                                        value={formData.city || ''}
                                        onChange={(e) => setFormData({ ...formData, city: e.target.value })}
                                        className="w-full rounded-xl border border-slate-200 px-3.5 py-2 text-sm text-slate-800 shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                                        placeholder="VD: TP. Hồ Chí Minh, Hà Nội..."
                                    />
                                </div>

                                <div>
                                    <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                                        Địa chỉ thường trú
                                    </label>
                                    <input
                                        type="text"
                                        value={formData.address || ''}
                                        onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                                        className="w-full rounded-xl border border-slate-200 px-3.5 py-2 text-sm text-slate-800 shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                                        placeholder="VD: 123 Nguyễn Thị Minh Khai, Quận 1"
                                    />
                                </div>
                            </div>
                        </div>

                        {/* Professional Info Group */}
                        <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
                            <h2 className="text-base font-bold text-slate-900 mb-5 flex items-center gap-2">
                                <Briefcase className="h-5 w-5 text-blue-700" /> Thông tin nghề nghiệp
                            </h2>
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
                                <div>
                                    <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                                        Chức danh chuyên môn (Title)
                                    </label>
                                    <input
                                        type="text"
                                        value={formData.title || ''}
                                        onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                                        className="w-full rounded-xl border border-slate-200 px-3.5 py-2 text-sm text-slate-800 shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                                        placeholder="VD: Senior Frontend Developer"
                                    />
                                </div>

                                <div className="rounded-xl border border-blue-100 bg-blue-50/60 p-3.5">
                                    <p className="text-xs font-semibold text-blue-950">Kinh nghiệm hiện tại: {experienceDisplayLabel}</p>
                                    <p className="mt-1 text-xs text-blue-800">Thêm hoặc cập nhật các mốc công việc ở phần ngay bên dưới; hệ thống sẽ tự cộng thời gian, không tính trùng giai đoạn.</p>
                                </div>

                                <div>
                                    <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                                        Mức lương kỳ vọng (VND/tháng)
                                    </label>
                                    <input
                                        type="number"
                                        min="0"
                                        value={formData.expectedSalary ?? ''}
                                        onChange={(e) =>
                                            setFormData({
                                                ...formData,
                                                expectedSalary: e.target.value ? Number(e.target.value) : undefined,
                                            })
                                        }
                                        className="w-full rounded-xl border border-slate-200 px-3.5 py-2 text-sm text-slate-800 shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                                        placeholder="VD: 30000000"
                                    />
                                </div>

                                <div className="sm:col-span-2">
                                    <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                                        Giới thiệu bản thân / Tóm tắt kỹ năng
                                    </label>
                                    <textarea
                                        rows={4}
                                        value={formData.summary || ''}
                                        onChange={(e) => setFormData({ ...formData, summary: e.target.value })}
                                        className="w-full rounded-xl border border-slate-200 p-3.5 text-sm text-slate-800 shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                                        placeholder="Mô tả tóm tắt kinh nghiệm làm việc, thế mạnh công nghệ và mục tiêu nghề nghiệp của bạn..."
                                    />
                                </div>
                            </div>
                        </div>

                        <section id="candidate-experiences" className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm sm:p-8">
                            <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                                <div>
                                    <h2 className="flex items-center gap-2 text-base font-bold text-slate-900">
                                        <Building2 aria-hidden="true" className="size-5 text-blue-700" /> Kinh nghiệm làm việc
                                    </h2>
                                    <p className="mt-1 text-sm text-slate-600">
                                        Hệ thống tự cộng các giai đoạn không trùng nhau để tính tổng số năm kinh nghiệm.
                                    </p>
                                </div>
                                <div className="flex shrink-0 items-center gap-3">
                                    <span className="rounded-full bg-blue-50 px-3 py-1.5 text-sm font-semibold text-blue-800">
                                        {experienceDisplayLabel}
                                    </span>
                                    <button
                                        className="inline-flex items-center gap-1.5 rounded-xl bg-blue-700 px-3.5 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-800"
                                        onClick={() => { setEditingExp(null); setIsExpModalOpen(true) }}
                                        type="button"
                                    >
                                        <Plus aria-hidden="true" className="size-4" /> Thêm
                                    </button>
                                </div>
                            </div>

                            {experiences.length === 0 ? (
                                <div className="mt-5 rounded-2xl border border-dashed border-slate-300 bg-slate-50 p-5 text-center">
                                    <p className="text-sm font-medium text-slate-700">Chưa có mốc công việc nào.</p>
                                    <p className="mt-1 text-sm text-slate-500">Thêm công ty, vị trí và thời gian làm việc để hệ thống tính kinh nghiệm cho hồ sơ.</p>
                                </div>
                            ) : (
                                <ul className="mt-5 divide-y divide-slate-100 rounded-2xl border border-slate-200">
                                    {experiences.map((experience) => (
                                        <li className="flex flex-col gap-3 p-4 sm:flex-row sm:items-start sm:justify-between" key={experience.id}>
                                            <div className="min-w-0">
                                                <p className="break-words font-semibold text-slate-900">{experience.position}</p>
                                                <p className="mt-0.5 break-words text-sm text-slate-600">{experience.companyName}</p>
                                                <p className="mt-1 text-sm text-slate-500">
                                                    {formatIsoDate(experience.startDate)} – {experience.isCurrent ? 'Hiện tại' : formatIsoDate(experience.endDate)}
                                                </p>
                                            </div>
                                            <div className="flex shrink-0 items-center gap-2">
                                                <button
                                                    className="rounded-lg border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-700 hover:bg-slate-50"
                                                    onClick={() => { setEditingExp(experience); setIsExpModalOpen(true) }}
                                                    type="button"
                                                >
                                                    Chỉnh sửa
                                                </button>
                                                <button
                                                    className="rounded-lg border border-rose-200 px-3 py-1.5 text-xs font-semibold text-rose-700 hover:bg-rose-50"
                                                    onClick={() => void handleDeleteExp(experience.id)}
                                                    type="button"
                                                >
                                                    Xóa
                                                </button>
                                            </div>
                                        </li>
                                    ))}
                                </ul>
                            )}
                        </section>

                        {/* Social & Portfolio Links */}
                        <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
                            <h2 className="text-base font-bold text-slate-900 mb-5 flex items-center gap-2">
                                <Globe className="h-5 w-5 text-emerald-600" /> Mạng xã hội & Portfolio
                            </h2>
                            <div className="grid grid-cols-1 sm:grid-cols-3 gap-5">
                                <div>
                                    <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                                        Website cá nhân
                                    </label>
                                    <input
                                        type="url"
                                        value={formData.personalWebsite || ''}
                                        onChange={(e) => setFormData({ ...formData, personalWebsite: e.target.value })}
                                        className="w-full rounded-xl border border-slate-200 px-3.5 py-2 text-sm text-slate-800 shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                                        placeholder="https://myportfolio.io"
                                    />
                                </div>

                                <div>
                                    <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                                        GitHub Profile
                                    </label>
                                    <input
                                        type="url"
                                        value={formData.githubUrl || ''}
                                        onChange={(e) => setFormData({ ...formData, githubUrl: e.target.value })}
                                        className="w-full rounded-xl border border-slate-200 px-3.5 py-2 text-sm text-slate-800 shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                                        placeholder="https://github.com/myusername"
                                    />
                                </div>

                                <div>
                                    <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                                        LinkedIn Profile
                                    </label>
                                    <input
                                        type="url"
                                        value={formData.linkedinUrl || ''}
                                        onChange={(e) => setFormData({ ...formData, linkedinUrl: e.target.value })}
                                        className="w-full rounded-xl border border-slate-200 px-3.5 py-2 text-sm text-slate-800 shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                                        placeholder="https://linkedin.com/in/myusername"
                                    />
                                </div>
                            </div>
                        </div>
                    </form>
                ) : (
                    /* VIEW MODE */
                    <div className="space-y-5">
                        {/* Summary Block */}
                        <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
                            <h2 className="text-base font-bold text-slate-900 mb-4 flex items-center gap-2">
                                <Sparkles className="h-5 w-5 text-amber-500" /> Tóm tắt giới thiệu
                            </h2>
                            <p className="text-sm leading-relaxed text-slate-700 whitespace-pre-line">
                                {profile.summary || (
                                    <span className="italic text-slate-400">
                                        Chưa có thông tin tóm tắt bản thân. Hãy bấm &quot;Chỉnh sửa hồ sơ&quot; để bổ sung thế mạnh của bạn.
                                    </span>
                                )}
                            </p>
                        </div>

                        {/* Details Grid */}
                        <div className="grid grid-cols-1 gap-4 xl:grid-cols-2">
                            {/* Personal & Contact */}
                            <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm space-y-4">
                                <h3 className="text-sm font-bold text-slate-900 flex items-center gap-2">
                                    <User className="h-4 w-4 text-blue-600" /> Thông tin cơ bản
                                </h3>
                                <div className="space-y-3 text-sm">
                                    <div className="flex justify-between border-b border-slate-100 pb-2">
                                        <span className="text-slate-500">Giới tính:</span>
                                        <span className="font-semibold text-slate-800">
                                            {profile.gender === 'NAM' ? 'Nam' : profile.gender === 'NU' ? 'Nữ' : 'Khác'}
                                        </span>
                                    </div>
                                    <div className="flex justify-between border-b border-slate-100 pb-2">
                                        <span className="text-slate-500">Ngày sinh:</span>
                                        <span className="font-semibold text-slate-800">
                                            {profile.dob ? (
                                                <span className="flex items-center gap-1">
                                                    <Calendar className="h-3.5 w-3.5 text-slate-400" />
                                                    {formatIsoDate(profile.dob)}
                                                </span>
                                            ) : (
                                                'Chưa cập nhật'
                                            )}
                                        </span>
                                    </div>
                                    <div className="flex justify-between border-b border-slate-100 pb-2">
                                        <span className="text-slate-500">Tỉnh / Thành phố:</span>
                                        <span className="font-semibold text-slate-800">{profile.city || 'Chưa cập nhật'}</span>
                                    </div>
                                    <div className="flex justify-between">
                                        <span className="text-slate-500">Địa chỉ:</span>
                                        <span className="font-semibold text-slate-800 text-right">{profile.address || 'Chưa cập nhật'}</span>
                                    </div>
                                </div>
                            </div>

                            {/* Career & Salary */}
                            <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm space-y-4">
                                <h3 className="text-sm font-bold text-slate-900 flex items-center gap-2">
                                    <Briefcase className="h-4 w-4 text-blue-700" /> Kỳ vọng công việc & Lương
                                </h3>
                                <div className="space-y-3 text-sm">
                                    <div className="flex justify-between border-b border-slate-100 pb-2">
                                        <span className="text-slate-500">Kinh nghiệm làm việc:</span>
                                        <span className="font-semibold text-slate-800">
                                            {experienceDisplayLabel}
                                        </span>
                                    </div>
                                    <div className="flex justify-between border-b border-slate-100 pb-2">
                                        <span className="text-slate-500">Mức lương kỳ vọng:</span>
                                        <span className="font-bold text-emerald-600">{formatCurrency(profile.expectedSalary)}</span>
                                    </div>
                                    <div className="flex justify-between">
                                        <span className="text-slate-500">Trạng thái tìm việc:</span>
                                        <span className="inline-flex items-center gap-1 font-semibold text-blue-600">
                                            <span className="h-2 w-2 rounded-full bg-blue-600" /> Sẵn sàng nhận lời mời
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* Uploaded Resumes (TopCV CV Files) */}
                        <ResumeUploadSection
                            resumes={resumes}
                            onRefresh={refetchResumes}
                        />

                        {/* Work Experience Section (TopCV Standard) */}
                        <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
                            <div className="flex items-center justify-between mb-6">
                                <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
                                    <Building2 className="h-5 w-5 text-blue-600" /> Kinh nghiệm làm việc
                                </h3>
                                <button
                                    type="button"
                                    onClick={() => {
                                        setEditingExp(null)
                                        setIsExpModalOpen(true)
                                    }}
                                    className="inline-flex items-center gap-1.5 rounded-xl bg-blue-50 px-3.5 py-1.5 text-xs font-semibold text-blue-700 hover:bg-blue-100 transition"
                                >
                                    <Plus className="h-4 w-4" /> Thêm kinh nghiệm
                                </button>
                            </div>

                            {experiences.length === 0 ? (
                                <div className="rounded-2xl border border-dashed border-slate-200 p-8 text-center">
                                    <EmptyStateIllustration className="w-20 h-auto mx-auto mb-2" />
                                    <p className="text-sm font-medium text-slate-600">Chưa có lịch sử kinh nghiệm làm việc</p>
                                    <p className="text-xs text-slate-400 mt-1">
                                        Thêm kinh nghiệm giúp nhà tuyển dụng đánh giá năng lực và tự động tính số năm kinh nghiệm của bạn.
                                    </p>
                                    <button
                                        type="button"
                                        onClick={() => {
                                            setEditingExp(null)
                                            setIsExpModalOpen(true)
                                        }}
                                        className="mt-4 inline-flex items-center gap-1 rounded-xl bg-blue-600 px-4 py-2 text-xs font-semibold text-white hover:bg-blue-700 transition shadow-sm"
                                    >
                                        <Plus className="h-3.5 w-3.5" /> Thêm ngay
                                    </button>
                                </div>
                            ) : (
                                <div className="space-y-4">
                                    {experiences.map((exp: WorkExperience) => (
                                        <div
                                            key={exp.id}
                                            className="relative rounded-2xl border border-slate-200 bg-slate-50/50 p-5 hover:border-slate-300 transition"
                                        >
                                            <div className="flex items-start justify-between gap-4">
                                                <div className="space-y-1">
                                                    <div className="flex flex-wrap items-center gap-2">
                                                        <h4 className="text-base font-bold text-slate-900">{exp.position}</h4>
                                                        {exp.isCurrent ? (
                                                            <span className="rounded-full bg-emerald-50 px-2.5 py-0.5 text-xs font-medium text-emerald-700 ring-1 ring-inset ring-emerald-600/20">
                                                                Hiện tại
                                                            </span>
                                                        ) : (
                                                            <span className="rounded-full bg-slate-100 px-2.5 py-0.5 text-xs font-medium text-slate-600">
                                                                Đã kết thúc
                                                            </span>
                                                        )}
                                                    </div>
                                                    <p className="text-sm font-semibold text-blue-600 flex items-center gap-1.5">
                                                        <Building2 className="h-4 w-4" /> {exp.companyName}
                                                    </p>
                                                    <p className="text-xs text-slate-500 flex items-center gap-1">
                                                        <Calendar className="h-3.5 w-3.5 text-slate-400" />
                                                        {formatIsoDate(exp.startDate)} - {exp.isCurrent ? 'Hiện tại' : (formatIsoDate(exp.endDate) || 'Chưa ghi')}
                                                    </p>
                                                </div>

                                                <div className="flex items-center gap-1">
                                                    <button
                                                        type="button"
                                                        onClick={() => {
                                                            setEditingExp(exp)
                                                            setIsExpModalOpen(true)
                                                        }}
                                                        className="rounded-lg p-1.5 text-slate-400 hover:bg-white hover:text-blue-600 hover:shadow-sm transition"
                                                        title="Chỉnh sửa"
                                                    >
                                                        <Edit3 className="h-4 w-4" />
                                                    </button>
                                                    <button
                                                        type="button"
                                                        onClick={() => handleDeleteExp(exp.id)}
                                                        className="rounded-lg p-1.5 text-slate-400 hover:bg-white hover:text-rose-600 hover:shadow-sm transition"
                                                        title="Xóa"
                                                    >
                                                        <Trash2 className="h-4 w-4" />
                                                    </button>
                                                </div>
                                            </div>

                                            {exp.description && (
                                                <div className="mt-3 text-xs leading-relaxed text-slate-600 whitespace-pre-line border-t border-slate-200/60 pt-3">
                                                    {exp.description}
                                                </div>
                                            )}

                                            {exp.achievements && (
                                                <div className="mt-2.5 flex items-start gap-2 rounded-xl bg-amber-50/70 p-3 text-xs text-amber-900 border border-amber-100">
                                                    <Award className="h-4 w-4 shrink-0 text-amber-600 mt-0.5" />
                                                    <div>
                                                        <span className="font-semibold text-amber-800">Thành tích nổi bật: </span>
                                                        <span className="whitespace-pre-line">{exp.achievements}</span>
                                                    </div>
                                                </div>
                                            )}
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>

                        {/* Professional Skills Section (TopCV Standard) */}
                        <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
                            <div className="flex items-center justify-between mb-6">
                                <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
                                    <Award className="h-5 w-5 text-blue-700" /> Kỹ năng chuyên môn
                                </h3>
                                <button
                                    type="button"
                                    onClick={() => setIsSkillModalOpen(true)}
                                    className="inline-flex items-center gap-1.5 rounded-xl bg-blue-50 px-3.5 py-1.5 text-xs font-semibold text-blue-800 hover:bg-blue-100 transition"
                                >
                                    <Plus className="h-4 w-4" /> Thêm kỹ năng
                                </button>
                            </div>

                            {hasSkillExperienceExceeding && (
                                <p className="mb-3 rounded-xl border border-amber-200 bg-amber-50 p-3 text-xs font-medium text-amber-800">
                                    Số năm ở một hoặc nhiều kỹ năng đang cao hơn tổng kinh nghiệm từ lịch sử công việc. Hãy kiểm tra lại từng kỹ năng.
                                </p>
                            )}
                            {skills.length === 0 ? (
                                <div className="rounded-2xl border border-dashed border-slate-200 p-8 text-center">
                                    <EmptyStateIllustration className="w-20 h-auto mx-auto mb-2" />
                                    <p className="text-sm font-medium text-slate-600">Chưa có kỹ năng nào được cập nhật</p>
                                    <p className="text-xs text-slate-400 mt-1">
                                        Làm nổi bật hồ sơ của bạn với danh sách kỹ năng chuyên môn và đánh giá mức độ thành thạo (1-5 sao).
                                    </p>
                                    <button
                                        type="button"
                                        onClick={() => setIsSkillModalOpen(true)}
                                        className="mt-4 inline-flex items-center gap-1 rounded-xl bg-blue-700 px-4 py-2 text-xs font-semibold text-white hover:bg-blue-800 transition shadow-sm"
                                    >
                                        <Plus className="h-3.5 w-3.5" /> Thêm kỹ năng ngay
                                    </button>
                                </div>
                            ) : (
                                <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3">
                                    {skills.map((s: CandidateSkill) => (
                                        <div
                                            key={s.id}
                                            className="group relative flex flex-col justify-between rounded-2xl border border-slate-200 bg-slate-50/40 p-4 hover:border-blue-300 hover:bg-white hover:shadow-sm transition"
                                        >
                                            <div className="flex items-start justify-between gap-2">
                                                <div>
                                                    <h4 className="text-sm font-bold text-slate-900">{s.skillName}</h4>
                                                    <div className="flex items-center gap-2 mt-1">
                                                        <span className="rounded-md bg-blue-50 px-2 py-0.5 text-[11px] font-semibold text-blue-800">
                                                            {s.proficiencyLevel === 'BEGINNER'
                                                                ? 'Cơ bản'
                                                                : s.proficiencyLevel === 'ELEMENTARY'
                                                                ? 'Sơ cấp'
                                                                : s.proficiencyLevel === 'INTERMEDIATE'
                                                                ? 'Trung cấp'
                                                                : s.proficiencyLevel === 'ADVANCED'
                                                                ? 'Cao cấp'
                                                                : 'Chuyên gia'}
                                                        </span>
                                                        {s.yearsOfExperience != null && s.yearsOfExperience > 0 && (
                                                            <span className="text-[11px] text-slate-400 font-medium">
                                                                {s.yearsOfExperience} năm kn
                                                            </span>
                                                        )}
                                                    </div>
                                                </div>

                                                <button
                                                    type="button"
                                                    onClick={() => handleDeleteSkill(s.id)}
                                                    className="opacity-0 group-hover:opacity-100 rounded-lg p-1 text-slate-400 hover:bg-rose-50 hover:text-rose-600 transition"
                                                    title="Xóa kỹ năng"
                                                >
                                                    <Trash2 className="h-3.5 w-3.5" />
                                                </button>
                                            </div>

                                            {/* Golden star rating 1-5 */}
                                            <div className="flex items-center gap-1 mt-3 pt-2.5 border-t border-slate-200/60">
                                                {[1, 2, 3, 4, 5].map((star) => (
                                                    <Star
                                                        key={star}
                                                        className={`h-3.5 w-3.5 ${
                                                            star <= (s.rating || 0)
                                                                ? 'fill-amber-400 text-amber-400'
                                                                : 'fill-slate-200 text-slate-200'
                                                        }`}
                                                    />
                                                ))}
                                                <span className="text-xs font-bold text-amber-600 ml-1.5">
                                                    {s.rating || 0}/5
                                                </span>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>

                        {/* Social & Portfolio Links Card */}
                        <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
                            <h3 className="text-sm font-bold text-slate-900 mb-4 flex items-center gap-2">
                                <Globe className="h-4 w-4 text-emerald-600" /> Liên kết nghề nghiệp
                            </h3>
                            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                                {profile.personalWebsite ? (
                                    <a
                                        href={profile.personalWebsite}
                                        target="_blank"
                                        rel="noreferrer"
                                        className="flex items-center justify-between rounded-2xl border border-slate-200 p-4 hover:border-blue-400 hover:bg-blue-50/50 transition group"
                                    >
                                        <div className="flex items-center gap-3">
                                            <Globe className="h-5 w-5 text-blue-600" />
                                            <div>
                                                <p className="text-xs text-slate-400 font-medium">Website</p>
                                                <p className="text-xs font-semibold text-slate-700 truncate max-w-[140px]">{profile.personalWebsite}</p>
                                            </div>
                                        </div>
                                        <ExternalLink className="h-4 w-4 text-slate-400 group-hover:text-blue-600" />
                                    </a>
                                ) : (
                                    <div className="rounded-2xl border border-dashed border-slate-200 p-4 text-center text-xs text-slate-400">
                                        Chưa có Website
                                    </div>
                                )}

                                {profile.githubUrl ? (
                                    <a
                                        href={profile.githubUrl}
                                        target="_blank"
                                        rel="noreferrer"
                                        className="flex items-center justify-between rounded-2xl border border-slate-200 p-4 hover:border-slate-800 hover:bg-slate-50 transition group"
                                    >
                                        <div className="flex items-center gap-3">
                                            <Code2 className="h-5 w-5 text-slate-900" />
                                            <div>
                                                <p className="text-xs text-slate-400 font-medium">GitHub</p>
                                                <p className="text-xs font-semibold text-slate-700 truncate max-w-[140px]">{profile.githubUrl}</p>
                                            </div>
                                        </div>
                                        <ExternalLink className="h-4 w-4 text-slate-400 group-hover:text-slate-900" />
                                    </a>
                                ) : (
                                    <div className="rounded-2xl border border-dashed border-slate-200 p-4 text-center text-xs text-slate-400">
                                        Chưa có GitHub
                                    </div>
                                )}

                                {profile.linkedinUrl ? (
                                    <a
                                        href={profile.linkedinUrl}
                                        target="_blank"
                                        rel="noreferrer"
                                        className="flex items-center justify-between rounded-2xl border border-slate-200 p-4 hover:border-sky-600 hover:bg-sky-50/50 transition group"
                                    >
                                        <div className="flex items-center gap-3">
                                            <Share2 className="h-5 w-5 text-sky-600" />
                                            <div>
                                                <p className="text-xs text-slate-400 font-medium">LinkedIn</p>
                                                <p className="text-xs font-semibold text-slate-700 truncate max-w-[140px]">{profile.linkedinUrl}</p>
                                            </div>
                                        </div>
                                        <ExternalLink className="h-4 w-4 text-slate-400 group-hover:text-sky-600" />
                                    </a>
                                ) : (
                                    <div className="rounded-2xl border border-dashed border-slate-200 p-4 text-center text-xs text-slate-400">
                                        Chưa có LinkedIn
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>
                )}
                    </section>
                </div>

                {/* Modals */}
                <WorkExperienceModal
                    isOpen={isExpModalOpen}
                    onClose={() => {
                        setIsExpModalOpen(false)
                        setEditingExp(null)
                    }}
                    onSuccess={() => {
                        setIsExpModalOpen(false)
                        setEditingExp(null)
                        refetchExperiences()
                        refetch()
                    }}
                    initialData={editingExp}
                />

                <CandidateSkillModal
                    isOpen={isSkillModalOpen}
                    onClose={() => setIsSkillModalOpen(false)}
                    onSuccess={() => {
                        setIsSkillModalOpen(false)
                        refetchSkills()
                    }}
                    availableSkills={masterSkills.filter((ms) => !skills.some((s) => s.skillId === ms.id))}
                />

                {profile && (
                    <CvBuilderModal
                        isOpen={isCvBuilderOpen}
                        onClose={() => setIsCvBuilderOpen(false)}
                        profile={profile}
                        skills={skills}
                        experiences={experiences}
                        onSuccess={() => {
                            refetchResumes()
                            setSuccessMessage('Đã tạo và lưu CV thành công!')
                        }}
                    />
                )}

                <CvImportModal
                    isOpen={isCvImportOpen}
                    onClose={() => setIsCvImportOpen(false)}
                    onSuccess={() => {
                        refetch()
                        refetchSkills()
                        refetchExperiences()
                        setSuccessMessage('Đã đồng bộ thông tin từ CV vào hồ sơ thành công!')
                    }}
                />
            </div>
        </div>
    )
}
