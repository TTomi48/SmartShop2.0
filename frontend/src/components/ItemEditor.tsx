import {useCallback, useState} from 'react'
import type {SubmitEvent} from 'react'
import {api} from '../api/shop.ts'
import {useResource} from '../hooks/useResource.ts'
import {useMutation} from '../hooks/useMutation.ts'
import type {ShoppingItem} from '../types/index.ts'
import {ErrorNotice, Loading} from './Feedback.tsx'
import {toItemRequest} from './itemFormValues.ts'
import type {ItemFormValues} from './itemFormValues.ts'

interface EditorProps {
    itemId: number | null
    onSaved: () => void
    onCancel: () => void
}

export function ItemEditor({itemId, onSaved, onCancel}: Readonly<EditorProps>) {
    const load = useCallback(() => itemId === null ? Promise.resolve(null) : api.item(itemId), [itemId])
    const item = useResource(load)
    return <section className="card" aria-labelledby="editor-heading">
        <h2 id="editor-heading">{itemId === null ? 'New item' : 'Edit item'}</h2>
        {item.loading && <Loading/>}
        <ErrorNotice message={item.error} onRetry={item.reload}/>
        {item.data !== undefined
            ? <ItemForm initial={item.data} onSaved={onSaved} onCancel={onCancel}/>
            : <button type="button" onClick={onCancel}>Cancel</button>}
    </section>
}

function ItemForm({initial, onSaved, onCancel}: Readonly<{
    initial: ShoppingItem | null
    onSaved: () => void
    onCancel: () => void
}>) {
    const categories = useResource(api.categories)
    const mutation = useMutation()
    const [values, setValues] = useState<ItemFormValues>({
        name: initial?.name ?? '', categoryId: initial ? String(initial.categoryId) : '',
        quantity: initial?.quantity?.toString() ?? '', unit: initial?.unit ?? '', note: initial?.note ?? '',
    })

    function change(field: keyof ItemFormValues, value: string) {
        setValues(current => ({...current, [field]: value}))
    }

    async function submit(event: SubmitEvent<HTMLFormElement>) {
        event.preventDefault()
        if (await mutation.run(() => {
            const body = toItemRequest(values)
            return initial ? api.updateItem(initial.id, body) : api.createItem(body)
        })) onSaved()
    }

    return <form onSubmit={submit}>
        {categories.loading && <Loading/>}
        <ErrorNotice message={categories.error} onRetry={categories.reload}/>
        {categories.data?.length === 0 &&
            <p className="notice">No global categories yet. Create one on the Categories page.</p>}
        <ErrorNotice message={mutation.error}/>
        <fieldset disabled={mutation.pending}>
            <legend className="sr-only">Item details</legend>
            <div className="form-grid">
                <label className="field full-width">Item name *{' '}
                    <input required value={values.name} onChange={e => change('name', e.target.value)}/>
                </label>
                <label className="field full-width">Category *{' '}
                    <select required value={values.categoryId} onChange={e => change('categoryId', e.target.value)}
                            disabled={!categories.data?.length}>
                        <option value="">Select a category</option>
                        {categories.data?.map(category => <option key={category.id}
                                                                  value={category.id}>{category.name}</option>)}
                    </select>
                </label>
                <label className="field">Quantity{' '}
                    <input inputMode="decimal" placeholder="e.g. 1.5" value={values.quantity}
                           onChange={e => change('quantity', e.target.value)}/>
                </label>
                <label className="field">Unit{' '}
                    <input list="unit-options" value={values.unit} onChange={e => change('unit', e.target.value)}/>
                    <datalist id="unit-options">{['pcs', 'kg', 'g', 'l'].map(unit => <option key={unit}
                                                                                             value={unit}/>)}</datalist>
                </label>
                <label className="field full-width">Notes{' '}
                    <textarea rows={3} maxLength={250} value={values.note}
                              onChange={e => change('note', e.target.value)} aria-describedby="note-limit"/>{' '}
                    <span id="note-limit" className="muted">{`${values.note.length}/250 characters`}</span>
                </label>
                <div className="actions full-width">
                    <button className="primary" type="submit"
                            disabled={!categories.data?.length}>{mutation.pending ? 'Saving…' : 'Save'}</button>
                    <button type="button" onClick={onCancel}>Cancel</button>
                </div>
            </div>
        </fieldset>
    </form>
}
