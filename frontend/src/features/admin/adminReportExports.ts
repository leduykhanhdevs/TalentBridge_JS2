import { Document, HeadingLevel, Packer, Paragraph, TextRun } from 'docx'
import { strToU8, zipSync } from 'fflate'
import type { AdminApiMetricsResponse, AdminDashboardStatsResponse, AdminReportFormat } from './adminTypes'

export type { AdminReportFormat } from './adminTypes'

export interface AdminReportSnapshot {
    stats: AdminDashboardStatsResponse | null
    metrics: AdminApiMetricsResponse | null
    generatedAt: Date
}
const numberFormat = new Intl.NumberFormat('vi-VN')
const workbookMimeType = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'

function numberOrZero(value: number | undefined): number {
    return Number.isFinite(value) ? value ?? 0 : 0
}

function summaryRows(snapshot: AdminReportSnapshot): (string | number)[][] {
    const { stats, metrics, generatedAt } = snapshot
    const rows: (string | number)[][] = [
        ['Chỉ số', 'Giá trị', 'Ghi chú'],
        ['Thời điểm tạo', generatedAt.toLocaleString('vi-VN'), 'Thời điểm tạo báo cáo trên trình duyệt'],
    ]

    if (stats) {
        rows.push(
            ['Tài khoản', numberOrZero(stats.totalUsers), 'Tổng số người dùng'],
            ['Doanh nghiệp', numberOrZero(stats.totalCompanies), `${numberOrZero(stats.pendingCompanies)} đang chờ duyệt`],
            ['Doanh nghiệp chờ duyệt', numberOrZero(stats.pendingCompanies), 'Lấy từ API quản trị'],
            ['Tin tuyển dụng', numberOrZero(stats.totalJobs), 'Tổng số tin'],
            ['Tin đang hoạt động', numberOrZero(stats.activeJobs), 'Lấy từ API quản trị'],
            ['Tin chờ kiểm duyệt', numberOrZero(stats.pendingJobs), 'Lấy từ API quản trị'],
        )
    }

    if (metrics) {
        rows.push(
            ['Yêu cầu API', numberOrZero(metrics.requestCount), `Cửa sổ tối đa ${metrics.retentionHours} giờ`],
            ['Lỗi API', numberOrZero(metrics.clientErrorCount + metrics.serverErrorCount), `Tỷ lệ lỗi ${numberOrZero(metrics.errorRatePercent)}%`],
            ['Độ trễ API trung bình (ms)', numberOrZero(metrics.averageLatencyMs), 'Độ trễ xử lý phía server'],
            ['Độ trễ API cao nhất (ms)', numberOrZero(metrics.maxLatencyMs), 'Độ trễ xử lý phía server'],
            ['Bucket', numberOrZero(metrics.bucketMinutes), 'Đơn vị phút'],
            ['Thời gian bắt đầu thu thập', metrics.collectedSince, 'Theo API thống kê'],
            ['Dữ liệu mất khi restart', metrics.resetsOnRestart ? 'Có' : 'Không', 'Phạm vi tiến trình hiện tại'],
        )
    }
    return rows
}

