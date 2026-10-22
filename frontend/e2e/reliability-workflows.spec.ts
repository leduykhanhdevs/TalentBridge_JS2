import { expect, test } from '@playwright/test'
import { join } from 'node:path'

const screenshotDirectory = process.env.TALENTBRIDGE_E2E_SCREENSHOT_DIR!

async function signOutThroughSettings(page: import('@playwright/test').Page) {
    await page.getByRole('button', { name: 'Cài đặt tài khoản' }).click()
    await page.getByRole('menuitem', { name: 'Đăng xuất' }).click()
}

test('password reset email failure stays on the email step and shows the server error', async ({ page }) => {
    await page.route('**/api/v1/auth/forgot-password', async (route) => {
        await route.fulfill({
            status: 503,
            contentType: 'application/json',
            body: JSON.stringify({
                statusCode: 50301,
                message: 'Dịch vụ gửi email khôi phục mật khẩu hiện không khả dụng. Vui lòng thử lại sau hoặc liên hệ quản trị viên.',
            }),
        })
    })

    await page.goto('/forgot-password')
    await page.getByLabel('Địa chỉ email').fill('qa.password-reset@example.invalid')
    await page.getByRole('button', { name: 'Gửi mã OTP' }).click()

    await expect(page.getByRole('alert')).toContainText('Dịch vụ gửi email khôi phục mật khẩu hiện không khả dụng.')
    await expect(page.getByRole('heading', { name: 'Quên mật khẩu?' })).toBeVisible()
    await expect(page.getByLabel('Mã OTP')).toHaveCount(0)
})

test('password reset confirmation does not claim an email was sent', async ({ page }) => {
    await page.route('**/api/v1/auth/forgot-password', async (route) => {
        await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({ statusCode: 200, message: 'ok', data: null }),
        })
    })

    await page.goto('/forgot-password')
    await page.getByLabel('Địa chỉ email').fill('qa.password-reset@example.invalid')
    await page.getByRole('button', { name: 'Gửi mã OTP' }).click()

    await expect(page.getByLabel('Mã OTP')).toBeVisible()
    await expect(page.getByRole('status')).toContainText('Nếu email tồn tại trong hệ thống, mã OTP sẽ được gửi.')
    await expect(page.getByRole('status')).not.toContainText('đã được gửi')
})

test('password reset keeps OTP verification bound to the email used by the pending request', async ({ page }) => {
    let releaseRequest!: () => void
    let releaseVerification!: () => void
    let markRequestStarted!: () => void
    let markVerificationStarted!: () => void
    let verifiedEmail: string | undefined
    const requestStarted = new Promise<void>((resolve) => { markRequestStarted = resolve })
    const requestGate = new Promise<void>((resolve) => { releaseRequest = resolve })
    const verificationStarted = new Promise<void>((resolve) => { markVerificationStarted = resolve })
    const verificationGate = new Promise<void>((resolve) => { releaseVerification = resolve })

    await page.route('**/api/v1/auth/forgot-password', async (route) => {
        expect(route.request().postDataJSON()).toEqual({ email: 'qa.original@example.invalid' })
        markRequestStarted()
        await requestGate
        await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({ statusCode: 200, message: 'ok', data: null }),
        })
    })
    await page.route('**/api/v1/auth/verify-reset-otp', async (route) => {
        verifiedEmail = route.request().postDataJSON().email
        markVerificationStarted()
        await verificationGate
        await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({ statusCode: 200, message: 'ok', data: { resetToken: 'qa-reset-grant' } }),
        })
    })

    await page.goto('/forgot-password')
    await expect(page.getByRole('navigation', { name: 'Điều hướng chính' })).toHaveCount(0)
    await page.getByLabel('Địa chỉ email').fill('qa.original@example.invalid')
    await page.getByRole('button', { name: 'Gửi mã OTP' }).click()
    await requestStarted

    await expect(page.getByLabel('Địa chỉ email')).toBeDisabled()
    await expect(page.getByLabel('Địa chỉ email')).toHaveValue('qa.original@example.invalid')
    releaseRequest()

    await expect(page.getByLabel('Mã OTP')).toBeVisible()
    await page.getByLabel('Mã OTP').fill('123456')
    await page.getByRole('button', { name: 'Xác minh OTP' }).click()
    await verificationStarted
    await expect(page.getByRole('button', { name: 'Sửa email' })).toBeDisabled()
    await expect(page.getByRole('button', { name: /Gửi lại sau/ })).toBeDisabled()
    await expect(page.getByLabel('Mã OTP')).toBeDisabled()
    releaseVerification()
    await expect(page.getByRole('heading', { name: 'Tạo mật khẩu mới' })).toBeVisible()
    expect(verifiedEmail).toBe('qa.original@example.invalid')
})

