package com.ecommerce.sufi.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(Long id, List<Item> items, BigDecimal total) {
    public record Item(Long id, Long productId, String productName, String imageUrl, BigDecimal unitPrice,
            Integer quantity, BigDecimal subtotal) {
    }
}
