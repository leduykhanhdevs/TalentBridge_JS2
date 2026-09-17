import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
    AlertCircle,
    Briefcase,
    Building2,
    CheckCircle2,
    Clock,
    Image,
    LoaderCircle,
    Mail,
    Phone,
    Save,
    User,
} from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { Link } from 'react-router'
import {
    getRecruiterProfile,
    RecruiterApiError,
    updateRecruiterProfile,
} from '../../features/recruiter/recruiterApi'
import type {
    RecruiterProfile,
    UpdateRecruiterProfileRequest,
} from '../../features/recruiter/recruiterTypes'

type FormValues = {
    fullName: string
    phone: string
    position: string
    avatarUrl: string
}

type FormErrors = Partial<Record<keyof FormValues, string>>

function ProfileEditForm({ profile }: { profile: RecruiterProfile }) {
    const queryClient = useQueryClient()

    const [formValues, setFormValues] = useState<FormValues>(() => ({
        fullName: profile.fullName || '',
        phone: profile.phone || '',
        position: profile.position || '',
        avatarUrl: profile.avatarUrl || '',
    }))
    const [fieldErrors, setFieldErrors] = useState<FormErrors>({})
    const [serverError, setServerError] = useState('')
    const [successMessage, setSuccessMessage] = useState('')

    const updateMutation = useMutation({
        mutationFn: (data: UpdateRecruiterProfileRequest) => updateRecruiterProfile(data),
        onSuccess: (updated) => {
            queryClient.setQueryData(['recruiter-profile'], updated)
            setSuccessMessage('Cập nhật hồ sơ nhà tuyển dụng thành công!')
            setServerError('')
            setFieldErrors({})
        },
        onError: (err) => {
            setSuccessMessage('')
            if (err instanceof RecruiterApiError) {
                setServerError(err.message)
            } else {
                setServerError('Không thể cập nhật hồ sơ. Vui lòng thử lại.')
            }
        },
    })

    function validate(): FormErrors {
        const errors: FormErrors = {}
        const phonePattern = /^\d{10,11}$/

        if (!formValues.fullName.trim()) {
            errors.fullName = 'Họ và tên không được để trống.'
        } else if (formValues.fullName.trim().length > 100) {
            errors.fullName = 'Họ và tên không được vượt quá 100 ký tự.'
        }

        if (formValues.phone.trim() && !phonePattern.test(formValues.phone.trim())) {
            errors.phone = 'Số điện thoại phải gồm 10–11 chữ số.'
        }

        if (formValues.position.trim().length > 100) {
            errors.position = 'Chức danh không được vượt quá 100 ký tự.'
        }

        if (formValues.avatarUrl.trim().length > 500) {
            errors.avatarUrl = 'URL ảnh đại diện không được vượt quá 500 ký tự.'
        }

        return errors
    }

    function handleSubmit(e: FormEvent) {
        e.preventDefault()
        const errors = validate()
        setFieldErrors(errors)
        setServerError('')
        setSuccessMessage('')

        if (Object.keys(errors).length > 0) {
            return
        }

        updateMutation.mutate({
            fullName: formValues.fullName.trim(),
            phone: formValues.phone.trim() || undefined,
            position: formValues.position.trim() || undefined,
            avatarUrl: formValues.avatarUrl.trim() || undefined,
        })
    }

    return (
        <div className="lg:col-span-2 rounded-2xl border border-slate-200 bg-white p-6 sm:p-8 shadow-sm">
            <h2 className="text-lg font-bold text-slate-900 mb-1">Cập nhật thông tin</h2>
            <p className="text-sm text-slate-500 mb-6">
                Thông tin này sẽ hiển thị khi bạn trao đổi với ứng viên và đại diện cho doanh nghiệp.
            </p>

            <form className="space-y-5" noValidate onSubmit={handleSubmit}>
                <div>
                    <label className="mb-2 block text-sm font-medium text-slate-700" htmlFor="hr-email">
                        Địa chỉ Email <span className="text-xs text-slate-400">(Cố định theo tài khoản)</span>
                    </label>
                    <div className="relative">
                        <Mail className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                        <input
                            className="h-11 w-full rounded-xl border border-slate-200 bg-slate-100 pl-11 pr-4 text-slate-500 text-sm outline-none cursor-not-allowed"
                            disabled
                            id="hr-email"
                            type="email"
                            value={profile.email}
                        />
                    </div>
                </div>

                <div>
                    <label className="mb-2 block text-sm font-medium text-slate-700" htmlFor="hr-fullname">
                        Họ và tên đầy đủ <span className="text-red-500">*</span>
                    </label>
                    <div className="relative">
                        <User className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                        <input
                            className={`h-11 w-full rounded-xl border bg-white pl-11 pr-4 text-sm text-slate-900 outline-none transition focus:border-emerald-500 focus:ring-4 focus:ring-emerald-100 ${
                                fieldErrors.fullName ? 'border-red-400 focus:border-red-500' : 'border-slate-300'
                            }`}
                            id="hr-fullname"
                            onChange={(e) => {
                                setFormValues((prev) => ({ ...prev, fullName: e.target.value }))
                                setFieldErrors((prev) => ({ ...prev, fullName: undefined }))
                            }}
                            placeholder="Ví dụ: Nguyễn Phan Minh Hiếu"
                            type="text"
                            value={formValues.fullName}
                        />
                    </div>
                    {fieldErrors.fullName && (
                        <p className="mt-1 text-xs text-red-600">{fieldErrors.fullName}</p>
                    )}
                </div>

                <div className="grid gap-4 sm:grid-cols-2">
                    <div>
                        <label className="mb-2 block text-sm font-medium text-slate-700" htmlFor="hr-phone">
                            Số điện thoại liên hệ
                        </label>
                        <div className="relative">
                            <Phone className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                            <input
                                className={`h-11 w-full rounded-xl border bg-white pl-11 pr-4 text-sm text-slate-900 outline-none transition focus:border-emerald-500 focus:ring-4 focus:ring-emerald-100 ${
                                    fieldErrors.phone ? 'border-red-400 focus:border-red-500' : 'border-slate-300'
                                }`}
                                id="hr-phone"
                                onChange={(e) => {
                                    setFormValues((prev) => ({ ...prev, phone: e.target.value }))
                                    setFieldErrors((prev) => ({ ...prev, phone: undefined }))
                                }}
                                placeholder="Ví dụ: 0901234567"
                                type="tel"
                                value={formValues.phone}
                            />
                        </div>
                        {fieldErrors.phone && (
                            <p className="mt-1 text-xs text-red-600">{fieldErrors.phone}</p>
                        )}
                    </div>

                    <div>
                        <label className="mb-2 block text-sm font-medium text-slate-700" htmlFor="hr-position">
                            Chức danh tuyển dụng
                        </label>
                        <div className="relative">
                            <Briefcase className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                            <input
                                className={`h-11 w-full rounded-xl border bg-white pl-11 pr-4 text-sm text-slate-900 outline-none transition focus:border-emerald-500 focus:ring-4 focus:ring-emerald-100 ${
                                    fieldErrors.position ? 'border-red-400 focus:border-red-500' : 'border-slate-300'
                                }`}
                                id="hr-position"
                                onChange={(e) => {
                                    setFormValues((prev) => ({ ...prev, position: e.target.value }))
                                    setFieldErrors((prev) => ({ ...prev, position: undefined }))
                                }}
                                placeholder="Ví dụ: Senior Talent Acquisition"
                                type="text"
                                value={formValues.position}
                            />
                        </div>
                        {fieldErrors.position && (
                            <p className="mt-1 text-xs text-red-600">{fieldErrors.position}</p>
                        )}
                    </div>
                </div>

                <div>
                    <label className="mb-2 block text-sm font-medium text-slate-700" htmlFor="hr-avatar">
                        Đường dẫn ảnh đại diện (Avatar URL)
                    </label>
                    <div className="relative">
                        <Image className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                        <input
                            className={`h-11 w-full rounded-xl border bg-white pl-11 pr-4 text-sm text-slate-900 outline-none transition focus:border-emerald-500 focus:ring-4 focus:ring-emerald-100 ${
                                fieldErrors.avatarUrl ? 'border-red-400 focus:border-red-500' : 'border-slate-300'
                            }`}
                            id="hr-avatar"
                            onChange={(e) => {
                                setFormValues((prev) => ({ ...prev, avatarUrl: e.target.value }))
                                setFieldErrors((prev) => ({ ...prev, avatarUrl: undefined }))
                            }}
                            placeholder="https://example.com/avatar.jpg"
                            type="url"
                            value={formValues.avatarUrl}
                        />
                    </div>
                    {fieldErrors.avatarUrl && (
                        <p className="mt-1 text-xs text-red-600">{fieldErrors.avatarUrl}</p>
                    )}
                </div>

                {serverError && (
                    <div className="flex gap-2 rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700">
                        <AlertCircle className="shrink-0 mt-0.5" size={18} />
                        <span>{serverError}</span>
                    </div>
                )}

                {successMessage && (
                    <div className="flex gap-2 rounded-xl border border-emerald-200 bg-emerald-50 p-3 text-sm text-emerald-700">
                        <CheckCircle2 className="shrink-0 mt-0.5" size={18} />
                        <span>{successMessage}</span>
                    </div>
                )}

                <div className="flex justify-end pt-2">
                    <button
                        className="inline-flex items-center gap-2 rounded-xl bg-emerald-600 px-5 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-emerald-700 disabled:opacity-70 disabled:cursor-wait"
                        disabled={updateMutation.isPending}
                        type="submit"
                    >
                        {updateMutation.isPending ? (
                            <LoaderCircle className="animate-spin" size={16} />
                        ) : (
                            <Save size={16} />
                        )}
                        <span>{updateMutation.isPending ? 'Đang lưu...' : 'Lưu thay đổi'}</span>
                    </button>
                </div>
            </form>
        </div>
    )
}

