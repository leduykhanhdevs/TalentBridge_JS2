import { afterEach, describe, expect, it, vi } from 'vitest'
import { authenticatedFetch } from '../../auth/authenticatedFetch'
import { COMPANY_STATUS_MUTATION_TIMEOUT_MS, updateCompanyStatus } from '../adminApi'

vi.mock('../../auth/authenticatedFetch', () => ({
    authenticatedFetch: vi.fn(),
}))

vi.mock('../../auth/tokenStorage', () => ({
    getAccessToken: () => 'qa-admin-token',
}))

afterEach(() => vi.clearAllMocks())

describe('company status update deadline', () => {
    it('allows a Render cold start to finish before timing out the status mutation', async () => {
        vi.mocked(authenticatedFetch).mockResolvedValue({
            ok: true,
            json: async () => ({ data: { id: 77, status: 'APPROVED' } }),
        } as Response)

        await updateCompanyStatus(77, { status: 'APPROVED' })

        expect(authenticatedFetch).toHaveBeenCalledWith(
            expect.stringContaining('/admin/companies/77/status'),
            expect.objectContaining({ method: 'PATCH' }),
            COMPANY_STATUS_MUTATION_TIMEOUT_MS,
        )
        expect(COMPANY_STATUS_MUTATION_TIMEOUT_MS).toBeGreaterThan(50_000)
    })
})
