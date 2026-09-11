package com.saas.backend.service;

import com.saas.backend.dto.UserRequest;
import com.saas.backend.response.UserResponse;

public interface UserService {
     


    UserResponse createAdmin(UserRequest userRequest);

    
} 
