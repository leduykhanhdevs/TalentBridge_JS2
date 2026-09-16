import {
    CheckCircle2,
    LockKeyhole,
    Mail,
    ShieldCheck,
} from 'lucide-react'
import { Link } from 'react-router'

export function LoginPage() {
    return (
        <section className="relative overflow-hidden bg-slate-50 px-4 py-12 sm:px-6 lg:px-8">
            <div
                aria-hidden="true"
                className="absolute -left-32 top-20 size-80 rounded-full bg-indigo-100/70 blur-3xl"
            />
            <div
                aria-hidden="true"
                className="absolute -right-32 bottom-10 size-80 rounded-full bg-violet-100/70 blur-3xl"
            />

            <div className="relative mx-auto grid w-full max-w-6xl overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-xl shadow-slate-200/60 lg:grid-cols-[1.05fr_0.95fr]">
                <div className="relative hidden overflow-hidden bg-slate-950 p-12 text-white lg:flex lg:flex-col lg:justify-between">
                    <div
                        aria-hidden="true"
                        className="absolute -right-20 -top-20 size-64 rounded-full bg-indigo-500/30 blur-3xl"
                    />
                    <div
                        aria-hidden="true"
                        className="absolute -bottom-24 -left-16 size-72 rounded-full bg-violet-500/20 blur-3xl"
                    />

                    <div className="relative">
                        <div className="grid size-12 place-items-center rounded-2xl bg-indigo-500">
                            <ShieldCheck aria-hidden="true" size={25} />
                        </div>

                        <p className="mt-10 text-sm font-semibold uppercase tracking-wider text-indigo-300">
                            TalentBridge ATS
                        </p>

                        <h1 className="mt-4 text-4xl font-bold leading-tight">
                            Tiếp tục hành trình nghề nghiệp của bạn
                        </h1>

                        <p className="mt-5 max-w-md leading-7 text-slate-300">
                            Đăng nhập để quản lý hồ sơ, theo dõi trạng thái ứng tuyển và khám
                            phá những cơ hội phù hợp.
                        </p>
                    </div>

                    <div className="relative mt-12 space-y-4 text-sm text-slate-300">
                        <p className="flex items-center gap-3">
                            <CheckCircle2
                                aria-hidden="true"
                                className="text-emerald-400"
                                size={19}
                            />
                            Quản lý hồ sơ ứng viên tập trung
                        </p>

                        <p className="flex items-center gap-3">
                            <CheckCircle2
                                aria-hidden="true"
                                className="text-emerald-400"
                                size={19}
                            />
                            Theo dõi tiến trình ứng tuyển
                        </p>

                        <p className="flex items-center gap-3">
                            <CheckCircle2
                                aria-hidden="true"
                                className="text-emerald-400"
                                size={19}
                            />
                            Bảo vệ thông tin tài khoản
                        </p>
                    </div>
                </div>

                <div className="p-6 sm:p-10 lg:p-12">
                    <div className="mx-auto w-full max-w-md">
                        <p className="text-sm font-semibold text-indigo-600">
                            Chào mừng trở lại
                        </p>

                        <h2 className="mt-2 text-3xl font-bold tracking-tight text-slate-950">
                            Đăng nhập tài khoản
                        </h2>

                        <p className="mt-3 leading-7 text-slate-600">
                            Nhập thông tin tài khoản TalentBridge của bạn.
                        </p>

                        <form className="mt-8 space-y-5">
                            <div>
                                <label
                                    className="mb-2 block text-sm font-medium text-slate-700"
                                    htmlFor="email"
                                >
                                    Địa chỉ email
                                </label>

                                <div className="relative">
                                    <Mail
                                        aria-hidden="true"
                                        className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400"
                                        size={19}
                                    />
                                    <input
                                        autoComplete="email"
                                        className="h-12 w-full rounded-xl border border-slate-300 bg-white pl-11 pr-4 text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-indigo-500 focus:ring-4 focus:ring-indigo-100"
                                        id="email"
                                        name="email"
                                        placeholder="ban@example.com"
                                        type="email"
                                    />
                                </div>
                            </div>

                            <div>
                                <div className="mb-2 flex items-center justify-between gap-4">
                                    <label
                                        className="block text-sm font-medium text-slate-700"
                                        htmlFor="password"
                                    >
                                        Mật khẩu
                                    </label>

                                    <span className="text-sm text-slate-400">
                    Quên mật khẩu?
                  </span>
                                </div>

                                <div className="relative">
                                    <LockKeyhole
                                        aria-hidden="true"
                                        className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400"
                                        size={19}
                                    />
                                    <input
                                        autoComplete="current-password"
                                        className="h-12 w-full rounded-xl border border-slate-300 bg-white pl-11 pr-4 text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-indigo-500 focus:ring-4 focus:ring-indigo-100"
                                        id="password"
                                        name="password"
                                        placeholder="Nhập mật khẩu"
                                        type="password"
                                    />
                                </div>
                            </div>

                            <label className="flex items-center gap-3 text-sm text-slate-600">
                                <input
                                    className="size-4 rounded border-slate-300 text-indigo-600 accent-indigo-600"
                                    type="checkbox"
                                />
                                Ghi nhớ đăng nhập
                            </label>

                            <button
                                className="inline-flex h-12 w-full cursor-not-allowed items-center justify-center rounded-xl bg-indigo-600 px-5 text-sm font-semibold text-white opacity-70"
                                disabled
                                type="button"
                            >
                                Đăng nhập
                            </button>
                        </form>

                        <div className="mt-5 rounded-xl border border-amber-200 bg-amber-50 p-3 text-sm leading-6 text-amber-800">
                            Chức năng xác thực sẽ được kết nối với backend trong HRPM-10.
                        </div>

                        <p className="mt-7 text-center text-sm text-slate-600">
                            Chưa có tài khoản?{' '}
                            <Link
                                className="font-semibold text-indigo-600 hover:text-indigo-700"
                                to="/register"
                            >
                                Đăng ký ngay
                            </Link>
                        </p>
                    </div>
                </div>
            </div>
        </section>
    )
}