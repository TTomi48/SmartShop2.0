export function ErrorNotice({message, onRetry}: Readonly<{ message?: string | null; onRetry?: () => void }>) {
    if (!message) return null
    return <div className="notice error" role="alert">
        <p>{message}</p>
        {onRetry && <button type="button" onClick={onRetry}>Try again</button>}
    </div>
}

export function Loading() {
    return <p className="notice"><output>Loading…</output></p>
}
