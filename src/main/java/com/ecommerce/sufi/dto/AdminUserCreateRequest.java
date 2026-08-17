package com.ecommerce.sufi.dto;

import java.util.Set;

import com.ecommerce.sufi.model.RoleName;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminUserCreateRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters") String name,
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 150, message = "Email must not exceed 150 characters") String email,
        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^[0-9+() -]{7,20}$", message = "Phone number must be valid") String phone,
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters") String password,
        @NotEmpty(message = "At least one role is required") Set<RoleName> roles,
        Boolean enabled) {
}
