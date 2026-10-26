package com.saas.backend.dto;

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.saas.backend.models.Gender;
import com.saas.backend.models.UserStatus;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountPersonalizationRequest {
    
    @NotBlank(message = "Action type is required")
    private String actionType; // PASSWORD_RESET, STATUS_CHANGE, DATA_CORRECTION, CUSTOM_INTERVENTION

    @NotBlank(message = "Reason for personalization is mandatory for audit transparency")
    private String reason; // Why was this intervention performed?

    private String notes; // Additional support notes
    private UserStatus status;
    private String firstName;
    private String lastName;
    private String phone;
    private Gender gender;
    private String roleName;
    private String newPassword;
}
