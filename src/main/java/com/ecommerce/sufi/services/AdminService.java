package com.ecommerce.sufi.services;

import org.springframework.data.domain.Page;

import com.ecommerce.sufi.dto.AdminDashboardResponse;
import com.ecommerce.sufi.dto.AdminUserCreateRequest;
import com.ecommerce.sufi.dto.AdminUserUpdateRequest;
import com.ecommerce.sufi.dto.UserResponse;
import com.ecommerce.sufi.model.RoleName;
import java.util.Set;

public interface AdminService {

    // Dashboard
    AdminDashboardResponse getDashboard();

    // Users
    Page<UserResponse> getUsers(int page, int size);

    UserResponse getUser(Long id);
    UserResponse createUser(AdminUserCreateRequest request);
    UserResponse updateUser(Long id, AdminUserUpdateRequest request);

    void deleteUser(Long id);
    UserResponse setUserEnabled(Long id, boolean enabled);
    Page<UserResponse> getSellers(int page, int size);
    UserResponse setUserRoles(Long id, Set<RoleName> roles);
}
