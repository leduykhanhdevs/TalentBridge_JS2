import { Home, LogIn } from 'lucide-react'
import { Link } from 'react-router'
import { NotFoundIllustration } from '../components/illustrations'

export function NotFoundPage() {
    return (
        <section className="bg-bento-canvas min-h-[calc(100dvh-4rem)] flex items-center justify-center px-4 py-16 sm:px-6 lg:px-8">
            <div className="bento-card mx-auto max-w-lg w-full p-8 sm:p-10 text-center bg-white shadow-xl rounded-3xl border border-slate-200/80">
                <div className="mb-6 flex items-center justify-between border-b border-slate-100 pb-3 text-left">
                    <div className="flex items-center gap-2">
                        <span className="size-2.5 rounded-full bg-rose-400" />
                        <span className="size-2.5 rounded-full bg-amber-400" />
                        <span className="size-2.5 rounded-full bg-emerald-400" />
                        <span className="text-xs font-semibold text-slate-500 ml-1">Lỗi điều hướng</span>
                    </div>
                    <span className="bento-badge bg-rose-50 text-rose-700 border-rose-200/60 text-[11px]">
                        404 Not Found
                    </span>
                </div>

                <div className="mx-auto my-2">
                    <NotFoundIllustration className="w-48 h-auto mx-auto" />
                </div>

                <h1 className="mt-4 text-2xl sm:text-3xl font-bold tracking-tight text-slate-900">
                    Không tìm thấy trang
                </h1>

                <p className="mx-auto mt-3 max-w-md text-sm leading-relaxed text-slate-600">
                    Địa chỉ bạn truy cập không tồn tại hoặc đã được di chuyển. Vui lòng kiểm tra lại đường dẫn hoặc quay về trang chủ.
                </p>

                <div className="mt-8 flex flex-col justify-center gap-3 sm:flex-row">
                    <Link
                        className="btn-bento-primary h-11 px-5 text-sm font-semibold"
                        to="/"
                    >
                        <Home aria-hidden="true" size={16} />
                        <span>Quay về trang chủ</span>
                    </Link>

                    <Link
                        className="btn-bento-secondary h-11 px-5 text-sm font-semibold"
                        to="/login"
                    >
                        <LogIn aria-hidden="true" size={16} />
                        <span>Đi đến đăng nhập</span>
                    </Link>
                </div>
            </div>
        </section>
    )
}
