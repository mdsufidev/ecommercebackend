package com.ecommerce.sufi.dto;
import com.ecommerce.sufi.model.PaymentMethod;

import jakarta.validation.constraints.NotNull;
public record OrderRequest(@NotNull(message = "Shipping address is required") Long addressId,
        @NotNull(message = "Payment method is required") PaymentMethod paymentMethod) { }
