package com.ecommerce.sufi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Value;

import com.ecommerce.sufi.dto.LoginRequest;
import com.ecommerce.sufi.dto.LoginResponse;
import com.ecommerce.sufi.dto.RegisterRequest;
import com.ecommerce.sufi.dto.UserResponse;
import com.ecommerce.sufi.dto.ForgotPasswordRequest;
import com.ecommerce.sufi.dto.ResetPasswordRequest;
import com.ecommerce.sufi.dto.PasswordResetResponse;
import com.ecommerce.sufi.model.User;
import com.ecommerce.sufi.services.UserService;
import com.ecommerce.sufi.services.GmailPasswordResetService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final boolean exposeResetToken;
    private final GmailPasswordResetService gmailPasswordResetService;

    public AuthController(UserService userService, GmailPasswordResetService gmailPasswordResetService,
            @Value("${app.password-reset.expose-token:true}") boolean exposeResetToken) {
        this.userService = userService;
        this.gmailPasswordResetService = gmailPasswordResetService;
        this.exposeResetToken = exposeResetToken;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setPhone(request.getPhone());

        User savedUser = userService.register(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(savedUser));
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = userService.login(
                request.getEmail(),
                request.getPassword()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<PasswordResetResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String token = userService.createPasswordResetToken(request.email());
        if (token != null && gmailPasswordResetService.isEnabled()) {
            gmailPasswordResetService.sendResetLink(request.email().trim().toLowerCase(), token);
        }
        return ResponseEntity.ok(new PasswordResetResponse(
                "If an enabled account exists, a password reset link has been sent to your email.",
                exposeResetToken ? token : null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<java.util.Map<String,String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(java.util.Map.of("message", "Password reset successful"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        userService.logout(authentication.getName());
        return ResponseEntity.noContent().build();
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getPhone(),
                user.getRoles().stream().map(role -> role.getName()).collect(java.util.stream.Collectors.toUnmodifiableSet()),
                user.isEnabled(), user.getCreatedAt());
    }
}
