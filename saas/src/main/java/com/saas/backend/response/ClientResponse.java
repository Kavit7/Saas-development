package com.saas.backend.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor 
public class ClientResponse {
    String companyName;
    String salePeson;
    UUID id;
    OffsetDateTime createdAt;

}
