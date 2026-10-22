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
        routes: ['/', '/jobs', '/jobs/1', '/candidate/profile', '/candidate/applications'],
    },
    {
        name: 'recruiter',
        email: 'recruiter@fpt.com',
        password: 'Password123!',
        route: '/recruiter/jobs',
        heading: 'Tin tuyển dụng của doanh nghiệp',
        routes: ['/recruiter/profile', '/recruiter/jobs', '/recruiter/jobs/1/applicants', '/recruiter/statistics', '/recruiter/company', '/recruiter/join-company', '/recruiter/peer-approval'],
    },
    {
        name: 'admin',
        email: 'admin@talentbridge.vn',
        password: 'AdminPassword123!',
        route: '/admin/jobs',
        heading: 'Kiểm duyệt tin tuyển dụng',
        routes: ['/admin/candidates', '/admin/recruiters', '/admin/companies', '/admin/jobs', '/admin/metrics', '/admin/assistant'],
    },
] as const

test('đăng nhập 3 vai trò, chụp giao diện và kiểm tra tràn ngang tại 360/390/555/1280/1440px', async ({ browser, baseURL }) => {
    await mkdir(screenshotDirectory, { recursive: true })

    for (const actor of actors) {
        const context = await browser.newContext({ viewport: { width: 1280, height: 900 } })
        const page = await context.newPage()
        const pageErrors: string[] = []
        page.on('pageerror', (error) => pageErrors.push(error.message))

        await page.goto(`${baseURL}/login`)
        await page.getByLabel('Email').fill(actor.email)
        await page.getByLabel('Mật khẩu').fill(actor.password)
        await Promise.all([
            page.waitForURL((url) => !url.pathname.endsWith('/login')),
            page.getByRole('button', { name: 'Đăng nhập' }).click(),
        ])
        await page.goto(`${baseURL}${actor.route}`)
        await expect(page.getByRole('heading', { name: actor.heading, exact: false }).first()).toBeVisible()
        if (actor.name === 'candidate') {
            await expect(page.getByRole('heading', { name: 'Thể hiện năng lực bằng trải nghiệm', exact: true })).toBeVisible()
            await expect(page.getByRole('complementary')).toContainText(actor.heading)
            await expect(page.getByRole('region', { name: 'Nội dung hồ sơ' })).toBeVisible()
        }
        if (actor.name === 'recruiter') {
            const statusTabs = page.getByRole('navigation', { name: 'Lọc tin theo trạng thái' })
            await expect(statusTabs.getByRole('button', { name: 'Tất cả' })).toHaveAttribute('aria-pressed', 'true')
            await expect(statusTabs.getByRole('button', { name: 'Đang mở' })).toBeVisible()
            await expect(statusTabs.getByRole('button', { name: 'Chờ duyệt' })).toBeVisible()
            await expect(statusTabs.getByRole('button', { name: 'Bản nháp' })).toBeVisible()
            await expect(page.getByText('Tổng tin tuyển dụng', { exact: true })).toHaveCount(0)
            await page.getByRole('button', { name: 'Tìm tin tuyển dụng' }).click()
            await expect(page.getByRole('searchbox', { name: 'Tìm theo tiêu đề, địa điểm hoặc kỹ năng' })).toBeVisible()
        }
        if (actor.name === 'admin') {
            await expect(page.getByRole('button', { name: 'Duyệt' }).first()).toBeVisible()
            await expect(page.getByText('Phiên làm việc an toàn')).toHaveCount(0)
            await page.getByRole('button', { name: 'Cài đặt tài khoản' }).click()
            await expect(page.getByRole('menuitem', { name: 'Đổi mật khẩu' })).toBeVisible()
            await expect(page.getByRole('menuitem', { name: 'Đăng xuất' })).toBeVisible()
            await page.getByRole('button', { name: 'Cài đặt tài khoản' }).click()
        }

        if (actor.name === 'recruiter' || actor.name === 'admin') {
            await page.setViewportSize({ width: 360, height: 900 })
            const assistantLabel = actor.name === 'admin' ? 'Trợ lý quản trị' : 'Trợ lý'
            const assistantPath = actor.name === 'admin' ? '/admin/assistant' : '/assistant'
            const assistantHeading = actor.name === 'admin' ? 'Trợ lý quản trị' : 'Trợ lý TalentBridge'
            if (actor.name === 'recruiter') await page.getByRole('button', { name: 'Mở điều hướng' }).click()
            await page.getByRole('link', { name: assistantLabel, exact: true }).click()
            await expect(page).toHaveURL(new RegExp(`${assistantPath.replaceAll('/', '\\/')}$`))
            await expect(page.getByRole('heading', { name: assistantHeading, level: 1 })).toBeVisible()
            await page.goto(`${baseURL}${actor.route}`)
        }

        for (const width of [360, 390, 555, 1280, 1440]) {
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

        for (const route of actor.routes) {
            await page.goto(`${baseURL}${route}`)
            await expect(page.locator('main').first(), `${actor.name} route ${route} phải render main`).toBeVisible()
            const contentLength = await page.locator('main').first().evaluate((element) => element.textContent?.trim().length ?? 0)
            expect(contentLength, `${actor.name} route ${route} không được trắng`).toBeGreaterThan(40)

            const routeSlug = route.slice(1).replaceAll('/', '-') || 'home'
            for (const width of [360, 390, 1280, 1440]) {
                await page.setViewportSize({ width, height: 900 })
                if (actor.name === 'admin' && route === '/admin/candidates' && width === 1280) {
                    await expect(page.getByRole('button', { name: 'Khóa', exact: true }).first()).toBeInViewport()
                }
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
                if (layout.documentWidth > width) {
                    await page.screenshot({ path: join(screenshotDirectory, `${actor.name}-route-${routeSlug}-${width}-overflow.png`), fullPage: true })
                }
                expect(layout.documentWidth, `${actor.name} route ${route} tràn ngang tại ${width}px: ${JSON.stringify(layout.offenders)}`).toBeLessThanOrEqual(width)
                if (actor.name === 'recruiter' && route === '/recruiter/company' && width >= 1280) {
                    const gap = await page.evaluate(() => {
                        const sidebar = document.querySelector('#workspace-navigation')
                        const footer = document.querySelector('[data-testid="workspace-footer"]')
                        if (!sidebar || !footer) return null
                        return Math.round(
                            footer.getBoundingClientRect().top
                            - sidebar.getBoundingClientRect().bottom,
                        )
                    })
                    expect(gap, 'Không để khoảng hở dọc giữa menu nhà tuyển dụng và chân trang').toBe(0)
                }
                await page.screenshot({
                    path: join(screenshotDirectory, `${actor.name}-route-${routeSlug}-${width}.png`),
                    fullPage: true,
                    animations: 'disabled',
                })
            }
        }

        expect(pageErrors, `Lỗi JavaScript ở phân hệ ${actor.name}`).toEqual([])
        await context.close()
    }
})

test('trang chủ hiển thị việc làm từ API và không tràn ngang ở desktop/mobile', async ({ page, baseURL }) => {
    const pageErrors: string[] = []
    page.on('pageerror', (error) => pageErrors.push(error.message))
    await page.goto(`${baseURL}/`)
    await expect(page.getByRole('heading', { name: 'Tìm một công việc đáng để bắt đầu' })).toBeVisible()
    await expect.poll(() => page.locator('article a[href^="/jobs/"]').count()).toBeGreaterThan(0)

    for (const width of [360, 555, 1280]) {
        await page.setViewportSize({ width, height: 900 })
        const documentWidth = await page.evaluate(() => document.documentElement.scrollWidth)
        expect(documentWidth, `Trang chủ tràn ngang tại viewport ${width}px`).toBeLessThanOrEqual(width)
        await page.screenshot({
            path: join(screenshotDirectory, `home-${width}.png`),
            fullPage: true,
            animations: 'disabled',
        })
    }
    expect(pageErrors, 'Lỗi JavaScript trên trang chủ').toEqual([])
})

test('các route công khai và route 404 có nội dung, không render màn hình trắng', async ({ page, baseURL }) => {
    const pageErrors: string[] = []
    page.on('pageerror', (error) => pageErrors.push(error.message))
    const routes = [
        '/',
        '/login',
        '/register',
        '/forgot-password',
        '/reset-password',
        '/jobs',
        '/jobs/1',
        '/route-kiem-thu-khong-ton-tai',
    ]

    for (const route of routes) {
        await page.goto(`${baseURL}${route}`)
        const main = page.locator('main').first()
        await expect(main, `Route ${route} cần hiển thị nội dung chính`).toBeVisible()
        const contentLength = await main.evaluate((element) => element.textContent?.trim().length ?? 0)
        expect(contentLength, `Route ${route} không được trắng`).toBeGreaterThan(30)
        if (['/login', '/register', '/forgot-password', '/reset-password'].includes(route)) {
            await expect(page.getByRole('navigation', { name: 'Điều hướng chính' })).toHaveCount(0)
            await expect(page.getByRole('button', { name: /^(Mở|Đóng) menu$/ })).toHaveCount(0)
        }
        const routeSlug = route.slice(1).replaceAll('/', '-') || 'home'
        for (const width of [360, 390, 1280, 1440]) {
            await page.setViewportSize({ width, height: 900 })
            await expect(main).toBeVisible()
            const documentWidth = await page.evaluate(() => document.documentElement.scrollWidth)
            expect(documentWidth, `Route ${route} tràn ngang tại ${width}px`).toBeLessThanOrEqual(width)
            await page.screenshot({
                path: join(screenshotDirectory, `public-route-${routeSlug}-${width}.png`),
                fullPage: true,
                animations: 'disabled',
            })
        }
    }
    expect(pageErrors, 'Lỗi JavaScript trên route công khai').toEqual([])
})

test('trang chủ có thông báo lỗi và trạng thái rỗng khi API không có việc làm', async ({ page, baseURL }) => {
    let calls = 0
    let returnEmptyResult = false
    await page.route('**/api/v1/jobs?*', async (route) => {
        calls += 1
        if (!returnEmptyResult) {
            await route.fulfill({
                status: 503,
                contentType: 'application/json',
                body: JSON.stringify({ statusCode: 503, message: 'Dịch vụ đang tạm thời không khả dụng.' }),
            })
            return
        }
        await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({
                statusCode: 200,
                message: 'Tải thành công.',
                data: { content: [], pageNumber: 1, pageSize: 2, totalElements: 0, totalPages: 0, isLast: true },
            }),
        })
    })

    await page.goto(`${baseURL}/`)
    await expect(page.getByRole('alert')).toContainText('Chưa tải được danh sách việc làm')
    expect(calls).toBe(1)
    returnEmptyResult = true
    await page.getByRole('button', { name: 'Thử lại' }).click()
    await expect(page.getByText('Chưa có tin tuyển dụng đang mở')).toBeVisible()
    expect(calls).toBe(2)
})
