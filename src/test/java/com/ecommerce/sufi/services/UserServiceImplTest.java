package com.ecommerce.sufi.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ecommerce.sufi.model.Role;
import com.ecommerce.sufi.model.RoleName;
import com.ecommerce.sufi.model.User;
import com.ecommerce.sufi.repo.RoleRepository;
import com.ecommerce.sufi.repo.UserRepository;
import com.ecommerce.sufi.repo.PasswordResetTokenRepository;
import com.ecommerce.sufi.security.JwtService;

class UserServiceImplTest {
    @Test
    void registerHashesPasswordAndAssignsDefaultUserRole() {
        UserRepository users = mock(UserRepository.class);
        RoleRepository roles = mock(RoleRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        JwtService jwtService = mock(JwtService.class);
        PasswordResetTokenRepository resetTokens = mock(PasswordResetTokenRepository.class);
        Role userRole = new Role();
        userRole.setName(RoleName.ROLE_USER);
        User user = new User();
        user.setEmail("Customer@Example.com");
        user.setPassword("plain-password");

        when(users.existsByEmail("customer@example.com")).thenReturn(false);
        when(encoder.encode("plain-password")).thenReturn("bcrypt-hash");
        when(roles.findByName(RoleName.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = new UserServiceImpl(users, jwtService, roles, encoder, resetTokens).register(user);

        assertEquals("customer@example.com", saved.getEmail());
        assertEquals("bcrypt-hash", saved.getPassword());
        assertTrue(saved.getRoles().contains(userRole));
        verify(encoder).encode("plain-password");
    }
}
