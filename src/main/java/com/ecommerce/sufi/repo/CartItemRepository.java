package com.ecommerce.sufi.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.sufi.model.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByIdAndCartUserId(Long id, Long userId);
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
}
