package com.ecommerce.sufi.dto;

import com.ecommerce.sufi.model.OrderItemStatus;
import jakarta.validation.constraints.NotNull;

public record OrderItemStatusUpdateRequest(@NotNull(message = "Item status is required") OrderItemStatus status) {
}
