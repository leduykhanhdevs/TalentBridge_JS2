import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
    AlertCircle,
    ArrowRight,
    Building2,
    CheckCircle2,
    Clock,
    FileText,
    Globe,
    Hash,
    LoaderCircle,
    MapPin,
    PlusCircle,
    Send,
    Users,
} from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { Link } from 'react-router'
import {
    getRecruiterProfile,
    RecruiterApiError,
    requestCreateCompany,
} from '../../features/recruiter/recruiterApi'
import type { RequestCreateCompanyRequest } from '../../features/recruiter/recruiterTypes'

type CompanyFormValues = {
    name: string
    taxCode: string
    website: string
    companySize: string
    address: string
    city: string
    description: string
    logoUrl: string
}

type FormErrors = Partial<Record<keyof CompanyFormValues, string>>

const initialFormValues: CompanyFormValues = {
    name: '',
    taxCode: '',
    website: '',
    companySize: '50-100 nhân viên',
    address: '',
    city: '',
    description: '',
    logoUrl: '',
}

export function RecruiterCompanyPage() {
    const queryClient = useQueryClient()

    const {
        data: profile,
        isLoading,
        error: profileError,
    } = useQuery({
        queryKey: ['recruiter-profile'],
        queryFn: getRecruiterProfile,
    })

    const [formValues, setFormValues] = useState<CompanyFormValues>(initialFormValues)
    const [fieldErrors, setFieldErrors] = useState<FormErrors>({})
    const [serverError, setServerError] = useState('')
    const [successMessage, setSuccessMessage] = useState('')
    const [showCreateForm, setShowCreateForm] = useState(false)

    const createMutation = useMutation({
        mutationFn: (data: RequestCreateCompanyRequest) => requestCreateCompany(data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['recruiter-profile'] })
            setSuccessMessage(
                'Yêu cầu tạo doanh nghiệp đã được gửi thành công và đang chờ Quản trị viên (Admin) xét duyệt!',
            )
            setServerError('')
            setShowCreateForm(false)
        },
        onError: (err) => {
            setSuccessMessage('')
            if (err instanceof RecruiterApiError) {
                setServerError(err.message)
            } else {
                setServerError('Không thể gửi yêu cầu tạo doanh nghiệp. Vui lòng thử lại.')
            }
        },
    })

    function validate(): FormErrors {
        const errors: FormErrors = {}

        if (!formValues.name.trim()) {
            errors.name = 'Tên công ty không được để trống.'
        } else if (formValues.name.trim().length > 200) {
            errors.name = 'Tên công ty không được vượt quá 200 ký tự.'
        }

        if (formValues.taxCode.trim().length > 50) {
            errors.taxCode = 'Mã số thuế không được vượt quá 50 ký tự.'
        }

        if (formValues.website.trim().length > 255) {
            errors.website = 'Website không được vượt quá 255 ký tự.'
        }

        if (formValues.address.trim().length > 255) {
            errors.address = 'Địa chỉ không được vượt quá 255 ký tự.'
        }

        if (formValues.city.trim().length > 100) {
            errors.city = 'Thành phố không được vượt quá 100 ký tự.'
        }

        if (formValues.logoUrl.trim().length > 500) {
            errors.logoUrl = 'URL logo không được vượt quá 500 ký tự.'
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

        createMutation.mutate({
            name: formValues.name.trim(),
            taxCode: formValues.taxCode.trim() || undefined,
            website: formValues.website.trim() || undefined,
            companySize: formValues.companySize.trim() || undefined,
            address: formValues.address.trim() || undefined,
            city: formValues.city.trim() || undefined,
            description: formValues.description.trim() || undefined,
            logoUrl: formValues.logoUrl.trim() || undefined,
        })
    }

    function handleLogoFileChange(e: React.ChangeEvent<HTMLInputElement>) {
        const file = e.target.files?.[0]
        if (file) {
            const reader = new FileReader()
            reader.onloadend = () => {
                if (typeof reader.result === 'string') {
                    setFormValues((prev) => ({ ...prev, logoUrl: reader.result as string }))
                    setFieldErrors((prev) => ({ ...prev, logoUrl: undefined }))
                }
            }
            reader.readAsDataURL(file)
        }
    }

    if (isLoading) {
        return (
            <div className="flex h-64 items-center justify-center">
                <LoaderCircle className="animate-spin text-emerald-600" size={32} />
            </div>
        )
    }

    if (profileError) {
        return (
            <div className="rounded-xl border border-red-200 bg-red-50 p-6 text-center text-red-700">
                <AlertCircle className="mx-auto mb-2" size={24} />
                <p className="font-semibold">Không thể tải thông tin doanh nghiệp.</p>
                <p className="mt-1 text-sm">{(profileError as Error).message}</p>
            </div>
        )
    }

    const hasCompany = Boolean(profile?.companyId)
    const isApproved = profile?.companyStatus === 'APPROVED'
    const isPending = profile?.companyStatus === 'PENDING'
    const isRejected = profile?.companyStatus === 'REJECTED'

    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-bold text-slate-900">Doanh nghiệp của tôi</h1>
                <p className="text-sm text-slate-500">
                    Quản lý thông tin tổ chức, kiểm tra trạng thái phê duyệt và liên kết nhân sự HR.
                </p>
            </div>

            {successMessage && (
                <div className="flex gap-2 rounded-xl border border-emerald-200 bg-emerald-50 p-4 text-sm text-emerald-800">
                    <CheckCircle2 className="shrink-0 mt-0.5 text-emerald-600" size={18} />
                    <span>{successMessage}</span>
                </div>
            )}

            {/* Case 1: Recruiter has an APPROVED or PENDING company */}
            {hasCompany && (
                <div className="space-y-6">
                    {/* Status Banner */}
                    {isPending && (
                        <div className="flex items-start gap-3 rounded-2xl border border-amber-200 bg-amber-50 p-5">
                            <Clock className="mt-0.5 shrink-0 text-amber-600" size={22} />
                            <div>
                                <h3 className="font-bold text-amber-900">
                                    Hồ sơ công ty đang chờ Ban quản trị phê duyệt
                                </h3>
                                <p className="mt-1 text-sm text-amber-700">
                                    Hồ sơ đăng ký doanh nghiệp của bạn đang trong hàng đợi phê duyệt. Sau khi Ban quản trị kích hoạt, bạn sẽ có toàn quyền đăng tin tuyển dụng và phê duyệt các HR khác vào công ty.
                                </p>
                            </div>
                        </div>
                    )}

                    {isRejected && (
                        <div className="flex items-start gap-3 rounded-2xl border border-red-200 bg-red-50 p-5">
                            <AlertCircle className="mt-0.5 shrink-0 text-red-600" size={22} />
                            <div>
                                <h3 className="font-bold text-red-900">
                                    Yêu cầu tạo doanh nghiệp đã bị từ chối
                                </h3>
                                <p className="mt-1 text-sm text-red-700">
                                    Hồ sơ không đáp ứng điều kiện xác thực của ban quản trị. Bạn có thể liên hệ hỗ trợ hoặc gửi lại yêu cầu mới.
                                </p>
                            </div>
                        </div>
                    )}

                    {/* Company Card */}
                    <div className="rounded-2xl border border-slate-200 bg-white p-6 sm:p-8 shadow-sm">
                        <div className="flex flex-col gap-6 sm:flex-row sm:items-center sm:justify-between border-b border-slate-100 pb-6">
                            <div className="flex items-center gap-4">
                                {profile?.companyLogoUrl ? (
                                    <img
                                        alt={profile.companyName || 'Company Logo'}
                                        className="size-20 rounded-2xl border border-slate-200 object-cover shadow-sm"
                                        src={profile.companyLogoUrl}
                                    />
                                ) : (
                                    <div className="grid size-20 place-items-center rounded-2xl bg-emerald-100 text-emerald-700 font-bold text-3xl border border-emerald-200">
                                        <Building2 size={36} />
                                    </div>
                                )}
                                <div>
                                    <div className="flex items-center gap-3">
                                        <h2 className="text-xl font-bold text-slate-900">
                                            {profile?.companyName}
                                        </h2>
                                        <span
                                            className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                                                isApproved
                                                    ? 'bg-emerald-100 text-emerald-800'
                                                    : isPending
                                                    ? 'bg-amber-100 text-amber-800'
                                                    : 'bg-red-100 text-red-800'
                                            }`}
                                        >
                                            {isApproved
                                                ? 'ĐÃ PHÊ DUYỆT'
                                                : isPending
                                                ? 'CHỜ DUYỆT'
                                                : 'TỪ CHỐI'}
                                        </span>
                                    </div>
                                    <p className="mt-1 text-sm text-slate-500">
                                        Mã định danh công ty: #{profile?.companyId}
                                    </p>
                                </div>
                            </div>

                            {isApproved && (
                                <div className="flex gap-2">
                                    <Link
                                        className="inline-flex items-center gap-1.5 rounded-xl bg-emerald-600 px-4 py-2 text-xs font-semibold text-white shadow-sm hover:bg-emerald-700"
                                        to="/recruiter/peer-approval"
                                    >
                                        <Users size={15} />
                                        <span>Duyệt thành viên ({profile?.companyName})</span>
                                    </Link>
                                </div>
                            )}
                        </div>

                        {/* Quick Stats / Info Grid */}
                        <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
                            <div className="rounded-xl border border-slate-100 bg-slate-50 p-4">
                                <span className="text-xs font-medium text-slate-500">Trạng thái xác thực</span>
                                <p className="mt-1 text-sm font-bold text-slate-900">
                                    {isApproved ? 'Hoạt động hợp lệ' : isPending ? 'Chờ kiểm duyệt' : 'Không hợp lệ'}
                                </p>
                            </div>

                            <div className="rounded-xl border border-slate-100 bg-slate-50 p-4">
                                <span className="text-xs font-medium text-slate-500">Chức vụ của bạn</span>
                                <p className="mt-1 text-sm font-bold text-slate-900 truncate">
                                    {profile?.position || 'Thành viên HR'}
                                </p>
                            </div>

                            <div className="rounded-xl border border-slate-100 bg-slate-50 p-4">
                                <span className="text-xs font-medium text-slate-500">Quyền hạn</span>
                                <p className="mt-1 text-sm font-bold text-emerald-700">
                                    {isApproved ? 'Đại diện tuyển dụng' : 'Chờ cấp quyền'}
                                </p>
                            </div>

                            <div className="rounded-xl border border-slate-100 bg-slate-50 p-4">
                                <span className="text-xs font-medium text-slate-500">Đơn vị chủ quản</span>
                                <p className="mt-1 text-sm font-bold text-slate-900 truncate">
                                    {profile?.companyName}
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* Case 2: Recruiter has NO company */}
            {!hasCompany && !showCreateForm && (
                <div className="grid gap-6 md:grid-cols-2">
                    {/* Option 1: Request Create New Company */}
                    <div className="flex flex-col justify-between rounded-2xl border border-slate-200 bg-white p-6 sm:p-8 shadow-sm hover:border-emerald-300 transition">
                        <div>
                            <div className="grid size-12 place-items-center rounded-xl bg-emerald-100 text-emerald-700 mb-4">
                                <PlusCircle size={24} />
                            </div>
                            <h2 className="text-lg font-bold text-slate-900">
                                Tạo mới Doanh nghiệp
                            </h2>
                            <p className="mt-2 text-sm leading-relaxed text-slate-600">
                                Doanh nghiệp của bạn chưa có mặt trên TalentBridge? Hãy điền thông tin để gửi yêu cầu phê duyệt thành lập tổ chức đến Quản trị viên (Admin).
                            </p>
                            <ul className="mt-4 space-y-2 text-xs text-slate-500">
                                <li className="flex items-center gap-2">
                                    <CheckCircle2 className="text-emerald-600" size={14} />
                                    <span>Tự do quản trị thương hiệu tuyển dụng của công ty</span>
                                </li>
                                <li className="flex items-center gap-2">
                                    <CheckCircle2 className="text-emerald-600" size={14} />
                                    <span>Trở thành HR phụ trách chính duyệt thành viên khác</span>
                                </li>
                            </ul>
                        </div>

                        <button
                            className="mt-6 inline-flex items-center justify-center gap-2 rounded-xl bg-emerald-600 px-4 py-2.5 text-sm font-semibold text-white shadow transition hover:bg-emerald-700"
                            onClick={() => setShowCreateForm(true)}
                            type="button"
                        >
                            <span>Bắt đầu tạo công ty</span>
                            <ArrowRight size={16} />
                        </button>
                    </div>

                    {/* Option 2: Search & Join Existing Company */}
                    <div className="flex flex-col justify-between rounded-2xl border border-slate-200 bg-white p-6 sm:p-8 shadow-sm hover:border-indigo-300 transition">
                        <div>
                            <div className="grid size-12 place-items-center rounded-xl bg-indigo-100 text-indigo-700 mb-4">
                                <Users size={24} />
                            </div>
                            <h2 className="text-lg font-bold text-slate-900">
                                Gia nhập Doanh nghiệp có sẵn
                            </h2>
                            <p className="mt-2 text-sm leading-relaxed text-slate-600">
                                Công ty của bạn đã có tài khoản trên hệ thống? Hãy tìm kiếm tên công ty và gửi yêu cầu gia nhập để HR đồng nghiệp xét duyệt.
                            </p>
                            <ul className="mt-4 space-y-2 text-xs text-slate-500">
                                <li className="flex items-center gap-2">
                                    <CheckCircle2 className="text-indigo-600" size={14} />
                                    <span>Tham gia ngay không cần chờ Admin xác thực giấy tờ</span>
                                </li>
                                <li className="flex items-center gap-2">
                                    <CheckCircle2 className="text-indigo-600" size={14} />
                                    <span>Chia sẻ quyền đăng tin và theo dõi ứng viên cùng đồng nghiệp</span>
                                </li>
                            </ul>
                        </div>

                        <Link
                            className="mt-6 inline-flex items-center justify-center gap-2 rounded-xl border border-indigo-200 bg-indigo-50 px-4 py-2.5 text-sm font-semibold text-indigo-700 transition hover:bg-indigo-100"
                            to="/recruiter/join-company"
                        >
                            <span>Tìm kiếm & nộp đơn</span>
                            <ArrowRight size={16} />
                        </Link>
                    </div>
                </div>
            )}

            {/* Create Company Form */}
            {!hasCompany && showCreateForm && (
                <div className="rounded-2xl border border-slate-200 bg-white p-6 sm:p-8 shadow-sm">
                    <div className="flex items-center justify-between border-b border-slate-100 pb-4 mb-6">
                        <div>
                            <h2 className="text-lg font-bold text-slate-900">
                                Yêu cầu thành lập Doanh nghiệp
                            </h2>
                            <p className="text-sm text-slate-500">
                                Sau khi gửi, hồ sơ sẽ được Admin xét duyệt trước khi kích hoạt.
                            </p>
                        </div>
                        <button
                            className="text-xs text-slate-500 hover:text-slate-700 font-medium"
                            onClick={() => setShowCreateForm(false)}
                            type="button"
                        >
                            Hủy bỏ
                        </button>
                    </div>

                    <form className="space-y-5" noValidate onSubmit={handleSubmit}>
                        <div>
                            <label className="mb-1 block text-sm font-medium text-slate-700" htmlFor="comp-name">
                                Tên công ty đầy đủ <span className="text-red-500">*</span>
                            </label>
                            <div className="relative">
                                <Building2 className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                                <input
                                    className={`h-11 w-full rounded-xl border bg-white pl-11 pr-4 text-sm text-slate-900 outline-none transition focus:border-emerald-500 focus:ring-4 focus:ring-emerald-100 ${
                                        fieldErrors.name ? 'border-red-400 focus:border-red-500' : 'border-slate-300'
                                    }`}
                                    id="comp-name"
                                    onChange={(e) => {
                                        setFormValues((prev) => ({ ...prev, name: e.target.value }))
                                        setFieldErrors((prev) => ({ ...prev, name: undefined }))
                                    }}
                                    placeholder="Ví dụ: Công ty Cổ phần Công nghệ FPT Software"
                                    type="text"
                                    value={formValues.name}
                                />
                            </div>
                            {fieldErrors.name && (
                                <p className="mt-1 text-xs text-red-600">{fieldErrors.name}</p>
                            )}
                        </div>

                        <div className="grid gap-4 sm:grid-cols-2">
                            <div>
                                <label className="mb-1 block text-sm font-medium text-slate-700" htmlFor="comp-tax">
                                    Mã số thuế
                                </label>
                                <div className="relative">
                                    <Hash className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                                    <input
                                        className="h-11 w-full rounded-xl border border-slate-300 bg-white pl-11 pr-4 text-sm text-slate-900 outline-none transition focus:border-emerald-500 focus:ring-4 focus:ring-emerald-100"
                                        id="comp-tax"
                                        onChange={(e) => setFormValues((prev) => ({ ...prev, taxCode: e.target.value }))}
                                        placeholder="0101234567"
                                        type="text"
                                        value={formValues.taxCode}
                                    />
                                </div>
                            </div>

                            <div>
                                <label className="mb-1 block text-sm font-medium text-slate-700" htmlFor="comp-size">
                                    Quy mô công ty
                                </label>
                                <select
                                    className="h-11 w-full rounded-xl border border-slate-300 bg-white px-3 text-sm text-slate-900 outline-none transition focus:border-emerald-500 focus:ring-4 focus:ring-emerald-100"
                                    id="comp-size"
                                    onChange={(e) => setFormValues((prev) => ({ ...prev, companySize: e.target.value }))}
                                    value={formValues.companySize}
                                >
                                    <option value="1-10 nhân viên">1-10 nhân viên</option>
                                    <option value="10-50 nhân viên">10-50 nhân viên</option>
                                    <option value="50-100 nhân viên">50-100 nhân viên</option>
                                    <option value="100-500 nhân viên">100-500 nhân viên</option>
                                    <option value="500-1000 nhân viên">500-1000 nhân viên</option>
                                    <option value="1000+ nhân viên">1000+ nhân viên</option>
                                </select>
                            </div>
                        </div>

                        <div className="grid gap-4 sm:grid-cols-2">
                            <div>
                                <label className="mb-1 block text-sm font-medium text-slate-700" htmlFor="comp-city">
                                    Thành phố
                                </label>
                                <div className="relative">
                                    <MapPin className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                                    <input
                                        className="h-11 w-full rounded-xl border border-slate-300 bg-white pl-11 pr-4 text-sm text-slate-900 outline-none transition focus:border-emerald-500 focus:ring-4 focus:ring-emerald-100"
                                        id="comp-city"
                                        onChange={(e) => setFormValues((prev) => ({ ...prev, city: e.target.value }))}
                                        placeholder="Hồ Chí Minh, Hà Nội, Đà Nẵng..."
                                        type="text"
                                        value={formValues.city}
                                    />
                                </div>
                            </div>

                            <div>
                                <label className="mb-1 block text-sm font-medium text-slate-700" htmlFor="comp-website">
                                    Website công ty
                                </label>
                                <div className="relative">
                                    <Globe className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                                    <input
                                        className="h-11 w-full rounded-xl border border-slate-300 bg-white pl-11 pr-4 text-sm text-slate-900 outline-none transition focus:border-emerald-500 focus:ring-4 focus:ring-emerald-100"
                                        id="comp-website"
                                        onChange={(e) => setFormValues((prev) => ({ ...prev, website: e.target.value }))}
                                        placeholder="https://fptsoftware.com"
                                        type="url"
                                        value={formValues.website}
                                    />
                                </div>
                            </div>
                        </div>

                        <div>
                            <label className="mb-1 block text-sm font-medium text-slate-700" htmlFor="comp-address">
                                Địa chỉ trụ sở
                            </label>
                            <input
                                className="h-11 w-full rounded-xl border border-slate-300 bg-white px-4 text-sm text-slate-900 outline-none transition focus:border-emerald-500 focus:ring-4 focus:ring-emerald-100"
                                id="comp-address"
                                onChange={(e) => setFormValues((prev) => ({ ...prev, address: e.target.value }))}
                                placeholder="Tòa nhà FPT, Khu công nghệ cao, TP. Thủ Đức"
                                type="text"
                                value={formValues.address}
                            />
                        </div>

                        <div>
                            <label className="mb-1 block text-sm font-medium text-slate-700" htmlFor="comp-logo">
                                Logo công ty (Upload ảnh hoặc nhập URL)
                            </label>
                            <div className="space-y-3">
                                <div className="flex flex-col sm:flex-row gap-3">
                                    <input
                                        className="h-11 flex-1 rounded-xl border border-slate-300 bg-white px-4 text-sm text-slate-900 outline-none transition focus:border-emerald-500 focus:ring-4 focus:ring-emerald-100"
                                        id="comp-logo"
                                        onChange={(e) => setFormValues((prev) => ({ ...prev, logoUrl: e.target.value }))}
                                        placeholder="Nhập link ảnh (https://...) hoặc bấm nút tải ảnh bên cạnh"
                                        type="url"
                                        value={formValues.logoUrl}
                                    />
                                    <label className="inline-flex items-center justify-center gap-2 cursor-pointer h-11 px-4 rounded-xl border border-dashed border-emerald-500 bg-emerald-50 hover:bg-emerald-100 text-emerald-700 text-xs font-semibold transition shrink-0">
                                        <span>Tải ảnh lên</span>
                                        <input
                                            accept="image/*"
                                            className="hidden"
                                            onChange={handleLogoFileChange}
                                            type="file"
                                        />
                                    </label>
                                </div>

                                {formValues.logoUrl && (
                                    <div className="flex items-center gap-3 rounded-xl border border-slate-200 bg-slate-50 p-3">
                                        <span className="text-xs font-medium text-slate-500">Xem trước logo:</span>
                                        <img
                                            alt="Logo preview"
                                            className="size-14 rounded-lg border border-slate-200 bg-white object-contain p-1 shadow-sm"
                                            onError={(e) => {
                                                ;(e.target as HTMLElement).style.display = 'none'
                                            }}
                                            src={formValues.logoUrl}
                                        />
                                        <button
                                            className="ml-auto text-xs font-medium text-red-600 hover:text-red-700"
                                            onClick={() => setFormValues((prev) => ({ ...prev, logoUrl: '' }))}
                                            type="button"
                                        >
                                            Xóa logo
                                        </button>
                                    </div>
                                )}
                            </div>
                        </div>

                        <div>
                            <label className="mb-1 block text-sm font-medium text-slate-700" htmlFor="comp-desc">
                                Giới thiệu tóm tắt về công ty
                            </label>
                            <div className="relative">
                                <FileText className="pointer-events-none absolute left-3.5 top-3 text-slate-400" size={18} />
                                <textarea
                                    className="w-full rounded-xl border border-slate-300 bg-white pl-11 pr-4 pt-2.5 text-sm text-slate-900 outline-none transition focus:border-emerald-500 focus:ring-4 focus:ring-emerald-100"
                                    id="comp-desc"
                                    onChange={(e) => setFormValues((prev) => ({ ...prev, description: e.target.value }))}
                                    placeholder="Mô tả ngành nghề kinh doanh, văn hóa công ty và chế độ đãi ngộ..."
                                    rows={3}
                                    value={formValues.description}
                                />
                            </div>
                        </div>

                        {serverError && (
                            <div className="flex gap-2 rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700">
                                <AlertCircle className="shrink-0 mt-0.5" size={18} />
                                <span>{serverError}</span>
                            </div>
                        )}

                        <div className="flex items-center justify-end gap-3 pt-2">
                            <button
                                className="rounded-xl border border-slate-300 px-4 py-2.5 text-sm font-semibold text-slate-700 hover:bg-slate-50"
                                onClick={() => setShowCreateForm(false)}
                                type="button"
                            >
                                Hủy
                            </button>

                            <button
                                className="inline-flex items-center gap-2 rounded-xl bg-emerald-600 px-5 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-emerald-700 disabled:opacity-70 disabled:cursor-wait"
                                disabled={createMutation.isPending}
                                type="submit"
                            >
                                {createMutation.isPending ? (
                                    <LoaderCircle className="animate-spin" size={16} />
                                ) : (
                                    <Send size={16} />
                                )}
                                <span>{createMutation.isPending ? 'Đang gửi yêu cầu...' : 'Gửi yêu cầu xét duyệt'}</span>
                            </button>
                        </div>
                    </form>
                </div>
            )}
        </div>
    )
}
