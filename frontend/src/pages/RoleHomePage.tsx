import { BriefcaseBusiness, LogOut, ShieldCheck, UserRound } from 'lucide-react'
import { Navigate, useNavigate } from 'react-router'
import { getRoleHomePath } from '../features/auth/authRoutes'
import {
    clearAuthTokens,
    getAccessToken,
    getStoredUser,
} from '../features/auth/tokenStorage'
import type { UserRole } from '../features/auth/authTypes'

const roleContent: Record<
    UserRole,
    { eyebrow: string; title: string; description: string }
> = {
    ROLE_CANDIDATE: {
        eyebrow: 'Candidate workspace',
        title: 'Trang chủ Ứng viên',
        description: 'Quản lý hồ sơ và theo dõi hành trình tìm việc của bạn.',
    },
    ROLE_RECRUITER: {
        eyebrow: 'Recruiter workspace',
        title: 'Trang chủ Nhà tuyển dụng',
        description: 'Quản lý doanh nghiệp và quy trình tuyển dụng của bạn.',
    },
    ROLE_ADMIN: {
        eyebrow: 'Admin workspace',
        title: 'Trang chủ Quản trị viên',
        description: 'Theo dõi và quản trị hoạt động của hệ thống TalentBridge.',
    },
}

export function RoleHomePage({ role }: { role: UserRole }) {
    const navigate = useNavigate()
    const accessToken = getAccessToken()
    const user = getStoredUser()

    if (!accessToken || !user) {
        return <Navigate replace to="/login" />
    }

    if (!user.roles.includes(role)) {
        return <Navigate replace to={getRoleHomePath(user.roles)} />
    }

    const content = roleContent[role]

    function handleLogout() {
        clearAuthTokens()
        navigate('/login', { replace: true })
    }

    return (
        <section className="bg-slate-50 px-4 py-16 sm:px-6 lg:px-8">
            <div className="mx-auto max-w-5xl overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-xl shadow-slate-200/60">
                <div className="bg-gradient-to-r from-indigo-600 to-violet-600 px-6 py-10 text-white sm:px-10">
                    <div className="grid size-12 place-items-center rounded-2xl bg-white/15 ring-1 ring-white/20">
                        <ShieldCheck aria-hidden="true" size={25} />
                    </div>
                    <p className="mt-6 text-sm font-semibold uppercase tracking-wider text-indigo-100">
                        {content.eyebrow}
                    </p>
                    <h1 className="mt-2 text-3xl font-bold sm:text-4xl">
                        {content.title}
                    </h1>
                    <p className="mt-3 max-w-2xl leading-7 text-indigo-100">
                        {content.description}
                    </p>
                </div>

                <div className="grid gap-6 p-6 sm:grid-cols-[1fr_auto] sm:items-center sm:p-10">
                    <div className="flex items-center gap-4">
                        <div className="grid size-12 shrink-0 place-items-center rounded-2xl bg-indigo-50 text-indigo-600">
                            <UserRound aria-hidden="true" size={23} />
                        </div>
                        <div>
                            <p className="font-semibold text-slate-950">
                                {user.fullName}
                            </p>
                            <p className="mt-1 text-sm text-slate-500">
                                {user.email}
                            </p>
                        </div>
                    </div>

                    <button
                        className="inline-flex h-11 items-center justify-center gap-2 rounded-xl border border-slate-300 px-4 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
                        onClick={handleLogout}
                        type="button"
                    >
                        <LogOut aria-hidden="true" size={18} />
                        Đăng xuất
                    </button>
                </div>

                <div className="border-t border-slate-100 p-6 sm:p-10">
                    <div className="flex items-start gap-4 rounded-2xl bg-slate-50 p-5">
                        <BriefcaseBusiness
                            aria-hidden="true"
                            className="mt-0.5 shrink-0 text-indigo-600"
                            size={22}
                        />
                        <p className="leading-7 text-slate-600">
                            Bạn đã đăng nhập thành công. Các chức năng chi tiết của vai
                            trò sẽ được bổ sung trong những nhiệm vụ tương ứng.
                        </p>
                    </div>
                </div>
            </div>
        </section>
    )
}
