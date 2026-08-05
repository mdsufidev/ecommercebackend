package com.ecommerce.sufi.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.sufi.model.Role;


public interface RoleRepository extends JpaRepository<Role, Long>{
	Optional<Role> findByName (String name);
	boolean existsByname (String name);
	
}
