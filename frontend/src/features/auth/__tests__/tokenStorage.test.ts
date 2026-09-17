import { describe, it, expect, beforeEach } from 'vitest'
import {
    saveAuthTokens,
    getAccessToken,
    getRefreshToken,
    getStoredUser,
    clearAuthTokens,
    isAuthenticated,
} from '../tokenStorage'
import type { AuthResponse } from '../authTypes'

describe('tokenStorage utility', () => {
    const storageMap = new Map<string, string>()

    beforeEach(() => {
        storageMap.clear()

        globalThis.localStorage = {
            getItem: (key: string) => storageMap.get(key) ?? null,
            setItem: (key: string, value: string) => {
                storageMap.set(key, value)
            },
            removeItem: (key: string) => {
                storageMap.delete(key)
            },
            clear: () => {
                storageMap.clear()
            },
            length: storageMap.size,
            key: () => null,
        } as Storage

        globalThis.window = {
            dispatchEvent: () => true,
            addEventListener: () => {},
            removeEventListener: () => {},
        } as unknown as Window & typeof globalThis
    })

    const mockAuthResponse: AuthResponse = {
        accessToken: 'mock-access-jwt-token-12345',
        refreshToken: 'mock-refresh-jwt-token-67890',
        tokenType: 'Bearer',
        expiresInMs: 86400000,
        user: {
            id: 10,
            email: 'candidate@example.com',
            fullName: 'Nguyen Van Candidate',
            phone: '0901234567',
            status: 'ACTIVE',
            roles: ['ROLE_CANDIDATE'],
        },
    }

    it('should initially be unauthenticated with empty storage', () => {
        expect(getAccessToken()).toBeNull()
        expect(getRefreshToken()).toBeNull()
        expect(getStoredUser()).toBeNull()
        expect(isAuthenticated()).toBe(false)
    })

    it('should correctly persist tokens and user information upon saveAuthTokens', () => {
        saveAuthTokens(mockAuthResponse)

        expect(getAccessToken()).toBe('mock-access-jwt-token-12345')
        expect(getRefreshToken()).toBe('mock-refresh-jwt-token-67890')
        expect(isAuthenticated()).toBe(true)

        const user = getStoredUser()
        expect(user).not.toBeNull()
        expect(user?.id).toBe(10)
        expect(user?.email).toBe('candidate@example.com')
        expect(user?.fullName).toBe('Nguyen Van Candidate')
        expect(user?.roles).toContain('ROLE_CANDIDATE')
    })

    it('should clear all tokens and user profile upon clearAuthTokens', () => {
        saveAuthTokens(mockAuthResponse)
        expect(isAuthenticated()).toBe(true)

        clearAuthTokens()

        expect(getAccessToken()).toBeNull()
        expect(getRefreshToken()).toBeNull()
        expect(getStoredUser()).toBeNull()
        expect(isAuthenticated()).toBe(false)
    })
})
