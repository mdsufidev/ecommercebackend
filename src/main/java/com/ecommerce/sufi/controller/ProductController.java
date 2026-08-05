package com.ecommerce.sufi.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.sufi.dto.ProductRequest;
import com.ecommerce.sufi.model.Product;
import com.ecommerce.sufi.services.ProductService;

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
    public ResponseEntity<Page<Product>> getAllProducts(

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                productService.getAllProducts(page, size)
        );
    }

    // ==========================================
    // GET PRODUCT BY ID
    // PUBLIC
    // ==========================================

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                productService.getProductById(id)
        );
    }

    // ==========================================
    // CREATE PRODUCT
    // SELLER / ADMIN
    // ==========================================

    @PostMapping
    public ResponseEntity<Product> createProduct(
            @RequestBody ProductRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        Product product =
                productService.createProduct(
                        request,
                        email
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(product);
    }

    // ==========================================
    // UPDATE PRODUCT
    // OWNER / ADMIN
    // ==========================================

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        Product product =
                productService.updateProduct(
                        id,
                        request,
                        email
                );

        return ResponseEntity.ok(product);
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
    public ResponseEntity<Product> approveProduct(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                productService.approveProduct(id)
        );
    }

    // ==========================================
    // REJECT PRODUCT
    // ADMIN ONLY
    // ==========================================

    @PatchMapping("/{id}/reject")
    public ResponseEntity<Product> rejectProduct(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                productService.rejectProduct(id)
        );
    }
}