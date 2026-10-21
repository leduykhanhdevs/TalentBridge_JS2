import { useEffect, useRef, useState } from 'react'
import { Bot, LoaderCircle, Send, Sparkles, User } from 'lucide-react'
import { Link } from 'react-router'
import { askTalentBridge } from '../assistantApi'
import type { AssistantTurn } from '../assistantTypes'
import { getAccessToken } from '../../auth/tokenStorage'
import type { AdminApiMetricsResponse, AdminDashboardStatsResponse } from '../../admin/adminTypes'

interface DisplayTurn extends AssistantTurn {
    references?: string[]
    source?: string
}

interface AssistantChatPanelProps {
    compact?: boolean
    onClose?: () => void
    adminMode?: boolean
    adminStats?: AdminDashboardStatsResponse | null
    adminMetrics?: AdminApiMetricsResponse | null
}

const STARTER_TURN: DisplayTurn = {
    role: 'ASSISTANT',
    content: 'Xin chào! Mình có thể hướng dẫn các chức năng và quy trình trong TalentBridge. Mình không xem dữ liệu riêng hoặc thay bạn thao tác trên hồ sơ.',
}

const ADMIN_STARTER_TURN: DisplayTurn = {
    role: 'ASSISTANT',
    content: 'Xin chào! Tôi là Trợ lý quản trị. Tôi có thể tóm tắt số liệu hệ thống, chỉ ra mục cần xử lý và hướng dẫn bạn tới đúng màn quản lý. Các thao tác duyệt hoặc thay đổi tài khoản vẫn được thực hiện trong màn quản trị tương ứng.',
}

const ADMIN_STARTER_PROMPTS = [
    'Tóm tắt số liệu quản trị hiện tại',
    'Có doanh nghiệp hoặc tin nào cần duyệt?',
    'Hướng dẫn duyệt doanh nghiệp',
    'Cách xem lỗi và độ trễ API?',
]

function normalizeQuestion(value: string) {
    return value.toLocaleLowerCase('vi').normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/[đĐ]/g, 'd')
}

