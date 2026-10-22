import type { JobApplicant } from './recruiterApplicantTypes'

function csvCell(value: string | number | null | undefined): string {
    const text = value == null ? '' : String(value)
    const safeText = /^[=+\-@\t\r]/.test(text) ? `'${text}` : text
    return `"${safeText.replaceAll('"', '""')}"`
}

export function buildRecruiterApplicantsCsv(applicants: JobApplicant[]): string {
    const rows = [
        ['Ứng viên', 'Email', 'Điện thoại', 'Vị trí', 'Kinh nghiệm (năm)', 'Địa điểm', 'Vòng tuyển dụng', 'Ngày ứng tuyển'],
        ...applicants.map((applicant) => [
            applicant.candidateFullName,
            applicant.candidateEmail,
            applicant.candidatePhone,
            applicant.candidateTitle,
            applicant.candidateExperienceYears,
            applicant.candidateCity,
            applicant.currentStage,
            applicant.appliedAt,
        ]),
    ]
    return `\uFEFF${rows.map((row) => row.map(csvCell).join(',')).join('\r\n')}`
}

export function downloadRecruiterApplicantsCsv(applicants: JobApplicant[], jobTitle: string): void {
    const blob = new Blob([buildRecruiterApplicantsCsv(applicants)], { type: 'text/csv;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const safeTitle = jobTitle.normalize('NFKD').replace(/[^a-zA-Z0-9]+/g, '-').replace(/^-|-$/g, '').toLowerCase() || 'tin-tuyen-dung'
    const link = document.createElement('a')
    link.href = url
    link.download = `TalentBridge-ung-vien-${safeTitle}.csv`
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.setTimeout(() => URL.revokeObjectURL(url), 60_000)
}
