package com.saas.backend.dto;


import com.saas.backend.models.Company;
import com.saas.backend.models.Role;
import com.saas.backend.models.UserStatus;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;



@Data 
@AllArgsConstructor 
public class UserRequest {
    private String companyName;
    private String firstName;
    private String lastName;
    private String roleName;
    @Email 
    private String email;
    private String phone;
    private UserStatus status;
    private String password;


    
}
