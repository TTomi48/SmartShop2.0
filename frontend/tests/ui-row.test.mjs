import assert from 'node:assert/strict'
import {test} from 'node:test'
import {mkdtemp, mkdir, readFile, rm, writeFile} from 'node:fs/promises'
import {pathToFileURL} from 'node:url'
import {join} from 'node:path'
import {createElement} from 'react'
import {renderToStaticMarkup} from 'react-dom/server'
import ts from 'typescript'

test('EF4, EF6–EF7: decimal display, missing-item warning and purchased state are rendered', async () => {
    const scratch = new URL('../node_modules/.tmp/', import.meta.url)
    await mkdir(scratch, {recursive: true})
    const directory = await mkdtemp(new URL('row-', scratch))
    try {
        const source = await readFile(new URL('../src/components/ShoppingItemRow.tsx', import.meta.url), 'utf8')
        const code = ts.transpileModule(source, {
            compilerOptions: {
                jsx: ts.JsxEmit.ReactJSX, module: ts.ModuleKind.ESNext,
            }
        }).outputText
        const file = join(directory, 'row.mjs')
        await writeFile(file, code)
        const {ShoppingItemRow} = await import(pathToFileURL(file).href)
        const item = {id: 1, name: 'Milk', purchased: false, categoryMissing: true, quantity: null}
        const render = item => renderToStaticMarkup(createElement(ShoppingItemRow, {
            item, disabled: false, onToggle() {
            }, onEdit() {
            }, onDelete() {
            },
        }))
        const missing = render(item)
        assert.match(missing, /class="item-name missing"/)
        assert.match(missing, /This category is not assigned to this store/)
        assert.match(missing, /type="checkbox"/)
        assert.doesNotMatch(missing, /disabled=""/)
        const placed = render({...item, categoryMissing: false, purchased: true})
        assert.doesNotMatch(placed, /item-name missing|This category is not assigned/)
        assert.match(placed, /checked=""/)
        assert.match(placed, /item-content purchased/)
        for (const [quantity, displayed] of [[1, '1'], [1.5, '1,5'], [1.125, '1,125'], [1.126, '1,126'], [0.001, '0,001'], [3, '3']]) {
            assert.ok(render({...item, quantity, unit: 'kg'}).includes(`<p>${displayed} kg</p>`))
        }
    } finally {
        await rm(directory, {recursive: true, force: true})
    }
})
