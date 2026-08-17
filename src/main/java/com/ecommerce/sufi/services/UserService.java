package com.ecommerce.sufi.services;


import com.ecommerce.sufi.dto.LoginResponse;
import com.ecommerce.sufi.dto.UserProfileUpdateRequest;
import com.ecommerce.sufi.model.User;

public interface UserService {

    User register(User user);

    User getUserById(Long id);

    User getUserByEmail(String email);

    User updateProfile(String email, UserProfileUpdateRequest request);

    void changePassword(String email, String currentPassword, String newPassword);

    LoginResponse login(String email, String password);
    String createPasswordResetToken(String email);
    void resetPassword(String token, String newPassword);
    void logout(String email);
}
