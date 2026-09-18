import { useMutation, useQuery } from '@tanstack/react-query'
import {
    AlertCircle,
    Building2,
    CheckCircle2,
    Globe,
    LoaderCircle,
    MapPin,
    PlusCircle,
    Search,
    Send,
    UserPlus,
    Users,
    X,
} from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { Link } from 'react-router'
import {
    getRecruiterProfile,
    RecruiterApiError,
    searchApprovedCompanies,
    submitJoinCompanyRequest,
} from '../../features/recruiter/recruiterApi'
import type {
    CompanyResponse,
    SubmitJoinCompanyRequest,
} from '../../features/recruiter/recruiterTypes'

export function RecruiterJoinCompanyPage() {
    const [keyword, setKeyword] = useState('')
    const [searchTerm, setSearchTerm] = useState('')
    const [currentPage, setCurrentPage] = useState(1)
    const pageSize = 9

    const [selectedCompany, setSelectedCompany] = useState<CompanyResponse | null>(null)
    const [position, setPosition] = useState('')
    const [message, setMessage] = useState('')
    const [formError, setFormError] = useState('')
    const [serverError, setServerError] = useState('')
    const [successMessage, setSuccessMessage] = useState('')
    const [showAutocomplete, setShowAutocomplete] = useState(false)
    const [hasPendingRequest, setHasPendingRequest] = useState<boolean>(() => {
        try {
            return localStorage.getItem('tb_recruiter_pending_join') === 'true'
        } catch {
            return false
        }
    })

    const { data: profile } = useQuery({
        queryKey: ['recruiter-profile'],
        queryFn: getRecruiterProfile,
    })

    const {
        data: companyPage,
        isLoading: isSearching,
        error: searchError,
        refetch,
    } = useQuery({
        queryKey: ['approved-companies', searchTerm, currentPage, pageSize],
        queryFn: () =>
            searchApprovedCompanies({
                keyword: searchTerm || undefined,
                page: currentPage,
                size: pageSize,
            }),
    })

    const joinMutation = useMutation({
        mutationFn: ({ companyId, data }: { companyId: number; data: SubmitJoinCompanyRequest }) =>
            submitJoinCompanyRequest(companyId, data),
        onSuccess: () => {
            setHasPendingRequest(true)
            try {
                localStorage.setItem('tb_recruiter_pending_join', 'true')
            } catch {
                // ignore
            }
            setSuccessMessage(
                `Đã gửi yêu cầu gia nhập ${selectedCompany?.name} thành công! Đang chờ HR công ty xét duyệt.`,
            )
            setSelectedCompany(null)
            setPosition('')
            setMessage('')
            setServerError('')
            setFormError('')
        },
        onError: (err) => {
            if (err instanceof RecruiterApiError) {
                if (err.status === 409) {
                    setHasPendingRequest(true)
                    try {
                        localStorage.setItem('tb_recruiter_pending_join', 'true')
                    } catch {
                        // ignore
                    }
                }
                setServerError(err.message)
            } else {
                setServerError('Không thể gửi yêu cầu xin gia nhập. Vui lòng thử lại.')
            }
        },
    })

    function handleSearchSubmit(e: FormEvent) {
        e.preventDefault()
        setSearchTerm(keyword.trim())
        setCurrentPage(1)
        setShowAutocomplete(false)
    }

    function openJoinModal(company: CompanyResponse) {
        setSelectedCompany(company)
        setPosition(profile?.position || '')
        setMessage('')
        setFormError('')
        setServerError('')
    }

    function handleModalSubmit(e: FormEvent) {
        e.preventDefault()
        if (!selectedCompany) return

        if (!position.trim()) {
            setFormError('Vui lòng nhập chức danh ứng tuyển vào công ty.')
            return
        }

        joinMutation.mutate({
            companyId: selectedCompany.id,
            data: {
                position: position.trim(),
                message: message.trim() || undefined,
            },
        })
    }

    const hasCompany = Boolean(profile?.companyId)

    return (
        <div className="space-y-6">
            <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <h1 className="text-2xl font-bold text-slate-900">Tìm & Xin gia nhập Doanh nghiệp</h1>
                    <p className="text-sm text-slate-500">
                        Tìm kiếm các doanh nghiệp đã được xác thực trên hệ thống và nộp đơn gia nhập đội ngũ tuyển dụng.
                    </p>
                </div>
                {!hasCompany && (
                    <Link
                        className="inline-flex items-center gap-2 rounded-xl bg-emerald-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-emerald-700 shrink-0"
                        to="/recruiter/company"
                    >
                        <PlusCircle size={18} />
                        <span>Tạo doanh nghiệp mới</span>
                    </Link>
                )}
            </div>

            {/* If recruiter already belongs to an approved company */}
            {hasCompany && (
                <div className="flex items-start gap-3 rounded-2xl border border-blue-200 bg-blue-50 p-5">
                    <Building2 className="mt-0.5 shrink-0 text-blue-600" size={22} />
                    <div className="flex-1">
                        <h3 className="font-bold text-blue-900">
                            Bạn đã liên kết với: {profile?.companyName}
                        </h3>
                        <p className="mt-1 text-sm text-blue-700">
                            Tài khoản HR của bạn hiện tại đã thuộc về một tổ chức trên TalentBridge. Theo quy định, mỗi tài khoản HR chỉ được liên kết với một doanh nghiệp tại một thời điểm.
                        </p>
                        <div className="mt-3">
                            <Link
                                className="inline-flex items-center gap-1.5 rounded-xl bg-blue-600 px-3.5 py-1.5 text-xs font-semibold text-white shadow-sm hover:bg-blue-700"
                                to="/recruiter/company"
                            >
                                <span>Xem trang doanh nghiệp của tôi</span>
                            </Link>
                        </div>
                    </div>
                </div>
            )}

            {successMessage && (
                <div className="flex items-center justify-between rounded-xl border border-emerald-200 bg-emerald-50 p-4 text-sm text-emerald-800">
                    <div className="flex items-center gap-2">
                        <CheckCircle2 className="shrink-0 text-emerald-600" size={18} />
                        <span>{successMessage}</span>
                    </div>
                    <button
                        className="text-xs text-emerald-700 underline"
                        onClick={() => setSuccessMessage('')}
                        type="button"
                    >
                        Đóng
                    </button>
                </div>
            )}

            {hasPendingRequest && (
                <div className="flex items-center gap-3 rounded-2xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-800">
                    <AlertCircle className="shrink-0 text-amber-600" size={20} />
                    <div className="flex-1">
                        <p className="font-semibold">Bạn đang có yêu cầu xin gia nhập đang chờ xét duyệt</p>
                        <p className="text-xs text-amber-700">
                            Hệ thống tạm thời khóa nút gửi yêu cầu gia nhập mới cho đến khi có phản hồi từ Ban quản trị công ty.
                        </p>
                    </div>
                </div>
            )}

            {/* Search Toolbar */}
            <div className="rounded-2xl border border-slate-200 bg-white p-4 sm:p-5 shadow-sm">
                <form className="flex flex-col gap-3 sm:flex-row" onSubmit={handleSearchSubmit}>
                    <div className="relative flex-1">
                        <Search className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                        <input
                            className="h-11 w-full rounded-xl border border-slate-300 bg-white pl-10 pr-4 text-sm text-slate-900 outline-none transition focus:border-emerald-500 focus:ring-4 focus:ring-emerald-100"
                            onBlur={() => setTimeout(() => setShowAutocomplete(false), 200)}
                            onChange={(e) => {
                                setKeyword(e.target.value)
                                setShowAutocomplete(e.target.value.trim().length > 0)
                            }}
                            onFocus={() => {
                                if (keyword.trim().length > 0) setShowAutocomplete(true)
                            }}
                            placeholder="Tìm kiếm công ty dạng Auto-complete / Danh sách thẻ công ty..."
                            type="text"
                            value={keyword}
                        />

                        {/* Auto-complete Dropdown */}
                        {showAutocomplete && companyPage?.content && companyPage.content.length > 0 && (
                            <div className="absolute left-0 right-0 top-full z-20 mt-1 max-h-60 overflow-y-auto rounded-xl border border-slate-200 bg-white p-1.5 shadow-xl">
                                {companyPage.content
                                    .filter((c) =>
                                        c.name.toLowerCase().includes(keyword.toLowerCase()),
                                    )
                                    .slice(0, 5)
                                    .map((company) => (
                                        <button
                                            className="flex w-full items-center gap-3 rounded-lg px-3 py-2 text-left transition hover:bg-emerald-50"
                                            key={company.id}
                                            onClick={() => {
                                                setKeyword(company.name)
                                                setSearchTerm(company.name)
                                                setShowAutocomplete(false)
                                                openJoinModal(company)
                                            }}
                                            type="button"
                                        >
                                            <div className="grid size-8 shrink-0 place-items-center rounded bg-emerald-100 text-xs font-bold text-emerald-700">
                                                {company.logoUrl ? (
                                                    <img alt="" className="size-8 rounded object-contain" src={company.logoUrl} />
                                                ) : (
                                                    <Building2 size={16} />
                                                )}
                                            </div>
                                            <div className="min-w-0 flex-1">
                                                <p className="truncate text-xs font-semibold text-slate-800">{company.name}</p>
                                                <p className="truncate text-[11px] text-slate-500">{company.address || company.city || 'Doanh nghiệp đã duyệt'}</p>
                                            </div>
                                            <span className="shrink-0 rounded bg-emerald-100 px-1.5 py-0.5 text-[10px] font-semibold text-emerald-800">
                                                Xin gia nhập
                                            </span>
                                        </button>
                                    ))}
                            </div>
                        )}
                    </div>

                    <button
                        className="inline-flex h-11 items-center justify-center gap-2 rounded-xl bg-emerald-600 px-5 text-sm font-semibold text-white shadow-sm transition hover:bg-emerald-700"
                        type="submit"
                    >
                        <Search size={16} />
                        <span>Tìm kiếm</span>
                    </button>

                    {searchTerm && (
                        <button
                            className="h-11 rounded-xl border border-slate-300 px-4 text-sm font-medium text-slate-600 hover:bg-slate-50"
                            onClick={() => {
                                setKeyword('')
                                setSearchTerm('')
                                setCurrentPage(1)
                            }}
                            type="button"
                        >
                            Xóa lọc
                        </button>
                    )}

                    {!hasCompany && (
                        <Link
                            className="inline-flex h-11 items-center justify-center gap-1.5 rounded-xl border border-emerald-600 bg-emerald-50 px-4 text-sm font-semibold text-emerald-700 transition hover:bg-emerald-100 shrink-0"
                            to="/recruiter/company"
                        >
                            <PlusCircle size={16} />
                            <span>Tạo doanh nghiệp</span>
                        </Link>
                    )}
                </form>
            </div>

            {/* Companies Grid */}
            {isSearching ? (
                <div className="flex h-64 items-center justify-center">
                    <LoaderCircle className="animate-spin text-emerald-600" size={32} />
                </div>
            ) : searchError ? (
                <div className="rounded-xl border border-red-200 bg-red-50 p-6 text-center text-red-700">
                    <AlertCircle className="mx-auto mb-2" size={24} />
                    <p className="font-semibold">Lỗi khi tải danh sách doanh nghiệp.</p>
                    <p className="mt-1 text-sm">{(searchError as Error).message}</p>
                    <button
                        className="mt-3 rounded-lg bg-red-600 px-3 py-1.5 text-xs font-medium text-white"
                        onClick={() => refetch()}
                        type="button"
                    >
                        Thử lại
                    </button>
                </div>
            ) : companyPage?.content?.length === 0 ? (
                <div className="rounded-2xl border border-slate-200 bg-white p-12 text-center shadow-sm">
                    <Building2 className="mx-auto text-slate-300 mb-3" size={44} />
                    <h3 className="font-bold text-slate-800">Không tìm thấy doanh nghiệp phù hợp</h3>
                    <p className="mt-1 text-sm text-slate-500 max-w-md mx-auto">
                        {searchTerm
                            ? `Không có kết quả nào cho "${searchTerm}". Doanh nghiệp của bạn chưa có trên hệ thống?`
                            : 'Hiện chưa có doanh nghiệp nào phù hợp với tìm kiếm của bạn.'}
                    </p>
                    {!hasCompany && (
                        <div className="mt-5">
                            <Link
                                className="inline-flex items-center gap-2 rounded-xl bg-emerald-600 px-5 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-emerald-700"
                                to="/recruiter/company"
                            >
                                <PlusCircle size={18} />
                                <span>Tạo hồ sơ doanh nghiệp mới ngay</span>
                            </Link>
                        </div>
                    )}
                </div>
            ) : (
                <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
                    {companyPage?.content.map((company) => (
                        <div
                            className="flex flex-col justify-between rounded-2xl border border-slate-200 bg-white p-5 shadow-sm hover:border-emerald-300 transition"
                            key={company.id}
                        >
                            <div>
                                <div className="flex items-start gap-3 mb-3">
                                    {company.logoUrl ? (
                                        <img
                                            alt={company.name}
                                            className="size-14 rounded-xl border border-slate-200 object-cover shrink-0"
                                            src={company.logoUrl}
                                        />
                                    ) : (
                                        <div className="grid size-14 place-items-center rounded-xl bg-emerald-100 text-emerald-700 font-bold shrink-0">
                                            <Building2 size={24} />
                                        </div>
                                    )}
                                    <div className="min-w-0 flex-1">
                                        <h3 className="font-bold text-slate-900 leading-snug line-clamp-2" title={company.name}>
                                            {company.name}
                                        </h3>
                                        <span className="mt-1 inline-block rounded bg-emerald-50 px-2 py-0.5 text-[11px] font-semibold text-emerald-700 border border-emerald-200">
                                            ĐÃ XÁC THỰC
                                        </span>
                                    </div>
                                </div>

                                <div className="space-y-1.5 text-xs text-slate-600 border-t border-slate-100 pt-3">
                                    {company.city && (
                                        <div className="flex items-center gap-1.5">
                                            <MapPin className="text-slate-400 shrink-0" size={14} />
                                            <span className="truncate">{company.city}{company.address ? ` - ${company.address}` : ''}</span>
                                        </div>
                                    )}

                                    {company.companySize && (
                                        <div className="flex items-center gap-1.5">
                                            <Users className="text-slate-400 shrink-0" size={14} />
                                            <span>{company.companySize}</span>
                                        </div>
                                    )}

                                    {company.website && (
                                        <div className="flex items-center gap-1.5">
                                            <Globe className="text-slate-400 shrink-0" size={14} />
                                            <a
                                                className="text-emerald-700 hover:underline truncate"
                                                href={company.website.startsWith('http') ? company.website : `https://${company.website}`}
                                                rel="noreferrer"
                                                target="_blank"
                                            >
                                                {company.website}
                                            </a>
                                        </div>
                                    )}

                                    {company.description && (
                                        <p className="mt-2 text-xs text-slate-500 line-clamp-2 leading-relaxed">
                                            {company.description}
                                        </p>
                                    )}
                                </div>
                            </div>

                            <div className="mt-5 pt-3 border-t border-slate-100">
                                <button
                                    className="w-full inline-flex items-center justify-center gap-2 rounded-xl bg-emerald-50 px-4 py-2 text-xs font-semibold text-emerald-700 hover:bg-emerald-600 hover:text-white transition disabled:opacity-50 disabled:cursor-not-allowed"
                                    disabled={hasCompany || hasPendingRequest}
                                    onClick={() => openJoinModal(company)}
                                    title={hasCompany ? 'Bạn đã thuộc về một công ty' : hasPendingRequest ? 'Bạn đang có yêu cầu khác đang chờ duyệt' : 'Gửi đơn xin gia nhập'}
                                    type="button"
                                >
                                    <UserPlus size={15} />
                                    <span>Xin gia nhập</span>
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {/* Pagination */}
            {companyPage && companyPage.totalPages > 1 && (
                <div className="flex items-center justify-between border-t border-slate-200 pt-4">
                    <p className="text-xs text-slate-500">
                        Hiển thị trang {companyPage.pageNumber} / {companyPage.totalPages} ({companyPage.totalElements} doanh nghiệp)
                    </p>
                    <div className="flex gap-2">
                        <button
                            className="rounded-lg border border-slate-300 px-3 py-1.5 text-xs font-medium text-slate-700 hover:bg-slate-50 disabled:opacity-40"
                            disabled={currentPage <= 1}
                            onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
                            type="button"
                        >
                            Trang trước
                        </button>
                        <button
                            className="rounded-lg border border-slate-300 px-3 py-1.5 text-xs font-medium text-slate-700 hover:bg-slate-50 disabled:opacity-40"
                            disabled={companyPage.isLast}
                            onClick={() => setCurrentPage((p) => p + 1)}
                            type="button"
                        >
                            Trang kế
                        </button>
                    </div>
                </div>
            )}

            {/* Join Request Modal Dialog */}
            {selectedCompany && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-sm p-4">
                    <div className="w-full max-w-lg rounded-2xl bg-white p-6 shadow-2xl animate-in fade-in zoom-in-95">
                        <div className="flex items-start justify-between border-b border-slate-100 pb-4 mb-4">
                            <div>
                                <h3 className="font-bold text-slate-900">
                                    Xin gia nhập Doanh nghiệp
                                </h3>
                                <p className="text-xs text-slate-500">
                                    Gửi yêu cầu tới đội ngũ quản trị của <strong className="text-slate-800">{selectedCompany.name}</strong>
                                </p>
                            </div>
                            <button
                                className="rounded-lg p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
                                onClick={() => setSelectedCompany(null)}
                                type="button"
                            >
                                <X size={18} />
                            </button>
                        </div>

                        <form className="space-y-4" onSubmit={handleModalSubmit}>
                            <div>
                                <label className="mb-1 block text-xs font-medium text-slate-700" htmlFor="join-pos">
                                    Chức danh ứng tuyển <span className="text-red-500">*</span>
                                </label>
                                <input
                                    className="h-10 w-full rounded-xl border border-slate-300 bg-white px-3 text-sm text-slate-900 outline-none transition focus:border-emerald-500 focus:ring-4 focus:ring-emerald-100"
                                    id="join-pos"
                                    onChange={(e) => {
                                        setPosition(e.target.value)
                                        setFormError('')
                                    }}
                                    placeholder="Ví dụ: Talent Acquisition Specialist"
                                    type="text"
                                    value={position}
                                />
                                {formError && <p className="mt-1 text-xs text-red-600">{formError}</p>}
                            </div>

                            <div>
                                <label className="mb-1 block text-xs font-medium text-slate-700" htmlFor="join-msg">
                                    Lời nhắn gửi đến HR / Ban quản trị
                                </label>
                                <textarea
                                    className="w-full rounded-xl border border-slate-300 bg-white p-3 text-sm text-slate-900 outline-none transition focus:border-emerald-500 focus:ring-4 focus:ring-emerald-100"
                                    id="join-msg"
                                    onChange={(e) => setMessage(e.target.value)}
                                    placeholder="Giới thiệu nhanh về bản thân hoặc kinh nghiệm tuyển dụng của bạn..."
                                    rows={3}
                                    value={message}
                                />
                            </div>

                            {serverError && (
                                <div className="flex gap-2 rounded-xl border border-red-200 bg-red-50 p-3 text-xs text-red-700">
                                    <AlertCircle className="shrink-0 mt-0.5" size={16} />
                                    <span>{serverError}</span>
                                </div>
                            )}

                            <div className="flex items-center justify-end gap-3 pt-2">
                                <button
                                    className="rounded-xl border border-slate-300 px-4 py-2 text-xs font-semibold text-slate-700 hover:bg-slate-50"
                                    onClick={() => setSelectedCompany(null)}
                                    type="button"
                                >
                                    Hủy bỏ
                                </button>
                                <button
                                    className="inline-flex items-center gap-1.5 rounded-xl bg-emerald-600 px-4 py-2 text-xs font-semibold text-white shadow transition hover:bg-emerald-700 disabled:opacity-50 disabled:cursor-not-allowed"
                                    disabled={joinMutation.isPending || hasPendingRequest || hasCompany}
                                    title={hasPendingRequest ? 'Bạn đã có yêu cầu khác đang chờ duyệt' : undefined}
                                    type="submit"
                                >
                                    {joinMutation.isPending ? (
                                        <LoaderCircle className="animate-spin" size={14} />
                                    ) : (
                                        <Send size={14} />
                                    )}
                                    <span>{joinMutation.isPending ? 'Đang gửi...' : 'Gửi yêu cầu'}</span>
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    )
}
