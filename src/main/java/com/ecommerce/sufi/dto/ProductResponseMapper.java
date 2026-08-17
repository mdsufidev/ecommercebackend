package com.ecommerce.sufi.dto;

import com.ecommerce.sufi.model.Product;

public final class ProductResponseMapper {
    private ProductResponseMapper() {
    }

    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getDescription(), product.getPrice(),
                product.getStock(), product.getSku(), product.getImageUrl(),
                new CategoryResponse(product.getCategory().getId(), product.getCategory().getName()),
                product.getSeller() == null ? null : new SellerResponse(product.getSeller().getId(), product.getSeller().getName()),
                product.getStatus().name(), product.getRejectionReason(),
                product.getCreatedAt(), product.getUpdatedAt());
    }
}
