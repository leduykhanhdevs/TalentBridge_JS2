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

test('recruiter must confirm before cancelling an applicant', async ({ page }) => {
    await installSession(page, 806, 'ROLE_RECRUITER')
    let stageUpdate: Record<string, unknown> | undefined
    const applicant = {
        id: 55, jobId: 77, jobTitle: 'Backend Engineer', candidateId: 12,
        candidateFullName: 'Ứng viên cần xác nhận', candidateEmail: 'cancel@example.invalid',
        candidateTitle: 'Backend Developer', currentStage: 'APPLIED', status: 'APPLIED',
        resumeId: 93, resumeFileName: 'candidate.pdf', appliedAt: '2026-10-20T00:00:00Z', notesCount: 0,
    }
    await page.route('**/api/v1/recruiters/jobs/77/applicants*', async (route) => route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(apiResponse([applicant])),
    }))
    await page.route('**/api/v1/recruiters/jobs/77/applicants/55/stage', async (route) => {
        stageUpdate = route.request().postDataJSON() as Record<string, unknown>
        await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify(apiResponse({ ...applicant, ...stageUpdate, currentStage: 'REJECTED', status: 'REJECTED' })),
        })
    })

    await page.goto('/recruiter/jobs/77/applicants')
    await page.getByRole('button', { name: 'Hủy ứng viên' }).click()
    const confirmation = page.getByRole('dialog', { name: 'Xác nhận hủy ứng viên' })
    await expect(confirmation).toContainText('Ứng viên cần xác nhận')
    await expect(confirmation.getByRole('button', { name: 'Xác nhận hủy' })).toBeVisible()
    expect(stageUpdate).toBeUndefined()

    await confirmation.getByRole('button', { name: 'Quay lại' }).click()
    await expect(page.getByRole('dialog')).toHaveCount(0)
    expect(stageUpdate).toBeUndefined()

    await page.getByRole('button', { name: 'Hủy ứng viên' }).click()
    await page.getByRole('dialog', { name: 'Xác nhận hủy ứng viên' }).getByRole('button', { name: 'Xác nhận hủy' }).click()
    await expect.poll(() => stageUpdate?.stage).toBe('REJECTED')
    await expect(page.getByText(/Đã hủy hồ sơ ứng tuyển/)).toBeVisible()
})

test('candidate verifies email and phone, then applies with their saved primary CV', async ({ page }) => {
    await installSession(page, 807, 'ROLE_CANDIDATE')
    const verification = { emailVerified: false, phoneVerified: false, maskedEmail: 'q***@example.invalid', maskedPhone: '***4567' }
    const resumes = [{
        id: 94, candidateId: 807, title: 'CV chính của tôi', fileName: 'my-cv.pdf',
        fileUrl: 'resumes/807/my-cv.pdf', fileType: 'application/pdf', resumeType: 'UPLOADED',
        isDefault: true, createdAt: '2026-10-21T00:00:00Z',
    }]
    let applicationRequest: Record<string, unknown> | undefined
    let currentVerification = { ...verification }

    await page.route('**/api/v1/jobs/102', async (route) => route.fulfill({
        status: 200, contentType: 'application/json',
        body: JSON.stringify(apiResponse({
            id: 102, companyId: 3, companyName: 'QA Company', title: 'QA Engineer',
            description: 'Test product behavior', requirements: 'Testing', benefits: 'Flexible',
            location: 'Hà Nội', jobType: 'FULL_TIME', experienceLevel: 'MID', minSalary: null,
            maxSalary: null, deadline: '2026-12-31', status: 'ACTIVE', isDemo: false,
            createdAt: '2026-10-01T00:00:00Z', updatedAt: null,
        })),
    }))
    await page.route('**/api/v1/ai/jobs/102/my-fit', async (route) => route.fulfill({
        status: 200, contentType: 'application/json',
        body: JSON.stringify(apiResponse({
            match: { matchPercentage: 75, matchingStrengths: ['Testing'], missingCriticalSkills: [] },
            profileCompletenessPercentage: 80,
            missingProfileSections: [],
            candidateOverallAssessment: 'Hồ sơ phù hợp ở mức tham khảo.',
            advisoryNotice: 'Kết quả AI chỉ mang tính tham khảo.',
            resumeAssessmentStatus: 'NO_RESUME',
        })),
    }))
    await page.route('**/api/v1/candidates/applications', async (route) => {
        if (route.request().method() === 'GET') {
            await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(apiResponse([])) })
            return
        }
        applicationRequest = route.request().postDataJSON() as Record<string, unknown>
        await route.fulfill({
            status: 201, contentType: 'application/json',
            body: JSON.stringify(apiResponse({ id: 950, jobId: 102, resumeId: 94, status: 'SUBMITTED', currentStage: 'APPLIED' }, 201)),
        })
    })
    await page.route('**/api/v1/candidates/contact-verification', async (route) => route.fulfill({
        status: 200, contentType: 'application/json', body: JSON.stringify(apiResponse(currentVerification)),
    }))
    await page.route('**/api/v1/candidates/contact-verification/*/request', async (route) => route.fulfill({
        status: 200, contentType: 'application/json', body: JSON.stringify(apiResponse(null)),
    }))
    await page.route('**/api/v1/candidates/contact-verification/*/verify', async (route) => {
        const channel = new URL(route.request().url()).pathname.split('/').at(-2)
        currentVerification = {
            ...currentVerification,
            ...(channel === 'EMAIL' ? { emailVerified: true } : { phoneVerified: true }),
        }
        await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(apiResponse(currentVerification)) })
    })
    await page.route('**/api/v1/candidates/resumes', async (route) => route.fulfill({
        status: 200, contentType: 'application/json', body: JSON.stringify(apiResponse(resumes)),
    }))

    await page.goto('/jobs/102')
    await page.getByRole('button', { name: 'Ứng tuyển ngay' }).first().click()
    const dialog = page.getByRole('dialog')
    await expect(dialog.getByRole('region', { name: 'Xác minh thông tin liên hệ' })).toBeVisible()
    await expect(dialog.getByText('Chọn CV ứng tuyển')).toHaveCount(0)

    await dialog.getByRole('button', { name: 'Gửi mã xác minh' }).nth(0).click()
    await dialog.getByLabel('Mã OTP Email').fill('123456')
    await dialog.getByRole('button', { name: 'Xác minh' }).nth(0).click()
    await expect(dialog.getByText('Đã xác minh').first()).toBeVisible()

    await dialog.getByRole('button', { name: 'Gửi mã xác minh' }).click()
    await dialog.getByLabel('Mã OTP Số điện thoại qua Zalo').fill('654321')
    await dialog.getByRole('button', { name: 'Xác minh' }).click()
    await expect(dialog.getByText('Chọn CV ứng tuyển')).toBeVisible()
    await expect(dialog.getByRole('region', { name: 'Xác minh thông tin liên hệ' })).toHaveCount(0)
    await expect(dialog.getByText('CV chính của tôi')).toBeVisible()
    await dialog.getByRole('button', { name: 'Xác nhận ứng tuyển' }).click()
    await expect.poll(() => applicationRequest?.resumeId).toBe(94)
    await expect(dialog.getByText('Ứng tuyển thành công!')).toBeVisible()
})

