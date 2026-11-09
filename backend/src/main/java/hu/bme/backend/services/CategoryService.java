package hu.bme.backend.services;

import hu.bme.backend.entities.Category;
import hu.bme.backend.exceptions.DuplicateResourcesException;
import hu.bme.backend.exceptions.ResourceNotFoundException;
import hu.bme.backend.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;


import java.util.List;

@RequiredArgsConstructor
@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    public Category getCategoryById(Long id) {
        if (id == null) throw new IllegalArgumentException("Category id is null");

        var category = categoryRepository.findById(id).orElse(null);
        if (category == null) {
            throw new ResourceNotFoundException("Category not found");
        }

        return category;
    }

    @Transactional
    public Category createCategory(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Category name is null");

        var normalizedName = name.trim();

        if (categoryRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new DuplicateResourcesException("Category with name " + normalizedName + " already exists");
        }

        var category = new Category();
        category.setName(normalizedName);

        return categoryRepository.save(category);
    }
}
