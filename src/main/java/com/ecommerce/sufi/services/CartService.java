package com.ecommerce.sufi.services;

import com.ecommerce.sufi.dto.CartItemRequest;
import com.ecommerce.sufi.dto.CartResponse;

public interface CartService {
    CartResponse getCart(String email);
    CartResponse addItem(String email, CartItemRequest request);
    CartResponse updateItem(String email, Long itemId, CartItemRequest request);
    void removeItem(String email, Long itemId);
    void clearCart(String email);
}
