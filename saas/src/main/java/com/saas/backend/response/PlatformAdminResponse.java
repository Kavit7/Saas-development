package com.saas.backend.response;

import java.util.UUID;

import com.saas.backend.models.PlatformAdminStatus;
import com.saas.backend.models.PlatformRole;

import lombok.AllArgsConstructor;
import lombok.Data;



@Data 
@AllArgsConstructor 
public class PlatformAdminResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private PlatformRole role; 
    private PlatformAdminStatus status;
}
