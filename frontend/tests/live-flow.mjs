// Run only against a disposable, freshly initialized Compose project (see docs).
import assert from 'node:assert/strict'
import {mkdir, readFile, writeFile} from 'node:fs/promises'
import {api, loadInitialData} from '../src/api/shop.ts'
import {ApiError} from '../src/api/client.ts'
import {toItemFormValues, toItemRequest} from '../src/components/itemFormValues.ts'

const base = process.env.SMARTSHOP_TEST_URL
assert.ok(base, 'Set SMARTSHOP_TEST_URL to the disposable frontend URL.')
const originalFetch = globalThis.fetch
globalThis.fetch = (path, options) => originalFetch(new URL(path, base), options)
const snapshotFile = new URL('./evidence/live-snapshot.json', import.meta.url)

async function snapshot() {
    const initial = await loadInitialData()
    return {
        ...initial,
        categories: await api.categories(),
        layouts: await Promise.all(initial.stores.map(store => api.storeCategories(store.id))),
        items: await (await fetch('/api/items')).json(),
        list: await api.shoppingList(initial.selectedStoreId),
    }
}

function pass(message) {
    console.log(`PASS: ${message}`)
}

async function reject(action, status) {
    await assert.rejects(action, error => error instanceof ApiError && error.status === status)
}

