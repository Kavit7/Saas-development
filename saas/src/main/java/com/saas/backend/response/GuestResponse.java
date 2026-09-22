package com.saas.backend.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor 

public class GuestResponse { 
    private UUID id;
    private String firstName;
    private String lastName;
}
