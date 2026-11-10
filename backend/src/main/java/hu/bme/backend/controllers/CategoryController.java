package hu.bme.backend.controllers;

import hu.bme.backend.dto.CreateCategoryRequest;
import hu.bme.backend.dto.NamedResponse;
import hu.bme.backend.services.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public List<NamedResponse> getAllCategories() {
        return categoryService.getAllCategories().stream().map(NamedResponse::from).toList();
    }

    @GetMapping("/{id}")
    public NamedResponse getById(@PathVariable Long id) {
        return NamedResponse.from(categoryService.getCategoryById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NamedResponse create(@Valid @RequestBody CreateCategoryRequest request) {
        return NamedResponse.from(categoryService.createCategory(request.name()));
    }
}
