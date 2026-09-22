package com.saas.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class ClientUpdate {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String nationality;
    private String preferredLanguage;
    private String countryOfResidence;
    private String notes;
}
