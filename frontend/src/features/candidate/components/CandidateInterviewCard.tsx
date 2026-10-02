import { useQuery } from '@tanstack/react-query'
import {
    Clock,
    Video,
    MapPin,
    ExternalLink,
    Download,
    CalendarCheck,
    Loader2,
    FileText
} from 'lucide-react'
import { getCandidateInterview, API_BASE_URL } from '../candidateApi'

interface CandidateInterviewCardProps {
    applicationId: number
}

export function CandidateInterviewCard({ applicationId }: CandidateInterviewCardProps) {
    const { data: interview, isLoading } = useQuery({
        queryKey: ['candidate-interview', applicationId],
        queryFn: () => getCandidateInterview(applicationId),
    })

    if (isLoading) {
        return (
            <div className="mt-3.5 flex items-center gap-2 rounded-2xl border border-purple-100 bg-purple-50/50 p-3 text-xs text-purple-700">
                <Loader2 className="h-4 w-4 animate-spin text-purple-600" />
                <span>Đang tải thông tin lịch phỏng vấn...</span>
            </div>
        )
    }

    if (!interview) {
        return null
    }

    const formattedTime = new Date(interview.interviewTime).toLocaleString('vi-VN', {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    })

    const isOnline = interview.locationType === 'ONLINE'

    const handleDownloadIcs = () => {
        const url = `${API_BASE_URL}/interviews/${interview.id}/calendar.ics`
        window.open(url, '_blank')
    }

    const handleGoogleCalendar = () => {
        if (interview.googleCalendarUrl) {
            window.open(interview.googleCalendarUrl, '_blank')
        }
    }

    return (
        <div className="mt-4 overflow-hidden rounded-2xl border border-purple-200 bg-gradient-to-br from-purple-50/70 via-indigo-50/40 to-white p-4 sm:p-5 shadow-2xs">
            {/* Header */}
            <div className="flex flex-wrap items-center justify-between gap-2 border-b border-purple-100/80 pb-3">
                <div className="flex items-center gap-2">
                    <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-purple-600 text-white shadow-2xs">
                        <CalendarCheck className="h-4 w-4" />
                    </div>
                    <div>
                        <h4 className="text-xs sm:text-sm font-bold text-purple-950">
                            Lịch phỏng vấn đã được xác nhận
                        </h4>
                        <span className="text-[11px] font-medium text-purple-700">
                            Vui lòng kiểm tra thời gian và chuẩn bị tham gia đúng giờ
                        </span>
                    </div>
                </div>

                <span className="rounded-full bg-purple-100 px-2.5 py-0.5 text-[11px] font-bold text-purple-800">
                    {isOnline ? 'Online (Trực tuyến)' : 'Tại văn phòng (Trực tiếp)'}
                </span>
            </div>

            {/* Information Grid */}
            <div className="mt-3.5 grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs">
                {/* Time */}
                <div className="flex items-start gap-2.5 rounded-xl bg-white/80 p-2.5 border border-purple-100">
                    <Clock className="h-4 w-4 text-purple-600 shrink-0 mt-0.5" />
                    <div>
                        <span className="text-[10px] uppercase font-bold text-purple-600 tracking-wider block">
                            Thời gian
                        </span>
                        <span className="font-bold text-slate-800 text-xs sm:text-sm">
                            {formattedTime}
                        </span>
                    </div>
                </div>

                {/* Location / Meeting link */}
                <div className="flex items-start gap-2.5 rounded-xl bg-white/80 p-2.5 border border-purple-100">
                    {isOnline ? (
                        <Video className="h-4 w-4 text-purple-600 shrink-0 mt-0.5" />
                    ) : (
                        <MapPin className="h-4 w-4 text-purple-600 shrink-0 mt-0.5" />
                    )}
                    <div className="min-w-0">
                        <span className="text-[10px] uppercase font-bold text-purple-600 tracking-wider block">
                            {isOnline ? 'Phòng họp trực tuyến' : 'Địa điểm'}
                        </span>
                        {isOnline && interview.meetingLinkOrAddress.startsWith('http') ? (
                            <a
                                href={interview.meetingLinkOrAddress}
                                target="_blank"
                                rel="noreferrer"
                                className="font-bold text-blue-600 hover:underline truncate block"
                            >
                                {interview.meetingLinkOrAddress}
                            </a>
                        ) : (
                            <span className="font-semibold text-slate-800 break-words">
                                {interview.meetingLinkOrAddress}
                            </span>
                        )}
                    </div>
                </div>
            </div>

            {/* Notes if present */}
            {interview.notes && (
                <div className="mt-3 flex items-start gap-2 rounded-xl bg-white/80 p-2.5 border border-purple-100 text-xs">
                    <FileText className="h-3.5 w-3.5 text-purple-600 shrink-0 mt-0.5" />
                    <div>
                        <span className="font-bold text-purple-900">Ghi chú từ Nhà tuyển dụng: </span>
                        <span className="text-slate-700">{interview.notes}</span>
                    </div>
                </div>
            )}

            {/* Calendar Actions */}
            <div className="mt-3.5 flex flex-wrap items-center gap-2 pt-2 border-t border-purple-100/60">
                <button
                    type="button"
                    onClick={handleGoogleCalendar}
                    className="inline-flex items-center gap-1.5 rounded-xl bg-purple-600 px-3.5 py-1.5 text-xs font-bold text-white shadow-2xs hover:bg-purple-700 transition"
                >
                    <ExternalLink className="h-3.5 w-3.5" />
                    Thêm vào Google Calendar (1-Click)
                </button>

                <button
                    type="button"
                    onClick={handleDownloadIcs}
                    className="inline-flex items-center gap-1.5 rounded-xl border border-purple-200 bg-white px-3 py-1.5 text-xs font-semibold text-purple-800 hover:bg-purple-50 transition"
                >
                    <Download className="h-3.5 w-3.5 text-purple-600" />
                    Tải file lịch iCalendar (.ics)
                </button>
            </div>
        </div>
    )
}
