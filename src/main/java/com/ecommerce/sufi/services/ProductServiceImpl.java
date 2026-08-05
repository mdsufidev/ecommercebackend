package com.ecommerce.sufi.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ecommerce.sufi.dto.ProductRequest;
import com.ecommerce.sufi.model.Category;
import com.ecommerce.sufi.model.Product;
import com.ecommerce.sufi.model.ProductStatus;
import com.ecommerce.sufi.model.User;
import com.ecommerce.sufi.repo.CategoryRepository;
import com.ecommerce.sufi.repo.ProductRepository;
import com.ecommerce.sufi.repo.UserRepository;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public ProductServiceImpl(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository) {

        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    // ==========================================
    // GET ALL PRODUCTS WITH PAGINATION
    // ==========================================

    @Override
    public Page<Product> getAllProducts(int page, int size) {

        if (page < 0) {
            throw new RuntimeException("Page number cannot be negative");
        }

        if (size <= 0) {
            throw new RuntimeException("Page size must be greater than zero");
        }

        Pageable pageable = PageRequest.of(page, size);

        return productRepository.findAll(pageable);
    }

    // ==========================================
    // GET PRODUCT BY ID
    // ==========================================

    @Override
    public Product getProductById(Long id) {

        return productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));
    }

    // ==========================================
    // CREATE PRODUCT
    // ==========================================

    @Override
    public Product createProduct(
            ProductRequest request,
            String email) {

        User seller = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Category category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() ->
                        new RuntimeException("Category not found"));

        Product product = new Product();

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setSku(request.getSku());
        product.setImageUrl(request.getImageUrl());

        product.setCategory(category);
        product.setSeller(seller);

        // New products require admin approval
        product.setStatus(ProductStatus.PENDING);

        return productRepository.save(product);
    }

    // ==========================================
    // UPDATE PRODUCT
    // ==========================================

    @Override
    public Product updateProduct(
            Long id,
            ProductRequest request,
            String email) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // Admin can update any product
        boolean isAdmin = user.getRoles()
                .stream()
                .anyMatch(role ->
                        role.getName().name().equals("ROLE_ADMIN"));

        // Seller can update only own product
        boolean isOwner = product.getSeller()
                .getId()
                .equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new RuntimeException(
                    "You are not authorized to update this product");
        }

        Category category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() ->
                        new RuntimeException("Category not found"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setSku(request.getSku());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(category);

        return productRepository.save(product);
    }

    // ==========================================
    // DELETE PRODUCT
    // ==========================================

    @Override
    public void deleteProduct(
            Long id,
            String email) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        boolean isAdmin = user.getRoles()
                .stream()
                .anyMatch(role ->
                        role.getName().name().equals("ROLE_ADMIN"));

        boolean isOwner = product.getSeller()
                .getId()
                .equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new RuntimeException(
                    "You are not authorized to delete this product");
        }

        productRepository.delete(product);
    }

    // ==========================================
    // APPROVE PRODUCT
    // ==========================================

    @Override
    public Product approveProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        product.setStatus(ProductStatus.APPROVED);

        return productRepository.save(product);
    }

    // ==========================================
    // REJECT PRODUCT
    // ==========================================

    @Override
    public Product rejectProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        product.setStatus(ProductStatus.REJECTED);

        return productRepository.save(product);
    }
}