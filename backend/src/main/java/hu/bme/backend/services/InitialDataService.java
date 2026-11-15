package hu.bme.backend.services;

import hu.bme.backend.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * EF1–EF2: seed a fresh database once; never change an existing layout.
 */
@Service
@RequiredArgsConstructor
public class InitialDataService implements ApplicationRunner {
    private final StoreRepository stores;
    private final CategoryRepository categories;
    private final StoreCategoryRepository placements;
    private final ShoppingItemRepository items;
    private final StoreService storeService;
    private final CategoryService categoryService;
    private final StoreCategoryService layoutService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (stores.count() != 0 || categories.count() != 0
                || placements.count() != 0 || items.count() != 0) return;

        var categoryIds = List.of("Zöldség-gyümölcs", "Pékáru", "Tejtermékek",
                        "Húsáru", "Italok", "Háztartás").stream()
                .map(name -> categoryService.createCategory(name).getId()).toList();
        var first = storeService.createStore("Mintaüzlet A");
        var second = storeService.createStore("Mintaüzlet B");
        for (var categoryId : categoryIds) {
            layoutService.addCategoryToStore(first.getId(), categoryId);
        }
        for (var categoryId : categoryIds.reversed()) {
            layoutService.addCategoryToStore(second.getId(), categoryId);
        }
    }
}
