import {jsonBody, request} from './client.ts'
import type {ItemRequest, NamedResource, ShoppingItem, ShoppingListItem, StoreCategory} from '../types/index.ts'

export const api = {
    stores: () => request<NamedResource[]>('/api/stores'),
    createStore: (name: string) => request<NamedResource>('/api/stores', jsonBody('POST', {name})),
    selectedStore: () => request<{ storeId: number | null }>('/api/settings/selected-store'),
    selectStore: (storeId: number) => request<void>('/api/settings/selected-store', jsonBody('PUT', {storeId})),
    categories: () => request<NamedResource[]>('/api/categories'),
    createCategory: (name: string) => request<NamedResource>('/api/categories', jsonBody('POST', {name})),
    shoppingList: (storeId: number, remainingOnly = false) => request<ShoppingListItem[]>(
        `/api/stores/${storeId}/shopping-list${remainingOnly ? '?remainingOnly=true' : ''}`,
    ),
    item: (id: number) => request<ShoppingItem>(`/api/items/${id}`),
    createItem: (item: ItemRequest) => request<ShoppingItem>('/api/items', jsonBody('POST', item)),
    updateItem: (id: number, item: ItemRequest) => request<ShoppingItem>(`/api/items/${id}`, jsonBody('PUT', item)),
    deleteItem: (id: number) => request<void>(`/api/items/${id}`, jsonBody('DELETE')),
    setPurchased: (id: number, purchased: boolean) => request<void>(`/api/items/${id}/purchased`, jsonBody('PATCH', {purchased})),
    resetPurchased: () => request<void>('/api/items/reset-purchased', jsonBody('POST')),
    deletePurchased: () => request<void>('/api/items/purchased', jsonBody('DELETE')),
    storeCategories: (storeId: number) => request<StoreCategory[]>(`/api/stores/${storeId}/categories`),
    addStoreCategory: (storeId: number, categoryId: number) => request<StoreCategory>(
        `/api/stores/${storeId}/categories`, jsonBody('POST', {categoryId}),
    ),
    removeStoreCategory: (storeId: number, categoryId: number) => request<void>(
        `/api/stores/${storeId}/categories/${categoryId}`, jsonBody('DELETE'),
    ),
    moveCategory: (storeId: number, categoryId: number, direction: 'up' | 'down') => request<void>(
        `/api/stores/${storeId}/categories/${categoryId}/move-${direction}`, jsonBody('PATCH'),
    ),
}

export async function loadInitialData() {
    const [stores, selected] = await Promise.all([api.stores(), api.selectedStore()])
    return {stores, selectedStoreId: selected.storeId}
}
