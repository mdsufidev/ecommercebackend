package com.ecommerce.sufi.controller;

import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.sufi.dto.ChangePasswordRequest;
import com.ecommerce.sufi.dto.UserProfileUpdateRequest;
import com.ecommerce.sufi.dto.UserResponse;
import com.ecommerce.sufi.model.User;
import com.ecommerce.sufi.services.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(toResponse(userService.getUserByEmail(authentication.getName())));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(Authentication authentication,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        return ResponseEntity.ok(toResponse(userService.updateProfile(authentication.getName(), request)));
    }

    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(authentication.getName(), request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getPhone(),
                user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toUnmodifiableSet()),
                user.isEnabled(),
                user.getCreatedAt());
    }
}
