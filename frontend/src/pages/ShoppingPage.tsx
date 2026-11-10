import {useCallback, useState} from 'react'
import {api} from '../api/shop.ts'
import {ErrorNotice, Loading} from '../components/Feedback.tsx'
import {ItemEditor} from '../components/ItemEditor.tsx'
import {ShoppingItemRow} from '../components/ShoppingItemRow.tsx'
import {useMutation} from '../hooks/useMutation.ts'
import {useResource} from '../hooks/useResource.ts'

interface ShoppingPageProps {
    storeId: number
    remainingOnly: boolean
    onViewChange: (remainingOnly: boolean) => void
}

export function ShoppingPage({storeId, remainingOnly, onViewChange}: Readonly<ShoppingPageProps>) {
    const load = useCallback(() => api.shoppingList(storeId, remainingOnly), [storeId, remainingOnly])
    const list = useResource(load)
    const mutation = useMutation()
    const [editor, setEditor] = useState<{ itemId: number | null } | null>(null)
    const [saved, setSaved] = useState(false)
    const busy = mutation.pending || list.loading || editor !== null
    const items = list.data ?? []
    const allPurchased = items.length > 0 && items.every(item => item.purchased)
    const emptyMessage = remainingOnly
        ? 'No remaining items. Switch to All to see purchased items too.'
        : 'Your shopping list is empty. Add an item to get started.'

    async function perform(action: () => Promise<unknown>, confirmation?: string) {
        if (confirmation && !window.confirm(confirmation)) return
        setSaved(false)
        if (await mutation.run(action)) list.reload()
    }

    function openEditor(itemId: number | null) {
        setSaved(false)
        setEditor({itemId})
    }

    function finishEditing() {
        setEditor(null)
        setSaved(true)
        list.reload()
    }

    return <>
        <div className="actions shopping-toolbar">
            <fieldset className="actions">
                <legend className="sr-only">List view</legend>
                <button type="button" aria-pressed={!remainingOnly} disabled={busy}
                        onClick={() => onViewChange(false)}>All
                </button>
                <button type="button" aria-pressed={remainingOnly} disabled={busy}
                        onClick={() => onViewChange(true)}>Remaining
                </button>
            </fieldset>
            <button type="button" className="primary" disabled={busy} onClick={() => openEditor(null)}>New item</button>
            <button type="button" disabled={busy || Boolean(list.error)} onClick={() => void perform(
                api.resetPurchased,
                'Start a new shopping trip? All purchase marks will be cleared and your items will be kept.',
            )}>New shopping trip
            </button>
            <button type="button" className="danger" disabled={busy || Boolean(list.error)} onClick={() => void perform(
                api.deletePurchased,
                'Delete all purchased items? The remaining items will be kept.',
            )}>Delete purchased
            </button>
        </div>
        {editor && <ItemEditor
            key={editor.itemId ?? 'new'}
            itemId={editor.itemId}
            onCancel={() => setEditor(null)}
            onSaved={finishEditing}
        />}
        {saved && <p className="notice"><output>Item saved successfully.</output></p>}
        <section className="card" aria-label="Shopping list items" aria-busy={list.loading || mutation.pending}>
            <ErrorNotice message={mutation.error}/>
            {mutation.pending && <p><output>Working…</output></p>}
            {list.loading && <Loading/>}
            <ErrorNotice message={list.error} onRetry={list.reload}/>
            {list.data?.length === 0 && <p className="notice">{emptyMessage}</p>}
            {allPurchased && <p className="notice"><output>All items have been purchased.</output></p>}
            {list.data?.map((item, index, items) => <div key={item.id}>
                {(index === 0 || items[index - 1].categoryId !== item.categoryId) && (
                    <h2 className={item.categoryMissing ? 'missing' : ''}>{item.categoryName}</h2>
                )}
                <ShoppingItemRow
                    item={item}
                    disabled={busy}
                    onToggle={() => void perform(() => api.setPurchased(item.id, !item.purchased))}
                    onEdit={() => openEditor(item.id)}
                    onDelete={() => void perform(() => api.deleteItem(item.id), `Delete this item: ${item.name}?`)}
                />
            </div>)}
        </section>
    </>
}
