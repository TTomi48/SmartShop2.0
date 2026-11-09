package hu.bme.backend.services;

import hu.bme.backend.entities.Category;
import hu.bme.backend.entities.Store;
import hu.bme.backend.entities.StoreCategory;
import hu.bme.backend.exceptions.DuplicateResourcesException;
import hu.bme.backend.exceptions.ResourceNotFoundException;
import hu.bme.backend.repositories.StoreCategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreCategoryServiceTest {

    @Mock
    private StoreCategoryRepository storeCategoryRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private StoreService storeService;

    @InjectMocks
    private StoreCategoryService service;

    @Test
    void getStoreCategoriesReturnsTheStoreLayoutInPositionOrder() {
        var store = store(1L);
        var first = placement(11L, store, category(101L), 0);
        var second = placement(12L, store, category(102L), 1);
        when(storeService.getStoreById(1L)).thenReturn(store);
        when(storeCategoryRepository.findByStoreIdOrderByPositionAsc(1L))
                .thenReturn(List.of(first, second));

        var result = service.getStoreCategories(1L);

        assertThat(result).containsExactly(first, second);
    }

    @Test
    void addCategoryToEmptyStoreAssignsPositionZero() {
        var store = store(1L);
        var category = category(101L);
        stubResources(store, category);
        when(storeCategoryRepository.findByStoreIdOrderByPositionAsc(1L))
                .thenReturn(List.of());
        returnSavedPlacement();

        var result = service.addCategoryToStore(1L, 101L);

        assertThat(result.getStore()).isSameAs(store);
        assertThat(result.getCategory()).isSameAs(category);
        assertThat(result.getPosition()).isZero();
        verify(storeCategoryRepository).save(result);
    }

    @Test
    void addSecondCategoryAssignsPositionOneAndPreservesTheFirst() {
        var store = store(1L);
        var first = placement(11L, store, category(101L), 0);
        var secondCategory = category(102L);
        stubResources(store, secondCategory);
        when(storeCategoryRepository.findByStoreIdOrderByPositionAsc(1L))
                .thenReturn(List.of(first));
        returnSavedPlacement();

        var result = service.addCategoryToStore(1L, 102L);

        assertThat(result.getCategory()).isSameAs(secondCategory);
        assertThat(result.getStore()).isSameAs(store);
        assertThat(result.getPosition()).isEqualTo(1);
        assertThat(first.getPosition()).isZero();
        verify(storeCategoryRepository).save(result);
        verify(storeCategoryRepository, never()).save(first);
    }

    @Test
    void addCategoryAlreadyPlacedInTheStoreThrowsDuplicateResourcesException() {
        stubResources(store(1L), category(101L));
        when(storeCategoryRepository.existsByStoreIdAndCategoryId(1L, 101L))
                .thenReturn(true);

        assertThatThrownBy(() -> service.addCategoryToStore(1L, 101L))
                .isInstanceOf(DuplicateResourcesException.class);

        verify(storeCategoryRepository, never()).save(any(StoreCategory.class));
        verify(storeCategoryRepository, never()).delete(any(StoreCategory.class));
    }

    @Test
    void addTheSameGlobalCategoryToTwoDifferentStoresCreatesSeparatePlacements() {
        var firstStore = store(1L);
        var secondStore = store(2L);
        var sharedCategory = category(101L);
        when(storeService.getStoreById(1L)).thenReturn(firstStore);
        when(storeService.getStoreById(2L)).thenReturn(secondStore);
        when(categoryService.getCategoryById(101L)).thenReturn(sharedCategory);
        when(storeCategoryRepository.findByStoreIdOrderByPositionAsc(1L))
                .thenReturn(List.of());
        when(storeCategoryRepository.findByStoreIdOrderByPositionAsc(2L))
                .thenReturn(List.of());
        returnSavedPlacement();

        var first = service.addCategoryToStore(1L, 101L);
        var second = service.addCategoryToStore(2L, 101L);

        assertThat(first).isNotSameAs(second);
        assertThat(first.getStore()).isSameAs(firstStore);
        assertThat(second.getStore()).isSameAs(secondStore);
        assertThat(first.getCategory()).isSameAs(sharedCategory);
        assertThat(second.getCategory()).isSameAs(sharedCategory);
        assertThat(first.getPosition()).isZero();
        assertThat(second.getPosition()).isZero();
        verify(storeCategoryRepository).existsByStoreIdAndCategoryId(1L, 101L);
        verify(storeCategoryRepository).existsByStoreIdAndCategoryId(2L, 101L);
        verify(storeCategoryRepository).save(first);
        verify(storeCategoryRepository).save(second);
    }

    @Test
    void removeMiddleCategoryRenumbersTheRemainingPositionsStartingAtZero() {
        var layout = layout(store(1L));
        var first = layout.get(0);
        var middle = layout.get(1);
        var last = layout.get(2);
        stubExistingPlacement(middle, layout);

        service.removeCategoryFromStore(1L, middle.getCategory().getId());

        assertThat(List.of(first, last)).extracting(StoreCategory::getPosition)
                .containsExactly(0, 1);
        verify(storeCategoryRepository).delete(middle);
        verify(storeCategoryRepository).save(first);
        verify(storeCategoryRepository).save(last);
        verify(storeCategoryRepository, never()).save(middle);
    }

    @Test
    void moveFirstCategoryUpLeavesTheLayoutUnchanged() {
        var layout = layout(store(1L));
        var first = layout.getFirst();
        stubExistingPlacement(first, layout);

        service.moveCategoryUp(1L, first.getCategory().getId());

        assertThat(layout).extracting(StoreCategory::getPosition).containsExactly(0, 1, 2);
        verify(storeCategoryRepository, never()).save(any(StoreCategory.class));
        verify(storeCategoryRepository, never()).delete(any(StoreCategory.class));
    }

    @Test
    void moveLastCategoryDownLeavesTheLayoutUnchanged() {
        var layout = layout(store(1L));
        var last = layout.getLast();
        stubExistingPlacement(last, layout);

        service.moveCategoryDown(1L, last.getCategory().getId());

        assertThat(layout).extracting(StoreCategory::getPosition).containsExactly(0, 1, 2);
        verify(storeCategoryRepository, never()).save(any(StoreCategory.class));
        verify(storeCategoryRepository, never()).delete(any(StoreCategory.class));
    }

    @Test
    void moveCategoryUpSwapsPositionsOnlyWithThePreviousCategory() {
        var layout = layout(store(1L));
        var first = layout.get(0);
        var middle = layout.get(1);
        var last = layout.get(2);
        stubExistingPlacement(middle, layout);

        service.moveCategoryUp(1L, middle.getCategory().getId());

        assertThat(List.of(first, middle, last)).extracting(StoreCategory::getPosition)
                .containsExactly(1, 0, 2);
        verify(storeCategoryRepository).save(middle);
        verify(storeCategoryRepository).save(first);
        verify(storeCategoryRepository, never()).save(last);
    }

    @Test
    void moveCategoryDownSwapsPositionsOnlyWithTheNextCategory() {
        var layout = layout(store(1L));
        var first = layout.get(0);
        var middle = layout.get(1);
        var last = layout.get(2);
        stubExistingPlacement(middle, layout);

        service.moveCategoryDown(1L, middle.getCategory().getId());

        assertThat(List.of(first, middle, last)).extracting(StoreCategory::getPosition)
                .containsExactly(0, 2, 1);
        verify(storeCategoryRepository).save(middle);
        verify(storeCategoryRepository).save(last);
        verify(storeCategoryRepository, never()).save(first);
    }

    @Test
    void operationsRejectUnknownStore() {
        when(storeService.getStoreById(99L))
                .thenThrow(new ResourceNotFoundException("Store not found"));

        assertThatThrownBy(() -> service.addCategoryToStore(99L, 101L))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.removeCategoryFromStore(99L, 101L))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.moveCategoryUp(99L, 101L))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.moveCategoryDown(99L, 101L))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.getStoreCategories(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(categoryService, storeCategoryRepository);
    }

    @Test
    void operationsRejectUnknownCategory() {
        when(storeService.getStoreById(1L)).thenReturn(store(1L));
        when(categoryService.getCategoryById(99L))
                .thenThrow(new ResourceNotFoundException("Category not found"));

        assertThatThrownBy(() -> service.addCategoryToStore(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.removeCategoryFromStore(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.moveCategoryUp(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.moveCategoryDown(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(storeCategoryRepository);
    }

    @Test
    void removeAndMoveRejectCategoryNotPlacedInTheStore() {
        stubResources(store(1L), category(101L));

        assertThatThrownBy(() -> service.removeCategoryFromStore(1L, 101L))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.moveCategoryUp(1L, 101L))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.moveCategoryDown(1L, 101L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(storeCategoryRepository, never()).save(any(StoreCategory.class));
        verify(storeCategoryRepository, never()).delete(any(StoreCategory.class));
    }

    @Test
    void addCategoryRejectsNullIdentifiers() {
        assertThatThrownBy(() -> service.addCategoryToStore(null, 101L))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.addCategoryToStore(1L, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.addCategoryToStore(null, null))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(storeService, categoryService, storeCategoryRepository);
    }

    @Test
    void removeCategoryRejectsNullIdentifiers() {
        assertThatThrownBy(() -> service.removeCategoryFromStore(null, 101L))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.removeCategoryFromStore(1L, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.removeCategoryFromStore(null, null))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(storeService, categoryService, storeCategoryRepository);
    }

    @Test
    void moveCategoryUpRejectsNullIdentifiers() {
        assertThatThrownBy(() -> service.moveCategoryUp(null, 101L))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.moveCategoryUp(1L, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.moveCategoryUp(null, null))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(storeService, categoryService, storeCategoryRepository);
    }

    @Test
    void moveCategoryDownRejectsNullIdentifiers() {
        assertThatThrownBy(() -> service.moveCategoryDown(null, 101L))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.moveCategoryDown(1L, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.moveCategoryDown(null, null))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(storeService, categoryService, storeCategoryRepository);
    }

    @Test
    void getStoreCategoriesRejectsNullStoreId() {
        assertThatThrownBy(() -> service.getStoreCategories(null))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(storeService, categoryService, storeCategoryRepository);
    }

    @Test
    void removingCategoryDoesNotChangeAnotherStoreLayout() {
        var layout = layout(store(1L));
        stubExistingPlacement(layout.get(1), layout);
        var otherLayout = stubOtherStoreLayout(layout);

        service.removeCategoryFromStore(1L, 102L);

        assertThat(List.of(layout.get(0), layout.get(2)))
                .extracting(StoreCategory::getPosition).containsExactly(0, 1);
        assertOtherStoreUnchanged(otherLayout);
    }

    @Test
    void movingCategoryUpDoesNotChangeAnotherStoreLayout() {
        var layout = layout(store(1L));
        stubExistingPlacement(layout.get(1), layout);
        var otherLayout = stubOtherStoreLayout(layout);

        service.moveCategoryUp(1L, 102L);

        assertThat(layout).extracting(StoreCategory::getPosition).containsExactly(1, 0, 2);
        assertOtherStoreUnchanged(otherLayout);
    }

    @Test
    void movingCategoryDownDoesNotChangeAnotherStoreLayout() {
        var layout = layout(store(1L));
        stubExistingPlacement(layout.get(1), layout);
        var otherLayout = stubOtherStoreLayout(layout);

        service.moveCategoryDown(1L, 102L);

        assertThat(layout).extracting(StoreCategory::getPosition).containsExactly(0, 2, 1);
        assertOtherStoreUnchanged(otherLayout);
    }

    private List<StoreCategory> stubOtherStoreLayout(List<StoreCategory> layout) {
        var otherStore = store(2L);
        var otherLayout = List.of(
                placement(21L, otherStore, layout.get(2).getCategory(), 0),
                placement(22L, otherStore, layout.get(1).getCategory(), 1),
                placement(23L, otherStore, layout.get(0).getCategory(), 2)
        );
        when(storeService.getStoreById(2L)).thenReturn(otherStore);
        when(storeCategoryRepository.findByStoreIdOrderByPositionAsc(2L)).thenReturn(otherLayout);
        return otherLayout;
    }

    private void assertOtherStoreUnchanged(List<StoreCategory> otherLayout) {
        assertThat(service.getStoreCategories(2L)).containsExactlyElementsOf(otherLayout);
        assertThat(otherLayout).extracting(StoreCategory::getPosition).containsExactly(0, 1, 2);
        assertThat(otherLayout).extracting(link -> link.getCategory().getId())
                .containsExactly(103L, 102L, 101L);
        for (var placement : otherLayout) {
            verify(storeCategoryRepository, never()).save(placement);
            verify(storeCategoryRepository, never()).delete(placement);
        }
    }

    private void stubResources(Store store, Category category) {
        when(storeService.getStoreById(store.getId())).thenReturn(store);
        when(categoryService.getCategoryById(category.getId())).thenReturn(category);
    }

    private void stubExistingPlacement(StoreCategory placement, List<StoreCategory> layout) {
        stubResources(placement.getStore(), placement.getCategory());
        var storeId = placement.getStore().getId();
        var categoryId = placement.getCategory().getId();
        when(storeCategoryRepository.existsByStoreIdAndCategoryId(storeId, categoryId))
                .thenReturn(true);
        when(storeCategoryRepository.findByStoreIdAndCategoryId(storeId, categoryId))
                .thenReturn(Optional.of(placement));
        when(storeCategoryRepository.findByStoreIdOrderByPositionAsc(storeId)).thenReturn(layout);
    }

    private void returnSavedPlacement() {
        when(storeCategoryRepository.save(any(StoreCategory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private static List<StoreCategory> layout(Store store) {
        return List.of(
                placement(11L, store, category(101L), 0),
                placement(12L, store, category(102L), 1),
                placement(13L, store, category(103L), 2)
        );
    }

    private static Store store(Long id) {
        var store = new Store();
        store.setId(id);
        store.setName("Store " + id);
        return store;
    }

    private static Category category(Long id) {
        var category = new Category();
        category.setId(id);
        category.setName("Category " + id);
        return category;
    }

    private static StoreCategory placement(Long id, Store store, Category category, int position) {
        var placement = new StoreCategory();
        placement.setId(id);
        placement.setStore(store);
        placement.setCategory(category);
        placement.setPosition(position);
        return placement;
    }

}
