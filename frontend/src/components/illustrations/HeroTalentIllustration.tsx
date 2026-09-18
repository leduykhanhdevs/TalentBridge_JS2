interface IllustrationProps {
    className?: string
}

export function HeroTalentIllustration({ className = 'w-full h-auto' }: IllustrationProps) {
    return (
        <svg
            className={className}
            fill="none"
            viewBox="0 0 520 350"
            xmlns="http://www.w3.org/2000/svg"
        >
            <defs>
                <linearGradient id="heroBgGlow" x1="0%" x2="100%" y1="0%" y2="100%">
                    <stop offset="0%" stopColor="#eef2ff" stopOpacity="0.8" />
                    <stop offset="50%" stopColor="#f0fdf4" stopOpacity="0.5" />
                    <stop offset="100%" stopColor="#fef3c7" stopOpacity="0.6" />
                </linearGradient>

                <linearGradient id="laptopScreenGrad" x1="0%" x2="100%" y1="0%" y2="100%">
                    <stop offset="0%" stopColor="#1e1b4b" />
                    <stop offset="100%" stopColor="#0f172a" />
                </linearGradient>

                <linearGradient id="cardGrad" x1="0%" x2="0%" y1="0%" y2="100%">
                    <stop offset="0%" stopColor="#ffffff" />
                    <stop offset="100%" stopColor="#f8fafc" />
                </linearGradient>

                <linearGradient id="primaryGrad" x1="0%" x2="100%" y1="0%" y2="0%">
                    <stop offset="0%" stopColor="#4f46e5" />
                    <stop offset="100%" stopColor="#6366f1" />
                </linearGradient>

                <filter id="softShadow" height="130%" width="130%" x="-15%" y="-10%">
                    <feDropShadow dx="0" dy="6" floodColor="#0f172a" floodOpacity="0.07" stdDeviation="8" />
                </filter>

                <filter id="cardShadow" height="130%" width="130%" x="-15%" y="-10%">
                    <feDropShadow dx="0" dy="4" floodColor="#0f172a" floodOpacity="0.06" stdDeviation="6" />
                </filter>
            </defs>

            {/* Background Ambient Aura */}
            <rect
                fill="url(#heroBgGlow)"
                height="320"
                rx="24"
                width="490"
                x="15"
                y="15"
            />

            {/* Subtle tech grid mesh in background */}
            <g opacity="0.25">
                <line stroke="#c7d2fe" strokeDasharray="3 3" x1="45" x2="475" y1="80" y2="80" />
                <line stroke="#c7d2fe" strokeDasharray="3 3" x1="45" x2="475" y1="150" y2="150" />
                <line stroke="#c7d2fe" strokeDasharray="3 3" x1="45" x2="475" y1="220" y2="220" />
                <line stroke="#c7d2fe" strokeDasharray="3 3" x1="120" x2="120" y1="40" y2="290" />
                <line stroke="#c7d2fe" strokeDasharray="3 3" x1="260" x2="260" y1="40" y2="290" />
                <line stroke="#c7d2fe" strokeDasharray="3 3" x1="400" x2="400" y1="40" y2="290" />
            </g>

            {/* Modern Desk Surface */}
            <rect fill="#e2e8f0" height="8" rx="4" width="380" x="70" y="270" />
            <rect fill="#cbd5e1" height="4" rx="2" width="400" x="60" y="278" />

            {/* Modern Laptop Workstation */}
            <g filter="url(#softShadow)">
                {/* Laptop Base */}
                <path d="M120 270 L300 270 L310 275 L110 275 Z" fill="#94a3b8" />
                <rect fill="#cbd5e1" height="4" rx="2" width="70" x="175" y="271" />

                {/* Laptop Screen Body */}
                <rect fill="#0f172a" height="135" rx="10" width="180" x="120" y="135" />
                <rect fill="url(#laptopScreenGrad)" height="121" rx="6" width="168" x="126" y="141" />

                {/* Screen Top Bar */}
                <rect fill="#1e293b" height="14" rx="4" width="168" x="126" y="141" />
                <circle cx="134" cy="148" fill="#f43f5e" r="2.5" />
                <circle cx="142" cy="148" fill="#f59e0b" r="2.5" />
                <circle cx="150" cy="148" fill="#10b981" r="2.5" />
                <rect fill="#334155" height="6" rx="3" width="50" x="170" y="145" />

                {/* Screen Code & Analytics Content */}
                <rect fill="#818cf8" height="4" rx="2" width="28" x="136" y="165" />
                <rect fill="#38bdf8" height="4" rx="2" width="42" x="168" y="165" />
                <rect fill="#64748b" height="4" rx="2" width="22" x="136" y="174" />
                <rect fill="#34d399" height="4" rx="2" width="55" x="162" y="174" />
                <rect fill="#818cf8" height="4" rx="2" width="34" x="136" y="183" />
                <rect fill="#fbbf24" height="4" rx="2" width="30" x="174" y="183" />

                {/* Mini Graph inside Laptop */}
                <rect fill="#1e293b" height="44" rx="6" width="60" x="224" y="165" />
                <polyline
                    fill="none"
                    points="230,198 242,186 254,192 266,176 278,172"
                    stroke="#10b981"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth="2.5"
                />
                <circle cx="278" cy="172" fill="#10b981" r="3" />

                {/* Status Bar */}
                <rect fill="#10b981" height="6" rx="3" width="18" x="136" y="246" />
                <rect fill="#64748b" height="4" rx="2" width="60" x="160" y="247" />
            </g>

            {/* Coffee Mug on Desk */}
            <g>
                <rect fill="#ffffff" height="24" rx="4" width="18" x="85" y="246" />
                <rect fill="#6366f1" height="20" rx="3" width="16" x="86" y="248" />
                <path d="M101 251 C106 251 106 261 101 261" fill="none" stroke="#6366f1" strokeWidth="2.5" />
                <path d="M91 241 C89 238 93 236 91 233" fill="none" opacity="0.6" stroke="#cbd5e1" strokeLinecap="round" strokeWidth="1.5" />
                <path d="M95 241 C97 238 94 236 96 233" fill="none" opacity="0.6" stroke="#cbd5e1" strokeLinecap="round" strokeWidth="1.5" />
            </g>

            {/* Modern Potted Desk Plant */}
            <g>
                <polygon fill="#f59e0b" points="310,270 330,270 326,252 314,252" />
                <path d="M320 252 C312 244 314 230 320 224 C326 230 328 244 320 252 Z" fill="#10b981" />
                <path d="M320 248 C328 244 336 234 336 226 C330 226 322 238 320 248 Z" fill="#34d399" />
                <path d="M320 248 C312 244 304 234 304 226 C310 226 318 238 320 248 Z" fill="#059669" />
            </g>

            {/* Storyset Character: Modern IT Professional */}
            <g filter="url(#softShadow)">
                <path
                    d="M340 270 C340 225 352 210 375 205 C398 210 410 225 410 270 Z"
                    fill="url(#primaryGrad)"
                />
                <polygon fill="#ffffff" points="375,218 368,205 382,205" />
                <rect fill="#fed7aa" height="14" rx="3" width="16" x="367" y="196" />
                <circle cx="375" cy="176" fill="#fed7aa" r="22" />

                <path
                    d="M353 174 C353 156 364 148 380 148 C396 148 399 158 398 168 C394 167 388 165 382 169 C376 173 371 166 366 170 C361 174 357 172 353 174 Z"
                    fill="#1e293b"
                />

                <rect fill="none" height="10" rx="3" stroke="#0f172a" strokeWidth="2" width="14" x="362" y="172" />
                <rect fill="none" height="10" rx="3" stroke="#0f172a" strokeWidth="2" width="14" x="378" y="172" />
                <line stroke="#0f172a" strokeWidth="2" x1="376" x2="378" y1="177" y2="177" />

                <path d="M372 188 C375 190 379 190 382 188" fill="none" stroke="#ea580c" strokeLinecap="round" strokeWidth="1.8" />
                <circle cx="363" cy="184" fill="#fbcfe8" opacity="0.6" r="3" />
                <circle cx="389" cy="184" fill="#fbcfe8" opacity="0.6" r="3" />

                <path
                    d="M352 230 C332 238 310 252 290 262"
                    fill="none"
                    stroke="#4338ca"
                    strokeLinecap="round"
                    strokeWidth="14"
                />
                <circle cx="286" cy="264" fill="#fed7aa" r="6" />
            </g>

            {/* FLOATING MODERN BENTO CARD 1: TopCV Match 98% */}
            <g filter="url(#cardShadow)">
                <rect
                    fill="url(#cardGrad)"
                    height="70"
                    rx="14"
                    stroke="#e0e7ff"
                    strokeWidth="1.2"
                    width="170"
                    x="40"
                    y="45"
                />
                <circle cx="68" cy="74" fill="#e0e7ff" r="16" />
                <path d="M68 64 A6 6 0 1 1 68 76 A6 6 0 1 1 68 64 Z" fill="#6366f1" />
                <path d="M57 84 C57 78 62 76 68 76 C74 76 79 78 79 84 Z" fill="#6366f1" />

                <rect fill="#dcfce7" height="15" rx="7.5" width="84" x="92" y="58" />
                <text fill="#15803d" fontSize="9" fontWeight="700" x="98" y="69">TOPCV VERIFIED</text>

                <text fill="#0f172a" fontSize="13" fontWeight="800" x="92" y="88">Độ khớp: 98.5%</text>
                <circle cx="188" cy="80" fill="#f59e0b" r="4" />
                <path d="M188 77 L189 79 L191 80 L189 81 L188 83 L187 81 L185 80 L187 79 Z" fill="#ffffff" />
            </g>

            {/* FLOATING MODERN BENTO CARD 2: 5-Stage ATS Pipeline */}
            <g filter="url(#cardShadow)">
                <rect
                    fill="url(#cardGrad)"
                    height="72"
                    rx="14"
                    stroke="#e2e8f0"
                    strokeWidth="1.2"
                    width="165"
                    x="315"
                    y="42"
                />
                <rect fill="#fef3c7" height="28" rx="8" width="28" x="328" y="54" />
                <path d="M336 62 L348 62 L344 68 L344 74 L340 74 L340 68 Z" fill="#f59e0b" />

                <text fill="#64748b" fontSize="9" fontWeight="700" x="364" y="64">ATS SMART PIPELINE</text>
                <text fill="#0f172a" fontSize="12" fontWeight="800" x="364" y="79">Vòng 4: Đậu PV</text>

                <g transform="translate(364, 86)">
                    <rect fill="#10b981" height="4" rx="2" width="18" x="0" y="0" />
                    <rect fill="#10b981" height="4" rx="2" width="18" x="22" y="0" />
                    <rect fill="#10b981" height="4" rx="2" width="18" x="44" y="0" />
                    <rect fill="#10b981" height="4" rx="2" width="18" x="66" y="0" />
                    <rect fill="#e2e8f0" height="4" rx="2" width="18" x="88" y="0" />
                </g>
            </g>

            {/* FLOATING MODERN BENTO CARD 3: Java 21 & Spring Boot 3 */}
            <g filter="url(#cardShadow)">
                <rect
                    fill="#ffffff"
                    height="50"
                    rx="12"
                    stroke="#cbd5e1"
                    strokeWidth="1"
                    width="135"
                    x="345"
                    y="130"
                />
                <circle cx="363" cy="155" fill="#e0e7ff" r="11" />
                <path d="M360 152 L363 150 L366 152 L366 157 L363 159 L360 157 Z" fill="#4f46e5" />
                <text fill="#0f172a" fontSize="10" fontWeight="700" x="382" y="151">Java 21 + Spring 3</text>
                <text fill="#10b981" fontSize="9" fontWeight="600" x="382" y="164">27 Bảng Chuẩn 3NF</text>
            </g>
        </svg>
    )
}
