package hu.bme.backend.controllers;

import hu.bme.backend.dto.AddStoreCategoryRequest;
import hu.bme.backend.dto.StoreCategoryResponse;
import hu.bme.backend.services.StoreCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores/{storeId}/categories")
@RequiredArgsConstructor
public class StoreCategoryController {
    private final StoreCategoryService storeCategoryService;

    @GetMapping
    public List<StoreCategoryResponse> getCategories(@PathVariable Long storeId) {
        return storeCategoryService.getStoreCategories(storeId).stream()
                .map(StoreCategoryResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StoreCategoryResponse add(@PathVariable Long storeId,
                                     @Valid @RequestBody AddStoreCategoryRequest request) {
        return StoreCategoryResponse.from(
                storeCategoryService.addCategoryToStore(storeId, request.categoryId()));
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long storeId, @PathVariable Long categoryId) {
        storeCategoryService.removeCategoryFromStore(storeId, categoryId);
    }

    @PatchMapping("/{categoryId}/move-up")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void moveUp(@PathVariable Long storeId, @PathVariable Long categoryId) {
        storeCategoryService.moveCategoryUp(storeId, categoryId);
    }

    @PatchMapping("/{categoryId}/move-down")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void moveDown(@PathVariable Long storeId, @PathVariable Long categoryId) {
        storeCategoryService.moveCategoryDown(storeId, categoryId);
    }
}
