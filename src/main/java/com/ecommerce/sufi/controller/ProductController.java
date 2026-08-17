package com.ecommerce.sufi.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;

import com.ecommerce.sufi.dto.ProductRequest;
import com.ecommerce.sufi.dto.ProductResponse;
import com.ecommerce.sufi.dto.ProductPageResponse;
import com.ecommerce.sufi.dto.ProductResponseMapper;
import com.ecommerce.sufi.model.Product;
import com.ecommerce.sufi.services.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ==========================================
    // GET ALL PRODUCTS
    // PUBLIC
    // ==========================================

    @GetMapping
    public ResponseEntity<ProductPageResponse> getAllProducts(

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "10") int size,

            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "false") boolean inStock,
            @RequestParam(defaultValue = "newest") String sort) {

        return ResponseEntity.ok(ProductPageResponse.from(productService.browseProducts(q, categoryId, minPrice,
                maxPrice, inStock, sort, page, size).map(ProductResponseMapper::from)));
    }

    // ==========================================
    // GET PRODUCT BY ID
    // PUBLIC
    // ==========================================

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ProductResponseMapper.from(productService.getProductById(id))
        );
    }

    // ==========================================
    // CREATE PRODUCT
    // SELLER / ADMIN
    // ==========================================

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        Product product =
                productService.createProduct(
                        request,
                        email
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ProductResponseMapper.from(product));
    }

    // ==========================================
    // UPDATE PRODUCT
    // OWNER / ADMIN
    // ==========================================

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        Product product =
                productService.updateProduct(
                        id,
                        request,
                        email
                );

        return ResponseEntity.ok(ProductResponseMapper.from(product));
    }

    // ==========================================
    // DELETE PRODUCT
    // OWNER / ADMIN
    // ==========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();

        productService.deleteProduct(
                id,
                email
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    // ==========================================
    // APPROVE PRODUCT
    // ADMIN ONLY
    // ==========================================

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ProductResponse> approveProduct(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ProductResponseMapper.from(productService.approveProduct(id))
        );
    }

    // ==========================================
    // REJECT PRODUCT
    // ADMIN ONLY
    // ==========================================

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ProductResponse> rejectProduct(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ProductResponseMapper.from(productService.rejectProduct(id))
        );
    }

}
