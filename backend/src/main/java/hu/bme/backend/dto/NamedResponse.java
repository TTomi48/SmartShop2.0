package hu.bme.backend.dto;

import hu.bme.backend.entities.Category;
import hu.bme.backend.entities.Store;

public record NamedResponse(Long id, String name) {
    public static NamedResponse from(Store store) {
        return new NamedResponse(store.getId(), store.getName());
    }

    public static NamedResponse from(Category category) {
        return new NamedResponse(category.getId(), category.getName());
    }
}
