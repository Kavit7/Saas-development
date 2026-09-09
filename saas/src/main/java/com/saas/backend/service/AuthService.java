package com.saas.backend.service;

import com.saas.backend.dto.LoginRequest;
import com.saas.backend.response.AuthResponse;

public interface AuthService {
    
     public AuthResponse login(LoginRequest request);
     
}
