package com.saas.backend.service;

import java.util.Map;
import org.springframework.security.core.Authentication;

import com.saas.backend.dto.ChangePasswordRequest;
import com.saas.backend.dto.LoginRequest;
import com.saas.backend.response.AuthResponse;
import com.saas.backend.response.UserResponse;

public interface AuthService {    
    AuthResponse login(LoginRequest request);
    AuthResponse platformAdminlogin(LoginRequest request);
    UserResponse getCurrentUserProfile(Authentication auth);
    Map<String, Object> changePassword(ChangePasswordRequest request, Authentication auth);
}
