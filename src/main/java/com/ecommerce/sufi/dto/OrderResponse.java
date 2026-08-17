package com.ecommerce.sufi.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.ecommerce.sufi.model.OrderStatus;
import com.ecommerce.sufi.model.OrderItemStatus;
public record OrderResponse(Long id, OrderStatus status, BigDecimal total, String shippingFullName, String shippingPhone,
        String shippingAddress, List<Item> items, PaymentResponse payment, LocalDateTime createdAt) {
 public record Item(Long productId, String productName, String imageUrl, BigDecimal unitPrice, Integer quantity,
         BigDecimal subtotal, OrderItemStatus status) { }
}
