import { describe, it, expect } from 'vitest'

const API_BASE = 'http://localhost:8080/api/v1'

describe('E2E Ecosystem Integration Test: Admin -> HR -> Candidate', () => {
    const timestamp = Date.now()
    const candidateEmail = `candidate_${timestamp}@talentbridge.test`
    const hr1Email = `hr1_${timestamp}@vinai.test`
    const hr2Email = `hr2_${timestamp}@vinai.test`
    const companyName = `VinAI Research Lab ${timestamp}`
    const taxCode = `MST-${timestamp.toString().slice(-8)}`

    let candidateToken = ''
    let candidateUserId = 0
    let adminToken = ''
    let hr1Token = ''
    let hr1CompanyId = 0
    let hr2Token = ''
    let joinRequestId = 0

    // Helper fetch wrapper
    async function apiRequest(endpoint: string, options: RequestInit = {}, token?: string) {
        const headers: Record<string, string> = {
            'Content-Type': 'application/json; charset=UTF-8',
            ...(options.headers as Record<string, string>),
        }
        if (token) {
            headers['Authorization'] = `Bearer ${token}`
        }
        const res = await fetch(`${API_BASE}${endpoint}`, {
            ...options,
            headers,
        })
        const data = await res.json().catch(() => null)
        return { status: res.status, ok: res.ok, data }
    }

    // --- PHASE 1: CANDIDATE LIFECYCLE ---
    describe('Phase 1: Candidate Lifecycle & Registration', () => {
        it('1.1. Should register a new Candidate account', async () => {
            const res = await apiRequest('/auth/register', {
                method: 'POST',
                body: JSON.stringify({
                    email: candidateEmail,
                    password: 'Password123!',
                    fullName: 'Lê Văn Ứng Viên',
                    phone: '0912345678',
                    role: 'ROLE_CANDIDATE',
                }),
            })

            expect(res.status).toBe(201)
            expect(res.data?.data?.accessToken).toBeDefined()
            expect(res.data?.data?.user?.email).toBe(candidateEmail)
            expect(res.data?.data?.user?.roles).toContain('ROLE_CANDIDATE')

            candidateToken = res.data.data.accessToken
            candidateUserId = res.data.data.user.id
        })

        it('1.2. Candidate should login successfully and acquire session token', async () => {
            const res = await apiRequest('/auth/login', {
                method: 'POST',
                body: JSON.stringify({
                    email: candidateEmail,
                    password: 'Password123!',
                }),
            })

            expect(res.status).toBe(200)
            expect(res.data?.data?.accessToken).toBeDefined()
            candidateToken = res.data.data.accessToken
        })

        it('1.3. Candidate cannot access Recruiter-only endpoints (RBAC 403)', async () => {
            const res = await apiRequest('/recruiters/profile', { method: 'GET' }, candidateToken)
            expect(res.status).toBe(403)
        })

        it('1.4. Candidate views and updates profile details (HRPM-14)', async () => {
            const getRes = await apiRequest('/candidates/profile', { method: 'GET' }, candidateToken)
            expect(getRes.status).toBe(200)
            expect(getRes.data?.data?.email).toBe(candidateEmail)

            const updateRes = await apiRequest(
                '/candidates/profile',
                {
                    method: 'PUT',
                    body: JSON.stringify({
                        fullName: 'Lê Văn Ứng Viên Pro',
                        phone: '0912345678',
                        title: 'Senior Fullstack Engineer',
                        experienceYears: 4,
                        currentSalary: 22000000,
                        expectedSalary: 35000000,
                        city: 'Đà Nẵng',
                        address: 'Hải Châu, Đà Nẵng',
                        summary: 'Lập trình viên đam mê công nghệ cao với kinh nghiệm Spring Boot & React.',
                        githubUrl: 'https://github.com/ungvienpro',
                        linkedinUrl: 'https://linkedin.com/in/ungvienpro',
                    }),
                },
                candidateToken,
            )

            expect(updateRes.status).toBe(200)
            expect(updateRes.data?.data?.fullName).toBe('Lê Văn Ứng Viên Pro')
            expect(updateRes.data?.data?.title).toBe('Senior Fullstack Engineer')
            expect(updateRes.data?.data?.experienceYears).toBe(4)
            expect(updateRes.data?.data?.city).toBe('Đà Nẵng')
        })
    })

    // --- PHASE 2: ADMIN OVERSIGHT ON CANDIDATE ---
    describe('Phase 2: Admin Authentication & Candidate Management', () => {
        it('2.1. Admin should login and acquire ROLE_ADMIN session', async () => {
            const res = await apiRequest('/auth/login', {
                method: 'POST',
                body: JSON.stringify({
                    email: 'admin@talentbridge.vn',
                    password: 'AdminPassword123!',
                }),
            })

            expect(res.status).toBe(200)
            expect(res.data?.data?.user?.roles).toContain('ROLE_ADMIN')
            adminToken = res.data.data.accessToken
        })

        it('2.2. Admin should view Candidates in Candidate Management Portal', async () => {
            const res = await apiRequest(
                `/admin/candidates?keyword=${encodeURIComponent(candidateEmail)}`,
                { method: 'GET' },
                adminToken,
            )

            expect(res.status).toBe(200)
            expect(res.data?.data?.content?.length).toBeGreaterThan(0)
            const candidateFound = res.data.data.content.find(
                (c: { email: string }) => c.email === candidateEmail,
            )
            expect(candidateFound).toBeDefined()
            expect(candidateFound.title).toBe('Senior Fullstack Engineer')
            expect(candidateFound.status).toBe('ACTIVE')
        })

        it('2.3. Admin should ban Candidate for policy violation', async () => {
            const res = await apiRequest(
                `/admin/users/${candidateUserId}/status`,
                {
                    method: 'PATCH',
                    body: JSON.stringify({
                        status: 'BANNED',
                        reason: 'E2E Automated Violation Test',
                    }),
                },
                adminToken,
            )

            expect(res.status).toBe(200)
        })

        it('2.4. Banned Candidate should be rejected upon login', async () => {
            const res = await apiRequest('/auth/login', {
                method: 'POST',
                body: JSON.stringify({
                    email: candidateEmail,
                    password: 'Password123!',
                }),
            })

            expect(res.status).toBe(403)
        })

        it('2.5. Admin should unban Candidate back to ACTIVE', async () => {
            const res = await apiRequest(
                `/admin/users/${candidateUserId}/status`,
                {
                    method: 'PATCH',
                    body: JSON.stringify({
                        status: 'ACTIVE',
                    }),
                },
                adminToken,
            )

            expect(res.status).toBe(200)

            // Verify login succeeds again
            const loginRes = await apiRequest('/auth/login', {
                method: 'POST',
                body: JSON.stringify({
                    email: candidateEmail,
                    password: 'Password123!',
                }),
            })
            expect(loginRes.status).toBe(200)
        })
    })

    // --- PHASE 3: HR1 ONBOARDING & COMPANY CREATION ---
    describe('Phase 3: HR1 Onboarding & Company Creation Request', () => {
        it('3.1. HR1 registers as ROLE_RECRUITER with position', async () => {
            const res = await apiRequest('/auth/register', {
                method: 'POST',
                body: JSON.stringify({
                    email: hr1Email,
                    password: 'Password123!',
                    fullName: 'Nguyễn HR Lead',
                    phone: '0988776655',
                    role: 'ROLE_RECRUITER',
                    position: 'Chief People Officer',
                }),
            })

            expect(res.status).toBe(201)
            expect(res.data?.data?.user?.roles).toContain('ROLE_RECRUITER')
            hr1Token = res.data.data.accessToken
        })

        it('3.2. HR1 profile initially has no company', async () => {
            const res = await apiRequest('/recruiters/profile', { method: 'GET' }, hr1Token)

            expect(res.status).toBe(200)
            expect(res.data?.data?.companyId).toBeNull()
            expect(res.data?.data?.position).toBe('Chief People Officer')
        })

        it('3.3. HR1 updates profile information', async () => {
            const res = await apiRequest(
                '/recruiters/profile',
                {
                    method: 'PUT',
                    body: JSON.stringify({
                        fullName: 'Nguyễn HR Lead Updated',
                        phone: '0988112233',
                        position: 'Head of Talent Acquisition',
                    }),
                },
                hr1Token,
            )

            expect(res.status).toBe(200)
            expect(res.data?.data?.fullName).toBe('Nguyễn HR Lead Updated')
            expect(res.data?.data?.phone).toBe('0988112233')
        })

        it('3.4. HR1 requests to create a new Company (PENDING)', async () => {
            const res = await apiRequest(
                '/recruiters/companies/request-create',
                {
                    method: 'POST',
                    body: JSON.stringify({
                        name: companyName,
                        taxCode: taxCode,
                        website: 'https://vinai-lab.io',
                        companySize: '100-500 nhân viên',
                        address: 'Tầng 12, Keangnam Landmark 72',
                        city: 'Hà Nội',
                        description: 'Viện nghiên cứu trí tuệ nhân tạo hàng đầu khu vực.',
                    }),
                },
                hr1Token,
            )

            expect(res.status).toBe(201)
            expect(res.data?.data?.name).toBe(companyName)
            expect(res.data?.data?.status).toBe('PENDING')
            hr1CompanyId = res.data.data.id
        })

        it('3.5. HR1 profile remains unlinked until Admin approves company', async () => {
            const res = await apiRequest('/recruiters/profile', { method: 'GET' }, hr1Token)

            expect(res.status).toBe(200)
            expect(res.data?.data?.companyId).toBeNull()
            expect(res.data?.data?.companyName).toBeNull()
        })
    })

    // --- PHASE 4: ADMIN APPROVAL OF COMPANY ---
    describe('Phase 4: Admin Company Review & Approval', () => {
        it('4.1. Admin finds pending company in Admin Portal', async () => {
            const res = await apiRequest(
                `/admin/companies?status=PENDING&keyword=${encodeURIComponent(companyName)}`,
                { method: 'GET' },
                adminToken,
            )

            expect(res.status).toBe(200)
            const found = res.data?.data?.content?.find((c: { id: number }) => c.id === hr1CompanyId)
            expect(found).toBeDefined()
            expect(found.status).toBe('PENDING')
        })

        it('4.2. Admin approves the company', async () => {
            const res = await apiRequest(
                `/admin/companies/${hr1CompanyId}/status`,
                {
                    method: 'PATCH',
                    body: JSON.stringify({
                        status: 'APPROVED',
                    }),
                },
                adminToken,
            )

            expect(res.status).toBe(200)
            expect(res.data?.data?.status).toBe('APPROVED')
        })

        it('4.3. HR1 profile now reflects APPROVED status', async () => {
            const res = await apiRequest('/recruiters/profile', { method: 'GET' }, hr1Token)

            expect(res.status).toBe(200)
            expect(res.data?.data?.companyId).toBe(hr1CompanyId)
            expect(res.data?.data?.companyName).toBe(companyName)
            expect(res.data?.data?.companyStatus).toBe('APPROVED')
        })
    })

    // --- PHASE 5: HR2 DISCOVERY & PEER APPROVAL ---
    describe('Phase 5: HR2 Join Company & HR1 Peer Approval', () => {
        it('5.1. HR2 registers as ROLE_RECRUITER without company', async () => {
            const res = await apiRequest('/auth/register', {
                method: 'POST',
                body: JSON.stringify({
                    email: hr2Email,
                    password: 'Password123!',
                    fullName: 'Trần Junior HR',
                    phone: '0933445566',
                    role: 'ROLE_RECRUITER',
                    position: 'Technical Recruiter',
                }),
            })

            expect(res.status).toBe(201)
            hr2Token = res.data.data.accessToken
        })

        it('5.2. HR2 searches approved companies and finds VinAI', async () => {
            const res = await apiRequest(
                `/recruiters/companies/search?keyword=${encodeURIComponent(companyName)}`,
                { method: 'GET' },
                hr2Token,
            )

            expect(res.status).toBe(200)
            const company = res.data?.data?.content?.find((c: { id: number }) => c.id === hr1CompanyId)
            expect(company).toBeDefined()
            expect(company.name).toBe(companyName)
        })

        it('5.3. HR2 submits join request to VinAI', async () => {
            const res = await apiRequest(
                `/recruiters/companies/${hr1CompanyId}/join-request`,
                {
                    method: 'POST',
                    body: JSON.stringify({
                        position: 'Senior AI Recruiter',
                        message: 'Mong muốn được đóng góp cho viện VinAI!',
                    }),
                },
                hr2Token,
            )

            expect(res.status).toBe(201)
            expect(res.data?.data?.status).toBe('PENDING')
            expect(res.data?.data?.companyName).toBe(companyName)
            joinRequestId = res.data.data.id
        })

        it('5.4. Duplicate join request should be rejected (409 Conflict)', async () => {
            const res = await apiRequest(
                `/recruiters/companies/${hr1CompanyId}/join-request`,
                {
                    method: 'POST',
                    body: JSON.stringify({
                        position: 'Duplicate Request',
                    }),
                },
                hr2Token,
            )

            expect(res.status).toBe(409)
        })

        it('5.5. HR1 reviews peer requests and finds HR2', async () => {
            const res = await apiRequest(
                '/recruiters/companies/my-company/join-requests?status=PENDING',
                { method: 'GET' },
                hr1Token,
            )

            expect(res.status).toBe(200)
            const reqFound = res.data?.data?.content?.find(
                (r: { id: number }) => r.id === joinRequestId,
            )
            expect(reqFound).toBeDefined()
            expect(reqFound.applicantEmail).toBe(hr2Email)
            expect(reqFound.position).toBe('Senior AI Recruiter')
        })

        it('5.6. HR1 approves HR2 join request', async () => {
            const res = await apiRequest(
                `/recruiters/companies/join-requests/${joinRequestId}`,
                {
                    method: 'PATCH',
                    body: JSON.stringify({
                        status: 'ACCEPTED',
                        reason: 'Chào mừng gia nhập team VinAI!',
                    }),
                },
                hr1Token,
            )

            expect(res.status).toBe(200)
            expect(res.data?.data?.status).toBe('ACCEPTED')
        })

        it('5.7. HR2 profile is immediately linked to VinAI with APPROVED status', async () => {
            const res = await apiRequest('/recruiters/profile', { method: 'GET' }, hr2Token)

            expect(res.status).toBe(200)
            expect(res.data?.data?.companyId).toBe(hr1CompanyId)
            expect(res.data?.data?.companyName).toBe(companyName)
            expect(res.data?.data?.companyStatus).toBe('APPROVED')
            expect(res.data?.data?.position).toBe('Senior AI Recruiter')
        })
    })

    // --- PHASE 6: ADMIN ECOSYSTEM VERIFICATION ---
    describe('Phase 6: Final Admin Ecosystem Verification', () => {
        it('6.1. Admin verifies both HRs belong to the newly approved company', async () => {
            const res = await apiRequest(
                `/admin/recruiters?keyword=${encodeURIComponent(companyName)}`,
                { method: 'GET' },
                adminToken,
            )

            expect(res.status).toBe(200)
            const recruiters = res.data?.data?.content || []
            const hr1Found = recruiters.find((r: { email: string }) => r.email === hr1Email)
            const hr2Found = recruiters.find((r: { email: string }) => r.email === hr2Email)

            expect(hr1Found).toBeDefined()
            expect(hr1Found.companyName).toBe(companyName)
            expect(hr2Found).toBeDefined()
            expect(hr2Found.companyName).toBe(companyName)
        })
    })
})
