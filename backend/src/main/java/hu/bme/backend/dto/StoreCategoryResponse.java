package hu.bme.backend.dto;

import hu.bme.backend.entities.StoreCategory;

public record StoreCategoryResponse(
        Long id,
        Long storeId,
        Long categoryId,
        String categoryName,
        int position) {
    public static StoreCategoryResponse from(StoreCategory placement) {
        return new
                StoreCategoryResponse(
                placement.getId(),
                placement.getStore().getId(),
                placement.getCategory().getId(),
                placement.getCategory().getName(),
                placement.getPosition());
    }
}