if (process.argv.includes('--verify-persistence')) {
    assert.deepEqual(await snapshot(), JSON.parse(await readFile(snapshotFile, 'utf8')))
    pass('EF8: stores, categories, layouts, decimal quantity, items, purchase flags and selection survive recreation')
} else {
    assert.ok(process.argv.includes('--disposable'), 'Pass --disposable only for an isolated test database.')
    assert.deepEqual(await (await fetch('/api/items')).json(), [], 'Requires an empty shopping list')
    assert.match(await (await fetch('/')).text(), /id="root"/)
    const initial = await loadInitialData()
    assert.equal(initial.stores.length, 2)
    const [a, b] = initial.stores
    const categories = await api.categories()
    assert.equal(categories.length, 6)
    const layoutA = await api.storeCategories(a.id)
    const layoutB = await api.storeCategories(b.id)
    assert.deepEqual(layoutA.map(p => p.categoryId), layoutB.map(p => p.categoryId).reverse())
    pass('EF1–EF3: frontend HTTP, two sample stores and opposite StoreCategory layouts')

    const store = await api.createStore('Audit store')
    const missing = await api.createCategory('Audit category')
    assert.deepEqual(await api.storeCategories(store.id), [])
    assert.equal((await api.storeCategories(a.id)).some(p => p.categoryId === missing.id), false)
    await reject(() => api.createStore(' audit STORE '), 409)
    await reject(() => api.createCategory(' audit CATEGORY '), 409)
    await reject(() => api.createCategory('  '), 400)
    await reject(() => api.selectStore(99999999), 404)
    const [first, second] = layoutA
    await api.addStoreCategory(store.id, first.categoryId)
    await api.addStoreCategory(store.id, second.categoryId)
    await reject(() => api.addStoreCategory(store.id, first.categoryId), 409)
    await api.moveCategory(store.id, first.categoryId, 'up')
    await api.moveCategory(store.id, second.categoryId, 'down')
    assert.deepEqual((await api.storeCategories(store.id)).map(p => p.position), [0, 1])
    await api.moveCategory(store.id, second.categoryId, 'up')
    assert.deepEqual((await api.storeCategories(store.id)).map(p => p.categoryId), [second.categoryId, first.categoryId])
    assert.deepEqual(await api.storeCategories(a.id), layoutA)
    pass('EF1–EF3, EF8: creation, uniqueness, invalid IDs, boundary moves and independent ordering')

    const payload = {name: 'Milk', categoryId: first.categoryId, quantity: 1.1255, unit: 'custom', note: 'a'.repeat(250)}
    const milk = await api.createItem(payload)
    assert.equal(milk.quantity, 1.126)
    for (const [quantity, expected] of [[1, 1], [1.5, 1.5], [1.12, 1.12], [1.125, 1.125],
        [1.1255, 1.126], [1.1254, 1.125], [0.0009, 0.001], [0.0005, 0.001], [2.9999, 3], [1.23456789, 1.235], [null, null]]) {
        const created = await api.createItem({...payload, quantity})
        assert.equal(created.quantity, expected)
        assert.equal((await api.item(created.id)).quantity, expected)
        const updated = await api.updateItem(milk.id, {...payload, quantity})
        assert.equal(updated.quantity, expected)
        assert.equal((await api.item(milk.id)).quantity, expected)
        assert.equal(toItemFormValues(await api.item(milk.id)).quantity, expected?.toString().replace('.', ',') ?? '')
        await api.deleteItem(created.id)
    }
    const commaInput = toItemRequest({name: 'Milk', categoryId: String(first.categoryId), quantity: '1,1255', unit: '', note: ''})
    assert.equal((await api.updateItem(milk.id, commaInput)).quantity, 1.126)
    await api.updateItem(milk.id, payload)
    pass('EF4, EF8: HALF_UP create/update, exact readback, optional quantity and reopened comma-input form')
    const bread = await api.createItem({...payload, name: 'Bread', categoryId: second.categoryId, quantity: null})
    const duplicate = await api.createItem({...payload, name: 'Bread', categoryId: second.categoryId})
    assert.notEqual(bread.id, duplicate.id)
    const unplaced = await api.createItem({...payload, name: 'Missing', categoryId: missing.id})
    const list = () => api.shoppingList(store.id)
    assert.deepEqual((await list()).map(i => i.id), [bread.id, duplicate.id, milk.id, unplaced.id])
    assert.equal((await list()).at(-1).categoryMissing, true)
    await api.setPurchased(unplaced.id, true)
    assert.equal((await api.shoppingList(store.id, true)).some(i => i.id === unplaced.id), false)
    await api.selectStore(b.id)
    assert.equal((await api.item(unplaced.id)).purchased, true)
    await api.selectStore(store.id)
    await api.addStoreCategory(store.id, missing.id)
    assert.equal((await list()).at(-1).categoryMissing, false)
    await api.removeStoreCategory(store.id, missing.id)
    assert.equal((await list()).at(-1).categoryMissing, true)
    pass('EF4–EF7: duplicates, decimal quantity, category order, missing category and persisted purchase state')

    const before = await api.item(milk.id)
    for (const quantity of [0, -1, -0.0001, 0.0001, 0.0004999, 'abc']) {
        await reject(() => api.createItem({...payload, quantity}), 400)
        await reject(() => api.updateItem(milk.id, {...payload, quantity}), 400)
    }
    await reject(() => api.updateItem(milk.id, {...payload, note: 'a'.repeat(251)}), 400)
    await reject(() => api.updateItem(milk.id, {...payload, categoryId: 99999999}), 404)
    await reject(() => api.updateItem(milk.id, {...payload, name: ' '}), 400)
    assert.deepEqual(await api.item(milk.id), before)
    await api.updateItem(bread.id, {...payload, name: 'Edited bread', quantity: null, unit: null, note: null})
    assert.equal((await api.item(bread.id)).note, null)
    await api.setPurchased(unplaced.id, false)
    await api.setPurchased(milk.id, true)
    await api.deletePurchased()
    await reject(() => api.item(milk.id), 404)
    assert.equal((await list()).length, 3)
    await api.setPurchased(unplaced.id, true)
    await api.resetPurchased()
    assert.equal((await list()).some(i => i.purchased), false)
    await api.deleteItem(duplicate.id)
    // Keep a decimal and a purchased item for the separate restart verification.
    await api.updateItem(unplaced.id, {...payload, categoryId: missing.id})
    await api.setPurchased(unplaced.id, true)
    pass('EF4, EF7–EF8: invalid updates leave data unchanged; edit, undo, delete purchased, reset and delete')
    await mkdir(new URL('./evidence/', import.meta.url), {recursive: true})
    await writeFile(snapshotFile, JSON.stringify(await snapshot(), null, 2))
    pass('EF8: saved snapshot for verification after application/database recreation')
}
