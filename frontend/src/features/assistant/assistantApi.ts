import { getAccessToken } from '../auth/tokenStorage'
import type { AssistantApiEnvelope, AssistantReply, AssistantTurn } from './assistantTypes'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || '/api/v1').replace(/\/$/, '')
const REQUEST_TIMEOUT_MS = 15_000

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

    const controller = new AbortController()
    const timeoutId = window.setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS)
    let response: Response
    try {
        response = await fetch(`${API_BASE_URL}/assistant/chat`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json; charset=UTF-8',
                Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify({ message, history: history.slice(-8) }),
            signal: controller.signal,
        })
    } catch {
        if (controller.signal.aborted) {
            throw new AssistantApiError('Trợ lý phản hồi quá chậm. Vui lòng thử lại sau một lát.', 408)
        }
        throw new AssistantApiError('Không thể kết nối Trợ lý TalentBridge. Vui lòng thử lại.', 0)
    } finally {
        window.clearTimeout(timeoutId)
    }

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
