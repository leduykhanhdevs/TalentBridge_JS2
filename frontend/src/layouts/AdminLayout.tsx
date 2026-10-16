import { useEffect, useState } from 'react'
import {
    ArrowLeft,
    Bot,
    Building2,
    BriefcaseBusiness,
    LayoutDashboard,
    LogOut,
    Menu,
    ShieldAlert,
    UserCheck,
    Users,
    X,
} from 'lucide-react'
import { NavLink, Outlet, useNavigate } from 'react-router'
import { getStoredUser, isAuthenticated } from '../features/auth/tokenStorage'
import { logoutUser } from '../features/auth/authApi'
import type { UserResponse } from '../features/auth/authTypes'

export function AdminLayout() {
    const navigate = useNavigate()
    const [user, setUser] = useState<UserResponse | null>(() => getStoredUser())
    const [authenticated, setAuthenticated] = useState<boolean>(() => isAuthenticated())
    const [isMobileActionsOpen, setIsMobileActionsOpen] = useState(false)

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

    const displayName = user?.fullName
        ? user.fullName.replace(/\s*\((?:Admin|Recruiter|Candidate)\)\s*$/i, '')
        : ''

    const getInitials = (name: string) => {
        const parts = name.trim().split(/\s+/)
        if (!parts.length || !parts[0]) return 'AD'
        if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase()
        return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
    }

    if (!isAdmin) {
        return (
            <div className="flex min-h-screen items-center justify-center p-4">
                <div className="bento-card w-full max-w-md p-6 text-center">
                    <div className="mb-4 flex items-center justify-between border-b border-slate-100 pb-2.5 text-left">
                        <div className="flex items-center gap-1.5">
                            <div className="size-2.5 rounded-full bg-rose-400" />
                            <div className="size-2.5 rounded-full bg-amber-400" />
                            <div className="size-2.5 rounded-full bg-emerald-400" />
                            <span className="text-xs font-bold text-slate-700 ml-1">Kiểm tra quyền truy cập</span>
                        </div>
                        <span className="bento-badge border-rose-200 bg-rose-50 text-rose-700 text-[10px]">
                            ACCESS_DENIED
                        </span>
                    </div>

                    <div className="mx-auto mb-4 grid size-12 place-items-center rounded-2xl border border-rose-200 bg-rose-50 text-rose-700 shadow-xs">
                        <ShieldAlert size={26} />
                    </div>
                    <h1 className="text-lg font-bold tracking-tight text-slate-900">Truy cập bị từ chối</h1>
                    <p className="mt-2 text-xs font-semibold text-slate-600 leading-relaxed">
                        Bạn cần đăng nhập bằng tài khoản Quản trị viên (ROLE_ADMIN) để truy cập cổng quản trị.
                    </p>
                    <div className="mt-6 flex flex-col gap-2.5 sm:flex-row sm:justify-center">
                        <button
                            className="btn-bento-primary h-10 px-4 text-xs font-bold"
                            onClick={() => navigate('/login')}
                            type="button"
                        >
                            Đăng nhập lại
                        </button>
                        <button
                            className="btn-bento-secondary h-10 px-4 text-xs font-bold"
                            onClick={() => navigate('/')}
                            type="button"
                        >
                            Trang chủ
                        </button>
                    </div>
                </div>
            </div>
        )
    }

    const navItems = [
        {
            to: '/admin/candidates',
            label: 'Quản lý ứng viên',
            mobileLabel: 'Ứng viên',
            icon: Users,
            desc: 'Danh sách, tìm kiếm, khóa/mở',
        },
        {
            to: '/admin/recruiters',
            label: 'Nhà tuyển dụng (HR)',
            mobileLabel: 'HR',
            icon: UserCheck,
            desc: 'Quản lý HR & công ty trực thuộc',
        },
        {
            to: '/admin/companies',
            label: 'Duyệt doanh nghiệp',
            mobileLabel: 'Công ty',
            icon: Building2,
            desc: 'Phê duyệt & lý do từ chối',
        },
        {
            to: '/admin/jobs',
            label: 'Kiểm duyệt tin',
            mobileLabel: 'Tin',
            icon: BriefcaseBusiness,
            desc: 'Duyệt, từ chối và gỡ tin',
        },
        {
            to: '/assistant',
            label: 'Trợ lý TalentBridge',
            mobileLabel: 'Trợ lý',
            icon: Bot,
            desc: 'Hướng dẫn sử dụng trong ứng dụng',
        },
    ]

    return (
        <div className="admin-shell relative flex min-h-screen flex-col overflow-x-clip bg-bento-canvas text-slate-900 selection:bg-blue-100 selection:text-blue-900">
            {/* Header */}
            <header className="sticky top-0 z-40 border-b border-slate-200 bg-white/95 text-slate-900 shadow-sm backdrop-blur-md">
                <div className="mx-auto flex h-16 max-w-[1200px] items-center justify-between gap-2 px-4 sm:px-6 lg:px-8">
                    <div className="flex items-center gap-3">
                        <div className="grid size-10 place-items-center rounded-xl bg-gradient-to-br from-blue-600 via-blue-700 to-blue-800 text-white font-black shadow-md shadow-blue-950/50 border border-blue-400/30">
                            <LayoutDashboard size={20} />
                        </div>
                        <div>
                            <div className="flex items-center gap-2">
                                <span className="text-sm font-bold tracking-tight text-slate-900 sm:text-lg">TalentBridge Admin</span>
                                <span className="hidden items-center gap-1 rounded-full border border-blue-200 bg-blue-50 px-2 py-0.5 text-[10px] font-bold uppercase tracking-wider text-blue-800 shadow-2xs md:inline-flex">
                                    <span className="size-1.5 rounded-full bg-emerald-400 animate-pulse" />
                                    SYS_ADMIN
                                </span>
                            </div>
                            <p className="hidden sm:block text-[11px] font-medium text-slate-600">
                                Trung tâm Kiểm soát Hệ sinh thái Tuyển dụng & Xác thực Doanh nghiệp
                            </p>
                        </div>
                    </div>

                    <div className="flex items-center gap-2 sm:gap-3">
                        <NavLink
                            className="hidden items-center gap-1.5 rounded-lg border border-slate-200 bg-white px-3 py-2 text-xs font-semibold text-slate-700 shadow-2xs transition hover:border-slate-300 hover:bg-slate-50 hover:text-slate-900 md:inline-flex"
                            to="/"
                        >
                            <ArrowLeft size={14} />
                            <span>Về website</span>
                        </NavLink>

                        {/* Bento Admin User Capsule */}
                        <div className="hidden items-center rounded-lg border border-slate-200 bg-white p-1 shadow-2xs md:flex">
                            <div className="flex items-center gap-2 pl-1.5 pr-2 py-0.5">
                                <span className="grid size-7 place-items-center rounded-lg bg-blue-700 text-[11px] font-bold text-white shadow-2xs">
                                    {getInitials(displayName)}
                                </span>
                                <div className="flex items-center gap-1.5">
                                    <span className="max-w-[130px] truncate text-xs font-semibold text-slate-900 sm:max-w-none">
                                        {displayName}
                                    </span>
                                    <span className="hidden sm:inline-flex items-center rounded-md border border-blue-200 bg-blue-50 px-1.5 py-0.5 text-[10px] font-semibold text-blue-800">
                                        Quản trị viên
                                    </span>
                                </div>
                            </div>

                            <div className="mx-0.5 h-4 w-px bg-slate-200" />

                            <button
                                className="inline-flex items-center gap-1.5 rounded-lg px-2.5 py-1.5 text-xs font-medium text-slate-600 transition-colors hover:bg-rose-50 hover:text-rose-700"
                                onClick={handleLogout}
                                title="Đăng xuất"
                                type="button"
                            >
                                <LogOut size={13} />
                                <span className="hidden sm:inline">Đăng xuất</span>
                            </button>
                        </div>

                        <button
                            aria-controls="admin-mobile-actions"
                            aria-expanded={isMobileActionsOpen}
                            aria-label={isMobileActionsOpen ? 'Đóng menu tài khoản' : 'Mở menu tài khoản'}
                            className="inline-flex size-10 shrink-0 items-center justify-center rounded-lg border border-slate-200 bg-white text-slate-700 md:hidden"
                            onClick={() => setIsMobileActionsOpen((open) => !open)}
                            type="button"
                        >
                            {isMobileActionsOpen ? <X aria-hidden="true" size={18} /> : <Menu aria-hidden="true" size={18} />}
                        </button>
                    </div>
                </div>

                {/* Subnav Bento Navigation Dock */}
                <div className="border-t border-slate-200 bg-slate-50/80 px-4 py-2 sm:px-6 lg:px-8">
                    <div className="mx-auto flex min-w-0 max-w-[1200px] items-center justify-between gap-4">
                        <nav
                            aria-label="Điều hướng quản trị"
                            className="inline-flex min-w-0 max-w-full items-center gap-1 overflow-x-auto rounded-lg border border-slate-200 bg-slate-100/80 p-1 shadow-2xs sm:gap-1.5 scrollbar-none"
                        >
                            {navItems.map((item) => {
                                const Icon = item.icon
                                return (
                                    <NavLink
                                        aria-label={item.label}
                                        className={({ isActive }) =>
                                            `inline-flex items-center gap-1.5 rounded-lg border px-2.5 py-1.5 text-xs font-semibold whitespace-nowrap transition-all duration-150 sm:gap-2 sm:px-3 sm:text-sm ${
                                                isActive
                                                    ? 'border-blue-200 bg-blue-50 text-blue-800 shadow-2xs'
                                                    : 'border-transparent text-slate-600 hover:bg-white/80 hover:text-slate-900'
                                            }`
                                        }
                                        key={item.to}
                                        to={item.to}
                                        title={item.label}
                                    >
                                        <Icon size={16} className="shrink-0" />
                                        <span className="hidden sm:inline md:hidden">{item.mobileLabel}</span>
                                        <span className="hidden md:inline">{item.label}</span>
                                    </NavLink>
                                )
                            })}
                        </nav>

                        <div className="hidden shrink-0 items-center gap-2 rounded-lg border border-slate-200 bg-white px-3.5 py-1.5 text-xs font-medium text-slate-600 shadow-2xs lg:flex">
                            <span className="relative flex size-2">
                                <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-emerald-400 opacity-75" />
                                <span className="relative inline-flex size-2 rounded-full bg-emerald-400" />
                            </span>
                            <span className="text-slate-600">Kiểm duyệt</span>
                            <span className="text-slate-300">•</span>
                            <span className="font-semibold text-slate-700">Quản lý hệ thống</span>
                        </div>
                    </div>
                </div>
                {isMobileActionsOpen && (
                    <div id="admin-mobile-actions" className="border-t border-slate-200 bg-white px-4 py-3 md:hidden sm:px-6">
                        <p className="truncate text-sm font-bold text-slate-900">{displayName}</p>
                        <p className="mt-0.5 text-xs font-medium text-blue-800">Quản trị viên</p>
                        <button className="mt-3 inline-flex items-center gap-1.5 rounded-lg border border-rose-200 px-3 py-2 text-xs font-semibold text-rose-700" onClick={() => { setIsMobileActionsOpen(false); void handleLogout() }} type="button">
                            <LogOut aria-hidden="true" size={13} /> Đăng xuất
                        </button>
                        <button className="ml-2 inline-flex items-center gap-1.5 rounded-lg border border-slate-200 px-3 py-2 text-xs font-semibold text-slate-700" onClick={() => { setIsMobileActionsOpen(false); navigate('/') }} type="button">
                            <ArrowLeft aria-hidden="true" size={13} /> Về website
                        </button>
                    </div>
                )}
            </header>

            {/* Main Content Area */}
            <main className="relative z-10 mx-auto flex-1 w-full max-w-[1200px] px-4 py-6 sm:px-6 lg:px-8">
                <Outlet />
            </main>
        </div>
    )
}
