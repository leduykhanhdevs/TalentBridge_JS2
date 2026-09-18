interface IllustrationProps {
    className?: string
}

export function AtsPipelineIllustration({ className = 'size-12' }: IllustrationProps) {
    return (
        <svg
            className={className}
            fill="none"
            viewBox="0 0 64 64"
            xmlns="http://www.w3.org/2000/svg"
        >
            <rect fill="#fef3c7" height="60" rx="16" width="60" x="2" y="2" />
            {/* Funnel Layers */}
            <path d="M14 16 L50 16 L44 26 L20 26 Z" fill="#f59e0b" />
            <path d="M20 28 L44 28 L38 38 L26 38 Z" fill="#d97706" />
            <path d="M26 40 L38 40 L35 50 L29 50 Z" fill="#b45309" />
            {/* Checkmark bubble */}
            <circle cx="46" cy="46" fill="#10b981" r="9" />
            <polyline fill="none" points="42,46 45,49 50,43" stroke="#ffffff" strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" />
        </svg>
    )
}
