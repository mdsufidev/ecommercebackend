package com.ecommerce.sufi.repo;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ecommerce.sufi.model.Order;
public interface OrderRepository extends JpaRepository<Order, Long> {
 List<Order> findAllByUserIdOrderByCreatedAtDesc(Long userId);
 Optional<Order> findByIdAndUserId(Long id, Long userId);
 long countByStatus(com.ecommerce.sufi.model.OrderStatus status);
 List<Order> findDistinctByItemsProductSellerIdOrderByCreatedAtDesc(Long sellerId);
 Optional<Order> findDistinctByIdAndItemsProductSellerId(Long id, Long sellerId);
 @Query("select coalesce(sum(o.total), 0) from Order o where o.status <> com.ecommerce.sufi.model.OrderStatus.CANCELLED")
 java.math.BigDecimal calculateCompletedRevenue();
}
