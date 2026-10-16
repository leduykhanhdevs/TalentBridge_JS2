import { useEffect, useState } from 'react'
import { BriefcaseBusiness, Building2, FileText, Home, KeyRound, LogIn, LogOut, Menu, MessageCircle, Search, ShieldCheck, User, UserPlus, X } from 'lucide-react'
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
    const [isMobileNavOpen, setIsMobileNavOpen] = useState(false)

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
        : { label: 'Ứng viên', className: 'bg-blue-50 text-blue-800 border-blue-200/80' }

    const mobileLinks = [
        { to: '/', label: 'Trang chủ' },
        { to: '/jobs', label: 'Việc làm' },
        { to: '/assistant', label: 'Trợ lý TalentBridge' },
        ...(isCandidate ? [
            { to: '/candidate/profile', label: 'Hồ sơ TopCV' },
            { to: '/candidate/applications', label: 'Đơn ứng tuyển' },
        ] : []),
        ...(isRecruiter ? [{ to: '/recruiter/profile', label: 'Cổng nhà tuyển dụng' }] : []),
        ...(isAdmin ? [{ to: '/admin/candidates', label: 'Cổng quản trị' }] : []),
    ]

    return (
        <div className="flex min-h-screen flex-col bg-slate-50 text-slate-900">
            <header className="sticky top-0 z-50 border-b border-slate-200/80 bg-white/90 backdrop-blur-md shadow-2xs">
                <div className="mx-auto flex min-h-16 w-full max-w-[1200px] items-center gap-3 px-4 sm:px-6 lg:px-8">
                    <NavLink
                        className="flex shrink-0 items-center gap-3 transition-transform hover:-translate-y-0.5 active:translate-y-0"
                        end
                        to="/"
                    >
                        <span className="grid size-10 place-items-center rounded-xl bg-blue-700 text-white shadow-xs">
                            <BriefcaseBusiness aria-hidden="true" size={20} />
                        </span>

                        <span className="flex items-center gap-2">
                            <span className="block text-base font-bold tracking-tight text-slate-900">
                                TalentBridge
                            </span>
                            <span className="hidden shrink-0 whitespace-nowrap bento-badge bg-amber-100 text-amber-950 text-[10px] sm:inline-block">
                                ATS 2026
                            </span>
                        </span>
                    </NavLink>

                    {/* Bento Header Right Cluster */}
                    <div className="flex min-w-0 flex-1 items-center justify-end gap-2 sm:gap-3">
                        {/* Bento Navigation Dock */}
                        <nav
                            aria-label="Điều hướng chính"
                            className="hidden shrink-0 flex-nowrap items-center gap-0.5 rounded-xl border border-slate-200/60 bg-slate-100/80 p-1 shadow-2xs backdrop-blur-xs lg:flex"
                        >
                            <NavLink
                                aria-label="Trang chủ"
                                className={({ isActive }) =>
                                    `inline-flex h-10 shrink-0 items-center gap-1.5 whitespace-nowrap rounded-lg border px-2 py-1.5 text-[11px] font-semibold transition-all xl:px-2.5 xl:text-xs ${
                                        isActive
                                            ? 'bg-white text-blue-800 shadow-2xs border-slate-200/60'
                                            : 'border-transparent text-slate-600 hover:text-slate-900 hover:bg-white/60'
                                    }`
                                }
                                end
                                to="/"
                            >
                                <Home aria-hidden="true" className="xl:hidden" size={14} />
                                <span className="hidden xl:inline">Trang chủ</span>
                            </NavLink>

                            <NavLink
                                aria-label="Việc làm"
                                className={({ isActive }) =>
                                    `inline-flex h-10 shrink-0 items-center gap-1.5 whitespace-nowrap rounded-lg border px-2 py-1.5 text-[11px] font-semibold transition-all xl:px-2.5 xl:text-xs ${
                                        isActive
                                            ? 'bg-white text-blue-800 shadow-2xs border-slate-200/60'
                                            : 'border-transparent text-slate-600 hover:text-slate-900 hover:bg-white/60'
                                    }`
                                }
                                to="/jobs"
                            >
                                <Search aria-hidden="true" size={14} />
                                <span className="hidden xl:inline">Việc làm</span>
                            </NavLink>

                            <NavLink
                                aria-label="Trợ lý"
                                className={({ isActive }) =>
                                    `inline-flex h-10 shrink-0 items-center gap-1.5 whitespace-nowrap rounded-lg border px-2 py-1.5 text-[11px] font-semibold transition-all xl:px-2.5 xl:text-xs ${
                                        isActive
                                            ? 'bg-white text-indigo-700 shadow-2xs border-slate-200/60'
                                            : 'border-transparent text-slate-600 hover:text-slate-900 hover:bg-white/60'
                                    }`
                                }
                                to="/assistant"
                            >
                                <MessageCircle aria-hidden="true" size={14} />
                                <span className="hidden xl:inline">Trợ lý</span>
                            </NavLink>

                            {isCandidate && (
                                <>
                                    <NavLink
                                        aria-label="Hồ sơ TopCV"
                                        className={({ isActive }) =>
                                            `inline-flex h-10 shrink-0 items-center gap-1.5 whitespace-nowrap rounded-lg border px-2 py-1.5 text-[11px] font-semibold transition-all xl:px-2.5 xl:text-xs ${
                                                isActive
                                                    ? 'bg-white text-blue-800 shadow-2xs border-slate-200/60'
                                                    : 'border-transparent text-slate-600 hover:text-slate-900 hover:bg-white/60'
                                            }`
                                        }
                                        to="/candidate/profile"
                                    >
                                        <User aria-hidden="true" size={14} />
                                        <span className="hidden xl:inline">Hồ sơ TopCV</span>
                                    </NavLink>

                                    <NavLink
                                        aria-label="Đơn ứng tuyển"
                                        className={({ isActive }) =>
                                            `inline-flex h-10 shrink-0 items-center gap-1.5 whitespace-nowrap rounded-lg border px-2 py-1.5 text-[11px] font-semibold transition-all xl:px-2.5 xl:text-xs ${
                                                isActive
                                                    ? 'bg-white text-blue-800 shadow-2xs border-slate-200/60'
                                                    : 'border-transparent text-slate-600 hover:text-slate-900 hover:bg-white/60'
                                            }`
                                        }
                                        to="/candidate/applications"
                                    >
                                        <FileText aria-hidden="true" size={14} />
                                        <span className="hidden xl:inline">Đơn ứng tuyển</span>
                                    </NavLink>
                                </>
                            )}

                            {isRecruiter && (
                                <NavLink
                                    aria-label="Tuyển dụng"
                                    className={({ isActive }) =>
                                        `inline-flex h-10 shrink-0 items-center gap-1.5 whitespace-nowrap rounded-lg border px-2 py-1.5 text-[11px] font-semibold transition-all xl:px-2.5 xl:text-xs ${
                                            isActive
                                                ? 'bg-white text-emerald-700 shadow-2xs border-slate-200/60'
                                                : 'border-transparent text-slate-600 hover:text-slate-900 hover:bg-white/60'
                                        }`
                                    }
                                    to="/recruiter/profile"
                                >
                                    <Building2 aria-hidden="true" size={14} />
                                    <span className="hidden xl:inline">Tuyển dụng</span>
                                </NavLink>
                            )}

                            {isAdmin && (
                                <NavLink
                                    aria-label="Quản trị"
                                    className={({ isActive }) =>
                                        `inline-flex h-10 shrink-0 items-center gap-1.5 whitespace-nowrap rounded-lg border px-2 py-1.5 text-[11px] font-semibold transition-all xl:px-2.5 xl:text-xs ${
                                            isActive
                                                ? 'bg-white text-blue-800 shadow-2xs border-slate-200/60'
                                                : 'border-transparent text-slate-600 hover:text-slate-900 hover:bg-white/60'
                                        }`
                                    }
                                    to="/admin/candidates"
                                >
                                    <ShieldCheck aria-hidden="true" size={14} />
                                    <span className="hidden xl:inline">Quản trị</span>
                                </NavLink>
                            )}
                        </nav>

                        {/* Bento User Control Hub (When Authenticated) */}
                        {authenticated && user ? (
                            <div className="hidden shrink-0 items-center gap-1 rounded-xl border border-slate-200/80 bg-white p-1 shadow-2xs lg:flex">
                                {/* User Identity Chip */}
                                <div role="group" aria-label={`${displayName}, ${userRoleBadge.label}`} title={displayName} className="flex min-w-0 items-center gap-2 pl-1.5 pr-2 py-0.5">
                                    <span aria-hidden="true" className="grid size-7 shrink-0 place-items-center rounded-lg bg-gradient-to-tr from-blue-700 to-blue-600 text-[11px] font-bold text-white shadow-2xs">
                                        {getInitials(displayName)}
                                    </span>
                                    <div className="hidden min-w-0 items-center gap-1.5 xl:flex">
                                        <span className="max-w-[108px] truncate text-xs font-semibold text-slate-850">
                                            {displayName}
                                        </span>
                                        <span className={`inline-flex shrink-0 items-center whitespace-nowrap rounded-md border px-1.5 py-0.5 text-[10px] font-semibold ${userRoleBadge.className}`}>
                                            {userRoleBadge.label}
                                        </span>
                                    </div>
                                </div>

                                {/* Micro Divider */}
                                <div className="mx-0.5 h-5 w-px bg-slate-200/80" />

                                {/* Change Password Action */}
                                <button
                                    aria-label="Đổi mật khẩu"
                                    className="inline-flex size-9 shrink-0 items-center justify-center rounded-lg text-slate-600 transition-colors hover:bg-blue-50/70 hover:text-blue-700"
                                    onClick={() => setIsChangePasswordOpen(true)}
                                    title="Đổi mật khẩu"
                                    type="button"
                                >
                                    <KeyRound aria-hidden="true" size={15} />
                                </button>

                                {/* Logout Action */}
                                <button
                                    aria-label="Đăng xuất"
                                    className="inline-flex size-9 shrink-0 items-center justify-center rounded-lg text-slate-600 transition-colors hover:bg-rose-50 hover:text-rose-600"
                                    onClick={handleLogout}
                                    title="Đăng xuất"
                                    type="button"
                                >
                                    <LogOut aria-hidden="true" size={15} />
                                </button>
                            </div>
                        ) : (
                            <div className="hidden shrink-0 items-center gap-2 lg:flex">
                                <NavLink
                                    className="btn-bento-secondary shrink-0 whitespace-nowrap px-3 py-2 text-xs"
                                    to="/login"
                                >
                                    <LogIn aria-hidden="true" size={15} />
                                    <span>Đăng nhập</span>
                                </NavLink>

                                <NavLink
                                    className="btn-bento-primary shrink-0 whitespace-nowrap px-3 py-2 text-xs"
                                    to="/register"
                                >
                                    <UserPlus aria-hidden="true" size={15} />
                                    <span>Đăng ký</span>
                                </NavLink>
                            </div>
                        )}

                        <button
                            aria-controls="mobile-navigation"
                            aria-expanded={isMobileNavOpen}
                            aria-label={isMobileNavOpen ? 'Đóng menu' : 'Mở menu'}
                            className="inline-flex size-10 shrink-0 items-center justify-center rounded-xl border border-slate-200 bg-white text-slate-700 shadow-2xs hover:bg-slate-50 lg:hidden"
                            onClick={() => setIsMobileNavOpen((open) => !open)}
                            type="button"
                        >
                            {isMobileNavOpen ? <X aria-hidden="true" size={18} /> : <Menu aria-hidden="true" size={18} />}
                        </button>
                    </div>
                </div>

                {isMobileNavOpen && (
                    <div id="mobile-navigation" className="border-t border-slate-200 bg-white px-4 py-3 shadow-lg sm:px-6 lg:hidden">
                        <nav aria-label="Điều hướng di động" className="grid gap-1">
                            {mobileLinks.map((link) => (
                                <NavLink
                                    key={link.to}
                                    className={({ isActive }) => `rounded-lg px-3 py-2.5 text-sm font-semibold ${isActive ? 'bg-blue-50 text-blue-800' : 'text-slate-700 hover:bg-slate-50'}`}
                                    onClick={() => setIsMobileNavOpen(false)}
                                    to={link.to}
                                >
                                    {link.label}
                                </NavLink>
                            ))}
                        </nav>
                        {authenticated && user ? (
                            <div className="mt-3 flex flex-wrap items-center justify-between gap-2 border-t border-slate-100 pt-3">
                                <div className="min-w-0">
                                    <p className="truncate text-sm font-bold text-slate-900">{displayName}</p>
                                    <span className={`mt-1 inline-flex rounded-md border px-1.5 py-0.5 text-[10px] font-semibold ${userRoleBadge.className}`}>
                                        {userRoleBadge.label}
                                    </span>
                                </div>
                                <div className="flex items-center gap-2">
                                    <button
                                        className="inline-flex items-center gap-1.5 rounded-lg border border-slate-200 px-3 py-2 text-xs font-semibold text-slate-700"
                                        onClick={() => { setIsChangePasswordOpen(true); setIsMobileNavOpen(false) }}
                                        type="button"
                                    >
                                        <KeyRound aria-hidden="true" size={13} /> Đổi mật khẩu
                                    </button>
                                    <button
                                        className="inline-flex items-center gap-1.5 rounded-lg border border-rose-200 px-3 py-2 text-xs font-semibold text-rose-700"
                                        onClick={() => { setIsMobileNavOpen(false); void handleLogout() }}
                                        type="button"
                                    >
                                        <LogOut aria-hidden="true" size={13} /> Đăng xuất
                                    </button>
                                </div>
                            </div>
                        ) : (
                            <div className="mt-3 flex gap-2 border-t border-slate-100 pt-3">
                                <NavLink className="btn-bento-secondary flex-1 justify-center text-sm" onClick={() => setIsMobileNavOpen(false)} to="/login">
                                    <LogIn aria-hidden="true" size={15} /> Đăng nhập
                                </NavLink>
                                <NavLink className="btn-bento-primary flex-1 justify-center text-sm" onClick={() => setIsMobileNavOpen(false)} to="/register">
                                    <UserPlus aria-hidden="true" size={15} /> Đăng ký
                                </NavLink>
                            </div>
                        )}
                    </div>
                )}
            </header>

            <main className="flex-1">
                <Outlet />
            </main>

            <footer className="border-t border-slate-200/80 bg-white">
                <div className="mx-auto flex w-full max-w-[1200px] flex-col gap-3 px-4 py-6 text-xs text-slate-600 sm:flex-row sm:items-center sm:justify-between sm:px-6 lg:px-8">
                    <div className="flex items-center gap-2">
                        <span className="bento-badge border border-slate-200 bg-slate-50 text-slate-700 text-[10px]">
                            TALENTBRIDGE
                        </span>
                        <span>© {new Date().getFullYear()} TalentBridge</span>
                    </div>
                    <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-slate-600">
                        <span>Tìm việc minh bạch</span>
                        <span aria-hidden="true">·</span>
                        <span>Quản lý tuyển dụng</span>
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
