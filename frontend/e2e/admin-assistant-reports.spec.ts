import { readFile } from 'node:fs/promises'
import { expect, test } from '@playwright/test'

test('admin uses vertical sidebar and assistant exports real PNG, Excel and Word reports', async ({ page }) => {
    const pageErrors: string[] = []
    page.on('pageerror', (error) => pageErrors.push(error.message))
    await page.addInitScript(() => {
        localStorage.setItem('talentbridge_access_token', 'e2e-admin-token')
        localStorage.setItem('talentbridge_auth_user', JSON.stringify({
            id: 700,
            email: 'admin@example.invalid',
            fullName: 'QA Admin',
            roles: ['ROLE_ADMIN'],
        }))
    })
    await page.route('**/api/v1/health', (route) => route.fulfill({ status: 200, body: 'ok' }))
    await page.route('**/api/v1/admin/dashboard/stats', (route) => route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ statusCode: 200, message: 'ok', data: { totalUsers: 143, totalCompanies: 22, pendingCompanies: 3, totalJobs: 100, activeJobs: 91, pendingJobs: 4 } }),
    }))
    await page.route('**/api/v1/admin/metrics/api?*', (route) => route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ statusCode: 200, message: 'ok', data: {
            generatedAt: '2026-10-22T08:00:00Z', collectedSince: '2026-10-21T08:00:00Z', from: '2026-10-21T08:00:00Z', to: '2026-10-22T08:00:00Z',
            bucketMinutes: 5, retentionHours: 24, resetsOnRestart: true, requestCount: 38, clientErrorCount: 2, serverErrorCount: 1,
            errorRatePercent: 7.89, averageLatencyMs: 76.5, maxLatencyMs: 210,
            endpoints: [{ endpoint: 'GET /api/v1/jobs/{id}', requestCount: 12, clientErrorCount: 1, serverErrorCount: 0, errorRatePercent: 8.33, averageLatencyMs: 42.5, maxLatencyMs: 110, buckets: [{ startsAt: '2026-10-22T07:55:00Z', requestCount: 3, clientErrorCount: 1, serverErrorCount: 0, averageLatencyMs: 42.5, maxLatencyMs: 110 }] }],
        } }),
    }))
    const assistantMessages: string[] = []
    await page.route('**/api/v1/assistant/chat', (route) => {
        assistantMessages.push(route.request().postDataJSON().message)
        return route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({ statusCode: 200, message: 'ok', data: { answer: 'Đây là câu trả lời thử nghiệm từ trợ lý.', source: 'KNOWLEDGE_BASE', references: [] } }),
        })
    })

    await page.goto('/admin/assistant')
    await expect(page.getByRole('heading', { name: 'Trợ lý quản trị', level: 1 })).toBeVisible()
    await expect(page.getByText('143', { exact: true })).toBeVisible()
    await expect(page.getByText('Phiên làm việc an toàn')).toHaveCount(0)
    await expect(page.getByRole('link', { name: 'Về TalentBridge' })).toHaveCount(0)

    const nav = page.getByRole('navigation', { name: 'Điều hướng quản trị' })
    await expect(nav.getByRole('link', { name: 'Doanh nghiệp' })).toBeVisible()
    await expect(page.locator('#workspace-navigation')).toHaveCSS('width', '232px')
    await expect(nav).toHaveCSS('display', 'grid')
    for (const item of await nav.getByRole('link').all()) await expect(item).toBeVisible()
    const settingsButton = nav.getByRole('button', { name: 'Cài đặt tài khoản' })
    await expect(settingsButton).toBeVisible()
    await expect(settingsButton).not.toHaveClass(/border/)
    await settingsButton.click()
    await expect(page.getByRole('menuitem', { name: 'Đổi mật khẩu' })).toBeVisible()
    await expect(page.getByRole('menuitem', { name: 'Đăng xuất' })).toBeVisible()
    await settingsButton.click()

    await page.setViewportSize({ width: 360, height: 800 })
    await expect(nav).toBeHidden()
    await page.getByRole('button', { name: 'Mở điều hướng' }).click()
    await expect(nav).toBeVisible()
    await page.setViewportSize({ width: 1280, height: 900 })

    const assistant = page.getByRole('region', { name: 'Trợ lý quản trị' })
    await expect(assistant).toBeVisible()
    expect(await assistant.evaluate((element) => element.getBoundingClientRect().height)).toBeGreaterThan(500)

    const exports = [
        { button: 'Ảnh PNG', link: 'Tải báo cáo PNG', extension: '.png', signature: [0x89, 0x50, 0x4e, 0x47] },
        { button: 'Excel', link: 'Tải báo cáo Excel', extension: '.xlsx', signature: [0x50, 0x4b] },
        { button: 'Word', link: 'Tải báo cáo Word', extension: '.docx', signature: [0x50, 0x4b] },
    ]
    for (const report of exports) {
        await assistant.getByRole('button', { name: report.button, exact: true }).click()
        const reportLink = assistant.getByRole('link', { name: report.link })
        await expect(reportLink).toBeVisible()
        const [download] = await Promise.all([
            page.waitForEvent('download'),
            reportLink.click(),
        ])
        expect(download.suggestedFilename()).toMatch(new RegExp(`${report.extension.replace('.', '\\.')}$`))
        const artifact = new Uint8Array(await readFile((await download.path())!))
        expect(Array.from(artifact.slice(0, report.signature.length))).toEqual(report.signature)
    }

    const question = 'Anh hãy thống kê lỗi API'
    const pngReportsBeforeQuestion = await assistant.getByRole('link', { name: 'Tải báo cáo PNG' }).count()
    await assistant.getByRole('textbox', { name: 'Câu hỏi về TalentBridge' }).fill(question)
    await assistant.getByRole('button', { name: 'Gửi câu hỏi' }).click()
    await expect(assistant.getByText(/Thống kê API trong/)).toBeVisible()
    await expect(assistant.getByRole('link', { name: 'Tải báo cáo PNG' })).toHaveCount(pngReportsBeforeQuestion)
    expect(assistantMessages).toEqual([])

    expect(pageErrors).toEqual([])
})
