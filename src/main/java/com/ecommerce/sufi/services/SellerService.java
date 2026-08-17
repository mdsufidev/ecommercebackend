package com.ecommerce.sufi.services;

import com.ecommerce.sufi.dto.SellerDashboardResponse;
import com.ecommerce.sufi.dto.SellerEarningsResponse;

public interface SellerService {
    SellerDashboardResponse getDashboard(String email);
    SellerEarningsResponse getEarnings(String email);
}
