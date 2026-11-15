package hu.bme.backend.services;

import hu.bme.backend.entities.Category;
import hu.bme.backend.entities.Store;
import hu.bme.backend.repositories.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InitialDataServiceTest {
    @Mock
    StoreRepository stores;
    @Mock
    CategoryRepository categories;
    @Mock
    StoreCategoryRepository placements;
    @Mock
    ShoppingItemRepository items;
    @Mock
    StoreService storeService;
    @Mock
    CategoryService categoryService;
    @Mock
    StoreCategoryService layoutService;
    @InjectMocks
    InitialDataService initializer;

    @Test
    void freshDatabaseGetsTwoStoresWithOppositeGlobalCategoryOrders() {
        var nextId = new AtomicLong();
        when(categoryService.createCategory(anyString())).thenAnswer(invocation -> {
            var category = new Category();
            category.setId(nextId.incrementAndGet());
            return category;
        });
        var first = new Store();
        first.setId(10L);
        var second = new Store();
        second.setId(20L);
        when(storeService.createStore("Mintaüzlet A")).thenReturn(first);
        when(storeService.createStore("Mintaüzlet B")).thenReturn(second);

        initializer.run(null);

        verify(categoryService, times(6)).createCategory(anyString());
        var order = inOrder(layoutService);
        for (long id = 1; id <= 6; id++) order.verify(layoutService).addCategoryToStore(10L, id);
        for (long id = 6; id >= 1; id--) order.verify(layoutService).addCategoryToStore(20L, id);
        verifyNoMoreInteractions(layoutService);
    }

    @Test
    void restartWithExistingStoreDoesNotChangeUserData() {
        when(stores.count()).thenReturn(1L);
        initializer.run(null);
        verifyNoInteractions(storeService, categoryService, layoutService);
    }

    @Test
    void partiallyPopulatedDatabaseIsAlsoLeftUntouched() {
        when(categories.count()).thenReturn(1L);
        initializer.run(null);
        verifyNoInteractions(storeService, categoryService, layoutService);
    }

    @Test
    void existingItemsPreventSeedingEvenWithoutStores() {
        when(items.count()).thenReturn(1L);
        initializer.run(null);
        verifyNoInteractions(storeService, categoryService, layoutService);
    }
}
