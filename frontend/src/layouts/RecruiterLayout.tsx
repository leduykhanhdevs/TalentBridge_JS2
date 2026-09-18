import { useEffect, useState } from 'react'
import {
    ArrowLeft,
    Building2,
    CheckSquare,
    KeyRound,
    LogOut,
    Search,
    ShieldAlert,
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

    const displayName = user?.fullName
        ? user.fullName.replace(/\s*\((?:Admin|Recruiter|Candidate)\)\s*$/i, '')
        : ''

    const getInitials = (name: string) => {
        const parts = name.trim().split(/\s+/)
        if (!parts.length || !parts[0]) return 'HR'
        if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase()
        return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
    }

    if (!isRecruiter) {
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

                    <div className="mx-auto mb-4 grid size-12 place-items-center rounded-2xl border border-amber-200 bg-amber-50 text-amber-800 shadow-xs">
                        <ShieldAlert size={26} />
                    </div>
                    <h1 className="text-lg font-bold tracking-tight text-slate-900">Truy cập bị hạn chế</h1>
                    <p className="mt-2 text-xs font-semibold text-slate-600 leading-relaxed">
                        Bạn cần đăng nhập bằng tài khoản Nhà tuyển dụng (ROLE_RECRUITER) để truy cập Cổng tuyển dụng.
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
            <header className="sticky top-0 z-40 border-b border-emerald-800/60 bg-emerald-950 text-white shadow-xs">
                <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
                    <div className="flex items-center gap-3">
                        <div className="grid size-9 place-items-center rounded-xl bg-emerald-500 text-emerald-950 font-bold shadow-xs">
                            <Building2 size={20} />
                        </div>
                        <div>
                            <span className="text-sm sm:text-base font-bold tracking-tight">TalentBridge Recruiter</span>
                            <span className="bento-badge ml-2 border-emerald-400 bg-emerald-900 text-emerald-200 text-[10px]">
                                HR_PORTAL
                            </span>
                        </div>
                    </div>

                    <div className="flex items-center gap-2 sm:gap-3">
                        <NavLink
                            className="inline-flex items-center gap-1.5 rounded-xl border border-emerald-800/80 bg-emerald-900/80 px-3 py-1.5 text-xs font-semibold text-emerald-200 transition hover:border-emerald-700 hover:bg-emerald-900 hover:text-white shadow-2xs"
                            to="/"
                        >
                            <ArrowLeft size={14} />
                            <span>Về website</span>
                        </NavLink>

                        {/* Bento Recruiter User Capsule */}
                        <div className="flex items-center rounded-xl border border-emerald-800/80 bg-emerald-900/90 p-1 shadow-2xs">
                            <div className="flex items-center gap-2 pl-1.5 pr-2 py-0.5">
                                <span className="grid size-7 place-items-center rounded-lg bg-gradient-to-tr from-emerald-500 to-teal-500 text-[11px] font-bold text-white shadow-2xs">
                                    {getInitials(displayName)}
                                </span>
                                <div className="flex items-center gap-1.5">
                                    <span className="max-w-[120px] truncate text-xs font-semibold text-emerald-100 sm:max-w-none">
                                        {displayName}
                                    </span>
                                    <span className="hidden sm:inline-flex items-center px-1.5 py-0.5 rounded-md text-[10px] font-semibold border border-emerald-400/40 bg-emerald-400/10 text-emerald-300">
                                        Nhà tuyển dụng
                                    </span>
                                </div>
                            </div>

                            <div className="h-4 w-px bg-emerald-800 mx-0.5" />

                            <button
                                className="inline-flex items-center gap-1.5 rounded-lg px-2.5 py-1.5 text-xs font-medium text-emerald-200 hover:text-white hover:bg-emerald-800 transition-colors"
                                onClick={() => setIsChangePasswordOpen(true)}
                                title="Đổi mật khẩu"
                                type="button"
                            >
                                <KeyRound size={13} />
                                <span className="hidden md:inline">Đổi mật khẩu</span>
                            </button>

                            <button
                                className="inline-flex items-center gap-1.5 rounded-lg px-2.5 py-1.5 text-xs font-medium text-emerald-200 hover:text-rose-300 hover:bg-rose-500/20 transition-colors"
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

                {/* Subnav Navigation Bar */}
                <div className="border-t border-emerald-900/80 bg-emerald-950/90 px-4 sm:px-6 lg:px-8">
                    <nav className="mx-auto flex max-w-7xl space-x-2 sm:space-x-3 overflow-x-auto py-2">
                        {navItems.map((item) => {
                            const Icon = item.icon
                            return (
                                <NavLink
                                    className={({ isActive }) =>
                                        `inline-flex items-center gap-2 rounded-xl px-3.5 py-1.5 text-xs font-semibold transition whitespace-nowrap ${
                                            isActive
                                                ? 'bg-amber-400 text-slate-950 font-bold shadow-xs border border-amber-300'
                                                : 'border border-transparent text-emerald-200 hover:bg-emerald-900/60 hover:text-white'
                                        }`
                                    }
                                    key={item.to}
                                    to={item.to}
                                >
                                    <Icon size={14} />
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
