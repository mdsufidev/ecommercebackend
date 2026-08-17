package com.ecommerce.sufi.dto;

import java.math.BigDecimal;

public record SellerEarningsResponse(BigDecimal deliveredRevenue, long deliveredItems) {
}
