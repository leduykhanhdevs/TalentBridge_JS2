interface IllustrationProps {
    className?: string
}

export function DatabaseBentoIllustration({ className = 'size-12' }: IllustrationProps) {
    return (
        <svg
            className={className}
            fill="none"
            viewBox="0 0 64 64"
            xmlns="http://www.w3.org/2000/svg"
        >
            <rect fill="#e0e7ff" height="60" rx="16" width="60" x="2" y="2" />
            {/* Top Disk */}
            <ellipse cx="32" cy="18" fill="#4f46e5" rx="18" ry="6" />
            {/* Mid Disk */}
            <path d="M14 18 V28 C14 31.3 22 34 32 34 C42 34 50 31.3 50 28 V18" fill="#6366f1" />
            <ellipse cx="32" cy="28" fill="#818cf8" rx="18" ry="5.5" />
            {/* Bottom Disk */}
            <path d="M14 28 V38 C14 41.3 22 44 32 44 C42 44 50 41.3 50 38 V28" fill="#4338ca" />
            <ellipse cx="32" cy="38" fill="#6366f1" rx="18" ry="5.5" />
            {/* Green Light */}
            <circle cx="44" cy="46" fill="#10b981" r="3" />
        </svg>
    )
}
