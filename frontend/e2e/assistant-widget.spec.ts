import { expect, test } from '@playwright/test'
import { join } from 'node:path'

const screenshotDirectory = process.env.TALENTBRIDGE_E2E_SCREENSHOT_DIR!

test('assistant page and quick widget survive starter and typed prompts with non-void scroll behavior', async ({ page }) => {
    const pageErrors: string[] = []
    let backendWarmupSeen = false
    page.on('pageerror', (error) => pageErrors.push(error.message))
    page.on('request', (request) => {
        if (request.url().includes('/api/v1/health')) backendWarmupSeen = true
    })
    await page.addInitScript(() => {
        localStorage.setItem('talentbridge_access_token', 'e2e-access-token')
        Element.prototype.scrollIntoView = function () {
            return true as unknown as void
        }
    })
    await page.route('**/api/v1/assistant/chat', async (route) => {
        const request = route.request().postDataJSON() as { message: string }
        if (request.message === 'Mô phỏng lỗi API') {
            await route.fulfill({
                status: 503,
                contentType: 'application/json',
                body: JSON.stringify({ statusCode: 503, message: 'Backend tạm thời không phản hồi', data: null }),
            })
            return
        }
        await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({
                statusCode: 200,
                message: 'Trợ lý đã phản hồi',
                data: {
                    answer: `Hướng dẫn: ${request.message}`,
                    source: 'KNOWLEDGE_BASE',
                    references: ['Hướng dẫn nội bộ'],
                },
            }),
        })
    })

    await page.goto('/assistant')
    await expect(page.getByRole('heading', { name: 'Trợ lý TalentBridge' }).first()).toBeVisible()
    await expect.poll(() => backendWarmupSeen).toBe(true)
    await page.screenshot({ path: join(screenshotDirectory, 'assistant-page.png'), fullPage: true })
    await page.getByRole('button', { name: /Làm sao để ứng tuyển/ }).click()
    await expect(page.getByText('Hướng dẫn: Làm sao để ứng tuyển?')).toBeVisible()
    await page.getByLabel('Câu hỏi về TalentBridge').fill('Tôi cập nhật hồ sơ ở đâu?')
    await page.getByRole('button', { name: 'Gửi câu hỏi' }).click()
    await expect(page.getByText('Hướng dẫn: Tôi cập nhật hồ sơ ở đâu?')).toBeVisible()
    await page.getByLabel('Câu hỏi về TalentBridge').fill('Mô phỏng lỗi API')
    await page.getByRole('button', { name: 'Gửi câu hỏi' }).click()
    await expect(page.getByRole('alert')).toContainText('Backend tạm thời không phản hồi')
    await expect(page.getByRole('heading', { name: 'Trợ lý TalentBridge' }).first()).toBeVisible()

    await page.goto('/jobs')
    await expect(page.getByRole('heading', { name: 'Tìm việc làm phù hợp' })).toBeVisible()
    await page.getByRole('button', { name: 'Mở nhanh Trợ lý TalentBridge' }).click()
    await expect(page.getByRole('region', { name: 'Trợ lý TalentBridge' })).toBeVisible()
    await page.screenshot({ path: join(screenshotDirectory, 'assistant-widget-open.png') })

    await page.getByRole('button', { name: /Quên mật khẩu phải làm gì/ }).click()
    await expect(page.getByText('Hướng dẫn: Quên mật khẩu phải làm gì?')).toBeVisible()
    await page.getByLabel('Câu hỏi về TalentBridge').fill('Tạo tin tuyển dụng thế nào?')
    await page.getByRole('button', { name: 'Gửi câu hỏi' }).click()
    await expect(page.getByText('Hướng dẫn: Tạo tin tuyển dụng thế nào?')).toBeVisible()

    await page.getByRole('button', { name: 'Đóng', exact: true }).click()
    await expect(page.getByRole('heading', { name: 'Tìm việc làm phù hợp' })).toBeVisible()
    expect(pageErrors).toEqual([])
})
