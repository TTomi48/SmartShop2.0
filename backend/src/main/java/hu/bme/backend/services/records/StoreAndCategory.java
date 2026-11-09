package hu.bme.backend.services.records;

import hu.bme.backend.entities.Category;
import hu.bme.backend.entities.Store;

public record StoreAndCategory(
        Store store,
        Category category,
        boolean isExist
) {
}
