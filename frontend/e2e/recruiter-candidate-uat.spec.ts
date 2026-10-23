import { expect, test, type Page } from '@playwright/test'

type UserRole = 'ROLE_CANDIDATE' | 'ROLE_RECRUITER'

async function installSession(page: Page, userId: number, role: UserRole) {
    await page.addInitScript(({ userId, role }: { userId: number; role: UserRole }) => {
        localStorage.setItem('talentbridge_access_token', `uat-token-${userId}`)
        localStorage.setItem('talentbridge_auth_user', JSON.stringify({
            id: userId,
            email: `uat-${userId}@example.invalid`,
            fullName: 'QA TalentBridge',
            status: 'ACTIVE',
            roles: [role],
        }))
    }, { userId, role })
}

function apiResponse(data: unknown, statusCode = 200) {
    return {
        statusCode,
        message: 'OK',
        data,
    }
}

const recruiterProfile = {
    id: 801,
    userId: 801,
    fullName: 'QA Recruiter',
    email: 'qa-recruiter@example.invalid',
    phone: '0901234567',
    status: 'ACTIVE',
    position: 'Talent Acquisition',
    companyId: 801,
    companyName: 'QA Company',
    companyStatus: 'APPROVED',
    createdAt: '2026-01-01T00:00:00Z',
}

test('recruiter can import and drop an avatar, then save the selected image', async ({ page }) => {
    await installSession(page, 801, 'ROLE_RECRUITER')

    let avatarUploads = 0
    let savedProfile: Record<string, unknown> | undefined
    await page.route('**/api/v1/recruiters/profile', async (route) => {
        if (route.request().method() === 'PUT') {
            savedProfile = route.request().postDataJSON() as Record<string, unknown>
            await route.fulfill({
                status: 200,
                contentType: 'application/json',
                body: JSON.stringify(apiResponse({ ...recruiterProfile, ...savedProfile })),
            })
            return
        }
        await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify(apiResponse(recruiterProfile)),
        })
    })
    await page.route('**/api/v1/recruiters/profile/avatar', async (route) => {
        avatarUploads += 1
        expect(route.request().headers().authorization).toBe('Bearer uat-token-801')
        await route.fulfill({
            status: 201,
            contentType: 'application/json',
            body: JSON.stringify(apiResponse({ avatarUrl: `https://cdn.example.test/avatar-${avatarUploads}.png` }, 201)),
        })
    })
    await page.route('https://cdn.example.test/**', (route) => route.fulfill({ status: 200, contentType: 'image/png', body: Buffer.from('qa-image') }))

    await page.goto('/recruiter/profile')
    await expect(page.getByRole('heading', { name: 'Hồ sơ Nhà tuyển dụng' })).toBeVisible()
    await page.locator('#hr-avatar-upload').setInputFiles({
        name: 'avatar-import.png',
        mimeType: 'image/png',
        buffer: Buffer.from('image-import'),
    })
    await expect(page.getByText('Ảnh đã tải lên. Nhấn “Lưu thay đổi” để cập nhật hồ sơ.')).toBeVisible()
    await expect(page.locator('#hr-avatar')).toHaveValue('https://cdn.example.test/avatar-1.png')

    await page.getByRole('button', { name: /Thả ảnh vào đây/ }).evaluate((dropTarget) => {
        const dataTransfer = new DataTransfer()
        dataTransfer.items.add(new File(['image-drop'], 'avatar-drop.png', { type: 'image/png' }))
        dropTarget.dispatchEvent(new DragEvent('drop', { bubbles: true, cancelable: true, dataTransfer }))
    })
    await expect(page.locator('#hr-avatar')).toHaveValue('https://cdn.example.test/avatar-2.png')
    await page.getByRole('button', { name: 'Lưu thay đổi' }).click()

    await expect.poll(() => savedProfile?.avatarUrl).toBe('https://cdn.example.test/avatar-2.png')
    await expect(page.getByRole('img', { name: 'QA Recruiter' })).toHaveAttribute('src', 'https://cdn.example.test/avatar-2.png')
    expect(avatarUploads).toBe(2)
})