test('floating assistant stays off narrow mobile screens so it cannot cover page controls', async ({ page }) => {
    await page.setViewportSize({ width: 360, height: 800 })

    for (const path of ['/login', '/register', '/forgot-password', '/reset-password']) {
        await page.goto(path)
        await expect(page.getByRole('button', { name: 'Mở nhanh Trợ lý TalentBridge' })).toHaveCount(0)
    }

    await page.addInitScript(() => {
        localStorage.setItem('talentbridge_access_token', 'qa-mobile-candidate-token')
        localStorage.setItem('talentbridge_auth_user', JSON.stringify({
            id: 905,
            email: 'qa.mobile@example.invalid',
            fullName: 'QA Mobile Candidate',
            status: 'ACTIVE',
            roles: ['ROLE_CANDIDATE'],
        }))
    })
    await page.goto('/candidate/applications')
    await expect(page.getByRole('button', { name: 'Mở nhanh Trợ lý TalentBridge' })).toHaveCount(0)
})

test('assistant launcher stays hidden on auth and assistant routes with trailing slashes', async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 900 })

    for (const path of ['/login/', '/register/', '/forgot-password/', '/reset-password/', '/assistant/']) {
        await page.goto(path)
        await expect(page.getByRole('button', { name: 'Mở nhanh Trợ lý TalentBridge' })).toHaveCount(0)
    }
})

test('logout in another tab waits for an active refresh and revokes the rotated pair', async ({ browser }) => {
    test.setTimeout(15_000)
    const context = await browser.newContext()
    await context.addInitScript(() => {
        localStorage.setItem('talentbridge_access_token', 'qa-cross-tab-access')
        localStorage.setItem('talentbridge_refresh_token', 'qa-cross-tab-refresh')
        localStorage.setItem('talentbridge_auth_user', JSON.stringify({
            id: 906,
            email: 'qa.cross-tab@example.invalid',
            fullName: 'QA Cross Tab Candidate',
            status: 'ACTIVE',
            roles: ['ROLE_CANDIDATE'],
        }))
    })

    const refreshPage = await context.newPage()
    const logoutPage = await context.newPage()
    let releaseRefresh!: () => void
    let markRefreshStarted!: () => void
    let markLogoutSent!: () => void
    let logoutHeaders: Record<string, string> | undefined
    let logoutBody: string | undefined
    const refreshStarted = new Promise<void>((resolve) => { markRefreshStarted = resolve })
    const logoutSent = new Promise<void>((resolve) => { markLogoutSent = resolve })

    await refreshPage.route('**/api/v1/candidates/profile', async (route) => {
        await route.fulfill({ status: 401, contentType: 'application/json', body: '{"message":"expired"}' })
    })
    await refreshPage.route('**/api/v1/auth/refresh-token', async (route) => {
        markRefreshStarted()
        await new Promise<void>((resolve) => { releaseRefresh = resolve })
        await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({
                statusCode: 200,
                message: 'ok',
                data: {
                    accessToken: 'qa-cross-tab-access-rotated',
                    refreshToken: 'qa-cross-tab-refresh-rotated',
                    tokenType: 'Bearer',
                    expiresInMs: 60_000,
                    user: { id: 906, email: 'qa.cross-tab@example.invalid', fullName: 'QA Cross Tab Candidate', status: 'ACTIVE', roles: ['ROLE_CANDIDATE'] },
                },
            }),
        })
    })
    await logoutPage.route('**/api/v1/auth/logout', async (route) => {
        logoutHeaders = await route.request().allHeaders()
        logoutBody = route.request().postData() ?? undefined
        markLogoutSent()
        await route.fulfill({ status: 204 })
    })

    try {
        await refreshPage.goto('/candidate/profile')
        await refreshStarted
        await logoutPage.goto('/')
        await signOutThroughSettings(logoutPage)
        await expect(logoutPage).toHaveURL(/\/login$/)
        expect(logoutBody).toBeUndefined()

        releaseRefresh()
        await logoutSent
        expect(JSON.parse(logoutBody ?? 'null')).toEqual({ refreshToken: 'qa-cross-tab-refresh-rotated' })
        expect(logoutHeaders?.authorization).toBe('Bearer qa-cross-tab-access-rotated')
        await expect.poll(() => logoutPage.evaluate(() => localStorage.getItem('talentbridge_access_token'))).toBeNull()
        await expect.poll(() => refreshPage.evaluate(() => localStorage.getItem('talentbridge_refresh_token'))).toBeNull()
    } finally {
        releaseRefresh?.()
        await context.close()
    }
})

