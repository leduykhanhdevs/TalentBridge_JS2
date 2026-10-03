import { useState, useRef, useEffect } from 'react'
import {
    Sparkles,
    Check,
    Palette,
    Download,
    FileText,
    Printer,
    Mail,
    Phone,
    MapPin,
    X,
    Loader2
} from 'lucide-react'
import type {
    CandidateProfileResponse,
    CandidateSkill,
    WorkExperience
} from '../candidateTypes'
import { generateResume } from '../candidateApi'

interface CvBuilderModalProps {
    isOpen: boolean
    onClose: () => void
    profile: CandidateProfileResponse
    skills: CandidateSkill[]
    experiences: WorkExperience[]
    onSuccess?: () => void
}

const TEMPLATES: Array<{
    code: string
    name: string
    description: string
    tag: string
}> = [
    {
        code: 'MODERN_IT_01',
        name: 'Modern IT Professional',
        description: 'Bố cục 2 cột năng động, tối ưu cho lập trình viên & kỹ sư phần mềm.',
        tag: 'Khuyên dùng cho IT',
    },
    {
        code: 'CLASSIC_01',
        name: 'Classic Elegant',
        description: 'Bố cục 1 cột truyền thống, trang trọng, phù hợp quản lý & chuyên viên.',
        tag: 'Truyền thống',
    },
    {
        code: 'MINIMALIST_01',
        name: 'Creative Minimalist',
        description: 'Thiết kế tối giản hiện đại với thẻ Bento UI và khoảng trắng tinh tế.',
        tag: 'Tối giản',
    },
]

const COLOR_PALETTE = [
    { name: 'Xanh dương đậm', hex: '#2563EB' },
    { name: 'Xanh lục bảo', hex: '#059669' },
    { name: 'Tím Indigo', hex: '#4F46E5' },
    { name: 'Đen than Slate', hex: '#1F2937' },
    { name: 'Đỏ Ruby', hex: '#DC2626' },
]

