package com.saas.backend.dto;

import java.time.OffsetDateTime;

import com.saas.backend.models.FlightType;

import lombok.AllArgsConstructor;
import lombok.Data;
@Data 
@AllArgsConstructor 
public class FlightRequest {
    private FlightType flightType;
    private String airline;
    private String flightNumber;
    private String airport;
    private OffsetDateTime arrivalDatetime;
    private OffsetDateTime departureDatetime;
}
