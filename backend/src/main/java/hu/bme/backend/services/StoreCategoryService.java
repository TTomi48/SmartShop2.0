
package hu.bme.backend.services;

import hu.bme.backend.entities.StoreCategory;
import hu.bme.backend.exceptions.DuplicateResourcesException;
import hu.bme.backend.exceptions.ResourceNotFoundException;
import hu.bme.backend.repositories.StoreCategoryRepository;
import hu.bme.backend.services.records.StoreAndCategory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreCategoryService {
    private final StoreCategoryRepository storeCategoryRepository;
    private final CategoryService categoryService;
    private final StoreService storeService;

    public List<StoreCategory> getStoreCategories(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        storeService.getStoreById(id);

        return storeCategoryRepository.findByStoreIdOrderByPositionAsc(id);
    }

    @Transactional
    public StoreCategory addCategoryToStore(Long storeId, Long categoryId) {
        var storeAndCategory = validateInputsAndReturn(storeId, categoryId);

        if (storeAndCategory.isExist()) {
            throw new DuplicateResourcesException("Store and Category already exist");
        }

        var newPosition = findLastPositionPlusOneOrZero(storeId);

        var storeCategory = new StoreCategory();
        storeCategory.setStore(storeAndCategory.store());
        storeCategory.setCategory(storeAndCategory.category());
        storeCategory.setPosition(newPosition);

        return storeCategoryRepository.save(storeCategory);
    }

    @Transactional
    public void removeCategoryFromStore(Long storeId, Long categoryId) {
        var storeAndCategory = validateInputsAndReturn(storeId, categoryId);
        if (!storeAndCategory.isExist()) {
            throw new ResourceNotFoundException("Store and Category does not exist");
        }

        var storeCategory = storeCategoryRepository
                .findByStoreIdAndCategoryId(storeId, categoryId)
                .orElse(null);

        if (storeCategory == null) {
            throw new ResourceNotFoundException("Store and Category does not exist");
        }

        var categories = getStoreCategories(storeId);

        storeCategoryRepository.delete(storeCategory);

        int position = 0;
        for (var category : categories) {
            if (category.getId().equals(storeCategory.getId())) {
                continue;
            }

            category.setPosition(position++);
            storeCategoryRepository.save(category);
        }
    }

    @Transactional
    public void moveCategoryUp(Long storeId, Long categoryId) {
        moveCategory(storeId, categoryId, -1);
    }

    @Transactional
    public void moveCategoryDown(Long storeId, Long categoryId) {
        moveCategory(storeId, categoryId, 1);
    }

    private void moveCategory(Long storeId, Long categoryId, int direction) {
        var storeAndCategory = validateInputsAndReturn(storeId, categoryId);

        if (!storeAndCategory.isExist()) {
            throw new ResourceNotFoundException("Store and Category does not exist");
        }

        var selected = storeCategoryRepository
                .findByStoreIdAndCategoryId(storeId, categoryId)
                .orElse(null);

        if (selected == null) {
            throw new ResourceNotFoundException("Store and Category does not exist");
        }

        var categories = getStoreCategories(storeId);

        int index = -1;
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getId().equals(selected.getId())) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            throw new ResourceNotFoundException("Store and Category does not exist");
        }

        int newIndex = index + direction;

        if (newIndex < 0 || newIndex >= categories.size()) {
            return;
        }

        var neighbour = categories.get(newIndex);

        int oldPosition = selected.getPosition();
        selected.setPosition(neighbour.getPosition());
        neighbour.setPosition(oldPosition);

        storeCategoryRepository.save(selected);
        storeCategoryRepository.save(neighbour);
    }

    private Integer findLastPositionPlusOneOrZero(Long storeId) {
        var categories = getStoreCategories(storeId);

        if (categories.isEmpty()) {
            return 0;
        }

        return categories.getLast().getPosition() + 1;
    }

    private StoreAndCategory validateInputsAndReturn(Long storeId, Long categoryId) {
        if (storeId == null || categoryId == null) {
            throw new IllegalArgumentException("storeId or categoryId is null");
        }

        var store = storeService.getStoreById(storeId);
        var category = categoryService.getCategoryById(categoryId);
        var isExist = storeCategoryRepository
                .existsByStoreIdAndCategoryId(storeId, categoryId);

        return new StoreAndCategory(store, category, isExist);
    }
}