test('recruiter assistant filters their ATS data, answers pipeline questions and exports the filtered CSV', async ({ page }) => {
    await installSession(page, 802, 'ROLE_RECRUITER')

    const applicants = [
        {
            id: 901, jobId: 88, jobTitle: 'Backend Engineer', candidateId: 1,
            candidateFullName: 'Ứng viên mới', candidateEmail: 'new@example.invalid', candidateTitle: 'Java Developer',
            currentStage: 'APPLIED', status: 'APPLIED', appliedAt: '2026-10-20T00:00:00Z', notesCount: 0,
        },
        {
            id: 902, jobId: 88, jobTitle: 'Backend Engineer', candidateId: 2,
            candidateFullName: 'Ứng viên phỏng vấn', candidateEmail: 'interview@example.invalid', candidateTitle: 'Backend Developer',
            currentStage: 'INTERVIEW', status: 'INTERVIEW', appliedAt: '2026-10-21T00:00:00Z', notesCount: 0,
        },
    ]
    let applicantFilters: URLSearchParams | undefined
    let assistantRequests = 0
    await page.route('**/api/v1/jobs/my-jobs*', async (route) => route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(apiResponse({
            content: [{ id: 88, companyId: 802, companyName: 'QA Company', title: 'Backend Engineer', description: 'Build APIs', city: 'Hà Nội', jobType: 'FULL_TIME', experienceLevel: 'MID', deadline: '2026-12-31', status: 'ACTIVE' }],
            pageNumber: 1, pageSize: 100, totalElements: 1, totalPages: 1, isLast: true,
        })),
    }))
    await page.route('**/api/v1/recruiters/jobs/88/applicants*', async (route) => {
        applicantFilters = new URL(route.request().url()).searchParams
        const filteredApplicants = applicantFilters.get('stage') === 'INTERVIEW'
            ? applicants.filter((applicant) => applicant.currentStage === 'INTERVIEW')
            : applicants
        await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(apiResponse(filteredApplicants)) })
    })
    await page.route('**/api/v1/assistant/chat', async (route) => {
        assistantRequests += 1
        await route.fulfill({ status: 500, contentType: 'application/json', body: JSON.stringify({ message: 'ATS query must stay local' }) })
    })

    await page.goto('/assistant')
    await expect(page.getByRole('heading', { name: 'Trợ lý nhà tuyển dụng', level: 1 })).toBeVisible()
    const tools = page.getByRole('region', { name: 'Công cụ tuyển dụng' })
    await expect(tools).toBeVisible()
    await expect(tools.getByText('2 ứng viên theo bộ lọc')).toBeVisible()
    await expect(tools).toContainText('Mới nộp: 1')
    await expect(tools).toContainText('Phỏng vấn: 1')

    await tools.getByLabel('Vòng tuyển dụng').selectOption('INTERVIEW')
    await tools.getByRole('button', { name: 'Áp dụng bộ lọc' }).click()
    await expect.poll(() => applicantFilters?.get('stage')).toBe('INTERVIEW')
    await expect(tools.getByText('1 ứng viên theo bộ lọc')).toBeVisible()
    await expect(tools.getByRole('list', { name: 'Danh sách ứng viên đã lọc' })).toContainText('Ứng viên phỏng vấn')

    await page.getByRole('button', { name: 'Có bao nhiêu ứng viên đang phỏng vấn?' }).click()
    await expect(page.getByText(/có 1 hồ sơ ở vòng phỏng vấn trong 1 hồ sơ/)).toBeVisible()
    const downloadPromise = page.waitForEvent('download')
    await tools.getByRole('button', { name: 'Xuất CSV' }).click()
    const download = await downloadPromise
    expect(download.suggestedFilename()).toMatch(/\.csv$/)
    expect(assistantRequests).toBe(0)
})

