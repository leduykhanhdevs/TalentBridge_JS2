import {
    ArrowRight,
    BriefcaseBusiness,
    Building2,
    CheckCircle2,
    Search,
    ShieldCheck,
    Users,
} from 'lucide-react'
import { Link, Navigate } from 'react-router'
import { getStoredUser } from '../features/auth/tokenStorage'

const features = [
    {
        icon: Search,
        title: 'Tìm kiếm thông minh',
        description:
            'Khám phá những cơ hội phù hợp với kỹ năng, kinh nghiệm và định hướng nghề nghiệp.',
    },
    {
        icon: Building2,
        title: 'Doanh nghiệp uy tín',
        description:
            'Kết nối với các doanh nghiệp đã được xác minh và có nhu cầu tuyển dụng thực tế.',
    },
    {
        icon: ShieldCheck,
        title: 'Quy trình minh bạch',
        description:
            'Theo dõi trạng thái ứng tuyển rõ ràng trong suốt quá trình tuyển dụng.',
    },
] as const

export function HomePage() {
    const user = getStoredUser()
    if (user?.roles?.includes('ROLE_RECRUITER')) {
        return <Navigate replace to="/recruiter/profile" />
    }
    if (user?.roles?.includes('ROLE_ADMIN')) {
        return <Navigate replace to="/admin/candidates" />
    }

    return (
        <>
            <section className="relative overflow-hidden border-b border-slate-200 bg-white">
                <div
                    aria-hidden="true"
                    className="absolute -left-24 top-16 size-72 rounded-full bg-indigo-100/70 blur-3xl"
                />
                <div
                    aria-hidden="true"
                    className="absolute -right-24 bottom-0 size-80 rounded-full bg-violet-100/70 blur-3xl"
                />

                <div className="relative mx-auto grid w-full max-w-7xl items-center gap-12 px-4 py-16 sm:px-6 sm:py-20 lg:grid-cols-2 lg:px-8 lg:py-24">
                    <div>
                        <div className="mb-6 inline-flex items-center gap-2 rounded-full border border-indigo-200 bg-indigo-50 px-3 py-1.5 text-sm font-medium text-indigo-700">
                            <BriefcaseBusiness aria-hidden="true" size={16} />
                            Nền tảng tuyển dụng TalentBridge
                        </div>

                        <h1 className="max-w-2xl text-4xl font-bold tracking-tight text-slate-950 sm:text-5xl lg:text-6xl">
                            Kết nối đúng tài năng với{' '}
                            <span className="text-indigo-600">đúng cơ hội</span>
                        </h1>

                        <p className="mt-6 max-w-xl text-base leading-8 text-slate-600 sm:text-lg">
                            TalentBridge giúp ứng viên khám phá công việc phù hợp và hỗ trợ
                            doanh nghiệp quản lý quy trình tuyển dụng hiệu quả trên một nền
                            tảng duy nhất.
                        </p>

                        <div className="mt-8 flex flex-col gap-3 sm:flex-row">
                            <Link
                                className="inline-flex items-center justify-center gap-2 rounded-xl bg-indigo-600 px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-indigo-700"
                                to="/register"
                            >
                                Bắt đầu ngay
                                <ArrowRight aria-hidden="true" size={18} />
                            </Link>

                            <Link
                                className="inline-flex items-center justify-center rounded-xl border border-slate-300 bg-white px-5 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50"
                                to="/login"
                            >
                                Đăng nhập tài khoản
                            </Link>
                        </div>

                        <div className="mt-8 flex flex-wrap gap-x-6 gap-y-3 text-sm text-slate-600">
              <span className="inline-flex items-center gap-2">
                <CheckCircle2
                    aria-hidden="true"
                    className="text-emerald-500"
                    size={18}
                />
                Miễn phí cho ứng viên
              </span>

                            <span className="inline-flex items-center gap-2">
                <CheckCircle2
                    aria-hidden="true"
                    className="text-emerald-500"
                    size={18}
                />
                Thông tin minh bạch
              </span>
                        </div>
                    </div>

                    <div className="relative">
                        <div className="rounded-3xl border border-slate-200 bg-white p-5 shadow-2xl shadow-indigo-100 sm:p-7">
                            <div className="flex items-center justify-between border-b border-slate-100 pb-5">
                                <div>
                                    <p className="text-sm font-semibold text-slate-900">
                                        Cơ hội mới dành cho bạn
                                    </p>
                                    <p className="mt-1 text-xs text-slate-500">
                                        Gợi ý dựa trên hồ sơ ứng viên
                                    </p>
                                </div>

                                <span className="rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-700">
                  Đang tuyển
                </span>
                            </div>

                            <div className="space-y-4 pt-5">
                                <article className="rounded-2xl border border-slate-200 p-4 transition hover:border-indigo-300 hover:bg-indigo-50/40">
                                    <div className="flex items-start justify-between gap-4">
                                        <div className="grid size-11 shrink-0 place-items-center rounded-xl bg-indigo-100 text-indigo-700">
                                            <Building2 aria-hidden="true" size={20} />
                                        </div>

                                        <div className="min-w-0 flex-1">
                                            <h2 className="font-semibold text-slate-900">
                                                Backend Developer
                                            </h2>
                                            <p className="mt-1 text-sm text-slate-500">
                                                TP. Hồ Chí Minh · Toàn thời gian
                                            </p>
                                        </div>

                                        <span className="text-sm font-semibold text-indigo-600">
                      Mới
                    </span>
                                    </div>
                                </article>

                                <article className="rounded-2xl border border-slate-200 p-4 transition hover:border-indigo-300 hover:bg-indigo-50/40">
                                    <div className="flex items-start justify-between gap-4">
                                        <div className="grid size-11 shrink-0 place-items-center rounded-xl bg-violet-100 text-violet-700">
                                            <Users aria-hidden="true" size={20} />
                                        </div>

                                        <div className="min-w-0 flex-1">
                                            <h2 className="font-semibold text-slate-900">
                                                Frontend Developer
                                            </h2>
                                            <p className="mt-1 text-sm text-slate-500">
                                                Hà Nội · Làm việc kết hợp
                                            </p>
                                        </div>

                                        <span className="text-sm font-semibold text-indigo-600">
                      Mới
                    </span>
                                    </div>
                                </article>
                            </div>

                            <div className="mt-5 rounded-2xl bg-slate-950 p-5 text-white">
                                <p className="text-sm text-slate-300">
                                    Hồ sơ được nhà tuyển dụng quan tâm
                                </p>
                                <div className="mt-3 flex items-end justify-between">
                                    <strong className="text-3xl font-bold">1.200+</strong>
                                    <span className="text-sm text-emerald-400">
                    Tăng trưởng mỗi ngày
                  </span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            <section className="bg-slate-50 py-16 sm:py-20">
                <div className="mx-auto w-full max-w-7xl px-4 sm:px-6 lg:px-8">
                    <div className="mx-auto max-w-2xl text-center">
                        <p className="text-sm font-semibold uppercase tracking-wider text-indigo-600">
                            Vì sao chọn TalentBridge?
                        </p>
                        <h2 className="mt-3 text-3xl font-bold tracking-tight text-slate-950 sm:text-4xl">
                            Một nền tảng cho toàn bộ hành trình tuyển dụng
                        </h2>
                        <p className="mt-4 leading-7 text-slate-600">
                            Những công cụ cần thiết để ứng viên và doanh nghiệp kết nối nhanh
                            chóng, an toàn và hiệu quả hơn.
                        </p>
                    </div>

                    <div className="mt-10 grid gap-6 md:grid-cols-3">
                        {features.map(({ icon: Icon, title, description }) => (
                            <article
                                className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm transition hover:-translate-y-1 hover:border-indigo-200 hover:shadow-lg"
                                key={title}
                            >
                                <div className="grid size-12 place-items-center rounded-xl bg-indigo-50 text-indigo-600">
                                    <Icon aria-hidden="true" size={22} />
                                </div>
                                <h3 className="mt-5 text-lg font-semibold text-slate-900">
                                    {title}
                                </h3>
                                <p className="mt-2 leading-7 text-slate-600">{description}</p>
                            </article>
                        ))}
                    </div>
                </div>
            </section>
        </>
    )
}