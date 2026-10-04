package com.saas.backend.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.saas.backend.models.ClientStatus;
import com.saas.backend.models.Gender;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder
@NoArgsConstructor
@AllArgsConstructor 
public class ClientResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private Gender gender;
    private String email;
    private String phone;
    private String countryOfResidence;
    private String nationality;
    private String preferredLanguage;
    private String notes;
    private ClientStatus status;
    private UUID salesPersonId;
    private String salePeson;
    private UUID companyId;
    private String companyName;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public ClientResponse(String companyName, String salePeson, UUID id, OffsetDateTime createdAt) {
        this.companyName = companyName;
        this.salePeson = salePeson;
        this.id = id;
        this.createdAt = createdAt;
    }
}
