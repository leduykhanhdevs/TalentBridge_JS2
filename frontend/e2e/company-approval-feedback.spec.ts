import { expect, test } from '@playwright/test'

test('lỗi phê duyệt hiển thị trong modal và người quản trị có thể thử lại', async ({ page }) => {
    await page.goto('/login')
    await page.getByLabel('Email').fill('admin@talentbridge.vn')
    await page.getByLabel('Mật khẩu').fill('AdminPassword123!')
    await Promise.all([
        page.waitForURL((url) => !url.pathname.endsWith('/login')),
        page.getByRole('button', { name: 'Đăng nhập' }).click(),
    ])

    const company = {
        id: 99001,
        name: 'Công ty kiểm thử phê duyệt',
        taxCode: '0101234567',
        city: 'Hà Nội',
        status: 'PENDING',
        isDemo: false,
    }
    let approved = false
    let approvalAttempts = 0

    await page.route('**/api/v1/admin/companies?*', async (route) => {
        await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({
                statusCode: 200,
                message: 'Tải thành công.',
                data: {
                    content: approved ? [] : [company],
                    pageNumber: 1,
                    pageSize: 10,
                    totalElements: approved ? 0 : 1,
                    totalPages: approved ? 0 : 1,
                    isLast: true,
                },
            }),
        })
    })
    await page.route('**/api/v1/admin/companies/99001/status', async (route) => {
        approvalAttempts += 1
        if (approvalAttempts === 1) {
            await route.fulfill({
                status: 409,
                contentType: 'application/json',
                body: JSON.stringify({
                    statusCode: 409,
                    message: 'Người tạo doanh nghiệp đã được liên kết với một công ty khác',
                }),
            })
            return
        }

        approved = true
        await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({
                statusCode: 200,
                message: 'Cập nhật trạng thái doanh nghiệp thành công.',
                data: { ...company, status: 'APPROVED' },
            }),
        })
    })

    await page.goto('/admin/companies')
    await page.getByRole('button', { name: 'Duyệt', exact: true }).click()

    const dialog = page.getByRole('dialog', { name: 'Xác nhận phê duyệt pháp nhân' })
    await expect(dialog).toBeVisible()
    await dialog.getByRole('button', { name: 'Xác nhận phê duyệt' }).click()
    await expect(dialog.getByRole('alert')).toContainText('Người tạo doanh nghiệp đã được liên kết với một công ty khác')
    await expect(dialog).toBeVisible()

    await dialog.getByRole('button', { name: 'Xác nhận phê duyệt' }).click()
    await expect(dialog).toHaveCount(0)
    await expect(page.getByText('Doanh nghiệp "Công ty kiểm thử phê duyệt" đã được phê duyệt thành công.')).toBeVisible()
    expect(approvalAttempts).toBe(2)
})
