import { Home, LogIn, SearchX } from 'lucide-react'
import { Link } from 'react-router'

export function NotFoundPage() {
    return (
        <section className="relative flex min-h-[calc(100vh-9rem)] items-center justify-center overflow-hidden bg-slate-50 px-4 py-16 sm:px-6 lg:px-8">
            <div
                aria-hidden="true"
                className="absolute -left-20 top-10 size-72 rounded-full bg-indigo-100/80 blur-3xl"
            />
            <div
                aria-hidden="true"
                className="absolute -right-20 bottom-10 size-72 rounded-full bg-violet-100/80 blur-3xl"
            />

            <div className="relative w-full max-w-2xl rounded-3xl border border-slate-200 bg-white p-8 text-center shadow-xl shadow-slate-200/60 sm:p-12">
                <div className="mx-auto grid size-20 place-items-center rounded-3xl bg-indigo-50 text-indigo-600">
                    <SearchX aria-hidden="true" size={38} />
                </div>

                <p className="mt-8 text-7xl font-black tracking-tight text-indigo-600 sm:text-8xl">
                    404
                </p>

                <h1 className="mt-4 text-3xl font-bold tracking-tight text-slate-950 sm:text-4xl">
                    Không tìm thấy trang
                </h1>

                <p className="mx-auto mt-4 max-w-lg leading-7 text-slate-600">
                    Địa chỉ bạn truy cập không tồn tại, đã được di chuyển hoặc không còn
                    khả dụng. Hãy kiểm tra lại đường dẫn hoặc quay về trang chủ.
                </p>

                <div className="mt-8 flex flex-col justify-center gap-3 sm:flex-row">
                    <Link
                        className="inline-flex items-center justify-center gap-2 rounded-xl bg-indigo-600 px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-indigo-700"
                        to="/"
                    >
                        <Home aria-hidden="true" size={18} />
                        Quay về trang chủ
                    </Link>

                    <Link
                        className="inline-flex items-center justify-center gap-2 rounded-xl border border-slate-300 bg-white px-5 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50"
                        to="/login"
                    >
                        <LogIn aria-hidden="true" size={18} />
                        Đi đến đăng nhập
                    </Link>
                </div>

                <p className="mt-8 text-sm text-slate-400">
                    Mã lỗi: ROUTE_NOT_FOUND
                </p>
            </div>
        </section>
    )
}