import { useEffect, useState } from 'react'
import {
    BriefcaseBusiness,
    Building2,
    FileText,
    Home,
    KeyRound,
    LogIn,
    LogOut,
    Menu,
    MessageCircle,
    Search,
    ShieldCheck,
    User,
    UserPlus,
    X,
} from 'lucide-react'
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router'
import { getStoredUser, isAuthenticated } from '../features/auth/tokenStorage'
import { logoutUser } from '../features/auth/authApi'
import type { UserResponse } from '../features/auth/authTypes'
import { ChangePasswordModal } from '../features/auth/components/ChangePasswordModal'

function initialsFor(name: string) {
    const parts = name.trim().split(/\s+/).filter(Boolean)
    if (parts.length === 0) return 'TB'
    if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase()
    return `${parts[0][0]}${parts[parts.length - 1][0]}`.toUpperCase()
}

export function MainLayout() {
    const navigate = useNavigate()
    const pathname = useLocation().pathname
    const isLoginPage = pathname === '/login'
    const isRegisterPage = pathname === '/register'
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

    function handleLogout() {
        void logoutUser()
        setIsMobileNavOpen(false)
        navigate('/login')
    }

    const isAdmin = authenticated && Boolean(user?.roles?.includes('ROLE_ADMIN'))
    const isRecruiter = authenticated && Boolean(user?.roles?.includes('ROLE_RECRUITER'))
    const isCandidate = authenticated && (Boolean(user?.roles?.includes('ROLE_CANDIDATE')) || (!isAdmin && !isRecruiter))
    const displayName = user?.fullName?.replace(/\s*\((?:Admin|Recruiter|Candidate)\)\s*$/i, '') ?? ''
    const roleBadge = isAdmin
        ? { label: 'Quản trị', className: 'border-blue-200 bg-blue-50 text-blue-800' }
        : isRecruiter
            ? { label: 'Nhà tuyển dụng', className: 'border-emerald-200 bg-emerald-50 text-emerald-800' }
            : { label: 'Ứng viên', className: 'border-blue-200 bg-blue-50 text-blue-800' }

    const navigationLinks = [
        { to: '/', label: 'Trang chủ', icon: Home, end: true },
        { to: '/jobs', label: 'Việc làm', icon: Search },
        { to: '/assistant', label: 'Trợ lý', icon: MessageCircle },
        ...(isCandidate ? [
            { to: '/candidate/profile', label: 'Hồ sơ TopCV', icon: User },
            { to: '/candidate/applications', label: 'Đơn ứng tuyển', icon: FileText },
        ] : []),
        ...(isRecruiter ? [{ to: '/recruiter/profile', label: 'Tuyển dụng', icon: Building2 }] : []),
        ...(isAdmin ? [{ to: '/admin/candidates', label: 'Quản trị', icon: ShieldCheck }] : []),
    ]

    function closeMobileNav() {
        setIsMobileNavOpen(false)
    }

    return (
        <div className="flex min-h-screen flex-col bg-bento-canvas text-slate-900">
            {!isLoginPage && (
            <header className="sticky top-0 z-50 border-b border-slate-200 bg-white">
                <div className="mx-auto flex h-16 w-full max-w-[1280px] items-center justify-between gap-3 px-4 sm:h-[72px] sm:px-6 xl:h-[84px] xl:px-0">
                    <NavLink aria-label="TalentBridge — Trang chủ" className="flex shrink-0 items-center gap-2.5" end to="/">
                        <span className="grid size-9 place-items-center rounded-lg bg-blue-700 text-white">
                            <BriefcaseBusiness aria-hidden="true" size={18} />
                        </span>
                        <span className="text-base font-bold tracking-tight text-slate-900">TalentBridge</span>
                    </NavLink>

                    {!isRegisterPage && <nav aria-label="Điều hướng chính" className="hidden min-w-0 items-center gap-1 rounded-xl border border-slate-200 bg-slate-50 p-1 xl:flex">
                        {navigationLinks.map(({ to, label, icon: Icon, end }) => (
                            <NavLink
                                aria-label={label}
                                className={({ isActive }) => `inline-flex h-10 shrink-0 items-center gap-1.5 whitespace-nowrap rounded-lg border px-2.5 text-[13px] font-semibold transition-colors 2xl:px-3 ${
                                    isActive
                                        ? 'border-slate-200 bg-white text-slate-900 shadow-xs'
                                        : 'border-transparent text-slate-700 hover:border-slate-200 hover:bg-white hover:text-blue-700'
                                }`}
                                end={end}
                                key={to}
                                to={to}
                            >
                                <Icon aria-hidden="true" size={14} />
                                <span>{label}</span>
                            </NavLink>
                        ))}
                    </nav>}

                    {authenticated && user ? (
                        <div className="hidden min-w-0 shrink-0 items-center gap-2 rounded-xl border border-slate-200 bg-white px-2 py-1.5 xl:flex">
                            <div aria-label={`${displayName}, ${roleBadge.label}`} className="flex min-w-0 items-center gap-2" role="group" title={displayName}>
                                <span aria-hidden="true" className="grid size-8 shrink-0 place-items-center rounded-lg bg-blue-700 text-[11px] font-bold text-white">
                                    {initialsFor(displayName)}
                                </span>
                                <span className="max-w-[116px] truncate text-[13px] font-semibold text-slate-800">{displayName}</span>
                                <span className={`inline-flex shrink-0 items-center rounded-md border px-1.5 py-1 text-[10px] font-semibold ${roleBadge.className}`}>
                                    {roleBadge.label}
                                </span>
                            </div>
                            <span aria-hidden="true" className="mx-0.5 h-6 w-px shrink-0 bg-slate-200" />
                            <button aria-label="Đổi mật khẩu" className="grid size-8 shrink-0 place-items-center rounded-lg text-slate-600 transition hover:bg-slate-100 hover:text-blue-700" onClick={() => setIsChangePasswordOpen(true)} title="Đổi mật khẩu" type="button">
                                <KeyRound aria-hidden="true" size={15} />
                            </button>
                            <button aria-label="Đăng xuất" className="grid size-8 shrink-0 place-items-center rounded-lg text-slate-600 transition hover:bg-slate-100 hover:text-blue-700" onClick={() => void handleLogout()} title="Đăng xuất" type="button">
                                <LogOut aria-hidden="true" size={15} />
                            </button>
                        </div>
                    ) : (
                        <div className="hidden shrink-0 items-center gap-2 xl:flex">
                            <NavLink className="btn-bento-secondary h-10 px-3.5 text-[13px]" to="/login">Đăng nhập</NavLink>
                            <NavLink className="btn-bento-primary h-10 px-3.5 text-[13px]" to="/register">Đăng ký</NavLink>
                        </div>
                    )}

                    {isRegisterPage && !authenticated && (
                        <div className="flex shrink-0 items-center gap-2 xl:hidden">
                            <NavLink className="btn-bento-secondary h-9 px-3 text-xs" to="/login">Đăng nhập</NavLink>
                            <span className="btn-bento-primary h-9 px-3 text-xs" aria-current="page">Đăng ký</span>
                        </div>
                    )}

                    {!isRegisterPage && <button
                        aria-controls="mobile-navigation"
                        aria-expanded={isMobileNavOpen}
                        aria-label={isMobileNavOpen ? 'Đóng menu' : 'Mở menu'}
                        className="grid size-10 shrink-0 place-items-center rounded-lg border border-slate-200 bg-white text-slate-700 xl:hidden"
                        onClick={() => setIsMobileNavOpen((open) => !open)}
                        type="button"
                    >
                        {isMobileNavOpen ? <X aria-hidden="true" size={19} /> : <Menu aria-hidden="true" size={19} />}
                    </button>}
                </div>

                {isMobileNavOpen && (
                    <div id="mobile-navigation" className="border-t border-slate-200 bg-white px-4 py-3 shadow-lg sm:px-6 xl:hidden">
                        <nav aria-label="Điều hướng di động" className="grid gap-1">
                            {navigationLinks.map(({ to, label, icon: Icon, end }) => (
                                <NavLink
                                    className={({ isActive }) => `flex items-center gap-3 rounded-lg border px-3 py-2.5 text-sm font-semibold ${isActive ? 'border-blue-100 bg-blue-50 text-blue-800' : 'border-transparent text-slate-700 hover:bg-slate-50'}`}
                                    end={end}
                                    key={to}
                                    onClick={closeMobileNav}
                                    to={to}
                                >
                                    <Icon aria-hidden="true" size={17} />{label}
                                </NavLink>
                            ))}
                        </nav>
                        {authenticated && user ? (
                            <div className="mt-3 flex flex-wrap items-center justify-between gap-3 border-t border-slate-100 pt-3">
                                <div className="flex min-w-0 items-center gap-2">
                                    <span aria-hidden="true" className="grid size-8 shrink-0 place-items-center rounded-lg bg-blue-700 text-[11px] font-bold text-white">{initialsFor(displayName)}</span>
                                    <div className="min-w-0"><p className="truncate text-sm font-semibold text-slate-900">{displayName}</p><span className={`mt-1 inline-flex rounded-md border px-1.5 py-0.5 text-[10px] font-semibold ${roleBadge.className}`}>{roleBadge.label}</span></div>
                                </div>
                                <div className="flex gap-2">
                                    <button className="inline-flex items-center gap-2 rounded-lg border border-slate-200 px-3 py-2 text-xs font-semibold text-slate-700" onClick={() => { setIsChangePasswordOpen(true); closeMobileNav() }} type="button"><KeyRound aria-hidden="true" size={14} />Đổi mật khẩu</button>
                                    <button className="inline-flex items-center gap-2 rounded-lg border border-slate-200 px-3 py-2 text-xs font-semibold text-slate-700" onClick={() => void handleLogout()} type="button"><LogOut aria-hidden="true" size={14} />Đăng xuất</button>
                                </div>
                            </div>
                        ) : (
                            <div className="mt-3 flex gap-2 border-t border-slate-100 pt-3">
                                <NavLink className="btn-bento-secondary flex-1 text-sm" onClick={closeMobileNav} to="/login"><LogIn aria-hidden="true" size={15} />Đăng nhập</NavLink>
                                <NavLink className="btn-bento-primary flex-1 text-sm" onClick={closeMobileNav} to="/register"><UserPlus aria-hidden="true" size={15} />Đăng ký</NavLink>
                            </div>
                        )}
                    </div>
                )}
            </header>
            )}

            <main className="flex-1">
                <Outlet />
            </main>

            <footer className="border-t border-slate-200 bg-white">
                <div className="mx-auto flex min-h-16 w-full max-w-[1280px] flex-col justify-center gap-2 px-4 py-3 text-xs text-slate-600 sm:flex-row sm:items-center sm:justify-between sm:px-6 xl:px-0">
                    <span>TalentBridge · Kết nối nghề nghiệp có trách nhiệm</span>
                    <span className="font-medium text-slate-500">Thông tin tuyển dụng do doanh nghiệp cung cấp</span>
                </div>
            </footer>

            <ChangePasswordModal isOpen={isChangePasswordOpen} onClose={() => setIsChangePasswordOpen(false)} />
        </div>
    )
}
