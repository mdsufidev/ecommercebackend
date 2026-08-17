package com.ecommerce.sufi.dto;

import java.time.LocalDateTime;
import java.util.Set;

import com.ecommerce.sufi.model.RoleName;

public record UserResponse(
        Long id,
        String name,
        String email,
        String phone,
        Set<RoleName> roles,
        boolean enabled,
        LocalDateTime createdAt) {
}
