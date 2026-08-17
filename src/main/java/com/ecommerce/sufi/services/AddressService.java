package com.ecommerce.sufi.services;

import java.util.List;

import com.ecommerce.sufi.dto.AddressRequest;
import com.ecommerce.sufi.dto.AddressResponse;

public interface AddressService {

    // Create new address for logged-in user
    AddressResponse create(
            String email,
            AddressRequest request
    );

    // Get all addresses of logged-in user
    List<AddressResponse> findAll(
            String email
    );

    // Get one address of logged-in user
    AddressResponse findById(
            String email,
            Long id
    );

    // Update user's address
    AddressResponse update(
            String email,
            Long id,
            AddressRequest request
    );

    // Delete user's address
    void delete(
            String email,
            Long id
    );
}