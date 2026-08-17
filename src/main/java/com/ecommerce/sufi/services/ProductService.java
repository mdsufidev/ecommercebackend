package com.ecommerce.sufi.services;

import org.springframework.data.domain.Page;
import java.math.BigDecimal;

import com.ecommerce.sufi.dto.ProductRequest;
import com.ecommerce.sufi.model.Product;
import com.ecommerce.sufi.model.ProductStatus;

public interface ProductService {

    // Public
    Page<Product> getAllProducts(int page, int size);

    Page<Product> searchProducts(String query, int page, int size);

    Page<Product> browseProducts(String query, Long categoryId, BigDecimal minPrice,
            BigDecimal maxPrice, boolean inStock, String sort, int page, int size);

    Product getProductById(Long id);

    // Seller
    Product createProduct(ProductRequest request, String email);

    Product updateProduct(
            Long id,
            ProductRequest request,
            String email
    );

    void deleteProduct(Long id, String email);

    // Admin
    Product createProductByAdmin(ProductRequest request);

    Product updateProductByAdmin(
            Long id,
            ProductRequest request
    );

    void deleteProductByAdmin(Long id);

    Product approveProduct(Long id);

    Product rejectProduct(Long id);
    Product rejectProduct(Long id, String reason);
    Product updateSellerStock(Long id, Integer stock, String email);

    Page<Product> getProductsByStatus(
            ProductStatus status,
            int page,
            int size
    );

    Page<Product> getSellerProducts(
            String email,
            int page,
            int size
    );

    Page<Product> getAllProductsForAdmin(
            int page,
            int size
    );

    Product getProductForAdmin(Long id);
}
