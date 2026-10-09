export type AssistantRole = 'USER' | 'ASSISTANT'

export interface AssistantTurn {
    role: AssistantRole
    content: string
}

export interface AssistantReply {
    answer: string
    source: 'GEMINI' | 'KNOWLEDGE_BASE' | string
    references: string[]
}

export interface AssistantApiEnvelope<T> {
    statusCode: number
    message: string
    data: T
}
