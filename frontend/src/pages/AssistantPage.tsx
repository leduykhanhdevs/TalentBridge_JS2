import { ArrowLeft, Bot } from 'lucide-react'
import { Link } from 'react-router'
import { AssistantChatPanel } from '../features/assistant/components/AssistantChatPanel'
import { getStoredUser } from '../features/auth/tokenStorage'

export function AssistantPage() {
    const roles = getStoredUser()?.roles || []
    const backTo = roles.includes('ROLE_ADMIN')
        ? '/admin/jobs'
        : roles.includes('ROLE_RECRUITER')
            ? '/recruiter/jobs'
            : '/jobs'

    return (
        <div className="mx-auto max-w-4xl px-4 py-8 sm:px-6">
            <Link className="mb-5 inline-flex items-center gap-2 text-sm font-semibold text-slate-600 hover:text-indigo-700" to={backTo}>
                <ArrowLeft aria-hidden="true" size={16} /> Về TalentBridge
            </Link>
            <div className="mb-5 flex items-center gap-3">
                <span className="grid size-12 place-items-center rounded-2xl bg-indigo-600 text-white"><Bot aria-hidden="true" size={24} /></span>
                <div>
                    <p className="text-xs font-bold uppercase tracking-wider text-indigo-700">Hỗ trợ trong ứng dụng</p>
                    <h1 className="text-2xl font-black text-slate-900">Trợ lý TalentBridge</h1>
                </div>
            </div>
            <AssistantChatPanel />
        </div>
    )
}
