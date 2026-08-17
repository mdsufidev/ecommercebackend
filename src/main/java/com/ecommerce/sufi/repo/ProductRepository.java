package com.ecommerce.sufi.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.sufi.model.Product;
import com.ecommerce.sufi.model.ProductStatus;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
    @Query("""
            select product from Product product
            join product.category category
            left join product.seller seller
            where product.status = :status
              and (lower(product.name) like lower(concat('%', :query, '%'))
                or lower(coalesce(product.description, '')) like lower(concat('%', :query, '%'))
                or lower(category.name) like lower(concat('%', :query, '%'))
                or lower(coalesce(seller.name, '')) like lower(concat('%', :query, '%')))
            """)
    Page<Product> searchApprovedProducts(@Param("status") ProductStatus status,
            @Param("query") String query, Pageable pageable);
    Optional<Product> findByIdAndStatus(Long id, ProductStatus status);
    boolean existsBySku(String sku);
    boolean existsBySkuAndIdNot(String sku, Long id);
    boolean existsByCategoryId(Long categoryId);
    Page<Product> findBySellerId(Long sellerId, Pageable pageable);
    long countByStatus(ProductStatus status);
    long countBySellerId(Long sellerId);
    long countBySellerIdAndStatus(Long sellerId, ProductStatus status);
    long countBySellerIdAndStockBetween(Long sellerId, Integer minimum, Integer maximum);
    List<Product> findTop5BySellerIdAndStockBetweenOrderByStockAsc(Long sellerId, Integer minimum, Integer maximum);
    long countBySellerIdAndStockGreaterThan(Long sellerId, Integer stock);
    long countBySellerIdAndStock(Long sellerId, Integer stock);
}
