package com.ecommerce.sufi.dto;

import java.math.BigDecimal;
import java.util.List;

public record SellerDashboardResponse(
        long totalProducts,
        long approvedProducts,
        long pendingProducts,
        long rejectedProducts,
        long totalOrders,
        BigDecimal totalRevenue,
        long availableStockProducts,
        long lowStockProducts,
        long outOfStockProducts,
        List<ProductResponse> lowStockItems,
        List<SellerOrderResponse> recentOrders) {
}
