interface IllustrationProps {
    className?: string
}

export function HeroTalentIllustration({ className = 'w-full h-auto' }: IllustrationProps) {
    return (
        <svg
            aria-label="Minh họa hồ sơ ứng viên và cơ hội việc làm được kết nối"
            className={className}
            fill="none"
            role="img"
            viewBox="0 0 520 360"
            xmlns="http://www.w3.org/2000/svg"
        >
            <defs>
                <filter id="talent-card-shadow" height="150%" width="150%" x="-25%" y="-20%">
                    <feDropShadow dx="0" dy="18" floodColor="#172033" floodOpacity="0.12" stdDeviation="18" />
                </filter>
                <filter id="talent-float-shadow" height="160%" width="160%" x="-30%" y="-25%">
                    <feDropShadow dx="0" dy="10" floodColor="#172033" floodOpacity="0.12" stdDeviation="12" />
                </filter>
                <linearGradient id="talent-plane" x1="0" x2="1" y1="0" y2="1">
                    <stop stopColor="#eaf0fb" />
                    <stop offset="1" stopColor="#dbe5f6" />
                </linearGradient>
            </defs>

            <ellipse cx="267" cy="303" fill="#dce5f5" rx="213" ry="25" />
            <path d="M117 250 414 211l35 59-299 40-33-60Z" fill="url(#talent-plane)" />
            <path d="m117 250 297-39v22l-284 39-13-22Z" fill="#c8d5ec" />

            <g filter="url(#talent-card-shadow)" transform="rotate(-7 255 175)">
                <rect fill="#fff" height="214" rx="18" stroke="#e2e8f0" width="300" x="102" y="62" />
                <rect fill="#f8fafc" height="36" rx="10" width="268" x="118" y="78" />
                <circle cx="137" cy="96" r="6" fill="#1d4ed8" />
                <rect fill="#dbe3ef" height="7" rx="3.5" width="106" x="152" y="92" />
                <rect fill="#eff4ff" height="126" rx="13" width="82" x="120" y="130" />
                <circle cx="161" cy="169" r="20" fill="#c9d8f8" />
                <path d="M143 201c2-14 10-21 18-21s16 7 18 21" fill="#7294df" />
                <rect fill="#dbe3ef" height="7" rx="3.5" width="100" x="219" y="141" />
                <rect fill="#dbe3ef" height="7" rx="3.5" width="126" x="219" y="160" />
                <rect fill="#dbe3ef" height="7" rx="3.5" width="111" x="219" y="179" />
                <rect fill="#eff4ff" height="26" rx="8" width="123" x="219" y="206" />
                <rect fill="#1d4ed8" height="6" rx="3" width="72" x="232" y="216" />
            </g>

            <g filter="url(#talent-card-shadow)" transform="rotate(4 298 183)">
                <rect fill="#fff" height="226" rx="18" stroke="#dfe6f0" width="264" x="174" y="55" />
                <circle cx="210" cy="93" r="19" fill="#eff4ff" />
                <circle cx="210" cy="88" r="6" stroke="#1d4ed8" strokeWidth="2" />
                <path d="M199 104c1-7 5-10 11-10s10 3 11 10" stroke="#1d4ed8" strokeLinecap="round" strokeWidth="2" />
                <text fill="#172033" fontFamily="Be Vietnam Pro, sans-serif" fontSize="14" fontWeight="700" x="192" y="143">HỒ SƠ MẪU</text>
                <text fill="#475467" fontFamily="Be Vietnam Pro, sans-serif" fontSize="13" x="192" y="168">Backend Developer</text>
                <rect fill="#dbe3ef" height="7" rx="3.5" width="175" x="192" y="190" />
                <rect fill="#e4eaf3" height="7" rx="3.5" width="146" x="192" y="209" />
                <rect fill="#e4eaf3" height="7" rx="3.5" width="158" x="192" y="228" />
            </g>

            <circle cx="62" cy="156" r="15" fill="#1d4ed8" />
            <circle cx="456" cy="140" r="7" fill="#b9cdf7" />
            <g filter="url(#talent-float-shadow)">
                <rect fill="#fff" height="72" rx="14" width="190" x="296" y="248" />
                <rect fill="#eff4ff" height="38" rx="11" width="38" x="312" y="265" />
                <rect height="13" rx="2" stroke="#1d4ed8" strokeWidth="2" width="17" x="322" y="277" />
                <path d="M326 277v-3a4.5 4.5 0 0 1 9 0v3" stroke="#1d4ed8" strokeLinecap="round" strokeWidth="2" />
                <text fill="#172033" fontFamily="Be Vietnam Pro, sans-serif" fontSize="12" fontWeight="700" x="362" y="278">Cơ hội phù hợp</text>
                <text fill="#475467" fontFamily="Be Vietnam Pro, sans-serif" fontSize="10" x="362" y="296">Thông tin rõ ràng</text>
            </g>
        </svg>
    )
}
