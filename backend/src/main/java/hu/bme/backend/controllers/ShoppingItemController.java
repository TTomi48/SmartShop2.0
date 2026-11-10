package hu.bme.backend.controllers;

import hu.bme.backend.dto.SetPurchasedRequest;
import hu.bme.backend.dto.ShoppingItemResponse;
import hu.bme.backend.dto.ShoppingListItemResponse;
import hu.bme.backend.services.ShoppingItemService;
import hu.bme.backend.services.records.ShoppingItemRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ShoppingItemController {
    private final ShoppingItemService shoppingItemService;

    @GetMapping("/api/items")
    public List<ShoppingItemResponse> getAllItems() {
        return shoppingItemService.getAllItems().stream().map(ShoppingItemResponse::from).toList();
    }

    @GetMapping("/api/items/{id}")
    public ShoppingItemResponse getItem(@PathVariable Long id) {
        return ShoppingItemResponse.from(shoppingItemService.getItemById(id));
    }

    @PostMapping("/api/items")
    @ResponseStatus(HttpStatus.CREATED)
    public ShoppingItemResponse create(@RequestBody ShoppingItemRequest request) {
        return ShoppingItemResponse.from(shoppingItemService.createItem(request));
    }

    @PutMapping("/api/items/{id}")
    public ShoppingItemResponse update(@PathVariable Long id, @RequestBody ShoppingItemRequest request) {
        return ShoppingItemResponse.from(shoppingItemService.updateItem(id, request));
    }

    @DeleteMapping("/api/items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        shoppingItemService.deleteItem(id);
    }

    @PatchMapping("/api/items/{id}/purchased")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setPurchased(@PathVariable Long id, @Valid @RequestBody SetPurchasedRequest request) {
        shoppingItemService.setPurchased(id, request.purchased());
    }

    @PostMapping("/api/items/reset-purchased")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPurchased() {
        shoppingItemService.resetShopping();
    }

    @DeleteMapping("/api/items/purchased")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePurchased() {
        shoppingItemService.deletePurchasedItems();
    }

    @GetMapping("/api/stores/{storeId}/shopping-list")
    public List<ShoppingListItemResponse> getShoppingList(@PathVariable Long storeId,
            @RequestParam(defaultValue = "false") boolean remainingOnly) {
        return shoppingItemService.getShoppingListResponse(storeId, remainingOnly);
    }
}
