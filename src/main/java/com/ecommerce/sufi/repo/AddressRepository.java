package com.ecommerce.sufi.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.sufi.model.Address;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findAllByUserId(Long userId);
    Optional<Address> findByIdAndUserId(Long id, Long userId);
}
