package com.ecommerce.sufi.services;

import java.util.List;

import com.ecommerce.sufi.dto.ProductRequest;
import com.ecommerce.sufi.model.Product;

public interface ProductService {

    List<Product> getAllProducts();

    Product getProductById(Long id);

    Product createProduct(ProductRequest request, String email);

    Product updateProduct(
            Long id,
            ProductRequest request,
            String email);

    void deleteProduct(Long id, String email);

    Product approveProduct(Long id);

    Product rejectProduct(Long id);
}