export function buildAdminStatsSvg(snapshot: AdminReportSnapshot): string {
    const { stats, metrics, generatedAt } = snapshot
    const bars = [
        { label: 'Tài khoản', value: numberOrZero(stats?.totalUsers), color: '#2563eb' },
        { label: 'Doanh nghiệp', value: numberOrZero(stats?.totalCompanies), color: '#7c3aed' },
        { label: 'Chờ duyệt DN', value: numberOrZero(stats?.pendingCompanies), color: '#d97706' },
        { label: 'Tin tuyển dụng', value: numberOrZero(stats?.totalJobs), color: '#0891b2' },
        { label: 'Tin hoạt động', value: numberOrZero(stats?.activeJobs), color: '#059669' },
        { label: 'Chờ kiểm duyệt', value: numberOrZero(stats?.pendingJobs), color: '#e11d48' },
    ]
    const maximum = Math.max(1, ...bars.map(({ value }) => value))
    const chartTop = 150
    const chartHeight = 250
    const barWidth = 88
    const gap = 56
    const chartLeft = 80
    const barMarkup = bars.map(({ label, value, color }, index) => {
        const height = Math.max(value > 0 ? 6 : 0, (value / maximum) * chartHeight)
        const x = chartLeft + index * (barWidth + gap)
        const y = chartTop + chartHeight - height
        return `<g><text x="${x + barWidth / 2}" y="${y - 12}" text-anchor="middle" fill="#172033" font-size="18" font-weight="700">${numberFormat.format(value)}</text><rect x="${x}" y="${y}" width="${barWidth}" height="${height}" rx="12" fill="${color}"/><text x="${x + barWidth / 2}" y="${chartTop + chartHeight + 30}" text-anchor="middle" fill="#475569" font-size="14">${label}</text></g>`
    }).join('')
    const apiSummary = metrics
        ? `${numberFormat.format(numberOrZero(metrics.requestCount))} yêu cầu API  ·  ${numberFormat.format(numberOrZero(metrics.errorRatePercent))}% lỗi  ·  ${numberFormat.format(numberOrZero(metrics.averageLatencyMs))} ms trung bình`
        : 'Chưa có dữ liệu API trong báo cáo này'

    return `<svg xmlns="http://www.w3.org/2000/svg" width="1200" height="620" viewBox="0 0 1200 620"><rect width="1200" height="620" fill="#f5f7fb"/><rect x="24" y="24" width="1152" height="572" rx="24" fill="#fff" stroke="#dbe3ef"/><text x="64" y="78" fill="#1d4ed8" font-size="15" font-weight="700" letter-spacing="2">TALENTBRIDGE · BÁO CÁO QUẢN TRỊ</text><text x="64" y="122" fill="#111827" font-size="30" font-weight="800">Tổng quan hệ thống</text><text x="64" y="146" fill="#64748b" font-size="13">Tạo lúc ${generatedAt.toLocaleString('vi-VN')}</text><line x1="64" x2="1136" y1="${chartTop + chartHeight}" y2="${chartTop + chartHeight}" stroke="#cbd5e1"/>${barMarkup}<rect x="64" y="468" width="1072" height="84" rx="16" fill="#eff6ff"/><text x="88" y="502" fill="#1e3a8a" font-size="15" font-weight="700">SỐ LIỆU API</text><text x="88" y="529" fill="#334155" font-size="17">${apiSummary}</text><text x="64" y="574" fill="#64748b" font-size="12">Nguồn: API quản trị TalentBridge. Ảnh thể hiện dữ liệu hiện có tại thời điểm xuất.</text></svg>`
}

function escapeXml(value: string): string {
    return value.replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('"', '&quot;').replaceAll("'", '&apos;')
}

function columnName(index: number): string {
    let value = index + 1
    let name = ''
    while (value > 0) {
        const remainder = (value - 1) % 26
        name = String.fromCharCode(65 + remainder) + name
        value = Math.floor((value - 1) / 26)
    }
    return name
}

function worksheetXml(rows: (string | number)[][]): string {
    const sheetRows = rows.map((row, rowIndex) => {
        const cells = row.map((value, columnIndex) => {
            const cellRef = `${columnName(columnIndex)}${rowIndex + 1}`
            if (typeof value === 'number' && Number.isFinite(value)) return `<c r="${cellRef}" t="n"><v>${value}</v></c>`
            return `<c r="${cellRef}" t="inlineStr"><is><t xml:space="preserve">${escapeXml(String(value))}</t></is></c>`
        }).join('')
        return `<row r="${rowIndex + 1}">${cells}</row>`
    }).join('')
    return `<?xml version="1.0" encoding="UTF-8" standalone="yes"?><worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>${sheetRows}</sheetData></worksheet>`
}

