package com.saas.backend.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.backend.dto.ItineraryUpdateRequest;
import com.saas.backend.dto.SafariRequest;
import com.saas.backend.models.ItineraryDay;
import com.saas.backend.models.Safari;
import com.saas.backend.serviceImpl.SafariServiceImpl;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping("/safari")
@SecurityRequirement(name="bearerAuth")
@RequiredArgsConstructor 
public class SafariController {
       private final SafariServiceImpl safariServiceImpl;
       


    @PostMapping("{clientId}/create")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_PERSON')")
    ResponseEntity<?> createClientSafari( @PathVariable UUID clientId,@RequestBody SafariRequest safariRequest){
        Safari safari = safariServiceImpl.createClientSafari(clientId, safariRequest);
        return  ResponseEntity.ok(Map.of("message","Safari Successfully created", "data", safari));
    }
    

    @GetMapping("/{safariId}/itineraryDay")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_PERSON','RESERVATION_MANAGER')")
    ResponseEntity<?> getSafariItinerary(@PathVariable  UUID safariId){
        List<ItineraryDay> itinerary= safariServiceImpl.getItineraryDaySafari(safariId);
         return  ResponseEntity.ok(Map.of("message","Itinerary day Successfully Loaded", "data", itinerary));
    }

@PutMapping("/{safariId}/update/ItinenaryDays")
@PreAuthorize("hasAnyRole('ADMIN','SALES_PERSON')")
ResponseEntity<?> updateSafariItenaryDay(
        @PathVariable UUID safariId,
        @RequestBody ItineraryUpdateRequest request) {

    safariServiceImpl.updateSafariItenaryDay(safariId, request);

    return ResponseEntity.ok(
            Map.of("message", "Successfully saved all days")
    );
}

}
