package com.saas.backend.response;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;



@Data 
@AllArgsConstructor 
public class CompanyResponse {
    private String id;
    private String slug;
    private OffsetDateTime createdAt;
}
