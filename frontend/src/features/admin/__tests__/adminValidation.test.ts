import { describe, it, expect } from 'vitest'
import { AdminApiError } from '../adminApi'
import type {
    CompanyStatus,
    UpdateCompanyStatusRequest,
    UpdateUserStatusRequest,
    UserStatus,
} from '../adminTypes'

describe('Admin Validation and Business Logic Rules', () => {
    describe('Company Rejection Reason Validation', () => {
        const validateRejectReason = (reason?: string): { valid: boolean; error?: string } => {
            if (!reason || !reason.trim()) {
                return { valid: false, error: 'Lý do từ chối không được để trống' }
            }
            if (reason.trim().length < 5) {
                return { valid: false, error: 'Lý do từ chối phải có ít nhất 5 ký tự' }
            }
            return { valid: true }
        }

        it('should require a non-empty reason when rejecting a company', () => {
            expect(validateRejectReason('').valid).toBe(false)
            expect(validateRejectReason('   ').valid).toBe(false)
            expect(validateRejectReason(undefined).valid).toBe(false)
        })

        it('should require reason to be descriptive (at least 5 characters)', () => {
            expect(validateRejectReason('abc').valid).toBe(false)
            expect(validateRejectReason('Mã số thuế không hợp lệ').valid).toBe(true)
        })
    })

    describe('User Status Update Payloads', () => {
        it('should construct valid lock and unlock requests', () => {
            const lockRequest: UpdateUserStatusRequest = {
                status: 'BANNED',
                reason: 'Vi phạm điều khoản dịch vụ',
            }
            const unlockRequest: UpdateUserStatusRequest = {
                status: 'ACTIVE',
            }

            expect(lockRequest.status).toBe('BANNED')
            expect(lockRequest.reason).toBeDefined()
            expect(unlockRequest.status).toBe('ACTIVE')
        })

        it('should only accept valid UserStatus values', () => {
            const validStatuses: UserStatus[] = ['ACTIVE', 'BANNED']
            expect(validStatuses).toContain('ACTIVE')
            expect(validStatuses).toContain('BANNED')
        })
    })

    describe('Company Status Transitions', () => {
        it('should build valid approval and rejection payloads', () => {
            const approvePayload: UpdateCompanyStatusRequest = {
                status: 'APPROVED',
            }
            const rejectPayload: UpdateCompanyStatusRequest = {
                status: 'REJECTED',
                reason: 'Giấy phép kinh doanh không khớp',
            }

            expect(approvePayload.status).toBe('APPROVED')
            expect(rejectPayload.status).toBe('REJECTED')
            expect(rejectPayload.reason).toBeTruthy()
        })

        it('should recognize valid CompanyStatus enum values', () => {
            const validCompanyStatuses: CompanyStatus[] = ['PENDING', 'APPROVED', 'REJECTED']
            expect(validCompanyStatuses).toHaveLength(3)
            expect(validCompanyStatuses).toContain('PENDING')
            expect(validCompanyStatuses).toContain('APPROVED')
            expect(validCompanyStatuses).toContain('REJECTED')
        })
    })

    describe('AdminApiError', () => {
        it('should correctly store HTTP status code and message', () => {
            const error = new AdminApiError(403, 'Bạn không có quyền truy cập')
            expect(error.name).toBe('AdminApiError')
            expect(error.status).toBe(403)
            expect(error.message).toBe('Bạn không có quyền truy cập')
        })
    })
})
