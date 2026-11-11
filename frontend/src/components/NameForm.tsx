import {useId, useState} from 'react'
import type {SubmitEvent} from 'react'
import {useMutation} from '../hooks/useMutation.ts'
import {ErrorNotice} from './Feedback.tsx'

export function NameForm({label, onCreate}: Readonly<{ label: string; onCreate: (name: string) => Promise<unknown> }>) {
    const id = useId()
    const [name, setName] = useState('')
    const [saved, setSaved] = useState(false)
    const mutation = useMutation()

    async function submit(event: SubmitEvent<HTMLFormElement>) {
        event.preventDefault()
        setSaved(false)
        if (await mutation.run(async () => {
            if (!name.trim()) throw new Error('Name must not be empty.')
            await onCreate(name.trim())
        })) {
            setName('');
            setSaved(true)
        }
    }

    return <form onSubmit={submit}>
        <div className="inline-form">
            <label className="field" htmlFor={id}>{label}
                <input id={id} required value={name} disabled={mutation.pending} onChange={event => {
                    setName(event.target.value);
                    setSaved(false)
                }}/>
            </label>
            <button type="submit" className="primary"
                    disabled={mutation.pending}>{mutation.pending ? 'Saving…' : 'Create'}</button>
        </div>
        <ErrorNotice message={mutation.error}/>
        {saved && <p className="notice">
            <output>Created successfully.</output>
        </p>}
    </form>
}
