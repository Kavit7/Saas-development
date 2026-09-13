package com.saas.backend.dto;

import lombok.*;

@Data
@AllArgsConstructor 
public class CompanyUpdate {
     private String name;
     private String timezone;
     private String country;
     private String phone;
     private String email;
}
