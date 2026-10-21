import type { AdminReportFormat } from '../admin/adminTypes'

function normalizeQuestion(value: string) {
    return value.toLocaleLowerCase('vi').normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/[đĐ]/g, 'd')
}

function containsWholeTerm(question: string, term: string) {
    const pattern = term.split(/\s+/).map((part) => part.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')).join('\\s+')
    return new RegExp(`(^|[^\\p{L}\\p{N}])${pattern}($|[^\\p{L}\\p{N}])`, 'u').test(question)
}

export function getAdminReportFormat(question: string, adminMode: boolean): AdminReportFormat | null {
    if (!adminMode) return null

    const normalizedQuestion = normalizeQuestion(question)
    if (['excel', 'xlsx', 'bang tinh'].some((term) => containsWholeTerm(normalizedQuestion, term))) return 'xlsx'
    if (['word', 'docx', 'van ban', 'tai lieu'].some((term) => containsWholeTerm(normalizedQuestion, term))) return 'docx'

    const questionWithDiacritics = question.toLocaleLowerCase('vi').normalize('NFC')
    const hasExplicitImageWord = /(^|[^\p{L}\p{N}])ảnh([^\p{L}\p{N}]|$)/u.test(questionWithDiacritics)
    if (hasExplicitImageWord || ['png', 'bieu do', 'chart'].some((term) => containsWholeTerm(normalizedQuestion, term))) return 'png'

    return null
}
