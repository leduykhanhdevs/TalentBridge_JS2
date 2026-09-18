import React from 'react'

interface BadgeProps {
    className?: string
    size?: number
}

/**
 * Soft 3D Dimensional User / Candidate Avatar Badge
 */
export const Soft3DUserBadge: React.FC<BadgeProps> = ({ className = 'size-12', size = 48 }) => {
    return (
        <svg
            className={className}
            fill="none"
            height={size}
            viewBox="0 0 48 48"
            width={size}
            xmlns="http://www.w3.org/2000/svg"
        >
            <defs>
                <filter colorInterpolationFilters="sRGB" id="uShadow" x="-20%" y="-20%" width="140%" height="140%">
                    <feDropShadow dx="0" dy="4" stdDeviation="4" floodColor="#4f46e5" floodOpacity="0.25" />
                    <feDropShadow dx="0" dy="1" stdDeviation="1.5" floodColor="#0f172a" floodOpacity="0.1" />
                </filter>
                <linearGradient id="uGrad" x1="0" y1="0" x2="48" y2="48" gradientUnits="userSpaceOnUse">
                    <stop offset="0%" stopColor="#818cf8" />
                    <stop offset="50%" stopColor="#6366f1" />
                    <stop offset="100%" stopColor="#4338ca" />
                </linearGradient>
                <linearGradient id="uGloss" x1="12" y1="6" x2="36" y2="28" gradientUnits="userSpaceOnUse">
                    <stop offset="0%" stopColor="#ffffff" stopOpacity="0.5" />
                    <stop offset="100%" stopColor="#ffffff" stopOpacity="0" />
                </linearGradient>
            </defs>
            <g filter="url(#uShadow)">
                <rect x="4" y="4" width="40" height="40" rx="14" fill="url(#uGrad)" />
                <rect x="4.5" y="4.5" width="39" height="39" rx="13.5" stroke="url(#uGloss)" strokeWidth="1" />
                
                {/* 3D User Figure */}
                <circle cx="24" cy="18" r="6" fill="#ffffff" />
                <path d="M14 34 C14 27.5, 18.5 26, 24 26 C29.5 26, 34 27.5, 34 34 Z" fill="#ffffff" />
                
                {/* Highlight Sheen */}
                <path d="M8 8 C14 6, 34 6, 40 8 C40 8, 38 18, 24 18 C10 18, 8 8, 8 8 Z" fill="#ffffff" opacity="0.15" />
            </g>
        </svg>
    )
}

/**
 * Soft 3D Real-time Active Indicator Badge
 */
export const Soft3DActiveBadge: React.FC<BadgeProps> = ({ className = 'size-12', size = 48 }) => {
    return (
        <svg
            className={className}
            fill="none"
            height={size}
            viewBox="0 0 48 48"
            width={size}
            xmlns="http://www.w3.org/2000/svg"
        >
            <defs>
                <filter colorInterpolationFilters="sRGB" id="aShadow" x="-20%" y="-20%" width="140%" height="140%">
                    <feDropShadow dx="0" dy="4" stdDeviation="4" floodColor="#059669" floodOpacity="0.25" />
                    <feDropShadow dx="0" dy="1" stdDeviation="1.5" floodColor="#0f172a" floodOpacity="0.1" />
                </filter>
                <linearGradient id="aGrad" x1="0" y1="0" x2="48" y2="48" gradientUnits="userSpaceOnUse">
                    <stop offset="0%" stopColor="#34d399" />
                    <stop offset="50%" stopColor="#10b981" />
                    <stop offset="100%" stopColor="#047857" />
                </linearGradient>
                <linearGradient id="aGloss" x1="10" y1="6" x2="38" y2="28" gradientUnits="userSpaceOnUse">
                    <stop offset="0%" stopColor="#ffffff" stopOpacity="0.6" />
                    <stop offset="100%" stopColor="#ffffff" stopOpacity="0" />
                </linearGradient>
            </defs>
            <g filter="url(#aShadow)">
                <rect x="4" y="4" width="40" height="40" rx="14" fill="url(#aGrad)" />
                <rect x="4.5" y="4.5" width="39" height="39" rx="13.5" stroke="url(#aGloss)" strokeWidth="1" />
                
                {/* 3D Check Pulse */}
                <circle cx="24" cy="24" r="11" fill="#ffffff" opacity="0.2" />
                <circle cx="24" cy="24" r="8" fill="#ffffff" />
                <path d="M21 24 L23.5 26.5 L27 21.5" stroke="#059669" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" />
            </g>
        </svg>
    )
}

