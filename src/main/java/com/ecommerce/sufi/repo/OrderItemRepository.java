package com.ecommerce.sufi.repo;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.sufi.model.OrderItem;
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
 java.util.Optional<OrderItem> findByIdAndOrderIdAndProductSellerId(Long id, Long orderId, Long sellerId);
 java.util.Optional<OrderItem> findByIdAndProductSellerId(Long id, Long sellerId);
 @org.springframework.data.jpa.repository.Query("select count(distinct i.order.id) from OrderItem i where i.product.seller.id = :sellerId")
 long countDistinctOrdersByProductSellerId(Long sellerId);
 @org.springframework.data.jpa.repository.Query("select coalesce(sum(i.subtotal), 0) from OrderItem i where i.product.seller.id = :sellerId and i.status = com.ecommerce.sufi.model.OrderItemStatus.DELIVERED")
 java.math.BigDecimal calculateSellerRevenue(Long sellerId);
 @org.springframework.data.jpa.repository.Query("select count(i) from OrderItem i where i.product.seller.id = :sellerId and i.status = com.ecommerce.sufi.model.OrderItemStatus.DELIVERED")
 long countDeliveredItemsBySellerId(Long sellerId);
}