test('candidate sees an advisory job-fit assessment with its evidence and limitation', async ({ page }) => {
    await installSession(page, 803, 'ROLE_CANDIDATE')
    await page.route('**/api/v1/jobs/101', async (route) => route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(apiResponse({
            id: 101, companyId: 3, companyName: 'QA Company', title: 'Backend Developer',
            description: 'Build Java APIs', requirements: 'Java, Spring', benefits: 'Flexible work',
            location: 'Hà Nội', jobType: 'FULL_TIME', experienceLevel: 'MID', minSalary: null,
            maxSalary: null, deadline: '2026-12-31', status: 'ACTIVE', isDemo: false,
            createdAt: '2026-10-01T00:00:00Z', updatedAt: null,
        })),
    }))
    await page.route('**/api/v1/candidates/applications', async (route) => route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(apiResponse([])),
    }))
    await page.route('**/api/v1/ai/jobs/101/my-fit', async (route) => route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(apiResponse({
            match: {
                jobId: 101, jobTitle: 'Backend Developer', candidateId: 803, candidateName: 'QA Candidate',
                matchPercentage: 82, directKeywordScore: 0.8, contextualSimilarityScore: 0.75,
                inferredCapabilityScore: 0.6, inferredCapabilities: [], verificationQuestions: [],
                matchingStrengths: ['Java'], missingCriticalSkills: ['Docker'],
                recommendation: 'REVIEW', analysisSummary: 'So khớp tham khảo', matchingSource: 'DETERMINISTIC',
            },
            profileCompletenessPercentage: 67,
            missingProfileSections: ['CV'],
            candidateOverallAssessment: 'Có nhiều tiêu chí khớp theo hồ sơ hiện tại.',
            advisoryNotice: 'Kết quả tham khảo, không phải xác suất được tuyển.',
            resumeAssessmentStatus: 'NO_RESUME',
        })),
    }))

    await page.goto('/jobs/101')
    const assessment = page.getByRole('region', { name: 'Đánh giá mức độ phù hợp với công việc' })
    await expect(assessment).toBeVisible()
    await expect(assessment).toContainText('82%')
    await expect(assessment).toContainText('Java')
    await expect(assessment).toContainText('Docker')
    await expect(assessment).toContainText('không phải xác suất được tuyển')
    await expect(assessment).toContainText('NO_RESUME')
})

test('recruiter CV preview uses the authenticated, scoped download endpoint', async ({ page }) => {
    await installSession(page, 804, 'ROLE_RECRUITER')
    let authorization: string | undefined
    await page.route('**/api/v1/recruiters/jobs/77/applicants*', async (route) => route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(apiResponse([{
            id: 54, jobId: 77, jobTitle: 'Backend Engineer', candidateId: 11,
            candidateFullName: 'Ứng viên có CV', candidateEmail: 'cv@example.invalid',
            candidateTitle: 'Backend Developer', currentStage: 'APPLIED', status: 'APPLIED',
            resumeId: 92, resumeFileName: 'candidate.pdf', appliedAt: '2026-10-20T00:00:00Z', notesCount: 0,
        }])),
    }))
    await page.route('**/api/v1/recruiters/jobs/77/resumes/92/view', async (route) => {
        authorization = route.request().headers().authorization
        await route.fulfill({ status: 200, contentType: 'application/pdf', body: Buffer.from('%PDF-1.4\nQA CV') })
    })

    await page.goto('/recruiter/jobs/77/applicants')
    const popupPromise = page.waitForEvent('popup')
    await page.getByRole('button', { name: 'Xem CV' }).click()
    const preview = await popupPromise
    await expect.poll(() => authorization).toBe('Bearer uat-token-804')
    await expect(preview.getByTitle('Bản xem trước CV')).toHaveAttribute('src', /^blob:/)
    await expect(preview.getByRole('link', { name: /Tải CV/ })).toHaveAttribute('download', 'candidate.pdf')
})

test('recruiter without a company is directed to company linking before seeing jobs', async ({ page }) => {
    await installSession(page, 805, 'ROLE_RECRUITER')
    let myJobsRequests = 0
    await page.route('**/api/v1/recruiters/profile', async (route) => route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(apiResponse({ ...recruiterProfile, id: 805, userId: 805, companyId: null, companyName: null })),
    }))
    await page.route('**/api/v1/jobs/my-jobs*', async (route) => {
        myJobsRequests += 1
        await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(apiResponse({ content: [], totalElements: 0, totalPages: 0, pageNumber: 1, pageSize: 10 })) })
    })

    await page.goto('/recruiter/jobs')
    const dialog = page.getByRole('alertdialog')
    await expect(dialog).toContainText('Bạn cần liên kết doanh nghiệp trước khi tuyển dụng')
    await dialog.getByRole('button', { name: 'Đóng' }).click()
    await expect(page).toHaveURL(/\/recruiter\/company$/)
    expect(myJobsRequests).toBe(0)
})

