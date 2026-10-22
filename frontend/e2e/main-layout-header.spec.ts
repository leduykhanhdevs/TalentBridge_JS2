import { expect, test } from '@playwright/test'
import { join } from 'node:path'

const screenshotDirectory = process.env.TALENTBRIDGE_E2E_SCREENSHOT_DIR!

async function signInAsCandidate(page: import('@playwright/test').Page) {
    await page.addInitScript(() => {
        localStorage.setItem('talentbridge_access_token', 'qa-header-token')
        localStorage.setItem('talentbridge_auth_user', JSON.stringify({
            id: 990,
            email: 'qa.header@example.invalid',
            fullName: 'QA Header Candidate',
            status: 'ACTIVE',
            roles: ['ROLE_CANDIDATE'],
        }))
    })
}

async function stubPublicJobsApi(page: import('@playwright/test').Page) {
    await page.route('**/api/v1/health', (route) => route.fulfill({ status: 200, body: 'ok' }))
    await page.route('**/api/v1/jobs**', (route) => route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
            statusCode: 200,
            message: 'ok',
            data: { content: [], pageNumber: 1, pageSize: 10, totalElements: 0, totalPages: 0, isLast: true },
        }),
    }))
}

test('candidate header uses horizontal navigation and keeps account actions in settings on desktop', async ({ page }) => {
    await stubPublicJobsApi(page)
    await signInAsCandidate(page)
    await page.goto('/jobs')

    const navigation = page.getByRole('navigation', { name: 'Điều hướng chính' })
    await expect(navigation).toBeVisible()
    await expect(navigation.getByRole('link', { name: 'Hồ sơ TopCV' })).toBeVisible()
    await expect(navigation.getByRole('link', { name: 'Đơn ứng tuyển' })).toBeVisible()
    await expect(page.getByRole('button', { name: 'Cài đặt tài khoản' })).toBeVisible()
    await expect(page.getByRole('button', { name: 'Đổi mật khẩu' })).toHaveCount(0)
    await expect(page.getByRole('button', { name: 'Đăng xuất' })).toHaveCount(0)

    await page.screenshot({ path: join(screenshotDirectory, 'main-header-desktop.png') })
    await page.getByRole('button', { name: 'Cài đặt tài khoản' }).click()
    const settingsMenu = page.getByRole('menu')
    await expect(settingsMenu.getByRole('menuitem', { name: 'Đổi mật khẩu' })).toBeVisible()
    await expect(settingsMenu.getByRole('menuitem', { name: 'Đăng xuất' })).toBeVisible()

    await settingsMenu.getByRole('menuitem', { name: 'Đổi mật khẩu' }).click()
    await expect(page.getByRole('dialog')).toBeVisible()
    await expect(page.getByRole('heading', { name: 'Đổi mật khẩu' })).toBeVisible()
})

test('candidate header keeps the nav horizontally scrollable and settings usable on mobile', async ({ page }) => {
    await page.setViewportSize({ width: 360, height: 800 })
    await stubPublicJobsApi(page)
    await signInAsCandidate(page)
    await page.goto('/jobs')

    const navigation = page.getByRole('navigation', { name: 'Điều hướng chính' })
    await expect(navigation).toBeVisible()
    await expect(navigation).toHaveCSS('overflow-x', 'auto')
    await expect(page.getByRole('button', { name: 'Mở menu' })).toHaveCount(0)
    await expect(page.getByRole('button', { name: 'Cài đặt tài khoản' })).toBeVisible()
    await expect.poll(() => navigation.evaluate((element) => element.scrollWidth > element.clientWidth)).toBe(true)
    await page.screenshot({ path: join(screenshotDirectory, 'main-header-mobile.png') })
    const applicationsLink = navigation.getByRole('link', { name: 'Đơn ứng tuyển' })
    await applicationsLink.scrollIntoViewIfNeeded()
    await expect(applicationsLink).toBeInViewport()

    await page.getByRole('button', { name: 'Cài đặt tài khoản' }).click()
    const settingsMenu = page.getByRole('menu')
    await expect(settingsMenu.getByRole('menuitem', { name: 'Đổi mật khẩu' })).toBeVisible()
    await expect(settingsMenu.getByRole('menuitem', { name: 'Đăng xuất' })).toBeVisible()

    await settingsMenu.getByRole('menuitem', { name: 'Đổi mật khẩu' }).click()
    await expect(page.getByRole('dialog')).toBeVisible()
})