function workbookBlob(snapshot: AdminReportSnapshot): Blob {
    const sheets: Array<{ name: string; rows: (string | number)[][] }> = [
        { name: 'Tổng quan', rows: summaryRows(snapshot) },
    ]
    const metrics = snapshot.metrics
    if (metrics) {
        sheets.push({
            name: 'Theo endpoint',
            rows: [
                ['Endpoint', 'Yêu cầu', 'Lỗi 4xx', 'Lỗi 5xx', 'Tỷ lệ lỗi (%)', 'Trễ TB (ms)', 'Trễ cao nhất (ms)'],
                ...metrics.endpoints.map((endpoint) => [
                    endpoint.endpoint,
                    numberOrZero(endpoint.requestCount),
                    numberOrZero(endpoint.clientErrorCount),
                    numberOrZero(endpoint.serverErrorCount),
                    numberOrZero(endpoint.errorRatePercent),
                    numberOrZero(endpoint.averageLatencyMs),
                    numberOrZero(endpoint.maxLatencyMs),
                ]),
            ],
        }, {
            name: 'Bucket 5 phút',
            rows: [
                ['Endpoint', 'Thời điểm bucket', 'Yêu cầu', 'Lỗi 4xx', 'Lỗi 5xx', 'Trễ TB (ms)', 'Trễ cao nhất (ms)'],
                ...metrics.endpoints.flatMap((endpoint) => endpoint.buckets.map((bucket) => [
                    endpoint.endpoint,
                    bucket.startsAt,
                    numberOrZero(bucket.requestCount),
                    numberOrZero(bucket.clientErrorCount),
                    numberOrZero(bucket.serverErrorCount),
                    numberOrZero(bucket.averageLatencyMs),
                    numberOrZero(bucket.maxLatencyMs),
                ])),
            ],
        })
    }

    const files: Record<string, Uint8Array> = {
        '[Content_Types].xml': strToU8(`<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>${sheets.map((_, index) => `<Override PartName="/xl/worksheets/sheet${index + 1}.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>`).join('')}</Types>`),
        '_rels/.rels': strToU8('<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>'),
        'xl/workbook.xml': strToU8(`<?xml version="1.0" encoding="UTF-8" standalone="yes"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets>${sheets.map((sheet, index) => `<sheet name="${escapeXml(sheet.name)}" sheetId="${index + 1}" r:id="rId${index + 1}"/>`).join('')}</sheets></workbook>`),
        'xl/_rels/workbook.xml.rels': strToU8(`<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">${sheets.map((_, index) => `<Relationship Id="rId${index + 1}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet${index + 1}.xml"/>`).join('')}</Relationships>`),
    }
    sheets.forEach((sheet, index) => { files[`xl/worksheets/sheet${index + 1}.xml`] = strToU8(worksheetXml(sheet.rows)) })
    return new Blob([zipSync(files)], { type: workbookMimeType })
}

async function wordBlob(snapshot: AdminReportSnapshot): Promise<Blob> {
    const summary = summaryRows(snapshot).slice(1)
    const endpointParagraphs = snapshot.metrics?.endpoints.map((endpoint) => new Paragraph({
        children: [new TextRun({ text: endpoint.endpoint, bold: true }), new TextRun(` — ${numberFormat.format(endpoint.requestCount)} yêu cầu, ${numberFormat.format(endpoint.errorRatePercent)}% lỗi, trễ TB ${numberFormat.format(endpoint.averageLatencyMs)} ms`)],
        spacing: { after: 120 },
    })) ?? []
    const doc = new Document({
        sections: [{
            children: [
                new Paragraph({ text: 'Báo cáo quản trị TalentBridge', heading: HeadingLevel.TITLE }),
                new Paragraph(`Tạo lúc ${snapshot.generatedAt.toLocaleString('vi-VN')} · Số liệu được đọc từ API quản trị đã xác thực.`),
                new Paragraph({ text: 'Tổng quan', heading: HeadingLevel.HEADING_1 }),
                ...summary.map(([label, value, note]) => new Paragraph({
                    children: [new TextRun({ text: `${label}: `, bold: true }), new TextRun(String(value)), new TextRun({ text: ` (${String(note)})`, color: '64748B' })],
                    bullet: { level: 0 },
                    spacing: { after: 90 },
                })),
                new Paragraph({ text: 'Thống kê theo endpoint', heading: HeadingLevel.HEADING_1 }),
                ...(endpointParagraphs.length > 0 ? endpointParagraphs : [new Paragraph('Không có dữ liệu endpoint trong lần xuất này.')]),
                new Paragraph({ children: [new TextRun({ text: 'Ghi chú: ', bold: true }), new TextRun('Báo cáo chỉ phản ánh số liệu có sẵn tại thời điểm xuất, không phải dự báo hoặc kết quả do AI suy diễn.')], spacing: { before: 240 } }),
            ],
        }],
    })
    return Packer.toBlob(doc)
}

async function pngBlob(snapshot: AdminReportSnapshot): Promise<Blob> {
    const svg = buildAdminStatsSvg(snapshot)
    const svgUrl = URL.createObjectURL(new Blob([svg], { type: 'image/svg+xml;charset=utf-8' }))
    try {
        const image = new Image()
        image.src = svgUrl
        await image.decode()
        const canvas = document.createElement('canvas')
        canvas.width = 1200
        canvas.height = 620
        const context = canvas.getContext('2d')
        if (!context) throw new Error('Trình duyệt không thể tạo ảnh thống kê.')
        context.drawImage(image, 0, 0)
        const blob = await new Promise<Blob | null>((resolve) => canvas.toBlob(resolve, 'image/png'))
        if (!blob) throw new Error('Không thể chuyển biểu đồ thành ảnh PNG.')
        return blob
    } finally {
        URL.revokeObjectURL(svgUrl)
    }
}

export async function createAdminReportFile(format: AdminReportFormat, snapshot: AdminReportSnapshot): Promise<Blob> {
    if (!snapshot.stats && !snapshot.metrics) throw new Error('Chưa tải được số liệu quản trị để tạo báo cáo.')
    switch (format) {
        case 'png': return pngBlob(snapshot)
        case 'xlsx': return workbookBlob(snapshot)
        case 'docx': return wordBlob(snapshot)
    }
}
