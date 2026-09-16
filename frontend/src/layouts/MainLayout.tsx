import { BriefcaseBusiness, LogIn, UserPlus } from 'lucide-react'
import { NavLink, Outlet } from 'react-router'

export function MainLayout() {
    return (
        <div className="flex min-h-screen flex-col bg-slate-50 text-slate-900">
            <header className="sticky top-0 z-50 border-b border-slate-200 bg-white/95 backdrop-blur">
                <div className="mx-auto flex h-16 w-full max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
                    <NavLink
                        className="flex items-center gap-3"
                        end
                        to="/"
                    >
            <span className="grid size-10 place-items-center rounded-xl bg-indigo-600 text-white shadow-sm">
              <BriefcaseBusiness aria-hidden="true" size={21} />
            </span>

                        <span>
              <span className="block text-base font-bold leading-none">
                TalentBridge
              </span>
              <span className="mt-1 hidden text-xs text-slate-500 sm:block">
                Applicant Tracking System
              </span>
            </span>
                    </NavLink>

                    <nav
                        aria-label="Điều hướng chính"
                        className="flex items-center gap-1 sm:gap-2"
                    >
                        <NavLink
                            className={({ isActive }) =>
                                `rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
                                    isActive
                                        ? 'bg-indigo-50 text-indigo-700'
                                        : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
                                }`
                            }
                            end
                            to="/"
                        >
                            Trang chủ
                        </NavLink>

                        <NavLink
                            className={({ isActive }) =>
                                `inline-flex items-center gap-2 rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
                                    isActive
                                        ? 'bg-indigo-50 text-indigo-700'
                                        : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
                                }`
                            }
                            to="/login"
                        >
                            <LogIn aria-hidden="true" size={17} />
                            <span className="hidden sm:inline">Đăng nhập</span>
                        </NavLink>

                        <NavLink
                            className="inline-flex items-center gap-2 rounded-lg bg-indigo-600 px-3 py-2 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-indigo-700"
                            to="/register"
                        >
                            <UserPlus aria-hidden="true" size={17} />
                            <span className="hidden sm:inline">Đăng ký</span>
                        </NavLink>
                    </nav>
                </div>
            </header>

            <main className="flex-1">
                <Outlet />
            </main>

            <footer className="border-t border-slate-200 bg-white">
                <div className="mx-auto flex w-full max-w-7xl flex-col gap-2 px-4 py-6 text-sm text-slate-500 sm:flex-row sm:items-center sm:justify-between sm:px-6 lg:px-8">
                    <p>© {new Date().getFullYear()} TalentBridge.</p>
                    <p>Kết nối đúng tài năng với đúng cơ hội.</p>
                </div>
            </footer>
        </div>
    )
}