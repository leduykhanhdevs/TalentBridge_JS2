import { useEffect, useState } from 'react'
import {
    ArrowLeft,
    Briefcase,
    Building2,
    CheckSquare,
    KeyRound,
    LogOut,
    Menu,
    Search,
    ShieldAlert,
    UserCircle,
    X,
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
            mobileLabel: 'Hồ sơ',
            icon: UserCircle,
            desc: 'Thông tin cá nhân & chức vụ',
        },
        {
            to: '/recruiter/jobs',
            label: 'Tin tuyển dụng',
            mobileLabel: 'Tin',
            icon: Briefcase,
            desc: 'Quản lý bài đăng tuyển dụng',
        },
        {
            to: '/recruiter/company',
            label: 'Doanh nghiệp của tôi',
            mobileLabel: 'Công ty',
            icon: Building2,
            desc: 'Thông tin công ty hoặc tạo mới',
        },
        {
            to: '/recruiter/join-company',
            label: 'Tìm & Xin gia nhập',
            mobileLabel: 'Gia nhập',
            icon: Search,
            desc: 'Tìm kiếm công ty & nộp yêu cầu',
        },
        {
            to: '/recruiter/peer-approval',
            label: 'Duyệt thành viên nội bộ',
            mobileLabel: 'Duyệt',
            icon: CheckSquare,
            desc: 'Xét duyệt HR gia nhập công ty',
        },
    ]

    return (
        <div className="relative flex min-h-screen flex-col overflow-x-clip bg-bento-canvas text-slate-900 selection:bg-emerald-600 selection:text-white">
            {/* Ambient Background Glows */}
            <div className="pointer-events-none fixed -top-40 right-10 size-[32rem] rounded-full bg-emerald-100/40 blur-3xl" />
            <div className="pointer-events-none fixed top-72 -left-20 size-[28rem] rounded-full bg-teal-100/30 blur-3xl" />

            {/* Header */}
            <header className="sticky top-0 z-40 border-b border-slate-200/80 bg-white/95 backdrop-blur-md text-slate-900 shadow-2xs">
                <div className="mx-auto flex h-16 max-w-7xl items-center justify-between gap-2 px-4 sm:px-6 lg:px-8">
                    <div className="flex items-center gap-3">
                        <div className="grid size-10 place-items-center rounded-xl bg-gradient-to-tr from-emerald-600 to-teal-500 text-white font-bold shadow-xs border border-emerald-400/20">
                            <Building2 size={20} />
                        </div>
                        <div>
                            <div className="flex items-center gap-2">
                                <span className="text-sm font-bold tracking-tight text-slate-900 sm:text-lg">
                                    TalentBridge Recruiter
                                </span>
                                <span className="hidden items-center gap-1 rounded-full border border-emerald-200 bg-emerald-50 px-2 py-0.5 text-[10px] font-bold uppercase tracking-wider text-emerald-700 shadow-2xs md:inline-flex">
                                    <span className="size-1.5 rounded-full bg-emerald-500 animate-pulse" />
                                    HR_PORTAL
                                </span>
                            </div>
                            <p className="hidden sm:block text-[11px] font-medium text-slate-500">
                                Cổng Quản trị Tuyển dụng & Sàng lọc Ứng viên ATS
                            </p>
                        </div>
                    </div>

                    <div className="flex items-center gap-2 sm:gap-3">
                        <NavLink
                            className="hidden items-center gap-1.5 rounded-xl border border-slate-200/80 bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 shadow-2xs transition hover:border-slate-300 hover:bg-slate-50 hover:text-slate-900 md:inline-flex"
                            to="/"
                        >
                            <ArrowLeft size={14} />
                            <span>Về website</span>
                        </NavLink>

                        {/* Bento Recruiter User Capsule */}
                        <div className="hidden items-center rounded-xl border border-slate-200/80 bg-white p-1 shadow-2xs md:flex">
                            <div className="flex items-center gap-2 pl-1.5 pr-2 py-0.5">
                                <span className="grid size-7 place-items-center rounded-lg bg-gradient-to-tr from-emerald-600 to-teal-500 text-[11px] font-bold text-white shadow-2xs">
                                    {getInitials(displayName)}
                                </span>
                                <div className="flex items-center gap-1.5">
                                    <span className="max-w-[120px] truncate text-xs font-bold text-slate-800 sm:max-w-none">
                                        {displayName}
                                    </span>
                                    <span className="hidden sm:inline-flex items-center px-1.5 py-0.5 rounded-md text-[10px] font-semibold border border-emerald-200/80 bg-emerald-50 text-emerald-700">
                                        Nhà tuyển dụng
                                    </span>
                                </div>
                            </div>

                            <div className="h-4 w-px bg-slate-200/80 mx-0.5" />

                            <button
                                className="inline-flex items-center gap-1.5 rounded-lg px-2.5 py-1.5 text-xs font-medium text-slate-600 hover:text-emerald-700 hover:bg-emerald-50/70 transition-colors"
                                onClick={() => setIsChangePasswordOpen(true)}
                                title="Đổi mật khẩu"
                                type="button"
                            >
                                <KeyRound size={13} />
                                <span className="hidden md:inline">Đổi mật khẩu</span>
                            </button>

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

                        <button
                            aria-controls="recruiter-mobile-actions"
                            aria-expanded={isMobileActionsOpen}
                            aria-label={isMobileActionsOpen ? 'Đóng menu tài khoản' : 'Mở menu tài khoản'}
                            className="inline-flex size-10 shrink-0 items-center justify-center rounded-xl border border-slate-200 bg-white text-slate-700 shadow-2xs md:hidden"
                            onClick={() => setIsMobileActionsOpen((open) => !open)}
                            type="button"
                        >
                            {isMobileActionsOpen ? <X aria-hidden="true" size={18} /> : <Menu aria-hidden="true" size={18} />}
                        </button>
                    </div>
                </div>

                {/* Subnav Bento Navigation Dock */}
                <div className="border-t border-slate-200/60 bg-slate-50/70 backdrop-blur-xs px-4 sm:px-6 lg:px-8 py-2">
                    <div className="mx-auto flex min-w-0 max-w-7xl items-center justify-between gap-4">
                        <nav
                            aria-label="Điều hướng nhà tuyển dụng"
                            className="inline-flex min-w-0 max-w-full items-center gap-1 overflow-x-auto rounded-xl border border-slate-200/60 bg-slate-100/80 p-1 shadow-2xs backdrop-blur-xs sm:gap-1.5 scrollbar-none"
                        >
                            {navItems.map((item) => {
                                const Icon = item.icon
                                return (
                                    <NavLink
                                        aria-label={item.label}
                                        className={({ isActive }) =>
                                            `inline-flex items-center gap-1.5 rounded-lg border px-2.5 py-1.5 text-xs font-semibold whitespace-nowrap transition-all duration-150 sm:gap-2 sm:px-3 sm:text-sm ${
                                                isActive
                                                    ? 'bg-white text-emerald-700 shadow-2xs border-slate-200/60'
                                                    : 'border-transparent text-slate-600 hover:text-slate-900 hover:bg-white/60'
                                            }`
                                        }
                                        key={item.to}
                                        to={item.to}
                                        title={item.label}
                                    >
                                        <Icon size={15} className="shrink-0" />
                                        <span className="hidden sm:inline md:hidden">{item.mobileLabel}</span>
                                        <span className="hidden md:inline">{item.label}</span>
                                    </NavLink>
                                )
                            })}
                        </nav>

                        <div className="hidden lg:flex items-center gap-2 rounded-xl border border-slate-200/60 bg-white/80 px-3.5 py-1.5 text-xs font-medium text-slate-600 shadow-2xs backdrop-blur-xs shrink-0">
                            <span className="relative flex size-2">
                                <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-emerald-400 opacity-75" />
                                <span className="relative inline-flex size-2 rounded-full bg-emerald-500" />
                            </span>
                            <span className="text-slate-500">ATS Pipeline</span>
                            <span className="text-slate-300">•</span>
                            <span className="font-semibold text-emerald-700">Sẵn sàng tiếp nhận hồ sơ</span>
                        </div>
                    </div>
                </div>
                {isMobileActionsOpen && (
                    <div id="recruiter-mobile-actions" className="border-t border-slate-200 bg-white px-4 py-3 md:hidden sm:px-6">
                        <p className="truncate text-sm font-bold text-slate-900">{displayName}</p>
                        <p className="mt-0.5 text-xs font-medium text-emerald-700">Nhà tuyển dụng</p>
                        <div className="mt-3 flex flex-wrap gap-2">
                            <button className="inline-flex items-center gap-1.5 rounded-lg border border-slate-200 px-3 py-2 text-xs font-semibold text-slate-700" onClick={() => { setIsChangePasswordOpen(true); setIsMobileActionsOpen(false) }} type="button">
                                <KeyRound aria-hidden="true" size={13} /> Đổi mật khẩu
                            </button>
                            <button className="inline-flex items-center gap-1.5 rounded-lg border border-rose-200 px-3 py-2 text-xs font-semibold text-rose-700" onClick={() => { setIsMobileActionsOpen(false); void handleLogout() }} type="button">
                                <LogOut aria-hidden="true" size={13} /> Đăng xuất
                            </button>
                            <NavLink className="inline-flex items-center gap-1.5 rounded-lg border border-slate-200 px-3 py-2 text-xs font-semibold text-slate-700" onClick={() => setIsMobileActionsOpen(false)} to="/">
                                <ArrowLeft aria-hidden="true" size={13} /> Về website
                            </NavLink>
                        </div>
                    </div>
                )}
            </header>

            {/* Main Content Area */}
            <main className="relative z-10 mx-auto flex-1 w-full max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
                <Outlet />
            </main>

            <ChangePasswordModal
                isOpen={isChangePasswordOpen}
                onClose={() => setIsChangePasswordOpen(false)}
            />
        </div>
    )
}
