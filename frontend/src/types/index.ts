export interface NamedResource {
    id: number;
    name: string
}

export interface ShoppingItem {
    id: number
    name: string
    categoryId: number
    categoryName: string
    quantity: number | null
    unit: string | null
    note: string | null
    purchased: boolean
    createdAt: string
}

export interface ShoppingListItem extends ShoppingItem {
    categoryMissing: boolean
}

export interface ItemRequest {
    name: string
    categoryId: number
    quantity: number | null
    unit: string | null
    note: string | null
}

export interface StoreCategory {
    id: number
    storeId: number
    categoryId: number
    categoryName: string
    position: number
}
