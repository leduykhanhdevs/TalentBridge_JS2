import { afterEach, describe, expect, it, vi } from 'vitest'
import { strFromU8, unzipSync } from 'fflate'
import { buildAdminStatsSvg, createAdminReportFile, type AdminReportSnapshot } from '../adminReportExports'

const snapshot: AdminReportSnapshot = {
    generatedAt: new Date('2026-10-22T08:00:00.000Z'),
    stats: {
        totalUsers: 143,
        totalCompanies: 22,
        pendingCompanies: 3,
        totalJobs: 100,
        activeJobs: 91,
        pendingJobs: 4,
    },
    metrics: {
        generatedAt: '2026-10-22T08:00:00Z',
        collectedSince: '2026-10-21T08:00:00Z',
        from: '2026-10-21T08:00:00Z',
        to: '2026-10-22T08:00:00Z',
        bucketMinutes: 5,
        retentionHours: 24,
        resetsOnRestart: true,
        requestCount: 38,
        clientErrorCount: 2,
        serverErrorCount: 1,
        errorRatePercent: 7.89,
        averageLatencyMs: 76.5,
        maxLatencyMs: 210,
        endpoints: [{
            endpoint: 'GET /api/v1/jobs/{id}',
            requestCount: 12,
            clientErrorCount: 1,
            serverErrorCount: 0,
            errorRatePercent: 8.33,
            averageLatencyMs: 42.5,
            maxLatencyMs: 110,
            buckets: [{
                startsAt: '2026-10-22T07:55:00Z',
                requestCount: 3,
                clientErrorCount: 1,
                serverErrorCount: 0,
                averageLatencyMs: 42.5,
                maxLatencyMs: 110,
            }],
        }],
    },
}

afterEach(() => {
    vi.restoreAllMocks()
    vi.unstubAllGlobals()
})

describe('admin report exports', () => {
    it('creates a statistics image from the authorized live snapshot values', () => {
        const svg = buildAdminStatsSvg(snapshot)
        expect(svg).toContain('<svg')
        expect(svg).toContain('Tổng quan hệ thống')
        expect(svg).toContain('143')
        expect(svg).toContain('38 yêu cầu API')
        expect(svg).not.toContain('undefined')
    })

    it('rasterizes the statistics chart as a downloadable PNG', async () => {
        const drawImage = vi.fn()
        const canvas = {
            width: 0,
            height: 0,
            getContext: vi.fn(() => ({ drawImage })),
            toBlob: (callback: BlobCallback) => callback(new Blob(['png'], { type: 'image/png' })),
        }
        vi.stubGlobal('Image', class { src = ''; decode = async () => undefined })
        vi.stubGlobal('document', { createElement: vi.fn(() => canvas) })
        vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:admin-report')
        const revoke = vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => undefined)

        const blob = await createAdminReportFile('png', snapshot)

        expect(blob.type).toBe('image/png')
        expect(canvas.width).toBe(1200)
        expect(canvas.height).toBe(620)
        expect(drawImage).toHaveBeenCalledOnce()
        expect(revoke).toHaveBeenCalledWith('blob:admin-report')
    })

    it('creates a valid XLSX package with summary, endpoint and time-bucket sheets', async () => {
        const blob = await createAdminReportFile('xlsx', snapshot)
        expect(blob.type).toBe('application/vnd.openxmlformats-officedocument.spreadsheetml.sheet')
        const files = unzipSync(new Uint8Array(await blob.arrayBuffer()))
        const workbook = strFromU8(files['xl/workbook.xml'])
        const endpointSheet = strFromU8(files['xl/worksheets/sheet2.xml'])
        const bucketsSheet = strFromU8(files['xl/worksheets/sheet3.xml'])
        expect(workbook).toContain('Tổng quan')
        expect(workbook).toContain('Theo endpoint')
        expect(workbook).toContain('Bucket 5 phút')
        expect(endpointSheet).toContain('GET /api/v1/jobs/{id}')
        expect(bucketsSheet).toContain('2026-10-22T07:55:00Z')
    })

    it('escapes endpoint text in XLSX XML and treats it only as a literal cell value', async () => {
        const unsafeLabel = '<endpoint>&"'
        const data = { ...snapshot, metrics: { ...snapshot.metrics!, endpoints: [{ ...snapshot.metrics!.endpoints[0], endpoint: unsafeLabel }] } }
        const blob = await createAdminReportFile('xlsx', data)
        const files = unzipSync(new Uint8Array(await blob.arrayBuffer()))
        const endpointSheet = strFromU8(files['xl/worksheets/sheet2.xml'])
        expect(endpointSheet).toContain('&lt;endpoint&gt;&amp;&quot;')
        expect(endpointSheet).not.toContain(unsafeLabel)
        expect(endpointSheet).not.toContain('<endpoint>')
    })

    it('creates an Office Word document package', async () => {
        const blob = await createAdminReportFile('docx', snapshot)
        expect(blob.type).toBe('application/vnd.openxmlformats-officedocument.wordprocessingml.document')
        expect(blob.size).toBeGreaterThan(1000)
        const files = unzipSync(new Uint8Array(await blob.arrayBuffer()))
        const document = strFromU8(files['word/document.xml'])
        expect(document).toContain('Báo cáo quản trị TalentBridge')
        expect(document).toContain('GET /api/v1/jobs/')
    })

    it('refuses to generate a report when no authorized statistics are available', async () => {
        await expect(createAdminReportFile('xlsx', { ...snapshot, stats: null, metrics: null }))
            .rejects.toThrow('Chưa tải được số liệu quản trị')
    })
})
