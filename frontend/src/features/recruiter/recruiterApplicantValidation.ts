import type { AddNotePayload, UpdateStagePayload } from './recruiterApplicantTypes'

export interface ValidationError {
    field: string
    message: string
}

export const VALID_STAGES = [
    'APPLIED',
    'REVIEWING',
    'SHORTLISTED',
    'INTERVIEW',
    'OFFERED',
    'HIRED',
    'REJECTED',
] as const

export function validateStageTransition(payload: UpdateStagePayload): ValidationError[] {
    const errors: ValidationError[] = []

    if (!payload.stage || !payload.stage.trim()) {
        errors.push({ field: 'stage', message: 'Vòng tuyển dụng không được để trống' })
    } else if (!(VALID_STAGES as readonly string[]).includes(payload.stage.trim().toUpperCase())) {
        errors.push({ field: 'stage', message: `Vòng tuyển dụng '${payload.stage}' không hợp lệ` })
    }

    if (payload.note && payload.note.length > 1000) {
        errors.push({ field: 'note', message: 'Ghi chú lý do chuyển vòng không được vượt quá 1000 ký tự' })
    }

    return errors
}

export function validateApplicantNote(payload: AddNotePayload): ValidationError[] {
    const errors: ValidationError[] = []

    if (!payload.comment || !payload.comment.trim()) {
        errors.push({ field: 'comment', message: 'Nội dung ghi chú không được để trống' })
    } else if (payload.comment.length > 2000) {
        errors.push({ field: 'comment', message: 'Nội dung ghi chú không được vượt quá 2000 ký tự' })
    }

    if (payload.rating !== undefined && payload.rating !== null) {
        if (!Number.isInteger(payload.rating) || payload.rating < 1 || payload.rating > 5) {
            errors.push({ field: 'rating', message: 'Điểm đánh giá phải là số nguyên từ 1 đến 5 sao' })
        }
    }

    if (payload.tag && payload.tag.trim().length > 50) {
        errors.push({ field: 'tag', message: 'Nhãn tag không được vượt quá 50 ký tự' })
    }

    return errors
}

export function formatMatchScore(score?: number): {
    percentage: number
    label: string
    colorClass: string
} {
    if (score === undefined || score === null || isNaN(score)) {
        return { percentage: 0, label: 'Chưa tính toán', colorClass: 'text-slate-400 bg-slate-100' }
    }

    const rounded = Math.min(100, Math.max(0, Math.round(score)))
    if (rounded >= 80) {
        return { percentage: rounded, label: 'Rất phù hợp', colorClass: 'text-emerald-700 bg-emerald-50 border-emerald-200' }
    }
    if (rounded >= 60) {
        return { percentage: rounded, label: 'Phù hợp', colorClass: 'text-blue-700 bg-blue-50 border-blue-200' }
    }
    if (rounded >= 40) {
        return { percentage: rounded, label: 'Cân nhắc', colorClass: 'text-amber-700 bg-amber-50 border-amber-200' }
    }
    return { percentage: rounded, label: 'Ít phù hợp', colorClass: 'text-rose-700 bg-rose-50 border-rose-200' }
}
