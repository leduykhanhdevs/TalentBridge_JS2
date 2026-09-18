interface IllustrationProps {
    className?: string
}

export function EmptyStateIllustration({ className = 'w-24 h-auto' }: IllustrationProps) {
    return (
        <svg
            className={className}
            fill="none"
            viewBox="0 0 120 100"
            xmlns="http://www.w3.org/2000/svg"
        >
            <defs>
                <linearGradient id="folderBack" x1="0%" x2="0%" y1="0%" y2="100%">
                    <stop offset="0%" stopColor="#c7d2fe" />
                    <stop offset="100%" stopColor="#a5b4fc" />
                </linearGradient>
                <linearGradient id="folderFront" x1="0%" x2="0%" y1="0%" y2="100%">
                    <stop offset="0%" stopColor="#e0e7ff" />
                    <stop offset="100%" stopColor="#c7d2fe" />
                </linearGradient>
            </defs>

            {/* Ambient Shadow */}
            <ellipse cx="60" cy="85" fill="#e2e8f0" rx="42" ry="6" />

            {/* Folder Back Tab */}
            <path
                d="M20 28 L40 28 L48 34 L96 34 C98 34 100 36 100 38 L100 75 C100 78 98 80 95 80 L25 80 C22 80 20 78 20 75 Z"
                fill="url(#folderBack)"
            />

            {/* White Sheet inside Folder */}
            <rect fill="#ffffff" height="38" rx="4" width="64" x="28" y="36" />
            <rect fill="#e2e8f0" height="3" rx="1.5" width="36" x="35" y="44" />
            <rect fill="#e2e8f0" height="3" rx="1.5" width="48" x="35" y="52" />

            {/* Folder Front Flap */}
            <path
                d="M18 46 C18 43 20 41 23 41 L97 41 C100 41 102 43 102 46 L98 78 C98 81 95 83 92 83 L28 83 C25 83 22 81 22 78 Z"
                fill="url(#folderFront)"
            />

            {/* Clean Flat Magnifying Glass */}
            <g transform="translate(56, 32)">
                <circle cx="24" cy="24" fill="#ffffff" r="14" stroke="#f59e0b" strokeWidth="2.5" />
                <circle cx="21" cy="21" fill="#fef3c7" r="4" />
                <line
                    stroke="#f59e0b"
                    strokeLinecap="round"
                    strokeWidth="3.5"
                    x1="34"
                    x2="45"
                    y1="34"
                    y2="45"
                />
            </g>
        </svg>
    )
}
