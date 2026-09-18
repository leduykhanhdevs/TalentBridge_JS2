import { useEffect, useState } from 'react'
import {
    ArrowLeft,
    Building2,
    LayoutDashboard,
    LogOut,
    ShieldAlert,
    UserCheck,
    Users,
} from 'lucide-react'
import { NavLink, Outlet, useNavigate } from 'react-router'
import { getStoredUser, isAuthenticated } from '../features/auth/tokenStorage'
import { logoutUser } from '../features/auth/authApi'
import type { UserResponse } from '../features/auth/authTypes'

export function AdminLayout() {
    const navigate = useNavigate()
    const [user, setUser] = useState<UserResponse | null>(() => getStoredUser())
    const [authenticated, setAuthenticated] = useState<boolean>(() => isAuthenticated())

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
            icon: Users,
            desc: 'Danh sách, tìm kiếm, khóa/mở',
        },
        {
            to: '/admin/recruiters',
            label: 'Nhà tuyển dụng (HR)',
            icon: UserCheck,
            desc: 'Quản lý HR & công ty trực thuộc',
        },
        {
            to: '/admin/companies',
            label: 'Duyệt doanh nghiệp',
            icon: Building2,
            desc: 'Phê duyệt & lý do từ chối',
        },
    ]

    return (
        <div className="relative flex min-h-screen flex-col bg-bento-canvas text-slate-900 selection:bg-indigo-500 selection:text-white">
            {/* Ambient Background Glows */}
            <div className="pointer-events-none fixed -top-40 right-10 size-[32rem] rounded-full bg-indigo-200/30 blur-3xl" />
            <div className="pointer-events-none fixed top-72 -left-20 size-[28rem] rounded-full bg-amber-200/20 blur-3xl" />

            {/* Header */}
            <header className="sticky top-0 z-40 border-b border-slate-800 bg-slate-900/95 backdrop-blur-md text-white shadow-sm">
                <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
                    <div className="flex items-center gap-3">
                        <div className="grid size-10 place-items-center rounded-xl bg-gradient-to-br from-indigo-500 via-indigo-600 to-violet-700 text-white font-black shadow-md shadow-indigo-950/50 border border-indigo-400/30">
                            <LayoutDashboard size={20} />
                        </div>
                        <div>
                            <div className="flex items-center gap-2">
                                <span className="text-base sm:text-lg font-black tracking-tight text-white">TalentBridge Admin</span>
                                <span className="inline-flex items-center gap-1 rounded-full border border-indigo-400/40 bg-indigo-950/80 px-2 py-0.5 text-[10px] font-bold uppercase tracking-wider text-indigo-200 shadow-2xs">
                                    <span className="size-1.5 rounded-full bg-emerald-400 animate-pulse" />
                                    SYS_ADMIN
                                </span>
                            </div>
                            <p className="hidden sm:block text-[11px] font-medium text-slate-400">
                                Trung tâm Kiểm soát Hệ sinh thái Tuyển dụng & Xác thực Doanh nghiệp
                            </p>
                        </div>
                    </div>

                    <div className="flex items-center gap-2 sm:gap-3">
                        <NavLink
                            className="inline-flex items-center gap-1.5 rounded-xl border border-slate-700/80 bg-slate-800/80 px-3 py-1.5 text-xs font-bold text-slate-300 transition hover:border-slate-600 hover:bg-slate-800 hover:text-white shadow-2xs"
                            to="/"
                        >
                            <ArrowLeft size={14} />
                            <span>Về website</span>
                        </NavLink>

                        {/* Bento Admin User Capsule */}
                        <div className="flex items-center rounded-xl border border-slate-700/80 bg-slate-800/90 p-1 shadow-2xs">
                            <div className="flex items-center gap-2 pl-1.5 pr-2 py-0.5">
                                <span className="grid size-7 place-items-center rounded-lg bg-gradient-to-tr from-amber-400 to-amber-600 text-[11px] font-black text-slate-950 shadow-2xs">
                                    {getInitials(displayName)}
                                </span>
                                <div className="flex items-center gap-1.5">
                                    <span className="max-w-[130px] truncate text-xs font-bold text-slate-100 sm:max-w-none">
                                        {displayName}
                                    </span>
                                    <span className="hidden sm:inline-flex items-center px-1.5 py-0.5 rounded-md text-[10px] font-bold border border-amber-400/40 bg-amber-400/15 text-amber-300">
                                        Quản trị viên
                                    </span>
                                </div>
                            </div>

                            <div className="h-4 w-px bg-slate-700 mx-0.5" />

                            <button
                                className="inline-flex items-center gap-1.5 rounded-lg px-2.5 py-1.5 text-xs font-semibold text-slate-300 hover:text-rose-400 hover:bg-rose-500/10 transition-colors"
                                onClick={handleLogout}
                                title="Đăng xuất"
                                type="button"
                            >
                                <LogOut size={13} />
                                <span className="hidden sm:inline">Đăng xuất</span>
                            </button>
                        </div>
                    </div>
                </div>

                {/* Subnav Bento Navigation Dock */}
                <div className="border-t border-slate-800/80 bg-slate-950/90 px-4 sm:px-6 lg:px-8 py-2">
                    <div className="mx-auto flex max-w-7xl items-center justify-between">
                        <nav className="flex items-center space-x-1 sm:space-x-2 overflow-x-auto p-1 rounded-2xl bg-slate-900/90 border border-slate-800/90">
                            {navItems.map((item) => {
                                const Icon = item.icon
                                return (
                                    <NavLink
                                        className={({ isActive }) =>
                                            `inline-flex items-center gap-2 rounded-xl px-3.5 py-1.5 text-xs sm:text-sm font-bold transition-all whitespace-nowrap ${
                                                isActive
                                                    ? 'bg-gradient-to-r from-indigo-600 to-indigo-700 text-white shadow-sm shadow-indigo-950/60 border border-indigo-400/30'
                                                    : 'border border-transparent text-slate-400 hover:text-slate-200 hover:bg-slate-800/70'
                                            }`
                                        }
                                        key={item.to}
                                        to={item.to}
                                    >
                                        <Icon size={16} />
                                        <span>{item.label}</span>
                                    </NavLink>
                                )
                            })}
                        </nav>

                        <div className="hidden lg:flex items-center gap-2 text-xs font-semibold text-slate-400">
                            <span className="size-2 rounded-full bg-emerald-400 animate-pulse" />
                            <span>CSDL 3NF • Sẵn sàng giám sát</span>
                        </div>
                    </div>
                </div>
            </header>

            {/* Main Content Area */}
            <main className="relative z-10 mx-auto flex-1 w-full max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
                <Outlet />
            </main>
        </div>
    )
}