export function AssistantChatPanel({
    compact = false,
    onClose,
    adminMode = false,
    adminStats = null,
    adminMetrics = null,
}: AssistantChatPanelProps) {
    const [authenticated, setAuthenticated] = useState(() => Boolean(getAccessToken()))
    const [turns, setTurns] = useState<DisplayTurn[]>([adminMode ? ADMIN_STARTER_TURN : STARTER_TURN])
    const [message, setMessage] = useState('')
    const [error, setError] = useState<string | null>(null)
    const [isSending, setIsSending] = useState(false)
    const bottomRef = useRef<HTMLDivElement>(null)

    useEffect(() => {
        function syncAuthentication() {
            setAuthenticated(Boolean(getAccessToken()))
        }
        window.addEventListener('talentbridge_auth_change', syncAuthentication)
        window.addEventListener('storage', syncAuthentication)
        return () => {
            window.removeEventListener('talentbridge_auth_change', syncAuthentication)
            window.removeEventListener('storage', syncAuthentication)
        }
    }, [])

    useEffect(() => {
        const bottomElement = bottomRef.current
        if (!bottomElement || typeof bottomElement.scrollIntoView !== 'function') return

        try {
            bottomElement.scrollIntoView({ behavior: 'smooth', block: 'end' })
        } catch {
            const scrollContainer = bottomElement.parentElement
            if (scrollContainer) scrollContainer.scrollTop = scrollContainer.scrollHeight
        }
    }, [turns, isSending])

    async function sendMessage(text = message) {
        const question = text.trim()
        if (!question || isSending) return
        const history: AssistantTurn[] = turns.slice(-8).map(({ role, content }) => ({ role, content }))
        setTurns((current) => [...current, { role: 'USER', content: question }])
        setMessage('')
        setError(null)
        setIsSending(true)
        try {
            const normalizedQuestion = normalizeQuestion(question)
            const overviewIntent = ['thong ke', 'tong quan', 'bao nhieu', 'so lieu', 'bao cao', 'tinh hinh']
                .some((term) => normalizedQuestion.includes(term))
            const pendingIntent = ['cho duyet', 'can duyet', 'cho xu ly', 'can xu ly', 'chua duyet']
                .some((term) => normalizedQuestion.includes(term))
            const apiIntent = ['api', 'do tre', 'latency', 'loi'].some((term) => normalizedQuestion.includes(term))
            let adminAnswer: string | null = null

            if (adminMode && overviewIntent) {
                adminAnswer = adminStats
                    ? `Tổng quan hệ thống theo số liệu vừa tải:\n• ${adminStats.totalUsers.toLocaleString('vi-VN')} tài khoản\n• ${adminStats.totalCompanies.toLocaleString('vi-VN')} doanh nghiệp, ${adminStats.pendingCompanies.toLocaleString('vi-VN')} đang chờ duyệt\n• ${adminStats.totalJobs.toLocaleString('vi-VN')} tin tuyển dụng, ${adminStats.activeJobs.toLocaleString('vi-VN')} đang hoạt động và ${adminStats.pendingJobs.toLocaleString('vi-VN')} chờ kiểm duyệt.\n\nBạn có thể mở mục Doanh nghiệp hoặc Tin tuyển dụng để xử lý hàng chờ.`
                    : 'Chưa tải được số liệu quản trị. Hãy dùng nút “Cập nhật số liệu” phía trên rồi hỏi lại.'
            } else if (adminMode && pendingIntent) {
                adminAnswer = adminStats
                    ? `Hàng chờ hiện tại:\n• ${adminStats.pendingCompanies.toLocaleString('vi-VN')} doanh nghiệp chờ duyệt.\n• ${adminStats.pendingJobs.toLocaleString('vi-VN')} tin tuyển dụng chờ kiểm duyệt.\n\nMở “Doanh nghiệp” hoặc “Tin tuyển dụng” để xem danh sách và quyết định.`
                    : 'Chưa tải được số liệu hàng chờ. Hãy dùng nút “Cập nhật số liệu” phía trên rồi hỏi lại.'
            } else if (adminMode && apiIntent) {
                adminAnswer = adminMetrics
                    ? `Thống kê API trong ${adminMetrics.retentionHours} giờ dữ liệu đang lưu:\n• ${adminMetrics.requestCount.toLocaleString('vi-VN')} yêu cầu\n• ${(adminMetrics.clientErrorCount + adminMetrics.serverErrorCount).toLocaleString('vi-VN')} lỗi (${adminMetrics.errorRatePercent.toLocaleString('vi-VN')}%)\n• Độ trễ trung bình ${adminMetrics.averageLatencyMs.toLocaleString('vi-VN')} ms; cao nhất ${adminMetrics.maxLatencyMs.toLocaleString('vi-VN')} ms.\n\nMở “Thống kê API” để xem từng endpoint. ${adminMetrics.resetsOnRestart ? 'Bộ đếm có thể được đặt lại khi máy chủ khởi động lại.' : ''}`
                    : 'Chưa tải được thống kê API. Hãy dùng nút “Cập nhật số liệu” phía trên rồi hỏi lại.'
            }

            if (adminAnswer !== null) {
                setTurns((current) => [...current, { role: 'ASSISTANT', content: adminAnswer }])
            } else {
                const result = await askTalentBridge(question, history)
                setTurns((current) => [...current, {
                    role: 'ASSISTANT',
                    content: result.answer,
                    references: result.references,
                    source: result.source,
                }])
            }
        } catch (caught) {
            setError(caught instanceof Error ? caught.message : 'Không thể gửi câu hỏi lúc này.')
        } finally {
            setIsSending(false)
        }
    }

    return (
        <section className={`flex min-h-0 flex-col overflow-hidden bg-white ${compact ? 'h-full rounded-xl' : 'h-[min(62dvh,42rem)] min-h-[26rem] rounded-xl border border-slate-200 shadow-sm sm:h-[min(68vh,42rem)] sm:min-h-[34rem]'}`} aria-label={adminMode ? 'Trợ lý quản trị' : 'Trợ lý TalentBridge'}>
            <header className="flex items-center justify-between gap-3 border-b border-slate-200 bg-blue-50/70 px-4 py-3">
                <div className="flex min-w-0 items-center gap-3">
                    <span className="grid size-10 shrink-0 place-items-center rounded-lg bg-blue-700 text-white"><Bot aria-hidden="true" size={20} /></span>
                    <div className="min-w-0">
                        <h2 className="truncate text-sm font-bold text-slate-900">{adminMode ? 'Trợ lý quản trị' : 'Trợ lý TalentBridge'}</h2>
                        <p className="text-xs text-slate-600">{adminMode ? 'Thống kê và hỗ trợ quản lý hệ thống' : 'Hướng dẫn sử dụng trong ứng dụng'}</p>
                    </div>
                </div>
                {onClose && <button className="rounded-lg px-2.5 py-1.5 text-xs font-semibold text-slate-600 transition hover:bg-white hover:text-slate-900" onClick={onClose} type="button">Đóng</button>}
            </header>

            <div className="min-h-0 flex-1 space-y-4 overflow-y-auto bg-[#fbfcfe] p-4 sm:p-5" aria-live="polite">
                <p className="rounded-lg border border-blue-100 bg-white p-3 text-xs leading-5 text-slate-600">
                    {adminMode
                        ? 'Số liệu quản trị được lấy từ API phân quyền và các câu trả lời thống kê được tạo tại chỗ. Câu hỏi về quy trình mới dùng trợ lý kiến thức; không tự duyệt hoặc đổi trạng thái dữ liệu.'
                        : 'Chỉ hỗ trợ quy trình TalentBridge. Không gửi mật khẩu, mã xác minh hay dữ liệu nhạy cảm.'}
                </p>
                {!authenticated && <p className="rounded-xl border border-amber-200 bg-amber-50 p-3 text-xs leading-5 text-amber-900">Đăng nhập để trò chuyện với trợ lý. <Link className="font-bold underline" to="/login">Đăng nhập</Link></p>}
                {turns.map((turn, index) => (
                    <article className={`flex gap-2.5 ${turn.role === 'USER' ? 'flex-row-reverse' : ''}`} key={`${turn.role}-${index}`}>
                        <span className={`grid size-8 shrink-0 place-items-center rounded-full ${turn.role === 'USER' ? 'bg-slate-200 text-slate-700' : 'bg-blue-100 text-blue-700'}`}>
                            {turn.role === 'USER' ? <User aria-hidden="true" size={15} /> : <Bot aria-hidden="true" size={15} />}
                        </span>
                        <div className={`max-w-[85%] rounded-xl px-3.5 py-3 text-sm leading-6 ${turn.role === 'USER' ? 'bg-blue-700 text-white' : 'border border-slate-200 bg-white text-slate-800'}`}>
                            <p className="whitespace-pre-wrap">{turn.content}</p>
                            {turn.references && turn.references.length > 0 && (
                                <p className="mt-2 border-t border-slate-100 pt-2 text-[11px] leading-5 text-slate-500">
                                    Căn cứ: {turn.references.join(' · ')}{turn.source === 'KNOWLEDGE_BASE' ? ' · Hướng dẫn nội bộ' : ''}
                                </p>
                            )}
                        </div>
                    </article>
                ))}
                {authenticated && turns.length === 1 && (
                    <div className="flex flex-wrap gap-2 pl-10">
                        {(adminMode ? ADMIN_STARTER_PROMPTS : ['Làm sao để ứng tuyển?', 'Tạo tin tuyển dụng thế nào?', 'Quên mật khẩu phải làm gì?']).map((prompt) => (
                            <button className="rounded-full border border-blue-200 bg-white px-3 py-2 text-xs font-semibold text-blue-700 transition hover:bg-blue-50 sm:text-sm" key={prompt} onClick={() => void sendMessage(prompt)} type="button">
                                <Sparkles aria-hidden="true" className="mr-1 inline" size={12} />{prompt}
                            </button>
                        ))}
                    </div>
                )}
                {isSending && <div className="flex items-center gap-2 pl-10 text-xs text-slate-500" role="status"><LoaderCircle aria-hidden="true" className="animate-spin" size={14} />Đang tìm hướng dẫn phù hợp...</div>}
                <div ref={bottomRef} />
            </div>

            {error && (
                <div className="border-t border-rose-100 bg-rose-50 px-4 py-3 text-xs text-rose-800" role="alert">
                    {error}{error.includes('Đăng nhập') && <Link className="ml-1 font-bold underline" to="/login">Đăng nhập</Link>}
                </div>
            )}
            <form className="flex items-end gap-2 border-t border-slate-200 bg-white p-3" onSubmit={(event) => { event.preventDefault(); void sendMessage() }}>
                <label className="sr-only" htmlFor="assistant-message">Câu hỏi về TalentBridge</label>
                <textarea
                    className="max-h-28 min-h-11 flex-1 resize-y rounded-lg border border-slate-200 px-3 py-2.5 text-sm outline-none placeholder:text-slate-400 focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                    id="assistant-message"
                    disabled={!authenticated || isSending}
                    maxLength={1000}
                    onChange={(event) => setMessage(event.target.value)}
                    placeholder={adminMode ? 'Hỏi về thống kê hoặc quản lý hệ thống...' : 'Hỏi về cách dùng TalentBridge...'}
                    rows={1}
                    value={message}
                />
                <button aria-label="Gửi câu hỏi" className="grid size-11 shrink-0 place-items-center rounded-lg bg-blue-700 text-white transition hover:bg-blue-800 disabled:cursor-not-allowed disabled:opacity-50" disabled={!authenticated || !message.trim() || isSending} type="submit">
                    {isSending ? <LoaderCircle aria-hidden="true" className="animate-spin" size={17} /> : <Send aria-hidden="true" size={17} />}
                </button>
            </form>
        </section>
    )
}
