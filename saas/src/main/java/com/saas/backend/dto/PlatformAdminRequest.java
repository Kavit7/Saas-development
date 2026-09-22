package com.saas.backend.dto;

import com.saas.backend.models.PlatformAdminStatus;
import com.saas.backend.models.PlatformRole;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;



@AllArgsConstructor 
@Data 
public class PlatformAdminRequest {
    
    private String firstName;
    private String lastName;
    private String phone;
    @Email 
    private String email;
    private String password;
    private PlatformRole role;
    private PlatformAdminStatus status;
}
