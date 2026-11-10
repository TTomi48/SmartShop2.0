import type {ItemRequest} from '../types/index.ts'

export interface ItemFormValues {
    name: string
    categoryId: string
    quantity: string
    unit: string
    note: string
}

export function toItemRequest(values: ItemFormValues): ItemRequest {
    if (!values.name.trim()) throw new Error('Enter an item name.')
    const categoryId = Number(values.categoryId)
    if (!Number.isSafeInteger(categoryId) || categoryId <= 0) throw new Error('Select a category.')
    const rawQuantity = values.quantity.trim()
    const quantity = rawQuantity === '' ? null : Number(rawQuantity.replace(',', '.'))
    if (quantity !== null && (!/^(?:\d+(?:[.,]\d*)?|[.,]\d+)$/.test(rawQuantity)
        || !Number.isFinite(quantity) || quantity <= 0)) {
        throw new Error('Quantity must be a positive whole or decimal number.')
    }
    if (values.note.length > 250) throw new Error('Notes must not exceed 250 characters.')
    return {
        name: values.name.trim(), categoryId, quantity,
        unit: values.unit.trim() || null, note: values.note.trim() || null,
    }
}
