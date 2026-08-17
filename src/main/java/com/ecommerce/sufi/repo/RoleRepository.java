package com.ecommerce.sufi.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.sufi.model.Role;
import com.ecommerce.sufi.model.RoleName;


public interface RoleRepository extends JpaRepository<Role, Long>{
	Optional<Role> findByName(RoleName name);
	boolean existsByName(RoleName name);

}
