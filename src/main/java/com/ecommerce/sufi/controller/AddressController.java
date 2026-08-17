package com.ecommerce.sufi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.sufi.dto.AddressRequest;
import com.ecommerce.sufi.dto.AddressResponse;
import com.ecommerce.sufi.services.AddressService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {
    private final AddressService addressService;
    public AddressController(AddressService addressService) { this.addressService = addressService; }
    @PostMapping public ResponseEntity<AddressResponse> create(Authentication auth, @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.create(auth.getName(), request)); }
    @GetMapping public ResponseEntity<List<AddressResponse>> all(Authentication auth) { return ResponseEntity.ok(addressService.findAll(auth.getName())); }
    @GetMapping("/{id}") public ResponseEntity<AddressResponse> one(Authentication auth, @PathVariable Long id) { return ResponseEntity.ok(addressService.findById(auth.getName(), id)); }
    @PutMapping("/{id}") public ResponseEntity<AddressResponse> update(Authentication auth, @PathVariable Long id, @Valid @RequestBody AddressRequest request) { return ResponseEntity.ok(addressService.update(auth.getName(), id, request)); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long id) { addressService.delete(auth.getName(), id); return ResponseEntity.noContent().build(); }
}