test('failed recruiter job creation keeps the form open and shows the API error inside it', async ({ page }) => {
    await page.addInitScript(() => {
        localStorage.setItem('talentbridge_access_token', 'qa-recruiter-access-token')
        localStorage.setItem('talentbridge_auth_user', JSON.stringify({
            id: 901,
            email: 'qa.recruiter@example.invalid',
            fullName: 'QA Recruiter',
            status: 'ACTIVE',
            roles: ['ROLE_RECRUITER'],
        }))
    })

    await page.route('**/api/v1/jobs/my-jobs*', async (route) => {
        await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({
                statusCode: 200,
                message: 'Tải thành công',
                data: { content: [], pageNumber: 1, pageSize: 10, totalElements: 0, totalPages: 0, isLast: true },
            }),
        })
    })

    let submittedPayload: Record<string, unknown> | undefined
    await page.route('**/api/v1/jobs', async (route) => {
        if (route.request().method() !== 'POST') {
            await route.continue()
            return
        }

        submittedPayload = route.request().postDataJSON() as Record<string, unknown>
        await route.fulfill({
            status: 500,
            contentType: 'application/json',
            body: JSON.stringify({ statusCode: 500, message: 'QA: Không thể lưu tin lúc này.' }),
        })
    })

    await page.goto('/recruiter/jobs')
    await expect(page.getByRole('heading', { name: 'Tin tuyển dụng của doanh nghiệp' })).toBeVisible()
    await page.getByRole('button', { name: 'Tạo tin tuyển dụng' }).click()
    await expect(page.getByText('Đăng tin tuyển dụng mới')).toBeVisible()

    const title = 'QA: Senior Backend Engineer'
    await page.getByPlaceholder('Ví dụ: Senior Java Spring Boot Developer...').fill(title)
    await page.getByPlaceholder('Mô tả trách nhiệm, công việc hàng ngày của vị trí này...').fill('Xây dựng và duy trì dịch vụ backend cho sản phẩm.')
    await page.getByRole('button', { name: 'Đăng tin tuyển dụng', exact: true }).click()

    await expect.poll(() => submittedPayload?.title).toBe(title)
    const dialog = page.getByRole('dialog', { name: 'Đăng tin tuyển dụng mới' })
    await expect(dialog).toBeVisible()
    await expect(dialog.getByRole('alert')).toContainText('QA: Không thể lưu tin lúc này.')
    await expect(dialog.getByPlaceholder('Ví dụ: Senior Java Spring Boot Developer...')).toHaveValue(title)
})

test('stalled application history request stops loading and offers a retry', async ({ page }) => {
    test.setTimeout(25_000)
    await page.addInitScript(() => {
        localStorage.setItem('talentbridge_access_token', 'qa-candidate-access-token')
        localStorage.setItem('talentbridge_auth_user', JSON.stringify({
            id: 902,
            email: 'qa.candidate@example.invalid',
            fullName: 'QA Candidate',
            status: 'ACTIVE',
            roles: ['ROLE_CANDIDATE'],
        }))
    })

    let releaseRequest: (() => void) | undefined
    let notifyRequestStarted: (() => void) | undefined
    const requestStarted = new Promise<void>((resolve) => { notifyRequestStarted = resolve })
    await page.route('**/api/v1/candidates/applications', async (route) => {
        notifyRequestStarted?.()
        await new Promise<void>((resolve) => { releaseRequest = resolve })
        try {
            await route.fulfill({
                status: 200,
                contentType: 'application/json',
                body: JSON.stringify({ statusCode: 200, message: 'ok', data: [] }),
            })
        } catch {
            // The browser may have already aborted this deliberately stalled request at its deadline.
        }
    })

    await page.goto('/candidate/applications')
    await requestStarted
    await expect(page.getByText('Máy chủ phản hồi quá lâu. Vui lòng thử lại.')).toBeVisible({ timeout: 15_000 })
    await expect(page.getByRole('button', { name: 'Thử lại' })).toBeVisible()
    await expect(page.getByText('Đang tải lịch sử ứng tuyển của bạn...')).toHaveCount(0)
    await page.screenshot({ path: join(screenshotDirectory, 'candidate-applications-timeout.png'), fullPage: true })
    releaseRequest?.()
})

