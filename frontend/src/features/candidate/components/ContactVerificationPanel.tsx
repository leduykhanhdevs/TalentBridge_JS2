import { useState } from 'react'
import { BadgeCheck, CircleAlert, Mail, MessageCircle, Send, ShieldCheck } from 'lucide-react'
import {
    requestContactVerificationCode,
    verifyContactVerificationCode,
} from '../candidateApi'
import type { ContactVerificationStatus } from '../candidateTypes'

interface ContactVerificationPanelProps {
    status: ContactVerificationStatus
    onVerified: (status: ContactVerificationStatus) => void
}

type VerificationChannel = 'EMAIL' | 'PHONE'

export function ContactVerificationPanel({ status, onVerified }: ContactVerificationPanelProps) {
    const [emailOtp, setEmailOtp] = useState('')
    const [phoneOtp, setPhoneOtp] = useState('')
    const [emailCodeSent, setEmailCodeSent] = useState(false)
    const [phoneCodeSent, setPhoneCodeSent] = useState(false)
    const [busyChannel, setBusyChannel] = useState<VerificationChannel | null>(null)
    const [errorMessage, setErrorMessage] = useState<string | null>(null)
    const [successMessage, setSuccessMessage] = useState<string | null>(null)

    async function requestCode(channel: VerificationChannel) {
        setBusyChannel(channel)
        setErrorMessage(null)
        setSuccessMessage(null)
        try {
            await requestContactVerificationCode(channel)
            if (channel === 'EMAIL') setEmailCodeSent(true)
            else setPhoneCodeSent(true)
            setSuccessMessage(channel === 'EMAIL'
                ? 'Đã gửi mã đến email của bạn.'
                : 'Đã gửi yêu cầu OTP đến Zalo theo số điện thoại đã đăng ký.')
        } catch (error) {
            setErrorMessage(error instanceof Error ? error.message : 'Không thể gửi mã xác minh.')
        } finally {
            setBusyChannel(null)
        }
    }

    async function verifyCode(channel: VerificationChannel) {
        setBusyChannel(channel)
        setErrorMessage(null)
        setSuccessMessage(null)
        try {
            const otp = channel === 'EMAIL' ? emailOtp : phoneOtp
            const updated = await verifyContactVerificationCode(channel, otp)
            onVerified(updated)
            if (channel === 'EMAIL') setEmailOtp('')
            else setPhoneOtp('')
            setSuccessMessage(channel === 'EMAIL' ? 'Email đã được xác minh.' : 'Số điện thoại đã được xác minh.')
        } catch (error) {
            setErrorMessage(error instanceof Error ? error.message : 'Mã xác minh không hợp lệ.')
        } finally {
            setBusyChannel(null)
        }
    }

    return (
        <section aria-label="Xác minh thông tin liên hệ" className="rounded-2xl border border-amber-200 bg-amber-50/70 p-4 sm:p-5">
            <div className="flex items-start gap-3">
                <div className="grid size-10 shrink-0 place-items-center rounded-xl bg-white text-amber-700 shadow-sm">
                    <ShieldCheck aria-hidden="true" size={20} />
                </div>
                <div>
                    <h3 className="font-bold text-slate-900">Xác minh trước khi ứng tuyển</h3>
                    <p className="mt-1 text-xs leading-5 text-slate-600">
                        Xác nhận email và số điện thoại để nhà tuyển dụng có thể liên hệ với bạn. Mã OTP chỉ dùng một lần.
                    </p>
                </div>
            </div>

            <div className="mt-4 grid gap-3 md:grid-cols-2">
                <VerificationChannelCard
                    title="Email"
                    destination={status.maskedEmail}
                    verified={status.emailVerified}
                    sent={emailCodeSent}
                    otp={emailOtp}
                    busy={busyChannel === 'EMAIL'}
                    icon={<Mail aria-hidden="true" size={16} />}
                    onOtpChange={setEmailOtp}
                    onRequest={() => void requestCode('EMAIL')}
                    onVerify={() => void verifyCode('EMAIL')}
                />
                <VerificationChannelCard
                    title="Số điện thoại qua Zalo"
                    destination={status.maskedPhone || 'Chưa có số điện thoại'}
                    verified={status.phoneVerified}
                    sent={phoneCodeSent}
                    otp={phoneOtp}
                    busy={busyChannel === 'PHONE'}
                    icon={<MessageCircle aria-hidden="true" size={16} />}
                    onOtpChange={setPhoneOtp}
                    onRequest={() => void requestCode('PHONE')}
                    onVerify={() => void verifyCode('PHONE')}
                />
            </div>

            {errorMessage && (
                <p className="mt-3 flex items-start gap-2 rounded-xl border border-rose-200 bg-white px-3 py-2 text-xs leading-5 text-rose-700" role="alert">
                    <CircleAlert aria-hidden="true" className="mt-0.5 shrink-0" size={15} />{errorMessage}
                </p>
            )}
            {successMessage && <p className="mt-3 text-xs font-semibold text-emerald-700" role="status">{successMessage}</p>}
        </section>
    )
}

function VerificationChannelCard({
    title,
    destination,
    verified,
    sent,
    otp,
    busy,
    icon,
    onOtpChange,
    onRequest,
    onVerify,
}: {
    title: string
    destination: string
    verified: boolean
    sent: boolean
    otp: string
    busy: boolean
    icon: React.ReactNode
    onOtpChange: (value: string) => void
    onRequest: () => void
    onVerify: () => void
}) {
    return (
        <div className="rounded-xl border border-slate-200 bg-white p-3.5">
            <div className="flex items-center justify-between gap-2">
                <p className="flex items-center gap-2 text-xs font-bold text-slate-800">{icon}{title}</p>
                {verified && <span className="inline-flex items-center gap-1 text-[11px] font-bold text-emerald-700"><BadgeCheck aria-hidden="true" size={14} />Đã xác minh</span>}
            </div>
            <p className="mt-1 truncate text-xs text-slate-500">{destination}</p>
            {!verified && (
                <>
                    {!sent ? (
                        <button className="mt-3 inline-flex min-h-9 w-full items-center justify-center gap-2 rounded-lg bg-blue-700 px-3 text-xs font-bold text-white hover:bg-blue-800 disabled:opacity-50" disabled={busy} onClick={onRequest} type="button">
                            <Send aria-hidden="true" size={14} />Gửi mã xác minh
                        </button>
                    ) : (
                        <div className="mt-3 flex gap-2">
                            <input
                                aria-label={`Mã OTP ${title}`}
                                autoComplete="one-time-code"
                                className="min-w-0 flex-1 rounded-lg border border-slate-300 px-3 text-sm tracking-[0.25em] outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-100"
                                inputMode="numeric"
                                maxLength={6}
                                onChange={(event) => onOtpChange(event.target.value.replace(/\D/g, '').slice(0, 6))}
                                placeholder="6 chữ số"
                                value={otp}
                            />
                            <button className="rounded-lg bg-blue-700 px-3 text-xs font-bold text-white hover:bg-blue-800 disabled:opacity-50" disabled={busy || otp.length !== 6} onClick={onVerify} type="button">Xác minh</button>
                        </div>
                    )}
                </>
            )}
        </div>
    )
}
