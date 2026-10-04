package com.saas.backend.dto;



import com.saas.backend.models.ClientStatus;
import com.saas.backend.models.Gender;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor
public class ClientRequest {
    
    private String firstName;
    private String lastName;
    private Gender gender;
    private String email;
    private String phone;
    private String nationality;
    private String preferredLanguage;
    private String countryOfResidence;
    private String notes;
    private ClientStatus status;
}
