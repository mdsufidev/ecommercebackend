package com.ecommerce.sufi.dto;

import java.math.BigDecimal;

public record AdminDashboardResponse(
        long totalUsers,
        long totalSellers,
        long totalProducts,
        long totalCategories,
        long pendingProducts,
        long totalOrders,
        long pendingOrders,
        BigDecimal totalRevenue) {
}
