import { getAccessToken } from '../auth/tokenStorage'
import type { AssistantApiEnvelope, AssistantReply, AssistantTurn } from './assistantTypes'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || '/api/v1').replace(/\/$/, '')

export class AssistantApiError extends Error {
    readonly status: number

    constructor(message: string, status: number) {
        super(message)
        this.name = 'AssistantApiError'
        this.status = status
    }
}

export async function askTalentBridge(message: string, history: AssistantTurn[]): Promise<AssistantReply> {
    const token = getAccessToken()
    if (!token) throw new AssistantApiError('Đăng nhập để trò chuyện với Trợ lý TalentBridge.', 401)

    const response = await fetch(`${API_BASE_URL}/assistant/chat`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json; charset=UTF-8',
            Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ message, history: history.slice(-8) }),
    })

    let body: AssistantApiEnvelope<AssistantReply> | undefined
    try {
        body = await response.json() as AssistantApiEnvelope<AssistantReply>
    } catch {
        body = undefined
    }
    if (!response.ok) {
        throw new AssistantApiError(body?.message || 'Không thể kết nối Trợ lý TalentBridge.', response.status)
    }
    if (!body?.data) throw new AssistantApiError('Phản hồi chatbot không hợp lệ.', response.status)
    return body.data
}
