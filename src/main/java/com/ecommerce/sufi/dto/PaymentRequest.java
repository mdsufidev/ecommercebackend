package com.ecommerce.sufi.dto;
import com.ecommerce.sufi.model.PaymentMethod;

import jakarta.validation.constraints.NotNull;
public record PaymentRequest(@NotNull(message = "Payment method is required") PaymentMethod paymentMethod) { }
