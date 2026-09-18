import { useRef, useState } from 'react'
import {
    CheckCircle2,
    Download,
    Plus,
    Star,
    Trash2,
    UploadCloud,
    X,
    AlertCircle,
} from 'lucide-react'
import {
    deleteResume,
    downloadResumeFile,
    setDefaultResume,
    uploadResume,
    CandidateApiError,
} from '../candidateApi'
import type { ResumeItem } from '../candidateTypes'
import { EmptyStateIllustration, CvManagerIllustration } from '../../../components/illustrations'

interface ResumeUploadSectionProps {
    resumes: ResumeItem[]
    onRefresh: () => void
}

export function ResumeUploadSection({ resumes, onRefresh }: ResumeUploadSectionProps) {
    const fileInputRef = useRef<HTMLInputElement>(null)
    const [isModalOpen, setIsModalOpen] = useState(false)
    const [selectedFile, setSelectedFile] = useState<File | null>(null)
    const [title, setTitle] = useState('')
    const [isUploading, setIsUploading] = useState(false)
    const [errorMsg, setErrorMsg] = useState<string | null>(null)
    const [successMsg, setSuccessMsg] = useState<string | null>(null)

    const MAX_SIZE_MB = 10
    const MAX_SIZE_BYTES = MAX_SIZE_MB * 1024 * 1024

    function handleFileChange(e: React.ChangeEvent<HTMLInputElement>) {
        setErrorMsg(null)
        const file = e.target.files?.[0]
        if (!file) return

        validateAndSetFile(file)
    }

    function validateAndSetFile(file: File) {
        const lowerName = file.name.toLowerCase()
        const isPdf = lowerName.endsWith('.pdf')
        const isDocx = lowerName.endsWith('.docx')

        if (!isPdf && !isDocx) {
            setErrorMsg('Định dạng file không hợp lệ. Chỉ chấp nhận file .pdf hoặc .docx.')
            setSelectedFile(null)
            return
        }

        if (file.size > MAX_SIZE_BYTES) {
            setErrorMsg(`Dung lượng file vượt quá ${MAX_SIZE_MB}MB. Vui lòng chọn file nhẹ hơn.`)
            setSelectedFile(null)
            return
        }

        setSelectedFile(file)
        if (!title.trim()) {
            setTitle(file.name.replace(/\.[^/.]+$/, ''))
        }
    }

    async function handleUploadSubmit(e: React.FormEvent) {
        e.preventDefault()
        if (!selectedFile) {
            setErrorMsg('Vui lòng chọn file CV cần tải lên.')
            return
        }

        setIsUploading(true)
        setErrorMsg(null)

        try {
            await uploadResume(selectedFile, title.trim() || selectedFile.name)
            setSuccessMsg('Tải lên CV thành công!')
            setIsModalOpen(false)
            setSelectedFile(null)
            setTitle('')
            onRefresh()
            setTimeout(() => setSuccessMsg(null), 4000)
        } catch (err) {
            if (err instanceof CandidateApiError) {
                setErrorMsg(err.message)
            } else {
                setErrorMsg('Đã có lỗi xảy ra khi tải lên file CV.')
            }
        } finally {
            setIsUploading(false)
        }
    }

    async function handleSetDefault(id: number) {
        try {
            await setDefaultResume(id)
            setSuccessMsg('Đã cập nhật CV mặc định!')
            onRefresh()
            setTimeout(() => setSuccessMsg(null), 4000)
        } catch {
            setErrorMsg('Không thể đặt làm CV mặc định.')
        }
    }

    async function handleDelete(id: number, cvTitle: string) {
        if (window.confirm(`Bạn có chắc chắn muốn xóa hồ sơ CV "${cvTitle}" không?`)) {
            try {
                await deleteResume(id)
                setSuccessMsg('Đã xóa CV thành công!')
                onRefresh()
                setTimeout(() => setSuccessMsg(null), 4000)
            } catch {
                setErrorMsg('Không thể xóa CV.')
            }
        }
    }

    async function handleDownload(id: number, fileName: string) {
        try {
            await downloadResumeFile(id, fileName)
        } catch {
            setErrorMsg('Không thể tải xuống CV.')
        }
    }

    return (
        <div className="rounded-3xl border border-slate-200 bg-white p-6 sm:p-8 shadow-sm">
            <div className="flex items-center justify-between mb-6">
                <div className="flex items-center gap-3">
                    <CvManagerIllustration className="size-8 shrink-0" />
                    <div>
                        <h3 className="text-base font-bold text-slate-900">Hồ sơ CV đã tải lên</h3>
                        <p className="text-xs text-slate-500 font-medium">TopCV Standard - ATS Ready</p>
                    </div>
                </div>
                <button
                    type="button"
                    onClick={() => {
                        setErrorMsg(null)
                        setSelectedFile(null)
                        setTitle('')
                        setIsModalOpen(true)
                    }}
                    className="inline-flex items-center gap-1.5 rounded-xl bg-rose-50 px-3.5 py-1.5 text-xs font-semibold text-rose-700 hover:bg-rose-100 transition"
                >
                    <Plus className="h-4 w-4" /> Tải lên CV mới
                </button>
            </div>

            {successMsg && (
                <div className="mb-4 flex items-center gap-2 rounded-xl bg-emerald-50 border border-emerald-200 p-3 text-xs text-emerald-800">
                    <CheckCircle2 className="h-4 w-4 shrink-0 text-emerald-600" />
                    <span>{successMsg}</span>
                </div>
            )}

            {errorMsg && (
                <div className="mb-4 flex items-center gap-2 rounded-xl bg-rose-50 border border-rose-200 p-3 text-xs text-rose-800">
                    <AlertCircle className="h-4 w-4 shrink-0 text-rose-600" />
                    <span>{errorMsg}</span>
                </div>
            )}

            {resumes.length === 0 ? (
                <div className="rounded-2xl border border-dashed border-slate-200 p-8 text-center">
                    <EmptyStateIllustration className="w-24 h-auto mx-auto mb-3" />
                    <p className="text-sm font-medium text-slate-600">Bạn chưa có CV tải lên nào</p>
                    <p className="text-xs text-slate-400 mt-1">
                        Hỗ trợ file PDF hoặc DOCX (tối đa 10MB). Tải lên CV giúp nhà tuyển dụng tiếp cận hồ sơ của bạn nhanh chóng.
                    </p>
                    <button
                        type="button"
                        onClick={() => {
                            setErrorMsg(null)
                            setSelectedFile(null)
                            setTitle('')
                            setIsModalOpen(true)
                        }}
                        className="mt-4 inline-flex items-center gap-1.5 rounded-xl bg-rose-600 px-4 py-2 text-xs font-semibold text-white hover:bg-rose-700 transition shadow-sm"
                    >
                        <UploadCloud className="h-3.5 w-3.5" /> Tải lên ngay
                    </button>
                </div>
            ) : (
                <div className="space-y-3">
                    {resumes.map((resume) => {
                        const isDocx = resume.fileName.toLowerCase().endsWith('.docx')
                        return (
                            <div
                                key={resume.id}
                                className={`flex flex-col sm:flex-row sm:items-center justify-between gap-4 rounded-2xl border p-4 transition ${
                                    resume.isDefault
                                        ? 'border-rose-300 bg-rose-50/20 shadow-sm'
                                        : 'border-slate-200 bg-slate-50/40 hover:border-slate-300 hover:bg-white'
                                }`}
                            >
                                <div className="flex items-start sm:items-center gap-3.5">
                                    <div
                                        className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-xl font-bold text-xs shadow-sm ${
                                            isDocx
                                                ? 'bg-blue-600 text-white'
                                                : 'bg-rose-600 text-white'
                                        }`}
                                    >
                                        {isDocx ? 'DOCX' : 'PDF'}
                                    </div>
                                    <div>
                                        <div className="flex flex-wrap items-center gap-2">
                                            <h4 className="text-sm font-bold text-slate-900">{resume.title}</h4>
                                            {resume.isDefault && (
                                                <span className="inline-flex items-center gap-1 rounded-full bg-rose-100 px-2.5 py-0.5 text-[11px] font-semibold text-rose-700">
                                                    <Star className="h-3 w-3 fill-rose-600 text-rose-600" />
                                                    CV Mặc định
                                                </span>
                                            )}
                                        </div>
                                        <p className="text-xs text-slate-500 mt-0.5 truncate max-w-[280px] sm:max-w-md">
                                            {resume.fileName} &bull; Tải lên ngày {resume.createdAt ? resume.createdAt.split('T')[0] : ''}
                                        </p>
                                    </div>
                                </div>

                                <div className="flex items-center gap-2 self-end sm:self-center">
                                    {!resume.isDefault && (
                                        <button
                                            type="button"
                                            onClick={() => handleSetDefault(resume.id)}
                                            className="inline-flex items-center gap-1 rounded-lg border border-slate-200 bg-white px-3 py-1.5 text-xs font-semibold text-slate-600 hover:bg-slate-50 hover:text-slate-900 transition"
                                            title="Đặt làm CV chính để ứng tuyển"
                                        >
                                            <Star className="h-3.5 w-3.5 text-amber-500" />
                                            Đặt mặc định
                                        </button>
                                    )}

                                    <button
                                        type="button"
                                        onClick={() => handleDownload(resume.id, resume.fileName)}
                                        className="inline-flex items-center gap-1 rounded-lg border border-slate-200 bg-white px-3 py-1.5 text-xs font-semibold text-slate-600 hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700 transition"
                                        title="Tải xuống CV"
                                    >
                                        <Download className="h-3.5 w-3.5" />
                                        Tải về
                                    </button>

                                    <button
                                        type="button"
                                        onClick={() => handleDelete(resume.id, resume.title)}
                                        className="rounded-lg p-1.5 text-slate-400 hover:bg-rose-50 hover:text-rose-600 transition"
                                        title="Xóa CV"
                                    >
                                        <Trash2 className="h-4 w-4" />
                                    </button>
                                </div>
                            </div>
                        )
                    })}
                </div>
            )}

            {/* Upload Modal */}
            {isModalOpen && (
                <div
                    aria-modal="true"
                    className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4 backdrop-blur-sm"
                    role="dialog"
                >
                    <div className="relative w-full max-w-lg rounded-2xl bg-white p-6 shadow-2xl transition-all">
                        <div className="flex items-center justify-between border-b border-slate-100 pb-4">
                            <div className="flex items-center gap-2 text-slate-800">
                                <div className="grid size-9 place-items-center rounded-xl bg-rose-50 text-rose-600">
                                    <UploadCloud size={20} />
                                </div>
                                <h2 className="text-lg font-bold">Tải lên hồ sơ CV</h2>
                            </div>
                            <button
                                className="rounded-lg p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
                                disabled={isUploading}
                                onClick={() => setIsModalOpen(false)}
                                type="button"
                            >
                                <X size={20} />
                            </button>
                        </div>

                        {errorMsg && (
                            <div className="mt-4 rounded-xl border border-rose-200 bg-rose-50 p-3 text-xs text-rose-600 flex items-center gap-2">
                                <AlertCircle className="h-4 w-4 shrink-0" />
                                <span>{errorMsg}</span>
                            </div>
                        )}

                        <form className="mt-4 space-y-4" onSubmit={handleUploadSubmit}>
                            {/* File Drag / Drop area */}
                            <div
                                onClick={() => fileInputRef.current?.click()}
                                onDragOver={(e) => e.preventDefault()}
                                onDrop={(e) => {
                                    e.preventDefault()
                                    if (e.dataTransfer.files?.[0]) {
                                        validateAndSetFile(e.dataTransfer.files[0])
                                    }
                                }}
                                className={`flex flex-col items-center justify-center rounded-2xl border-2 border-dashed p-6 text-center cursor-pointer transition ${
                                    selectedFile
                                        ? 'border-emerald-400 bg-emerald-50/30'
                                        : 'border-slate-300 bg-slate-50/60 hover:border-rose-400 hover:bg-rose-50/20'
                                }`}
                            >
                                <input
                                    ref={fileInputRef}
                                    type="file"
                                    accept=".pdf,.docx"
                                    onChange={handleFileChange}
                                    className="hidden"
                                />
                                <UploadCloud className={`h-10 w-10 mb-2 ${selectedFile ? 'text-emerald-600' : 'text-slate-400'}`} />
                                {selectedFile ? (
                                    <div>
                                        <p className="text-sm font-bold text-emerald-800 truncate max-w-xs">{selectedFile.name}</p>
                                        <p className="text-xs text-emerald-600 mt-0.5">
                                            {(selectedFile.size / (1024 * 1024)).toFixed(2)} MB &bull; Nhấp để thay đổi
                                        </p>
                                    </div>
                                ) : (
                                    <div>
                                        <p className="text-sm font-semibold text-slate-700">
                                            Kéo thả file CV vào đây hoặc <span className="text-rose-600">chọn từ máy tính</span>
                                        </p>
                                        <p className="text-xs text-slate-400 mt-1">Định dạng hỗ trợ: PDF, DOCX (tối đa 10MB)</p>
                                    </div>
                                )}
                            </div>

                            {/* CV Title Input */}
                            <div>
                                <label className="block text-xs font-semibold text-slate-700 mb-1" htmlFor="cv-title">
                                    Tiêu đề hồ sơ CV
                                </label>
                                <input
                                    id="cv-title"
                                    type="text"
                                    value={title}
                                    onChange={(e) => setTitle(e.target.value)}
                                    placeholder="VD: CV Senior Java Backend Developer 2026"
                                    className="w-full rounded-xl border border-slate-200 px-3.5 py-2 text-sm text-slate-800 focus:border-rose-500 focus:outline-none focus:ring-2 focus:ring-rose-100"
                                />
                                <p className="text-[11px] text-slate-400 mt-1">
                                    Tiêu đề giúp bạn phân biệt các phiên bản CV khi ứng tuyển.
                                </p>
                            </div>

                            <div className="mt-6 flex justify-end gap-3 pt-2">
                                <button
                                    className="rounded-xl border border-slate-200 px-4 py-2 text-sm font-medium text-slate-600 transition hover:bg-slate-50"
                                    disabled={isUploading}
                                    onClick={() => setIsModalOpen(false)}
                                    type="button"
                                >
                                    Hủy
                                </button>
                                <button
                                    className="inline-flex items-center gap-2 rounded-xl bg-rose-600 px-5 py-2 text-sm font-semibold text-white transition hover:bg-rose-700 disabled:opacity-50"
                                    disabled={isUploading || !selectedFile}
                                    type="submit"
                                >
                                    {isUploading ? (
                                        <>
                                            <span className="inline-block size-4 animate-spin rounded-full border-2 border-white border-t-transparent" />
                                            <span>Đang tải lên...</span>
                                        </>
                                    ) : (
                                        <span>Tải lên CV</span>
                                    )}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    )
}
