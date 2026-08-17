package com.ecommerce.sufi.services;

import java.util.Set;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.sufi.dto.LoginResponse;
import com.ecommerce.sufi.dto.UserProfileUpdateRequest;
import com.ecommerce.sufi.exception.BadRequestException;
import com.ecommerce.sufi.exception.ResourceNotFoundException;
import com.ecommerce.sufi.exception.UnauthorizedException;
import com.ecommerce.sufi.model.Role;
import com.ecommerce.sufi.model.RoleName;
import com.ecommerce.sufi.model.User;
import com.ecommerce.sufi.repo.RoleRepository;
import com.ecommerce.sufi.repo.UserRepository;
import com.ecommerce.sufi.repo.PasswordResetTokenRepository;
import com.ecommerce.sufi.model.PasswordResetToken;
import com.ecommerce.sufi.security.JwtService;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public UserServiceImpl(
            UserRepository userRepository,
            JwtService jwtService,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder, PasswordResetTokenRepository passwordResetTokenRepository) {

        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @Override
    public User register(User user) {

        user.setEmail(user.getEmail().trim().toLowerCase());

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(RoleName.ROLE_USER);
                    return roleRepository.save(role);
                });
        user.getRoles().add(userRole);
        return userRepository.save(user);
    }

    @Override
    public User getUserById(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public User updateProfile(String email, UserProfileUpdateRequest request) {
        User user = getUserByEmail(email);
        user.setName(request.name().trim());
        user.setPhone(request.phone().trim());
        return userRepository.save(user);
    }

    @Override
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = getUserByEmail(email);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BadRequestException("New password must be different from the current password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setAuthVersion(user.getAuthVersion() + 1);
        userRepository.save(user);
    }

    @Override
    public LoginResponse login(String email, String password) {

        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!user.isEnabled()) {
            throw new UnauthorizedException("This account is disabled");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        Set<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toUnmodifiableSet());

        if (roles.isEmpty()) {
            throw new UnauthorizedException("User has no roles assigned");
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                roles,
                user.getAuthVersion()
        );

        return new LoginResponse(
                "Login successful",
                token
        );

    }

    @Override
    @Transactional
    public String createPasswordResetToken(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase()).orElse(null);
        if (user == null || !user.isEnabled()) return null;
        passwordResetTokenRepository.deleteByUserId(user.getId());
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setTokenHash(hashToken(rawToken));
        resetToken.setUser(user);
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        passwordResetTokenRepository.save(resetToken);
        return rawToken;
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenHashAndUsedAtIsNull(hashToken(token))
                .orElseThrow(() -> new BadRequestException("Reset link is invalid or already used"));
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset link has expired");
        }
        User user = resetToken.getUser();
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BadRequestException("New password must be different from the old password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setAuthVersion(user.getAuthVersion() + 1);
        userRepository.save(user);
        resetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(resetToken);
    }

    private String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    @Override
    @Transactional
    public void logout(String email) {
        User user = getUserByEmail(email);
        user.setAuthVersion(user.getAuthVersion() + 1);
        userRepository.save(user);
    }
}
