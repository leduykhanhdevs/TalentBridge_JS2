const JOB_TYPE_LABELS: Record<string, string> = {
    FULL_TIME: 'Toàn thời gian',
    PART_TIME: 'Bán thời gian',
    CONTRACT: 'Hợp đồng',
    INTERNSHIP: 'Thực tập',
    REMOTE: 'Làm việc từ xa',
}

const EXPERIENCE_LEVEL_LABELS: Record<string, string> = {
    INTERN: 'Thực tập sinh',
    FRESHER: 'Fresher',
    JUNIOR: 'Junior',
    MIDDLE: 'Middle',
    MID_LEVEL: 'Middle',
    SENIOR: 'Senior',
    LEAD: 'Trưởng nhóm',
    MANAGER: 'Quản lý',
}

export function formatJobType(value: string | null): string {
    if (!value) return 'Chưa cập nhật'
    return JOB_TYPE_LABELS[value] || value.replaceAll('_', ' ')
}

export function formatExperienceLevel(value: string | null): string {
    if (!value) return 'Không yêu cầu'
    return EXPERIENCE_LEVEL_LABELS[value] || value.replaceAll('_', ' ')
}

export function formatSalaryRange(
    minSalary: number | null,
    maxSalary: number | null,
): string {
    if (minSalary === null && maxSalary === null) {
        return 'Thỏa thuận'
    }

    const formatMillions = (amount: number) => {
        const millions = amount / 1_000_000
        return new Intl.NumberFormat('vi-VN', {
            maximumFractionDigits: 1,
        }).format(millions)
    }

    if (minSalary !== null && maxSalary !== null) {
        return `${formatMillions(minSalary)} - ${formatMillions(maxSalary)} triệu`
    }

    if (minSalary !== null) {
        return `Từ ${formatMillions(minSalary)} triệu`
    }

    return `Đến ${formatMillions(maxSalary as number)} triệu`
}

export function formatDate(value: string | null): string {
    if (!value) return 'Chưa cập nhật'

    const date = new Date(`${value.slice(0, 10)}T00:00:00`)
    if (Number.isNaN(date.getTime())) return 'Chưa cập nhật'

    return new Intl.DateTimeFormat('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
    }).format(date)
}
