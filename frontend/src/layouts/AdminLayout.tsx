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

    if (!isAdmin) {
        return (
            <div className="flex min-h-screen items-center justify-center bg-slate-100 p-4">
                <div className="w-full max-w-md rounded-2xl border border-red-200 bg-white p-6 text-center shadow-lg">
                    <div className="mx-auto mb-4 grid size-12 place-items-center rounded-full bg-red-100 text-red-600">
                        <ShieldAlert size={28} />
                    </div>
                    <h1 className="text-xl font-bold text-slate-900">Truy cập bị từ chối</h1>
                    <p className="mt-2 text-sm text-slate-600">
                        Bạn cần đăng nhập bằng tài khoản Quản trị viên (ROLE_ADMIN) để truy cập cổng quản trị.
                    </p>
                    <div className="mt-6 flex flex-col gap-2 sm:flex-row sm:justify-center">
                        <button
                            className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white shadow hover:bg-indigo-700"
                            onClick={() => navigate('/login')}
                            type="button"
                        >
                            Đăng nhập lại
                        </button>
                        <button
                            className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50"
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
        <div className="flex min-h-screen flex-col bg-slate-50 text-slate-900">
            {/* Header */}
            <header className="sticky top-0 z-40 border-b border-slate-200 bg-slate-900 text-white">
                <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
                    <div className="flex items-center gap-3">
                        <div className="grid size-9 place-items-center rounded-lg bg-indigo-500 text-white font-bold">
                            <LayoutDashboard size={20} />
                        </div>
                        <div>
                            <span className="text-base font-bold tracking-tight">TalentBridge Admin</span>
                            <span className="ml-2 inline-flex items-center rounded-md bg-indigo-950 px-2 py-0.5 text-xs font-medium text-indigo-300 border border-indigo-700/50">
                                Portal
                            </span>
                        </div>
                    </div>

                    <div className="flex items-center gap-4">
                        <NavLink
                            className="inline-flex items-center gap-1.5 text-xs font-medium text-slate-300 transition hover:text-white"
                            to="/"
                        >
                            <ArrowLeft size={14} />
                            <span>Về website</span>
                        </NavLink>

                        <div className="hidden h-4 w-px bg-slate-700 sm:block" />

                        <div className="flex items-center gap-2 text-xs">
                            <span className="font-semibold text-slate-200">{user?.fullName}</span>
                            <span className="rounded bg-emerald-500/20 px-1.5 py-0.5 text-[10px] font-semibold text-emerald-300 border border-emerald-500/40">
                                ADMIN
                            </span>
                        </div>

                        <button
                            className="inline-flex items-center gap-1 rounded-lg border border-slate-700 bg-slate-800 px-2.5 py-1.5 text-xs font-medium text-slate-300 transition hover:bg-slate-700 hover:text-white"
                            onClick={handleLogout}
                            title="Đăng xuất"
                            type="button"
                        >
                            <LogOut size={14} />
                            <span className="hidden sm:inline">Đăng xuất</span>
                        </button>
                    </div>
                </div>

                {/* Subnav Navigation Bar */}
                <div className="border-t border-slate-800 bg-slate-950/60 px-4 sm:px-6 lg:px-8">
                    <nav className="mx-auto flex max-w-7xl space-x-1 sm:space-x-4 overflow-x-auto py-2">
                        {navItems.map((item) => {
                            const Icon = item.icon
                            return (
                                <NavLink
                                    className={({ isActive }) =>
                                        `inline-flex items-center gap-2 rounded-lg px-3 py-2 text-xs sm:text-sm font-medium transition whitespace-nowrap ${
                                            isActive
                                                ? 'bg-indigo-600 text-white shadow-sm'
                                                : 'text-slate-300 hover:bg-slate-800 hover:text-white'
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
                </div>
            </header>

            {/* Main Content Area */}
            <main className="mx-auto flex-1 w-full max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
                <Outlet />
            </main>
        </div>
    )
}
