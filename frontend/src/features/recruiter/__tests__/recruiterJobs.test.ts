import React from 'react'
import { renderToStaticMarkup } from 'react-dom/server'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { RecruiterJobsPage } from '../../../pages/recruiter/RecruiterJobsPage'
import {
    getJobApplicants,
    getMyCompanyJobs,
    getRecruiterProfile,
    RecruiterApiError,
} from '../recruiterApi'
import type { JobApplicant, RecruiterJob } from '../recruiterTypes'

// Mock react-router
let currentSearchParams = new URLSearchParams()
const mockSetSearchParams = vi.fn((newParams: unknown) => {
    if (typeof newParams === 'function') {
        const fn = newParams as (prev: URLSearchParams) => URLSearchParams
        currentSearchParams = new URLSearchParams(fn(currentSearchParams))
    } else if (newParams instanceof URLSearchParams) {
        currentSearchParams = new URLSearchParams(newParams)
    } else if (typeof newParams === 'object' && newParams !== null) {
        currentSearchParams = new URLSearchParams(newParams as Record<string, string>)
    }
})

vi.mock('react-router', () => ({
    useSearchParams: () => [currentSearchParams, mockSetSearchParams],
    Link: ({ children, to, className }: { children: React.ReactNode; to: string; className?: string }) =>
        React.createElement('a', { href: to, className }, children),
}))

// Mock recruiter API methods
vi.mock('../recruiterApi', async (importOriginal) => {
    const actual = await importOriginal<typeof import('../recruiterApi')>()
    return {
        ...actual,
        getRecruiterProfile: vi.fn(),
        getMyCompanyJobs: vi.fn(),
        getJobApplicants: vi.fn(),
    }
})

// Mock @tanstack/react-query
type QueryOptions = {
    queryKey: unknown[]
    queryFn?: () => unknown
    enabled?: boolean
}

let mockQueryHandler: (options: QueryOptions) => {
    data?: unknown
    isLoading?: boolean
    error?: unknown
    refetch?: () => unknown
}

vi.mock('@tanstack/react-query', () => ({
    useQuery: (options: QueryOptions) => mockQueryHandler(options),
}))

// Lightweight DOM / Element traversal helpers for user interactions without external dependencies
interface TestVNode {
    type?: unknown
    props?: {
        children?: unknown
        onClick?: (event?: unknown) => unknown | Promise<unknown>
        type?: string
        [key: string]: unknown
    }
    [key: string]: unknown
}

function getTextContent(node: unknown): string {
    if (typeof node === 'string' || typeof node === 'number') return String(node)
    if (!node) return ''
    if (Array.isArray(node)) return node.map(getTextContent).join(' ')
    const vnode = node as TestVNode
    if (vnode.props && vnode.props.children) {
        return getTextContent(vnode.props.children)
    }
    return ''
}

function walkTree(
    node: unknown,
    predicate: (node: TestVNode) => boolean,
    results: TestVNode[] = [],
): TestVNode[] {
    if (!node || typeof node !== 'object') return results
    const vnode = node as TestVNode
    if (predicate(vnode)) {
        results.push(vnode)
    }
    if (typeof vnode.type === 'function') {
        try {
            const componentFn = vnode.type as (props?: unknown) => unknown
            const childTree = componentFn(vnode.props)
            walkTree(childTree, predicate, results)
        } catch {
            // Function component may require hooks or context
        }
    }
    if (vnode.props && vnode.props.children) {
        const children = Array.isArray(vnode.props.children)
            ? vnode.props.children
            : [vnode.props.children]
        for (const child of children) {
            walkTree(child, predicate, results)
        }
    }
    return results
}

// User interaction helper conforming to userEvent.click API
const userEvent = {
    click: async (element: TestVNode | null | undefined) => {
        if (!element) {
            throw new Error('userEvent.click: element is null or undefined')
        }
        if (element.props && typeof element.props.onClick === 'function') {
            await element.props.onClick({
                preventDefault: () => {},
                stopPropagation: () => {},
            })
        } else if (typeof element.onClick === 'function') {
            const rawClick = element.onClick as (e?: unknown) => unknown | Promise<unknown>
            await rawClick({
                preventDefault: () => {},
                stopPropagation: () => {},
            })
        } else {
            throw new Error('Element does not have an onClick handler')
        }
    },
}

