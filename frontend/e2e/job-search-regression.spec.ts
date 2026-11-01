import { expect, test } from '@playwright/test'

const fixtures = Array.from({ length: 102 }, (_, index) => ({
    id: 102 - index, companyId: 1, companyName: 'QA company', title: `QA job ${102 - index}`,
    description: 'QA fixture only', location: 'Địa điểm mô phỏng · Hà Nội', city: 'Hà Nội',
    jobType: 'FULL_TIME', experienceLevel: index === 0 ? 'NO_EXPERIENCE' : 'UNDER_1_YEAR',
    minSalary: 15000000, maxSalary: 30000000, isNegotiable: false,
    status: 'ACTIVE', isDemo: true, skills: [], deadline: null, createdAt: null,
}))

test.beforeEach(async ({ page }) => {
    // Block all non-local requests and mock ALL API paths, including warm-up/auth.
    await page.route('**/*', async route => {
        const url = new URL(route.request().url())
        if (url.hostname !== '127.0.0.1' && url.hostname !== 'localhost') return route.abort()
        if (!url.pathname.startsWith('/api/')) return route.continue()
        if (url.pathname === '/api/v1/jobs') {
            const number = Number(url.searchParams.get('page') || 1)
            const size = Number(url.searchParams.get('size') || 10)
            const experience = url.searchParams.get('experienceLevel')
            const jobs = fixtures.filter(job => !experience || job.experienceLevel === experience)
            return route.fulfill({ json: { statusCode: 200, data: {
                content: jobs.slice((number - 1) * size, number * size), pageNumber: number,
                pageSize: size, totalElements: jobs.length, totalPages: Math.ceil(jobs.length / size),
                isLast: number >= Math.ceil(jobs.length / size),
            } } })
        }
        const match = url.pathname.match(/^\/api\/v1\/jobs\/(\d+)$/)
        if (match) return route.fulfill({ json: { statusCode: 200, data: fixtures.find(job => job.id === Number(match[1])) } })
        return route.fulfill({ json: { statusCode: 200, data: [] } })
    })
})

test('TB-002 city is not duplicated on result cards', async ({ page }) => {
    await page.goto('/jobs')
    await expect(page.getByRole('article')).toHaveCount(10)
    const card = page.getByRole('article').first()
    await expect(card).toContainText('Địa điểm mô phỏng · Hà Nội')
    await expect(card).not.toContainText('Hà Nội · Hà Nội')
})

test('TB-003 both missing experience choices filter results', async ({ page }) => {
    await page.goto('/jobs')
    for (const [label, value, count] of [
        ['Không yêu cầu kinh nghiệm', 'NO_EXPERIENCE', 1], ['Dưới 1 năm', 'UNDER_1_YEAR', 101],
    ] as const) {
        await page.getByRole('button', { name: /Tất cả kinh nghiệm|Không yêu cầu kinh nghiệm|Dưới 1 năm/, exact: true }).click()
        await page.getByRole('option', { name: label, exact: true }).click()
        await page.getByRole('button', { name: 'Áp dụng bộ lọc', exact: true }).click()
        await expect(page).toHaveURL(new RegExp(`experienceLevel=${value}`))
        await expect(page.getByText(new RegExp(`Tìm thấy ${count} việc làm`))).toBeVisible()
    }
})

test('TB-006 Back restores page 3 after viewing job 82, including reload', async ({ page }, testInfo) => {
    await page.goto('/jobs')
    await page.getByRole('button', { name: '3', exact: true }).click()
    await expect(page.getByText(/Trang 3\/11/)).toBeVisible()
    await expect(page).toHaveURL(/page=3/)
    await page.getByRole('link', { name: 'QA job 82', exact: true }).click()
    await expect(page.getByRole('heading', { name: 'QA job 82', exact: true })).toBeVisible()
    await page.goBack()
    await expect(page.getByText(/Trang 3\/11/)).toBeVisible()
    await expect(page.getByRole('link', { name: 'QA job 82', exact: true })).toBeVisible()
    await page.reload()
    await expect(page.getByText(/Trang 3\/11/)).toBeVisible()
    await page.screenshot({ path: testInfo.outputPath('back-restores-page-3.png'), fullPage: true })
})

test('TB-006 Back also restores filters, sort and page size from URL', async ({ page }) => {
    await page.goto('/jobs?keyword=Java&location=Hà+Nội&sort=SALARY_DESC&page=3&size=20&minSalary=15000000')
    await expect(page.getByText(/Trang 3\/6/)).toBeVisible()
    await page.getByRole('link', { name: 'QA job 62', exact: true }).click()
    await expect(page.getByRole('heading', { name: 'QA job 62', exact: true })).toBeVisible()
    await page.goBack()
    await expect(page.getByText(/Trang 3\/6/)).toBeVisible()
    await expect(page.getByRole('searchbox', { name: 'Từ khóa' })).toHaveValue('Java')
    await expect(page.getByRole('searchbox', { name: 'Địa điểm' })).toHaveValue('Hà Nội')
    await expect(page.getByRole('spinbutton', { name: 'Lương tối thiểu' })).toHaveValue('15')
    await expect(page.getByRole('button', { name: 'Lương cao đến thấp', exact: true })).toBeVisible()
    await expect(page.getByRole('button', { name: '20 việc làm / trang', exact: true })).toBeVisible()
})
