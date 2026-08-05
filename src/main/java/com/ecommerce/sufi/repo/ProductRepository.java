package com.ecommerce.sufi.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.sufi.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

}