export function RecruiterProfilePage() {
    const {
        data: profile,
        isLoading,
        error: loadError,
    } = useQuery({
        queryKey: ['recruiter-profile'],
        queryFn: getRecruiterProfile,
    })

    if (isLoading) {
        return (
            <div className="flex h-64 items-center justify-center">
                <LoaderCircle className="animate-spin text-emerald-600" size={32} />
            </div>
        )
    }

    if (loadError) {
        return (
            <div className="rounded-xl border border-red-200 bg-red-50 p-6 text-center text-red-700">
                <AlertCircle className="mx-auto mb-2" size={24} />
                <p className="font-semibold">Không thể tải thông tin hồ sơ nhà tuyển dụng.</p>
                <p className="mt-1 text-sm">{(loadError as Error).message}</p>
            </div>
        )
    }

    return (
        <div className="space-y-6">
            {/* Page Title */}
            <div>
                <h1 className="text-2xl font-bold text-slate-900">Hồ sơ Nhà tuyển dụng</h1>
                <p className="text-sm text-slate-500">
                    Xem và quản lý thông tin đại diện tuyển dụng của bạn trên TalentBridge.
                </p>
            </div>

            {/* Company Link Alert if not attached */}
            {!profile?.companyId && (
                <div className="flex flex-col gap-3 rounded-2xl border border-amber-200 bg-amber-50 p-5 sm:flex-row sm:items-center sm:justify-between">
                    <div className="flex items-start gap-3">
                        <AlertCircle className="mt-0.5 shrink-0 text-amber-600" size={20} />
                        <div>
                            <p className="font-semibold text-amber-900">
                                Bạn chưa liên kết với doanh nghiệp nào
                            </p>
                            <p className="text-sm text-amber-700">
                                Để đăng tin tuyển dụng và quản lý ứng viên, vui lòng tạo mới hoặc gửi yêu cầu gia nhập một công ty đã được phê duyệt.
                            </p>
                        </div>
                    </div>
                    <div className="flex gap-2">
                        <Link
                            className="inline-flex items-center gap-1 rounded-xl bg-amber-600 px-3.5 py-2 text-xs font-semibold text-white transition hover:bg-amber-700"
                            to="/recruiter/company"
                        >
                            <Building2 size={14} />
                            <span>Tạo công ty</span>
                        </Link>
                        <Link
                            className="inline-flex items-center gap-1 rounded-xl border border-amber-300 bg-white px-3.5 py-2 text-xs font-semibold text-amber-800 transition hover:bg-amber-100"
                            to="/recruiter/join-company"
                        >
                            <span>Gia nhập công ty</span>
                        </Link>
                    </div>
                </div>
            )}

            <div className="grid gap-6 lg:grid-cols-3">
                {/* Profile Overview Card */}
                <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
                    <div className="flex flex-col items-center text-center">
                        <div className="relative mb-4">
                            {profile?.avatarUrl ? (
                                <img
                                    alt={profile.fullName}
                                    className="size-24 rounded-full border-2 border-emerald-500 object-cover shadow-sm"
                                    src={profile.avatarUrl}
                                />
                            ) : (
                                <div className="grid size-24 place-items-center rounded-full bg-emerald-100 text-emerald-700 font-bold text-2xl border-2 border-emerald-300">
                                    {profile?.fullName ? profile.fullName.charAt(0).toUpperCase() : 'HR'}
                                </div>
                            )}
                            <span
                                className={`absolute bottom-0 right-0 size-4 rounded-full border-2 border-white ${
                                    profile?.status === 'ACTIVE' ? 'bg-emerald-500' : 'bg-red-500'
                                }`}
                                title={`Tài khoản: ${profile?.status}`}
                            />
                        </div>

                        <h2 className="text-lg font-bold text-slate-900">{profile?.fullName}</h2>
                        <p className="text-sm text-emerald-700 font-medium">{profile?.position || 'Chuyên viên tuyển dụng'}</p>

                        <div className="mt-4 w-full border-t border-slate-100 pt-4 text-left space-y-3">
                            <div className="flex items-center gap-2 text-xs text-slate-600">
                                <Mail className="text-slate-400 shrink-0" size={15} />
                                <span className="truncate">{profile?.email}</span>
                            </div>

                            <div className="flex items-center gap-2 text-xs text-slate-600">
                                <Phone className="text-slate-400 shrink-0" size={15} />
                                <span>{profile?.phone || 'Chưa cập nhật SĐT'}</span>
                            </div>

                            <div className="flex items-center gap-2 text-xs text-slate-600">
                                <Building2 className="text-slate-400 shrink-0" size={15} />
                                <span className="truncate font-medium text-slate-900">
                                    {profile?.companyName || 'Chưa có công ty'}
                                </span>
                            </div>

                            {profile?.companyStatus && (
                                <div className="flex items-center gap-2 text-xs">
                                    <span className="text-slate-400">Trạng thái DN:</span>
                                    <span
                                        className={`rounded px-2 py-0.5 font-medium ${
                                            profile.companyStatus === 'APPROVED'
                                                ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                                                : profile.companyStatus === 'PENDING'
                                                ? 'bg-amber-50 text-amber-700 border border-amber-200'
                                                : 'bg-red-50 text-red-700 border border-red-200'
                                        }`}
                                    >
                                        {profile.companyStatus === 'APPROVED'
                                            ? 'Đã phê duyệt'
                                            : profile.companyStatus === 'PENDING'
                                            ? 'Chờ duyệt'
                                            : 'Đã từ chối'}
                                    </span>
                                </div>
                            )}

                            {profile?.createdAt && (
                                <div className="flex items-center gap-2 text-[11px] text-slate-400">
                                    <Clock size={13} />
                                    <span>Gia nhập: {new Date(profile.createdAt).toLocaleDateString('vi-VN')}</span>
                                </div>
                            )}
                        </div>
                    </div>
                </div>

                {/* Edit Profile Form */}
                {profile && <ProfileEditForm key={profile.id} profile={profile} />}
            </div>
        </div>
    )
}
