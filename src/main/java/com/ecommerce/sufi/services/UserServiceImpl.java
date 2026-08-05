package com.ecommerce.sufi.services;

import org.springframework.stereotype.Service;

import com.ecommerce.sufi.dto.LoginResponse;
import com.ecommerce.sufi.model.User;
import com.ecommerce.sufi.repo.UserRepository;
import com.ecommerce.sufi.security.JwtService;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public UserServiceImpl(
            UserRepository userRepository,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public User register(User user) {

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        return userRepository.save(user);
    }

    @Override
    public User getUserById(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    @Override
    public LoginResponse login(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Invalid email or password"));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid email or password");
        }

        String role = user.getRoles()
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("User has no role assigned"))
                .getName()
                .name();

        String token = jwtService.generateToken(
                user.getEmail(),
                role
        );

        return new LoginResponse(
                "Login successful",
                token
        );
    
    }
}