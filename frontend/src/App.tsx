import {useState} from 'react'
import {api, loadInitialData} from './api/shop.ts'
import {ErrorNotice, Loading} from './components/Feedback.tsx'
import {useMutation} from './hooks/useMutation.ts'
import {useResource} from './hooks/useResource.ts'
import {ShoppingPage} from './pages/ShoppingPage.tsx'
import {StoresPage} from './pages/StoresPage.tsx'
import {CategoriesPage} from './pages/CategoriesPage.tsx'
import type {NamedResource} from './types/index.ts'
import './App.css'

type Page = 'shopping' | 'stores' | 'categories'

function Shop({initial}: Readonly<{ initial: { stores: NamedResource[]; selectedStoreId: number | null } }>) {
    const [page, setPage] = useState<Page>('shopping')
    const [stores, setStores] = useState(initial.stores)
    const [storeId, setStoreId] = useState(initial.selectedStoreId)
    const [remainingOnly, setRemainingOnly] = useState(false)
    const selection = useMutation()
    const selectedStore = stores.find(store => store.id === storeId)
    const canShowList = stores.length > 0 && storeId !== null
    const storeMessage = stores.length === 0
        ? 'No stores yet. Create one on the Stores page.'
        : 'Select a store to view your shopping list.'

    async function selectStore(id: number) {
        if (await selection.run(() => api.selectStore(id))) setStoreId(id)
    }

    return <>
        <header className="site-header">
            <a className="brand" href="#shopping" onClick={() => setPage('shopping')}>SmartShop</a>
            <nav aria-label="Main navigation">
                {([['shopping', 'Shopping list'], ['stores', 'Stores'], ['categories', 'Categories']] as const).map(([id, label]) =>
                    <button key={id} type="button" aria-current={page === id ? 'page' : undefined}
                            onClick={() => setPage(id)}>{label}</button>,
                )}
            </nav>
        </header>
        <main>
            <section className="card store-selector">
                <label htmlFor="selected-store">Selected store</label>
                <select id="selected-store" value={storeId ?? ''} disabled={selection.pending || stores.length === 0}
                        onChange={event => void selectStore(Number(event.target.value))}>
                    <option value="" disabled>Select a store</option>
                    {stores.map(store => <option key={store.id} value={store.id}>{store.name}</option>)}
                </select>
                {selection.pending && <output>Switching store…</output>}
                <ErrorNotice message={selection.error}/>
            </section>
            {page === 'shopping' && <>
                <div className="page-heading">
                    <div><h1>Shopping list</h1>
                        <p>{selectedStore?.name ?? 'Your shared shopping list, ordered by store.'}</p></div>
                </div>
                {canShowList ? <ShoppingPage key={storeId} storeId={storeId} remainingOnly={remainingOnly}
                                             onViewChange={setRemainingOnly}/> :
                    <p className="card notice">{storeMessage}</p>}
            </>}
            {page === 'stores' && <StoresPage stores={stores} selectedStoreId={storeId} selecting={selection.pending}
                                              onSelect={selectStore}
                                              onCreated={store => setStores(current => [...current, store])}/>}
            {page === 'categories' && <CategoriesPage/>}
        </main>
    </>
}

export default function App() {
    const initial = useResource(loadInitialData)
    if (initial.loading) return <main><h1>SmartShop</h1><Loading/></main>
    if (initial.error) return <main><h1>SmartShop</h1><ErrorNotice message={initial.error} onRetry={initial.reload}/>
    </main>
    return initial.data && <Shop initial={initial.data}/>
}
