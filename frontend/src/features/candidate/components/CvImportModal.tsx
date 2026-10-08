import React, { useState, useRef } from 'react'
import {
    UploadCloud,
    FileText,
    CheckCircle2,
    AlertCircle,
    X,
    Briefcase,
    Code2,
    Building2,
    Calendar,
    Sparkles,
    Loader2,
    RefreshCw
} from 'lucide-react'
import { parseCv, applyParsedCv } from '../candidateApi'
import type { ParsedCvResult, ApplyParsedCvPayload } from '../candidateTypes'

interface CvImportModalProps {
    isOpen: boolean
    onClose: () => void
    onSuccess: () => void
}

export function CvImportModal({ isOpen, onClose, onSuccess }: CvImportModalProps) {
    const fileInputRef = useRef<HTMLInputElement>(null)
    const [file, setFile] = useState<File | null>(null)
    const [isDragging, setIsDragging] = useState(false)
    const [isParsing, setIsParsing] = useState(false)
    const [parsedResult, setParsedResult] = useState<ParsedCvResult | null>(null)
    const [formPayload, setFormPayload] = useState<ApplyParsedCvPayload | null>(null)
    const [isApplying, setIsApplying] = useState(false)
    const [errorMessage, setErrorMessage] = useState<string | null>(null)
    const [successMessage, setSuccessMessage] = useState<string | null>(null)

    if (!isOpen) return null

    const handleFileSelected = async (selected: File) => {
        const ext = selected.name.toLowerCase()
        if (!ext.endsWith('.pdf') && !ext.endsWith('.docx')) {
            setErrorMessage('Chỉ hỗ trợ file CV định dạng .pdf hoặc .docx')
            return
        }

        if (selected.size > 10 * 1024 * 1024) {
            setErrorMessage('Dung lượng file tối đa là 10MB.')
            return
        }

        setFile(selected)
        setErrorMessage(null)
        setSuccessMessage(null)

        // Automatically trigger parsing
        setIsParsing(true)
        try {
            const result = await parseCv(selected)
            setParsedResult(result)
            setFormPayload({
                fullName: result.fullName || '',
                phone: result.phone || '',
                title: result.title || '',
                city: result.city || '',
                summary: result.summary || '',
                skills: [...(result.skills || [])],
                experiences: result.experiences ? result.experiences.map((exp) => ({
                    companyName: exp.companyName,
                    position: exp.position,
                    startDate: exp.startDate,
                    endDate: exp.endDate,
                    isCurrent: exp.isCurrent,
                    description: exp.description || ''
                })) : []
            })
        } catch (err: unknown) {
            const msg = err instanceof Error ? err.message : 'Không thể bóc tách nội dung CV'
            setErrorMessage(msg)
            setParsedResult(null)
            setFormPayload(null)
        } finally {
            setIsParsing(false)
        }
    }

    const handleDrop = (e: React.DragEvent) => {
        e.preventDefault()
        setIsDragging(false)
        if (e.dataTransfer.files && e.dataTransfer.files[0]) {
            handleFileSelected(e.dataTransfer.files[0])
        }
    }

    const handleDragOver = (e: React.DragEvent) => {
        e.preventDefault()
        setIsDragging(true)
    }

    const handleDragLeave = () => {
        setIsDragging(false)
    }

    const handleRemoveSkill = (skillToRemove: string) => {
        if (!formPayload) return
        setFormPayload({
            ...formPayload,
            skills: (formPayload.skills || []).filter((s) => s !== skillToRemove)
        })
    }

    const handleRemoveExperience = (idx: number) => {
        if (!formPayload) return
        setFormPayload({
            ...formPayload,
            experiences: (formPayload.experiences || []).filter((_, i) => i !== idx)
        })
    }

    const handleApply = async () => {
        if (!formPayload) return

        setIsApplying(true)
        setErrorMessage(null)
        try {
            await applyParsedCv(formPayload)
            setSuccessMessage('Đồng bộ dữ liệu CV vào hồ sơ thành công!')
            setTimeout(() => {
                onSuccess()
                onClose()
            }, 1200)
        } catch (err: unknown) {
            const msg = err instanceof Error ? err.message : 'Lỗi đồng bộ hồ sơ'
            setErrorMessage(msg)
        } finally {
            setIsApplying(false)
        }
    }

    const handleReset = () => {
        setFile(null)
        setParsedResult(null)
        setFormPayload(null)
        setErrorMessage(null)
        setSuccessMessage(null)
        if (fileInputRef.current) {
            fileInputRef.current.value = ''
        }
    }

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-in fade-in duration-200">
            <div className="relative w-full max-w-3xl max-h-[92vh] flex flex-col rounded-3xl bg-white shadow-2xl border border-slate-200 overflow-hidden">
                {/* Modal Header */}
                <div className="flex items-center justify-between border-b border-slate-100 px-6 py-4 bg-gradient-to-r from-slate-50 to-blue-50/40">
                    <div className="flex items-center gap-3">
                        <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-600 text-white shadow-sm">
                            <Sparkles className="h-5 w-5" />
                        </div>
                        <div>
                            <h2 className="text-lg font-bold text-slate-900">
                                Nhập nhanh thông tin từ file CV
                            </h2>
                            <p className="text-xs font-medium text-slate-500">
                                Trích xuất tự động kỹ năng, kinh nghiệm và thông tin liên hệ từ CV (PDF/DOCX)
                            </p>
                        </div>
                    </div>
                    <button
                        onClick={onClose}
                        className="rounded-xl p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-600 transition"
                    >
                        <X className="h-5 w-5" />
                    </button>
                </div>

                {/* Modal Body */}
                <div className="flex-1 overflow-y-auto p-6 space-y-6">
                    {/* Alerts */}
                    {errorMessage && (
                        <div className="flex items-start gap-3 rounded-2xl border border-rose-200 bg-rose-50 p-4 text-xs font-medium text-rose-800">
                            <AlertCircle className="h-4 w-4 shrink-0 text-rose-600 mt-0.5" />
                            <span>{errorMessage}</span>
                        </div>
                    )}
                    {successMessage && (
                        <div className="flex items-center gap-3 rounded-2xl border border-emerald-200 bg-emerald-50 p-4 text-xs font-medium text-emerald-800">
                            <CheckCircle2 className="h-4 w-4 shrink-0 text-emerald-600" />
                            <span>{successMessage}</span>
                        </div>
                    )}
                    {parsedResult && (
                        <div className={`rounded-2xl border p-4 text-xs font-medium ${
                            parsedResult.processingSource === 'GEMINI'
                                ? 'border-violet-200 bg-violet-50 text-violet-800'
                                : 'border-amber-200 bg-amber-50 text-amber-800'
                        }`} role="status">
                            {parsedResult.processingSource === 'GEMINI'
                                ? 'CV đã được phân tích bằng Gemini AI. Vui lòng kiểm tra lại thông tin trước khi lưu.'
                                : 'Gemini AI chưa khả dụng; thông tin được trích xuất bằng bộ quy tắc cơ bản. Vui lòng kiểm tra kỹ trước khi lưu.'}
                        </div>
                    )}

                    {/* Step 1: Upload Dropzone if no parsed result */}
                    {!parsedResult && (
                        <div className="space-y-4">
                            <div
                                onDrop={handleDrop}
                                onDragOver={handleDragOver}
                                onDragLeave={handleDragLeave}
                                onClick={() => fileInputRef.current?.click()}
                                className={`flex flex-col items-center justify-center rounded-3xl border-2 border-dashed p-10 text-center cursor-pointer transition ${
                                    isDragging
                                        ? 'border-blue-500 bg-blue-50/60'
                                        : 'border-slate-300 hover:border-blue-400 hover:bg-slate-50/60'
                                }`}
                            >
                                <input
                                    ref={fileInputRef}
                                    type="file"
                                    accept=".pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                    className="hidden"
                                    onChange={(e) => {
                                        if (e.target.files && e.target.files[0]) {
                                            handleFileSelected(e.target.files[0])
                                        }
                                    }}
                                />

                                {isParsing ? (
                                    <div className="py-6 flex flex-col items-center gap-3 text-blue-600">
                                        <Loader2 className="h-10 w-10 animate-spin" />
                                        <p className="text-sm font-semibold text-slate-800">
                                            Đang đọc và bóc tách dữ liệu từ file CV...
                                        </p>
                                        <p className="text-xs text-slate-500 max-w-sm">
                                            Hệ thống đang trích xuất nội dung và sẽ dùng Gemini AI nếu dịch vụ đã được cấu hình.
                                        </p>
                                    </div>
                                ) : (
                                    <>
                                        <div className="mb-3 flex h-14 w-14 items-center justify-center rounded-2xl bg-blue-50 text-blue-600 shadow-inner">
                                            <UploadCloud className="h-7 w-7" />
                                        </div>
                                        <h3 className="text-sm font-bold text-slate-800">
                                            Kéo thả file CV của bạn vào đây hoặc <span className="text-blue-600 underline">chọn từ thiết bị</span>
                                        </h3>
                                        <p className="mt-1 text-xs text-slate-500">
                                            Hỗ trợ định dạng PDF (.pdf) hoặc Word (.docx), dung lượng tối đa 10MB
                                        </p>
                                        <div className="mt-4 flex items-center gap-2 text-[11px] font-semibold text-slate-400">
                                            <span className="rounded-md bg-slate-100 px-2 py-1">Họ tên & Liên hệ</span>
                                            <span className="rounded-md bg-slate-100 px-2 py-1">Kỹ năng công nghệ</span>
                                            <span className="rounded-md bg-slate-100 px-2 py-1">Kinh nghiệm làm việc</span>
                                        </div>
                                    </>
                                )}
                            </div>
                        </div>
                    )}

                    {/* Step 2: Parsed Preview & Edit Form */}
                    {parsedResult && formPayload && (
                        <div className="space-y-6 animate-in fade-in slide-in-from-bottom-2 duration-300">
                            {/* File banner */}
                            <div className="flex items-center justify-between rounded-2xl border border-blue-200 bg-blue-50/60 px-4 py-3 text-xs">
                                <div className="flex items-center gap-2 text-blue-900 font-semibold">
                                    <FileText className="h-4 w-4 text-blue-600" />
                                    <span>Đã phân tích: {file?.name}</span>
                                </div>
                                <button
                                    type="button"
                                    onClick={handleReset}
                                    className="flex items-center gap-1 text-blue-700 hover:text-blue-800 font-medium hover:underline"
                                >
                                    <RefreshCw className="h-3.5 w-3.5" /> Chọn file khác
                                </button>
                            </div>

                            {/* Section 1: Basic Info */}
                            <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm space-y-4">
                                <h3 className="text-xs font-bold uppercase tracking-wider text-slate-500 flex items-center gap-1.5">
                                    <Briefcase className="h-3.5 w-3.5 text-blue-600" />
                                    1. Thông tin cá nhân & Chức danh
                                </h3>

                                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                    <div>
                                        <label className="block text-xs font-semibold text-slate-700 mb-1">
                                            Họ và tên
                                        </label>
                                        <input
                                            type="text"
                                            value={formPayload.fullName || ''}
                                            onChange={(e) => setFormPayload({ ...formPayload, fullName: e.target.value })}
                                            className="w-full rounded-xl border border-slate-200 px-3.5 py-2 text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500 font-medium"
                                            placeholder="Nguyễn Văn A"
                                        />
                                    </div>

                                    <div>
                                        <label className="block text-xs font-semibold text-slate-700 mb-1">
                                            Vị trí công việc / Chức danh
                                        </label>
                                        <input
                                            type="text"
                                            value={formPayload.title || ''}
                                            onChange={(e) => setFormPayload({ ...formPayload, title: e.target.value })}
                                            className="w-full rounded-xl border border-slate-200 px-3.5 py-2 text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500 font-medium"
                                            placeholder="Senior Java Developer"
                                        />
                                    </div>

                                    <div>
                                        <label className="block text-xs font-semibold text-slate-700 mb-1">
                                            Số điện thoại
                                        </label>
                                        <input
                                            type="text"
                                            value={formPayload.phone || ''}
                                            onChange={(e) => setFormPayload({ ...formPayload, phone: e.target.value })}
                                            className="w-full rounded-xl border border-slate-200 px-3.5 py-2 text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500"
                                            placeholder="0912345678"
                                        />
                                    </div>

                                    <div>
                                        <label className="block text-xs font-semibold text-slate-700 mb-1">
                                            Tỉnh / Thành phố
                                        </label>
                                        <input
                                            type="text"
                                            value={formPayload.city || ''}
                                            onChange={(e) => setFormPayload({ ...formPayload, city: e.target.value })}
                                            className="w-full rounded-xl border border-slate-200 px-3.5 py-2 text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500"
                                            placeholder="Hồ Chí Minh"
                                        />
                                    </div>
                                </div>

                                <div>
                                    <label className="block text-xs font-semibold text-slate-700 mb-1">
                                        Tóm tắt bản thân
                                    </label>
                                    <textarea
                                        rows={3}
                                        value={formPayload.summary || ''}
                                        onChange={(e) => setFormPayload({ ...formPayload, summary: e.target.value })}
                                        className="w-full rounded-xl border border-slate-200 px-3.5 py-2 text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500 leading-relaxed"
                                        placeholder="Mô tả kinh nghiệm, thế mạnh bản thân..."
                                    />
                                </div>
                            </div>

                            {/* Section 2: Extracted Skills */}
                            <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm space-y-3">
                                <div className="flex items-center justify-between">
                                    <h3 className="text-xs font-bold uppercase tracking-wider text-slate-500 flex items-center gap-1.5">
                                        <Code2 className="h-3.5 w-3.5 text-indigo-600" />
                                        2. Kỹ năng nhận diện được ({formPayload.skills?.length || 0})
                                    </h3>
                                    <span className="text-[11px] text-slate-400">Bấm &times; để loại bỏ kỹ năng không khớp</span>
                                </div>

                                {formPayload.skills && formPayload.skills.length > 0 ? (
                                    <div className="flex flex-wrap gap-2">
                                        {formPayload.skills.map((skill) => (
                                            <span
                                                key={skill}
                                                className="inline-flex items-center gap-1.5 rounded-lg bg-indigo-50 border border-indigo-200 px-2.5 py-1 text-xs font-semibold text-indigo-700 shadow-2xs"
                                            >
                                                {skill}
                                                <button
                                                    type="button"
                                                    onClick={() => handleRemoveSkill(skill)}
                                                    className="rounded-full hover:bg-indigo-200 p-0.5 text-indigo-500 hover:text-indigo-800 transition"
                                                >
                                                    <X className="h-3 w-3" />
                                                </button>
                                            </span>
                                        ))}
                                    </div>
                                ) : (
                                    <p className="text-xs text-slate-400 italic">Không nhận diện được kỹ năng nào từ file CV.</p>
                                )}
                            </div>

                            {/* Section 3: Extracted Experiences */}
                            <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm space-y-3">
                                <h3 className="text-xs font-bold uppercase tracking-wider text-slate-500 flex items-center gap-1.5">
                                    <Building2 className="h-3.5 w-3.5 text-emerald-600" />
                                    3. Lịch sử kinh nghiệm ({formPayload.experiences?.length || 0})
                                </h3>

                                {formPayload.experiences && formPayload.experiences.length > 0 ? (
                                    <div className="space-y-3">
                                        {formPayload.experiences.map((exp, idx) => (
                                            <div
                                                key={idx}
                                                className="relative rounded-xl border border-slate-200 bg-slate-50/70 p-3.5 text-xs text-slate-700"
                                            >
                                                <button
                                                    type="button"
                                                    onClick={() => handleRemoveExperience(idx)}
                                                    className="absolute top-3 right-3 text-slate-400 hover:text-rose-600 transition"
                                                    title="Xóa kinh nghiệm này"
                                                >
                                                    <X className="h-4 w-4" />
                                                </button>
                                                <div className="font-bold text-slate-900 text-sm">{exp.position}</div>
                                                <div className="font-semibold text-blue-600 mt-0.5">{exp.companyName}</div>
                                                <div className="text-slate-500 text-[11px] mt-0.5 flex items-center gap-1">
                                                    <Calendar className="h-3 w-3" />
                                                    {exp.startDate} - {exp.isCurrent ? 'Hiện tại' : (exp.endDate || 'Chưa ghi')}
                                                </div>
                                                {exp.description && (
                                                    <p className="mt-2 text-slate-600 text-[11px] line-clamp-2">
                                                        {exp.description}
                                                    </p>
                                                )}
                                            </div>
                                        ))}
                                    </div>
                                ) : (
                                    <p className="text-xs text-slate-400 italic">Không phát hiện được lịch sử kinh nghiệm có cấu trúc rõ ràng.</p>
                                )}
                            </div>
                        </div>
                    )}
                </div>

                {/* Modal Footer */}
                <div className="flex items-center justify-between border-t border-slate-100 bg-slate-50/60 px-6 py-4">
                    <button
                        type="button"
                        onClick={onClose}
                        className="rounded-xl border border-slate-200 bg-white px-4 py-2 text-xs font-semibold text-slate-700 hover:bg-slate-50 transition"
                    >
                        Đóng
                    </button>

                    {parsedResult && formPayload && (
                        <div className="flex items-center gap-2">
                            <button
                                type="button"
                                onClick={handleApply}
                                disabled={isApplying}
                                className="inline-flex items-center gap-2 rounded-xl bg-blue-600 px-5 py-2.5 text-xs font-bold text-white shadow-sm hover:bg-blue-700 disabled:opacity-50 transition"
                            >
                                {isApplying ? (
                                    <>
                                        <Loader2 className="h-4 w-4 animate-spin" />
                                        Đang đồng bộ vào hồ sơ...
                                    </>
                                ) : (
                                    <>
                                        <CheckCircle2 className="h-4 w-4" />
                                        Đồng bộ vào Hồ sơ TalentBridge
                                    </>
                                )}
                            </button>
                        </div>
                    )}
                </div>
            </div>
        </div>
    )
}
