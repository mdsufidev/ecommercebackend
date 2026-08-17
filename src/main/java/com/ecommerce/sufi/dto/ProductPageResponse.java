package com.ecommerce.sufi.dto;

import java.util.List;

import org.springframework.data.domain.Page;

public record ProductPageResponse(
        List<ProductResponse> content,
        long totalElements,
        int totalPages,
        int number,
        int size,
        boolean first,
        boolean last) {

    public static ProductPageResponse from(Page<ProductResponse> page) {
        return new ProductPageResponse(page.getContent(), page.getTotalElements(), page.getTotalPages(),
                page.getNumber(), page.getSize(), page.isFirst(), page.isLast());
    }
}
