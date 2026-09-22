package com.saas.backend.dto;

import java.time.LocalDate;


import com.saas.backend.models.Gender;

import lombok.Data;



@Data 
public class GuestRequest {
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String nationality;
    private String passportNumber;
    private LocalDate passportExpiry;
    private Gender gender;
}
