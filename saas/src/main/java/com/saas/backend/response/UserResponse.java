package com.saas.backend.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.saas.backend.models.Gender;
import com.saas.backend.models.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor 
public class UserResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private Gender gender;
    private String phone;
    private String role;
    private UserStatus status;
    private UUID companyId;
    private String companyName;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public UserResponse(UUID id, String role, UserStatus status, OffsetDateTime createdAt) {
        this.id = id;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
    }
}
