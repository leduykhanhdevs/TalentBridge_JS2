type LoginSource = {
    from?: {
        pathname?: unknown
        search?: unknown
        hash?: unknown
    }
} | null

export function getCandidatePostLoginPath(state: unknown): string {
    const from = (state as LoginSource)?.from
    const pathname = typeof from?.pathname === 'string' ? from.pathname : ''
    if (!/^\/jobs\/[1-9]\d*$/.test(pathname)) return '/'

    const search = typeof from?.search === 'string' && from.search.startsWith('?') ? from.search : ''
    const hash = typeof from?.hash === 'string' && from.hash.startsWith('#') ? from.hash : ''
    return `${pathname}${search}${hash}`
}
