package com.ecommerce.sufi.dto;
import java.math.BigDecimal;

import com.ecommerce.sufi.model.PaymentMethod;
import com.ecommerce.sufi.model.PaymentStatus;
public record PaymentResponse(Long id, PaymentMethod method, PaymentStatus status, BigDecimal amount) { }
