import assert from 'node:assert/strict'
import {afterEach, test} from 'node:test'
import {ApiError, request} from '../src/api/client.ts'
import {api, loadInitialData} from '../src/api/shop.ts'
import {toItemRequest} from '../src/components/itemFormValues.ts'

const originalFetch = globalThis.fetch
afterEach(() => {
    globalThis.fetch = originalFetch
})
const values = {name: ' Bread ', categoryId: '2', quantity: '', unit: '', note: ''}

test('optional fields become null independently; duplicate names remain separate POSTs', async () => {
    const payload = toItemRequest(values)
    assert.deepEqual(payload, {name: 'Bread', categoryId: 2, quantity: null, unit: null, note: null})
    const calls = []
    globalThis.fetch = async (path, options) => {
        calls.push({path, options})
        return Response.json({id: calls.length, ...payload}, {status: 201})
    }
    const first = await api.createItem(payload)
    const second = await api.createItem(payload)
    assert.notEqual(first.id, second.id)
    assert.equal(calls.length, 2)
    assert.ok(calls.every(call => call.options.method === 'POST'))
})

for (const quantity of ['1', '1.5', '1,5', '.5', ',5', '0.001']) {
    test(`accept positive quantity ${quantity}`, () => {
        assert.equal(toItemRequest({...values, quantity}).quantity, Number(quantity.replace(',', '.')))
    })
}
for (const quantity of ['0', '-1', 'NaN', 'Infinity', 'abc', '1.2.3', '0x10', '1e3']) {
    test(`reject invalid quantity ${quantity}`, () => {
        assert.throws(() => toItemRequest({...values, quantity}), /positive/)
    })
}
test('name, category and note boundary validation', () => {
    assert.throws(() => toItemRequest({...values, name: '  '}), /name/)
    for (const categoryId of ['', '0', '-1', '1.5', 'bad']) {
        assert.throws(() => toItemRequest({...values, categoryId}), /category/)
    }
    assert.equal(toItemRequest({...values, note: 'a'.repeat(250)}).note.length, 250)
    assert.throws(() => toItemRequest({...values, note: 'a'.repeat(251)}), /250/)
    assert.equal(toItemRequest({...values, unit: ' pack '}).unit, 'pack')
})
test('PUT sends all editable fields, allows clearing values, and omits purchased state', async () => {
    const payload = toItemRequest({...values, quantity: '1,5', unit: 'kg', note: 'whole wheat'})
    globalThis.fetch = async (path, options) => {
        assert.equal(path, '/api/items/7')
        assert.equal(options.method, 'PUT')
        assert.deepEqual(JSON.parse(options.body), payload)
        assert.equal(Object.hasOwn(JSON.parse(options.body), 'purchased'), false)
        return Response.json({id: 7, ...payload, purchased: true})
    }
    assert.equal((await api.updateItem(7, payload)).purchased, true)
})
test('remaining filter, backend item order and missing-category flag pass through unchanged', async () => {
    const items = [{id: 8, categoryMissing: false}, {id: 2, categoryMissing: true}, {id: 9, categoryMissing: false}]
    globalThis.fetch = async path => {
        assert.equal(path, '/api/stores/3/shopping-list?remainingOnly=true')
        return Response.json(items)
    }
    assert.deepEqual(await api.shoppingList(3, true), items)
})
test('empty 204 responses need no JSON body; PATCH uses category id rather than placement id', async () => {
    const calls = []
    globalThis.fetch = async (path, options) => {
        calls.push([path, options]);
        return new Response(null, {status: 204})
    }
    assert.equal(await api.setPurchased(7, false), undefined)
    await api.moveCategory(3, 2, 'up')
    await api.removeStoreCategory(3, 2)
    await api.resetPurchased()
    await api.deletePurchased()
    await api.selectStore(3)
    assert.deepEqual(calls.map(([path, options]) => [path, options.method]), [
        ['/api/items/7/purchased', 'PATCH'], ['/api/stores/3/categories/2/move-up', 'PATCH'],
        ['/api/stores/3/categories/2', 'DELETE'], ['/api/items/reset-purchased', 'POST'],
        ['/api/items/purchased', 'DELETE'], ['/api/settings/selected-store', 'PUT'],
    ])
    assert.deepEqual(JSON.parse(calls[0][1].body), {purchased: false})
    assert.deepEqual(JSON.parse(calls[5][1].body), {storeId: 3})
})
for (const status of [400, 404, 409, 500]) {
    test(`HTTP ${status} rejects with the backend error, rather than indicating success`, async () => {
        globalThis.fetch = async () => Response.json({error: 'Test error'}, {status})
        await assert.rejects(request('/api/items'), error => error instanceof ApiError && error.status === status && error.message === 'Test error')
    })
}
test('non-JSON errors, malformed success and network errors have readable fallback messages', async () => {
    globalThis.fetch = async () => new Response('<html>Error</html>', {status: 500})
    await assert.rejects(request('/api/items'), /Server error/)
    globalThis.fetch = async () => new Response('not JSON')
    await assert.rejects(request('/api/items'), /could not be read/)
    globalThis.fetch = async () => {
        throw new TypeError('Failed to fetch')
    }
    await assert.rejects(request('/api/items'), /server is unavailable/)
})
test('startup loads persisted selection and stores with real API paths', async () => {
    globalThis.fetch = async path => Response.json(path === '/api/stores' ? [{id: 3, name: 'Store'}] : {storeId: 3})
    assert.deepEqual(await loadInitialData(), {stores: [{id: 3, name: 'Store'}], selectedStoreId: 3})
})
