interface IllustrationProps {
    className?: string
}

export function NotFoundIllustration({ className = 'w-48 h-auto' }: IllustrationProps) {
    return (
        <svg
            className={className}
            fill="none"
            viewBox="0 0 240 180"
            xmlns="http://www.w3.org/2000/svg"
        >
            <defs>
                <linearGradient id="nfGlow" x1="0%" x2="100%" y1="0%" y2="100%">
                    <stop offset="0%" stopColor="#e0e7ff" />
                    <stop offset="100%" stopColor="#f8fafc" />
                </linearGradient>
            </defs>

            {/* Ambient Background Bubble */}
            <rect fill="url(#nfGlow)" height="160" rx="24" width="220" x="10" y="10" />

            {/* Modern Floating Telescope / Radar Planet */}
            <circle cx="120" cy="85" fill="#eef2ff" r="48" />
            <circle cx="120" cy="85" opacity="0.4" r="60" stroke="#c7d2fe" strokeDasharray="4 4" strokeWidth="1.5" />

            {/* Big Sleek 404 Text */}
            <text fill="#4f46e5" fontSize="36" fontWeight="900" textAnchor="middle" x="120" y="70">404</text>
            <rect fill="#f59e0b" height="6" rx="3" width="24" x="108" y="78" />

            {/* Magnifier over missing link */}
            <g transform="translate(100, 95)">
                <circle cx="18" cy="18" fill="#ffffff" r="16" stroke="#4f46e5" strokeWidth="3" />
                <line stroke="#4f46e5" strokeLinecap="round" strokeWidth="4" x1="30" x2="42" y1="30" y2="42" />
                <line stroke="#f59e0b" strokeLinecap="round" strokeWidth="2" x1="12" x2="24" y1="18" y2="18" />
            </g>

            {/* Floating stars */}
            <circle cx="45" cy="40" fill="#f59e0b" r="3" />
            <circle cx="190" cy="45" fill="#6366f1" r="4" />
            <circle cx="195" cy="130" fill="#10b981" r="3.5" />
            <circle cx="50" cy="135" fill="#cbd5e1" r="2.5" />
        </svg>
    )
}
