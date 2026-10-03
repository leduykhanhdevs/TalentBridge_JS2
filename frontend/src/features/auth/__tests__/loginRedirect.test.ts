import { describe, expect, it } from 'vitest'
import { getCandidatePostLoginPath } from '../loginRedirect'

describe('getCandidatePostLoginPath', () => {
    it('returns the internal job detail URL, including valid query and hash', () => {
        expect(getCandidatePostLoginPath({
            from: { pathname: '/jobs/42', search: '?source=bookmark', hash: '#apply' },
        })).toBe('/jobs/42?source=bookmark#apply')
    })

    it.each([
        null,
        {},
        { from: { pathname: 'https://evil.example/jobs/2' } },
        { from: { pathname: '//evil.example/jobs/2' } },
        { from: { pathname: '/admin/users' } },
        { from: { pathname: '/jobs/0' } },
        { from: { pathname: '/jobs/not-a-number' } },
    ])('falls back to the candidate default route for unsafe or unrelated locations', (state) => {
        expect(getCandidatePostLoginPath(state)).toBe('/')
    })

    it('ignores malformed query and hash values', () => {
        expect(getCandidatePostLoginPath({
            from: { pathname: '/jobs/8', search: 'redirect=https://evil.example', hash: 'apply' },
        })).toBe('/jobs/8')
    })
})
