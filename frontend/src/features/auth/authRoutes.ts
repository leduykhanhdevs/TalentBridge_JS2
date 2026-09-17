import type { UserRole } from './authTypes'

const roleHomePaths: Record<UserRole, string> = {
    ROLE_ADMIN: '/admin',
    ROLE_RECRUITER: '/recruiter',
    ROLE_CANDIDATE: '/candidate',
}

const rolePriority: UserRole[] = [
    'ROLE_ADMIN',
    'ROLE_RECRUITER',
    'ROLE_CANDIDATE',
]

export function getRoleHomePath(roles: UserRole[]) {
    const primaryRole = rolePriority.find((role) => roles.includes(role))
    return primaryRole ? roleHomePaths[primaryRole] : '/'
}
