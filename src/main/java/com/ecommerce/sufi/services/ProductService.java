package com.ecommerce.sufi.services;

import java.util.List;

import org.springframework.data.domain.Page;

import com.ecommerce.sufi.dto.ProductRequest;
import com.ecommerce.sufi.model.Product;

public interface ProductService {

    // Get all products with pagination
    Page<Product> getAllProducts(int page, int size);

    // Get product by ID
    Product getProductById(Long id);

    // Create product
    Product createProduct(ProductRequest request, String email);

    // Update product
    Product updateProduct(
            Long id,
            ProductRequest request,
            String email
    );

    // Delete product
    void deleteProduct(Long id, String email);

    // Admin approval
    Product approveProduct(Long id);

    // Admin rejection
    Product rejectProduct(Long id);
}