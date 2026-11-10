import type {ShoppingListItem} from '../types/index.ts'

interface ShoppingItemRowProps {
    item: ShoppingListItem
    disabled: boolean
    onToggle: () => void
    onEdit: () => void
    onDelete: () => void
}

export function ShoppingItemRow({item, disabled, onToggle, onEdit, onDelete}: Readonly<ShoppingItemRowProps>) {
    const quantity = [item.quantity?.toLocaleString('en-GB'), item.unit].filter(Boolean).join(' ')

    return <div className="item-row">
        <input
            type="checkbox"
            aria-label={`${item.name}: purchased`}
            checked={item.purchased}
            disabled={disabled}
            onChange={onToggle}
        />
        <div className={item.purchased ? 'item-content purchased' : 'item-content'}>
            <strong className="item-name">{item.name}</strong>
            {quantity && <p>{quantity}</p>}
            {item.note && <p>{item.note}</p>}
            {item.categoryMissing && <p className="warning">This category is not assigned to this store</p>}
        </div>
        <div className="actions">
            <button type="button" disabled={disabled} aria-label={`${item.name}: edit`} onClick={onEdit}>Edit</button>
            <button type="button" className="danger" disabled={disabled} aria-label={`${item.name}: delete`}
                    onClick={onDelete}>Delete
            </button>
        </div>
    </div>
}
