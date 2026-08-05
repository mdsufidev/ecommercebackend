package com.ecommerce.sufi.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.sufi.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);
}