/**
 * Soft 3D Security Shield Badge (Lock / Restriction)
 */
export const Soft3DShieldBadge: React.FC<BadgeProps> = ({ className = 'size-12', size = 48 }) => {
    return (
        <svg
            className={className}
            fill="none"
            height={size}
            viewBox="0 0 48 48"
            width={size}
            xmlns="http://www.w3.org/2000/svg"
        >
            <defs>
                <filter colorInterpolationFilters="sRGB" id="sShadow" x="-20%" y="-20%" width="140%" height="140%">
                    <feDropShadow dx="0" dy="4" stdDeviation="4" floodColor="#e11d48" floodOpacity="0.22" />
                    <feDropShadow dx="0" dy="1" stdDeviation="1.5" floodColor="#0f172a" floodOpacity="0.1" />
                </filter>
                <linearGradient id="sGrad" x1="0" y1="0" x2="48" y2="48" gradientUnits="userSpaceOnUse">
                    <stop offset="0%" stopColor="#fb7185" />
                    <stop offset="50%" stopColor="#f43f5e" />
                    <stop offset="100%" stopColor="#be123c" />
                </linearGradient>
                <linearGradient id="sGloss" x1="12" y1="6" x2="36" y2="28" gradientUnits="userSpaceOnUse">
                    <stop offset="0%" stopColor="#ffffff" stopOpacity="0.5" />
                    <stop offset="100%" stopColor="#ffffff" stopOpacity="0" />
                </linearGradient>
            </defs>
            <g filter="url(#sShadow)">
                <rect x="4" y="4" width="40" height="40" rx="14" fill="url(#sGrad)" />
                <rect x="4.5" y="4.5" width="39" height="39" rx="13.5" stroke="url(#sGloss)" strokeWidth="1" />
                
                {/* 3D Shield & Lock */}
                <path
                    d="M24 13 C24 13, 31 16, 32 19 C32 26, 28 31, 24 33 C20 31, 16 26, 16 19 C17 16, 24 13, 24 13 Z"
                    fill="#ffffff"
                    opacity="0.95"
                />
                {/* Lock body */}
                <rect x="21" y="22" width="6" height="5" rx="1.5" fill="#be123c" />
                <path d="M22.5 22 V20.5 C22.5 19.7 23.2 19 24 19 C24.8 19 25.5 19.7 25.5 20.5 V22" stroke="#be123c" strokeWidth="1.2" strokeLinecap="round" />
            </g>
        </svg>
    )
}

/**
 * Soft 3D Verified Rosette Badge (ATS TopCV Certified)
 */
