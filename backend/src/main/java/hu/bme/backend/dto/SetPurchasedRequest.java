package hu.bme.backend.dto;

import jakarta.validation.constraints.NotNull;

public record SetPurchasedRequest(@NotNull Boolean purchased) {
}
