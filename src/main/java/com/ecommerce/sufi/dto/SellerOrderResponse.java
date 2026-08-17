package com.ecommerce.sufi.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.ecommerce.sufi.model.OrderStatus;
import com.ecommerce.sufi.model.OrderItemStatus;

public record SellerOrderResponse(
        Long id,
        OrderStatus status,
        String customerName,
        String shippingPhone,
        String shippingAddress,
        List<Item> items,
        BigDecimal sellerTotal,
        LocalDateTime createdAt) {
    public record Item(Long id, Long productId, String productName, BigDecimal unitPrice, Integer quantity,
            BigDecimal subtotal, OrderItemStatus status) {
    }
}
