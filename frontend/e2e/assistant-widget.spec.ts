import { expect, test } from '@playwright/test'
import { join } from 'node:path'

const screenshotDirectory = process.env.TALENTBRIDGE_E2E_SCREENSHOT_DIR!

test('assistant page and quick widget survive non-void scroll cleanup behavior', async ({ page }) => {
    const pageErrors: string[] = []
    page.on('pageerror', (error) => pageErrors.push(error.message))
    await page.addInitScript(() => {
        Element.prototype.scrollIntoView = function () {
            return true as unknown as void
        }
    })

    await page.goto('/assistant')
    await expect(page.getByRole('heading', { name: 'Trợ lý TalentBridge' }).first()).toBeVisible()
    await page.screenshot({ path: join(screenshotDirectory, 'assistant-page.png'), fullPage: true })

    await page.goto('/jobs')
    await expect(page.getByRole('heading', { name: 'Tìm việc làm phù hợp' })).toBeVisible()
    await page.getByRole('button', { name: 'Mở nhanh Trợ lý TalentBridge' }).click()
    await expect(page.getByRole('region', { name: 'Trợ lý TalentBridge' })).toBeVisible()
    await page.screenshot({ path: join(screenshotDirectory, 'assistant-widget-open.png') })

    await page.getByRole('button', { name: 'Đóng', exact: true }).click()
    await expect(page.getByRole('heading', { name: 'Tìm việc làm phù hợp' })).toBeVisible()
    expect(pageErrors).toEqual([])
})
