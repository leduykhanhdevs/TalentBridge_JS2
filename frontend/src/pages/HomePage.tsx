import { useEffect, useMemo, useState } from 'react'
import {
    Activity,
    ArrowRight,
    Award,
    Building2,
    CheckCircle2,
    Clock,
    Database,
    FileCheck,
    FileText,
    Filter,
    Lock,
    ShieldCheck,
    Sparkles,
    UserCheck,
} from 'lucide-react'
import { Link } from 'react-router'
import { getStoredUser } from '../features/auth/tokenStorage'
import {
    HeroTalentIllustration,
    DatabaseBentoIllustration,
    AtsPipelineIllustration,
    CvManagerIllustration,
} from '../components/illustrations'

const TYPEWRITER_PHRASES = ['đúng cơ hội', 'đúng ngành nghề']

function useTypewriter(words: string[], typingSpeed = 95, deletingSpeed = 45, pauseDuration = 2000) {
    const [wordIndex, setWordIndex] = useState(0)
    const [subIndex, setSubIndex] = useState(() => Array.from(words[0] || '').length)
    const [isDeleting, setIsDeleting] = useState(false)

    const currentChars = useMemo(() => Array.from(words[wordIndex % words.length] || ''), [words, wordIndex])

    useEffect(() => {
        if (!isDeleting && subIndex === currentChars.length) {
            const timeout = setTimeout(() => setIsDeleting(true), pauseDuration)
            return () => clearTimeout(timeout)
        }

        if (isDeleting && subIndex === 0) {
            const timeout = setTimeout(() => {
                setIsDeleting(false)
                setWordIndex((prev) => (prev + 1) % words.length)
            }, 350)
            return () => clearTimeout(timeout)
        }

        const timeout = setTimeout(() => {
            setSubIndex((prev) => prev + (isDeleting ? -1 : 1))
        }, isDeleting ? deletingSpeed : typingSpeed)

        return () => clearTimeout(timeout)
    }, [subIndex, isDeleting, currentChars.length, pauseDuration, deletingSpeed, typingSpeed, words.length])

    return currentChars.slice(0, subIndex).join('')
}

interface FeatureHighlight {
    icon: typeof ShieldCheck
    iconBg: string
    iconColor: string
    title: string
    badge: string
    badgeStyle: string
    subtitle: string
}

