import { expect, test } from '@playwright/test'

test('company request rejects incompatible website and logo values before sending, then submits valid URLs', async ({ page }) => {
    let submitCount = 0
    let submittedPayload: Record<string, unknown> | undefined

    await page.route('**/api/v1/recruiters/profile', async (route) => {
        const hasPendingCompany = submitCount > 0
        await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({
                statusCode: 200,
                message: 'Lấy hồ sơ nhà tuyển dụng thành công',
                data: {
                    id: 12,
                    userId: 12,
                    fullName: 'Nhà tuyển dụng E2E',
                    email: 'recruiter@fpt.com',
                    status: 'ACTIVE',
                    ...(hasPendingCompany && {
                        companyId: 101,
                        companyName: 'Công ty Vin',
                        companyStatus: 'PENDING',
                    }),
                },
            }),
        })
    })

    await page.route('**/api/v1/recruiters/companies/request-create', async (route) => {
        submitCount += 1
        submittedPayload = route.request().postDataJSON() as Record<string, unknown>
        await route.fulfill({
            status: 201,
            contentType: 'application/json',
            body: JSON.stringify({
                statusCode: 201,
                message: 'Yêu cầu đã được gửi',
                data: { id: 101, name: 'Công ty Vin', status: 'PENDING' },
            }),
        })
    })

    await page.goto('/login')
    await page.getByLabel('Email').fill('recruiter@fpt.com')
    await page.getByLabel('Mật khẩu').fill('Password123!')
    await Promise.all([
        page.waitForURL((url) => !url.pathname.endsWith('/login')),
        page.getByRole('button', { name: 'Đăng nhập' }).click(),
    ])
    await page.goto('/recruiter/company')
    await page.getByRole('button', { name: 'Bắt đầu tạo công ty' }).click()

    await page.getByLabel('Tên công ty đầy đủ').fill('Công ty Vin')
    await page.getByLabel('Website công ty').fill('recruiting@example.com')
    await page.getByLabel('Logo công ty (URL ảnh công khai, không bắt buộc)').fill('data:image/png;base64,aGVsbG8=')
    await page.getByRole('button', { name: 'Gửi yêu cầu xét duyệt' }).click()

    await expect(page.getByRole('alert').filter({ hasText: 'Website đầy đủ' })).toBeVisible()
    await expect(page.getByRole('alert').filter({ hasText: 'URL ảnh công khai' })).toBeVisible()
    expect(submitCount).toBe(0)

    await page.getByLabel('Website công ty').fill('https://company.example')
    await page.getByLabel('Logo công ty (URL ảnh công khai, không bắt buộc)').fill('https://cdn.example/company.png')
    await page.getByRole('button', { name: 'Gửi yêu cầu xét duyệt' }).click()

    await expect(page.getByText('Yêu cầu tạo doanh nghiệp đã được gửi thành công', { exact: false })).toBeVisible()
    await expect(page.getByRole('heading', { name: 'Công ty Vin' })).toBeVisible()
    await expect(page.getByRole('button', { name: 'Bắt đầu tạo công ty' })).toHaveCount(0)
    await expect(page.getByRole('link', { name: 'Tìm kiếm & nộp đơn' })).toHaveCount(0)
    expect(submitCount).toBe(1)
    expect(submittedPayload).toMatchObject({
        name: 'Công ty Vin',
        website: 'https://company.example',
        logoUrl: 'https://cdn.example/company.png',
    })
})
