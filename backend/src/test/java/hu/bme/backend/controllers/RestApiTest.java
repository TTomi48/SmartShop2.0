package hu.bme.backend.controllers;

import hu.bme.backend.dto.ShoppingListItemResponse;
import hu.bme.backend.entities.Category;
import hu.bme.backend.entities.ShoppingItem;
import hu.bme.backend.entities.Store;
import hu.bme.backend.entities.StoreCategory;
import hu.bme.backend.exceptions.DuplicateResourcesException;
import hu.bme.backend.exceptions.GlobalExceptionHandler;
import hu.bme.backend.exceptions.ResourceNotFoundException;
import hu.bme.backend.services.*;
import hu.bme.backend.services.records.ShoppingItemRequest;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RestApiTest {
    @Mock
    private StoreService storeService;
    @Mock
    private CategoryService categoryService;
    @Mock
    private StoreCategoryService storeCategoryService;
    @Mock
    private ShoppingItemService shoppingItemService;
    @Mock
    private AppSettingsService appSettingsService;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(
                new StoreController(storeService), new CategoryController(categoryService),
                new StoreCategoryController(storeCategoryService),
                new ShoppingItemController(shoppingItemService),
                new AppSettingsController(appSettingsService))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void getStoresReturnsNamesAndIds() throws Exception {
        when(storeService.getAllStores()).thenReturn(List.of(store()));
        mvc.perform(get("/api/stores")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Market"));
        verify(storeService).getAllStores();
    }

    @Test
    void getStoreUsesTheRequestedId() throws Exception {
        when(storeService.getStoreById(1L)).thenReturn(store());
        mvc.perform(get("/api/stores/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
        verify(storeService).getStoreById(1L);
    }

    @Test
    void createStoreReturnsCreated() throws Exception {
        when(storeService.createStore("Market")).thenReturn(store());
        mvc.perform(post("/api/stores").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Market\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.name").value("Market"));
        verify(storeService).createStore("Market");
    }

    @Test
    void getCategoriesReturnsNamesAndIds() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of(category()));
        mvc.perform(get("/api/categories")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Bakery"));
        verify(categoryService).getAllCategories();
    }

    @Test
    void getCategoryUsesTheRequestedId() throws Exception {
        when(categoryService.getCategoryById(2L)).thenReturn(category());
        mvc.perform(get("/api/categories/2")).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
        verify(categoryService).getCategoryById(2L);
    }

    @Test
    void createCategoryReturnsCreated() throws Exception {
        when(categoryService.createCategory("Bakery")).thenReturn(category());
        mvc.perform(post("/api/categories").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Bakery\"}"))
                .andExpect(status().isCreated());
        verify(categoryService).createCategory("Bakery");
    }

    @Test
    void getStoreCategoriesReturnsFlatDtosInServiceOrder() throws Exception {
        var first = placement();
        var second = placement();
        second.setId(11L);
        second.setPosition(1);
        when(storeCategoryService.getStoreCategories(1L)).thenReturn(List.of(first, second));
        mvc.perform(get("/api/stores/1/categories")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].position").value(0))
                .andExpect(jsonPath("$[1].position").value(1))
                .andExpect(jsonPath("$[0].categoryId").value(2))
                .andExpect(jsonPath("$[0].category").doesNotExist());
        verify(storeCategoryService).getStoreCategories(1L);
    }

    @Test
    void addStoreCategoryReturnsCreated() throws Exception {
        when(storeCategoryService.addCategoryToStore(1L, 2L)).thenReturn(placement());
        mvc.perform(post("/api/stores/1/categories").contentType(MediaType.APPLICATION_JSON)
                .content("{\"categoryId\":2}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.position").value(0));
        verify(storeCategoryService).addCategoryToStore(1L, 2L);
    }

    @Test
    void removeStoreCategoryReturnsNoContent() throws Exception {
        mvc.perform(delete("/api/stores/1/categories/2")).andExpect(status().isNoContent());
        verify(storeCategoryService).removeCategoryFromStore(1L, 2L);
    }

    @Test
    void moveCategoryUpReturnsNoContent() throws Exception {
        mvc.perform(patch("/api/stores/1/categories/2/move-up")).andExpect(status().isNoContent());
        verify(storeCategoryService).moveCategoryUp(1L, 2L);
    }

    @Test
    void moveCategoryDownReturnsNoContent() throws Exception {
        mvc.perform(patch("/api/stores/1/categories/2/move-down")).andExpect(status().isNoContent());
        verify(storeCategoryService).moveCategoryDown(1L, 2L);
    }

    @Test
    void getItemsReturnsFlatDtos() throws Exception {
        when(shoppingItemService.getAllItems()).thenReturn(List.of(item()));
        mvc.perform(get("/api/items")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryId").value(2))
                .andExpect(jsonPath("$[0].category").doesNotExist());
        verify(shoppingItemService).getAllItems();
    }

    @Test
    void getItemUsesTheRequestedId() throws Exception {
        when(shoppingItemService.getItemById(3L)).thenReturn(item());
        mvc.perform(get("/api/items/3")).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bread"));
        verify(shoppingItemService).getItemById(3L);
    }

    @Test
    void createItemReusesTheExistingRequestRecord() throws Exception {
        var request = new ShoppingItemRequest("Bread", 2L, null, null, null);
        when(shoppingItemService.createItem(request)).thenReturn(item());
        mvc.perform(post("/api/items").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Bread\",\"categoryId\":2}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(3));
        verify(shoppingItemService).createItem(request);
    }

    @Test
    void updateItemUsesPutAndPreservesTheServicePurchaseState() throws Exception {
        var item = item();
        item.setPurchased(true);
        var request = new ShoppingItemRequest("Bread", 2L, null, null, null);
        when(shoppingItemService.updateItem(3L, request)).thenReturn(item);
        mvc.perform(put("/api/items/3").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Bread\",\"categoryId\":2}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.purchased").value(true));
        verify(shoppingItemService).updateItem(3L, request);
    }

    @Test
    void deleteItemReturnsNoContent() throws Exception {
        mvc.perform(delete("/api/items/3")).andExpect(status().isNoContent());
        verify(shoppingItemService).deleteItem(3L);
    }

    @Test
    void patchPurchasedPassesTheBooleanToTheService() throws Exception {
        mvc.perform(patch("/api/items/3/purchased").contentType(MediaType.APPLICATION_JSON)
                .content("{\"purchased\":true}"))
                .andExpect(status().isNoContent());
        verify(shoppingItemService).setPurchased(3L, true);
    }

    @Test
    void resetPurchasedUsesTheResetOperation() throws Exception {
        mvc.perform(post("/api/items/reset-purchased")).andExpect(status().isNoContent());
        verify(shoppingItemService).resetShopping();
    }

    @Test
    void deletePurchasedUsesThePurchasedOnlyOperation() throws Exception {
        mvc.perform(delete("/api/items/purchased")).andExpect(status().isNoContent());
        verify(shoppingItemService).deletePurchasedItems();
    }

    @Test
    void shoppingListDefaultsToIncludingPurchasedItemsAndReturnsTheMissingFlag() throws Exception {
        var response = ShoppingListItemResponse.from(item(), true);
        when(shoppingItemService.getShoppingListResponse(1L, false)).thenReturn(List.of(response));
        mvc.perform(get("/api/stores/1/shopping-list")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryMissing").value(true));
        verify(shoppingItemService).getShoppingListResponse(1L, false);
    }

    @Test
    void remainingOnlyIsPassedToTheService() throws Exception {
        var response = ShoppingListItemResponse.from(item(), false);
        when(shoppingItemService.getShoppingListResponse(1L, true)).thenReturn(List.of(response));
        mvc.perform(get("/api/stores/1/shopping-list").param("remainingOnly", "true"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].categoryMissing").value(false));
        verify(shoppingItemService).getShoppingListResponse(1L, true);
    }

    @Test
    void getSelectedStoreReturnsTheSavedId() throws Exception {
        when(appSettingsService.getSelectedStoreId()).thenReturn(1L);
        mvc.perform(get("/api/settings/selected-store")).andExpect(status().isOk())
                .andExpect(jsonPath("$.storeId").value(1));
        verify(appSettingsService).getSelectedStoreId();
    }

    @Test
    void unsetSelectedStoreReturnsAnObjectWithNullId() throws Exception {
        when(appSettingsService.getSelectedStoreId()).thenReturn(null);
        mvc.perform(get("/api/settings/selected-store")).andExpect(status().isOk())
                .andExpect(content().json("{\"storeId\":null}"));
    }

    @Test
    void selectStoreUsesPutAndReturnsNoContent() throws Exception {
        mvc.perform(put("/api/settings/selected-store").contentType(MediaType.APPLICATION_JSON)
                .content("{\"storeId\":1}"))
                .andExpect(status().isNoContent());
        verify(appSettingsService).selectStore(1L);
        verifyNoInteractions(shoppingItemService);
    }

    @Test
    void missingStoreReturns404WithConsistentJson() throws Exception {
        when(storeService.getStoreById(99L)).thenThrow(new ResourceNotFoundException("Store not found"));
        mvc.perform(get("/api/stores/99")).andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"Store not found\"}"));
    }

    @Test
    void missingEntityReturns404() throws Exception {
        when(shoppingItemService.getItemById(99L)).thenThrow(new EntityNotFoundException("Item not found"));
        mvc.perform(get("/api/items/99")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Item not found"));
    }

    @Test
    void duplicateResourceReturns409() throws Exception {
        when(storeService.createStore("Market"))
                .thenThrow(new DuplicateResourcesException("Store already exists"));
        mvc.perform(post("/api/stores").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Market\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.error").value("Store already exists"));
    }

    @Test
    void businessValidationReturns400() throws Exception {
        when(shoppingItemService.createItem(any(ShoppingItemRequest.class)))
                .thenThrow(new IllegalArgumentException("Quantity must be positive"));
        mvc.perform(post("/api/items").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Bread\",\"categoryId\":2,\"quantity\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Quantity must be positive"));
    }

    @Test
    void blankNameReturns400BeforeCallingTheService() throws Exception {
        mvc.perform(post("/api/stores").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"   \"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("Invalid request"));
        verifyNoInteractions(storeService);
    }

    @Test
    void missingRequiredFieldsReturn400() throws Exception {
        mvc.perform(post("/api/categories").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/stores/1/categories").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/settings/selected-store").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/items/3/purchased").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(categoryService, storeCategoryService, appSettingsService, shoppingItemService);
    }

    @Test
    void missingAndMalformedBodiesReturnConsistent400Responses() throws Exception {
        mvc.perform(post("/api/items").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("Invalid request"));
        mvc.perform(post("/api/items").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("Invalid request"));
        mvc.perform(post("/api/items").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Bread\",\"categoryId\":2,\"quantity\":\"abc\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("Invalid request"));
        verifyNoInteractions(shoppingItemService);
    }

    @Test
    void invalidPathAndQueryTypesReturn400() throws Exception {
        mvc.perform(get("/api/stores/abc")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid request"));
        mvc.perform(get("/api/stores/1/shopping-list").param("remainingOnly", "abc"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("Invalid request"));
        verifyNoInteractions(storeService, shoppingItemService);
    }

    @Test
    void unexpectedExceptionsDoNotExposeInternalDetails() throws Exception {
        when(storeService.getAllStores()).thenThrow(new IllegalStateException("Internal database details"));
        mvc.perform(get("/api/stores")).andExpect(status().isInternalServerError())
                .andExpect(content().json("{\"error\":\"Internal server error\"}"));
    }

    @Test
    void failedDeleteReturns404() throws Exception {
        doThrow(new EntityNotFoundException("Item not found")).when(shoppingItemService).deleteItem(99L);
        mvc.perform(delete("/api/items/99")).andExpect(status().isNotFound());
    }

    private static Store store() {
        var store = new Store();
        store.setId(1L);
        store.setName("Market");
        return store;
    }

    private static Category category() {
        var category = new Category();
        category.setId(2L);
        category.setName("Bakery");
        return category;
    }

    private static ShoppingItem item() {
        var item = new ShoppingItem();
        item.setId(3L);
        item.setProductName("Bread");
        item.setCategory(category());
        return item;
    }

    private static StoreCategory placement() {
        var placement = new StoreCategory();
        placement.setId(10L);
        placement.setStore(store());
        placement.setCategory(category());
        placement.setPosition(0);
        return placement;
    }
}
