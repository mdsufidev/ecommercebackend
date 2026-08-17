package com.ecommerce.sufi.dto;

public record AddressResponse(Long id, String fullName, String phone, String addressLine1, String addressLine2,
        String city, String state, String pincode, String country) {
}