export const CvBuilderModal: React.FC<CvBuilderModalProps> = ({
    isOpen,
    onClose,
    profile,
    skills,
    experiences,
    onSuccess,
}) => {
    const [selectedTemplate, setSelectedTemplate] = useState('MODERN_IT_01')
    const [primaryColor, setPrimaryColor] = useState('#2563EB')
    const [title, setTitle] = useState(
        `CV ${profile.fullName || 'Ứng viên'} - ${profile.title || 'Chuyên viên'}`
    )
    const [isSaving, setIsSaving] = useState(false)
    const [mobilePanel, setMobilePanel] = useState<'edit' | 'preview'>('edit')
    const [message, setMessage] = useState<{ text: string; type: 'success' | 'error' } | null>(null)
    const printRef = useRef<HTMLDivElement>(null)

    useEffect(() => {
        if (isOpen) setMobilePanel('edit')
    }, [isOpen])

    if (!isOpen) return null

    const handleSaveToProfile = async () => {
        setIsSaving(true)
        setMessage(null)
        try {
            await generateResume({
                templateCode: selectedTemplate,
                title: title.trim() || 'CV tạo từ Hồ sơ',
                primaryColor,
                customizationJson: JSON.stringify({ primaryColor, templateCode: selectedTemplate }),
            })
            setMessage({ text: 'Tạo CV và lưu vào hồ sơ thành công!', type: 'success' })
            if (onSuccess) onSuccess()
            setTimeout(() => {
                onClose()
            }, 1200)
        } catch (err: unknown) {
            const error = err as Error
            setMessage({ text: error.message || 'Lỗi khi lưu CV', type: 'error' })
        } finally {
            setIsSaving(false)
        }
    }

    const handlePrint = () => {
        const printContent = printRef.current
        if (!printContent) return

        const printWindow = window.open('', '', 'width=900,height=1100')
        if (!printWindow) {
            alert('Trình duyệt đã chặn cửa sổ pop-up. Vui lòng cho phép để in CV.')
            return
        }

        printWindow.document.write(`
            <html>
                <head>
                    <title>${title}</title>
                    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css">
                    <style>
                        @page { size: A4; margin: 12mm; }
                        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; -webkit-print-color-adjust: exact; print-color-adjust: exact; }
                    </style>
                </head>
                <body class="bg-white text-gray-900 p-4">
                    ${printContent.innerHTML}
                </body>
            </html>
        `)
        printWindow.document.close()
        printWindow.focus()
        setTimeout(() => {
            printWindow.print()
            printWindow.close()
        }, 350)
    }

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center overflow-hidden bg-black/60 p-1 backdrop-blur-sm animate-in fade-in duration-200 sm:p-4">
            <div className="flex h-[100dvh] w-full max-w-6xl flex-col overflow-hidden border border-gray-100 bg-white shadow-2xl sm:h-[92vh] sm:rounded-2xl">
                {/* Header */}
                <div className="flex items-center justify-between gap-2 border-b border-gray-200 bg-gray-50/80 px-3 py-3 sm:px-6 sm:py-4">
                    <div className="flex min-w-0 items-center gap-2 sm:gap-3">
                        <div className="hidden h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-blue-600 text-white shadow-sm shadow-blue-500/20 sm:flex">
                            <Sparkles className="h-5 w-5" />
                        </div>
                        <div>
                            <h2 className="text-sm font-bold text-gray-900 sm:text-lg">
                                Trình tạo CV từ Hồ sơ cá nhân (CV Builder)
                            </h2>
                            <p className="hidden text-xs text-gray-500 sm:block">
                                Tự động đồng bộ kinh nghiệm & kỹ năng từ hồ sơ sang các mẫu CV chuyên nghiệp
                            </p>
                        </div>
                    </div>
                    <button
                        aria-label="Đóng trình tạo CV"
                        onClick={onClose}
                        className="rounded-lg p-2 text-gray-400 hover:bg-gray-200 hover:text-gray-700 transition"
                    >
                        <X className="h-5 w-5" />
                    </button>
                </div>

                {/* Content Layout */}
                <div className="grid grid-cols-2 gap-2 border-b border-gray-200 p-2 md:hidden">
                    <button type="button" onClick={() => setMobilePanel('edit')} className={`rounded-lg px-3 py-2 text-xs font-bold ${mobilePanel === 'edit' ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-700'}`}>Chỉnh sửa</button>
                    <button type="button" onClick={() => setMobilePanel('preview')} className={`rounded-lg px-3 py-2 text-xs font-bold ${mobilePanel === 'preview' ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-700'}`}>Xem trước</button>
                </div>

                <div className="flex min-h-0 flex-1 flex-col overflow-hidden md:flex-row">
                    {/* Left Panel: Settings */}
                    <div className={`${mobilePanel === 'edit' ? 'flex' : 'hidden'} min-h-0 w-full flex-shrink-0 flex-col overflow-y-auto border-r border-gray-200 bg-gray-50/50 p-4 space-y-6 sm:p-5 md:flex md:w-80`}>
                        {/* Title input */}
                        <div>
                            <label className="block text-xs font-bold uppercase tracking-wider text-gray-600 mb-1.5">
                                Tiêu đề CV
                            </label>
                            <input
                                type="text"
                                value={title}
                                onChange={(e) => setTitle(e.target.value)}
                                className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                                placeholder="Nhập tên gọi cho CV..."
                            />
                        </div>

                        {/* Template selector */}
                        <div>
                            <label className="block text-xs font-bold uppercase tracking-wider text-gray-600 mb-2">
                                Chọn mẫu giao diện
                            </label>
                            <div className="space-y-2.5">
                                {TEMPLATES.map((tpl) => {
                                    const isSelected = selectedTemplate === tpl.code
                                    return (
                                        <div
                                            key={tpl.code}
                                            onClick={() => setSelectedTemplate(tpl.code)}
                                            className={`cursor-pointer rounded-xl border p-3.5 transition-all ${
                                                isSelected
                                                    ? 'border-blue-600 bg-blue-50/70 shadow-sm ring-1 ring-blue-600'
                                                    : 'border-gray-200 bg-white hover:border-gray-300 hover:bg-gray-50'
                                            }`}
                                        >
                                            <div className="flex items-center justify-between mb-1">
                                                <span className="font-semibold text-sm text-gray-900">
                                                    {tpl.name}
                                                </span>
                                                {isSelected && (
                                                    <span className="flex h-5 w-5 items-center justify-center rounded-full bg-blue-600 text-white">
                                                        <Check className="h-3 w-3" />
                                                    </span>
                                                )}
                                            </div>
                                            <p className="text-xs text-gray-500 leading-relaxed mb-2">
                                                {tpl.description}
                                            </p>
                                            <span className="inline-block rounded-md bg-gray-100 px-2 py-0.5 text-[10px] font-medium text-gray-600">
                                                {tpl.tag}
                                            </span>
                                        </div>
                                    )
                                })}
                            </div>
                        </div>

                        {/* Color Picker */}
                        <div>
                            <label className="flex items-center gap-1.5 text-xs font-bold uppercase tracking-wider text-gray-600 mb-2">
                                <Palette className="h-3.5 w-3.5" /> Màu chủ đạo
                            </label>
                            <div className="flex items-center gap-2.5">
                                {COLOR_PALETTE.map((c) => (
                                    <button
                                        key={c.hex}
                                        type="button"
                                        onClick={() => setPrimaryColor(c.hex)}
                                        title={c.name}
                                        style={{ backgroundColor: c.hex }}
                                        className={`h-7 w-7 rounded-full shadow-sm transition-transform ${
                                            primaryColor === c.hex
                                                ? 'scale-125 ring-2 ring-offset-2 ring-gray-400'
                                                : 'hover:scale-110'
                                        }`}
                                    />
                                ))}
                            </div>
                        </div>

                        {/* Message alert */}
                        {message && (
                            <div
                                className={`rounded-lg p-3 text-xs font-medium ${
                                    message.type === 'success'
                                        ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                                        : 'bg-rose-50 text-rose-700 border border-rose-200'
                                }`}
                            >
                                {message.text}
                            </div>
                        )}
                    </div>

                    {/* Right Panel: Live CV Preview */}
                    <div className={`${mobilePanel === 'preview' ? 'flex' : 'hidden'} min-h-0 min-w-0 flex-1 overflow-auto bg-gray-100/70 p-3 sm:p-6 md:flex md:justify-center`}>
                        <div
                            ref={printRef}
                            className="min-h-[720px] w-full min-w-[320px] max-w-[760px] bg-white p-4 text-gray-800 shadow-xl transition-all sm:min-h-[960px] sm:rounded-lg sm:p-8 border border-gray-200/80"
                        >
                            {/* TEMPLATE 1: MODERN IT */}
                            {selectedTemplate === 'MODERN_IT_01' && (
                                <div className="space-y-6">
                                    {/* Header banner */}
                                    <div
                                        className="flex flex-col gap-4 rounded-xl p-4 text-white sm:flex-row sm:items-center sm:justify-between sm:p-6"
                                        style={{ backgroundColor: primaryColor }}
                                    >
                                        <div>
                                            <h1 className="break-words text-xl font-black uppercase tracking-wide sm:text-2xl">
                                                {profile.fullName || 'Họ và Tên'}
                                            </h1>
                                            <p className="text-sm font-medium opacity-90 mt-0.5">
                                                {profile.title || 'Chuyên viên kỹ thuật'}
                                            </p>
                                        </div>
                                        <div className="space-y-1 text-left text-xs opacity-90 sm:text-right">
                                            {profile.email && <p className="flex min-w-0 items-start gap-1.5 break-all sm:justify-end"><Mail className="mt-0.5 h-3 w-3 shrink-0" /> {profile.email}</p>}
                                            {profile.phone && <p className="flex items-center gap-1.5 sm:justify-end"><Phone className="h-3 w-3 shrink-0" /> {profile.phone}</p>}
                                            {profile.city && <p className="flex items-center gap-1.5 sm:justify-end"><MapPin className="h-3 w-3 shrink-0" /> {profile.city}</p>}
                                        </div>
                                    </div>

                                    {/* 2-column layout */}
                                    <div className="grid grid-cols-1 gap-5 md:grid-cols-12 md:gap-6">
                                        {/* Left sidebar: Summary & Skills */}
                                        <div className="space-y-6 border-b border-gray-100 pb-5 md:col-span-5 md:border-b-0 md:border-r md:pb-0 md:pr-4">
                                            {profile.summary && (
                                                <div>
                                                    <h3
                                                        className="text-xs font-bold uppercase tracking-wider pb-1 mb-2 border-b-2"
                                                        style={{ borderColor: primaryColor, color: primaryColor }}
                                                    >
                                                        Giới thiệu bản thân
                                                    </h3>
                                                    <p className="text-xs text-gray-600 leading-relaxed whitespace-pre-line">
                                                        {profile.summary}
                                                    </p>
                                                </div>
                                            )}

                                            <div>
                                                <h3
                                                    className="text-xs font-bold uppercase tracking-wider pb-1 mb-2 border-b-2"
                                                    style={{ borderColor: primaryColor, color: primaryColor }}
                                                >
                                                    Kỹ năng chuyên môn
                                                </h3>
                                                <div className="flex flex-wrap gap-1.5">
                                                    {skills.length > 0 ? (
                                                        skills.map((s) => (
                                                            <span
                                                                key={s.id}
                                                                className="rounded-md bg-gray-100 px-2 py-1 text-[11px] font-medium text-gray-800"
                                                            >
                                                                {s.skillName}
                                                            </span>
                                                        ))
                                                    ) : (
                                                        <span className="text-xs text-gray-400 italic">Chưa cập nhật kỹ năng</span>
                                                    )}
                                                </div>
                                            </div>

                                            <div>
                                                <h3
                                                    className="text-xs font-bold uppercase tracking-wider pb-1 mb-2 border-b-2"
                                                    style={{ borderColor: primaryColor, color: primaryColor }}
                                                >
                                                    Thông tin liên hệ
                                                </h3>
                                                <div className="text-xs text-gray-600 space-y-1.5">
                                                    {profile.address && <p><strong>Địa chỉ:</strong> {profile.address}</p>}
                                                    {profile.githubUrl && <p><strong>GitHub:</strong> {profile.githubUrl}</p>}
                                                    {profile.linkedinUrl && <p><strong>LinkedIn:</strong> {profile.linkedinUrl}</p>}
                                                </div>
                                            </div>
                                        </div>

                                        {/* Right Column: Work Experience */}
                                        <div className="space-y-4 md:col-span-7">
                                            <h3
                                                className="text-xs font-bold uppercase tracking-wider pb-1 mb-2 border-b-2"
                                                style={{ borderColor: primaryColor, color: primaryColor }}
                                            >
                                                Kinh nghiệm làm việc
                                            </h3>
                                            {experiences.length > 0 ? (
                                                <div className="space-y-4">
                                                    {experiences.map((exp) => (
                                                        <div key={exp.id} className="relative pl-3 border-l-2 border-gray-200">
                                                            <div
                                                                className="absolute -left-[5px] top-1 h-2 w-2 rounded-full"
                                                                style={{ backgroundColor: primaryColor }}
                                                            />
                                                            <h4 className="text-xs font-bold text-gray-900">{exp.position}</h4>
                                                            <p className="text-xs font-medium text-gray-600">{exp.companyName}</p>
                                                            <p className="text-[10px] text-gray-400 mt-0.5">
                                                                {exp.startDate} - {exp.isCurrent ? 'Hiện tại' : (exp.endDate || 'Hiện tại')}
                                                            </p>
                                                            {exp.description && (
                                                                <p className="text-xs text-gray-600 mt-1.5 leading-relaxed whitespace-pre-line">
                                                                    {exp.description}
                                                                </p>
                                                            )}
                                                        </div>
                                                    ))}
                                                </div>
                                            ) : (
                                                <p className="text-xs text-gray-400 italic">Chưa có kinh nghiệm làm việc trong hồ sơ</p>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            )}

                            {/* TEMPLATE 2: CLASSIC ELEGANT */}
                            {selectedTemplate === 'CLASSIC_01' && (
                                <div className="space-y-6 max-w-2xl mx-auto">
                                    {/* Classic Header */}
                                    <div className="text-center border-b pb-4" style={{ borderColor: primaryColor }}>
                                        <h1 className="text-2xl font-serif font-bold text-gray-900 tracking-wide uppercase">
                                            {profile.fullName || 'Họ và Tên'}
                                        </h1>
                                        <p className="text-xs font-medium uppercase tracking-widest text-gray-500 mt-1">
                                            {profile.title || 'Chuyên viên'}
                                        </p>
                                        <div className="mt-2 flex flex-wrap items-center justify-center gap-x-3 gap-y-1 text-xs text-gray-600">
                                            {profile.email && <span>{profile.email}</span>}
                                            {profile.phone && <span>• {profile.phone}</span>}
                                            {profile.city && <span>• {profile.city}</span>}
                                        </div>
                                    </div>

                                    {/* Summary */}
                                    {profile.summary && (
                                        <div>
                                            <h3 className="text-xs font-serif font-bold uppercase tracking-wider text-gray-900 mb-1">
                                                Tóm tắt nghề nghiệp
                                            </h3>
                                            <p className="text-xs text-gray-700 leading-relaxed text-justify">
                                                {profile.summary}
                                            </p>
                                        </div>
                                    )}

                                    {/* Experiences */}
                                    <div>
                                        <h3 className="text-xs font-serif font-bold uppercase tracking-wider text-gray-900 mb-2 border-b pb-1" style={{ borderColor: primaryColor }}>
                                            Quá trình làm việc
                                        </h3>
                                        {experiences.map((exp) => (
                                            <div key={exp.id} className="mb-3.5">
                                                <div className="flex flex-col gap-1 text-xs sm:flex-row sm:items-center sm:justify-between">
                                                    <span className="font-bold text-gray-900">{exp.position} — <span className="font-semibold text-gray-700">{exp.companyName}</span></span>
                                                    <span className="text-[11px] text-gray-500">{exp.startDate} – {exp.isCurrent ? 'Nay' : (exp.endDate || 'Nay')}</span>
                                                </div>
                                                {exp.description && (
                                                    <p className="text-xs text-gray-600 mt-1 leading-relaxed">
                                                        {exp.description}
                                                    </p>
                                                )}
                                            </div>
                                        ))}
                                    </div>

                                    {/* Skills */}
                                    <div>
                                        <h3 className="text-xs font-serif font-bold uppercase tracking-wider text-gray-900 mb-2 border-b pb-1" style={{ borderColor: primaryColor }}>
                                            Kỹ năng & Chuyên môn
                                        </h3>
                                        <p className="text-xs text-gray-700 leading-relaxed">
                                            {skills.map((s) => s.skillName).join(' • ')}
                                        </p>
                                    </div>
                                </div>
                            )}

                            {/* TEMPLATE 3: CREATIVE MINIMALIST */}
                            {selectedTemplate === 'MINIMALIST_01' && (
                                <div className="space-y-6">
                                    <div className="flex flex-col gap-3 border-b pb-5 sm:flex-row sm:items-start sm:justify-between" style={{ borderColor: primaryColor }}>
                                        <div>
                                            <span
                                                className="text-[10px] font-bold uppercase tracking-widest px-2 py-0.5 rounded-full text-white"
                                                style={{ backgroundColor: primaryColor }}
                                            >
                                                CURRICULUM VITAE
                                            </span>
                                            <h1 className="mt-2 break-words text-2xl font-extrabold tracking-tight text-gray-900 sm:text-3xl">
                                                {profile.fullName || 'Họ và Tên'}
                                            </h1>
                                            <p className="text-sm font-medium text-gray-500 mt-0.5">
                                                {profile.title || 'Chuyên viên kỹ thuật'}
                                            </p>
                                        </div>
                                        <div className="space-y-1 break-all text-left text-xs text-gray-600 sm:text-right">
                                            <p>{profile.email}</p>
                                            <p>{profile.phone}</p>
                                            <p>{profile.city}</p>
                                        </div>
                                    </div>

                                    {/* Bento Grid */}
                                    <div className="grid grid-cols-1 gap-3 md:grid-cols-3 md:gap-4">
                                        <div className="rounded-xl border border-gray-100 bg-gray-50 p-4 md:col-span-2">
                                            <h3 className="text-xs font-bold uppercase tracking-wider text-gray-800 mb-2">
                                                Mục tiêu nghề nghiệp
                                            </h3>
                                            <p className="text-xs text-gray-600 leading-relaxed">
                                                {profile.summary || 'Chưa có thông tin mục tiêu nghề nghiệp.'}
                                            </p>
                                        </div>
                                        <div className="rounded-xl border border-gray-100 bg-gray-50 p-4 md:col-span-1">
                                            <h3 className="text-xs font-bold uppercase tracking-wider text-gray-800 mb-2">
                                                Kỹ năng chính
                                            </h3>
                                            <div className="flex flex-wrap gap-1">
                                                {skills.slice(0, 8).map((s) => (
                                                    <span
                                                        key={s.id}
                                                        className="text-[10px] font-semibold rounded bg-white border border-gray-200 px-1.5 py-0.5"
                                                    >
                                                        {s.skillName}
                                                    </span>
                                                ))}
                                            </div>
                                        </div>
                                    </div>

                                    {/* Experience list */}
                                    <div className="rounded-xl border border-gray-100 p-5 bg-white">
                                        <h3
                                            className="text-xs font-bold uppercase tracking-wider mb-3"
                                            style={{ color: primaryColor }}
                                        >
                                            Kinh nghiệm & Dự án
                                        </h3>
                                        <div className="space-y-4">
                                            {experiences.map((exp) => (
                                                <div key={exp.id} className="border-b last:border-0 pb-3 last:pb-0">
                                                    <div className="flex flex-col gap-1 text-xs sm:flex-row sm:items-center sm:justify-between">
                                                        <span className="font-bold text-gray-900">{exp.position}</span>
                                                        <span className="text-[10px] text-gray-400">{exp.startDate} - {exp.isCurrent ? 'Nay' : (exp.endDate || 'Nay')}</span>
                                                    </div>
                                                    <p className="text-xs font-medium text-gray-600">{exp.companyName}</p>
                                                    {exp.description && (
                                                        <p className="text-xs text-gray-500 mt-1">{exp.description}</p>
                                                    )}
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>
                </div>

                {/* Footer Buttons */}
                <div className="flex flex-col gap-3 border-t border-gray-200 bg-white px-3 py-3.5 sm:flex-row sm:items-center sm:justify-between sm:px-6">
                    <div className="flex min-w-0 items-center gap-2 text-xs text-gray-500">
                        <FileText className="h-4 w-4 text-blue-600" />
                        <span className="truncate">Mẫu đang chọn: <strong>{TEMPLATES.find((t) => t.code === selectedTemplate)?.name}</strong></span>
                    </div>
                    <div className="grid grid-cols-1 gap-2 sm:flex sm:items-center sm:gap-3">
                        <button
                            type="button"
                            onClick={handlePrint}
                            className="inline-flex w-full min-w-0 items-center justify-center gap-2 rounded-lg border border-gray-300 bg-white px-3 py-2 text-xs font-medium text-gray-700 shadow-sm transition hover:bg-gray-50 sm:w-auto sm:px-4 sm:text-sm"
                        >
                            <Printer className="h-4 w-4 text-gray-500" />
                            In / Tải PDF
                        </button>
                        <button
                            type="button"
                            disabled={isSaving}
                            onClick={handleSaveToProfile}
                            className="inline-flex w-full min-w-0 items-center justify-center gap-2 rounded-lg bg-blue-600 px-3 py-2 text-xs font-semibold text-white shadow-sm transition hover:bg-blue-700 disabled:opacity-50 sm:w-auto sm:px-5 sm:text-sm"
                        >
                            {isSaving ? (
                                <>
                                    <Loader2 className="h-4 w-4 animate-spin" />
                                    Đang tạo & lưu CV...
                                </>
                            ) : (
                                <>
                                    <Download className="h-4 w-4" />
                                    Lưu vào Hồ sơ CV
                                </>
                            )}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    )
}
