import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
    AlertCircle,
    CheckCircle2,
    FileText,
    Loader2,
    Plus,
    Send,
    UploadCloud,
    X,
} from 'lucide-react'
import { type ChangeEvent, useState } from 'react'
import { Link } from 'react-router'
import {
    applyJob,
    CandidateApiError,
    getCandidateResumes,
    uploadResume,
} from '../../candidate/candidateApi'
import type { ResumeItem } from '../../candidate/candidateTypes'

interface ApplyJobModalProps {
    isOpen: boolean
    onClose: () => void
    jobId: number
    jobTitle: string
    companyName: string
    onApplySuccess?: () => void
}

export function ApplyJobModal({
    isOpen,
    onClose,
    jobId,
    jobTitle,
    companyName,
    onApplySuccess,
}: ApplyJobModalProps) {
    const queryClient = useQueryClient()
    const [selectedResumeId, setSelectedResumeId] = useState<number | null>(null)
    const [coverLetter, setCoverLetter] = useState('')
    const [isUploadingResume, setIsUploadingResume] = useState(false)
    const [uploadError, setUploadError] = useState<string | null>(null)
    const [isSubmittedSuccessfully, setIsSubmittedSuccessfully] = useState(false)

    // Fetch existing candidate resumes
    const resumesQuery = useQuery({
        queryKey: ['candidate-resumes'],
        queryFn: getCandidateResumes,
        enabled: isOpen,
    })

    const resumes: ResumeItem[] = resumesQuery.data ?? []

    // Default selection to default resume or first resume
    const effectiveSelectedResumeId =
        selectedResumeId ??
        (resumes.find((r) => r.isDefault)?.id ?? (resumes.length > 0 ? resumes[0]?.id : null))

    // Mutation to apply
    const applyMutation = useMutation({
        mutationFn: () => {
            if (!effectiveSelectedResumeId) {
                throw new Error('Vui lòng chọn hoặc tải lên một CV để ứng tuyển.')
            }
            return applyJob({
                jobId,
                resumeId: effectiveSelectedResumeId,
                coverLetter: coverLetter.trim() || undefined,
            })
        },
        onSuccess: () => {
            setIsSubmittedSuccessfully(true)
            void queryClient.invalidateQueries({ queryKey: ['candidate-applications'] })
            onApplySuccess?.()
        },
    })

    // Handler for uploading a new resume directly inside the modal
    async function handleFileUpload(e: ChangeEvent<HTMLInputElement>) {
        const file = e.target.files?.[0]
        if (!file) return

        if (file.size > 10 * 1024 * 1024) {
            setUploadError('Dung lượng file tối đa là 10MB.')
            return
        }

        const ext = file.name.split('.').pop()?.toLowerCase()
        if (ext !== 'pdf' && ext !== 'doc' && ext !== 'docx') {
            setUploadError('Chỉ hỗ trợ file định dạng .pdf, .doc, .docx.')
            return
        }

        setUploadError(null)
        setIsUploadingResume(true)
        try {
            const newResume = await uploadResume(file, file.name.replace(/\.[^/.]+$/, ''))
            await queryClient.invalidateQueries({ queryKey: ['candidate-resumes'] })
            setSelectedResumeId(newResume.id)
        } catch (err) {
            setUploadError(
                err instanceof CandidateApiError ? err.message : 'Không thể tải lên file CV.',
            )
        } finally {
            setIsUploadingResume(false)
            e.target.value = ''
        }
    }

    if (!isOpen) return null

    function handleClose() {
        if (!applyMutation.isPending) {
            setIsSubmittedSuccessfully(false)
            applyMutation.reset()
            onClose()
        }
    }

    return (
        <div
            aria-modal="true"
            className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs transition-opacity"
            role="dialog"
        >
            <div
                className="relative w-full max-w-xl overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-2xl transition-all"
                onClick={(e) => e.stopPropagation()}
            >
                {/* Header */}
                <div className="flex items-start justify-between border-b border-slate-100 bg-gradient-to-r from-slate-50 via-indigo-50/30 to-slate-50 p-6">
                    <div>
                        <span className="bento-badge bg-indigo-50 text-indigo-700 text-xs">
                            Nộp hồ sơ trực tuyến
                        </span>
                        <h2 className="mt-2 text-xl font-bold tracking-tight text-slate-950">
                            {jobTitle}
                        </h2>
                        <p className="mt-1 text-sm font-medium text-slate-600">{companyName}</p>
                    </div>
                    <button
                        className="rounded-xl p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-600 transition-colors"
                        disabled={applyMutation.isPending}
                        onClick={handleClose}
                        type="button"
                    >
                        <X size={20} />
                    </button>
                </div>

                {/* Content */}
                <div className="max-h-[75vh] overflow-y-auto p-6 space-y-6">
                    {isSubmittedSuccessfully ? (
                        <div className="py-6 text-center">
                            <div className="mx-auto grid size-16 place-items-center rounded-2xl bg-emerald-50 text-emerald-600 shadow-xs">
                                <CheckCircle2 size={36} />
                            </div>
                            <h3 className="mt-4 text-xl font-bold text-slate-950">
                                Ứng tuyển thành công!
                            </h3>
                            <p className="mt-2 text-sm leading-6 text-slate-600 max-w-md mx-auto">
                                Hồ sơ của bạn đã được gửi trực tiếp đến bộ phận tuyển dụng của{' '}
                                <span className="font-semibold text-slate-900">{companyName}</span>.
                                Bạn có thể theo dõi quy trình xét duyệt tại danh sách đơn ứng tuyển.
                            </p>

                            <div className="mt-8 flex flex-col sm:flex-row items-center justify-center gap-3">
                                <Link
                                    className="btn-bento-primary w-full sm:w-auto"
                                    to="/candidate/applications"
                                >
                                    <span>Xem đơn ứng tuyển của tôi</span>
                                </Link>
                                <button
                                    className="btn-bento-secondary w-full sm:w-auto"
                                    onClick={handleClose}
                                    type="button"
                                >
                                    <span>Đóng cửa sổ</span>
                                </button>
                            </div>
                        </div>
                    ) : (
                        <>
                            {/* Resume Selection Section */}
                            <div>
                                <div className="flex items-center justify-between">
                                    <label className="text-sm font-bold text-slate-900">
                                        Chọn CV ứng tuyển <span className="text-rose-500">*</span>
                                    </label>
                                    <label className="inline-flex items-center gap-1.5 text-xs font-semibold text-indigo-600 hover:text-indigo-700 cursor-pointer">
                                        <Plus size={14} />
                                        <span>Tải lên CV khác</span>
                                        <input
                                            accept=".pdf,.doc,.docx"
                                            className="hidden"
                                            disabled={isUploadingResume}
                                            onChange={handleFileUpload}
                                            type="file"
                                        />
                                    </label>
                                </div>

                                {uploadError && (
                                    <div className="mt-2.5 flex items-center gap-2 rounded-xl bg-rose-50 px-3.5 py-2 text-xs font-medium text-rose-700">
                                        <AlertCircle size={15} />
                                        <span>{uploadError}</span>
                                    </div>
                                )}

                                {isUploadingResume && (
                                    <div className="mt-3 flex items-center gap-2.5 rounded-2xl border border-indigo-200 bg-indigo-50/60 p-3.5 text-xs font-medium text-indigo-700">
                                        <Loader2 className="animate-spin" size={16} />
                                        <span>Đang tải lên và xử lý file CV...</span>
                                    </div>
                                )}

                                {resumesQuery.isLoading ? (
                                    <div className="mt-3 space-y-2">
                                        <div className="h-14 rounded-2xl bg-slate-100 animate-pulse" />
                                        <div className="h-14 rounded-2xl bg-slate-100 animate-pulse" />
                                    </div>
                                ) : resumes.length === 0 ? (
                                    <div className="mt-3 rounded-2xl border border-dashed border-slate-300 bg-slate-50/70 p-6 text-center">
                                        <UploadCloud className="mx-auto text-slate-400" size={32} />
                                        <p className="mt-2 text-sm font-bold text-slate-800">
                                            Bạn chưa có CV nào trong hồ sơ
                                        </p>
                                        <p className="mt-1 text-xs text-slate-500">
                                            Tải lên CV định dạng PDF, DOC, DOCX (tối đa 10MB)
                                        </p>
                                        <label className="btn-bento-primary mt-4 inline-flex cursor-pointer text-xs">
                                            <UploadCloud size={15} />
                                            <span>Tải lên CV ngay</span>
                                            <input
                                                accept=".pdf,.doc,.docx"
                                                className="hidden"
                                                disabled={isUploadingResume}
                                                onChange={handleFileUpload}
                                                type="file"
                                            />
                                        </label>
                                    </div>
                                ) : (
                                    <div className="mt-3 space-y-2">
                                        {resumes.map((resume) => {
                                            const isSelected =
                                                effectiveSelectedResumeId === resume.id
                                            return (
                                                <label
                                                    className={`flex items-center justify-between rounded-2xl border p-3.5 cursor-pointer transition-all ${
                                                        isSelected
                                                            ? 'border-indigo-600 bg-indigo-50/40 shadow-xs ring-1 ring-indigo-500'
                                                            : 'border-slate-200 bg-white hover:border-slate-300'
                                                    }`}
                                                    key={resume.id}
                                                >
                                                    <div className="flex items-center gap-3 min-w-0">
                                                        <input
                                                            checked={isSelected}
                                                            className="text-indigo-600 focus:ring-indigo-500 size-4 border-slate-300"
                                                            name="selectedResume"
                                                            onChange={() =>
                                                                setSelectedResumeId(resume.id)
                                                            }
                                                            type="radio"
                                                        />
                                                        <div className="grid size-9 shrink-0 place-items-center rounded-xl bg-slate-100 text-slate-600">
                                                            <FileText size={18} />
                                                        </div>
                                                        <div className="min-w-0">
                                                            <p className="truncate text-sm font-bold text-slate-900">
                                                                {resume.title || resume.fileName}
                                                            </p>
                                                            <p className="truncate text-xs text-slate-500">
                                                                {resume.fileName} &bull; Tải lên{' '}
                                                                {resume.createdAt
                                                                    ? resume.createdAt.split('T')[0]
                                                                    : ''}
                                                            </p>
                                                        </div>
                                                    </div>
                                                    {resume.isDefault && (
                                                        <span className="bento-badge bg-emerald-50 text-emerald-700 text-[10px] shrink-0">
                                                            Mặc định
                                                        </span>
                                                    )}
                                                </label>
                                            )
                                        })}
                                    </div>
                                )}
                            </div>

                            {/* Cover Letter Section */}
                            <div>
                                <div className="flex items-center justify-between">
                                    <label
                                        className="text-sm font-bold text-slate-900"
                                        htmlFor="coverLetter"
                                    >
                                        Thư giới thiệu (Cover Letter)
                                    </label>
                                    <span className="text-xs text-slate-400">
                                        {coverLetter.length}/1000 ký tự
                                    </span>
                                </div>
                                <textarea
                                    className="mt-2 w-full rounded-2xl border border-slate-200 bg-white p-3.5 text-sm text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-indigo-500 focus:ring-3 focus:ring-indigo-100"
                                    id="coverLetter"
                                    maxLength={1000}
                                    onChange={(e) => setCoverLetter(e.target.value)}
                                    placeholder="Nêu bật lý do bạn phù hợp với vị trí này, kinh nghiệm và giá trị bạn có thể mang lại cho doanh nghiệp..."
                                    rows={4}
                                    value={coverLetter}
                                />
                            </div>

                            {/* Error notification */}
                            {applyMutation.isError && (
                                <div className="rounded-2xl border border-rose-200 bg-rose-50 p-4 text-xs text-rose-700">
                                    <div className="flex items-start gap-2.5">
                                        <AlertCircle className="shrink-0 text-rose-600" size={17} />
                                        <div>
                                            <p className="font-bold">Không thể nộp đơn</p>
                                            <p className="mt-0.5 leading-relaxed">
                                                {applyMutation.error instanceof CandidateApiError
                                                    ? applyMutation.error.message
                                                    : applyMutation.error instanceof Error
                                                    ? applyMutation.error.message
                                                    : 'Đã có lỗi xảy ra trong quá trình nộp đơn.'}
                                            </p>
                                        </div>
                                    </div>
                                </div>
                            )}

                            {/* Footer Actions */}
                            <div className="flex items-center justify-end gap-3 pt-3 border-t border-slate-100">
                                <button
                                    className="btn-bento-secondary"
                                    disabled={applyMutation.isPending}
                                    onClick={handleClose}
                                    type="button"
                                >
                                    <span>Hủy bỏ</span>
                                </button>
                                <button
                                    className="btn-bento-primary"
                                    disabled={
                                        applyMutation.isPending || !effectiveSelectedResumeId
                                    }
                                    onClick={() => applyMutation.mutate()}
                                    type="button"
                                >
                                    {applyMutation.isPending ? (
                                        <>
                                            <Loader2 className="animate-spin" size={16} />
                                            <span>Đang nộp hồ sơ...</span>
                                        </>
                                    ) : (
                                        <>
                                            <Send size={15} />
                                            <span>Xác nhận ứng tuyển</span>
                                        </>
                                    )}
                                </button>
                            </div>
                        </>
                    )}
                </div>
            </div>
        </div>
    )
}
