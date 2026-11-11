import {api} from '../api/shop.ts'
import {ErrorNotice, Loading} from '../components/Feedback.tsx'
import {NameForm} from '../components/NameForm.tsx'
import {useResource} from '../hooks/useResource.ts'

export function CategoriesPage() {
    const categories = useResource(api.categories)

    async function createCategory(name: string) {
        await api.createCategory(name)
        categories.reload()
    }

    return <>
        <div className="page-heading">
            <div><h1>Categories</h1><p>Categories are shared across all stores.</p></div>
        </div>
        <section className="card" aria-label="Manage categories">
            <h2>New global category</h2>
            <p className="muted">Assign new categories to each store on the Stores page.</p>
            <NameForm label="Category name" onCreate={createCategory}/>
            <h2>Global categories</h2>
            {categories.loading && <Loading/>}
            <ErrorNotice message={categories.error} onRetry={categories.reload}/>
            {categories.data?.length === 0 &&
                <p className="notice">No global categories yet. Create your first category.</p>}
            <ul className="plain-list">{categories.data?.map(category => <li className="list-row" key={category.id}>
                <span>{category.name}</span></li>)}</ul>
        </section>
    </>
}
