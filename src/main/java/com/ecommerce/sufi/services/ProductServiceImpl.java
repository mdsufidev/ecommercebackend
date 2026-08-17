package com.ecommerce.sufi.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;
import jakarta.persistence.criteria.JoinType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.sufi.dto.ProductRequest;
import com.ecommerce.sufi.exception.BadRequestException;
import com.ecommerce.sufi.exception.ResourceNotFoundException;
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

    // =====================================================
    // PUBLIC - GET APPROVED PRODUCTS
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(int page, int size) {

        validatePagination(page, size);

        Pageable pageable = PageRequest.of(page, size);

        return productRepository.findByStatus(
                ProductStatus.APPROVED,
                pageable
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> searchProducts(String query, int page, int size) {
        validatePagination(page, size);
        String normalizedQuery = query == null ? "" : query.trim();
        if (normalizedQuery.isEmpty()) {
            return productRepository.findByStatus(ProductStatus.APPROVED, PageRequest.of(page, size));
        }
        if (normalizedQuery.length() > 100) {
            throw new BadRequestException("Search text must not exceed 100 characters");
        }
        return productRepository.searchApprovedProducts(
                ProductStatus.APPROVED, normalizedQuery, PageRequest.of(page, size));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> browseProducts(String query, Long categoryId, BigDecimal minPrice,
            BigDecimal maxPrice, boolean inStock, String sort, int page, int size) {
        validatePagination(page, size);
        if (size > 100) throw new BadRequestException("Page size must not exceed 100");
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase();
        if (normalizedQuery.length() > 100) throw new BadRequestException("Search text must not exceed 100 characters");
        if (minPrice != null && minPrice.signum() < 0) throw new BadRequestException("Minimum price cannot be negative");
        if (maxPrice != null && maxPrice.signum() < 0) throw new BadRequestException("Maximum price cannot be negative");
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0)
            throw new BadRequestException("Minimum price cannot be greater than maximum price");

        Specification<Product> specification = (root, criteriaQuery, builder) ->
                builder.equal(root.get("status"), ProductStatus.APPROVED);
        if (!normalizedQuery.isEmpty()) specification = specification.and((root, criteriaQuery, builder) -> {
            String term = "%" + normalizedQuery + "%";
            var category = root.join("category", JoinType.INNER);
            var seller = root.join("seller", JoinType.LEFT);
            return builder.or(
                    builder.like(builder.lower(root.get("name")), term),
                    builder.like(builder.lower(builder.coalesce(root.get("description"), "")), term),
                    builder.like(builder.lower(category.get("name")), term),
                    builder.like(builder.lower(builder.coalesce(seller.get("name"), "")), term));
        });
        if (categoryId != null) specification = specification.and((root, criteriaQuery, builder) ->
                builder.equal(root.get("category").get("id"), categoryId));
        if (minPrice != null) specification = specification.and((root, criteriaQuery, builder) ->
                builder.greaterThanOrEqualTo(root.get("price"), minPrice));
        if (maxPrice != null) specification = specification.and((root, criteriaQuery, builder) ->
                builder.lessThanOrEqualTo(root.get("price"), maxPrice));
        if (inStock) specification = specification.and((root, criteriaQuery, builder) ->
                builder.greaterThan(root.get("stock"), 0));

        Sort sorting = switch (sort == null ? "newest" : sort) {
            case "price-asc" -> Sort.by(Sort.Direction.ASC, "price").and(Sort.by("id"));
            case "price-desc" -> Sort.by(Sort.Direction.DESC, "price").and(Sort.by("id"));
            case "name" -> Sort.by(Sort.Direction.ASC, "name").and(Sort.by("id"));
            default -> Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"));
        };
        return productRepository.findAll(specification, PageRequest.of(page, size, sorting));
    }

    // =====================================================
    // PUBLIC - GET APPROVED PRODUCT BY ID
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {

        return productRepository
                .findByIdAndStatus(id, ProductStatus.APPROVED)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));
    }

    // =====================================================
    // SELLER - CREATE PRODUCT
    // =====================================================

    @Override
    public Product createProduct(
            ProductRequest request,
            String email) {

        User seller = userRepository
                .findByEmail(email.trim().toLowerCase())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Category category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found"));

        // Check SKU
        if (productRepository.existsBySku(request.getSku())) {
            throw new BadRequestException("SKU already exists");
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

        // Seller products need admin approval
        product.setStatus(ProductStatus.PENDING);
        product.setRejectionReason(null);

        return productRepository.save(product);
    }

    // =====================================================
    // SELLER / ADMIN - UPDATE PRODUCT
    // =====================================================

    @Override
    public Product updateProduct(
            Long id,
            ProductRequest request,
            String email) {

        Product product = productRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        User user = userRepository
                .findByEmail(email.trim().toLowerCase())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        // Check admin
        boolean isAdmin = user.getRoles()
                .stream()
                .anyMatch(role ->
                        role.getName().name().equals("ROLE_ADMIN"));

        // Check seller ownership
        boolean isOwner = product.getSeller() != null
                && product.getSeller()
                        .getId()
                        .equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException(
                    "You are not authorized to update this product"
            );
        }

        Category category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found"));

        // Check duplicate SKU
        if (productRepository.existsBySkuAndIdNot(
                request.getSku(),
                id)) {

            throw new BadRequestException("SKU already exists");
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setSku(request.getSku());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(category);

        // Any seller edit goes back through review.
        if (!isAdmin) {
            product.setStatus(ProductStatus.PENDING);
            product.setRejectionReason(null);
        }

        return productRepository.save(product);
    }

    // =====================================================
    // SELLER / ADMIN - DELETE PRODUCT
    // =====================================================

    @Override
    public void deleteProduct(
            Long id,
            String email) {

        Product product = productRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        User user = userRepository
                .findByEmail(email.trim().toLowerCase())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        boolean isAdmin = user.getRoles()
                .stream()
                .anyMatch(role ->
                        role.getName().name().equals("ROLE_ADMIN"));

        boolean isOwner = product.getSeller() != null
                && product.getSeller()
                        .getId()
                        .equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException(
                    "You are not authorized to delete this product"
            );
        }

        productRepository.delete(product);
    }

    @Override
    public Product updateSellerStock(Long id, Integer stock, String email) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        User seller = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (product.getSeller() == null || !product.getSeller().getId().equals(seller.getId())) {
            throw new AccessDeniedException("You are not authorized to update this product stock");
        }
        product.setStock(stock);
        return productRepository.save(product);
    }

    // =====================================================
    // ADMIN - CREATE PRODUCT
    // =====================================================

    @Override
    public Product createProductByAdmin(
            ProductRequest request) {

        Category category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found"));

        // Check duplicate SKU
        if (productRepository.existsBySku(request.getSku())) {
            throw new BadRequestException("SKU already exists");
        }

        Product product = new Product();

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setSku(request.getSku());
        product.setImageUrl(request.getImageUrl());

        product.setCategory(category);

        /*
         * Admin-created products are automatically approved.
         */
        product.setStatus(ProductStatus.APPROVED);
        product.setRejectionReason(null);

        /*
         * Admin product may not have a seller.
         *
         * This requires Product.seller to allow NULL.
         */
        product.setSeller(null);

        return productRepository.save(product);
    }

    // =====================================================
    // ADMIN - UPDATE PRODUCT
    // =====================================================

    @Override
    public Product updateProductByAdmin(
            Long id,
            ProductRequest request) {

        Product product = productRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        Category category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found"));

        // Check duplicate SKU
        if (productRepository.existsBySkuAndIdNot(
                request.getSku(),
                id)) {

            throw new BadRequestException("SKU already exists");
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setSku(request.getSku());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(category);

        return productRepository.save(product);
    }

    // =====================================================
    // ADMIN - DELETE PRODUCT
    // =====================================================

    @Override
    public void deleteProductByAdmin(Long id) {

        Product product = productRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        productRepository.delete(product);
    }

    // =====================================================
    // ADMIN - APPROVE PRODUCT
    // =====================================================

    @Override
    public Product approveProduct(Long id) {

        Product product = productRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        product.setStatus(ProductStatus.APPROVED);

        return productRepository.save(product);
    }

    // =====================================================
    // ADMIN - REJECT PRODUCT
    // =====================================================

    @Override
    public Product rejectProduct(Long id) {

        return rejectProduct(id, "Product did not meet marketplace guidelines");
    }

    public Product rejectProduct(Long id, String reason) {

        Product product = productRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        product.setStatus(ProductStatus.REJECTED);
        product.setRejectionReason(reason == null || reason.isBlank()
                ? "Product did not meet marketplace guidelines" : reason.trim());

        return productRepository.save(product);
    }

    // =====================================================
    // ADMIN - GET PRODUCTS BY STATUS
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getProductsByStatus(
            ProductStatus status,
            int page,
            int size) {

        validatePagination(page, size);

        return productRepository.findByStatus(
                status,
                PageRequest.of(page, size)
        );
    }

    // =====================================================
    // SELLER - GET OWN PRODUCTS
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getSellerProducts(
            String email,
            int page,
            int size) {

        validatePagination(page, size);

        User seller = userRepository
                .findByEmail(email.trim().toLowerCase())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        return productRepository.findBySellerId(
                seller.getId(),
                PageRequest.of(page, size)
        );
    }

    // =====================================================
    // ADMIN - GET ALL PRODUCTS
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getAllProductsForAdmin(
            int page,
            int size) {

        validatePagination(page, size);

        return productRepository.findAll(
                PageRequest.of(page, size)
        );
    }

    // =====================================================
    // ADMIN - GET PRODUCT BY ID
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public Product getProductForAdmin(Long id) {

        return productRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));
    }

    // =====================================================
    // PAGINATION VALIDATION
    // =====================================================

    private void validatePagination(
            int page,
            int size) {

        if (page < 0) {
            throw new BadRequestException(
                    "Page number cannot be negative"
            );
        }

        if (size <= 0) {
            throw new BadRequestException(
                    "Page size must be greater than zero"
            );
        }
    }
}
