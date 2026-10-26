package com.saas.backend.dto;

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.saas.backend.models.Gender;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRequest {
    
    private String companyName;
    private UUID companyId;
    private String firstName;
    private String lastName;
    private Gender gender;
    private String roleName;
    @Email 
    private String email;
    private String phone;
    
    private String password;


    
}
