package com.ecommerce.sufi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(
        @NotBlank(message = "Name is required") @Size(max = 100, message = "Name must not exceed 100 characters") String name,
        @NotBlank(message = "Phone is required") @Pattern(regexp = "^[0-9+() -]{7,20}$", message = "Phone number must be valid") String phone) {
}
