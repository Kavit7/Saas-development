package com.saas.backend.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.backend.dto.FlightRequest;
import com.saas.backend.models.FlightDetail;
import com.saas.backend.serviceImpl.FlightDetailServiceImpl;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/client-flight")
@SecurityRequirement(name="bearerAuth")
@RequiredArgsConstructor 

public class FlightDetailController {

    private final FlightDetailServiceImpl flightDetailServiceImpl;


    @PostMapping("/{clientId}/create")
    @PreAuthorize("hasAnyRole('SALES_PERSON','ADMIN')")
    ResponseEntity<?> createClientFlightDetail(@PathVariable UUID clientId,@RequestBody FlightRequest request){
        FlightDetail flightDetail= flightDetailServiceImpl.createClientFlightDetails(clientId, request);
        return ResponseEntity.ok(Map.of("message","Flight Details added successfullt to client: "+flightDetail.getClient().getFirstName() +" - "+flightDetail.getClient().getLastName()));
}
    @GetMapping("/{clientId}/view")
    @PreAuthorize("hasAnyRole('SALES_PERSON','ADMIN')")
    ResponseEntity<?> getFlightDetails(@PathVariable UUID clientId){
        List<FlightDetail> flights= flightDetailServiceImpl.getClientFlightDetails(clientId);
        return ResponseEntity.ok(Map.of("message","flight details","data",flights));
    }
}
