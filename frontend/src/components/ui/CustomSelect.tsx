import { Check, ChevronDown } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'

export interface SelectOption {
    value: string
    label: string
}

interface CustomSelectProps {
    value: string
    onChange: (value: string) => void
    options: SelectOption[]
    placeholder?: string
    className?: string
}

export function CustomSelect({
    value,
    onChange,
    options,
    placeholder = 'Chọn...',
    className = '',
}: CustomSelectProps) {
    const [isOpen, setIsOpen] = useState(false)
    const containerRef = useRef<HTMLDivElement>(null)

    const selectedOption = options.find((opt) => opt.value === value)

    useEffect(() => {
        function handleClickOutside(event: MouseEvent) {
            if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
                setIsOpen(false)
            }
        }
        document.addEventListener('mousedown', handleClickOutside)
        return () => document.removeEventListener('mousedown', handleClickOutside)
    }, [])

    useEffect(() => {
        function handleKeyDown(event: KeyboardEvent) {
            if (event.key === 'Escape') {
                setIsOpen(false)
            }
        }
        if (isOpen) {
            document.addEventListener('keydown', handleKeyDown)
            return () => document.removeEventListener('keydown', handleKeyDown)
        }
    }, [isOpen])

    return (
        <div className={`relative ${className}`} ref={containerRef}>
            <button
                type="button"
                onClick={() => setIsOpen((prev) => !prev)}
                aria-expanded={isOpen}
                aria-haspopup="listbox"
                className="mt-1.5 flex w-full items-center justify-between rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-left text-sm text-slate-800 shadow-2xs transition hover:border-slate-300 focus:border-indigo-500 focus:outline-none focus:ring-3 focus:ring-indigo-100"
            >
                <span className={selectedOption ? 'font-medium text-slate-900' : 'text-slate-500'}>
                    {selectedOption ? selectedOption.label : placeholder}
                </span>
                <ChevronDown
                    aria-hidden="true"
                    className={`size-4 text-slate-400 transition-transform duration-200 ${
                        isOpen ? 'rotate-180 text-indigo-600' : ''
                    }`}
                />
            </button>

            {isOpen && (
                <div
                    role="listbox"
                    className="absolute z-40 mt-1.5 w-full rounded-2xl border border-slate-200 bg-white/95 p-1.5 shadow-xl shadow-slate-200/50 backdrop-blur-md"
                >
                    <ul className="max-h-60 overflow-y-auto space-y-0.5">
                        {options.map((option) => {
                            const isSelected = option.value === value
                            return (
                                <li key={option.value || 'all'} role="option" aria-selected={isSelected}>
                                    <button
                                        type="button"
                                        onClick={() => {
                                            onChange(option.value)
                                            setIsOpen(false)
                                        }}
                                        className={`flex w-full items-center justify-between rounded-xl px-3 py-2 text-sm font-medium transition-colors ${
                                            isSelected
                                                ? 'bg-indigo-600 text-white shadow-xs'
                                                : 'text-slate-700 hover:bg-indigo-50 hover:text-indigo-900'
                                        }`}
                                    >
                                        <span>{option.label}</span>
                                        {isSelected && <Check className="size-4 shrink-0 text-white" aria-hidden="true" />}
                                    </button>
                                </li>
                            )
                        })}
                    </ul>
                </div>
            )}
        </div>
    )
}
