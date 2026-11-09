package hu.bme.backend.services.records;

import java.math.BigDecimal;

public record ShoppingItemRequest(
        String name,
        Long categoryId,
        BigDecimal quantity,
        String unit,
        String note) {
}
