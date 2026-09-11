package com.saas.backend.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.saas.backend.models.UserStatus;

import lombok.*;

@Data
@AllArgsConstructor 
public class UserResponse {
       private UUID id;
       private String role;
       private UserStatus status;
       private OffsetDateTime createdAt;

}
