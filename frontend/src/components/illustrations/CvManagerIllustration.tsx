interface IllustrationProps {
    className?: string
}

export function CvManagerIllustration({ className = 'size-12' }: IllustrationProps) {
    return (
        <svg
            className={className}
            fill="none"
            viewBox="0 0 64 64"
            xmlns="http://www.w3.org/2000/svg"
        >
            <rect fill="#e0f2fe" height="60" rx="16" width="60" x="2" y="2" />
            {/* CV Paper Card */}
            <rect fill="#ffffff" height="42" rx="6" width="32" x="16" y="11" />
            {/* Avatar on CV */}
            <circle cx="23" cy="19" fill="#0ea5e9" r="4" />
            <rect fill="#0ea5e9" height="3" rx="1.5" width="12" x="30" y="17" />
            <rect fill="#94a3b8" height="2" rx="1" width="18" x="22" y="27" />
            <rect fill="#94a3b8" height="2" rx="1" width="20" x="22" y="32" />
            <rect fill="#94a3b8" height="2" rx="1" width="14" x="22" y="37" />
            {/* Gold Star Badge */}
            <circle cx="44" cy="43" fill="#f59e0b" r="8" />
            <path d="M44 38 L45.5 41.5 L49 42 L46.5 44.5 L47 48 L44 46 L41 48 L41.5 44.5 L39 42 L42.5 41.5 Z" fill="#ffffff" />
        </svg>
    )
}