test('candidate can upload a CV from the application dialog when no saved CV exists', async ({ page }) => {
    await installSession(page, 809, 'ROLE_CANDIDATE')
    const uploadedResume = {
        id: 96, candidateId: 809, title: 'CV tải khi ứng tuyển', fileName: 'apply-now.pdf',
        fileUrl: 'resumes/809/apply-now.pdf', fileType: 'application/pdf', resumeType: 'UPLOADED',
        isDefault: false, createdAt: '2026-10-22T00:00:00Z',
    }
    let savedResumes: typeof uploadedResume[] = []
    let appliedResumeId: unknown

    await page.route('**/api/v1/jobs/103', async (route) => route.fulfill({
        status: 200, contentType: 'application/json',
        body: JSON.stringify(apiResponse({
            id: 103, companyId: 3, companyName: 'QA Company', title: 'Frontend QA Engineer',
            description: 'Test product behavior', requirements: 'Testing', benefits: 'Flexible',
            location: 'Hà Nội', jobType: 'FULL_TIME', experienceLevel: 'MID', minSalary: null,
            maxSalary: null, deadline: '2026-12-31', status: 'ACTIVE', isDemo: false,
            createdAt: '2026-10-01T00:00:00Z', updatedAt: null,
        })),
    }))
    await page.route('**/api/v1/ai/jobs/103/my-fit', async (route) => route.fulfill({
        status: 200, contentType: 'application/json',
        body: JSON.stringify(apiResponse({
            match: { matchPercentage: 50, matchingStrengths: [], missingCriticalSkills: [] },
            profileCompletenessPercentage: 50, missingProfileSections: [],
            candidateOverallAssessment: 'Đánh giá tham khảo.',
            advisoryNotice: 'Kết quả chỉ mang tính tham khảo.',
            resumeAssessmentStatus: 'NO_RESUME',
        })),
    }))
    await page.route('**/api/v1/candidates/applications', async (route) => {
        if (route.request().method() === 'GET') {
            await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(apiResponse([])) })
            return
        }
        appliedResumeId = (route.request().postDataJSON() as Record<string, unknown>).resumeId
        await route.fulfill({
            status: 201, contentType: 'application/json',
            body: JSON.stringify(apiResponse({ id: 951, jobId: 103, resumeId: uploadedResume.id, status: 'SUBMITTED', currentStage: 'APPLIED' }, 201)),
        })
    })
    await page.route('**/api/v1/candidates/contact-verification', async (route) => route.fulfill({
        status: 200, contentType: 'application/json',
        body: JSON.stringify(apiResponse({ emailVerified: true, phoneVerified: true, maskedEmail: 'q***@example.invalid', maskedPhone: '***4567' })),
    }))
    await page.route('**/api/v1/candidates/resumes', async (route) => route.fulfill({
        status: 200, contentType: 'application/json', body: JSON.stringify(apiResponse(savedResumes)),
    }))
    await page.route('**/api/v1/candidates/resumes/upload', async (route) => {
        savedResumes = [uploadedResume]
        await route.fulfill({ status: 201, contentType: 'application/json', body: JSON.stringify(apiResponse(uploadedResume, 201)) })
    })

    await page.goto('/jobs/103')
    await page.getByRole('button', { name: 'Ứng tuyển ngay' }).click()
    const dialog = page.getByRole('dialog')
    await expect(dialog.getByText('Bạn chưa có CV nào trong mục CV của tôi')).toBeVisible()
    await dialog.locator('input[type="file"]').last().setInputFiles({
        name: 'apply-now.pdf', mimeType: 'application/pdf', buffer: Buffer.from('%PDF-1.4\nQA application CV'),
    })
    await expect(dialog.getByText('CV tải khi ứng tuyển')).toBeVisible()
    await dialog.getByRole('button', { name: 'Xác nhận ứng tuyển' }).click()
    await expect.poll(() => appliedResumeId).toBe(uploadedResume.id)
    await expect(dialog.getByText('Ứng tuyển thành công!')).toBeVisible()
})

