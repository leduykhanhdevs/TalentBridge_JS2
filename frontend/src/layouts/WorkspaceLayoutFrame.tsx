import { useState } from 'react'
import { ArrowLeft, BriefcaseBusiness, Home, KeyRound, LogOut, Menu, MessageCircle, Search, X, type LucideIcon } from 'lucide-react'
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
}

function initialsFor(name: string) {
    const parts = name.trim().split(/\s+/).filter(Boolean)
    if (parts.length === 0) return 'TB'
    if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase()
    return `${parts[0][0]}${parts[parts.length - 1][0]}`.toUpperCase()
}

export function WorkspaceLayoutFrame({ sectionLabel, roleLabel, user, navigationItems }: WorkspaceLayoutFrameProps) {
    const navigate = useNavigate()
    const [isMenuOpen, setIsMenuOpen] = useState(false)
    const [isChangePasswordOpen, setIsChangePasswordOpen] = useState(false)
    const displayName = user.fullName?.replace(/\s*\((?:Admin|Recruiter|Candidate)\)\s*$/i, '') ?? ''

    async function handleLogout() {
        await logoutUser()
        navigate('/login')
    }

    return (
        <div className="min-h-screen bg-bento-canvas text-slate-900">
            <header className="sticky top-0 z-40 h-[68px] border-b border-slate-200 bg-white md:h-[72px]">
                <div className="mx-auto flex h-full w-full max-w-[1200px] items-center justify-between gap-4 px-4 sm:px-6 xl:px-0">
                    <NavLink aria-label="TalentBridge — Trang chủ" className="flex shrink-0 items-center gap-3" end to="/">
                        <span className="grid size-10 place-items-center rounded-xl bg-blue-700 text-white">
                            <BriefcaseBusiness aria-hidden="true" size={20} />
                        </span>
                        <span className="text-base font-bold tracking-tight text-slate-900">TalentBridge</span>
                    </NavLink>

                    <nav aria-label="Điều hướng chính" className="hidden items-center gap-1 rounded-xl border border-slate-200 bg-slate-50 p-1 lg:flex">
                        {[
                            { to: '/', label: 'Trang chủ', icon: Home, end: true },
                            { to: '/jobs', label: 'Việc làm', icon: Search },
                            { to: '/assistant', label: 'Trợ lý', icon: MessageCircle },
                        ].map(({ to, label, icon: Icon, end }) => (
                            <NavLink
                                className={({ isActive }) => `inline-flex h-10 items-center gap-1.5 whitespace-nowrap rounded-lg border px-3 text-[13px] font-semibold transition-colors ${isActive ? 'border-slate-200 bg-white text-slate-900 shadow-xs' : 'border-transparent text-slate-600 hover:border-slate-200 hover:bg-white hover:text-blue-700'}`}
                                end={end}
                                key={to}
                                to={to}
                            >
                                <Icon aria-hidden="true" size={15} />{label}
                            </NavLink>
                        ))}
                    </nav>

                    <div className="hidden min-w-0 items-center gap-2 rounded-xl border border-slate-200 bg-white px-2 py-1.5 md:flex">
                        <span aria-hidden="true" className="grid size-9 shrink-0 place-items-center rounded-lg bg-blue-700 text-xs font-bold text-white">{initialsFor(displayName)}</span>
                        <span className="max-w-[150px] truncate text-sm font-semibold text-slate-800">{displayName}</span>
                        <span className="inline-flex shrink-0 rounded-md border border-slate-200 bg-slate-50 px-2 py-1 text-[11px] font-semibold text-slate-700">{roleLabel}</span>
                        <span aria-hidden="true" className="mx-1 h-6 w-px bg-slate-200" />
                        <button aria-label="Đổi mật khẩu" className="grid size-9 shrink-0 place-items-center rounded-lg text-slate-600 hover:bg-slate-100 hover:text-blue-700" onClick={() => setIsChangePasswordOpen(true)} title="Đổi mật khẩu" type="button"><KeyRound aria-hidden="true" size={16} /></button>
                        <button aria-label="Đăng xuất" className="grid size-9 shrink-0 place-items-center rounded-lg text-slate-600 hover:bg-slate-100 hover:text-blue-700" onClick={() => void handleLogout()} title="Đăng xuất" type="button"><LogOut aria-hidden="true" size={16} /></button>
                    </div>

                    <button aria-controls="workspace-navigation" aria-expanded={isMenuOpen} aria-label={isMenuOpen ? 'Đóng điều hướng' : 'Mở điều hướng'} className="grid size-10 shrink-0 place-items-center rounded-lg border border-slate-200 bg-white text-slate-700 md:hidden" onClick={() => setIsMenuOpen((open) => !open)} type="button">
                        {isMenuOpen ? <X aria-hidden="true" size={19} /> : <Menu aria-hidden="true" size={19} />}
                    </button>
                </div>
            </header>

            <div className="relative min-h-[calc(100dvh-132px)] md:min-h-[calc(100dvh-136px)] md:grid md:grid-cols-[232px_minmax(0,1fr)]">
                {isMenuOpen && <button aria-label="Đóng điều hướng" className="fixed inset-x-0 bottom-0 top-[68px] z-20 bg-slate-950/20 md:hidden" onClick={() => setIsMenuOpen(false)} type="button" />}
                <aside id="workspace-navigation" className={`${isMenuOpen ? 'fixed inset-y-[68px] left-0 z-30 flex w-[min(86vw,288px)] shadow-xl' : 'hidden'} flex-col border-r border-slate-200 bg-white px-3 py-6 md:sticky md:top-[72px] md:flex md:h-[calc(100dvh-136px)] md:w-[232px] md:self-start md:shadow-none`}>
                    <p className="px-3 text-[11px] font-bold uppercase tracking-[0.12em] text-slate-500">{sectionLabel}</p>
                    <nav aria-label={`Điều hướng ${sectionLabel.toLowerCase()}`} className="mt-4 grid gap-1">
                        {navigationItems.map(({ to, label, icon: Icon }) => (
                            <NavLink
                                aria-label={label}
                                className={({ isActive }) => `flex min-h-10 items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors ${isActive ? 'bg-blue-50 text-blue-700' : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'}`}
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
                        <NavLink className="inline-flex items-center gap-2 text-xs font-medium text-slate-500 hover:text-blue-700" to="/">
                            <ArrowLeft aria-hidden="true" size={14} />Về TalentBridge
                        </NavLink>
                        <div className="mt-4 flex items-center gap-2 text-xs text-slate-500">
                            <span className="size-2 rounded-full bg-emerald-500" />Phiên làm việc an toàn
                        </div>
                    </div>
                </aside>

                <main className="min-w-0 px-5 py-8 sm:px-7 md:px-8 lg:px-10 lg:py-10">
                    <div className="mx-auto w-full max-w-[1176px]">
                        <Outlet />
                    </div>
                </main>
            </div>

            <footer className="flex min-h-16 items-center justify-between gap-3 border-t border-slate-200 bg-white px-5 text-xs text-slate-600 sm:px-8">
                <span>TalentBridge · Kết nối nghề nghiệp có trách nhiệm</span>
                <span className="hidden font-semibold text-slate-500 sm:inline">{roleLabel}</span>
            </footer>

            {isMenuOpen && <div className="fixed bottom-0 left-0 z-40 w-[min(86vw,288px)] border-t border-slate-200 bg-white p-4 md:hidden">
                <p className="truncate text-sm font-semibold text-slate-900">{displayName}</p>
                <p className="mt-0.5 text-xs text-slate-500">{roleLabel}</p>
                <div className="mt-3 flex gap-2">
                    <button className="inline-flex items-center gap-2 rounded-lg border border-slate-200 px-3 py-2 text-xs font-semibold text-slate-700" onClick={() => { setIsMenuOpen(false); setIsChangePasswordOpen(true) }} type="button"><KeyRound aria-hidden="true" size={14} />Đổi mật khẩu</button>
                    <button className="inline-flex items-center gap-2 rounded-lg border border-slate-200 px-3 py-2 text-xs font-semibold text-slate-700" onClick={() => void handleLogout()} type="button"><LogOut aria-hidden="true" size={14} />Đăng xuất</button>
                </div>
            </div>}

            <ChangePasswordModal isOpen={isChangePasswordOpen} onClose={() => setIsChangePasswordOpen(false)} />
        </div>
    )
}
