package com.ecommerce.sufi.services;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ecommerce.sufi.dto.AdminDashboardResponse;
import com.ecommerce.sufi.dto.AdminUserCreateRequest;
import com.ecommerce.sufi.dto.AdminUserUpdateRequest;
import com.ecommerce.sufi.dto.UserResponse;
import com.ecommerce.sufi.exception.BadRequestException;
import com.ecommerce.sufi.exception.ResourceNotFoundException;
import com.ecommerce.sufi.model.OrderStatus;
import com.ecommerce.sufi.model.ProductStatus;
import com.ecommerce.sufi.model.RoleName;
import com.ecommerce.sufi.model.User;
import com.ecommerce.sufi.repo.OrderRepository;
import com.ecommerce.sufi.repo.ProductRepository;
import com.ecommerce.sufi.repo.UserRepository;
import com.ecommerce.sufi.repo.CategoryRepository;
import com.ecommerce.sufi.repo.RoleRepository;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CategoryRepository categoryRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminServiceImpl(
            UserRepository userRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository, CategoryRepository categoryRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.categoryRepository = categoryRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =====================================================
    // DASHBOARD
    // =====================================================

    @Override
    public AdminDashboardResponse getDashboard() {

        return new AdminDashboardResponse(
                userRepository.count(),
                userRepository.countByRolesName(RoleName.ROLE_SELLER),
                productRepository.count(),
                categoryRepository.count(),
                productRepository.countByStatus(ProductStatus.PENDING),
                orderRepository.count(),
                orderRepository.countByStatus(OrderStatus.PENDING),
                orderRepository.calculateCompletedRevenue()
        );
    }

    // =====================================================
    // GET USERS
    // =====================================================

    @Override
    public Page<UserResponse> getUsers(int page, int size) {

        if (page < 0) {
            throw new BadRequestException(
                    "Page number cannot be negative"
            );
        }

        if (size <= 0) {
            throw new BadRequestException(
                    "Page size must be greater than zero"
            );
        }

        return userRepository
                .findAll(PageRequest.of(page, size))
                .map(this::toResponse);
    }

    // =====================================================
    // GET USER BY ID
    // =====================================================

    @Override
    public UserResponse getUser(Long id) {

        User user = userRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );

        return toResponse(user);
    }

    // =====================================================
    // CREATE / UPDATE USER
    // =====================================================

    @Override
    @Transactional
    public UserResponse createUser(AdminUserCreateRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email is already registered");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPhone(request.phone().trim());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEnabled(request.enabled() == null || request.enabled());
        user.setRoles(resolveRoles(request.roles()));
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, AdminUserUpdateRequest request) {

        User user = userRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );

        String email = normalizeEmail(request.email());
        if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email is already registered");
        }
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPhone(request.phone().trim());
        user.setEnabled(request.enabled());
        user.setRoles(resolveRoles(request.roles()));
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
            user.setAuthVersion(user.getAuthVersion() + 1);
        }
        return toResponse(userRepository.save(user));
    }

    // =====================================================
    // DELETE USER
    // =====================================================

    @Override
    @Transactional
    public void deleteUser(Long id) {

        User user = userRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );

        userRepository.delete(user);
    }

    @Override
    @Transactional
    public UserResponse setUserEnabled(Long id, boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setEnabled(enabled);
        return toResponse(userRepository.save(user));
    }

    @Override
    public Page<UserResponse> getSellers(int page, int size) {
        if (page < 0 || size <= 0) throw new BadRequestException("Invalid pagination");
        return userRepository.findDistinctByRolesName(RoleName.ROLE_SELLER, PageRequest.of(page, size))
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public UserResponse setUserRoles(Long id, Set<RoleName> roles) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setRoles(resolveRoles(roles));
        return toResponse(userRepository.save(user));
    }

    private Set<com.ecommerce.sufi.model.Role> resolveRoles(Set<RoleName> roles) {
        if (roles == null || roles.isEmpty()) throw new BadRequestException("At least one role is required");
        return roles.stream().map(roleName -> roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName)))
                .collect(Collectors.toSet());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    // =====================================================
    // USER RESPONSE MAPPER
    // =====================================================

    private UserResponse toResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRoles()
                        .stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toUnmodifiableSet()), user.isEnabled(),
                user.getCreatedAt()
        );
    }
}
