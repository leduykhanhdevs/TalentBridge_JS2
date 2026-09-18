import { useEffect, useState } from 'react'
import { BriefcaseBusiness, Building2, KeyRound, LogIn, LogOut, ShieldCheck, User, UserPlus } from 'lucide-react'
import { NavLink, Outlet, useNavigate } from 'react-router'
import { getStoredUser, isAuthenticated } from '../features/auth/tokenStorage'
import { logoutUser } from '../features/auth/authApi'
import type { UserResponse } from '../features/auth/authTypes'
import { ChangePasswordModal } from '../features/auth/components/ChangePasswordModal'

export function MainLayout() {
    const navigate = useNavigate()
    const [user, setUser] = useState<UserResponse | null>(() => getStoredUser())
    const [authenticated, setAuthenticated] = useState<boolean>(() => isAuthenticated())
    const [isChangePasswordOpen, setIsChangePasswordOpen] = useState(false)

    useEffect(() => {
        function syncAuth() {
            setUser(getStoredUser())
            setAuthenticated(isAuthenticated())
        }

        window.addEventListener('talentbridge_auth_change', syncAuth)
        window.addEventListener('storage', syncAuth)

        return () => {
            window.removeEventListener('talentbridge_auth_change', syncAuth)
            window.removeEventListener('storage', syncAuth)
        }
    }, [])

    async function handleLogout() {
        await logoutUser()
        navigate('/login')
    }

    const isAdmin = authenticated && user?.roles?.includes('ROLE_ADMIN')
    const isRecruiter = authenticated && user?.roles?.includes('ROLE_RECRUITER')
    const isCandidate = authenticated && (user?.roles?.includes('ROLE_CANDIDATE') || (!isAdmin && !isRecruiter))

    const displayName = user?.fullName
        ? user.fullName.replace(/\s*\((?:Admin|Recruiter|Candidate)\)\s*$/i, '')
        : ''

    const getInitials = (name: string) => {
        const parts = name.trim().split(/\s+/)
        if (!parts.length || !parts[0]) return 'TB'
        if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase()
        return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
    }

    const userRoleBadge = isAdmin
        ? { label: 'Quản trị', className: 'bg-amber-50 text-amber-700 border-amber-200/80' }
        : isRecruiter
        ? { label: 'Tuyển dụng', className: 'bg-emerald-50 text-emerald-700 border-emerald-200/80' }
        : { label: 'Ứng viên', className: 'bg-indigo-50 text-indigo-700 border-indigo-200/80' }

    return (
        <div className="flex min-h-screen flex-col bg-slate-50 text-slate-900">
            <header className="sticky top-0 z-50 border-b border-slate-200/80 bg-white/90 backdrop-blur-md shadow-2xs">
                <div className="mx-auto flex h-16 w-full max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
                    <NavLink
                        className="flex items-center gap-3 transition-transform hover:-translate-y-0.5 active:translate-y-0"
                        end
                        to="/"
                    >
                        <span className="grid size-10 place-items-center rounded-xl bg-indigo-600 text-white shadow-xs">
                            <BriefcaseBusiness aria-hidden="true" size={20} />
                        </span>

                        <span className="flex items-center gap-2">
                            <span className="block text-base font-bold tracking-tight text-slate-900">
                                TalentBridge
                            </span>
                            <span className="hidden sm:inline-block bento-badge bg-amber-100 text-amber-950 text-[10px]">
                                ATS 2026
                            </span>
                        </span>
                    </NavLink>

                    {/* Bento Header Right Cluster */}
                    <div className="flex items-center gap-2 sm:gap-3">
                        {/* Bento Navigation Dock */}
                        <nav
                            aria-label="Điều hướng chính"
                            className="flex items-center rounded-xl bg-slate-100/80 p-1 border border-slate-200/60 backdrop-blur-xs shadow-2xs"
                        >
                            <NavLink
                                className={({ isActive }) =>
                                    `inline-flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-xs sm:text-sm font-semibold transition-all ${
                                        isActive
                                            ? 'bg-white text-indigo-700 shadow-2xs border border-slate-200/60'
                                            : 'text-slate-600 hover:text-slate-900 hover:bg-white/60'
                                    }`
                                }
                                end
                                to="/"
                            >
                                <span>Trang chủ</span>
                            </NavLink>

                            {isCandidate && (
                                <NavLink
                                    className={({ isActive }) =>
                                        `inline-flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-xs sm:text-sm font-semibold transition-all ${
                                            isActive
                                                ? 'bg-white text-indigo-700 shadow-2xs border border-slate-200/60'
                                                : 'text-slate-600 hover:text-slate-900 hover:bg-white/60'
                                        }`
                                    }
                                    to="/candidate/profile"
                                >
                                    <User size={14} />
                                    <span>Hồ sơ TopCV</span>
                                </NavLink>
                            )}

                            {isRecruiter && (
                                <NavLink
                                    className={({ isActive }) =>
                                        `inline-flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-xs sm:text-sm font-semibold transition-all ${
                                            isActive
                                                ? 'bg-white text-emerald-700 shadow-2xs border border-slate-200/60'
                                                : 'text-slate-600 hover:text-slate-900 hover:bg-white/60'
                                        }`
                                    }
                                    to="/recruiter/profile"
                                >
                                    <Building2 size={14} />
                                    <span>Tuyển dụng</span>
                                </NavLink>
                            )}

                            {isAdmin && (
                                <NavLink
                                    className={({ isActive }) =>
                                        `inline-flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-xs sm:text-sm font-semibold transition-all ${
                                            isActive
                                                ? 'bg-white text-indigo-700 shadow-2xs border border-slate-200/60'
                                                : 'text-slate-600 hover:text-slate-900 hover:bg-white/60'
                                        }`
                                    }
                                    to="/admin/candidates"
                                >
                                    <ShieldCheck size={14} />
                                    <span>Quản trị</span>
                                </NavLink>
                            )}
                        </nav>

                        {/* Bento User Control Hub (When Authenticated) */}
                        {authenticated && user ? (
                            <div className="flex items-center rounded-xl bg-white border border-slate-200/80 p-1 shadow-2xs">
                                {/* User Identity Chip */}
                                <div className="flex items-center gap-2 pl-1.5 pr-2 py-0.5">
                                    <span className="grid size-7 place-items-center rounded-lg bg-gradient-to-tr from-indigo-600 to-violet-500 text-[11px] font-bold text-white shadow-2xs">
                                        {getInitials(displayName)}
                                    </span>
                                    <div className="flex items-center gap-1.5">
                                        <span className="max-w-[120px] truncate text-xs font-semibold text-slate-850 sm:max-w-none">
                                            {displayName}
                                        </span>
                                        <span className={`inline-flex items-center px-1.5 py-0.5 rounded-md text-[10px] font-semibold border ${userRoleBadge.className}`}>
                                            {userRoleBadge.label}
                                        </span>
                                    </div>
                                </div>

                                {/* Micro Divider */}
                                <div className="h-4 w-px bg-slate-200/80 mx-0.5" />

                                {/* Change Password Action */}
                                <button
                                    className="inline-flex items-center gap-1.5 rounded-lg px-2.5 py-1.5 text-xs font-medium text-slate-600 hover:text-indigo-600 hover:bg-indigo-50/70 transition-colors"
                                    onClick={() => setIsChangePasswordOpen(true)}
                                    title="Đổi mật khẩu"
                                    type="button"
                                >
                                    <KeyRound size={13} />
                                    <span className="hidden md:inline">Đổi mật khẩu</span>
                                </button>

                                {/* Logout Action */}
                                <button
                                    className="inline-flex items-center gap-1.5 rounded-lg px-2.5 py-1.5 text-xs font-medium text-slate-600 hover:text-rose-600 hover:bg-rose-50 transition-colors"
                                    onClick={handleLogout}
                                    title="Đăng xuất"
                                    type="button"
                                >
                                    <LogOut size={13} />
                                    <span className="hidden sm:inline">Đăng xuất</span>
                                </button>
                            </div>
                        ) : (
                            <div className="flex items-center gap-2">
                                <NavLink
                                    className="btn-bento-secondary text-xs sm:text-sm py-1.5 px-3"
                                    to="/login"
                                >
                                    <LogIn aria-hidden="true" size={15} />
                                    <span>Đăng nhập</span>
                                </NavLink>

                                <NavLink
                                    className="btn-bento-primary text-xs sm:text-sm py-1.5 px-3"
                                    to="/register"
                                >
                                    <UserPlus aria-hidden="true" size={15} />
                                    <span>Đăng ký</span>
                                </NavLink>
                            </div>
                        )}
                    </div>
                </div>
            </header>

            <main className="flex-1">
                <Outlet />
            </main>

            <footer className="border-t border-slate-200/80 bg-white">
                <div className="mx-auto flex w-full max-w-7xl flex-col gap-3 px-4 py-6 text-xs text-slate-600 sm:flex-row sm:items-center sm:justify-between sm:px-6 lg:px-8">
                    <div className="flex items-center gap-2 font-mono">
                        <span className="bento-badge bg-slate-900 text-white text-[10px]">
                            TALENTBRIDGE v1.0
                        </span>
                        <span>(C) {new Date().getFullYear()} TalentBridge Ecosystem</span>
                    </div>
                    <div className="flex items-center gap-4 font-mono text-slate-500">
                        <span>27 TABLES 3NF</span>
                        <span>*</span>
                        <span>TOPCV STANDARD</span>
                        <span>*</span>
                        <span>ATS PIPELINE</span>
                    </div>
                </div>
            </footer>

            <ChangePasswordModal
                isOpen={isChangePasswordOpen}
                onClose={() => setIsChangePasswordOpen(false)}
            />
        </div>
    )
}