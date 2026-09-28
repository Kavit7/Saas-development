package com.saas.backend.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor 

public class PropertyResponse {  
    private UUID id;
    private String name;
    private String createdBy;
    
}