test('candidate can register, sign out and sign back in using the local H2 backend', async ({ page }) => {
    const email = `qa.uat.${Date.now()}@talentbridge.test`
    const password = 'FixturePassword123!'

    await page.goto('/register')
    await page.getByRole('button', { name: 'Tạo tài khoản' }).click()
    await expect(page.getByRole('alert').filter({ hasText: 'Vui lòng nhập họ và tên.' })).toBeVisible()

    await page.getByLabel('Họ và tên').fill('Ứng viên UAT')
    await page.getByLabel('Email').fill(email)
    await page.getByLabel('Mật khẩu').fill(password)
    await page.getByRole('checkbox').check()
    await page.getByRole('button', { name: 'Tạo tài khoản' }).click()

    await expect(page.getByRole('status')).toContainText('Đăng ký thành công')
    await expect(page).toHaveURL(/\/$/, { timeout: 10_000 })
    await expect(page.getByRole('button', { name: 'Cài đặt tài khoản' })).toBeVisible()

    await signOutThroughSettings(page)
    await expect(page).toHaveURL(/\/login$/)
    expect(await page.evaluate(() => localStorage.getItem('talentbridge_access_token'))).toBeNull()

    await page.getByLabel('Email').fill(email)
    await page.getByLabel('Mật khẩu').fill(password)
    await page.getByRole('button', { name: 'Đăng nhập' }).click()
    await expect(page).toHaveURL(/\/$/, { timeout: 10_000 })
    await expect(page.getByRole('button', { name: 'Cài đặt tài khoản' })).toBeVisible()
})

test('a stalled server logout still clears the local session and returns to login', async ({ page }) => {
    test.setTimeout(10_000)
    await page.addInitScript(() => {
        localStorage.setItem('talentbridge_access_token', 'qa-candidate-access-token')
        localStorage.setItem('talentbridge_auth_user', JSON.stringify({
            id: 903,
            email: 'qa.candidate@example.invalid',
            fullName: 'QA Candidate',
            status: 'ACTIVE',
            roles: ['ROLE_CANDIDATE'],
        }))
    })

    let releaseRequest: (() => void) | undefined
    await page.route('**/api/v1/auth/logout', async (route) => {
        await new Promise<void>((resolve) => { releaseRequest = resolve })
        try {
            await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ statusCode: 200 }) })
        } catch {
            // A timed-out client request may already be closed.
        }
    })

    await page.route('**/api/v1/jobs?*', async (route) => {
        await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({ statusCode: 200, message: 'ok', data: { content: [], totalElements: 0, totalPages: 0, pageNumber: 1, pageSize: 6 } }),
        })
    })

    await page.goto('/')
    await signOutThroughSettings(page)
    await expect(page).toHaveURL(/\/login$/, { timeout: 5_000 })
    expect(await page.evaluate(() => localStorage.getItem('talentbridge_access_token'))).toBeNull()
    releaseRequest?.()
})

test('expired-token logout returns to login without starting another refresh', async ({ page }) => {
    test.setTimeout(5_000)
    await page.addInitScript(() => {
        localStorage.setItem('talentbridge_access_token', 'qa-expired-access-token')
        localStorage.setItem('talentbridge_refresh_token', 'qa-refresh-token')
        localStorage.setItem('talentbridge_auth_user', JSON.stringify({
            id: 904,
            email: 'qa.expired@example.invalid',
            fullName: 'QA Expired Candidate',
            status: 'ACTIVE',
            roles: ['ROLE_CANDIDATE'],
        }))
    })

    let logoutCalls = 0
    let refreshCalls = 0
    await page.route('**/api/v1/auth/logout', async (route) => {
        logoutCalls += 1
        await route.fulfill({ status: 401, contentType: 'application/json', body: '{"message":"expired"}' })
    })
    await page.route('**/api/v1/auth/refresh-token', async (route) => {
        refreshCalls += 1
        await route.fulfill({ status: 401, contentType: 'application/json', body: '{"message":"expired"}' })
    })

    await page.goto('/')
    await signOutThroughSettings(page)
    await expect(page).toHaveURL(/\/login$/, { timeout: 1_500 })
    expect(await page.evaluate(() => localStorage.getItem('talentbridge_access_token'))).toBeNull()
    expect(logoutCalls).toBe(1)
    expect(refreshCalls).toBe(0)
})
