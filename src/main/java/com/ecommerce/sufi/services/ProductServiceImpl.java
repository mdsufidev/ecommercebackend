package com.ecommerce.sufi.services;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.sufi.dto.ProductRequest;
import com.ecommerce.sufi.model.Category;
import com.ecommerce.sufi.model.Product;
import com.ecommerce.sufi.model.ProductStatus;
import com.ecommerce.sufi.model.User;
import com.ecommerce.sufi.repo.CategoryRepository;
import com.ecommerce.sufi.repo.ProductRepository;
import com.ecommerce.sufi.repo.UserRepository;

@Service
@Transactional
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

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {

        return productRepository.findByStatus(
                ProductStatus.APPROVED
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        if (product.getStatus() != ProductStatus.APPROVED) {
            throw new RuntimeException(
                    "Product is not available");
        }

        return product;
    }

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

        if (request.getPrice() == null ||
                request.getPrice().signum() < 0) {

            throw new RuntimeException(
                    "Price must be greater than or equal to zero");
        }

        if (request.getStock() == null ||
                request.getStock() < 0) {

            throw new RuntimeException(
                    "Stock must be greater than or equal to zero");
        }

        if (request.getSku() != null &&
                productRepository.existsBySku(request.getSku())) {

            throw new RuntimeException(
                    "SKU already exists");
        }

        Product product = new Product();

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setSku(request.getSku());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(category);
        product.setSeller(seller);

        /*
         * Seller products require admin approval.
         */
        product.setStatus(ProductStatus.PENDING);

        return productRepository.save(product);
    }

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

        boolean admin = user.getRoles()
                .stream()
                .anyMatch(role ->
                        role.getName().name()
                                .equals("ROLE_ADMIN"));

        boolean owner =
                product.getSeller()
                        .getId()
                        .equals(user.getId());

        if (!admin && !owner) {

            throw new AccessDeniedException(
                    "You are not allowed to update this product");
        }

        Category category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() ->
                        new RuntimeException("Category not found"));

        if (request.getPrice() == null ||
                request.getPrice().signum() < 0) {

            throw new RuntimeException(
                    "Invalid price");
        }

        if (request.getStock() == null ||
                request.getStock() < 0) {

            throw new RuntimeException(
                    "Invalid stock");
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setSku(request.getSku());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(category);

        /*
         * If seller changes product,
         * send it for approval again.
         */
        if (!admin) {
            product.setStatus(ProductStatus.PENDING);
        }

        return productRepository.save(product);
    }

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

        boolean admin = user.getRoles()
                .stream()
                .anyMatch(role ->
                        role.getName().name()
                                .equals("ROLE_ADMIN"));

        boolean owner =
                product.getSeller()
                        .getId()
                        .equals(user.getId());

        if (!admin && !owner) {

            throw new AccessDeniedException(
                    "You are not allowed to delete this product");
        }

        productRepository.delete(product);
    }

    @Override
    public Product approveProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        product.setStatus(ProductStatus.APPROVED);

        return productRepository.save(product);
    }

    @Override
    public Product rejectProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        product.setStatus(ProductStatus.REJECTED);

        return productRepository.save(product);
    }
}