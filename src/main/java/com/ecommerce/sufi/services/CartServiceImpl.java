package com.ecommerce.sufi.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.sufi.dto.CartItemRequest;
import com.ecommerce.sufi.dto.CartResponse;
import com.ecommerce.sufi.exception.BadRequestException;
import com.ecommerce.sufi.exception.ResourceNotFoundException;
import com.ecommerce.sufi.model.Cart;
import com.ecommerce.sufi.model.CartItem;
import com.ecommerce.sufi.model.Product;
import com.ecommerce.sufi.model.ProductStatus;
import com.ecommerce.sufi.model.User;
import com.ecommerce.sufi.repo.CartItemRepository;
import com.ecommerce.sufi.repo.CartRepository;
import com.ecommerce.sufi.repo.ProductRepository;

@Service
@Transactional
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    public CartServiceImpl(CartRepository cartRepository, CartItemRepository cartItemRepository,
            ProductRepository productRepository, UserService userService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userService = userService;
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(String email) {
        User user = userService.getUserByEmail(email);
        return cartRepository.findByUserId(user.getId()).map(this::toResponse)
                .orElseGet(() -> new CartResponse(null, List.of(), BigDecimal.ZERO));
    }

    @Override
    public CartResponse addItem(String email, CartItemRequest request) {
        User user = userService.getUserByEmail(email);
        Cart cart = getOrCreateCart(user);
        Product product = approvedProduct(request.productId());
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId()).orElse(null);
        int quantity = request.quantity() + (item == null ? 0 : item.getQuantity());
        validateStock(product, quantity);
        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(quantity);
            cart.getItems().add(item);
        } else {
            item.setQuantity(quantity);
        }
        return toResponse(cartRepository.save(cart));
    }

    @Override
    public CartResponse updateItem(String email, Long itemId, CartItemRequest request) {
        User user = userService.getUserByEmail(email);
        CartItem item = cartItemRepository.findByIdAndCartUserId(itemId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        if (!item.getProduct().getId().equals(request.productId())) {
            throw new BadRequestException("Cart item product cannot be changed");
        }
        Product product = approvedProduct(request.productId());
        validateStock(product, request.quantity());
        item.setQuantity(request.quantity());
        return toResponse(cartItemRepository.save(item).getCart());
    }

    @Override
    public void removeItem(String email, Long itemId) {
        User user = userService.getUserByEmail(email);
        CartItem item = cartItemRepository.findByIdAndCartUserId(itemId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        cartItemRepository.delete(item);
    }

    @Override
    public void clearCart(String email) {
        User user = userService.getUserByEmail(email);
        cartRepository.findByUserId(user.getId()).ifPresent(cart -> cart.getItems().clear());
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId()).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUser(user);
            return cartRepository.save(cart);
        });
    }

    private Product approvedProduct(Long productId) {
        return productRepository.findByIdAndStatus(productId, ProductStatus.APPROVED)
                .orElseThrow(() -> new ResourceNotFoundException("Approved product not found"));
    }

    private void validateStock(Product product, int quantity) {
        if (quantity > product.getStock()) {
            throw new BadRequestException("Requested quantity exceeds available stock");
        }
    }

    private CartResponse toResponse(Cart cart) {
        List<CartResponse.Item> items = cart.getItems().stream().map(item -> {
            BigDecimal subtotal = item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            return new CartResponse.Item(item.getId(), item.getProduct().getId(), item.getProduct().getName(),
                    item.getProduct().getImageUrl(), item.getProduct().getPrice(), item.getQuantity(), subtotal);
        }).toList();
        BigDecimal total = items.stream().map(CartResponse.Item::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(cart.getId(), items, total);
    }
}
