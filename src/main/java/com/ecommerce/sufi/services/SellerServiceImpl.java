package com.ecommerce.sufi.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.sufi.dto.SellerDashboardResponse;
import com.ecommerce.sufi.dto.ProductResponseMapper;
import com.ecommerce.sufi.dto.SellerEarningsResponse;
import com.ecommerce.sufi.model.ProductStatus;
import com.ecommerce.sufi.model.User;
import com.ecommerce.sufi.repo.OrderItemRepository;
import com.ecommerce.sufi.repo.ProductRepository;

@Service
@Transactional(readOnly = true)
public class SellerServiceImpl implements SellerService {
    private final UserService userService;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderService orderService;

    public SellerServiceImpl(UserService userService, ProductRepository productRepository,
            OrderItemRepository orderItemRepository, OrderService orderService) {
        this.userService = userService;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderService = orderService;
    }

    @Override
    public SellerDashboardResponse getDashboard(String email) {
        User seller = userService.getUserByEmail(email);
        return new SellerDashboardResponse(productRepository.countBySellerId(seller.getId()),
                productRepository.countBySellerIdAndStatus(seller.getId(), ProductStatus.APPROVED),
                productRepository.countBySellerIdAndStatus(seller.getId(), ProductStatus.PENDING),
                productRepository.countBySellerIdAndStatus(seller.getId(), ProductStatus.REJECTED),
                orderItemRepository.countDistinctOrdersByProductSellerId(seller.getId()),
                orderItemRepository.calculateSellerRevenue(seller.getId()),
                productRepository.countBySellerIdAndStockGreaterThan(seller.getId(), 0),
                productRepository.countBySellerIdAndStockBetween(seller.getId(), 1, 5),
                productRepository.countBySellerIdAndStock(seller.getId(), 0),
                productRepository.findTop5BySellerIdAndStockBetweenOrderByStockAsc(seller.getId(), 1, 5)
                        .stream().map(ProductResponseMapper::from).toList(),
                orderService.getSellerOrders(email).stream().limit(5).toList());
    }

    @Override
    public SellerEarningsResponse getEarnings(String email) {
        Long sellerId = userService.getUserByEmail(email).getId();
        return new SellerEarningsResponse(orderItemRepository.calculateSellerRevenue(sellerId),
                orderItemRepository.countDeliveredItemsBySellerId(sellerId));
    }
}
