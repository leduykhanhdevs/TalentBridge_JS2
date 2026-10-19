import { useState } from 'react'
import { Bot, X } from 'lucide-react'
import { useLocation } from 'react-router'
import { ErrorBoundary } from '../../../components/ErrorBoundary'
import { AssistantChatPanel } from './AssistantChatPanel'

export function TalentBridgeAssistantWidget() {
    const [isOpen, setIsOpen] = useState(false)
    const { pathname } = useLocation()
    const normalizedPathname = pathname.replace(/\/+$/, '') || '/'
    const isAuthenticationPage = [
        '/login',
        '/register',
        '/forgot-password',
        '/reset-password',
    ].includes(normalizedPathname)
    if (normalizedPathname === '/assistant' || isAuthenticationPage) return null

    return (
        <div className="hidden sm:fixed sm:bottom-7 sm:right-7 sm:z-[45] sm:block">
            {isOpen && (
                <div className="mb-3 h-[min(78dvh,43.5rem)] min-h-[28rem] w-[min(92vw,29.75rem)] rounded-xl shadow-[0_12px_32px_rgb(23_32_51_/_14%)]">
                    <ErrorBoundary fallback={(
                        <section className="rounded-xl border border-rose-200 bg-white p-5 text-sm text-slate-700 shadow-xl" role="alert">
                            <p>Trợ lý tạm thời gặp lỗi hiển thị.</p>
                            <button className="mt-3 font-semibold text-blue-700 underline" onClick={() => setIsOpen(false)} type="button">
                                Đóng trợ lý
                            </button>
                        </section>
                    )}>
                        <AssistantChatPanel compact onClose={() => setIsOpen(false)} />
                    </ErrorBoundary>
                </div>
            )}
            <button
                aria-expanded={isOpen}
                aria-label={isOpen ? 'Đóng Trợ lý TalentBridge' : 'Mở nhanh Trợ lý TalentBridge'}
                className="ml-auto grid size-12 place-items-center rounded-full border-2 border-white bg-blue-700 text-white shadow-[0_8px_22px_rgb(23_32_51_/_22%)] transition hover:-translate-y-0.5 hover:bg-blue-800 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-700 sm:size-[52px]"
                onClick={() => setIsOpen((open) => !open)}
                type="button"
            >
                {isOpen ? <X aria-hidden="true" size={23} /> : <Bot aria-hidden="true" size={24} />}
            </button>
        </div>
    )
}
