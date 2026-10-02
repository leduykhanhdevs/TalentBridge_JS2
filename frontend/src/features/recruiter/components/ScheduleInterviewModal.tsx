import { useState } from 'react'
import {
    Clock,
    Video,
    MapPin,
    FileText,
    ExternalLink,
    Download,
    CheckCircle2,
    AlertCircle,
    X,
    Loader2,
    CalendarCheck2
} from 'lucide-react'
import { scheduleApplicantInterview } from '../recruiterApplicantApi'
import type { InterviewItem, JobApplicant } from '../recruiterApplicantTypes'

interface ScheduleInterviewModalProps {
    isOpen: boolean
    onClose: () => void
    jobId: number
    applicant: JobApplicant
    onSuccess: (interview: InterviewItem) => void
}

const API_BASE_URL = (
    import.meta.env.VITE_API_BASE_URL || '/api/v1'
).replace(/\/$/, '')

export function ScheduleInterviewModal({
    isOpen,
    onClose,
    jobId,
    applicant,
    onSuccess
}: ScheduleInterviewModalProps) {
    // Default time: tomorrow at 09:00 AM
    const tomorrow = new Date()
    tomorrow.setDate(tomorrow.getDate() + 1)
    tomorrow.setHours(9, 0, 0, 0)
    const defaultTimeStr = tomorrow.toISOString().slice(0, 16)

    const [interviewTime, setInterviewTime] = useState(defaultTimeStr)
    const [locationType, setLocationType] = useState<'ONLINE' | 'OFFLINE'>('ONLINE')
    const [meetingLinkOrAddress, setMeetingLinkOrAddress] = useState('https://meet.google.com/new')
    const [notes, setNotes] = useState('')
    const [isSubmitting, setIsSubmitting] = useState(false)
    const [errorMessage, setErrorMessage] = useState<string | null>(null)
    const [createdInterview, setCreatedInterview] = useState<InterviewItem | null>(null)

    if (!isOpen) return null

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault()
        setErrorMessage(null)

        if (!interviewTime) {
            setErrorMessage('Vui lòng chọn thời gian phỏng vấn')
            return
        }

        if (!meetingLinkOrAddress.trim()) {
            setErrorMessage(
                locationType === 'ONLINE'
                    ? 'Vui lòng nhập đường dẫn phòng họp (Google Meet / Zoom)'
                    : 'Vui lòng nhập địa chỉ phỏng vấn trực tiếp'
            )
            return
        }

        setIsSubmitting(true)
        try {
            const res = await scheduleApplicantInterview(jobId, applicant.id, {
                interviewTime,
                locationType,
                meetingLinkOrAddress: meetingLinkOrAddress.trim(),
                notes: notes.trim() || undefined
            })

            setCreatedInterview(res)
            onSuccess(res)
        } catch (err: unknown) {
            const msg = err instanceof Error ? err.message : 'Không thể lên lịch phỏng vấn'
            setErrorMessage(msg)
        } finally {
            setIsSubmitting(false)
        }
    }

    const handleDownloadIcs = () => {
        if (!createdInterview) return
        const icsUrl = `${API_BASE_URL}/interviews/${createdInterview.id}/calendar.ics`
        window.open(icsUrl, '_blank')
    }

    const handleOpenGoogleCalendar = () => {
        if (!createdInterview?.googleCalendarUrl) return
        window.open(createdInterview.googleCalendarUrl, '_blank')
    }

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-in fade-in duration-200">
            <div className="relative w-full max-w-lg rounded-3xl bg-white shadow-2xl border border-slate-200 overflow-hidden flex flex-col max-h-[92vh]">
                {/* Header */}
                <div className="flex items-center justify-between border-b border-slate-100 px-6 py-4 bg-gradient-to-r from-blue-50/50 to-indigo-50/50">
                    <div className="flex items-center gap-3">
                        <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-600 text-white shadow-sm">
                            <CalendarCheck2 className="h-5 w-5" />
                        </div>
                        <div>
                            <h2 className="text-base font-bold text-slate-900">
                                {createdInterview ? 'Đã xếp lịch phỏng vấn' : 'Lên lịch phỏng vấn ứng viên'}
                            </h2>
                            <p className="text-xs text-slate-500">
                                {applicant.candidateFullName} &bull; {applicant.candidateEmail}
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

                {/* Body */}
                <div className="flex-1 overflow-y-auto p-6 space-y-5">
                    {errorMessage && (
                        <div className="flex items-start gap-2.5 rounded-2xl border border-rose-200 bg-rose-50 p-4 text-xs font-medium text-rose-800">
                            <AlertCircle className="h-4 w-4 shrink-0 text-rose-600 mt-0.5" />
                            <span>{errorMessage}</span>
                        </div>
                    )}

                    {createdInterview ? (
                        /* SUCCESS STATE */
                        <div className="space-y-6 text-center py-2 animate-in fade-in slide-in-from-bottom-2">
                            <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-2xl bg-emerald-100 text-emerald-600">
                                <CheckCircle2 className="h-8 w-8" />
                            </div>

                            <div>
                                <h3 className="text-lg font-bold text-slate-900">
                                    Lịch phỏng vấn đã được xác nhận!
                                </h3>
                                <p className="text-xs text-slate-500 mt-1 max-w-sm mx-auto">
                                    Hồ sơ ứng viên đã được tự động chuyển sang trạng thái <strong>Phỏng vấn (INTERVIEW)</strong>.
                                </p>
                            </div>

                            {/* Details Summary */}
                            <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4 text-left text-xs space-y-2.5">
                                <div className="flex items-center justify-between">
                                    <span className="text-slate-500 font-medium">Thời gian:</span>
                                    <span className="font-bold text-slate-800">
                                        {new Date(createdInterview.interviewTime).toLocaleString('vi-VN')}
                                    </span>
                                </div>
                                <div className="flex items-center justify-between">
                                    <span className="text-slate-500 font-medium">Hình thức:</span>
                                    <span className="font-semibold text-blue-700">
                                        {createdInterview.locationType === 'ONLINE' ? 'Trực tuyến (Online)' : 'Trực tiếp (Tại văn phòng)'}
                                    </span>
                                </div>
                                <div className="flex items-start justify-between gap-2">
                                    <span className="text-slate-500 font-medium shrink-0">Địa điểm / Link:</span>
                                    <span className="font-semibold text-slate-800 text-right truncate max-w-[240px]">
                                        {createdInterview.meetingLinkOrAddress}
                                    </span>
                                </div>
                            </div>

                            {/* Calendar Actions */}
                            <div className="space-y-2.5">
                                <button
                                    type="button"
                                    onClick={handleOpenGoogleCalendar}
                                    className="w-full flex items-center justify-center gap-2 rounded-2xl bg-blue-600 px-4 py-3 text-xs font-bold text-white shadow-md hover:bg-blue-700 transition"
                                >
                                    <ExternalLink className="h-4 w-4" />
                                    Mở Google Calendar (Thêm vào lịch 1-Click)
                                </button>

                                <button
                                    type="button"
                                    onClick={handleDownloadIcs}
                                    className="w-full flex items-center justify-center gap-2 rounded-2xl border border-slate-200 bg-white px-4 py-2.5 text-xs font-semibold text-slate-700 hover:bg-slate-50 transition"
                                >
                                    <Download className="h-4 w-4 text-slate-500" />
                                    Tải file lịch iCalendar (.ics)
                                </button>
                            </div>
                        </div>
                    ) : (
                        /* INPUT FORM */
                        <form id="schedule-interview-form" onSubmit={handleSubmit} className="space-y-4">
                            {/* Time Picker */}
                            <div>
                                <label className="block text-xs font-bold text-slate-700 mb-1.5 flex items-center gap-1.5">
                                    <Clock className="h-3.5 w-3.5 text-blue-600" />
                                    Thời gian diễn ra phỏng vấn *
                                </label>
                                <input
                                    type="datetime-local"
                                    value={interviewTime}
                                    onChange={(e) => setInterviewTime(e.target.value)}
                                    className="w-full rounded-xl border border-slate-200 px-3.5 py-2.5 text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500 font-medium"
                                    required
                                />
                            </div>

                            {/* Location Type Selection */}
                            <div>
                                <label className="block text-xs font-bold text-slate-700 mb-1.5">
                                    Hình thức phỏng vấn *
                                </label>
                                <div className="grid grid-cols-2 gap-3">
                                    <button
                                        type="button"
                                        onClick={() => {
                                            setLocationType('ONLINE')
                                            if (!meetingLinkOrAddress.startsWith('http')) {
                                                setMeetingLinkOrAddress('https://meet.google.com/new')
                                            }
                                        }}
                                        className={`flex items-center justify-center gap-2 rounded-xl border p-2.5 text-xs font-semibold transition ${
                                            locationType === 'ONLINE'
                                                ? 'border-blue-600 bg-blue-50 text-blue-700 ring-1 ring-blue-600'
                                                : 'border-slate-200 bg-white text-slate-600 hover:bg-slate-50'
                                        }`}
                                    >
                                        <Video className="h-4 w-4" />
                                        Trực tuyến (Online)
                                    </button>

                                    <button
                                        type="button"
                                        onClick={() => {
                                            setLocationType('OFFLINE')
                                            if (meetingLinkOrAddress.startsWith('http')) {
                                                setMeetingLinkOrAddress('')
                                            }
                                        }}
                                        className={`flex items-center justify-center gap-2 rounded-xl border p-2.5 text-xs font-semibold transition ${
                                            locationType === 'OFFLINE'
                                                ? 'border-blue-600 bg-blue-50 text-blue-700 ring-1 ring-blue-600'
                                                : 'border-slate-200 bg-white text-slate-600 hover:bg-slate-50'
                                        }`}
                                    >
                                        <MapPin className="h-4 w-4" />
                                        Trực tiếp (Tại văn phòng)
                                    </button>
                                </div>
                            </div>

                            {/* Meeting Link or Address */}
                            <div>
                                <label className="block text-xs font-bold text-slate-700 mb-1.5 flex items-center gap-1.5">
                                    {locationType === 'ONLINE' ? (
                                        <>
                                            <Video className="h-3.5 w-3.5 text-blue-600" />
                                            Đường link phòng họp (Google Meet / Zoom / Teams) *
                                        </>
                                    ) : (
                                        <>
                                            <MapPin className="h-3.5 w-3.5 text-blue-600" />
                                            Địa chỉ văn phòng / Phòng họp cụ thể *
                                        </>
                                    )}
                                </label>
                                <input
                                    type="text"
                                    value={meetingLinkOrAddress}
                                    onChange={(e) => setMeetingLinkOrAddress(e.target.value)}
                                    className="w-full rounded-xl border border-slate-200 px-3.5 py-2.5 text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500 font-medium"
                                    placeholder={
                                        locationType === 'ONLINE'
                                            ? 'https://meet.google.com/abc-defg-hij'
                                            : 'Tầng 5, Tòa nhà TalentBridge, Quận 1, TP. HCM'
                                    }
                                    required
                                />
                            </div>

                            {/* Notes */}
                            <div>
                                <label className="block text-xs font-bold text-slate-700 mb-1.5 flex items-center gap-1.5">
                                    <FileText className="h-3.5 w-3.5 text-slate-400" />
                                    Ghi chú dặn dò ứng viên (Tùy chọn)
                                </label>
                                <textarea
                                    rows={3}
                                    value={notes}
                                    onChange={(e) => setNotes(e.target.value)}
                                    className="w-full rounded-xl border border-slate-200 px-3.5 py-2 text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500"
                                    placeholder="Ví dụ: Vui lòng mang theo laptop và chuẩn bị bài test kỹ thuật 30 phút..."
                                />
                            </div>
                        </form>
                    )}
                </div>

                {/* Footer */}
                <div className="flex items-center justify-end gap-2 border-t border-slate-100 bg-slate-50/60 px-6 py-4">
                    <button
                        type="button"
                        onClick={onClose}
                        className="rounded-xl border border-slate-200 bg-white px-4 py-2 text-xs font-semibold text-slate-700 hover:bg-slate-50 transition"
                    >
                        {createdInterview ? 'Đóng' : 'Hủy'}
                    </button>

                    {!createdInterview && (
                        <button
                            form="schedule-interview-form"
                            type="submit"
                            disabled={isSubmitting}
                            className="inline-flex items-center gap-2 rounded-xl bg-blue-600 px-5 py-2 text-xs font-bold text-white shadow-sm hover:bg-blue-700 disabled:opacity-50 transition"
                        >
                            {isSubmitting ? (
                                <>
                                    <Loader2 className="h-4 w-4 animate-spin" />
                                    Đang tạo lịch...
                                </>
                            ) : (
                                <>
                                    <CalendarCheck2 className="h-4 w-4" />
                                    Xác nhận & Đồng bộ Calendar
                                </>
                            )}
                        </button>
                    )}
                </div>
            </div>
        </div>
    )
}
