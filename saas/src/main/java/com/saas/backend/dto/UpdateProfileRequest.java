package com.saas.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.saas.backend.models.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateProfileRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private Gender gender;
}
