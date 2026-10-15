import { describe, it, expect } from 'vitest'
import { RecruiterApiError } from '../recruiterApi'
import { validateCompanyRequest } from '../companyRequestValidation'
import type {
    CompanyResponse,
    RecruiterProfile,
    ReviewJoinRequest,
    SubmitJoinCompanyRequest,
    UpdateRecruiterProfileRequest,
} from '../recruiterTypes'

describe('Recruiter Validation and Domain Logic Rules', () => {
    describe('RecruiterApiError', () => {
        it('should correctly capture status and message', () => {
            const error = new RecruiterApiError(404, 'Không tìm thấy hồ sơ nhà tuyển dụng')
            expect(error.name).toBe('RecruiterApiError')
            expect(error.status).toBe(404)
            expect(error.message).toBe('Không tìm thấy hồ sơ nhà tuyển dụng')
        })

        it('should handle 401 unauthorized status', () => {
            const error = new RecruiterApiError(401, 'Phiên đăng nhập đã hết hạn')
            expect(error.status).toBe(401)
        })
    })

    describe('Company Creation Request Validation', () => {
        it('should reject company creation when name is empty', () => {
            expect(validateCompanyRequest({ name: '' }).name).toBe('Tên công ty không được để trống.')
        })

        it('should reject company creation when name is too long', () => {
            expect(validateCompanyRequest({ name: 'A'.repeat(201) }).name).toBe(
                'Tên công ty không được vượt quá 200 ký tự.',
            )
        })

        it('rejects an email address in the website field', () => {
            expect(validateCompanyRequest({
                name: 'Công ty Vin',
                website: 'recruiting@example.com',
            }).website).toBe('Nhập website đầy đủ bắt đầu bằng https:// hoặc http://.')
        })

        it('rejects a data URI in the logo field instead of sending it to the API', () => {
            expect(validateCompanyRequest({
                name: 'Công ty Vin',
                logoUrl: 'data:image/png;base64,aGVsbG8=',
            }).logoUrl).toBe('Logo cần là URL ảnh công khai bắt đầu bằng https:// hoặc http://.')
        })

        it('accepts valid HTTP(S) URLs and omitted optional fields', () => {
            const errors = validateCompanyRequest({
                name: 'FPT Software',
                taxCode: '0101234567',
                website: 'https://fptsoftware.com',
                logoUrl: 'https://cdn.example.com/logo.png',
                city: 'Hồ Chí Minh',
            })
            expect(errors).toEqual({})
            expect(validateCompanyRequest({ name: 'Công ty Vin' })).toEqual({})
        })

        it('enforces backend length limits for website and logo URLs', () => {
            const errors = validateCompanyRequest({
                name: 'Công ty Vin',
                website: `https://${'a'.repeat(250)}`,
                logoUrl: `https://${'a'.repeat(500)}`,
            })
            expect(errors.website).toBe('Website không được vượt quá 255 ký tự.')
            expect(errors.logoUrl).toBe('URL logo không được vượt quá 500 ký tự.')
        })
    })

    describe('Join Company Request Validation', () => {
        const validateJoinRequest = (
            req: SubmitJoinCompanyRequest,
        ): { valid: boolean; error?: string } => {
            if (!req.position || !req.position.trim()) {
                return { valid: false, error: 'Chức danh ứng tuyển không được để trống' }
            }
            if (req.position.trim().length > 100) {
                return { valid: false, error: 'Chức danh ứng tuyển không được vượt quá 100 ký tự' }
            }
            if (req.message && req.message.length > 1000) {
                return { valid: false, error: 'Lời nhắn không được vượt quá 1000 ký tự' }
            }
            return { valid: true }
        }

        it('should require a valid position', () => {
            expect(validateJoinRequest({ position: '' }).valid).toBe(false)
            expect(validateJoinRequest({ position: '   ' }).valid).toBe(false)
            expect(validateJoinRequest({ position: 'Talent Acquisition' }).valid).toBe(true)
        })

        it('should reject position longer than 100 characters', () => {
            expect(validateJoinRequest({ position: 'P'.repeat(101) }).valid).toBe(false)
        })

        it('should reject message longer than 1000 characters', () => {
            expect(
                validateJoinRequest({
                    position: 'Senior Recruiter',
                    message: 'M'.repeat(1001),
                }).valid,
            ).toBe(false)
        })

        it('should accept optional message within limit', () => {
            const result = validateJoinRequest({
                position: 'Senior Recruiter',
                message: 'Rất mong muốn được đóng góp cho team tuyển dụng!',
            })
            expect(result.valid).toBe(true)
        })
    })

    describe('Peer Approval Review Request Validation', () => {
        const validateReviewRequest = (
            req: ReviewJoinRequest,
        ): { valid: boolean; error?: string } => {
            if (!req.status || (req.status !== 'ACCEPTED' && req.status !== 'REJECTED')) {
                return { valid: false, error: 'Trạng thái phê duyệt không hợp lệ' }
            }
            if (req.status === 'REJECTED' && (!req.reason || !req.reason.trim())) {
                return { valid: false, error: 'Lý do từ chối không được để trống' }
            }
            if (req.reason && req.reason.length > 1000) {
                return { valid: false, error: 'Lý do không được vượt quá 1000 ký tự' }
            }
            return { valid: true }
        }

        it('should allow ACCEPTED status without mandatory reason', () => {
            const result = validateReviewRequest({ status: 'ACCEPTED' })
            expect(result.valid).toBe(true)
        })

        it('should reject invalid status or PENDING status in review command', () => {
            expect(validateReviewRequest({ status: 'PENDING' as unknown as 'ACCEPTED' | 'REJECTED' }).valid).toBe(false)
            expect(validateReviewRequest({ status: '' as unknown as 'ACCEPTED' | 'REJECTED' }).valid).toBe(false)
        })

        it('should require a non-empty reason when REJECTED', () => {
            expect(validateReviewRequest({ status: 'REJECTED' }).valid).toBe(false)
            expect(validateReviewRequest({ status: 'REJECTED', reason: '' }).valid).toBe(false)
            expect(validateReviewRequest({ status: 'REJECTED', reason: '    ' }).valid).toBe(false)
            expect(
                validateReviewRequest({ status: 'REJECTED', reason: 'Không phù hợp tiêu chí tuyển chọn' })
                    .valid,
            ).toBe(true)
        })

        it('should reject reason exceeding 1000 characters', () => {
            expect(
                validateReviewRequest({
                    status: 'REJECTED',
                    reason: 'R'.repeat(1001),
                }).valid,
            ).toBe(false)
        })
    })

    describe('Recruiter Profile Type Structures', () => {
        it('should instantiate valid RecruiterProfile object', () => {
            const profile: RecruiterProfile = {
                id: 1,
                userId: 10,
                fullName: 'Nguyễn Phan Minh Hiếu',
                email: 'hieu@example.com',
                phone: '0901234567',
                status: 'ACTIVE',
                position: 'Lead HR',
                companyId: 100,
                companyName: 'FPT Software',
                companyStatus: 'APPROVED',
                createdAt: '2026-03-10T10:00:00Z',
            }

            expect(profile.fullName).toBe('Nguyễn Phan Minh Hiếu')
            expect(profile.companyStatus).toBe('APPROVED')
        })

        it('should instantiate valid CompanyResponse object', () => {
            const company: CompanyResponse = {
                id: 100,
                name: 'FPT Software',
                taxCode: '0101234567',
                status: 'APPROVED',
                city: 'Hồ Chí Minh',
            }

            expect(company.status).toBe('APPROVED')
            expect(company.id).toBe(100)
        })

        it('should construct valid UpdateRecruiterProfileRequest', () => {
            const updateReq: UpdateRecruiterProfileRequest = {
                fullName: 'Trần Đình Tình',
                phone: '0912345678',
                position: 'Senior Talent Acquisition',
            }

            expect(updateReq.fullName).toBe('Trần Đình Tình')
            expect(updateReq.phone).toBe('0912345678')
        })
    })
})
