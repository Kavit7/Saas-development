package com.saas.backend.dto;

import java.util.UUID;

import lombok.Data;



@Data

public class PropertyRequest  {
    
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
   


}
