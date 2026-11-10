package hu.bme.backend.dto;

import jakarta.validation.constraints.NotNull;

public record SelectStoreRequest(@NotNull Long storeId) {
}
