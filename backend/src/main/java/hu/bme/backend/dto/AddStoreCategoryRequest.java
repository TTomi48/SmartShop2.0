package hu.bme.backend.dto;

import jakarta.validation.constraints.NotNull;

public record AddStoreCategoryRequest(@NotNull Long categoryId) {
}
