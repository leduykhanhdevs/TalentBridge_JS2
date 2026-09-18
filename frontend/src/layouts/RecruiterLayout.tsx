import { useEffect, useState } from 'react'
import {
    ArrowLeft,
    Building2,
    CheckSquare,
    KeyRound,
    LogOut,
    Search,
    ShieldAlert,
    UserCheck,
    UserCircle,
} from 'lucide-react'
import { NavLink, Outlet, useNavigate } from 'react-router'
import { getStoredUser, isAuthenticated } from '../features/auth/tokenStorage'
import { logoutUser } from '../features/auth/authApi'
import type { UserResponse } from '../features/auth/authTypes'
import { ChangePasswordModal } from '../features/auth/components/ChangePasswordModal'

export function RecruiterLayout() {
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

    const isRecruiter = authenticated && user?.roles?.includes('ROLE_RECRUITER')

    if (!isRecruiter) {
        return (
            <div className="flex min-h-screen items-center justify-center bg-slate-100 p-4">
                <div className="w-full max-w-md rounded-2xl border border-amber-200 bg-white p-6 text-center shadow-lg">
                    <div className="mx-auto mb-4 grid size-12 place-items-center rounded-full bg-amber-100 text-amber-600">
                        <ShieldAlert size={28} />
                    </div>
                    <h1 className="text-xl font-bold text-slate-900">Truy cập bị hạn chế</h1>
                    <p className="mt-2 text-sm text-slate-600">
                        Bạn cần đăng nhập bằng tài khoản Nhà tuyển dụng (ROLE_RECRUITER) để truy cập Cổng tuyển dụng.
                    </p>
                    <div className="mt-6 flex flex-col gap-2 sm:flex-row sm:justify-center">
                        <button
                            className="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-semibold text-white shadow hover:bg-emerald-700"
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
            to: '/recruiter/profile',
            label: 'Hồ sơ HR',
            icon: UserCircle,
            desc: 'Thông tin cá nhân & chức vụ',
        },
        {
            to: '/recruiter/company',
            label: 'Doanh nghiệp của tôi',
            icon: Building2,
            desc: 'Thông tin công ty hoặc tạo mới',
        },
        {
            to: '/recruiter/join-company',
            label: 'Tìm & Xin gia nhập',
            icon: Search,
            desc: 'Tìm kiếm công ty & nộp yêu cầu',
        },
        {
            to: '/recruiter/peer-approval',
            label: 'Duyệt thành viên nội bộ',
            icon: CheckSquare,
            desc: 'Xét duyệt HR gia nhập công ty',
        },
    ]

    return (
        <div className="flex min-h-screen flex-col bg-slate-50 text-slate-900">
            {/* Header */}
            <header className="sticky top-0 z-40 border-b border-emerald-800/20 bg-emerald-950 text-white">
                <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
                    <div className="flex items-center gap-3">
                        <div className="grid size-9 place-items-center rounded-lg bg-emerald-500 text-white font-bold">
                            <Building2 size={20} />
                        </div>
                        <div>
                            <span className="text-base font-bold tracking-tight">TalentBridge Recruiter</span>
                            <span className="ml-2 inline-flex items-center rounded-md bg-emerald-900 px-2 py-0.5 text-xs font-medium text-emerald-300 border border-emerald-700/50">
                                Portal
                            </span>
                        </div>
                    </div>

                    <div className="flex items-center gap-4">
                        <NavLink
                            className="inline-flex items-center gap-1.5 text-xs font-medium text-emerald-200 transition hover:text-white"
                            to="/"
                        >
                            <ArrowLeft size={14} />
                            <span>Về website</span>
                        </NavLink>

                        <div className="hidden h-4 w-px bg-emerald-800 sm:block" />

                        <div className="flex items-center gap-2 text-xs">
                            <UserCheck className="text-emerald-400" size={14} />
                            <span className="font-semibold text-emerald-100">{user?.fullName}</span>
                            <span className="rounded bg-emerald-500/20 px-1.5 py-0.5 text-[10px] font-semibold text-emerald-300 border border-emerald-500/40">
                                RECRUITER
                            </span>
                        </div>

                        <button
                            className="inline-flex items-center gap-1 rounded-lg border border-emerald-800 bg-emerald-900/60 px-2.5 py-1.5 text-xs font-medium text-emerald-200 transition hover:bg-emerald-800 hover:text-white"
                            onClick={() => setIsChangePasswordOpen(true)}
                            title="Đổi mật khẩu"
                            type="button"
                        >
                            <KeyRound size={14} />
                            <span className="hidden sm:inline">Đổi MK</span>
                        </button>

                        <button
                            className="inline-flex items-center gap-1 rounded-lg border border-emerald-800 bg-emerald-900/60 px-2.5 py-1.5 text-xs font-medium text-emerald-200 transition hover:bg-emerald-800 hover:text-white"
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
                <div className="border-t border-emerald-900/80 bg-emerald-950/80 px-4 sm:px-6 lg:px-8">
                    <nav className="mx-auto flex max-w-7xl space-x-1 sm:space-x-4 overflow-x-auto py-2">
                        {navItems.map((item) => {
                            const Icon = item.icon
                            return (
                                <NavLink
                                    className={({ isActive }) =>
                                        `inline-flex items-center gap-2 rounded-lg px-3 py-2 text-xs sm:text-sm font-medium transition whitespace-nowrap ${
                                            isActive
                                                ? 'bg-emerald-600 text-white shadow-sm'
                                                : 'text-emerald-200 hover:bg-emerald-900 hover:text-white'
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

            <ChangePasswordModal
                isOpen={isChangePasswordOpen}
                onClose={() => setIsChangePasswordOpen(false)}
            />
        </div>
    )
}
