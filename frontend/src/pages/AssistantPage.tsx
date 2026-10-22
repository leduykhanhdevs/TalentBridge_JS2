import { useEffect, useState } from 'react'
import { ArrowLeft, Bot, LockKeyhole, Sparkles } from 'lucide-react'
import { Link } from 'react-router'
import { ErrorBoundary } from '../components/ErrorBoundary'
import { AssistantChatPanel } from '../features/assistant/components/AssistantChatPanel'
import { getStoredUser } from '../features/auth/tokenStorage'

export function AssistantPage() {
    const [storedUser, setStoredUser] = useState(() => getStoredUser())
    useEffect(() => {
        const syncUser = () => setStoredUser(getStoredUser())
        window.addEventListener('talentbridge_auth_change', syncUser)
        window.addEventListener('storage', syncUser)
        return () => {
            window.removeEventListener('talentbridge_auth_change', syncUser)
            window.removeEventListener('storage', syncUser)
        }
    }, [])
    const recruiterMode = Boolean(storedUser?.roles.includes('ROLE_RECRUITER'))

    return (
        <main className="mx-auto w-full max-w-[1100px] px-4 py-7 sm:px-6 sm:py-9 lg:px-8">
            <Link className="mb-5 inline-flex items-center gap-2 text-sm font-semibold text-slate-600 transition hover:text-blue-700" to="/">
                <ArrowLeft aria-hidden="true" size={16} /> Về TalentBridge
            </Link>

            <header className="mb-6 flex items-start gap-4">
                <span className="grid size-12 shrink-0 place-items-center rounded-xl bg-blue-700 text-white shadow-sm">
                    <Bot aria-hidden="true" size={23} />
                </span>
                <div className="min-w-0">
                    <p className="text-xs font-bold uppercase tracking-[0.12em] text-blue-700">{recruiterMode ? 'Trợ lý tuyển dụng' : 'Hỗ trợ trong ứng dụng'}</p>
                    <h1 className="mt-1 text-2xl font-bold tracking-tight text-slate-900 sm:text-[28px]">{recruiterMode ? 'Trợ lý nhà tuyển dụng' : 'Trợ lý TalentBridge'}</h1>
                    <p className="mt-2 max-w-2xl text-sm leading-6 text-slate-600">
                        {recruiterMode
                            ? 'Thống kê hồ sơ theo từng tin, lọc ứng viên theo từ khóa, vòng tuyển dụng và kinh nghiệm, rồi xuất danh sách CSV theo bộ lọc.'
                            : 'Hướng dẫn thao tác tìm việc, quản lý hồ sơ và đăng tin trong TalentBridge. Trợ lý trả lời dựa trên hướng dẫn có sẵn.'}
                    </p>
                </div>
            </header>

            <div className="grid items-start gap-4 lg:grid-cols-[minmax(0,1fr)_270px] lg:gap-5">
                <ErrorBoundary fallback={(
                    <section className="rounded-xl border border-rose-200 bg-white p-6 text-sm text-slate-700" role="alert">
                        <p>Trợ lý tạm thời gặp lỗi hiển thị. Các mục khác của TalentBridge vẫn dùng được.</p>
                        <button className="mt-3 font-semibold text-blue-700 underline" onClick={() => window.location.reload()} type="button">
                            Tải lại trợ lý
                        </button>
                    </section>
                )}>
                    <AssistantChatPanel key={`assistant-${storedUser?.id ?? 'guest'}`} recruiterMode={recruiterMode} />
                </ErrorBoundary>

                <aside aria-label={recruiterMode ? 'Thông tin về Trợ lý nhà tuyển dụng' : 'Thông tin về Trợ lý TalentBridge'} className="grid gap-3 sm:grid-cols-2 lg:grid-cols-1">
                    <section className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
                        <div className="flex items-center gap-2.5">
                            <span className="grid size-8 place-items-center rounded-lg bg-blue-50 text-blue-700"><LockKeyhole aria-hidden="true" size={16} /></span>
                            <h2 className="text-sm font-bold text-slate-900">Riêng tư &amp; an toàn</h2>
                        </div>
                        <p className="mt-2 text-sm leading-6 text-slate-600">Không nhập mật khẩu, OTP, thông tin thanh toán hoặc nội dung hồ sơ nhạy cảm vào khung chat.</p>
                    </section>
                    <section className="rounded-xl border border-blue-100 bg-blue-50/50 p-4">
                        <div className="flex items-center gap-2.5">
                            <span className="grid size-8 place-items-center rounded-lg bg-white text-blue-700"><Sparkles aria-hidden="true" size={16} /></span>
                            <h2 className="text-sm font-bold text-slate-900">Phạm vi hỗ trợ</h2>
                        </div>
                        <p className="mt-2 text-sm leading-6 text-slate-700">{recruiterMode ? 'Dữ liệu ATS được đọc từ các tin tuyển dụng mà tài khoản nhà tuyển dụng có quyền quản lý.' : 'Ứng tuyển, cập nhật hồ sơ, theo dõi đơn và quy trình đăng tin tuyển dụng.'}</p>
                    </section>
                </aside>
            </div>
        </main>
    )
}
