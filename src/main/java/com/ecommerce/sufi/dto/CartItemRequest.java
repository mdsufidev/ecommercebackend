package com.ecommerce.sufi.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CartItemRequest(
        @NotNull(message = "Product is required") Long productId,
        @NotNull(message = "Quantity is required") @Positive(message = "Quantity must be greater than zero") Integer quantity) {
}