const FEATURE_HIGHLIGHTS: FeatureHighlight[] = [
    {
        icon: ShieldCheck,
        iconBg: 'bg-emerald-50 border border-emerald-200/70',
        iconColor: 'text-emerald-600',
        title: '100% Doanh nghiệp xác thực',
        badge: 'XÁC THỰC',
        badgeStyle: 'bg-emerald-50 text-emerald-700 border-emerald-200/80',
        subtitle: 'Kiểm duyệt GPKD & Mã số thuế bởi Admin',
    },
    {
        icon: FileCheck,
        iconBg: 'bg-indigo-50 border border-indigo-200/70',
        iconColor: 'text-indigo-600',
        title: 'Chuẩn hóa hồ sơ TopCV',
        badge: 'ĐÁNH GIÁ SAO',
        badgeStyle: 'bg-indigo-50 text-indigo-700 border-indigo-200/80',
        subtitle: 'Tự động tính năm KN & kỹ năng 1-5 sao',
    },
    {
        icon: Filter,
        iconBg: 'bg-amber-50 border border-amber-200/70',
        iconColor: 'text-amber-600',
        title: 'Quy trình tuyển dụng ATS 5 vòng',
        badge: 'TỰ ĐỘNG',
        badgeStyle: 'bg-amber-50 text-amber-700 border-amber-200/80',
        subtitle: 'Sàng lọc, phỏng vấn và nhận việc minh bạch',
    },
    {
        icon: Clock,
        iconBg: 'bg-rose-50 border border-rose-200/70',
        iconColor: 'text-rose-600',
        title: 'Tiết kiệm 70% thời gian HR',
        badge: 'HIỆU SUẤT',
        badgeStyle: 'bg-rose-50 text-rose-700 border-rose-200/80',
        subtitle: 'Phễu lọc thông minh và rút ngắn chu kỳ tuyển dụng',
    },
    {
        icon: Lock,
        iconBg: 'bg-slate-100 border border-slate-200/70',
        iconColor: 'text-slate-700',
        title: 'Bảo mật dữ liệu chuẩn Doanh nghiệp',
        badge: 'JWT & RBAC',
        badgeStyle: 'bg-slate-100 text-slate-700 border-slate-200/80',
        subtitle: 'Xác thực bảo mật kép và phân quyền 3 cấp độ',
    },
    {
        icon: Database,
        iconBg: 'bg-violet-50 border border-violet-200/70',
        iconColor: 'text-violet-600',
        title: 'Kiến trúc CSDL 27 Bảng 3NF',
        badge: 'CHUẨN 3NF',
        badgeStyle: 'bg-violet-50 text-violet-700 border-violet-200/80',
        subtitle: 'Toàn vẹn quan hệ & truy vấn tối ưu không độ trễ',
    },
    {
        icon: Activity,
        iconBg: 'bg-teal-50 border border-teal-200/70',
        iconColor: 'text-teal-600',
        title: 'Theo dõi trạng thái Real-time',
        badge: 'MINH BẠCH',
        badgeStyle: 'bg-teal-50 text-teal-700 border-teal-200/80',
        subtitle: 'Cập nhật tiến độ ứng tuyển ngay lập tức cho ứng viên',
    },
    {
        icon: Building2,
        iconBg: 'bg-sky-50 border border-sky-200/70',
        iconColor: 'text-sky-600',
        title: 'Kết nối mạng lưới IT hàng đầu',
        badge: 'ĐỐI TÁC',
        badgeStyle: 'bg-sky-50 text-sky-700 border-sky-200/80',
        subtitle: 'FPT Software, VNG, Viettel, One Mount, MoMo',
    },
    {
        icon: FileText,
        iconBg: 'bg-amber-50 border border-amber-200/70',
        iconColor: 'text-amber-600',
        title: 'Quản lý đa bản CV PDF 10MB',
        badge: 'LƯU TRỮ',
        badgeStyle: 'bg-amber-50 text-amber-700 border-amber-200/80',
        subtitle: 'Lưu trữ tập trung, xem trước và tải về tức thì',
    },
    {
        icon: Award,
        iconBg: 'bg-indigo-50 border border-indigo-200/70',
        iconColor: 'text-indigo-600',
        title: 'Trực quan hóa năng lực IT',
        badge: 'NĂNG LỰC',
        badgeStyle: 'bg-indigo-50 text-indigo-700 border-indigo-200/80',
        subtitle: 'Đánh giá chính xác theo tiêu chuẩn ngành IT',
    },
]

