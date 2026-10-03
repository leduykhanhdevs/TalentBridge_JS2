import { mkdir } from 'node:fs/promises'
import { join } from 'node:path'
import { expect, test } from '@playwright/test'

const screenshotDirectory = process.env.TALENTBRIDGE_E2E_SCREENSHOT_DIR!

const actors = [
    {
        name: 'candidate',
        email: 'candidate@talentbridge.vn',
        password: 'Password123!',
        route: '/candidate/profile',
        heading: 'Trần Minh Anh',
    },
    {
        name: 'recruiter',
        email: 'recruiter@fpt.com',
        password: 'Password123!',
        route: '/recruiter/jobs',
        heading: 'Quản lý tin tuyển dụng',
    },
    {
        name: 'admin',
        email: 'admin@talentbridge.vn',
        password: 'AdminPassword123!',
        route: '/admin/jobs',
        heading: 'Kiểm duyệt tin tuyển dụng',
    },
] as const

test('đăng nhập 3 vai trò, chụp giao diện và kiểm tra tràn ngang tại 360/555/1280px', async ({ browser, baseURL }) => {
    await mkdir(screenshotDirectory, { recursive: true })

    for (const actor of actors) {
        const context = await browser.newContext({ viewport: { width: 1280, height: 900 } })
        const page = await context.newPage()
        const pageErrors: string[] = []
        page.on('pageerror', (error) => pageErrors.push(error.message))

        await page.goto(`${baseURL}/login`)
        await page.getByLabel('Địa chỉ email').fill(actor.email)
        await page.getByLabel('Mật khẩu').fill(actor.password)
        await Promise.all([
            page.waitForURL((url) => !url.pathname.endsWith('/login')),
            page.getByRole('button', { name: 'Đăng nhập hệ thống' }).click(),
        ])
        await page.goto(`${baseURL}${actor.route}`)
        await expect(page.getByRole('heading', { name: actor.heading, exact: false }).first()).toBeVisible()
        if (actor.name === 'recruiter') {
            const token = await page.evaluate(() => localStorage.getItem('talentbridge_access_token'))
            const stats = await page.evaluate(async (accessToken) => {
                const response = await fetch('/api/v1/recruiters/my-jobs/stats', {
                    headers: { Authorization: `Bearer ${accessToken}` },
                })
                const body = await response.json()
                return body.data as { total: number; active: number }
            }, token)
            await expect(page.getByText('Tổng tin tuyển dụng', { exact: true }).locator('xpath=../..'))
                .toContainText(String(stats.total))
            await expect(page.getByText('Đang tuyển dụng', { exact: true }).locator('xpath=../..'))
                .toContainText(String(stats.active))
        }
        if (actor.name === 'admin') {
            await expect(page.getByRole('button', { name: 'Duyệt' }).first()).toBeVisible()
        }

        for (const width of [360, 555, 1280]) {
            await page.setViewportSize({ width, height: 900 })
            await expect(page.getByRole('heading', { name: actor.heading, exact: false }).first()).toBeVisible()
            await page.screenshot({
                path: join(screenshotDirectory, `${actor.name}-${width}.png`),
                fullPage: true,
                animations: 'disabled',
            })

            const layout = await page.evaluate(() => ({
                documentWidth: document.documentElement.scrollWidth,
                offenders: Array.from(document.querySelectorAll('body *'))
                    .map((element) => {
                        const rect = element.getBoundingClientRect()
                        return { tag: element.tagName, className: typeof element.className === 'string' ? element.className : '', text: element.textContent?.trim().slice(0, 70), left: Math.round(rect.left), right: Math.round(rect.right), width: Math.round(rect.width) }
                    })
                    .filter((element) => element.right > window.innerWidth + 1 && element.width > 0)
                    .sort((left, right) => right.right - left.right)
                    .slice(0, 8),
            }))
            expect(layout.documentWidth, `${actor.name} tràn ngang ở viewport ${width}px: ${JSON.stringify(layout.offenders)}`).toBeLessThanOrEqual(width)
        }

        if (actor.name === 'candidate') {
            for (const width of [360, 555]) {
                await page.setViewportSize({ width, height: 900 })
                await page.getByRole('button', { name: 'Tạo CV từ Hồ sơ' }).click()
                await expect(page.getByRole('heading', { name: /Trình tạo CV từ Hồ sơ cá nhân/ })).toBeVisible()

                for (const panel of ['edit', 'preview'] as const) {
                    if (panel === 'preview') {
                        await page.getByRole('button', { name: 'Xem trước' }).click()
                        await expect(page.getByText('Giới thiệu bản thân')).toBeVisible()
                    }
                    await page.screenshot({
                        path: join(screenshotDirectory, `candidate-cvbuilder-${width}-${panel}.png`),
                        animations: 'disabled',
                    })
                    const modalWidth = await page.evaluate(() => document.documentElement.scrollWidth)
                    expect(modalWidth, `CV Builder (${panel}) tràn ngang ở ${width}px`).toBeLessThanOrEqual(width)
                    if (panel === 'edit') await expect(page.getByText('Chọn mẫu giao diện')).toBeVisible()
                }

                await page.getByRole('button', { name: 'Đóng trình tạo CV' }).click()
            }
        }

        expect(pageErrors, `Lỗi JavaScript ở phân hệ ${actor.name}`).toEqual([])
        await context.close()
    }
})
