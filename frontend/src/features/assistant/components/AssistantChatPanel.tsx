import { useEffect, useRef, useState } from 'react'
import { Bot, LoaderCircle, Send, Sparkles, User } from 'lucide-react'
import { Link } from 'react-router'
import { askTalentBridge } from '../assistantApi'
import type { AssistantTurn } from '../assistantTypes'
import { getAccessToken } from '../../auth/tokenStorage'

interface DisplayTurn extends AssistantTurn {
    references?: string[]
    source?: string
}

interface AssistantChatPanelProps {
    compact?: boolean
    onClose?: () => void
}

const STARTER_TURN: DisplayTurn = {
    role: 'ASSISTANT',
    content: 'Xin chào! Mình có thể hướng dẫn các chức năng và quy trình trong TalentBridge. Mình không xem dữ liệu riêng hoặc thay bạn thao tác trên hồ sơ.',
}

export function AssistantChatPanel({ compact = false, onClose }: AssistantChatPanelProps) {
    const [authenticated, setAuthenticated] = useState(() => Boolean(getAccessToken()))
    const [turns, setTurns] = useState<DisplayTurn[]>([STARTER_TURN])
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

    useEffect(() => bottomRef.current?.scrollIntoView({ behavior: 'smooth', block: 'end' }), [turns, isSending])

    async function sendMessage(text = message) {
        const question = text.trim()
        if (!question || isSending) return
        const history: AssistantTurn[] = turns.slice(-8).map(({ role, content }) => ({ role, content }))
        setTurns((current) => [...current, { role: 'USER', content: question }])
        setMessage('')
        setError(null)
        setIsSending(true)
        try {
            const result = await askTalentBridge(question, history)
            setTurns((current) => [...current, {
                role: 'ASSISTANT',
                content: result.answer,
                references: result.references,
                source: result.source,
            }])
        } catch (caught) {
            setError(caught instanceof Error ? caught.message : 'Không thể gửi câu hỏi lúc này.')
        } finally {
            setIsSending(false)
        }
    }

    return (
        <section className={`flex min-h-0 flex-col overflow-hidden bg-white ${compact ? 'h-full rounded-2xl' : 'min-h-[65vh] rounded-3xl border border-slate-200 shadow-sm'}`} aria-label="Trợ lý TalentBridge">
            <header className="flex items-center justify-between gap-3 border-b border-slate-100 bg-gradient-to-r from-indigo-50 to-violet-50 px-4 py-3">
                <div className="flex min-w-0 items-center gap-3">
                    <span className="grid size-10 shrink-0 place-items-center rounded-xl bg-indigo-600 text-white"><Bot aria-hidden="true" size={21} /></span>
                    <div className="min-w-0">
                        <h2 className="truncate text-sm font-bold text-slate-900">Trợ lý TalentBridge</h2>
                        <p className="text-xs text-slate-600">Hướng dẫn sử dụng trong ứng dụng</p>
                    </div>
                </div>
                {onClose && <button className="rounded-lg px-2 py-1 text-xs font-semibold text-slate-600 hover:bg-white" onClick={onClose} type="button">Đóng</button>}
            </header>

            <div className="min-h-0 flex-1 space-y-4 overflow-y-auto bg-slate-50/70 p-4" aria-live="polite">
                <p className="rounded-xl border border-indigo-100 bg-white p-3 text-xs leading-5 text-slate-600">
                    Chỉ hỗ trợ quy trình TalentBridge. Không gửi mật khẩu, mã xác minh hay dữ liệu nhạy cảm.
                </p>
                {!authenticated && <p className="rounded-xl border border-amber-200 bg-amber-50 p-3 text-xs leading-5 text-amber-900">Đăng nhập để trò chuyện với trợ lý. <Link className="font-bold underline" to="/login">Đăng nhập</Link></p>}
                {turns.map((turn, index) => (
                    <article className={`flex gap-2.5 ${turn.role === 'USER' ? 'flex-row-reverse' : ''}`} key={`${turn.role}-${index}`}>
                        <span className={`grid size-8 shrink-0 place-items-center rounded-full ${turn.role === 'USER' ? 'bg-slate-200 text-slate-700' : 'bg-indigo-100 text-indigo-700'}`}>
                            {turn.role === 'USER' ? <User aria-hidden="true" size={15} /> : <Bot aria-hidden="true" size={15} />}
                        </span>
                        <div className={`max-w-[85%] rounded-2xl px-3.5 py-3 text-sm leading-6 ${turn.role === 'USER' ? 'bg-indigo-600 text-white' : 'border border-slate-200 bg-white text-slate-800'}`}>
                            <p className="whitespace-pre-wrap">{turn.content}</p>
                            {turn.references && turn.references.length > 0 && (
                                <p className="mt-2 border-t border-slate-100 pt-2 text-[11px] text-slate-500">
                                    Căn cứ: {turn.references.join(' · ')}{turn.source === 'KNOWLEDGE_BASE' ? ' · Hướng dẫn nội bộ' : ''}
                                </p>
                            )}
                        </div>
                    </article>
                ))}
                {authenticated && turns.length === 1 && (
                    <div className="flex flex-wrap gap-2 pl-10">
                        {['Làm sao để ứng tuyển?', 'Tạo tin tuyển dụng thế nào?', 'Quên mật khẩu phải làm gì?'].map((prompt) => (
                            <button className="rounded-full border border-indigo-200 bg-white px-3 py-1.5 text-xs font-semibold text-indigo-700 hover:bg-indigo-50" key={prompt} onClick={() => void sendMessage(prompt)} type="button">
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
                    className="max-h-28 min-h-11 flex-1 resize-y rounded-xl border border-slate-200 px-3 py-2.5 text-sm outline-none placeholder:text-slate-400 focus:border-indigo-400 focus:ring-2 focus:ring-indigo-100"
                    id="assistant-message"
                    disabled={!authenticated || isSending}
                    maxLength={1000}
                    onChange={(event) => setMessage(event.target.value)}
                    placeholder="Hỏi về cách dùng TalentBridge..."
                    rows={1}
                    value={message}
                />
                <button aria-label="Gửi câu hỏi" className="grid size-11 shrink-0 place-items-center rounded-xl bg-indigo-600 text-white transition hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-50" disabled={!authenticated || !message.trim() || isSending} type="submit">
                    {isSending ? <LoaderCircle aria-hidden="true" className="animate-spin" size={17} /> : <Send aria-hidden="true" size={17} />}
                </button>
            </form>
        </section>
    )
}
