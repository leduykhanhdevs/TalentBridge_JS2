import type { AdminReportFormat } from '../admin/adminTypes'

function normalizeQuestion(value: string) {
    return value.toLocaleLowerCase('vi').normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/[đĐ]/g, 'd')
}

export function getAdminReportFormat(question: string, adminMode: boolean): AdminReportFormat | null {
    if (!adminMode) return null

    const normalizedQuestion = normalizeQuestion(question)
    if (['excel', 'xlsx', 'bang tinh'].some((term) => normalizedQuestion.includes(term))) return 'xlsx'
    if (['word', 'docx', 'van ban', 'tai lieu'].some((term) => normalizedQuestion.includes(term))) return 'docx'

    const questionWithDiacritics = question.toLocaleLowerCase('vi').normalize('NFC')
    const hasExplicitImageWord = /(^|[^\p{L}\p{N}])ảnh([^\p{L}\p{N}]|$)/u.test(questionWithDiacritics)
    if (hasExplicitImageWord || ['png', 'bieu do', 'chart'].some((term) => normalizedQuestion.includes(term))) return 'png'

    return null
}
