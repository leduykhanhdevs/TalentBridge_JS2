interface IllustrationProps {
    className?: string
}

export function AuthIllustration({ className = 'w-full h-auto' }: IllustrationProps) {
    return (
        <svg
            className={className}
            fill="none"
            viewBox="0 0 420 320"
            xmlns="http://www.w3.org/2000/svg"
        >
            <defs>
                <linearGradient id="authBgGlow" x1="0%" x2="100%" y1="0%" y2="100%">
                    <stop offset="0%" stopColor="#eef2ff" />
                    <stop offset="100%" stopColor="#f8fafc" />
                </linearGradient>

                <linearGradient id="authCardGrad" x1="0%" x2="100%" y1="0%" y2="100%">
                    <stop offset="0%" stopColor="#ffffff" />
                    <stop offset="100%" stopColor="#f1f5f9" />
                </linearGradient>

                <linearGradient id="shieldGrad" x1="0%" x2="0%" y1="0%" y2="100%">
                    <stop offset="0%" stopColor="#4f46e5" />
                    <stop offset="100%" stopColor="#3730a3" />
                </linearGradient>

                <filter id="authShadow" height="130%" width="130%" x="-15%" y="-10%">
                    <feDropShadow dx="0" dy="8" floodColor="#0f172a" floodOpacity="0.08" stdDeviation="10" />
                </filter>
            </defs>

            {/* Ambient Background Box */}
            <rect
                fill="url(#authBgGlow)"
                height="300"
                rx="24"
                width="400"
                x="10"
                y="10"
            />

            {/* Subtle tech rings */}
            <circle cx="210" cy="160" opacity="0.3" r="110" stroke="#c7d2fe" strokeDasharray="4 4" strokeWidth="1.5" />
            <circle cx="210" cy="160" opacity="0.2" r="140" stroke="#c7d2fe" strokeDasharray="6 6" strokeWidth="1.5" />

            {/* Modern Central ID Card */}
            <g filter="url(#authShadow)">
                <rect
                    fill="url(#authCardGrad)"
                    height="200"
                    rx="20"
                    stroke="#e2e8f0"
                    strokeWidth="1.5"
                    width="260"
                    x="80"
                    y="60"
                />

                {/* Card Header Strip */}
                <path
                    d="M80 80 C80 69 89 60 100 60 L320 60 C331 60 340 69 340 80 L340 96 L80 96 Z"
                    fill="#4f46e5"
                />
                <circle cx="102" cy="78" fill="#ffffff" opacity="0.9" r="4" />
                <rect fill="#ffffff" height="6" opacity="0.8" rx="3" width="70" x="114" y="75" />

                {/* Developer Avatar Profile */}
                <circle cx="130" cy="140" fill="#e0e7ff" r="24" />
                <circle cx="130" cy="133" fill="#4f46e5" r="9" />
                <path d="M114 156 C114 148 121 146 130 146 C139 146 146 148 146 156 Z" fill="#4f46e5" />

                {/* User Credentials Info */}
                <rect fill="#0f172a" height="8" rx="4" width="80" x="170" y="125" />
                <rect fill="#64748b" height="6" rx="3" width="110" x="170" y="140" />
                <rect fill="#10b981" height="16" rx="8" width="75" x="170" y="155" />
                <text fill="#ffffff" fontSize="9" fontWeight="700" x="180" y="166">ROLE VERIFIED</text>

                {/* Card Footer Biometrics & Key */}
                <line stroke="#e2e8f0" strokeWidth="1" x1="100" x2="320" y1="190" y2="190" />
                {/* Fingerprint / Chip Motif */}
                <rect fill="#fef3c7" height="22" rx="4" width="30" x="105" y="202" />
                <rect fill="#f59e0b" height="2" width="22" x="109" y="208" />
                <rect fill="#f59e0b" height="2" width="22" x="109" y="213" />
                <rect fill="#f59e0b" height="2" width="22" x="109" y="218" />

                <rect fill="#94a3b8" height="5" rx="2.5" width="120" x="155" y="210" />
            </g>

            {/* Floating Security Shield Badge */}
            <g filter="url(#authShadow)" transform="translate(45, 180)">
                <circle cx="30" cy="30" fill="#ffffff" r="26" stroke="#e0e7ff" strokeWidth="1.5" />
                <path
                    d="M30 14 C30 14 42 16 42 26 C42 38 30 46 30 46 C30 46 18 38 18 26 C18 16 30 14 30 14 Z"
                    fill="url(#shieldGrad)"
                />
                <polyline
                    fill="none"
                    points="25,29 29,33 36,25"
                    stroke="#ffffff"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth="2.5"
                />
            </g>

            {/* Floating Star / OAuth2 Badge */}
            <g filter="url(#authShadow)" transform="translate(305, 45)">
                <rect fill="#ffffff" height="42" rx="12" stroke="#fde68a" strokeWidth="1.2" width="80" />
                <circle cx="20" cy="21" fill="#fef3c7" r="11" />
                <path d="M20 15 L22 19 L26 20 L23 23 L24 27 L20 25 L16 27 L17 23 L14 20 L18 19 Z" fill="#f59e0b" />
                <text fill="#0f172a" fontSize="10" fontWeight="700" x="38" y="20">JWT</text>
                <text fill="#10b981" fontSize="8" fontWeight="600" x="38" y="30">ACTIVE</text>
            </g>
        </svg>
    )
}
