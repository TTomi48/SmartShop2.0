const baseUrl = (import.meta.env?.VITE_API_BASE_URL ?? '').replace(/\/$/, '')

export class ApiError extends Error {
    readonly status: number

    constructor(message: string, status: number) {
        super(message)
        this.name = 'ApiError'
        this.status = status
    }
}

export async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
    let response: Response
    try {
        response = await fetch(`${baseUrl}${path}`, {
            ...options,
            headers: {Accept: 'application/json', ...options.headers},
        })
    } catch {
        throw new Error('The server is unavailable. Check your connection and try again.')
    }
    if (!response.ok) {
        const fallback = response.status >= 500
            ? 'Server error. Please try again later.'
            : `The operation failed (HTTP ${response.status}).`
        const body: unknown = await response.json().catch(() => null)
        const message = body && typeof body === 'object' && 'error' in body
        && typeof body.error === 'string' && body.error.trim() ? body.error : fallback
        throw new ApiError(message, response.status)
    }
    if (response.status === 204) return undefined as T
    try {
        return await response.json() as T
    } catch {
        throw new Error('The server response could not be read. Try loading again.')
    }
}

export function jsonBody(method: string, body?: unknown): RequestInit {
    return body === undefined ? {method} : {
        method, headers: {'Content-Type': 'application/json'}, body: JSON.stringify(body),
    }
}

export function errorMessage(error: unknown): string {
    return error instanceof Error ? error.message : 'An unexpected error occurred. Please try again.'
}
