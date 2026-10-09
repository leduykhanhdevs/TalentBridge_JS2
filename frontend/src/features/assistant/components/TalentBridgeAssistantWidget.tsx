import { useState } from 'react'
import { Bot, X } from 'lucide-react'
import { useLocation } from 'react-router'
import { AssistantChatPanel } from './AssistantChatPanel'

export function TalentBridgeAssistantWidget() {
    const [isOpen, setIsOpen] = useState(false)
    const { pathname } = useLocation()
    if (pathname === '/assistant') return null

    return (
        <div className="fixed bottom-5 right-5 z-[60] sm:bottom-7 sm:right-7">
            {isOpen && (
                <div className="mb-3 h-[min(72vh,38rem)] w-[min(92vw,25rem)] drop-shadow-2xl">
                    <AssistantChatPanel compact onClose={() => setIsOpen(false)} />
                </div>
            )}
            <button
                aria-expanded={isOpen}
                aria-label={isOpen ? 'Đóng Trợ lý TalentBridge' : 'Mở nhanh Trợ lý TalentBridge'}
                className="ml-auto grid size-14 place-items-center rounded-full border-4 border-white bg-indigo-600 text-white shadow-xl transition hover:scale-105 hover:bg-indigo-700 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-700"
                onClick={() => setIsOpen((open) => !open)}
                type="button"
            >
                {isOpen ? <X aria-hidden="true" size={23} /> : <Bot aria-hidden="true" size={24} />}
            </button>
        </div>
    )
}
