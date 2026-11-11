import {useCallback, useState} from 'react'
import type {SubmitEvent} from 'react'
import {api} from '../api/shop.ts'
import {ErrorNotice, Loading} from './Feedback.tsx'
import {useMutation} from '../hooks/useMutation.ts'
import {useResource} from '../hooks/useResource.ts'
import type {NamedResource} from '../types/index.ts'

export function StoreLayout({store}: Readonly<{ store: NamedResource }>) {
    const load = useCallback(async () => {
        const [placements, categories] = await Promise.all([api.storeCategories(store.id), api.categories()])
        return {placements, categories}
    }, [store.id])
    const layout = useResource(load)
    const mutation = useMutation()
    const [categoryId, setCategoryId] = useState('')
    const {categories = [], placements = []} = layout.data ?? {}
    const available = categories.filter(category => !placements.some(placement => placement.categoryId === category.id))
    const selectedCategory = available.some(category => String(category.id) === categoryId) ? categoryId : ''
    const busy = mutation.pending || layout.loading
    let assignmentMessage = ''
    if (categories.length === 0) {
        assignmentMessage = 'No global categories yet. Create one on the Categories page.'
    } else if (available.length === 0) {
        assignmentMessage = 'All global categories are already assigned to this store.'
    }

    async function perform(action: () => Promise<unknown>) {
        if (await mutation.run(action)) layout.reload()
    }

    function add(event: SubmitEvent<HTMLFormElement>) {
        event.preventDefault()
        if (selectedCategory) void perform(() => api.addStoreCategory(store.id, Number(selectedCategory)))
    }

    function removeCategory(id: number, name: string) {
        const confirmed = window.confirm(`Remove ${name} from this store? The global category and its items will be kept.`)
        if (confirmed) void perform(() => api.removeStoreCategory(store.id, id))
    }

    return <section className="card" aria-label={`${store.name} category layout`} aria-busy={busy}>
        <h2>{`${store.name} – category layout`}</h2>
        <p className="muted">This order applies only to this store.</p>
        {layout.loading && <Loading/>}
        <ErrorNotice message={layout.error} onRetry={layout.reload}/>
        <ErrorNotice message={mutation.error}/>
        {mutation.pending && <p>
            <output>Saving…</output>
        </p>}
        {layout.data && <>
            {placements.length === 0 && <p className="notice">No categories have been assigned to this store yet.</p>}
            <ol className="plain-list">{placements.map((placement, index, placements) => <li className="list-row"
                                                                                             key={placement.id}>
                <span>{`${index + 1}. ${placement.categoryName}`}</span>
                <div className="actions">
                    <button type="button" disabled={busy || index === 0}
                            aria-label={`${placement.categoryName}: move up`}
                            onClick={() => void perform(() => api.moveCategory(store.id, placement.categoryId, 'up'))}>↑
                    </button>
                    <button type="button" disabled={busy || index === placements.length - 1}
                            aria-label={`${placement.categoryName}: move down`}
                            onClick={() => void perform(() => api.moveCategory(store.id, placement.categoryId, 'down'))}>↓
                    </button>
                    <button type="button" className="danger" disabled={busy}
                            aria-label={`${placement.categoryName}: remove from store`}
                            onClick={() => removeCategory(placement.categoryId, placement.categoryName)}>Remove
                    </button>
                </div>
            </li>)}</ol>
            {assignmentMessage ? <p className="notice">{assignmentMessage}</p> :
                <form className="inline-form" onSubmit={add}>
                    <label className="field">Assign category{' '}
                        <select required value={selectedCategory} disabled={busy}
                                onChange={event => setCategoryId(event.target.value)}>
                            <option value="">Select a category</option>
                            {available.map(category => <option key={category.id}
                                                               value={category.id}>{category.name}</option>)}
                        </select>
                    </label>
                    <button type="submit" className="primary" disabled={busy || !selectedCategory}>Add</button>
                </form>}
        </>}
    </section>
}