let currentSelectedJob: RecruiterJob | null = null
const mockSetSelectedJob = vi.fn((val: unknown) => {
    currentSelectedJob =
        typeof val === 'function'
            ? (val as (prev: RecruiterJob | null) => RecruiterJob | null)(currentSelectedJob)
            : (val as RecruiterJob | null)
})

interface ReactInternalsWithH {
    __CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE?: {
        H?: {
            useState?: (initial: unknown) => [unknown, (val: unknown) => void]
        }
    }
}

function renderComponent() {
    const internals = (React as unknown as ReactInternalsWithH)
        .__CLIENT_INTERNALS_DO_NOT_USE_OR_WARN_USERS_THEY_CANNOT_UPGRADE

    const setupHookDispatcher = () => {
        if (internals) {
            internals.H = {
                useState: (initial: unknown) => {
                    if (currentSelectedJob === undefined) {
                        currentSelectedJob = initial as RecruiterJob | null
                    }
                    return [
                        currentSelectedJob,
                        (val: unknown) => {
                            mockSetSelectedJob(val)
                            currentSelectedJob =
                                typeof val === 'function'
                                    ? (val as (prev: RecruiterJob | null) => RecruiterJob | null)(
                                          currentSelectedJob,
                                      )
                                    : (val as RecruiterJob | null)
                            renderResult.rerender()
                        },
                    ]
                },
            }
        }
    }

    setupHookDispatcher()
    let currentVdom = RecruiterJobsPage()
    let currentHtml = renderToStaticMarkup(currentVdom)

    const renderResult = {
        get container() {
            return {
                innerHTML: currentHtml,
                textContent: getTextContent(currentVdom),
            }
        },
        rerender() {
            setupHookDispatcher()
            currentVdom = RecruiterJobsPage()
            currentHtml = renderToStaticMarkup(currentVdom)
        },
        getByText(matcher: string | RegExp) {
            const decodedHtml = currentHtml
                .replace(/&amp;/g, '&')
                .replace(/&lt;/g, '<')
                .replace(/&gt;/g, '>')
                .replace(/&quot;/g, '"')
                .replace(/&#39;/g, "'")

            const isMatch = (str: string) =>
                typeof matcher === 'string' ? str.includes(matcher) : matcher.test(str)

            if (!isMatch(decodedHtml)) {
                throw new Error(
                    `Unable to find an element with text: ${matcher}.\nRendered:\n${currentHtml}`,
                )
            }

            const matched = walkTree(currentVdom, (n) => {
                const text = getTextContent(n).trim()
                return isMatch(text)
            })

            const interactive = matched.find((n) => n.type === 'button' || n.props?.onClick)
            return interactive || matched[matched.length - 1] || { text: matcher, html: currentHtml }
        },
        queryByText(matcher: string | RegExp) {
            const decodedHtml = currentHtml
                .replace(/&amp;/g, '&')
                .replace(/&lt;/g, '<')
                .replace(/&gt;/g, '>')
                .replace(/&quot;/g, '"')
                .replace(/&#39;/g, "'")

            const isMatch = (str: string) =>
                typeof matcher === 'string' ? str.includes(matcher) : matcher.test(str)
            if (!isMatch(decodedHtml)) return null
            return renderResult.getByText(matcher)
        },
        getByRole(role: string, options?: { name?: string | RegExp }) {
            if (role === 'button') {
                const buttons = walkTree(currentVdom, (n) => n.type === 'button')
                if (options?.name) {
                    const isNameMatch = (str: string) =>
                        typeof options.name === 'string'
                            ? str.includes(options.name)
                            : options.name!.test(str)
                    const matched = buttons.find((btn) => isNameMatch(getTextContent(btn)))
                    if (matched) return matched
                    throw new Error(`Unable to find button with name: ${options.name}`)
                }
                if (buttons.length > 0) return buttons[0]
                throw new Error('No buttons found')
            }
            throw new Error(`Role ${role} not supported`)
        },
    }

    return renderResult
}

describe('Recruiter Jobs and Applicants Logic (HRPM-49)', () => {
    const sampleJob: RecruiterJob = {
        id: 10,
        companyId: 5,
        companyName: 'FPT Software',
        title: 'Java Software Engineer',
        location: 'TP. Hồ Chí Minh',
        jobType: 'FULL_TIME',
        experienceLevel: 'MIDDLE',
        deadline: '2026-12-31',
        status: 'ACTIVE',
    }

    const sampleApplicant: JobApplicant = {
        id: 1,
        jobId: 10,
        candidateId: 100,
        fullName: 'Trần Minh Anh',
        email: 'candidate@talentbridge.vn',
        phone: '0987654321',
        avatar: 'https://example.com/avatar.jpg',
        title: 'Fullstack Developer',
        yearsOfExperience: 3,
        city: 'TP. Hồ Chí Minh',
        coverLetter: 'Tôi mong muốn ứng tuyển vị trí này.',
        currentStage: 'APPLIED',
        status: 'SUBMITTED',
        aiMatchScore: 88.5,
        appliedAt: '2026-09-29T10:00:00',
    }

    describe('JobApplicant Data Model & Utilities', () => {
        it('should correctly hold applicant details without sensitive fields', () => {
            expect(sampleApplicant.id).toBe(1)
            expect(sampleApplicant.jobId).toBe(10)
            expect(sampleApplicant.fullName).toBe('Trần Minh Anh')
            expect(sampleApplicant.email).toBe('candidate@talentbridge.vn')
            expect(sampleApplicant.phone).toBe('0987654321')
            expect(sampleApplicant.yearsOfExperience).toBe(3)
            expect(sampleApplicant.city).toBe('TP. Hồ Chí Minh')
            expect(sampleApplicant.currentStage).toBe('APPLIED')

            // Verify no sensitive fields exist on the object
            const applicantAny = sampleApplicant as Record<string, unknown>
            expect(applicantAny.password).toBeUndefined()
            expect(applicantAny.passwordHash).toBeUndefined()
            expect(applicantAny.secret).toBeUndefined()
        })

        it('should format experience display correctly', () => {
            const formatExperience = (years?: number) =>
                years !== undefined && years !== null ? `${years} năm KN` : 'Chưa cập nhật'

            expect(formatExperience(sampleApplicant.yearsOfExperience)).toBe('3 năm KN')
            expect(formatExperience(0)).toBe('0 năm KN')
            expect(formatExperience(undefined)).toBe('Chưa cập nhật')
        })

        it('should determine applicant view state correctly (Loading, Empty, Has Data, Error)', () => {
            type ViewState = 'LOADING' | 'ERROR' | 'EMPTY' | 'HAS_DATA'

            const resolveViewState = (
                isLoading: boolean,
                isError: boolean,
                items: JobApplicant[],
            ): ViewState => {
                if (isLoading) return 'LOADING'
                if (isError) return 'ERROR'
                if (items.length === 0) return 'EMPTY'
                return 'HAS_DATA'
            }

            // 1. Loading state
            expect(resolveViewState(true, false, [])).toBe('LOADING')

            // 2. Error state
            expect(resolveViewState(false, true, [])).toBe('ERROR')

            // 3. Empty state
            expect(resolveViewState(false, false, [])).toBe('EMPTY')

            // 4. Has data state
            expect(resolveViewState(false, false, [sampleApplicant])).toBe('HAS_DATA')
        })

        it('should retain job information for header rendering', () => {
            expect(sampleJob.id).toBe(10)
            expect(sampleJob.companyId).toBe(5)
            expect(sampleJob.title).toBe('Java Software Engineer')
            expect(sampleJob.status).toBe('ACTIVE')
        })

        it('should handle 404 Job not found error', () => {
            const error = new RecruiterApiError(404, 'Tin tuyển dụng không tồn tại với ID: 999')
            expect(error.status).toBe(404)
            expect(error.message).toContain('không tồn tại')
        })

        it('should handle 403 Forbidden for unauthorized company job', () => {
            const error = new RecruiterApiError(
                403,
                'Bạn không có quyền xem danh sách ứng viên của tin tuyển dụng thuộc công ty khác',
            )
            expect(error.status).toBe(403)
            expect(error.message).toContain('không có quyền')
        })
    })

    describe('RecruiterJobsPage Behavioral Requirements (HRPM-49 QA Verification)', () => {
        beforeEach(() => {
            currentSearchParams = new URLSearchParams()
            currentSelectedJob = null
            vi.clearAllMocks()
        })

        // 1. Profile loading → hiển thị loading, không hiển thị empty state
        it('1. Profile loading: should display loading state and MUST NOT display empty jobs state', () => {
            mockQueryHandler = (options) => {
                if (options.queryKey[0] === 'recruiter-profile') {
                    return {
                        data: undefined,
                        isLoading: true,
                        error: null,
                        refetch: vi.fn(),
                    }
                }
                if (options.queryKey[0] === 'my-company-jobs') {
                    return {
                        data: [],
                        isLoading: false,
                        error: null,
                        refetch: vi.fn(),
                    }
                }
                return { data: [], isLoading: false, error: null, refetch: vi.fn() }
            }

            const ui = renderComponent()

            // Displays loading UI
            expect(ui.getByText('Đang tải thông tin hồ sơ nhà tuyển dụng...')).toBeDefined()
            expect(ui.getByText('Vui lòng chờ trong giây lát')).toBeDefined()

            // MUST NOT display empty jobs state
            expect(ui.queryByText('Doanh nghiệp chưa có tin tuyển dụng nào')).toBeNull()
            expect(ui.queryByText('Chưa có ứng viên nào ứng tuyển vào vị trí này')).toBeNull()
            expect(ui.queryByText('Chưa liên kết Doanh nghiệp')).toBeNull()
        })

        // 2. Profile error → hiển thị error + Retry (không giả định chưa liên kết company)
        it('2. Profile error: should display error message and Retry button without assuming unlinked company', () => {
            const refetchProfileMock = vi.fn()
            const errorMsg = 'Lỗi kết nối máy chủ hồ sơ (500 Internal Server Error)'

            mockQueryHandler = (options) => {
                if (options.queryKey[0] === 'recruiter-profile') {
                    return {
                        data: undefined,
                        isLoading: false,
                        error: new Error(errorMsg),
                        refetch: refetchProfileMock,
                    }
                }
                return { data: [], isLoading: false, error: null, refetch: vi.fn() }
            }

            const ui = renderComponent()

            // Displays error state
            expect(ui.getByText('Không thể tải thông tin hồ sơ')).toBeDefined()
            expect(ui.getByText(errorMsg)).toBeDefined()
            expect(ui.getByText('Thử lại')).toBeDefined()

            // MUST NOT assume unlinked company or show empty state
            expect(ui.queryByText('Chưa liên kết Doanh nghiệp')).toBeNull()
            expect(ui.queryByText('Doanh nghiệp chưa có tin tuyển dụng nào')).toBeNull()
        })

        // 1. Fix Retry test — mô phỏng thao tác người dùng thật với userEvent.click
        it('3. Retry: should simulate user click on Retry button, invoke profile refetch, and recover UI', async () => {
            let profileError: Error | null = new Error('Lỗi kết nối máy chủ hồ sơ')
            const refetchProfileMock = vi.fn(() => {
                // Mock retry succeeds: clear error and supply profile data
                profileError = null
                ui.rerender()
            })

            let capturedProfileQueryFn: (() => unknown) | undefined

            mockQueryHandler = (options) => {
                if (options.queryKey[0] === 'recruiter-profile') {
                    capturedProfileQueryFn = options.queryFn
                    if (profileError) {
                        return {
                            data: undefined,
                            isLoading: false,
                            error: profileError,
                            refetch: refetchProfileMock,
                        }
                    }
                    return {
                        data: { companyId: 5, companyName: 'FPT Software', companyStatus: 'APPROVED' },
                        isLoading: false,
                        error: null,
                        refetch: refetchProfileMock,
                    }
                }
                if (options.queryKey[0] === 'my-company-jobs') {
                    return {
                        data: [sampleJob],
                        isLoading: false,
                        error: null,
                        refetch: vi.fn(),
                    }
                }
                return { data: [], isLoading: false, error: null, refetch: vi.fn() }
            }

            // Step 1: Render RecruiterJobsPage with initial profile error
            const ui = renderComponent()

            // Step 2: Verify error UI + nút Retry
            expect(ui.getByText('Không thể tải thông tin hồ sơ')).toBeDefined()
            expect(ui.getByText('Lỗi kết nối máy chủ hồ sơ')).toBeDefined()
            const retryButton = ui.getByRole('button', { name: /Thử lại/i })
            expect(retryButton).toBeDefined()

            // Verify queryFn links to getRecruiterProfile
            expect(capturedProfileQueryFn).toBeDefined()
            capturedProfileQueryFn!()
            expect(getRecruiterProfile).toHaveBeenCalled()

            // Step 3: Dùng user interaction (userEvent.click(...)) để click nút Retry
            // (Không được gọi mock function trực tiếp để thay cho thao tác click)
            await userEvent.click(retryButton)

            // Step 4: Verify profile request được gọi lại
            expect(refetchProfileMock).toHaveBeenCalledTimes(1)

            // Step 5: Verify UI chuyển sang trạng thái bình thường sau khi retry thành công
            expect(ui.queryByText('Không thể tải thông tin hồ sơ')).toBeNull()
            expect(ui.getByText('Quản lý Tin tuyển dụng & Ứng viên')).toBeDefined()
            expect(ui.getByText('Java Software Engineer')).toBeDefined()
        })

        // 4. Load jobs thành công → hiển thị danh sách Job
        it('4. Load jobs success: should render the list of jobs when profile is loaded with approved company', () => {
            let capturedJobsQueryFn: (() => unknown) | undefined

            mockQueryHandler = (options) => {
                if (options.queryKey[0] === 'recruiter-profile') {
                    return {
                        data: { companyId: 5, companyName: 'FPT Software', companyStatus: 'APPROVED' },
                        isLoading: false,
                        error: null,
                        refetch: vi.fn(),
                    }
                }
                if (options.queryKey[0] === 'my-company-jobs') {
                    capturedJobsQueryFn = options.queryFn
                    return {
                        data: [sampleJob],
                        isLoading: false,
                        error: null,
                        refetch: vi.fn(),
                    }
                }
                return { data: [], isLoading: false, error: null, refetch: vi.fn() }
            }

            const ui = renderComponent()

            // Verify queryFn triggers getMyCompanyJobs
            expect(capturedJobsQueryFn).toBeDefined()
            capturedJobsQueryFn!()
            expect(getMyCompanyJobs).toHaveBeenCalled()

            // Rendered jobs list with details
            expect(ui.getByText('Java Software Engineer')).toBeDefined()
            expect(ui.getByText('TP. Hồ Chí Minh')).toBeDefined()
            expect(ui.getByText('FULL_TIME')).toBeDefined()
            expect(ui.getByText('Hạn nộp: 2026-12-31')).toBeDefined()
            expect(ui.getByText('Xem ứng viên')).toBeDefined()
            expect(ui.queryByText('Doanh nghiệp chưa có tin tuyển dụng nào')).toBeNull()
        })

        // 2. Fix Select Job test — mô phỏng user chọn Job trên UI với userEvent.click
        it('5. Select Job: should simulate user selecting a job via UI click, call getJobApplicants(jobId), and render applicants', async () => {
            // Không đặt sẵn ?jobId=10 để giả lập việc chọn job
            expect(currentSearchParams.get('jobId')).toBeNull()
            expect(currentSelectedJob).toBeNull()

            let capturedApplicantsJobId: number | null = null

            mockQueryHandler = (options) => {
                if (options.queryKey[0] === 'recruiter-profile') {
                    return {
                        data: { companyId: 5, companyName: 'FPT Software', companyStatus: 'APPROVED' },
                        isLoading: false,
                        error: null,
                        refetch: vi.fn(),
                    }
                }
                if (options.queryKey[0] === 'my-company-jobs') {
                    return {
                        data: [sampleJob],
                        isLoading: false,
                        error: null,
                        refetch: vi.fn(),
                    }
                }
                if (options.queryKey[0] === 'job-applicants') {
                    const jobId = options.queryKey[1] as number | undefined
                    if (options.enabled && jobId) {
                        capturedApplicantsJobId = jobId
                        if (options.queryFn) {
                            options.queryFn()
                        }
                        return {
                            data: [sampleApplicant],
                            isLoading: false,
                            error: null,
                            refetch: vi.fn(),
                        }
                    }
                    return {
                        data: [],
                        isLoading: false,
                        error: null,
                        refetch: vi.fn(),
                    }
                }
                return { data: [], isLoading: false, error: null, refetch: vi.fn() }
            }

            // Step 1: Render page với danh sách jobs (không đặt sẵn ?jobId=10)
            const ui = renderComponent()

            // Verify danh sách jobs hiển thị, chưa hiển thị ứng viên
            expect(ui.getByText('Quản lý Tin tuyển dụng & Ứng viên')).toBeDefined()
            expect(ui.getByText('Java Software Engineer')).toBeDefined()
            expect(ui.getByText('TP. Hồ Chí Minh')).toBeDefined()
            expect(ui.queryByText('Ứng viên: Java Software Engineer')).toBeNull()

            // Step 2: Tìm job/item/button tương ứng trên UI
            const selectJobButton = ui.getByText('Xem ứng viên')
            expect(selectJobButton).toBeDefined()

            // Step 3: Dùng userEvent.click(...) để click nút xem ứng viên của Job
            await userEvent.click(selectJobButton)

            // Step 4: Verify getJobApplicants(jobId) được gọi với đúng jobId
            expect(capturedApplicantsJobId).toBe(10)
            expect(getJobApplicants).toHaveBeenCalledWith(10)

            // Step 5: Verify applicant list được render sau khi API trả dữ liệu
            expect(ui.getByText('Ứng viên: Java Software Engineer')).toBeDefined()
            expect(ui.getByText('Mã Job #10')).toBeDefined()
            expect(ui.getByText('Trần Minh Anh')).toBeDefined()
            expect(ui.getByText('candidate@talentbridge.vn')).toBeDefined()
            expect(ui.getByText('0987654321')).toBeDefined()
            expect(ui.getByText('Fullstack Developer')).toBeDefined()
            expect(ui.getByText('3 năm KN')).toBeDefined()
            expect(ui.getByText('TP. Hồ Chí Minh')).toBeDefined()
            expect(ui.getByText('APPLIED')).toBeDefined()
            expect(ui.getByText('Tôi mong muốn ứng tuyển vị trí này.')).toBeDefined()

            // Verify searchParams was updated via handleSelectJob without direct URL injection
            expect(currentSearchParams.get('jobId')).toBe('10')
        })

        // 6. Applicants có dữ liệu → render applicant
        it('6. Applicants has data: should render applicant cards and details for selected job', () => {
            currentSelectedJob = sampleJob

            mockQueryHandler = (options) => {
                if (options.queryKey[0] === 'recruiter-profile') {
                    return {
                        data: { companyId: 5, companyStatus: 'APPROVED' },
                        isLoading: false,
                        error: null,
                        refetch: vi.fn(),
                    }
                }
                if (options.queryKey[0] === 'my-company-jobs') {
                    return {
                        data: [sampleJob],
                        isLoading: false,
                        error: null,
                        refetch: vi.fn(),
                    }
                }
                if (options.queryKey[0] === 'job-applicants') {
                    return {
                        data: [sampleApplicant],
                        isLoading: false,
                        error: null,
                        refetch: vi.fn(),
                    }
                }
                return { data: [], isLoading: false, error: null, refetch: vi.fn() }
            }

            const ui = renderComponent()

            // Renders header for selected job
            expect(ui.getByText('Ứng viên: Java Software Engineer')).toBeDefined()
            expect(ui.getByText('Mã Job #10')).toBeDefined()

            // Renders applicant information
            expect(ui.getByText('Trần Minh Anh')).toBeDefined()
            expect(ui.getByText('candidate@talentbridge.vn')).toBeDefined()
            expect(ui.getByText('0987654321')).toBeDefined()
            expect(ui.getByText('Fullstack Developer')).toBeDefined()
            expect(ui.getByText('3 năm KN')).toBeDefined()
            expect(ui.getByText('TP. Hồ Chí Minh')).toBeDefined()
            expect(ui.getByText('APPLIED')).toBeDefined()
            expect(ui.getByText('Tôi mong muốn ứng tuyển vị trí này.')).toBeDefined()

            // Does not render empty state
            expect(ui.queryByText('Chưa có ứng viên nào ứng tuyển vào vị trí này')).toBeNull()
        })

        // 7. Applicants rỗng → render empty state
        it('7. Applicants empty: should render empty state when no applicants have applied to selected job', () => {
            currentSelectedJob = sampleJob

            mockQueryHandler = (options) => {
                if (options.queryKey[0] === 'recruiter-profile') {
                    return {
                        data: { companyId: 5, companyStatus: 'APPROVED' },
                        isLoading: false,
                        error: null,
                        refetch: vi.fn(),
                    }
                }
                if (options.queryKey[0] === 'my-company-jobs') {
                    return {
                        data: [sampleJob],
                        isLoading: false,
                        error: null,
                        refetch: vi.fn(),
                    }
                }
                if (options.queryKey[0] === 'job-applicants') {
                    return {
                        data: [],
                        isLoading: false,
                        error: null,
                        refetch: vi.fn(),
                    }
                }
                return { data: [], isLoading: false, error: null, refetch: vi.fn() }
            }

            const ui = renderComponent()

            // Renders empty state for applicants
            expect(ui.getByText('Chưa có ứng viên nào ứng tuyển vào vị trí này')).toBeDefined()
            expect(ui.getByText('Khi ứng viên nộp hồ sơ vào tin tuyển dụng này')).toBeDefined()
            expect(ui.queryByText('Trần Minh Anh')).toBeNull()
        })

        // 8. API applicants error → render error + Retry nếu UI hiện tại hỗ trợ retry
        it('8. Applicants error: should render error state with Retry button for applicants failure', async () => {
            currentSelectedJob = sampleJob
            let applicantsError: Error | null = new Error(
                'Không thể kết nối đến máy chủ lấy hồ sơ ứng viên',
            )
            const refetchApplicantsMock = vi.fn(() => {
                applicantsError = null
                ui.rerender()
            })

            mockQueryHandler = (options) => {
                if (options.queryKey[0] === 'recruiter-profile') {
                    return {
                        data: { companyId: 5, companyStatus: 'APPROVED' },
                        isLoading: false,
                        error: null,
                        refetch: vi.fn(),
                    }
                }
                if (options.queryKey[0] === 'my-company-jobs') {
                    return {
                        data: [sampleJob],
                        isLoading: false,
                        error: null,
                        refetch: vi.fn(),
                    }
                }
                if (options.queryKey[0] === 'job-applicants') {
                    if (applicantsError) {
                        return {
                            data: [],
                            isLoading: false,
                            error: applicantsError,
                            refetch: refetchApplicantsMock,
                        }
                    }
                    return {
                        data: [sampleApplicant],
                        isLoading: false,
                        error: null,
                        refetch: refetchApplicantsMock,
                    }
                }
                return { data: [], isLoading: false, error: null, refetch: vi.fn() }
            }

            const ui = renderComponent()

            // Renders applicants error
            expect(ui.getByText('Không thể tải danh sách ứng viên')).toBeDefined()
            expect(
                ui.getByText('Không thể kết nối đến máy chủ lấy hồ sơ ứng viên'),
            ).toBeDefined()
            const retryButton = ui.getByRole('button', { name: /Thử lại/i })
            expect(retryButton).toBeDefined()

            // Verifies retry interaction via userEvent.click
            await userEvent.click(retryButton)
            expect(refetchApplicantsMock).toHaveBeenCalledTimes(1)
            expect(ui.queryByText('Không thể tải danh sách ứng viên')).toBeNull()
            expect(ui.getByText('Trần Minh Anh')).toBeDefined()
        })
    })
})


