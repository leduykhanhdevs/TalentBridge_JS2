import { expect, test } from '@playwright/test'
import { join } from 'node:path'

const screenshotDirectory = process.env.TALENTBRIDGE_E2E_SCREENSHOT_DIR!

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
    await expect(page.getByRole('button', { name: 'Đăng xuất' })).toBeVisible()

    await page.getByRole('button', { name: 'Đăng xuất' }).click()
    await expect(page).toHaveURL(/\/login$/)
    expect(await page.evaluate(() => localStorage.getItem('talentbridge_access_token'))).toBeNull()

    await page.getByLabel('Email').fill(email)
    await page.getByLabel('Mật khẩu').fill(password)
    await page.getByRole('button', { name: 'Đăng nhập' }).click()
    await expect(page).toHaveURL(/\/$/, { timeout: 10_000 })
    await expect(page.getByRole('button', { name: 'Đăng xuất' })).toBeVisible()
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
    await page.getByRole('button', { name: 'Đăng xuất' }).click()
    await expect(page).toHaveURL(/\/login$/, { timeout: 5_000 })
    expect(await page.evaluate(() => localStorage.getItem('talentbridge_access_token'))).toBeNull()
    releaseRequest?.()
})
