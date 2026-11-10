package hu.bme.backend.dto;

import hu.bme.backend.entities.ShoppingItem;

import java.math.BigDecimal;
import java.time.Instant;

public record ShoppingListItemResponse(
        Long id,
        String name,
        Long categoryId,
        String categoryName,
        BigDecimal quantity,
        String unit,
        String note,
        boolean purchased,
        Instant createdAt,
        boolean categoryMissing) {
    public static ShoppingListItemResponse from(ShoppingItem item, boolean categoryMissing) {
        return new
                ShoppingListItemResponse(
                item.getId(),
                item.getProductName(),
                item.getCategory().getId(),
                item.getCategory().getName(),
                item.getQty(),
                item.getUnit(),
                item.getComment(),
                item.isPurchased(),
                item.getCreatedAt(),
                categoryMissing);
    }
}
