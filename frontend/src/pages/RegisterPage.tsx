import {
    CheckCircle2,
    LockKeyhole,
    Mail,
    UserPlus,
    UserRound,
} from 'lucide-react'
import { Link } from 'react-router'

const inputClassName =
    'h-12 w-full rounded-xl border border-slate-300 bg-white pl-11 pr-4 text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-indigo-500 focus:ring-4 focus:ring-indigo-100'

export function RegisterPage() {
    return (
        <section className="relative overflow-hidden bg-slate-50 px-4 py-12 sm:px-6 lg:px-8">
            <div
                aria-hidden="true"
                className="absolute -left-32 bottom-10 size-80 rounded-full bg-violet-100/70 blur-3xl"
            />
            <div
                aria-hidden="true"
                className="absolute -right-32 top-20 size-80 rounded-full bg-indigo-100/70 blur-3xl"
            />

            <div className="relative mx-auto grid w-full max-w-6xl overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-xl shadow-slate-200/60 lg:grid-cols-[0.95fr_1.05fr]">
                <div className="relative hidden overflow-hidden bg-indigo-600 p-12 text-white lg:flex lg:flex-col lg:justify-between">
                    <div
                        aria-hidden="true"
                        className="absolute -right-20 -top-20 size-64 rounded-full bg-white/15 blur-3xl"
                    />
                    <div
                        aria-hidden="true"
                        className="absolute -bottom-24 -left-16 size-72 rounded-full bg-violet-950/30 blur-3xl"
                    />

                    <div className="relative">
                        <div className="grid size-12 place-items-center rounded-2xl bg-white/15 ring-1 ring-white/20">
                            <UserPlus aria-hidden="true" size={25} />
                        </div>

                        <p className="mt-10 text-sm font-semibold uppercase tracking-wider text-indigo-100">
                            Tài khoản ứng viên
                        </p>

                        <h1 className="mt-4 text-4xl font-bold leading-tight">
                            Khởi đầu hành trình mới cùng TalentBridge
                        </h1>

                        <p className="mt-5 max-w-md leading-7 text-indigo-100">
                            Tạo hồ sơ chuyên nghiệp, khám phá việc làm phù hợp và kết nối với
                            những doanh nghiệp uy tín.
                        </p>
                    </div>

                    <div className="relative mt-12 space-y-4 text-sm text-indigo-50">
                        <p className="flex items-center gap-3">
                            <CheckCircle2 aria-hidden="true" size={19} />
                            Tạo và quản lý hồ sơ cá nhân
                        </p>

                        <p className="flex items-center gap-3">
                            <CheckCircle2 aria-hidden="true" size={19} />
                            Nhận gợi ý công việc phù hợp
                        </p>

                        <p className="flex items-center gap-3">
                            <CheckCircle2 aria-hidden="true" size={19} />
                            Theo dõi trạng thái ứng tuyển
                        </p>
                    </div>
                </div>

                <div className="p-6 sm:p-10 lg:p-12">
                    <div className="mx-auto w-full max-w-lg">
                        <p className="text-sm font-semibold text-indigo-600">
                            Candidate account
                        </p>

                        <h2 className="mt-2 text-3xl font-bold tracking-tight text-slate-950">
                            Đăng ký ứng viên
                        </h2>

                        <p className="mt-3 leading-7 text-slate-600">
                            Điền thông tin bên dưới để tạo tài khoản TalentBridge.
                        </p>

                        <form className="mt-8 space-y-5">
                            <div>
                                <label
                                    className="mb-2 block text-sm font-medium text-slate-700"
                                    htmlFor="full-name"
                                >
                                    Họ và tên
                                </label>

                                <div className="relative">
                                    <UserRound
                                        aria-hidden="true"
                                        className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400"
                                        size={19}
                                    />
                                    <input
                                        autoComplete="name"
                                        className={inputClassName}
                                        id="full-name"
                                        name="fullName"
                                        placeholder="Nguyễn Văn A"
                                        type="text"
                                    />
                                </div>
                            </div>

                            <div>
                                <label
                                    className="mb-2 block text-sm font-medium text-slate-700"
                                    htmlFor="register-email"
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
                                        className={inputClassName}
                                        id="register-email"
                                        name="email"
                                        placeholder="ban@example.com"
                                        type="email"
                                    />
                                </div>
                            </div>

                            <div className="grid gap-5 sm:grid-cols-2">
                                <div>
                                    <label
                                        className="mb-2 block text-sm font-medium text-slate-700"
                                        htmlFor="register-password"
                                    >
                                        Mật khẩu
                                    </label>

                                    <div className="relative">
                                        <LockKeyhole
                                            aria-hidden="true"
                                            className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400"
                                            size={19}
                                        />
                                        <input
                                            autoComplete="new-password"
                                            className={inputClassName}
                                            id="register-password"
                                            name="password"
                                            placeholder="Tối thiểu 8 ký tự"
                                            type="password"
                                        />
                                    </div>
                                </div>

                                <div>
                                    <label
                                        className="mb-2 block text-sm font-medium text-slate-700"
                                        htmlFor="confirm-password"
                                    >
                                        Xác nhận mật khẩu
                                    </label>

                                    <div className="relative">
                                        <LockKeyhole
                                            aria-hidden="true"
                                            className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400"
                                            size={19}
                                        />
                                        <input
                                            autoComplete="new-password"
                                            className={inputClassName}
                                            id="confirm-password"
                                            name="confirmPassword"
                                            placeholder="Nhập lại mật khẩu"
                                            type="password"
                                        />
                                    </div>
                                </div>
                            </div>

                            <label className="flex items-start gap-3 text-sm leading-6 text-slate-600">
                                <input
                                    className="mt-1 size-4 shrink-0 rounded border-slate-300 accent-indigo-600"
                                    type="checkbox"
                                />
                                <span>
                  Tôi đồng ý với điều khoản sử dụng và chính sách bảo mật của
                  TalentBridge.
                </span>
                            </label>

                            <button
                                className="inline-flex h-12 w-full cursor-not-allowed items-center justify-center gap-2 rounded-xl bg-indigo-600 px-5 text-sm font-semibold text-white opacity-70"
                                disabled
                                type="button"
                            >
                                <UserPlus aria-hidden="true" size={18} />
                                Tạo tài khoản
                            </button>
                        </form>

                        <div className="mt-5 rounded-xl border border-amber-200 bg-amber-50 p-3 text-sm leading-6 text-amber-800">
                            Chức năng đăng ký sẽ được kết nối với backend trong HRPM-8.
                        </div>

                        <p className="mt-7 text-center text-sm text-slate-600">
                            Đã có tài khoản?{' '}
                            <Link
                                className="font-semibold text-indigo-600 hover:text-indigo-700"
                                to="/login"
                            >
                                Đăng nhập
                            </Link>
                        </p>
                    </div>
                </div>
            </div>
        </section>
    )
}