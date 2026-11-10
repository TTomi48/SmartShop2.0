import assert from 'node:assert/strict'
import {spawnSync} from 'node:child_process'
import {mkdir, mkdtemp, readFile, rm, writeFile} from 'node:fs/promises'
import {dirname, join, resolve, sep} from 'node:path'
import {fileURLToPath} from 'node:url'
import ts from 'typescript'

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..')
const scratch = join(root, 'node_modules', '.tmp')
await mkdir(scratch, {recursive: true})
const temporary = await mkdtemp(join(scratch, 'regression-204-'))
const source = await readFile(join(root, 'src/api/client.ts'), 'utf8')
const guard = 'if (response.status === 204) return undefined as T'
assert.ok(source.includes(guard), 'The 204 guard must exist in the source under test.')
const evidence = [`Run: ${new Date().toISOString()}`, 'The broken variant is intentionally altered in an isolated temporary copy; production source is unchanged.']

try {
    for (const [variant, code] of [['broken', source.replace(guard, '')], ['corrected', source]]) {
        const javascript = ts.transpileModule(code, {
            compilerOptions: {
                module: ts.ModuleKind.ESNext,
                target: ts.ScriptTarget.ES2022
            }
        }).outputText
        await writeFile(join(temporary, 'client.mjs'), javascript)
        await writeFile(join(temporary, '204.test.mjs'), `
      import assert from 'node:assert/strict'
      import { test } from 'node:test'
      import { request } from './client.mjs'
      test('204 No Content is a successful empty response', async () => {
        globalThis.fetch = async () => new Response(null, { status: 204 })
        assert.equal(await request('/api/items/7/purchased', { method: 'PATCH' }), undefined)
      })
    `)
        const result = spawnSync(process.execPath, ['--test', join(temporary, '204.test.mjs')], {
            encoding: 'utf8',
            windowsHide: true
        })
        evidence.push(`\n${variant.toUpperCase()} — node --test 204.test.mjs — exit ${result.status}`, result.stdout, result.stderr)
        assert.equal(result.status, variant === 'broken' ? 1 : 0, `${variant}: unexpected test outcome`)
    }
    await mkdir(join(root, 'tests/evidence'), {recursive: true})
    await writeFile(join(root, 'tests/evidence/regression-204.txt'), evidence.join('\n'))
    console.log('PASS: intentionally broken copy fails; corrected client passes the same 204 regression test. Evidence: tests/evidence/regression-204.txt')
} finally {
    if (resolve(temporary).startsWith(resolve(scratch) + sep)) await rm(temporary, {recursive: true, force: true})
}
