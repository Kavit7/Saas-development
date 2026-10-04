package com.saas.backend.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.saas.backend.models.VerificationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder
@NoArgsConstructor
@AllArgsConstructor 
public class PropertyResponse {  
    private UUID id;
    private String name;
    private String slug;
    private String location;
    private String region;
    private String country;
    private UUID categoryId;
    private String categoryName;
    private UUID priceTierId;
    private String priceTierName;
    private String description;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private String website;
    private VerificationStatus verificationStatus;
    private String createdBy;
    private OffsetDateTime verifiedAt;
    private String verifiedBy;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public PropertyResponse(UUID id, String name, String createdBy) {
        this.id = id;
        this.name = name;
        this.createdBy = createdBy;
    }
}
