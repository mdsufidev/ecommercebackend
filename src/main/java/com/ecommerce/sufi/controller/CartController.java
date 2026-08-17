package com.ecommerce.sufi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.sufi.dto.CartItemRequest;
import com.ecommerce.sufi.dto.CartResponse;
import com.ecommerce.sufi.services.CartService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) { this.cartService = cartService; }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(Authentication authentication) {
        return ResponseEntity.ok(cartService.getCart(authentication.getName()));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(Authentication authentication, @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItem(authentication.getName(), request));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<CartResponse> updateItem(Authentication authentication, @PathVariable Long id,
            @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.updateItem(authentication.getName(), id, request));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> removeItem(Authentication authentication, @PathVariable Long id) {
        cartService.removeItem(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        cartService.clearCart(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
