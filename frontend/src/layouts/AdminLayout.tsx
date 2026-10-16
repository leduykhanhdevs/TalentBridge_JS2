import { useEffect, useState } from 'react'
import { BriefcaseBusiness, Building2, ShieldAlert, UserCheck, Users } from 'lucide-react'
import { useNavigate } from 'react-router'
import { getStoredUser, isAuthenticated } from '../features/auth/tokenStorage'
import type { UserResponse } from '../features/auth/authTypes'
import { WorkspaceLayoutFrame } from './WorkspaceLayoutFrame'

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

    if (!authenticated || !user?.roles?.includes('ROLE_ADMIN')) {
        return (
            <main className="grid min-h-[70vh] place-items-center bg-bento-canvas px-4 py-12">
                <section className="bento-card w-full max-w-md p-7 text-center">
                    <span className="mx-auto grid size-12 place-items-center rounded-xl bg-rose-50 text-rose-700"><ShieldAlert aria-hidden="true" size={23} /></span>
                    <h1 className="mt-4 text-xl font-bold text-slate-900">Truy cập bị từ chối</h1>
                    <p className="mt-2 text-sm leading-6 text-slate-600">Đăng nhập bằng tài khoản Quản trị viên (ROLE_ADMIN) để mở khu vực quản trị.</p>
                    <div className="mt-5 flex justify-center gap-3">
                        <button className="btn-bento-primary" onClick={() => navigate('/login')} type="button">Đăng nhập</button>
                        <button className="btn-bento-secondary" onClick={() => navigate('/')} type="button">Trang chủ</button>
                    </div>
                </section>
            </main>
        )
    }

    const navigationItems = [
        { to: '/admin/candidates', label: 'Ứng viên', icon: Users },
        { to: '/admin/recruiters', label: 'Nhà tuyển dụng', icon: UserCheck },
        { to: '/admin/companies', label: 'Doanh nghiệp', icon: Building2 },
        { to: '/admin/jobs', label: 'Tin tuyển dụng', icon: BriefcaseBusiness },
    ]

    return <WorkspaceLayoutFrame sectionLabel="Quản trị" roleLabel="Quản trị viên" user={user} navigationItems={navigationItems} />
}
