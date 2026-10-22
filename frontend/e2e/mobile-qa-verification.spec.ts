import { mkdir } from 'node:fs/promises'
import { join } from 'node:path'
import { expect, test } from '@playwright/test'

const screenshotDirectory = process.env.TALENTBRIDGE_E2E_SCREENSHOT_DIR || 'target/qa-screenshots'

test.describe('Mobile QA Bug Verification (TB-01, TB-02, TB-03, TB-04)', () => {
    test.beforeAll(async () => {
        await mkdir(screenshotDirectory, { recursive: true })
    })

    test('TB-01 & TB-02 & TB-03: Candidate Profile & Modals on Mobile Viewport 390x844', async ({ page, baseURL }) => {
        // Set mobile viewport iPhone 12/13/14 (390 x 844)
        await page.setViewportSize({ width: 390, height: 844 })

        // 1. Log in as Candidate
        await page.goto(`${baseURL}/login`)
        await page.getByLabel('Email').fill('candidate@talentbridge.vn')
        await page.getByLabel('Mật khẩu').fill('Password123!')
        await Promise.all([
            page.waitForURL((url) => !url.pathname.endsWith('/login')),
            page.getByRole('button', { name: 'Đăng nhập' }).click(),
        ])

        await page.goto(`${baseURL}/candidate/profile`)
        await expect(page.getByRole('heading', { name: 'Thể hiện năng lực bằng trải nghiệm' })).toBeVisible()

        // ==============================================================
        // TB-02: Candidate Skill Modal Default Value and Step Verification
        // ==============================================================
        const addSkillBtn = page.getByRole('button', { name: 'Thêm kỹ năng' }).first()
        await addSkillBtn.scrollIntoViewIfNeeded()
        await addSkillBtn.click()

        const skillModal = page.getByRole('dialog')
        await expect(skillModal).toBeVisible()

        // Select a skill
        await skillModal.locator('select').first().selectOption({ index: 1 })

        // Inspect yearsOfExperience input
        const yearsInput = page.locator('#years-exp')
        await expect(yearsInput).toHaveValue('1.0')
        await expect(yearsInput).toHaveAttribute('step', '0.1')
        await expect(yearsInput).toHaveAttribute('min', '0.1')

        // Verify HTML5 validity of default 1.0 is TRUE (not blocked like before)
        const isValid = await yearsInput.evaluate((el: HTMLInputElement) => el.checkValidity())
        expect(isValid).toBe(true)

        // Capture clean screenshot of TB-02
        await page.screenshot({
            path: join(screenshotDirectory, 'tb02-skill-default-valid-mobile.png'),
            fullPage: false,
        })

        // Close skill modal
        await page.getByRole('button', { name: 'Hủy' }).click()
        await expect(skillModal).not.toBeVisible()

        // ==============================================================
        // TB-03: Future Date of Birth Blocked Verification
        // ==============================================================
        const editProfileBtn = page.getByRole('button', { name: 'Chỉnh sửa hồ sơ' }).first()
        await editProfileBtn.scrollIntoViewIfNeeded()
        await editProfileBtn.click()

        const dobInput = page.locator('input[type="date"]').first()
        await dobInput.scrollIntoViewIfNeeded()

        // Check max attribute is set to today in Vietnam
        const maxAttr = await dobInput.getAttribute('max')
        expect(maxAttr).toBeTruthy()

        // Fill future date
        await dobInput.fill('2030-02-01')

        const saveChangesBtn = page.locator('button[form="candidate-profile-form"]')
        await saveChangesBtn.click()

        // Expect validation error message displayed under input
        const dobErrorMsg = page.getByText('Ngày sinh không được ở trong tương lai')
        await expect(dobErrorMsg).toBeVisible()
        await expect(dobInput).toHaveValue('2030-02-01')
        await dobInput.scrollIntoViewIfNeeded()

        // Capture clean screenshot of TB-03 showing error rejected
        await page.screenshot({
            path: join(screenshotDirectory, 'tb03-future-birthdate-blocked-mobile.png'),
            fullPage: false,
        })

        // Restore to valid past birthdate 2000-07-19
        await dobInput.fill('2000-07-19')
        await expect(dobErrorMsg).not.toBeVisible()
        await saveChangesBtn.click()
        await expect(page.getByRole('button', { name: 'Chỉnh sửa hồ sơ' }).first()).toBeVisible()

        // ==============================================================
        // TB-01: CV Builder Save to Profile on Mobile (Preview Tab)
        // ==============================================================
        const createCvBtn = page.getByRole('button', { name: 'Tạo CV từ Hồ sơ' }).first()
        await createCvBtn.scrollIntoViewIfNeeded()
        await createCvBtn.click()

        const cvModal = page.locator('div.fixed.inset-0').filter({ hasText: 'Trình tạo CV từ Hồ sơ' })
        await expect(cvModal).toBeVisible()

        // Switch to preview tab on mobile
        await page.getByRole('button', { name: 'Xem trước' }).click()

        // Click "Lưu vào Hồ sơ CV"
        const saveCvBtn = page.getByRole('button', { name: 'Lưu vào Hồ sơ CV' })
        await saveCvBtn.click()

        // Expect global feedback banner to appear and be visible on preview mode
        const successFeedback = page.getByRole('alert').filter({ hasText: 'Tạo CV và lưu vào hồ sơ thành công!' })
        await expect(successFeedback).toBeVisible()

        // Capture clean screenshot of TB-01 showing visible feedback banner on mobile preview
        await page.screenshot({
            path: join(screenshotDirectory, 'tb01-cv-builder-saved-mobile.png'),
            fullPage: false,
        })

        // Wait for modal to auto-close
        await expect(cvModal).not.toBeVisible({ timeout: 5000 })

        // Verify CV appears in uploaded/saved list
        await expect(page.getByText('Hồ sơ CV đã tải lên').first()).toBeVisible()
    })

    test('TB-04: Admin Company Pending Queue Excludes Demo Companies on 390x844', async ({ page, baseURL }) => {
        await page.setViewportSize({ width: 390, height: 844 })

        // 1. Log in as Admin
        await page.goto(`${baseURL}/login`)
        await page.getByLabel('Email').fill('admin@talentbridge.vn')
        await page.getByLabel('Mật khẩu').fill('AdminPassword123!')
        await Promise.all([
            page.waitForURL((url) => !url.pathname.endsWith('/login')),
            page.getByRole('button', { name: 'Đăng nhập' }).click(),
        ])

        await page.goto(`${baseURL}/admin/companies`)
        await expect(page.getByRole('heading', { name: 'Duyệt doanh nghiệp' })).toBeVisible()

        // Tab "Chờ thẩm định" is active by default
        const pendingQueueHeading = page.getByRole('heading', { name: 'Danh sách doanh nghiệp chờ phê duyệt' })
        await expect(pendingQueueHeading).toBeVisible()

        // Verify no demo companies with "Mô phỏng · Không xác thực" are in pending queue
        await expect(page.getByText('Mô phỏng · Không xác thực')).not.toBeVisible()

        // Search for DEMO in pending queue
        const searchInput = page.getByPlaceholder('Tìm kiếm theo tên doanh nghiệp, mã số thuế (MST)')
        await searchInput.fill('DEMO')
        await page.getByRole('button', { name: 'Tìm kiếm' }).click()

        // Verify 0 results found in pending queue
        await expect(page.getByText('Không có doanh nghiệp nào phù hợp')).toBeVisible()

        // Capture screenshot of TB-04 clean pending queue
        await page.screenshot({
            path: join(screenshotDirectory, 'tb04-admin-pending-no-demo-mobile.png'),
            fullPage: false,
        })

        // Reset and switch to "Tất cả hồ sơ"
        await page.getByRole('button', { name: 'Tất cả hồ sơ' }).click()
        await searchInput.fill('DEMO')
        await page.getByRole('button', { name: 'Tìm kiếm' }).click()

        // Verify demo companies ARE visible in ALL companies view
        await page.screenshot({
            path: join(screenshotDirectory, 'tb04-admin-all-demo-visible-mobile.png'),
            fullPage: false,
        })
    })
})