export function HomePage() {
    const user = getStoredUser()
    const isCandidate = Boolean(user?.roles?.includes('ROLE_CANDIDATE'))
    const isRecruiter = Boolean(user?.roles?.includes('ROLE_RECRUITER'))
    const isAdmin = Boolean(user?.roles?.includes('ROLE_ADMIN'))

    const typedHeadline = useTypewriter(TYPEWRITER_PHRASES)

    return (
        <div className="bg-bento-canvas min-h-screen text-slate-800">
            {/* ============================================================ */}
            {/* HERO BENTO SECTION (Godly / Mobbin Bento Grid Style)         */}
            {/* ============================================================ */}
            <section className="mx-auto w-full max-w-7xl px-4 pt-6 pb-12 sm:px-6 lg:px-8">
                <div className="grid grid-cols-1 gap-6 lg:grid-cols-12">
                    {/* Bento Cell 1: Hero Main (col-span-7) */}
                    <div className="bento-card flex flex-col justify-between p-6 sm:p-8 lg:col-span-7 min-h-[480px] lg:min-h-[510px]">
                        <div>
                            {/* Headline with Smooth Typewriter Box */}
                            <h1 className="text-2xl font-extrabold tracking-tight text-slate-900 sm:text-4xl lg:text-[40px] xl:text-[45px] leading-tight sm:leading-tight">
                                <span className="block">Kết nối tài năng công nghệ</span>
                                <span className="mt-2 inline-flex items-center gap-2.5 whitespace-nowrap min-h-[1.45em]">
                                    <span className="font-bold text-slate-800">với</span>
                                    <span className="relative inline-flex items-center">
                                        {/* Ambient Backlight Glow for Semi-Square Box */}
                                        <span
                                            aria-hidden="true"
                                            className="absolute -inset-1 rounded-lg sm:rounded-xl bg-gradient-to-r from-indigo-500/40 via-violet-500/40 to-indigo-600/40 blur-md pointer-events-none"
                                        />

                                        {/* High-End Bento Semi-Square Box (Ô hình bán vuông tạo điểm nhấn) */}
                                        <span className="relative inline-flex items-center h-[1.38em] px-3.5 sm:px-4.5 rounded-lg sm:rounded-xl bg-gradient-to-r from-indigo-600 via-indigo-700 to-violet-700 text-white shadow-[0_8px_22px_-4px_rgba(79,70,229,0.45),inset_0_1px_1px_rgba(255,255,255,0.45)] border border-indigo-300/50 backdrop-blur-xs before:absolute before:inset-x-2 before:top-0 before:h-px before:bg-gradient-to-r before:from-transparent before:via-white/70 before:to-transparent">
                                            {/* Typed text with beautiful font styling */}
                                            <span className="whitespace-pre font-black tracking-tight text-white drop-shadow-xs">
                                                {typedHeadline}
                                            </span>

                                            {/* Glowing Neon Beam Caret */}
                                            <span
                                                aria-hidden="true"
                                                className="inline-block w-[3px] h-[0.9em] ml-2 bg-gradient-to-b from-amber-200 via-amber-300 to-amber-400 rounded-full shadow-[0_0_10px_rgba(251,191,36,0.95)] animate-pulse select-none"
                                            />
                                        </span>
                                    </span>
                                </span>
                            </h1>

                            {/* Subtext */}
                            <p className="mt-4 max-w-xl text-base leading-relaxed text-slate-600 sm:text-lg">
                                Nền tảng tuyển dụng thông minh, hồ sơ chuẩn TopCV và quy trình ATS minh bạch dành cho kỹ sư phần mềm.
                            </p>
                        </div>

                        {/* CTA Buttons */}
                        <div className="mt-8 pt-6 border-t border-slate-100">
                            <div className="flex flex-wrap items-center gap-3">
                                {isAdmin ? (
                                    <>
                                        <Link
                                            className="btn-bento-primary text-sm sm:text-base"
                                            to="/admin/candidates"
                                        >
                                            <span>Vào cổng quản trị</span>
                                            <ArrowRight aria-hidden="true" size={18} />
                                        </Link>
                                        <Link
                                            className="btn-bento-secondary text-sm sm:text-base"
                                            to="/admin/companies"
                                        >
                                            <span>Duyệt doanh nghiệp</span>
                                        </Link>
                                    </>
                                ) : isRecruiter ? (
                                    <>
                                        <Link
                                            className="btn-bento-primary text-sm sm:text-base"
                                            to="/recruiter/profile"
                                        >
                                            <span>Vào kênh tuyển dụng</span>
                                            <ArrowRight aria-hidden="true" size={18} />
                                        </Link>
                                        <Link
                                            className="btn-bento-secondary text-sm sm:text-base"
                                            to="/recruiter/company"
                                        >
                                            <span>Doanh nghiệp của tôi</span>
                                        </Link>
                                    </>
                                ) : isCandidate ? (
                                    <>
                                        <Link
                                            className="btn-bento-primary text-sm sm:text-base"
                                            to="/candidate/profile"
                                        >
                                            <span>Vào hồ sơ TopCV</span>
                                            <ArrowRight aria-hidden="true" size={18} />
                                        </Link>
                                        <a
                                            className="btn-bento-secondary text-sm sm:text-base"
                                            href="#stats"
                                        >
                                            <span>Hệ sinh thái việc làm</span>
                                        </a>
                                    </>
                                ) : (
                                    <>
                                        <Link
                                            className="btn-bento-primary text-sm sm:text-base"
                                            to="/register"
                                        >
                                            <span>Bắt đầu ngay</span>
                                            <ArrowRight aria-hidden="true" size={18} />
                                        </Link>

                                        <Link
                                            className="btn-bento-secondary text-sm sm:text-base"
                                            to="/login"
                                        >
                                            <span>Đăng nhập</span>
                                        </Link>
                                    </>
                                )}
                            </div>

                            <div className="mt-6 flex flex-wrap gap-4 text-xs font-semibold text-slate-600 sm:text-sm">
                                <span className="inline-flex items-center gap-1.5">
                                    <CheckCircle2 aria-hidden="true" className="text-emerald-600" size={16} />
                                    Miễn phí cho ứng viên
                                </span>
                                <span className="inline-flex items-center gap-1.5">
                                    <CheckCircle2 aria-hidden="true" className="text-emerald-600" size={16} />
                                    100% Doanh nghiệp xác thực
                                </span>
                                <span className="inline-flex items-center gap-1.5">
                                    <CheckCircle2 aria-hidden="true" className="text-emerald-600" size={16} />
                                    Chuẩn hồ sơ TopCV
                                </span>
                            </div>
                        </div>
                    </div>

                    {/* Bento Cell 2: Hero Visual Illustration (col-span-5) */}
                    <div className="bento-card overflow-hidden p-5 lg:col-span-5 min-h-[480px] lg:min-h-[510px] flex flex-col justify-between bg-white">
                        <div className="flex items-center justify-between border-b border-slate-100 pb-3">
                            <div className="flex items-center gap-2">
                                <span className="size-2.5 rounded-full bg-rose-400" />
                                <span className="size-2.5 rounded-full bg-amber-400" />
                                <span className="size-2.5 rounded-full bg-emerald-400" />
                                <span className="ml-2 text-xs font-semibold text-slate-600">
                                    Talent Radar & Smart Match
                                </span>
                            </div>
                            <span className="bento-badge bg-emerald-50 text-emerald-700 border-emerald-200/60 text-[11px]">
                                Hệ thống trực tuyến
                            </span>
                        </div>

                        <div className="my-auto py-2">
                            <HeroTalentIllustration className="w-full h-auto" />
                        </div>

                        <div className="flex items-center justify-between border-t border-slate-100 pt-3 text-xs">
                            <div className="flex items-center gap-2">
                                <span className="size-2 rounded-full bg-emerald-500 animate-pulse" />
                                <span className="text-slate-600 font-medium">ATS 5 Vòng Tuyển dụng thông minh</span>
                            </div>
                            <span className="text-indigo-600 font-bold">Chuẩn hóa 100%</span>
                        </div>
                    </div>
                </div>

                {/* Sub-Bento Row: Quick Stats & Pipeline with Spot Illustrations */}
                <div className="mt-6 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
                    <div className="bento-card bg-gradient-to-br from-amber-50/40 via-white to-white p-6">
                        <div className="flex items-center gap-3.5">
                            <DatabaseBentoIllustration className="size-12 shrink-0" />
                            <div>
                                <span className="text-2xl font-black text-slate-900">27 Bảng 3NF</span>
                                <p className="text-xs font-medium text-slate-600 mt-0.5">
                                    Kiến trúc CSDL tối ưu, chuẩn hóa theo mẫu TopCV
                                </p>
                            </div>
                        </div>
                    </div>

                    <div className="bento-card bg-gradient-to-br from-indigo-50/40 via-white to-white p-6">
                        <div className="flex items-center gap-3.5">
                            <CvManagerIllustration className="size-12 shrink-0" />
                            <div>
                                <span className="text-2xl font-black text-slate-900">10MB CV PDF</span>
                                <p className="text-xs font-medium text-slate-600 mt-0.5">
                                    Tải lên, xem trước và quản lý đa bản CV
                                </p>
                            </div>
                        </div>
                    </div>

                    <div className="bento-card bg-gradient-to-br from-emerald-50/40 via-white to-white p-6 sm:col-span-2 lg:col-span-1">
                        <div className="flex items-center gap-3.5">
                            <AtsPipelineIllustration className="size-12 shrink-0" />
                            <div>
                                <span className="text-2xl font-black text-slate-900">5 Vòng ATS</span>
                                <p className="text-xs font-medium text-slate-600 mt-0.5">
                                    Sàng lọc, phỏng vấn và nhận việc minh bạch
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            {/* ============================================================ */}
            {/* INFINITE MARQUEE STRIP (Bento Feature & Value Highlights)     */}
            {/* ============================================================ */}
            <div className="relative border-y border-slate-200/80 bg-gradient-to-r from-slate-50/70 via-white/90 to-slate-50/70 backdrop-blur-md py-4 sm:py-5 shadow-2xs overflow-hidden">
                {/* Ambient Gradient Fade Edge Masks */}
                <div className="pointer-events-none absolute inset-y-0 left-0 w-16 sm:w-36 bg-gradient-to-r from-[#f8fafc] to-transparent z-10" />
                <div className="pointer-events-none absolute inset-y-0 right-0 w-16 sm:w-36 bg-gradient-to-l from-[#f8fafc] to-transparent z-10" />

                {/* Marquee Track with Bento Feature Cards (Ample vertical padding to prevent border/shadow clipping) */}
                <div className="marquee-container overflow-hidden py-3">
                    <div className="animate-infinite-marquee flex gap-4 sm:gap-5 items-center py-2">
                        {FEATURE_HIGHLIGHTS.concat(FEATURE_HIGHLIGHTS).map((item, index) => {
                            const Icon = item.icon
                            return (
                                <div
                                    key={index}
                                    className="group/card inline-flex items-center gap-3.5 rounded-2xl bg-white/95 backdrop-blur-md px-4 py-2.5 shadow-2xs border border-slate-200/90 shrink-0 hover:border-indigo-400 hover:shadow-md hover:bg-white transition-all duration-200 cursor-default my-1"
                                >
                                    <div
                                        className={`grid size-9 place-items-center rounded-xl ${item.iconBg} ${item.iconColor} transition-transform group-hover/card:scale-110 duration-200 shadow-2xs`}
                                    >
                                        <Icon size={18} />
                                    </div>
                                    <div className="flex flex-col text-left">
                                        <div className="flex items-center gap-2">
                                            <span className="text-xs sm:text-sm font-bold text-slate-900 tracking-tight whitespace-nowrap">
                                                {item.title}
                                            </span>
                                            <span
                                                className={`text-[9.5px] font-bold px-1.5 py-0.5 rounded-md border ${item.badgeStyle}`}
                                            >
                                                {item.badge}
                                            </span>
                                        </div>
                                        <span className="text-[11px] font-medium text-slate-500 whitespace-nowrap mt-0.5">
                                            {item.subtitle}
                                        </span>
                                    </div>
                                </div>
                            )
                        })}
                    </div>
                </div>
            </div>

            {/* ============================================================ */}
            {/* ECOSYSTEM BENTO GRID (Modern Asymmetrical 4-Card Composition) */}
            {/* ============================================================ */}
            <section className="mx-auto w-full max-w-7xl px-4 py-16 sm:px-6 lg:px-8" id="stats">
                <div className="mb-12 text-center max-w-3xl mx-auto">
                    <h2 className="text-2xl font-black tracking-tight text-slate-900 sm:text-4xl">
                        Hệ sinh thái tuyển dụng toàn diện
                    </h2>
                    <p className="mt-3 text-sm text-slate-600 sm:text-base">
                        Mọi tính năng được thiết kế chặt chẽ, phục vụ đồng thời ứng viên và nhà tuyển dụng.
                    </p>
                </div>

                <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
                    {/* Bento Cell A: TopCV Candidate Profile */}
                    <div className="bento-card flex flex-col justify-between p-6 sm:p-8 lg:col-span-2">
                        <div>
                            <div className="flex items-start gap-4">
                                <div className="grid size-12 place-items-center rounded-2xl bg-indigo-50 border border-indigo-200/60 shadow-2xs shrink-0">
                                    <CvManagerIllustration className="size-8" />
                                </div>
                                <div>
                                    <h3 className="text-xl sm:text-2xl font-black tracking-tight text-slate-900">
                                        Hồ sơ TopCV
                                    </h3>
                                    <p className="mt-1 text-xs sm:text-sm font-semibold text-indigo-600">
                                        Hồ sơ ứng viên chuyên sâu & Đánh giá kỹ năng sao
                                    </p>
                                </div>
                            </div>
                            <p className="mt-4 text-sm leading-relaxed text-slate-600">
                                Tự động tính tổng số năm kinh nghiệm từ lịch sử công tác, đánh giá kỹ năng từ 1 đến 5 sao theo chuẩn TopCV và quản lý đa bản CV tiện lợi.
                            </p>
                        </div>

                        <div className="mt-6 rounded-2xl border border-slate-200/80 bg-slate-50/60 p-4">
                            <div className="grid grid-cols-1 gap-3 sm:grid-cols-3 text-xs">
                                <div className="rounded-xl border border-slate-200 bg-white p-3 shadow-xs">
                                    <p className="text-slate-500 font-semibold text-[11px]">KINH NGHIỆM</p>
                                    <p className="font-bold text-slate-900 mt-1">Tự động tính năm</p>
                                </div>
                                <div className="rounded-xl border border-slate-200 bg-white p-3 shadow-xs">
                                    <p className="text-slate-500 font-semibold text-[11px]">KỸ NĂNG</p>
                                    <p className="font-bold text-slate-900 mt-1">1-5 Sao TopCV</p>
                                </div>
                                <div className="rounded-xl border border-slate-200 bg-white p-3 shadow-xs">
                                    <p className="text-slate-500 font-semibold text-[11px]">ĐỊNH DẠNG CV</p>
                                    <p className="font-bold text-slate-900 mt-1">PDF / DOCX 10MB</p>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Bento Cell B: ATS Pipeline */}
                    <div className="bento-card flex flex-col justify-between p-6 sm:p-8">
                        <div>
                            <div className="flex items-start gap-4">
                                <div className="grid size-12 place-items-center rounded-2xl bg-amber-50 border border-amber-200/60 shadow-2xs shrink-0">
                                    <AtsPipelineIllustration className="size-8" />
                                </div>
                                <div>
                                    <h3 className="text-xl sm:text-2xl font-black tracking-tight text-slate-900">
                                        ATS Pipeline
                                    </h3>
                                    <p className="mt-1 text-xs sm:text-sm font-semibold text-amber-600">
                                        Quy trình 5 vòng tuyển dụng chuẩn hóa
                                    </p>
                                </div>
                            </div>
                            <p className="mt-4 text-sm leading-relaxed text-slate-600">
                                Theo dõi tiến độ ứng tuyển theo thời gian thực từ lúc nộp đơn đến khi nhận việc minh bạch.
                            </p>
                        </div>

                        <div className="mt-6 space-y-2 text-xs">
                            <div className="flex items-center justify-between rounded-xl border border-slate-200/70 bg-white px-3 py-2 shadow-2xs">
                                <span className="text-slate-700 font-medium">1. APPLIED</span>
                                <span className="text-indigo-600 font-semibold">Mới nộp</span>
                            </div>
                            <div className="flex items-center justify-between rounded-xl border border-slate-200/70 bg-white px-3 py-2 shadow-2xs">
                                <span className="text-slate-700 font-medium">2. SCREENING</span>
                                <span className="text-blue-600 font-semibold">Sơ tuyển</span>
                            </div>
                            <div className="flex items-center justify-between rounded-xl border border-slate-200/70 bg-white px-3 py-2 shadow-2xs">
                                <span className="text-slate-700 font-medium">3. INTERVIEW</span>
                                <span className="text-amber-600 font-semibold">Phỏng vấn</span>
                            </div>
                            <div className="flex items-center justify-between rounded-xl border border-slate-200/70 bg-white px-3 py-2 shadow-2xs">
                                <span className="text-slate-700 font-medium">4. OFFER / HIRED</span>
                                <span className="text-emerald-600 font-bold">Thành công</span>
                            </div>
                        </div>
                    </div>

                    {/* Bento Cell C: Recruiter & Peer Approval */}
                    <div className="bento-card flex flex-col justify-between p-6 sm:p-8">
                        <div>
                            <div className="flex items-start gap-4">
                                <div className="grid size-12 place-items-center rounded-2xl bg-violet-50 text-violet-600 border border-violet-200/60 shadow-2xs shrink-0">
                                    <Building2 size={24} />
                                </div>
                                <div>
                                    <h3 className="text-xl sm:text-2xl font-black tracking-tight text-slate-900">
                                        Doanh nghiệp
                                    </h3>
                                    <p className="mt-1 text-xs sm:text-sm font-semibold text-violet-600">
                                        Phê duyệt nội bộ HR (Peer-Approval)
                                    </p>
                                </div>
                            </div>
                            <p className="mt-4 text-sm leading-relaxed text-slate-600">
                                Nhân sự mới gửi yêu cầu gia nhập công ty và được đồng nghiệp nội bộ phê duyệt an toàn.
                            </p>
                        </div>

                        <div className="mt-6 rounded-2xl border border-violet-100 bg-violet-50/50 p-4 text-xs">
                            <p className="font-bold text-slate-900">Cơ chế 2 lớp kiểm duyệt:</p>
                            <p className="mt-1 text-slate-600">Admin phê duyệt doanh nghiệp, HR nội bộ xét duyệt đồng nghiệp.</p>
                        </div>
                    </div>

                    {/* Bento Cell D: Multi-role RBAC Architecture */}
                    <div className="bento-card flex flex-col justify-between p-6 sm:p-8 lg:col-span-2">
                        <div>
                            <div className="flex items-start gap-4">
                                <div className="grid size-12 place-items-center rounded-2xl bg-rose-50 text-rose-600 border border-rose-200/60 shadow-2xs shrink-0">
                                    <ShieldCheck size={24} />
                                </div>
                                <div>
                                    <h3 className="text-xl sm:text-2xl font-black tracking-tight text-slate-900">
                                        Bảo mật & Phân quyền
                                    </h3>
                                    <p className="mt-1 text-xs sm:text-sm font-semibold text-rose-600">
                                        Phân quyền RBAC 3 cấp độ chặt chẽ
                                    </p>
                                </div>
                            </div>
                            <p className="mt-4 text-sm leading-relaxed text-slate-600">
                                Hỗ trợ phân chia quyền hạn rõ ràng và bảo vệ dữ liệu giữa Quản trị viên (Admin), Nhà tuyển dụng (Recruiter) và Ứng viên (Candidate).
                            </p>
                        </div>

                        <div className="mt-6 grid grid-cols-1 gap-3 sm:grid-cols-3 text-xs">
                            <div className="rounded-2xl border border-indigo-100 bg-indigo-50/40 p-3.5">
                                <p className="font-bold text-indigo-700">ROLE_CANDIDATE</p>
                                <p className="mt-1 text-slate-600 text-[11px]">Ứng tuyển, lưu tin, quản lý CV</p>
                            </div>
                            <div className="rounded-2xl border border-emerald-100 bg-emerald-50/40 p-3.5">
                                <p className="font-bold text-emerald-700">ROLE_RECRUITER</p>
                                <p className="mt-1 text-slate-600 text-[11px]">Đăng tin, lọc hồ sơ, ghi chú HR</p>
                            </div>
                            <div className="rounded-2xl border border-rose-100 bg-rose-50/40 p-3.5">
                                <p className="font-bold text-rose-700">ROLE_ADMIN</p>
                                <p className="mt-1 text-slate-600 text-[11px]">Duyệt công ty, kiểm soát hệ thống</p>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            {/* ============================================================ */}
            {/* MODERN BENTO CTA SECTION                                     */}
            {/* ============================================================ */}
            <section className="mx-auto w-full max-w-7xl px-4 pb-20 sm:px-6 lg:px-8">
                <div className="bento-card border border-indigo-700/20 bg-gradient-to-br from-indigo-900 via-indigo-800 to-slate-900 p-8 sm:p-14 text-white text-center shadow-xl relative overflow-hidden">
                    <div className="max-w-2xl mx-auto relative z-10">
                        {/* Elegant Eyebrow Accent (No enclosing box) */}
                        <div className="flex items-center justify-center gap-3 mb-4">
                            <span className="h-px w-8 sm:w-16 bg-gradient-to-r from-transparent to-amber-400/50" />
                            <span className="inline-flex items-center gap-2 text-xs sm:text-sm font-bold tracking-widest uppercase text-amber-300 drop-shadow-xs">
                                <Sparkles className="size-3.5 text-amber-400 animate-pulse" />
                                Bắt đầu trải nghiệm ngay
                            </span>
                            <span className="h-px w-8 sm:w-16 bg-gradient-to-l from-transparent to-amber-400/50" />
                        </div>

                        <h2 className="text-2xl font-black sm:text-4xl text-white tracking-tight">
                            Chọn vai trò của bạn trên TalentBridge
                        </h2>
                        <p className="mt-3 text-sm text-indigo-200 sm:text-base">
                            Đăng ký tài khoản ngay để trải nghiệm hệ thống tuyển dụng hiện đại dành cho cả ứng viên và nhà tuyển dụng.
                        </p>

                        <div className="mt-8 flex flex-col gap-3.5 sm:flex-row sm:justify-center">
                            {isAdmin ? (
                                <Link
                                    className="btn-bento-primary text-base px-6 py-3"
                                    to="/admin/candidates"
                                >
                                    <Sparkles size={18} />
                                    <span>Vào Cổng Quản trị Hệ thống</span>
                                </Link>
                            ) : isRecruiter ? (
                                <Link
                                    className="btn-bento-primary text-base px-6 py-3"
                                    to="/recruiter/profile"
                                >
                                    <Building2 size={18} />
                                    <span>Vào Kênh Quản lý Tuyển dụng</span>
                                </Link>
                            ) : isCandidate ? (
                                <Link
                                    className="btn-bento-accent text-base px-6 py-3"
                                    to="/candidate/profile"
                                >
                                    <UserCheck size={18} />
                                    <span>Vào Hồ sơ Ứng viên TopCV</span>
                                </Link>
                            ) : (
                                <>
                                    <Link
                                        className="btn-bento-accent text-base px-6 py-3"
                                        to="/register"
                                    >
                                        <UserCheck size={18} />
                                        <span>Tôi là Ứng viên</span>
                                    </Link>

                                    <Link
                                        className="btn-bento-secondary text-base px-6 py-3 text-slate-900"
                                        to="/register"
                                    >
                                        <Building2 size={18} />
                                        <span>Tôi là Nhà tuyển dụng</span>
                                    </Link>
                                </>
                            )}
                        </div>
                    </div>
                </div>
            </section>
        </div>
    )
}
