package hu.bme.backend.services;

import hu.bme.backend.entities.ShoppingItem;
import hu.bme.backend.exceptions.ResourceNotFoundException;
import hu.bme.backend.repositories.CategoryRepository;
import hu.bme.backend.repositories.ShoppingItemRepository;
import hu.bme.backend.services.records.ShoppingItemRequest;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShoppingItemService {
    private final ShoppingItemRepository shoppingItemRepository;
    private final CategoryRepository categoryRepository;
    private final StoreService storeService;
    private final StoreCategoryService storeCategoryService;

    public List<ShoppingItem> getAllItems() {
        return shoppingItemRepository.findAll();
    }

    public ShoppingItem getItemById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        return shoppingItemRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Item with id " + id + " not found"));
    }

    @Transactional
    public ShoppingItem createItem(ShoppingItemRequest dto) {
        validateInputs(dto);

        var item = new ShoppingItem();
        saveItem(dto, item);
        return item;
    }

    public ShoppingItem updateItem(
            Long id,
            ShoppingItemRequest dto) {

        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }

        validateInputs(dto);

        var item = getItemById(id);

        saveItem(dto, item);

        return item;
    }

    public void validateInputs(ShoppingItemRequest dto) {
        if (dto == null) {
            throw new IllegalArgumentException("createShoppingItemRequest is null");
        }

        if (dto.name() == null
                || dto.name().isBlank()
                || dto.categoryId() == null
        ) {
            throw new IllegalArgumentException("createShoppingItemRequest is null");
        }

        if (dto.quantity() != null && dto.quantity().signum() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        if (dto.note() != null && dto.note().length() > 250) {
            throw new IllegalArgumentException("createShoppingItemRequest is too long");
        }
    }

    public void saveItem(ShoppingItemRequest dto, ShoppingItem item) {
        var category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category with id " + dto.categoryId() + " not found"));

        item.setProductName(dto.name());
        item.setCategory(category);
        item.setQty(dto.quantity());
        item.setUnit(dto.unit());
        item.setComment(dto.note());
        shoppingItemRepository.save(item);
    }

    @Transactional
    public void deleteItem(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        var item = getItemById(id);
        shoppingItemRepository.delete(item);
    }

    @Transactional
    public void setPurchased(Long id, boolean purchased) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        var item = getItemById(id);
        item.setPurchased(purchased);
        shoppingItemRepository.save(item);
    }

    @Transactional
    public void resetShopping() {
        shoppingItemRepository
                .findAll()
                .forEach(item -> {
                    item.setPurchased(false);
                });
    }

    @Transactional
    public void deletePurchasedItems() {
        shoppingItemRepository.deleteByPurchasedTrue();
    }


    public List<ShoppingItem> getShoppingList(Long storeId) {
        if (storeId == null) {
            throw new IllegalArgumentException("storeId is null");
        }

        var store = storeCategoryService.getStoreCategories(storeId);

        var positions = store.stream()
                .collect(Collectors.toMap(
                        storeCategory -> storeCategory.getCategory().getId(),
                        storeCategory -> storeCategory.getPosition()
                ));

        var items = new ArrayList<>(
                shoppingItemRepository.findAllByOrderByCreatedAtAscIdAsc()
        );

        items.sort(Comparator.comparingInt(
                item -> positions.getOrDefault(
                        item.getCategory().getId(),
                        Integer.MAX_VALUE
                )
        ));

        return items;
    }
}