test('candidate importing a CV keeps the original file in My CV after profile sync', async ({ page }) => {
    await installSession(page, 808, 'ROLE_CANDIDATE')
    const profile = {
        id: 808, userId: 808, fullName: 'QA Candidate', email: 'qa-808@example.invalid', phone: '0901234567',
        avatarUrl: null, status: 'ACTIVE', title: null, dob: null, gender: 'OTHER', summary: null,
        experienceYears: 0, currentSalary: null, expectedSalary: null, city: null, address: null,
        personalWebsite: null, linkedinUrl: null, githubUrl: null, createdAt: null, updatedAt: null,
    }
    const savedResume = {
        id: 95, candidateId: 808, title: 'my-imported-cv', fileName: 'my-imported-cv.pdf',
        fileUrl: 'resumes/808/my-imported-cv.pdf', fileType: 'application/pdf', resumeType: 'UPLOADED',
        isDefault: true, createdAt: '2026-10-21T00:00:00Z',
    }
    let storedResumes: Array<typeof savedResume> = []
    let uploadRequests = 0
    let parseRequests = 0

    await page.route('**/api/v1/candidates/profile', async (route) => route.fulfill({
        status: 200, contentType: 'application/json', body: JSON.stringify(apiResponse(profile)),
    }))
    await page.route('**/api/v1/candidates/work-experiences', async (route) => route.fulfill({
        status: 200, contentType: 'application/json', body: JSON.stringify(apiResponse([])),
    }))
    await page.route('**/api/v1/candidates/skills', async (route) => route.fulfill({
        status: 200, contentType: 'application/json', body: JSON.stringify(apiResponse([])),
    }))
    await page.route('**/api/v1/skills', async (route) => route.fulfill({
        status: 200, contentType: 'application/json', body: JSON.stringify(apiResponse([])),
    }))
    await page.route('**/api/v1/candidates/resumes', async (route) => route.fulfill({
        status: 200, contentType: 'application/json', body: JSON.stringify(apiResponse(storedResumes)),
    }))
    await page.route('**/api/v1/candidates/resumes/upload', async (route) => {
        uploadRequests += 1
        storedResumes = [savedResume]
        await route.fulfill({ status: 201, contentType: 'application/json', body: JSON.stringify(apiResponse(savedResume, 201)) })
    })
    await page.route('**/api/v1/candidates/profile/parse-cv', async (route) => {
        parseRequests += 1
        await route.fulfill({
            status: 200, contentType: 'application/json',
            body: JSON.stringify(apiResponse({
                fullName: 'QA Candidate', phone: '0901234567', title: 'QA Engineer', city: 'Hà Nội',
                summary: 'Quality assurance', skills: ['Testing'], experiences: [],
            })),
        })
    })
    await page.route('**/api/v1/candidates/profile/apply-parsed-cv', async (route) => route.fulfill({
        status: 200, contentType: 'application/json', body: JSON.stringify(apiResponse(profile)),
    }))

    await page.goto('/candidate/profile')
    await page.getByRole('button', { name: 'Nhập nhanh từ CV' }).click()
    await page.locator('input[type="file"]').last().setInputFiles({
        name: 'my-imported-cv.pdf', mimeType: 'application/pdf', buffer: Buffer.from('%PDF-1.4\nQA CV'),
    })
    await expect(page.getByText(/KỸ NĂNG NHẬN DIỆN ĐƯỢC \(1\)/i)).toBeVisible()
    await page.getByRole('button', { name: 'Đồng bộ vào Hồ sơ TalentBridge' }).click()
    await expect(page.locator('p').filter({ hasText: /my-imported-cv\.pdf\s+• Tải lên ngày/ })).toBeVisible({ timeout: 10_000 })
    expect(uploadRequests).toBe(1)
    expect(parseRequests).toBe(1)
    await expect(page.getByRole('heading', { name: 'Nhập nhanh thông tin từ file CV' })).toHaveCount(0)
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
