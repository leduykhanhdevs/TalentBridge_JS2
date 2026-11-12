const PENDING_ADMIN_TERMS = [
    'cho duyet',
    'cho phe duyet',
    'can duyet',
    'cho xu ly',
    'can xu ly',
    'chua duyet',
]

export function isPendingAdminQuestion(normalizedQuestion: string): boolean {
    return PENDING_ADMIN_TERMS.some((term) => normalizedQuestion.includes(term))
}
