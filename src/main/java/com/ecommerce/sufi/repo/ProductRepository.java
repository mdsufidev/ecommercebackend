package com.ecommerce.sufi.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.sufi.model.Product;
import com.ecommerce.sufi.model.ProductStatus;

public interface ProductRepository
        extends JpaRepository<Product, Long> {

    List<Product> findByStatus(ProductStatus status);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findBySellerId(Long sellerId);

    boolean existsBySku(String sku);
}