package com.saas.backend.dto;

import java.util.UUID;

import com.saas.backend.models.ClientStatus;

import lombok.AllArgsConstructor;
import lombok.Data;



@Data 
@AllArgsConstructor 
public class ClientRequest {
    
    private UUID saleId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String nationality;
    private String preferredLanguage;
    private String countryOfResidence;
    private String notes;
    private ClientStatus status;
}