test('a pending join request from one recruiter account is not shown to another account', async ({ page }) => {
    await installSession(page, 807, 'ROLE_RECRUITER')
    let pendingRequestLookups = 0
    await page.route('**/api/v1/recruiters/profile', async (route) => {
        const userId = route.request().headers().authorization === 'Bearer uat-token-808' ? 808 : 807
        await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify(apiResponse({ ...recruiterProfile, id: userId, userId, companyId: null, companyName: null })),
        })
    })
    await page.route('**/api/v1/recruiters/companies/my-pending-request', async (route) => {
        pendingRequestLookups += 1
        const isNewAccount = route.request().headers().authorization === 'Bearer uat-token-808'
        await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify(apiResponse(isNewAccount ? null : { id: 300, companyId: 3, companyName: 'Công ty cũ', position: 'Recruiter', status: 'PENDING' })),
        })
    })
    await page.route('**/api/v1/recruiters/companies/search*', async (route) => route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(apiResponse({ content: [], pageNumber: 1, pageSize: 9, totalElements: 0, totalPages: 0, isLast: true })),
    }))

    await page.goto('/recruiter/join-company')
    await expect(page.getByText('Bạn đang có yêu cầu xin gia nhập công ty Công ty cũ đang chờ xét duyệt')).toBeVisible()
    await page.getByRole('link', { name: 'Hồ sơ HR' }).click()
    await page.evaluate(() => {
        localStorage.setItem('talentbridge_access_token', 'uat-token-808')
        localStorage.setItem('talentbridge_auth_user', JSON.stringify({
            id: 808, email: 'uat-808@example.invalid', fullName: 'QA TalentBridge',
            status: 'ACTIVE', roles: ['ROLE_RECRUITER'],
        }))
        window.dispatchEvent(new Event('talentbridge_auth_change'))
    })
    await page.getByRole('link', { name: 'Tìm & gia nhập' }).click()
    await expect(page.getByRole('heading', { name: 'Tìm & Xin gia nhập Doanh nghiệp' })).toBeVisible()
    await expect(page.getByText('Bạn đang có yêu cầu xin gia nhập công ty Công ty cũ đang chờ xét duyệt')).toHaveCount(0)
    expect(pendingRequestLookups).toBe(2)
})

test('candidate calendar download authenticates, saves an ICS file and shows API errors', async ({ page }) => {
    await installSession(page, 806, 'ROLE_CANDIDATE')
    let calendarRequests = 0
    let authorization: string | undefined
    await page.route('**/api/v1/candidates/applications', async (route) => route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(apiResponse([{
            id: 44, jobId: 88, jobTitle: 'Backend Engineer', companyId: 802, companyName: 'QA Company',
            companyLogo: null, location: 'Hà Nội', city: 'Hà Nội', jobType: 'FULL_TIME', experienceLevel: 'MID',
            minSalary: null, maxSalary: null, isNegotiable: null, resumeId: null, resumeFileName: null,
            resumeFileUrl: null, coverLetter: null, currentStage: 'INTERVIEW', status: 'INTERVIEW',
            aiMatchScore: null, appliedAt: '2026-10-20T00:00:00Z',
        }])),
    }))
    await page.route('**/api/v1/candidates/applications/44/interview', async (route) => route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(apiResponse({
            id: 9, applicationId: 44, jobId: 88, jobTitle: 'Backend Engineer', companyName: 'QA Company',
            candidateName: 'QA TalentBridge', candidateEmail: 'candidate@example.invalid',
            interviewTime: '2026-10-30T14:00:00', locationType: 'ONLINE',
            meetingLinkOrAddress: 'https://meet.example.test', notes: 'Interview notes',
            status: 'SCHEDULED', googleCalendarUrl: 'https://calendar.google.com/calendar/render',
            createdAt: '2026-10-23T00:00:00Z',
        })),
    }))
    await page.route('**/api/v1/interviews/9/calendar.ics', async (route) => {
        calendarRequests += 1
        authorization = route.request().headers().authorization
        if (calendarRequests > 1) {
            await route.fulfill({ status: 403, contentType: 'application/json', body: JSON.stringify({ statusCode: 40301, message: 'Bạn không có quyền tải lịch này.' }) })
            return
        }
        await route.fulfill({ status: 200, contentType: 'text/calendar; charset=UTF-8', body: 'BEGIN:VCALENDAR\r\nEND:VCALENDAR\r\n' })
    })

    await page.goto('/candidate/applications')
    await expect(page.getByText('Lịch phỏng vấn', { exact: false }).first()).toBeVisible()
    const downloadPromise = page.waitForEvent('download')
    await page.getByRole('button', { name: 'Tải file lịch iCalendar (.ics)' }).click()
    const download = await downloadPromise
    expect(download.suggestedFilename()).toBe('TalentBridge-phong-van-9.ics')
    expect(authorization).toBe('Bearer uat-token-806')

    await page.getByRole('button', { name: 'Tải file lịch iCalendar (.ics)' }).click()
    await expect(page.getByRole('alert')).toContainText('Bạn không có quyền tải lịch này.')
})
