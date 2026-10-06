package com.saas.backend.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyRequest {
    private String name;
    private String location;
    private String region;
    private String country;
    private UUID categoryId;
    private UUID priceId;
    private String description;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private String website;
    private List<UUID> amenityIds;
    private List<UUID> tagIds;
}
