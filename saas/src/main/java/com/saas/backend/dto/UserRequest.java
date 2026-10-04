package com.saas.backend.dto;


import com.saas.backend.models.Gender;
import com.saas.backend.models.UserStatus;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor
public class UserRequest {
    
    private String companyName;
    private String firstName;
    private String lastName;
    private Gender gender;
    private String roleName;
    @Email 
    private String email;
    private String phone;
    private UserStatus status;
    private String password;


    
}
