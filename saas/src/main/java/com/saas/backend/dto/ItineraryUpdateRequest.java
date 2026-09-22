package com.saas.backend.dto;

import java.util.List;

import lombok.Data;



@Data 
public class ItineraryUpdateRequest {
    

    private List<ItineraryDayUpdateRequest> days;
}
