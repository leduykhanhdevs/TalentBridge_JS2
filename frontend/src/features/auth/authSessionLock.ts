export const AUTH_SESSION_LOCK_NAME = 'talentbridge-auth-session'

/** Refresh must stay disabled where tabs cannot serialize session rotation and logout. */
export function isAuthSessionLockAvailable(): boolean {
    return typeof navigator !== 'undefined' && navigator.locks !== undefined
}

/** Serializes refresh, logout, and new sign-in writes across same-origin tabs. */
export function withAuthSessionLock<T>(
    operation: () => Promise<T> | T,
    timeoutMs: number,
): Promise<T> {
    const lockManager = isAuthSessionLockAvailable() ? navigator.locks : undefined
    if (!lockManager) return Promise.resolve(operation())

    const controller = new AbortController()
    const timeoutId = globalThis.setTimeout(
        () => controller.abort(new DOMException('Auth session lock timed out', 'TimeoutError')),
        timeoutMs,
    )

    return lockManager.request(
        AUTH_SESSION_LOCK_NAME,
        { mode: 'exclusive', signal: controller.signal },
        () => {
            globalThis.clearTimeout(timeoutId)
            return operation()
        },
    ).finally(() => globalThis.clearTimeout(timeoutId))
}