export const Soft3DVerifiedBadge: React.FC<BadgeProps> = ({ className = 'size-12', size = 48 }) => {
    return (
        <svg
            className={className}
            fill="none"
            height={size}
            viewBox="0 0 48 48"
            width={size}
            xmlns="http://www.w3.org/2000/svg"
        >
            <defs>
                <filter colorInterpolationFilters="sRGB" id="vShadow" x="-20%" y="-20%" width="140%" height="140%">
                    <feDropShadow dx="0" dy="4" stdDeviation="4" floodColor="#d97706" floodOpacity="0.25" />
                    <feDropShadow dx="0" dy="1" stdDeviation="1.5" floodColor="#0f172a" floodOpacity="0.1" />
                </filter>
                <linearGradient id="vGrad" x1="0" y1="0" x2="48" y2="48" gradientUnits="userSpaceOnUse">
                    <stop offset="0%" stopColor="#fde047" />
                    <stop offset="50%" stopColor="#fbbf24" />
                    <stop offset="100%" stopColor="#d97706" />
                </linearGradient>
                <linearGradient id="vGloss" x1="12" y1="6" x2="36" y2="28" gradientUnits="userSpaceOnUse">
                    <stop offset="0%" stopColor="#ffffff" stopOpacity="0.6" />
                    <stop offset="100%" stopColor="#ffffff" stopOpacity="0" />
                </linearGradient>
            </defs>
            <g filter="url(#vShadow)">
                <rect x="4" y="4" width="40" height="40" rx="14" fill="url(#vGrad)" />
                <rect x="4.5" y="4.5" width="39" height="39" rx="13.5" stroke="url(#vGloss)" strokeWidth="1" />
                
                {/* 3D Star / Sparkle */}
                <path
                    d="M24 14 L26.5 21 L34 21.5 L28 26 L30 33 L24 29 L18 33 L20 26 L14 21.5 L21.5 21 Z"
                    fill="#ffffff"
                />
            </g>
        </svg>
    )
}

/**
 * Soft 3D Enterprise / Building Badge
 */
export const Soft3DBuildingBadge: React.FC<BadgeProps> = ({ className = 'size-12', size = 48 }) => {
    return (
        <svg
            className={className}
            fill="none"
            height={size}
            viewBox="0 0 48 48"
            width={size}
            xmlns="http://www.w3.org/2000/svg"
        >
            <defs>
                <filter colorInterpolationFilters="sRGB" id="bShadow" x="-20%" y="-20%" width="140%" height="140%">
                    <feDropShadow dx="0" dy="4" stdDeviation="4" floodColor="#0284c7" floodOpacity="0.25" />
                    <feDropShadow dx="0" dy="1" stdDeviation="1.5" floodColor="#0f172a" floodOpacity="0.1" />
                </filter>
                <linearGradient id="bGrad" x1="0" y1="0" x2="48" y2="48" gradientUnits="userSpaceOnUse">
                    <stop offset="0%" stopColor="#38bdf8" />
                    <stop offset="50%" stopColor="#0284c7" />
                    <stop offset="100%" stopColor="#0369a1" />
                </linearGradient>
                <linearGradient id="bGloss" x1="12" y1="6" x2="36" y2="28" gradientUnits="userSpaceOnUse">
                    <stop offset="0%" stopColor="#ffffff" stopOpacity="0.5" />
                    <stop offset="100%" stopColor="#ffffff" stopOpacity="0" />
                </linearGradient>
            </defs>
            <g filter="url(#bShadow)">
                <rect x="4" y="4" width="40" height="40" rx="14" fill="url(#bGrad)" />
                <rect x="4.5" y="4.5" width="39" height="39" rx="13.5" stroke="url(#bGloss)" strokeWidth="1" />
                
                {/* 3D Modern Office Tower */}
                <rect x="14" y="14" width="20" height="20" rx="3" fill="#ffffff" />
                <rect x="18" y="18" width="3" height="3" rx="0.8" fill="#0284c7" />
                <rect x="23" y="18" width="3" height="3" rx="0.8" fill="#0284c7" />
                <rect x="27" y="18" width="3" height="3" rx="0.8" fill="#0284c7" />
                <rect x="18" y="24" width="3" height="3" rx="0.8" fill="#0284c7" />
                <rect x="23" y="24" width="3" height="3" rx="0.8" fill="#0284c7" />
                <rect x="27" y="24" width="3" height="3" rx="0.8" fill="#0284c7" />
                <rect x="22" y="30" width="4" height="4" rx="1" fill="#0369a1" />
            </g>
        </svg>
    )
}
