package com.saas.backend.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

import com.saas.backend.dto.UserRequest;
import com.saas.backend.dto.UserStatusRequest;
import com.saas.backend.response.UserResponse;

public interface UserService {

    UserResponse createAdmin(UserRequest userRequest);

    Page<UserResponse> getAllUsers(int page, int size, String sortBy, String direction, String role, String company, String status, String search);

    default Page<UserResponse> getAllUsers(int page, int size, String sortBy, String direction) {
        return getAllUsers(page, size, sortBy, direction, null, null, null, null);
    }

    UserResponse getUserById(UUID id, Authentication auth);

    UserResponse updateUserStatus(UUID id, UserStatusRequest status, Authentication auth);

    UserResponse updateUser(UUID id, UserRequest request, Authentication auth);

    java.util.Map<String, Object> triggerPasswordResetForUser(UUID id, Authentication auth);

    java.util.Map<String, Object> personalizeUserAccount(UUID id, com.saas.backend.dto.AccountPersonalizationRequest request, Authentication auth);

    java.util.Map<String, Object> impersonateUser(UUID id, String reason, Authentication auth);
}
