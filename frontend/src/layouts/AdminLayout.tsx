import { useEffect, useState } from 'react'
import {
    Activity,
    BriefcaseBusiness,
    Building2,
    ChevronDown,
    KeyRound,
    LogOut,
    MessageCircle,
    ShieldAlert,
    UserCheck,
    Users,
} from 'lucide-react'
import { Link, NavLink, Outlet, useNavigate } from 'react-router'
import { ChangePasswordModal } from '../features/auth/components/ChangePasswordModal'
import { logoutUser } from '../features/auth/authApi'
import { getStoredUser, isAuthenticated } from '../features/auth/tokenStorage'
import type { UserResponse } from '../features/auth/authTypes'

function initialsFor(name: string) {
    const parts = name.trim().split(/\s+/).filter(Boolean)
    if (parts.length === 0) return 'TB'
    if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase()
    return `${parts[0][0]}${parts[parts.length - 1][0]}`.toUpperCase()
}

export function AdminLayout() {
    const navigate = useNavigate()
    const [user, setUser] = useState<UserResponse | null>(() => getStoredUser())
    const [authenticated, setAuthenticated] = useState<boolean>(() => isAuthenticated())
    const [settingsOpen, setSettingsOpen] = useState(false)
    const [changePasswordOpen, setChangePasswordOpen] = useState(false)

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

    if (!authenticated || !user?.roles?.includes('ROLE_ADMIN')) {
        return (
            <main className="grid min-h-[70vh] place-items-center bg-bento-canvas px-4 py-12">
                <section className="bento-card w-full max-w-md p-7 text-center">
                    <span className="mx-auto grid size-12 place-items-center rounded-xl bg-rose-50 text-rose-700"><ShieldAlert aria-hidden="true" size={23} /></span>
                    <h1 className="mt-4 text-xl font-bold text-slate-900">Truy cập bị từ chối</h1>
                    <p className="mt-2 text-sm leading-6 text-slate-600">Đăng nhập bằng tài khoản Quản trị viên (ROLE_ADMIN) để mở khu vực quản trị.</p>
                    <div className="mt-5 flex justify-center gap-3">
                        <button className="btn-bento-primary" onClick={() => navigate('/login')} type="button">Đăng nhập</button>
                        <button className="btn-bento-secondary" onClick={() => navigate('/')} type="button">Trang chủ</button>
                    </div>
                </section>
            </main>
        )
    }

    const displayName = user.fullName?.replace(/\s*\((?:Admin|Recruiter|Candidate)\)\s*$/i, '') || 'Quản trị viên'
    const navigationItems = [
        { to: '/admin/assistant', label: 'Trợ lý quản trị', icon: MessageCircle },
        { to: '/admin/candidates', label: 'Ứng viên', icon: Users },
        { to: '/admin/recruiters', label: 'Nhà tuyển dụng', icon: UserCheck },
        { to: '/admin/companies', label: 'Doanh nghiệp', icon: Building2 },
        { to: '/admin/jobs', label: 'Tin tuyển dụng', icon: BriefcaseBusiness },
        { to: '/admin/metrics', label: 'Thống kê API', icon: Activity },
    ]

    function handleLogout() {
        setSettingsOpen(false)
        void logoutUser()
        navigate('/login')
    }

    return (
        <div className="min-h-screen bg-[#F5F7FB] text-[#172033]">
            <header className="sticky top-0 z-40 border-b border-slate-200 bg-white/95 shadow-sm shadow-slate-900/[0.02] backdrop-blur">
                <div className="mx-auto flex min-h-[76px] w-full max-w-[1600px] flex-wrap items-center gap-x-5 gap-y-2 px-4 py-3 sm:px-6 lg:px-8">
                    <Link aria-label="TalentBridge — Trang chủ" className="flex shrink-0 items-center gap-2.5" to="/">
                        <span className="grid size-10 place-items-center rounded-xl bg-[#1D4ED8] text-white shadow-sm shadow-blue-900/20"><BriefcaseBusiness aria-hidden="true" size={19} /></span>
                        <span className="leading-tight">
                            <span className="block text-base font-extrabold tracking-tight text-slate-950">TalentBridge</span>
                            <span className="mt-0.5 block text-[10px] font-bold uppercase tracking-[0.12em] text-slate-500">Quản trị hệ thống</span>
                        </span>
                    </Link>

                    <nav
                        aria-label="Điều hướng quản trị"
                        className="order-3 -mx-4 flex w-[calc(100%+2rem)] gap-1 overflow-x-auto border-t border-slate-100 px-4 pt-2 md:order-none md:mx-0 md:w-auto md:flex-1 md:justify-center md:gap-0 md:border-0 md:p-0"
                        id="admin-primary-navigation"
                    >
                        {navigationItems.map(({ to, label, icon: Icon }) => (
                            <NavLink
                                className={({ isActive }) => `inline-flex min-h-10 shrink-0 items-center gap-1.5 rounded-xl px-2.5 py-2 text-xs font-semibold transition 2xl:gap-2 2xl:px-3 2xl:text-sm ${isActive ? 'bg-blue-50 text-blue-800 ring-1 ring-inset ring-blue-100' : 'text-slate-600 hover:bg-slate-50 hover:text-slate-950'}`}
                                key={to}
                                to={to}
                            >
                                <Icon aria-hidden="true" className="shrink-0" size={16} />
                                <span>{label}</span>
                            </NavLink>
                        ))}
                    </nav>

                    <div className={`relative order-2 ml-auto flex shrink-0 items-center gap-2 md:order-none md:ml-0 ${settingsOpen ? 'z-50' : ''}`}>
                        <span aria-hidden="true" className="grid size-9 place-items-center rounded-xl bg-blue-50 text-xs font-extrabold text-blue-800">{initialsFor(displayName)}</span>
                        <div className="hidden min-w-0 max-w-36 sm:block">
                            <p className="truncate text-sm font-bold text-slate-900" title={displayName}>{displayName}</p>
                            <p className="text-[11px] font-medium text-slate-500">Quản trị viên</p>
                        </div>
                        <button
                            aria-expanded={settingsOpen}
                            aria-haspopup="menu"
                            aria-label="Cài đặt tài khoản"
                            className={`inline-flex h-10 items-center gap-2 rounded-xl border px-3 text-sm font-semibold transition ${settingsOpen ? 'relative z-50 border-blue-200 bg-blue-50 text-blue-800' : 'border-slate-200 bg-white text-slate-700 hover:bg-slate-50'}`}
                            onClick={() => setSettingsOpen((open) => !open)}
                            type="button"
                        >
                            <span className="hidden lg:inline">Cài đặt</span><ChevronDown aria-hidden="true" size={15} />
                        </button>
                        {settingsOpen && (
                            <>
                                <button aria-label="Đóng cài đặt" className="fixed inset-0 z-40 cursor-default" onClick={() => setSettingsOpen(false)} type="button" />
                                <div className="absolute right-0 top-full z-50 mt-2 w-56 rounded-2xl border border-slate-200 bg-white p-2 shadow-xl shadow-slate-900/10" role="menu">
                                    <div className="border-b border-slate-100 px-3 py-2 sm:hidden">
                                        <p className="truncate text-sm font-bold text-slate-900">{displayName}</p>
                                        <p className="text-xs text-slate-500">Quản trị viên</p>
                                    </div>
                                    <button
                                        className="flex min-h-10 w-full items-center gap-2.5 rounded-xl px-3 text-left text-sm font-medium text-slate-700 transition hover:bg-slate-50 hover:text-blue-800"
                                        onClick={() => { setSettingsOpen(false); setChangePasswordOpen(true) }}
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
                </div>
            </header>

            <main className="mx-auto min-h-[calc(100dvh-140px)] w-full max-w-[1600px] px-4 py-6 sm:px-6 sm:py-8 lg:px-8">
                <Outlet />
            </main>

            <footer className="flex min-h-14 items-center justify-between gap-3 border-t border-slate-200 bg-white px-5 text-xs text-slate-500 sm:px-8">
                <span>TalentBridge · Kết nối nghề nghiệp có trách nhiệm</span>
                <span className="hidden font-semibold sm:inline">TalentBridge ATS · Khu vực quản trị</span>
            </footer>

            <ChangePasswordModal isOpen={changePasswordOpen} onClose={() => setChangePasswordOpen(false)} />
        </div>
    )
}
