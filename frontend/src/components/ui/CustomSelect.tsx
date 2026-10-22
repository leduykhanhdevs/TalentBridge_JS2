import { Check, ChevronDown } from 'lucide-react'
import { useCallback, useEffect, useId, useRef, useState } from 'react'
import { createPortal } from 'react-dom'

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
    disabled?: boolean
    id?: string
    'aria-label'?: string
}

interface PopoverPosition {
    top: number
    left: number
    width: number
    maxHeight: number
}

export function CustomSelect({
    value,
    onChange,
    options,
    placeholder = 'Chọn...',
    className = '',
    disabled = false,
    id,
    'aria-label': ariaLabel,
}: CustomSelectProps) {
    const generatedId = useId()
    const listboxId = `${generatedId}-options`
    const [isOpen, setIsOpen] = useState(false)
    const [activeIndex, setActiveIndex] = useState(0)
    const [position, setPosition] = useState<PopoverPosition | null>(null)
    const triggerRef = useRef<HTMLButtonElement>(null)
    const listboxRef = useRef<HTMLDivElement>(null)
    const typeaheadRef = useRef('')

    const selectedIndex = Math.max(0, options.findIndex((option) => option.value === value))
    const selectedOption = options.find((option) => option.value === value)

    const updatePosition = useCallback(() => {
        const trigger = triggerRef.current
        if (!trigger) return
        const rect = trigger.getBoundingClientRect()
        const margin = 8
        const width = Math.min(rect.width, window.innerWidth - margin * 2)
        const desiredHeight = Math.min(280, window.innerHeight - margin * 2)
        const below = window.innerHeight - rect.bottom - margin
        const openBelow = below >= Math.min(200, desiredHeight)
        const maxHeight = openBelow ? Math.min(desiredHeight, below - 6) : Math.max(72, Math.min(desiredHeight, rect.top - margin * 2 - 6))
        const top = openBelow ? rect.bottom + 6 : Math.max(margin, rect.top - maxHeight - 6)
        const left = Math.min(Math.max(margin, rect.left), window.innerWidth - width - margin)
        setPosition({ top, left, width, maxHeight })
    }, [])

    function openListbox() {
        if (disabled || options.length === 0) return
        setActiveIndex(selectedIndex)
        updatePosition()
        setIsOpen(true)
    }

    const selectOption = useCallback((index: number) => {
        const option = options[index]
        if (!option) return
        onChange(option.value)
        setIsOpen(false)
        triggerRef.current?.focus()
    }, [onChange, options])

    useEffect(() => {
        if (!isOpen) return

        function handleOutsidePointer(event: PointerEvent) {
            const target = event.target as Node
            if (!triggerRef.current?.contains(target) && !listboxRef.current?.contains(target)) {
                setIsOpen(false)
            }
        }

        function handleKeyDown(event: KeyboardEvent) {
            if (event.key === 'Escape') {
                event.preventDefault()
                setIsOpen(false)
                triggerRef.current?.focus()
            } else if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
                event.preventDefault()
                setActiveIndex((index) => event.key === 'ArrowDown'
                    ? Math.min(index + 1, options.length - 1)
                    : Math.max(index - 1, 0))
            } else if (event.key === 'Home') {
                event.preventDefault()
                setActiveIndex(0)
            } else if (event.key === 'End') {
                event.preventDefault()
                setActiveIndex(Math.max(0, options.length - 1))
            } else if (event.key === 'Enter' || event.key === ' ') {
                event.preventDefault()
                selectOption(activeIndex)
            } else if (event.key === 'Tab') {
                setIsOpen(false)
            } else if (event.key.length === 1 && !event.ctrlKey && !event.metaKey && !event.altKey) {
                typeaheadRef.current += event.key.toLocaleLowerCase()
                const matchIndex = options.findIndex((option) => option.label.toLocaleLowerCase().startsWith(typeaheadRef.current))
                if (matchIndex >= 0) setActiveIndex(matchIndex)
                window.setTimeout(() => { typeaheadRef.current = '' }, 650)
            }
        }

        function keepPositionCurrent() {
            updatePosition()
        }

        document.addEventListener('pointerdown', handleOutsidePointer)
        document.addEventListener('keydown', handleKeyDown)
        window.addEventListener('resize', keepPositionCurrent)
        window.addEventListener('scroll', keepPositionCurrent, true)
        return () => {
            document.removeEventListener('pointerdown', handleOutsidePointer)
            document.removeEventListener('keydown', handleKeyDown)
            window.removeEventListener('resize', keepPositionCurrent)
            window.removeEventListener('scroll', keepPositionCurrent, true)
        }
    }, [isOpen, options, activeIndex, selectOption, updatePosition])

    useEffect(() => {
        if (!isOpen) return
        listboxRef.current?.querySelector<HTMLElement>(`[data-option-index="${activeIndex}"]`)
            ?.scrollIntoView?.({ block: 'nearest' })
    }, [activeIndex, isOpen])

    return (
        <div className={`relative min-w-0 ${className}`}>
            <button
                aria-activedescendant={isOpen ? `${listboxId}-${activeIndex}` : undefined}
                aria-controls={listboxId}
                aria-expanded={isOpen}
                aria-haspopup="listbox"
                aria-label={ariaLabel}
                className="flex min-h-11 w-full items-center justify-between gap-3 rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-left text-sm text-slate-800 shadow-sm outline-none transition hover:border-slate-300 focus:border-blue-600 focus:ring-3 focus:ring-blue-100 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-400"
                disabled={disabled}
                id={id}
                onClick={() => isOpen ? setIsOpen(false) : openListbox()}
                ref={triggerRef}
                role="combobox"
                type="button"
            >
                <span className={`truncate ${selectedOption ? 'font-medium text-slate-900' : 'text-slate-500'}`}>
                    {selectedOption ? selectedOption.label : placeholder}
                </span>
                <ChevronDown aria-hidden="true" className={`size-4 shrink-0 text-slate-400 transition-transform ${isOpen ? 'rotate-180 text-blue-700' : ''}`} />
            </button>

            {isOpen && position && createPortal(
                <div
                    className="fixed z-[100] overflow-y-auto rounded-xl border border-slate-200 bg-white p-1.5 shadow-xl shadow-slate-900/15"
                    id={listboxId}
                    ref={listboxRef}
                    role="listbox"
                    style={{ top: position.top, left: position.left, width: position.width, maxHeight: position.maxHeight }}
                >
                    {options.map((option, index) => {
                        const isSelected = option.value === value
                        const isActive = index === activeIndex
                        return (
                            <button
                                aria-selected={isSelected}
                                className={`flex min-h-10 w-full items-center justify-between gap-3 rounded-lg px-3 py-2 text-left text-sm transition-colors ${
                                    isSelected
                                        ? 'bg-blue-50 font-semibold text-blue-800'
                                        : isActive ? 'bg-slate-100 text-slate-900' : 'text-slate-700 hover:bg-slate-50'
                                }`}
                                data-option-index={index}
                                id={`${listboxId}-${index}`}
                                key={`${option.value}-${index}`}
                                onClick={() => selectOption(index)}
                                onMouseEnter={() => setActiveIndex(index)}
                                role="option"
                                type="button"
                            >
                                <span className="min-w-0 break-words">{option.label || placeholder}</span>
                                {isSelected && <Check aria-hidden="true" className="size-4 shrink-0 text-blue-700" />}
                            </button>
                        )
                    })}
                </div>,
                document.body,
            )}
        </div>
    )
}
