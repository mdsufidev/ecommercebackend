package com.ecommerce.sufi.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record StockUpdateRequest(@NotNull @PositiveOrZero Integer stock) {
}
