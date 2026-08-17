package com.ecommerce.sufi.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.sufi.dto.AddressRequest;
import com.ecommerce.sufi.dto.AddressResponse;
import com.ecommerce.sufi.exception.ResourceNotFoundException;
import com.ecommerce.sufi.model.Address;
import com.ecommerce.sufi.model.User;
import com.ecommerce.sufi.repo.AddressRepository;

@Service
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserService userService;

    public AddressServiceImpl(
            AddressRepository addressRepository,
            UserService userService) {

        this.addressRepository = addressRepository;
        this.userService = userService;
    }

    // =====================================================
    // CREATE ADDRESS
    // =====================================================

    @Override
    public AddressResponse create(
            String email,
            AddressRequest request) {

        User user = userService.getUserByEmail(
                email.trim().toLowerCase()
        );

        Address address = new Address();

        address.setUser(user);

        apply(address, request);

        Address savedAddress = addressRepository.save(address);

        return toResponse(savedAddress);
    }

    // =====================================================
    // GET ALL ADDRESSES
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> findAll(String email) {

        User user = userService.getUserByEmail(
                email.trim().toLowerCase()
        );

        return addressRepository
                .findAllByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =====================================================
    // GET ADDRESS BY ID
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public AddressResponse findById(
            String email,
            Long id) {

        Address address = ownedAddress(email, id);

        return toResponse(address);
    }

    // =====================================================
    // UPDATE ADDRESS
    // =====================================================

    @Override
    public AddressResponse update(
            String email,
            Long id,
            AddressRequest request) {

        Address address = ownedAddress(email, id);

        apply(address, request);

        Address updatedAddress =
                addressRepository.save(address);

        return toResponse(updatedAddress);
    }

    // =====================================================
    // DELETE ADDRESS
    // =====================================================

    @Override
    public void delete(
            String email,
            Long id) {

        Address address = ownedAddress(email, id);

        addressRepository.delete(address);
    }

    // =====================================================
    // FIND USER'S ADDRESS
    // =====================================================

    private Address ownedAddress(
            String email,
            Long id) {

        User user = userService.getUserByEmail(
                email.trim().toLowerCase()
        );

        return addressRepository
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Address not found"
                        )
                );
    }

    // =====================================================
    // APPLY REQUEST DATA
    // =====================================================

    private void apply(
            Address address,
            AddressRequest request) {

        address.setFullName(
                request.fullName().trim()
        );

        address.setPhone(
                request.phone().trim()
        );

        address.setAddressLine1(
                request.addressLine1().trim()
        );

        address.setAddressLine2(
                blankToNull(request.addressLine2())
        );

        address.setCity(
                request.city().trim()
        );

        address.setState(
                request.state().trim()
        );

        address.setPincode(
                request.pincode().trim()
        );

        address.setCountry(
                request.country().trim()
        );
    }

    // =====================================================
    // EMPTY STRING -> NULL
    // =====================================================

    private String blankToNull(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    // =====================================================
    // ENTITY -> RESPONSE
    // =====================================================

    private AddressResponse toResponse(
            Address address) {

        return new AddressResponse(
                address.getId(),
                address.getFullName(),
                address.getPhone(),
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getCity(),
                address.getState(),
                address.getPincode(),
                address.getCountry()
        );
    }
}