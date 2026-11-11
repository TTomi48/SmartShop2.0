import {useEffect, useState} from 'react'
import {errorMessage} from '../api/client.ts'

export function useResource<T>(load: () => Promise<T>) {
    const [version, setVersion] = useState(0)
    const [result, setResult] = useState<{
        load: typeof load; version: number; data?: T; error?: string
    } | null>(null)
    useEffect(() => {
        let active = true
        load().then(
            data => {
                if (active) setResult({load, version, data})
            },
            error => {
                if (active) setResult({load, version, error: errorMessage(error)})
            },
        )
        return () => {
            active = false
        }
    }, [load, version])
    const current = result?.load === load && result.version === version
    return {
        data: current ? result.data : undefined,
        error: current ? result.error : undefined,
        loading: !current,
        reload: () => setVersion(value => value + 1),
    }
}
