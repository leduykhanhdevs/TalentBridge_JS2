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

test('candidate uses a vertical sidebar and keeps account actions in settings on desktop', async ({ page }) => {
    await stubPublicJobsApi(page)
    await signInAsCandidate(page)
    await page.goto('/jobs')

    const navigation = page.getByRole('navigation', { name: 'Điều hướng ứng viên' })
    await expect(navigation).toBeVisible()
    await expect(navigation.getByRole('link', { name: 'Hồ sơ TopCV' })).toBeVisible()
    await expect(navigation.getByRole('link', { name: 'Đơn ứng tuyển' })).toBeVisible()
    await expect(navigation.getByRole('link', { name: 'Trang chủ' })).not.toHaveAttribute('aria-current', 'page')
    await expect(page.getByText('Phiên làm việc an toàn')).toHaveCount(0)
    await expect(page.getByRole('link', { name: 'Về TalentBridge' })).toHaveCount(0)
    const sidebar = page.locator('#workspace-navigation')
    await expect(sidebar).toHaveCSS('width', '232px')
    expect(await sidebar.evaluate((element) => Math.round(element.getBoundingClientRect().bottom))).toBe(page.viewportSize()!.height)
    const settingsButton = navigation.getByRole('button', { name: 'Cài đặt tài khoản' })
    await expect(settingsButton).toBeVisible()
    await expect(settingsButton).not.toHaveClass(/border/)
    const firstNavigationLink = navigation.getByRole('link').first()
    const [navigationTypography, settingsTypography] = await Promise.all([
        firstNavigationLink.evaluate((element) => {
            const style = getComputedStyle(element)
            return `${style.fontSize}/${style.fontWeight}`
        }),
        settingsButton.evaluate((element) => {
            const style = getComputedStyle(element)
            return `${style.fontSize}/${style.fontWeight}`
        }),
    ])
    expect(settingsTypography).toBe(navigationTypography)
    await expect(page.getByRole('button', { name: 'Đổi mật khẩu' })).toHaveCount(0)
    await expect(page.getByRole('button', { name: 'Đăng xuất' })).toHaveCount(0)

    await page.screenshot({ path: join(screenshotDirectory, 'main-sidebar-desktop.png') })
    await settingsButton.click()
    const settingsMenu = page.getByRole('menu')
    await expect(settingsMenu.getByRole('menuitem', { name: 'Đổi mật khẩu' })).toBeVisible()
    await expect(settingsMenu.getByRole('menuitem', { name: 'Đăng xuất' })).toBeVisible()

    await settingsMenu.getByRole('menuitem', { name: 'Đổi mật khẩu' }).click()
    await expect(page.getByRole('dialog')).toBeVisible()
    await expect(page.getByRole('heading', { name: 'Đổi mật khẩu' })).toBeVisible()
})

test('candidate sidebar opens as a mobile drawer and settings stay usable', async ({ page }) => {
    await page.setViewportSize({ width: 360, height: 800 })
    await stubPublicJobsApi(page)
    await signInAsCandidate(page)
    await page.goto('/jobs')

    const navigation = page.getByRole('navigation', { name: 'Điều hướng ứng viên' })
    await expect(navigation).toBeHidden()
    await expect(page.getByRole('button', { name: 'Mở điều hướng' })).toBeVisible()
    await page.screenshot({ path: join(screenshotDirectory, 'main-sidebar-mobile-closed.png') })
    await page.getByRole('button', { name: 'Mở điều hướng' }).click()
    await expect(navigation).toBeVisible()
    const settingsButton = navigation.getByRole('button', { name: 'Cài đặt tài khoản' })
    await expect(settingsButton).toBeVisible()
    await expect(settingsButton).not.toHaveClass(/border/)
    await page.screenshot({ path: join(screenshotDirectory, 'main-sidebar-mobile-open.png') })
    const applicationsLink = navigation.getByRole('link', { name: 'Đơn ứng tuyển' })
    await expect(applicationsLink).toBeInViewport()

    await settingsButton.click()
    const settingsMenu = page.getByRole('menu')
    await expect(settingsMenu.getByRole('menuitem', { name: 'Đổi mật khẩu' })).toBeVisible()
    await expect(settingsMenu.getByRole('menuitem', { name: 'Đăng xuất' })).toBeVisible()

    await settingsMenu.getByRole('menuitem', { name: 'Đổi mật khẩu' }).click()
    await expect(page.getByRole('dialog')).toBeVisible()
})
