import { useEffect, useState } from 'react'
import {
    BriefcaseBusiness,
    Building2,
    ChevronDown,
    FileText,
    Home,
    KeyRound,
    LogIn,
    LogOut,
    MessageCircle,
    Search,
    Settings,
    ShieldCheck,
    User,
    UserPlus,
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
    const normalizedPathname = pathname.replace(/\/+$/, '') || '/'
    const isLoginPage = normalizedPathname === '/login'
    const isRegisterPage = normalizedPathname === '/register'
    const showBrowsingNavigation = !['/login', '/register', '/forgot-password', '/reset-password'].includes(normalizedPathname)
    const [user, setUser] = useState<UserResponse | null>(() => getStoredUser())
    const [authenticated, setAuthenticated] = useState<boolean>(() => isAuthenticated())
    const [isChangePasswordOpen, setIsChangePasswordOpen] = useState(false)
    const [isSettingsOpen, setIsSettingsOpen] = useState(false)

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
        setIsSettingsOpen(false)
        navigate('/login')
    }

    const isAdmin = authenticated && Boolean(user?.roles?.includes('ROLE_ADMIN'))
    const isRecruiter = authenticated && Boolean(user?.roles?.includes('ROLE_RECRUITER'))
    const isCandidate = authenticated && (Boolean(user?.roles?.includes('ROLE_CANDIDATE')) || (!isAdmin && !isRecruiter))
    const displayName = user?.fullName?.replace(/\s*\((?:Admin|Recruiter|Candidate)\)\s*$/i, '') || 'Tài khoản'
    const roleBadge = isAdmin
        ? { label: 'Quản trị', className: 'border-blue-200 bg-blue-50 text-blue-800' }
        : isRecruiter
            ? { label: 'Nhà tuyển dụng', className: 'border-emerald-200 bg-emerald-50 text-emerald-800' }
            : { label: 'Ứng viên', className: 'border-blue-200 bg-blue-50 text-blue-800' }

    const navigationLinks = [
        { to: '/', label: 'Trang chủ', icon: Home, end: true },
        { to: '/jobs', label: 'Việc làm', icon: Search },
        { to: isAdmin ? '/admin/assistant' : '/assistant', label: isAdmin ? 'Trợ lý quản trị' : 'Trợ lý', icon: MessageCircle },
        ...(isCandidate ? [
            { to: '/candidate/profile', label: 'Hồ sơ TopCV', icon: User },
            { to: '/candidate/applications', label: 'Đơn ứng tuyển', icon: FileText },
        ] : []),
        ...(isRecruiter ? [{ to: '/recruiter/profile', label: 'Tuyển dụng', icon: Building2 }] : []),
        ...(isAdmin ? [{ to: '/admin/candidates', label: 'Quản trị', icon: ShieldCheck }] : []),
    ]

    return (
        <div className="flex min-h-screen flex-col bg-bento-canvas text-slate-900">
            {!isLoginPage && (
            <header className="sticky top-0 z-50 border-b border-slate-200 bg-white">
                <div className="mx-auto flex min-h-[76px] w-full max-w-[1600px] flex-wrap items-center gap-x-5 gap-y-2 px-4 py-3 sm:px-6 lg:px-8">
                    <NavLink aria-label="TalentBridge — Trang chủ" className="flex shrink-0 items-center gap-2.5" end to="/">
                        <span className="grid size-9 place-items-center rounded-lg bg-blue-700 text-white">
                            <BriefcaseBusiness aria-hidden="true" size={18} />
                        </span>
                        <span className="text-base font-bold tracking-tight text-slate-900">TalentBridge</span>
                    </NavLink>

                    {showBrowsingNavigation && <nav aria-label="Điều hướng chính" className="order-3 -mx-4 flex w-[calc(100%+2rem)] min-w-0 gap-1 overflow-x-auto border-t border-slate-100 px-4 pt-2 sm:-mx-6 sm:w-[calc(100%+3rem)] sm:px-6 md:order-none md:mx-0 md:w-auto md:flex-1 md:justify-center md:gap-0 md:border-0 md:p-0">
                        {navigationLinks.map(({ to, label, icon: Icon, end }) => (
                            <NavLink
                                aria-label={label}
                                className={({ isActive }) => `inline-flex min-h-10 shrink-0 items-center gap-1.5 whitespace-nowrap rounded-xl px-2.5 py-2 text-xs font-semibold transition-colors 2xl:gap-2 2xl:px-3 2xl:text-sm ${
                                    isActive
                                        ? 'bg-blue-50 text-blue-800 ring-1 ring-inset ring-blue-100'
                                        : 'text-slate-600 hover:bg-slate-50 hover:text-slate-950'
                                }`}
                                end={end}
                                key={to}
                                to={to}
                            >
                                <Icon aria-hidden="true" className="shrink-0" size={16} />
                                <span>{label}</span>
                            </NavLink>
                        ))}
                    </nav>}

                    {authenticated && user ? (
                        <div className={`relative order-2 ml-auto flex shrink-0 items-center gap-2 md:order-none md:ml-0 ${isSettingsOpen ? 'z-50' : ''}`}>
                            <span aria-hidden="true" className="grid size-9 shrink-0 place-items-center rounded-xl bg-blue-50 text-xs font-extrabold text-blue-800">
                                    {initialsFor(displayName)}
                            </span>
                            <div className="hidden min-w-0 max-w-36 sm:block">
                                <p className="truncate text-sm font-bold text-slate-900" title={displayName}>{displayName}</p>
                                <span className={`mt-0.5 inline-flex rounded-md border px-1.5 py-0.5 text-[10px] font-semibold ${roleBadge.className}`}>{roleBadge.label}</span>
                            </div>
                            <button
                                aria-expanded={isSettingsOpen}
                                aria-haspopup="menu"
                                aria-label="Cài đặt tài khoản"
                                className={`inline-flex h-10 items-center gap-2 rounded-xl border px-2.5 text-sm font-semibold transition sm:px-3 ${isSettingsOpen ? 'relative z-50 border-blue-200 bg-blue-50 text-blue-800' : 'border-slate-200 bg-white text-slate-700 hover:bg-slate-50'}`}
                                onClick={() => setIsSettingsOpen((open) => !open)}
                                title="Cài đặt"
                                type="button"
                            >
                                <Settings aria-hidden="true" size={16} />
                                <span className="hidden sm:inline">Cài đặt</span>
                                <ChevronDown aria-hidden="true" size={15} />
                            </button>
                            {isSettingsOpen && (
                                <>
                                    <button aria-label="Đóng cài đặt" className="fixed inset-0 z-40 cursor-default" onClick={() => setIsSettingsOpen(false)} type="button" />
                                    <div className="absolute right-0 top-full z-50 mt-2 w-56 rounded-2xl border border-slate-200 bg-white p-2 shadow-xl shadow-slate-900/10" role="menu">
                                        <div className="border-b border-slate-100 px-3 py-2 sm:hidden">
                                            <p className="truncate text-sm font-bold text-slate-900">{displayName}</p>
                                            <p className="text-xs text-slate-500">{roleBadge.label}</p>
                                        </div>
                                        <button
                                            className="flex min-h-10 w-full items-center gap-2.5 rounded-xl px-3 text-left text-sm font-medium text-slate-700 transition hover:bg-slate-50 hover:text-blue-800"
                                            onClick={() => { setIsSettingsOpen(false); setIsChangePasswordOpen(true) }}
                                            role="menuitem"
                                            type="button"
                                        >
                                            <KeyRound aria-hidden="true" size={16} />Đổi mật khẩu
                                        </button>
                                        <button
                                            className="flex min-h-10 w-full items-center gap-2.5 rounded-xl px-3 text-left text-sm font-semibold text-rose-700 transition hover:bg-rose-50"
                                            onClick={handleLogout}
                                            role="menuitem"
                                            type="button"
                                        >
                                            <LogOut aria-hidden="true" size={16} />Đăng xuất
                                        </button>
                                    </div>
                                </>
                            )}
                        </div>
                    ) : (
                        <div className="order-2 ml-auto flex shrink-0 items-center gap-2 md:order-none md:ml-0">
                            <NavLink className="btn-bento-secondary h-9 px-2.5 text-xs sm:h-10 sm:px-3.5 sm:text-[13px]" to="/login"><LogIn aria-hidden="true" className="mr-1 inline" size={14} />Đăng nhập</NavLink>
                            {isRegisterPage
                                ? <span aria-current="page" className="btn-bento-primary h-9 px-2.5 text-xs sm:h-10 sm:px-3.5 sm:text-[13px]">Đăng ký</span>
                                : <NavLink className="btn-bento-primary h-9 px-2.5 text-xs sm:h-10 sm:px-3.5 sm:text-[13px]" to="/register"><UserPlus aria-hidden="true" className="mr-1 inline" size={14} />Đăng ký</NavLink>}
                        </div>
                    )}

                </div>
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
