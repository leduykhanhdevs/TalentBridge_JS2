import { useEffect, useRef, useState } from 'react'
import { formatDateDraft, formatIsoDate, parseVietnameseDate } from './dateFormat'

interface DateInputProps {
    id: string
    value: string
    onChange: (isoDate: string) => void
    disabled?: boolean
    required?: boolean
    max?: string
    className?: string
    'aria-label'?: string
}

const inputClass = 'w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm text-slate-800 shadow-sm outline-none transition placeholder:text-slate-400 focus:border-blue-500 focus:ring-3 focus:ring-blue-100 disabled:bg-slate-100 disabled:text-slate-400'

export function DateInput({
    id,
    value,
    onChange,
    disabled = false,
    required = false,
    max,
    className = '',
    'aria-label': ariaLabel,
}: DateInputProps) {
    const [draft, setDraft] = useState(() => formatIsoDate(value))
    const [hasError, setHasError] = useState(false)
    const inputRef = useRef<HTMLInputElement>(null)

    const validationMessage = `Nhập ngày hợp lệ theo định dạng dd/mm/yyyy${max ? ' và không vượt quá hôm nay' : ''}.`

    useEffect(() => {
        setDraft(formatIsoDate(value))
        setHasError(false)
        inputRef.current?.setCustomValidity('')
    }, [value])

    function handleChange(nextValue: string) {
        const nextDraft = formatDateDraft(nextValue)
        const digits = nextDraft.replace(/\D/g, '')
        setDraft(nextDraft)

        if (digits.length === 0) {
            setHasError(false)
            inputRef.current?.setCustomValidity('')
            onChange('')
            return
        }

        const parsed = parseVietnameseDate(nextDraft)
        if (parsed && (!max || parsed <= max)) {
            setHasError(false)
            inputRef.current?.setCustomValidity('')
            onChange(parsed)
        } else if (digits.length === 8) {
            setHasError(true)
            inputRef.current?.setCustomValidity(validationMessage)
        } else {
            setHasError(false)
            inputRef.current?.setCustomValidity('')
        }
    }

    function validateOnBlur() {
        const digits = draft.replace(/\D/g, '')
        if (digits.length === 0 && !required) {
            setHasError(false)
            inputRef.current?.setCustomValidity('')
            onChange('')
            return
        }
        const parsed = parseVietnameseDate(draft)
        const isValid = Boolean(parsed && (!max || parsed <= max))
        setHasError(!isValid)
        inputRef.current?.setCustomValidity(isValid ? '' : validationMessage)
        if (isValid && parsed) onChange(parsed)
    }

    return (
        <div>
            <input
                aria-invalid={hasError}
                aria-label={ariaLabel}
                autoComplete="off"
                className={`${inputClass} ${hasError ? 'border-rose-400 focus:border-rose-500 focus:ring-rose-100' : ''} ${className}`}
                disabled={disabled}
                id={id}
                inputMode="numeric"
                maxLength={10}
                onBlur={validateOnBlur}
                onChange={(event) => handleChange(event.target.value)}
                placeholder="dd/mm/yyyy"
                required={required}
                ref={inputRef}
                type="text"
                value={draft}
            />
            {hasError && (
                <p className="mt-1 text-xs text-rose-600" role="alert">
                    {validationMessage}
                </p>
            )}
        </div>
    )
}
