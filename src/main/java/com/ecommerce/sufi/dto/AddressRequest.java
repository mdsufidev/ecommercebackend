package com.ecommerce.sufi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank(message = "Full name is required") @Size(max = 100, message = "Full name must not exceed 100 characters") String fullName,
        @NotBlank(message = "Phone is required") @Pattern(regexp = "^[0-9+() -]{7,20}$", message = "Phone number must be valid") String phone,
        @NotBlank(message = "Address line 1 is required") @Size(max = 255, message = "Address line 1 must not exceed 255 characters") String addressLine1,
        @Size(max = 255, message = "Address line 2 must not exceed 255 characters") String addressLine2,
        @NotBlank(message = "City is required") @Size(max = 100, message = "City must not exceed 100 characters") String city,
        @NotBlank(message = "State is required") @Size(max = 100, message = "State must not exceed 100 characters") String state,
        @NotBlank(message = "Pincode is required") @Size(max = 20, message = "Pincode must not exceed 20 characters") String pincode,
        @NotBlank(message = "Country is required") @Size(max = 100, message = "Country must not exceed 100 characters") String country) {
}
