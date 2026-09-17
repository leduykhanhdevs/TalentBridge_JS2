import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
    AlertCircle,
    Briefcase,
    Calendar,
    CheckCircle2,
    Code2,
    Edit3,
    ExternalLink,
    Globe,
    Mail,
    MapPin,
    Phone,
    RefreshCw,
    Save,
    Share2,
    Sparkles,
    User,
    X,
} from 'lucide-react'
import { getCandidateProfile, updateCandidateProfile } from '../../features/candidate/candidateApi'
import type { UpdateCandidateProfileRequest } from '../../features/candidate/candidateTypes'
import { validateCandidateProfile } from '../../features/candidate/candidateValidation'

export function CandidateProfilePage() {
    const queryClient = useQueryClient()
    const [isEditing, setIsEditing] = useState(false)
    const [successMessage, setSuccessMessage] = useState<string | null>(null)
    const [errorMessage, setErrorMessage] = useState<string | null>(null)

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

    const [formData, setFormData] = useState<UpdateCandidateProfileRequest>({})
    const [formErrors, setFormErrors] = useState<Record<string, string>>({})

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
            experienceYears: profile.experienceYears ?? 0,
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
            experienceYears: formData.experienceYears,
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

    const formatCurrency = (val: number | null | undefined) => {
        if (!val || val <= 0) return 'Thỏa thuận'
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
    }

    return (
        <div className="min-h-screen bg-slate-50 py-8 px-4 sm:px-6 lg:px-8">
            <div className="mx-auto max-w-5xl space-y-6">
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

                {/* Profile Hero Card */}
                <div className="relative overflow-hidden rounded-3xl border border-slate-200 bg-white p-6 sm:p-8 shadow-sm">
                    <div className="absolute top-0 right-0 h-40 w-40 translate-x-8 -translate-y-8 rounded-full bg-gradient-to-br from-blue-400/10 to-indigo-500/20 blur-2xl pointer-events-none" />

                    <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-6">
                        <div className="flex items-center gap-5">
                            <div className="relative flex h-20 w-20 shrink-0 items-center justify-center rounded-2xl bg-gradient-to-br from-blue-600 to-indigo-700 text-2xl font-bold text-white shadow-md">
                                {profile.avatarUrl ? (
                                    <img
                                        src={profile.avatarUrl}
                                        alt={profile.fullName}
                                        className="h-full w-full rounded-2xl object-cover"
                                    />
                                ) : (
                                    profile.fullName?.charAt(0)?.toUpperCase() || 'U'
                                )}
                                <span className="absolute bottom-1 right-1 h-3.5 w-3.5 rounded-full border-2 border-white bg-emerald-500" />
                            </div>

                            <div>
                                <div className="flex flex-wrap items-center gap-2">
                                    <h1 className="text-2xl font-black tracking-tight text-slate-900">
                                        {profile.fullName || 'Chưa cập nhật tên'}
                                    </h1>
                                    <span className="rounded-full bg-blue-50 px-3 py-1 text-xs font-semibold text-blue-700 ring-1 ring-inset ring-blue-700/10">
                                        Ứng viên
                                    </span>
                                </div>
                                <p className="mt-1 text-sm font-medium text-slate-600 flex items-center gap-1.5">
                                    <Briefcase className="h-4 w-4 text-slate-400" />
                                    {profile.title || 'Chưa có chức danh nghề nghiệp'}
                                </p>
                                <div className="mt-2 flex flex-wrap items-center gap-4 text-xs text-slate-500">
                                    <span className="flex items-center gap-1">
                                        <Mail className="h-3.5 w-3.5 text-slate-400" />
                                        {profile.email}
                                    </span>
                                    {profile.phone && (
                                        <span className="flex items-center gap-1">
                                            <Phone className="h-3.5 w-3.5 text-slate-400" />
                                            {profile.phone}
                                        </span>
                                    )}
                                    {profile.city && (
                                        <span className="flex items-center gap-1">
                                            <MapPin className="h-3.5 w-3.5 text-slate-400" />
                                            {profile.city}
                                        </span>
                                    )}
                                </div>
                            </div>
                        </div>

                        {!isEditing ? (
                            <button
                                onClick={handleStartEditing}
                                className="inline-flex items-center gap-2 rounded-xl bg-blue-600 px-5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-blue-700 transition"
                            >
                                <Edit3 className="h-4 w-4" /> Chỉnh sửa hồ sơ
                            </button>
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

                {/* Main Content Area */}
                {isEditing ? (
                    /* EDIT FORM */
                    <form
                        id="candidate-profile-form"
                        onSubmit={handleSubmit}
                        className="space-y-6"
                    >
                        {/* Personal Info Group */}
                        <div className="rounded-3xl border border-slate-200 bg-white p-6 sm:p-8 shadow-sm">
                            <h2 className="text-base font-bold text-slate-900 mb-5 flex items-center gap-2">
                                <User className="h-5 w-5 text-blue-600" /> Thông tin cá nhân
                            </h2>
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
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
                                    <input
                                        type="date"
                                        value={formData.dob || ''}
                                        onChange={(e) => setFormData({ ...formData, dob: e.target.value })}
                                        className="w-full rounded-xl border border-slate-200 px-3.5 py-2 text-sm text-slate-800 shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                                    />
                                </div>

                                <div>
                                    <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                                        Giới tính
                                    </label>
                                    <select
                                        value={formData.gender || 'OTHER'}
                                        onChange={(e) => setFormData({ ...formData, gender: e.target.value })}
                                        className="w-full rounded-xl border border-slate-200 px-3.5 py-2 text-sm text-slate-800 shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                                    >
                                        <option value="NAM">Nam</option>
                                        <option value="NU">Nữ</option>
                                        <option value="OTHER">Khác</option>
                                    </select>
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
                        <div className="rounded-3xl border border-slate-200 bg-white p-6 sm:p-8 shadow-sm">
                            <h2 className="text-base font-bold text-slate-900 mb-5 flex items-center gap-2">
                                <Briefcase className="h-5 w-5 text-indigo-600" /> Thông tin nghề nghiệp
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

                                <div>
                                    <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                                        Số năm kinh nghiệm
                                    </label>
                                    <input
                                        type="number"
                                        min="0"
                                        value={formData.experienceYears ?? 0}
                                        onChange={(e) =>
                                            setFormData({
                                                ...formData,
                                                experienceYears: parseInt(e.target.value, 10) || 0,
                                            })
                                        }
                                        className={`w-full rounded-xl border px-3.5 py-2 text-sm text-slate-800 shadow-sm focus:outline-none focus:ring-2 ${
                                            formErrors.experienceYears
                                                ? 'border-rose-300 focus:ring-rose-400'
                                                : 'border-slate-200 focus:ring-blue-500'
                                        }`}
                                    />
                                    {formErrors.experienceYears && (
                                        <p className="mt-1 text-xs text-rose-500">{formErrors.experienceYears}</p>
                                    )}
                                </div>

                                <div>
                                    <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                                        Mức lương hiện tại (VND/tháng)
                                    </label>
                                    <input
                                        type="number"
                                        min="0"
                                        value={formData.currentSalary ?? ''}
                                        onChange={(e) =>
                                            setFormData({
                                                ...formData,
                                                currentSalary: e.target.value ? Number(e.target.value) : undefined,
                                            })
                                        }
                                        className="w-full rounded-xl border border-slate-200 px-3.5 py-2 text-sm text-slate-800 shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                                        placeholder="VD: 20000000"
                                    />
                                </div>

                                <div>
                                    <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                                        Mức lương mong muốn (VND/tháng)
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

                        {/* Social & Portfolio Links */}
                        <div className="rounded-3xl border border-slate-200 bg-white p-6 sm:p-8 shadow-sm">
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
                    <div className="space-y-6">
                        {/* Summary Block */}
                        <div className="rounded-3xl border border-slate-200 bg-white p-6 sm:p-8 shadow-sm">
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
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            {/* Personal & Contact */}
                            <div className="rounded-3xl border border-slate-200 bg-white p-6 sm:p-8 shadow-sm space-y-4">
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
                                                    {profile.dob}
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
                            <div className="rounded-3xl border border-slate-200 bg-white p-6 sm:p-8 shadow-sm space-y-4">
                                <h3 className="text-sm font-bold text-slate-900 flex items-center gap-2">
                                    <Briefcase className="h-4 w-4 text-indigo-600" /> Kỳ vọng công việc & Lương
                                </h3>
                                <div className="space-y-3 text-sm">
                                    <div className="flex justify-between border-b border-slate-100 pb-2">
                                        <span className="text-slate-500">Kinh nghiệm làm việc:</span>
                                        <span className="font-semibold text-slate-800">
                                            {profile.experienceYears ? `${profile.experienceYears} năm` : 'Chưa có kinh nghiệm'}
                                        </span>
                                    </div>
                                    <div className="flex justify-between border-b border-slate-100 pb-2">
                                        <span className="text-slate-500">Mức lương hiện tại:</span>
                                        <span className="font-semibold text-slate-800">{formatCurrency(profile.currentSalary)}</span>
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

                        {/* Social & Portfolio Links Card */}
                        <div className="rounded-3xl border border-slate-200 bg-white p-6 sm:p-8 shadow-sm">
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
            </div>
        </div>
    )
}
