const JOB_TYPE_LABELS: Record<string, string> = {
    FULL_TIME: 'Toàn thời gian',
    PART_TIME: 'Bán thời gian',
    REMOTE: 'Làm việc từ xa',
    HYBRID: 'Làm việc kết hợp',
}

const EXPERIENCE_LABELS: Record<string, string> = {
    INTERN: 'Thực tập sinh',
    FRESHER: 'Fresher',
    JUNIOR: 'Junior',
    MIDDLE: 'Middle',
    SENIOR: 'Senior',
}

export function formatJobType(value: string | null): string {
    return value ? JOB_TYPE_LABELS[value] || value.replaceAll('_', ' ') : 'Chưa cập nhật'
}

export function formatExperienceLevel(value: string | null): string {
    return value ? EXPERIENCE_LABELS[value] || value.replaceAll('_', ' ') : 'Không yêu cầu'
}

export function formatSalary(
    minSalary: number | null,
    maxSalary: number | null,
    isNegotiable: boolean | null,
): string {
    if (isNegotiable || (minSalary === null && maxSalary === null)) return 'Thỏa thuận'
    const inMillions = (amount: number) => new Intl.NumberFormat('vi-VN', {
        maximumFractionDigits: 1,
    }).format(amount / 1_000_000)
    if (minSalary !== null && maxSalary !== null) {
        return `${inMillions(minSalary)} - ${inMillions(maxSalary)} triệu VNĐ`
    }
    if (minSalary !== null) return `Từ ${inMillions(minSalary)} triệu VNĐ`
    return `Đến ${inMillions(maxSalary as number)} triệu VNĐ`
}
