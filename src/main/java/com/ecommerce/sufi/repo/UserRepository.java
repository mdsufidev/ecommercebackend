package com.ecommerce.sufi.repo;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ecommerce.sufi.model.RoleName;
import com.ecommerce.sufi.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRolesName(RoleName roleName);
    Page<User> findDistinctByRolesName(RoleName roleName, Pageable pageable);
}
