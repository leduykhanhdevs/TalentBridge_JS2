import { useState } from 'react'
import { ArrowLeft, BriefcaseBusiness, KeyRound, LogOut, Menu, X, type LucideIcon } from 'lucide-react'
import { NavLink, Outlet, useNavigate } from 'react-router'
import { ChangePasswordModal } from '../features/auth/components/ChangePasswordModal'
import { logoutUser } from '../features/auth/authApi'
import type { UserResponse } from '../features/auth/authTypes'

export interface WorkspaceNavigationItem {
    to: string
    label: string
    icon: LucideIcon
}

interface WorkspaceLayoutFrameProps {
    sectionLabel: string
    roleLabel: string
    user: UserResponse
    navigationItems: WorkspaceNavigationItem[]
    showWorkspaceLabel?: boolean
    showRoleBadge?: boolean
    showRoleCaption?: boolean
}

function initialsFor(name: string) {
    const parts = name.trim().split(/\s+/).filter(Boolean)
    if (parts.length === 0) return 'TB'
    if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase()
    return `${parts[0][0]}${parts[parts.length - 1][0]}`.toUpperCase()
}

export function WorkspaceLayoutFrame({
    sectionLabel,
    roleLabel,
    user,
    navigationItems,
    showWorkspaceLabel = true,
    showRoleBadge = true,
    showRoleCaption = true,
}: WorkspaceLayoutFrameProps) {
    const navigate = useNavigate()
    const [isMenuOpen, setIsMenuOpen] = useState(false)
    const [isChangePasswordOpen, setIsChangePasswordOpen] = useState(false)
    const displayName = user.fullName?.replace(/\s*\((?:Admin|Recruiter|Candidate)\)\s*$/i, '') ?? ''

    function handleLogout() {
        void logoutUser()
        navigate('/login')
    }

    return (
        <div className="min-h-screen bg-[#F5F7FB] text-[#172033]">
            <header className="sticky top-0 z-40 h-16 border-b border-slate-200 bg-white md:h-[84px]">
                <div className="mx-auto grid h-full w-full max-w-[1440px] grid-cols-[1fr_auto_1fr] items-center gap-3 px-4 sm:px-6 lg:px-8">
                    <NavLink aria-label="TalentBridge — Trang chủ" className="flex w-fit shrink-0 items-center gap-2.5" end to="/">
                        <span className="grid size-9 place-items-center rounded-lg bg-[#1D4ED8] text-white">
                            <BriefcaseBusiness aria-hidden="true" size={18} />
                        </span>
                        <span className="text-base font-bold tracking-tight text-[#172033]">TalentBridge</span>
                    </NavLink>

                    {showWorkspaceLabel && <p className="hidden text-sm font-semibold text-slate-700 sm:block">Không gian làm việc</p>}

                    <div className="flex justify-end">
                        {showRoleBadge && <span className="hidden items-center rounded-md border border-slate-200 bg-slate-50 px-3 py-1.5 text-xs font-semibold text-slate-700 sm:inline-flex">{roleLabel}</span>}
                        <button
                            aria-controls="workspace-navigation"
                            aria-expanded={isMenuOpen}
                            aria-label={isMenuOpen ? 'Đóng điều hướng' : 'Mở điều hướng'}
                            className="grid size-10 shrink-0 place-items-center rounded-lg border border-slate-200 bg-white text-slate-700 md:hidden"
                            onClick={() => setIsMenuOpen((open) => !open)}
                            type="button"
                        >
                            {isMenuOpen ? <X aria-hidden="true" size={19} /> : <Menu aria-hidden="true" size={19} />}
                        </button>
                    </div>
                </div>
            </header>

            <div className="relative min-h-[calc(100dvh-128px)] md:grid md:grid-cols-[232px_minmax(0,1fr)]">
                {isMenuOpen && <button aria-label="Đóng điều hướng" className="fixed inset-x-0 bottom-0 top-16 z-20 bg-slate-950/20 md:hidden" onClick={() => setIsMenuOpen(false)} type="button" />}

                <aside
                    id="workspace-navigation"
                    className={`${isMenuOpen ? 'fixed inset-y-16 left-0 z-30 flex w-[min(86vw,288px)] shadow-xl' : 'hidden'} flex-col border-r border-slate-200 bg-white px-3 py-5 md:sticky md:top-[84px] md:flex md:h-[calc(100dvh-148px)] md:w-[232px] md:self-start md:shadow-none`}
                >
                    <p className="px-3 text-[11px] font-bold uppercase tracking-[0.12em] text-slate-500">{sectionLabel}</p>
                    <nav aria-label={`Điều hướng ${sectionLabel.toLowerCase()}`} className="mt-4 grid gap-1">
                        {navigationItems.map(({ to, label, icon: Icon }) => (
                            <NavLink
                                aria-label={label}
                                className={({ isActive }) => `flex min-h-10 items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors ${isActive ? 'bg-[#EFF4FF] text-[#1D4ED8]' : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'}`}
                                key={to}
                                onClick={() => setIsMenuOpen(false)}
                                title={label}
                                to={to}
                            >
                                <Icon aria-hidden="true" className="shrink-0" size={16} />
                                <span>{label}</span>
                            </NavLink>
                        ))}
                    </nav>

                    <div className="mt-auto border-t border-slate-200 px-3 pt-4">
                        <div className="mb-3 flex min-w-0 items-center gap-2.5">
                            <span aria-hidden="true" className="grid size-8 shrink-0 place-items-center rounded-lg bg-[#EFF4FF] text-[11px] font-bold text-[#1D4ED8]">{initialsFor(displayName)}</span>
                            <div className="min-w-0">
                                <p className="truncate text-xs font-semibold text-slate-800" title={displayName}>{displayName}</p>
                                {showRoleCaption && <p className="mt-0.5 text-[11px] text-slate-500">{roleLabel}</p>}
                            </div>
                        </div>
                        <div className="grid grid-cols-2 gap-2">
                            <button aria-label="Đổi mật khẩu" className="grid size-9 place-items-center rounded-lg border border-slate-200 text-slate-600 transition hover:bg-slate-50 hover:text-[#1D4ED8]" onClick={() => setIsChangePasswordOpen(true)} title="Đổi mật khẩu" type="button">
                                <KeyRound aria-hidden="true" size={15} />
                            </button>
                            <button aria-label="Đăng xuất" className="grid size-9 place-items-center rounded-lg border border-slate-200 text-slate-600 transition hover:bg-slate-50 hover:text-[#1D4ED8]" onClick={() => void handleLogout()} title="Đăng xuất" type="button">
                                <LogOut aria-hidden="true" size={15} />
                            </button>
                        </div>
                        <NavLink className="mt-4 inline-flex items-center gap-2 text-xs font-medium text-slate-500 hover:text-[#1D4ED8]" to="/">
                            <ArrowLeft aria-hidden="true" size={14} />Về TalentBridge
                        </NavLink>
                        <p className="mt-3 flex items-center gap-2 text-[11px] text-slate-500">
                            <span className="size-1.5 rounded-full bg-emerald-500" />Phiên làm việc an toàn
                        </p>
                    </div>
                </aside>

                <main className="min-w-0 px-4 py-7 sm:px-6 md:px-8 lg:px-10 lg:py-9">
                    <div className="mx-auto w-full max-w-[1176px]">
                        <Outlet />
                    </div>
                </main>
            </div>

            <footer className="flex min-h-16 items-center justify-between gap-3 border-t border-slate-200 bg-white px-5 text-xs text-slate-600 sm:px-8">
                <span>TalentBridge · Kết nối nghề nghiệp có trách nhiệm</span>
                <span className="hidden font-semibold text-slate-500 sm:inline">TalentBridge ATS</span>
            </footer>

            <ChangePasswordModal isOpen={isChangePasswordOpen} onClose={() => setIsChangePasswordOpen(false)} />
        </div>
    )
}
