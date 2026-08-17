package com.ecommerce.sufi.dto;

import com.ecommerce.sufi.model.OrderStatus;

import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(
        @NotNull(message = "Order status is required") OrderStatus status) {
}
