package com.ecommerce.sufi.services;
import java.util.List;

import com.ecommerce.sufi.dto.OrderRequest;
import com.ecommerce.sufi.dto.OrderResponse;
import com.ecommerce.sufi.dto.SellerOrderResponse;
import com.ecommerce.sufi.model.OrderStatus;
import com.ecommerce.sufi.model.OrderItemStatus;
public interface OrderService {
 OrderResponse checkout(String email, OrderRequest request);
 List<OrderResponse> getMyOrders(String email);
 OrderResponse getOrder(String email, Long id);
 void cancelOrder(String email, Long id);
 List<OrderResponse> getAllOrders();
    OrderResponse updateStatus(Long id, OrderStatus status);

    OrderResponse getOrderForAdmin(Long id);

    List<SellerOrderResponse> getSellerOrders(String email);

    SellerOrderResponse getSellerOrder(String email, Long id);
    SellerOrderResponse updateSellerItemStatus(String email, Long orderId, Long itemId, OrderItemStatus status);
    SellerOrderResponse updateSellerItemStatus(String email, Long itemId, OrderItemStatus status);
}
