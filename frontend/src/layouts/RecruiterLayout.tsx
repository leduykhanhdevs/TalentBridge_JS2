import { useEffect, useState } from 'react'
import { BarChart3, BriefcaseBusiness, Building2, CheckSquare, Search, ShieldAlert, UserCircle } from 'lucide-react'
import { useNavigate } from 'react-router'
import { getStoredUser, isAuthenticated } from '../features/auth/tokenStorage'
import type { UserResponse } from '../features/auth/authTypes'
import { WorkspaceLayoutFrame } from './WorkspaceLayoutFrame'

export function RecruiterLayout() {
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

    if (!authenticated || !user?.roles?.includes('ROLE_RECRUITER')) {
        return (
            <main className="grid min-h-[70vh] place-items-center bg-bento-canvas px-4 py-12">
                <section className="bento-card w-full max-w-md p-7 text-center">
                    <span className="mx-auto grid size-12 place-items-center rounded-xl bg-amber-50 text-amber-700"><ShieldAlert aria-hidden="true" size={23} /></span>
                    <h1 className="mt-4 text-xl font-bold text-slate-900">Cần tài khoản nhà tuyển dụng</h1>
                    <p className="mt-2 text-sm leading-6 text-slate-600">Đăng nhập bằng tài khoản có quyền ROLE_RECRUITER để quản lý tin và hồ sơ ứng tuyển.</p>
                    <div className="mt-5 flex justify-center gap-3">
                        <button className="btn-bento-primary" onClick={() => navigate('/login')} type="button">Đăng nhập</button>
                        <button className="btn-bento-secondary" onClick={() => navigate('/')} type="button">Trang chủ</button>
                    </div>
                </section>
            </main>
        )
    }

    const navigationItems = [
        { to: '/recruiter/profile', label: 'Hồ sơ HR', icon: UserCircle },
        { to: '/recruiter/jobs', label: 'Tin tuyển dụng', icon: BriefcaseBusiness },
        { to: '/recruiter/statistics', label: 'Thống kê ATS', icon: BarChart3 },
        { to: '/recruiter/company', label: 'Doanh nghiệp', icon: Building2 },
        { to: '/recruiter/join-company', label: 'Tìm & gia nhập', icon: Search },
        { to: '/recruiter/peer-approval', label: 'Duyệt thành viên', icon: CheckSquare },
    ]

    return <WorkspaceLayoutFrame sectionLabel="Tuyển dụng" roleLabel="Nhà tuyển dụng" user={user} navigationItems={navigationItems} />
}
