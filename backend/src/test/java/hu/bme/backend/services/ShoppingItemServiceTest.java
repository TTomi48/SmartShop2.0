package hu.bme.backend.services;

import hu.bme.backend.entities.Category;
import hu.bme.backend.entities.ShoppingItem;
import hu.bme.backend.entities.StoreCategory;
import hu.bme.backend.exceptions.ResourceNotFoundException;
import hu.bme.backend.repositories.CategoryRepository;
import hu.bme.backend.repositories.ShoppingItemRepository;
import hu.bme.backend.services.records.ShoppingItemRequest;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShoppingItemServiceTest {

    @Mock
    private ShoppingItemRepository shoppingItemRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private StoreService storeService;

    @Mock
    private StoreCategoryService storeCategoryService;

    @InjectMocks
    private ShoppingItemService service;

    @Test
    void getAllItemsReturnsTheStoredItemsIncludingPurchasedOnes() {
        var first = item(1L, category(1L));
        var second = item(2L, category(1L));
        second.setPurchased(true);
        when(shoppingItemRepository.findAll()).thenReturn(List.of(first, second));

        assertEquals(List.of(first, second), service.getAllItems());
    }

    @Test
    void getAllItemsReturnsAnEmptyListWhenThereAreNoItems() {
        when(shoppingItemRepository.findAll()).thenReturn(List.of());

        assertTrue(service.getAllItems().isEmpty());
    }

    @Test
    void getItemByIdReturnsTheExistingItem() {
        var item = item(1L, category(1L));
        when(shoppingItemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertSame(item, service.getItemById(1L));
    }

    @Test
    void getItemByIdRejectsUnknownItem() {
        when(shoppingItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.getItemById(99L));
    }

    @Test
    void createItemAcceptsOnlyTheRequiredFields() {
        var category = category(1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        var result = service.createItem(request());

        assertEquals("Milk", result.getProductName());
        assertSame(category, result.getCategory());
        assertNull(result.getQty());
        assertNull(result.getUnit());
        assertNull(result.getComment());
        assertFalse(result.isPurchased());
        verify(shoppingItemRepository).save(result);
    }

    @Test
    void createItemAcceptsDecimalQuantityCustomUnitAnd250CharacterNote() {
        var request = new ShoppingItemRequest("Flowers", 1L, new BigDecimal("1.5"),
                "bunch", "a".repeat(250));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category(1L)));

        var result = service.createItem(request);

        assertEquals("Flowers", result.getProductName());
        assertEquals(new BigDecimal("1.5"), result.getQty());
        assertEquals("bunch", result.getUnit());
        assertEquals("a".repeat(250), result.getComment());
        verify(shoppingItemRepository).save(result);
    }

    @Test
    void createItemAcceptsQuantityWithoutUnitOrNote() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category(1L)));

        var result = service.createItem(
                new ShoppingItemRequest("Milk", 1L, BigDecimal.ONE, null, null));

        assertEquals(BigDecimal.ONE, result.getQty());
        assertNull(result.getUnit());
        assertNull(result.getComment());
    }

    @Test
    void createItemAcceptsUnitWithoutQuantityOrNote() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category(1L)));

        var result = service.createItem(new ShoppingItemRequest("Milk", 1L, null, "l", null));

        assertNull(result.getQty());
        assertEquals("l", result.getUnit());
        assertNull(result.getComment());
    }

    @Test
    void createItemAcceptsNoteWithoutQuantityOrUnit() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category(1L)));

        var result = service.createItem(
                new ShoppingItemRequest("Milk", 1L, null, null, "Lactose free"));

        assertNull(result.getQty());
        assertNull(result.getUnit());
        assertEquals("Lactose free", result.getComment());
    }

    @Test
    void createItemsWithTheSameNameCreatesSeparateListEntries() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category(1L)));

        var first = service.createItem(request());
        var second = service.createItem(request());

        assertNotSame(second, first);
        assertEquals(second.getProductName(), first.getProductName());
        verify(shoppingItemRepository).save(first);
        verify(shoppingItemRepository).save(second);
    }

    @Test
    void createItemRejectsMissingRequiredFieldsWithoutSaving() {
        assertThrows(IllegalArgumentException.class, () -> service.createItem(null));
        assertThrows(IllegalArgumentException.class, () -> service.createItem(
                new ShoppingItemRequest(null, 1L, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> service.createItem(
                new ShoppingItemRequest("", 1L, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> service.createItem(
                new ShoppingItemRequest("   ", 1L, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> service.createItem(
                new ShoppingItemRequest("Milk", null, null, null, null)));

        verifyNoInteractions(categoryRepository, shoppingItemRepository);
    }

    @Test
    void createItemRejectsZeroAndNegativeQuantityWithoutSaving() {
        assertThrows(IllegalArgumentException.class, () -> service.createItem(
                new ShoppingItemRequest("Milk", 1L, BigDecimal.ZERO, null, null)));
        assertThrows(IllegalArgumentException.class, () -> service.createItem(
                new ShoppingItemRequest("Milk", 1L, new BigDecimal("-0.5"), null, null)));

        verifyNoInteractions(categoryRepository, shoppingItemRepository);
    }

    @Test
    void createItemRejects251CharacterNoteWithoutSaving() {
        assertThrows(IllegalArgumentException.class, () -> service.createItem(
                new ShoppingItemRequest("Milk", 1L, null, null, "a".repeat(251))));

        verifyNoInteractions(categoryRepository, shoppingItemRepository);
    }

    @Test
    void createItemRejectsUnknownCategoryWithoutSaving() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.createItem(request()));

        verifyNoInteractions(shoppingItemRepository);
    }

    @Test
    void updateItemChangesItsFieldsAndPreservesIdentityPurchaseStateAndCreationTime() {
        var item = item(1L, category(1L));
        item.setPurchased(true);
        var createdAt = item.getCreatedAt();
        var nextCategory = category(2L);
        when(shoppingItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(nextCategory));

        var result = service.updateItem(1L,
                new ShoppingItemRequest("Bread", 2L, BigDecimal.ONE, "piece", "Fresh"));

        assertSame(item, result);
        assertEquals(1L, result.getId());
        assertEquals("Bread", result.getProductName());
        assertSame(nextCategory, result.getCategory());
        assertEquals(BigDecimal.ONE, result.getQty());
        assertEquals("piece", result.getUnit());
        assertEquals("Fresh", result.getComment());
        assertTrue(result.isPurchased());
        assertEquals(createdAt, result.getCreatedAt());
        verify(shoppingItemRepository).save(item);
    }

    @Test
    void updateItemCanClearAllOptionalFields() {
        var category = category(1L);
        var item = item(1L, category);
        item.setQty(BigDecimal.ONE);
        item.setUnit("l");
        item.setComment("Fresh");
        when(shoppingItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        service.updateItem(1L, request());

        assertNull(item.getQty());
        assertNull(item.getUnit());
        assertNull(item.getComment());
        verify(shoppingItemRepository).save(item);
    }

    @Test
    void updateItemRejectsInvalidInputBeforeReadingOrChangingItems() {
        assertThrows(IllegalArgumentException.class, () -> service.updateItem(1L, null));
        assertThrows(IllegalArgumentException.class, () -> service.updateItem(1L,
                new ShoppingItemRequest("Milk", 1L, BigDecimal.ZERO, null, null)));
        assertThrows(IllegalArgumentException.class, () -> service.updateItem(1L,
                new ShoppingItemRequest("Milk", 1L, null, null, "a".repeat(251))));

        verifyNoInteractions(shoppingItemRepository, categoryRepository);
    }

    @Test
    void updateItemWithUnknownCategoryLeavesTheItemUnchanged() {
        var category = category(1L);
        var item = item(1L, category);
        item.setQty(BigDecimal.ONE);
        item.setUnit("l");
        item.setComment("Fresh");
        item.setPurchased(true);
        when(shoppingItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.updateItem(1L,
                new ShoppingItemRequest("Bread", 99L, null, null, null)));

        assertEquals("Item 1", item.getProductName());
        assertSame(category, item.getCategory());
        assertEquals(BigDecimal.ONE, item.getQty());
        assertEquals("l", item.getUnit());
        assertEquals("Fresh", item.getComment());
        assertTrue(item.isPurchased());
        verify(shoppingItemRepository, never()).save(any(ShoppingItem.class));
    }

    @Test
    void updateItemRejectsUnknownItemWithoutSaving() {
        when(shoppingItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.updateItem(99L, request()));

        verifyNoInteractions(categoryRepository);
        verify(shoppingItemRepository, never()).save(any(ShoppingItem.class));
    }

    @Test
    void operationsRejectNullIdentifiers() {
        assertThrows(IllegalArgumentException.class, () -> service.getItemById(null));
        assertThrows(IllegalArgumentException.class, () -> service.updateItem(null, request()));
        assertThrows(IllegalArgumentException.class, () -> service.deleteItem(null));
        assertThrows(IllegalArgumentException.class, () -> service.setPurchased(null, true));
        assertThrows(IllegalArgumentException.class, () -> service.getShoppingList(null));

        verifyNoInteractions(shoppingItemRepository, categoryRepository,
                storeService, storeCategoryService);
    }

    @Test
    void deleteItemDeletesTheRequestedItem() {
        var item = item(1L, category(1L));
        when(shoppingItemRepository.findById(1L)).thenReturn(Optional.of(item));

        service.deleteItem(1L);

        verify(shoppingItemRepository).delete(item);
    }

    @Test
    void deleteItemRejectsUnknownItemWithoutDeleting() {
        when(shoppingItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.deleteItem(99L));

        verify(shoppingItemRepository, never()).delete(any(ShoppingItem.class));
    }

    @Test
    void setPurchasedCanMarkAnItemAndUndoTheSelection() {
        var item = item(1L, category(1L));
        when(shoppingItemRepository.findById(1L)).thenReturn(Optional.of(item));

        service.setPurchased(1L, true);
        assertTrue(item.isPurchased());

        service.setPurchased(1L, false);
        assertFalse(item.isPurchased());
        verify(shoppingItemRepository, times(2)).save(item);
    }

    @Test
    void setPurchasedRejectsUnknownItemWithoutSaving() {
        when(shoppingItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.setPurchased(99L, true));

        verify(shoppingItemRepository, never()).save(any(ShoppingItem.class));
    }

    @Test
    void resetShoppingClearsEveryPurchaseFlagAndPreservesItemData() {
        var category = category(1L);
        var first = item(1L, category);
        var second = item(2L, category);
        first.setPurchased(true);
        first.setQty(BigDecimal.ONE);
        first.setUnit("l");
        first.setComment("Fresh");
        when(shoppingItemRepository.findAll()).thenReturn(List.of(first, second));

        service.resetShopping();

        assertFalse(first.isPurchased());
        assertFalse(second.isPurchased());
        assertEquals(1L, first.getId());
        assertEquals("Item 1", first.getProductName());
        assertSame(category, first.getCategory());
        assertEquals(BigDecimal.ONE, first.getQty());
        assertEquals("l", first.getUnit());
        assertEquals("Fresh", first.getComment());
        verify(shoppingItemRepository, never()).delete(any(ShoppingItem.class));
    }

    @Test
    void resetShoppingAcceptsAnEmptyList() {
        when(shoppingItemRepository.findAll()).thenReturn(List.of());

        service.resetShopping();

        verify(shoppingItemRepository).findAll();
        verify(shoppingItemRepository, never()).delete(any(ShoppingItem.class));
    }

    @Test
    void deletePurchasedItemsUsesThePurchasedOnlyDeleteOperation() {
        service.deletePurchasedItems();

        verify(shoppingItemRepository).deleteByPurchasedTrue();
        verify(shoppingItemRepository, never()).deleteAll();
    }

    @Test
    void getShoppingListUsesStoreOrderAndPreservesAdditionOrderWithinCategories() {
        var bakery = category(1L);
        var dairy = category(2L);
        var milk = item(1L, dairy);
        var bread = item(2L, bakery);
        var cheese = item(3L, dairy);
        var roll = item(4L, bakery);
        when(storeCategoryService.getStoreCategories(1L))
                .thenReturn(List.of(placement(bakery, 0), placement(dairy, 1)));
        when(shoppingItemRepository.findAllByOrderByCreatedAtAscIdAsc())
                .thenReturn(List.of(milk, bread, cheese, roll));

        assertEquals(List.of(bread, roll, milk, cheese), service.getShoppingList(1L));
    }

    @Test
    void getShoppingListPlacesMissingCategoriesLastAndAllowsPurchasingTheirItems() {
        var placedCategory = category(1L);
        var missing = item(1L, category(2L));
        var placed = item(2L, placedCategory);
        var anotherMissing = item(3L, category(3L));
        when(storeCategoryService.getStoreCategories(1L))
                .thenReturn(List.of(placement(placedCategory, 0)));
        when(shoppingItemRepository.findAllByOrderByCreatedAtAscIdAsc())
                .thenReturn(List.of(missing, placed, anotherMissing));
        when(shoppingItemRepository.findById(1L)).thenReturn(Optional.of(missing));

        assertEquals(List.of(placed, missing, anotherMissing), service.getShoppingList(1L));
        service.setPurchased(1L, true);

        assertTrue(missing.isPurchased());
        verify(shoppingItemRepository).save(missing);
    }

    @Test
    void differentStoreLayoutsChangeOnlyTheDisplayOrder() {
        var bakery = category(1L);
        var dairy = category(2L);
        var milk = item(1L, dairy);
        var bread = item(2L, bakery);
        milk.setPurchased(true);
        when(storeCategoryService.getStoreCategories(1L))
                .thenReturn(List.of(placement(bakery, 0), placement(dairy, 1)));
        when(storeCategoryService.getStoreCategories(2L))
                .thenReturn(List.of(placement(dairy, 0), placement(bakery, 1)));
        when(shoppingItemRepository.findAllByOrderByCreatedAtAscIdAsc())
                .thenReturn(List.of(milk, bread));

        assertEquals(List.of(bread, milk), service.getShoppingList(1L));
        assertEquals(List.of(milk, bread), service.getShoppingList(2L));
        assertTrue(milk.isPurchased());
        assertFalse(bread.isPurchased());
        verify(shoppingItemRepository, never()).save(any(ShoppingItem.class));
        verify(shoppingItemRepository, never()).delete(any(ShoppingItem.class));
    }

    @Test
    void getShoppingListPreservesAdditionOrderWhenNoCategoriesArePlaced() {
        var first = item(1L, category(1L));
        var second = item(2L, category(2L));
        when(storeCategoryService.getStoreCategories(1L)).thenReturn(List.of());
        when(shoppingItemRepository.findAllByOrderByCreatedAtAscIdAsc())
                .thenReturn(List.of(first, second));

        assertEquals(List.of(first, second), service.getShoppingList(1L));
    }

    @Test
    void getShoppingListReturnsAnEmptyListWhenThereAreNoItems() {
        when(storeCategoryService.getStoreCategories(1L)).thenReturn(List.of());
        when(shoppingItemRepository.findAllByOrderByCreatedAtAscIdAsc()).thenReturn(List.of());

        assertTrue(service.getShoppingList(1L).isEmpty());
    }

    @Test
    void getShoppingListRejectsUnknownStore() {
        when(storeCategoryService.getStoreCategories(99L))
                .thenThrow(new ResourceNotFoundException("Store not found"));

        assertThrows(ResourceNotFoundException.class, () -> service.getShoppingList(99L));

        verifyNoInteractions(shoppingItemRepository);
    }

    private static ShoppingItemRequest request() {
        return new ShoppingItemRequest("Milk", 1L, null, null, null);
    }

    private static Category category(Long id) {
        var category = new Category();
        category.setId(id);
        category.setName("Category " + id);
        return category;
    }

    private static ShoppingItem item(Long id, Category category) {
        var item = new ShoppingItem();
        item.setId(id);
        item.setProductName("Item " + id);
        item.setCategory(category);
        item.setCreatedAt(Instant.EPOCH.plusSeconds(id));
        return item;
    }

    private static StoreCategory placement(Category category, int position) {
        var placement = new StoreCategory();
        placement.setCategory(category);
        placement.setPosition(position);
        return placement;
    }
}
