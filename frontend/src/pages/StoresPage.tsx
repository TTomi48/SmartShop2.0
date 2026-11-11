import {useState} from 'react'
import {api} from '../api/shop.ts'
import {NameForm} from '../components/NameForm.tsx'
import {StoreLayout} from '../components/StoreLayout.tsx'
import type {NamedResource} from '../types/index.ts'

export function StoresPage({stores, selectedStoreId, selecting, onSelect, onCreated}: Readonly<{
    stores: NamedResource[]
    selectedStoreId: number | null
    selecting: boolean
    onSelect: (id: number) => Promise<void>
    onCreated: (store: NamedResource) => void
}>) {
    const [editingId, setEditingId] = useState(selectedStoreId ?? stores[0]?.id ?? null)
    const editingStore = stores.find(store => store.id === editingId)

    async function createStore(name: string) {
        const store = await api.createStore(name)
        onCreated(store)
        setEditingId(store.id)
    }

    return <>
        <div className="page-heading">
            <div><h1>Stores</h1><p>Set the category order for each store.</p></div>
        </div>
        <section className="card" aria-label="Manage stores">
            <h2>New store</h2>
            <NameForm label="Store name" onCreate={createStore}/>
            <h2>Stores</h2>
            {stores.length === 0 &&
                <p className="notice">No stores yet. Create your first store using the form above.</p>}
            <ul className="plain-list">{stores.map(store => <li className="list-row" key={store.id}>
                <span><strong>{store.name}</strong>{store.id === selectedStoreId &&
                    <span className="muted"> · Selected for shopping</span>}</span>
                <div className="actions">
                    <button type="button" aria-pressed={editingId === store.id} aria-label={`${store.name} layout`}
                            onClick={() => setEditingId(store.id)}>Layout
                    </button>
                    <button type="button" disabled={selecting || selectedStoreId === store.id}
                            aria-label={`${store.name}: select for shopping`}
                            onClick={() => void onSelect(store.id)}>Select
                    </button>
                </div>
            </li>)}</ul>
        </section>
        {editingStore && <StoreLayout key={editingStore.id} store={editingStore}/>}
    </>
}
