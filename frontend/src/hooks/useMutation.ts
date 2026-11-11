import {useRef, useState} from 'react'
import {errorMessage} from '../api/client.ts'

export function useMutation() {
    const lock = useRef(false)
    const [pending, setPending] = useState(false)
    const [error, setError] = useState<string | null>(null)

    async function run(action: () => Promise<unknown>) {
        if (lock.current) return false
        lock.current = true
        setPending(true)
        setError(null)
        try {
            await action()
            return true
        } catch (error) {
            setError(errorMessage(error))
            return false
        } finally {
            lock.current = false
            setPending(false)
        }
    }

    return {pending, error, run}
}
