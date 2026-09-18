import React from 'react'

interface IllustrationProps {
    className?: string
}

export const AdminControlCenterIllustration: React.FC<IllustrationProps> = ({
    className = 'w-full h-auto max-w-[340px]',
}) => {
    return (
        <svg
            className={className}
            fill="none"
            viewBox="0 0 380 240"
            xmlns="http://www.w3.org/2000/svg"
        >
            <defs>
                {/* Ambient Soft Glow */}
                <filter colorInterpolationFilters="sRGB" id="adminAmbientGlow" x="-20%" y="-20%" width="140%" height="140%">
                    <feGaussianBlur stdDeviation="16" result="blur" />
                </filter>

                {/* Soft 3D Drop Shadows */}
                <filter colorInterpolationFilters="sRGB" id="adminCardShadow" x="-10%" y="-10%" width="125%" height="130%">
                    <feDropShadow dx="0" dy="8" stdDeviation="10" floodColor="#0f172a" floodOpacity="0.1" />
                    <feDropShadow dx="0" dy="2" stdDeviation="3" floodColor="#0f172a" floodOpacity="0.06" />
                </filter>

                <filter colorInterpolationFilters="sRGB" id="adminBadgeShadow" x="-20%" y="-20%" width="140%" height="150%">
                    <feDropShadow dx="0" dy="6" stdDeviation="6" floodColor="#4f46e5" floodOpacity="0.2" />
                </filter>

                {/* Gradients */}
                <linearGradient id="adminScreenBg" x1="0" y1="0" x2="320" y2="180" gradientUnits="userSpaceOnUse">
                    <stop offset="0%" stopColor="#ffffff" />
                    <stop offset="100%" stopColor="#f1f5f9" />
                </linearGradient>

                <linearGradient id="adminPrimaryGrad" x1="0" y1="0" x2="1" y2="1">
                    <stop offset="0%" stopColor="#6366f1" />
                    <stop offset="100%" stopColor="#4338ca" />
                </linearGradient>

                <linearGradient id="adminAmberGrad" x1="0" y1="0" x2="1" y2="1">
                    <stop offset="0%" stopColor="#fbbf24" />
                    <stop offset="100%" stopColor="#d97706" />
                </linearGradient>

                <linearGradient id="adminEmeraldGrad" x1="0" y1="0" x2="1" y2="1">
                    <stop offset="0%" stopColor="#34d399" />
                    <stop offset="100%" stopColor="#059669" />
                </linearGradient>

                <linearGradient id="adminShieldGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="#818cf8" />
                    <stop offset="100%" stopColor="#4f46e5" />
                </linearGradient>
            </defs>

            {/* Ambient Background Aura */}
            <circle cx="190" cy="120" r="85" fill="#e0e7ff" filter="url(#adminAmbientGlow)" opacity="0.75" />
            <circle cx="280" cy="80" r="50" fill="#fef3c7" filter="url(#adminAmbientGlow)" opacity="0.6" />

            {/* Ground Elevation Grid Rings */}
            <ellipse cx="190" cy="200" rx="140" ry="24" fill="#e2e8f0" opacity="0.4" />
            <ellipse cx="190" cy="198" rx="110" ry="18" fill="#f1f5f9" opacity="0.6" />

            {/* Main Console Board (Soft 3D Angled Screen) */}
            <g filter="url(#adminCardShadow)">
                <rect x="55" y="45" width="270" height="150" rx="20" fill="url(#adminScreenBg)" stroke="#cbd5e1" strokeWidth="1.5" />
                
                {/* Console Header Bar */}
                <rect x="55" y="45" width="270" height="32" rx="20" fill="#f8fafc" />
                <path d="M55 77 L325 77" stroke="#e2e8f0" strokeWidth="1" />
                
                {/* Window Control Dots */}
                <circle cx="75" cy="61" r="4" fill="#f43f5e" />
                <circle cx="89" cy="61" r="4" fill="#fbbf24" />
                <circle cx="103" cy="61" r="4" fill="#10b981" />

                {/* Console Title Pill */}
                <rect x="135" y="53" width="110" height="16" rx="8" fill="#e0e7ff" opacity="0.8" />
                <rect x="150" y="58" width="80" height="6" rx="3" fill="#6366f1" opacity="0.7" />

                {/* Left Panel: Mini Candidate Analytics Chart */}
                <rect x="75" y="93" width="115" height="85" rx="12" fill="#ffffff" stroke="#e2e8f0" strokeWidth="1" />
                <rect x="87" y="103" width="55" height="7" rx="3.5" fill="#64748b" opacity="0.8" />
                <rect x="87" y="115" width="40" height="5" rx="2.5" fill="#94a3b8" opacity="0.5" />
                
                {/* Mini Bar Chart Bars */}
                <rect x="87" y="152" width="12" height="18" rx="4" fill="#cbd5e1" />
                <rect x="105" y="142" width="12" height="28" rx="4" fill="#818cf8" />
                <rect x="123" y="132" width="12" height="38" rx="4" fill="#6366f1" />
                <rect x="141" y="124" width="12" height="46" rx="4" fill="url(#adminPrimaryGrad)" />
                <rect x="159" y="138" width="12" height="32" rx="4" fill="#a5b4fc" />

                {/* Right Panel: Metric Highlights */}
                <rect x="200" y="93" width="110" height="38" rx="10" fill="#ecfdf5" stroke="#a7f3d0" strokeWidth="1" />
                <circle cx="216" cy="112" r="7" fill="#10b981" />
                <path d="M213.5 112 L215.5 114 L219 110.5" stroke="#ffffff" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                <rect x="230" y="106" width="60" height="6" rx="3" fill="#065f46" />
                <rect x="230" y="115" width="40" height="4" rx="2" fill="#059669" opacity="0.8" />

                <rect x="200" y="140" width="110" height="38" rx="10" fill="#eef2ff" stroke="#c7d2fe" strokeWidth="1" />
                <circle cx="216" cy="159" r="7" fill="#6366f1" />
                <rect x="230" y="153" width="55" height="6" rx="3" fill="#3730a3" />
                <rect x="230" y="162" width="45" height="4" rx="2" fill="#4f46e5" opacity="0.8" />
            </g>

            {/* Floating Soft 3D Candidate ID Card (Left Foreground) */}
            <g filter="url(#adminCardShadow)">
                <rect x="25" y="105" width="110" height="72" rx="14" fill="#ffffff" stroke="#e0e7ff" strokeWidth="1.5" />
                {/* Avatar with Glow */}
                <circle cx="48" cy="132" r="14" fill="url(#adminPrimaryGrad)" />
                <circle cx="48" cy="128" r="5" fill="#ffffff" opacity="0.9" />
                <path d="M40 142 C40 137, 44 135, 48 135 C52 135, 56 137, 56 142 Z" fill="#ffffff" opacity="0.9" />
                
                {/* Active Online Indicator */}
                <circle cx="58" cy="142" r="4" fill="#10b981" stroke="#ffffff" strokeWidth="1.5" />

                {/* Candidate Info Lines */}
                <rect x="68" y="124" width="54" height="6" rx="3" fill="#1e293b" />
                <rect x="68" y="134" width="42" height="4.5" rx="2.2" fill="#6366f1" />
                
                {/* Skill Badge Pill */}
                <rect x="36" y="155" width="86" height="14" rx="7" fill="#f1f5f9" />
                <rect x="44" y="160" width="70" height="4" rx="2" fill="#64748b" opacity="0.7" />
            </g>

            {/* Floating Soft 3D Security Shield (Right Foreground) */}
            <g filter="url(#adminBadgeShadow)">
                <path
                    d="M305 130 C305 130, 328 120, 335 110 C335 142, 320 168, 305 178 C290 168, 275 142, 275 110 C282 120, 305 130, 305 130 Z"
                    fill="url(#adminShieldGrad)"
                />
                <path
                    d="M305 134 C305 134, 324 125, 330 116 C330 141, 318 163, 305 171 C292 163, 280 141, 280 116 C286 125, 305 134, 305 134 Z"
                    fill="#ffffff"
                    opacity="0.25"
                />
                {/* Lock Core */}
                <rect x="299" y="142" width="12" height="10" rx="2.5" fill="#ffffff" />
                <path d="M302 142 V138 C302 136.3 303.3 135 305 135 C306.7 135 308 136.3 308 138 V142" stroke="#ffffff" strokeWidth="1.8" strokeLinecap="round" />
            </g>

            {/* Floating Soft 3D Verified Check Badge (Top Right) */}
            <g filter="url(#adminBadgeShadow)">
                <circle cx="285" cy="40" r="16" fill="url(#adminAmberGrad)" />
                <circle cx="285" cy="40" r="12" fill="#ffffff" opacity="0.2" />
                <path d="M279 40 L283.5 44.5 L292 35.5" stroke="#ffffff" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" />
            </g>

            {/* Sparkles / Dynamic Ambient Particles */}
            <path d="M60 30 L62 35 L67 37 L62 39 L60 44 L58 39 L53 37 L58 35 Z" fill="#fbbf24" opacity="0.9" />
            <path d="M340 75 L341.5 78.5 L345 80 L341.5 81.5 L340 85 L338.5 81.5 L335 80 L338.5 78.5 Z" fill="#818cf8" opacity="0.8" />
            <circle cx="160" cy="30" r="2.5" fill="#a5b4fc" opacity="0.7" />
            <circle cx="210" cy="28" r="2" fill="#fbbf24" opacity="0.8" />
        </svg>
    )
}
