package hu.bme.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateStoreRequest(@NotBlank String name) {
}
