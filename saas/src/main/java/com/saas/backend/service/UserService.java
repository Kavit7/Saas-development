package com.saas.backend.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

import com.saas.backend.dto.UserRequest;
import com.saas.backend.dto.UserStatusRequest;
import com.saas.backend.models.User;
import com.saas.backend.response.UserResponse;

public interface UserService {
     


    UserResponse createAdmin(UserRequest userRequest);

    Page<User> getAllUsers(int page,int size, String sortBy,String direction);

    User getUserById(UUID id, Authentication auth);

    User updateUserStatus(UUID id ,UserStatusRequest status,Authentication auth);
} 
