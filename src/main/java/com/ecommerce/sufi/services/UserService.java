package com.ecommerce.sufi.services;


import com.ecommerce.sufi.dto.LoginResponse;
import com.ecommerce.sufi.model.User;

public interface UserService {

    User register(User user);

    User getUserById(Long id);
    
    LoginResponse login(String email, String password);
}