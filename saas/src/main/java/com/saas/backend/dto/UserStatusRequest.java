package com.saas.backend.dto;

import com.saas.backend.models.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Data;



@Data 
@AllArgsConstructor 
public class UserStatusRequest {
    UserStatus status;
}
