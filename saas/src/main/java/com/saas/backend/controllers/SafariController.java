package com.saas.backend.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.saas.backend.dto.ItineraryRequest;
import com.saas.backend.dto.ItineraryUpdateRequest;
import com.saas.backend.dto.SafariRequest;
import com.saas.backend.models.ItineraryDay;
import com.saas.backend.models.Safari;
import com.saas.backend.models.SafariStatus;
import com.saas.backend.serviceImpl.SafariServiceImpl;
import com.saas.backend.specification.SafariSpecification;

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
    
    @GetMapping("/{safariId}/itineraryDays")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_PERSON','RESERVATION_MANAGER')")
    ResponseEntity<?> getSafariItinerary(@PathVariable  UUID safariId){
        List<ItineraryDay> itinerary= safariServiceImpl.getItineraryDaySafari(safariId);
         return  ResponseEntity.ok(Map.of("message","Itinerary day Successfully Loaded", "data", itinerary));
    }

@PutMapping("/{safariId}/update/itinenaryDays")
@PreAuthorize("hasAnyRole('ADMIN','SALES_PERSON')")
ResponseEntity<?> updateSafariItenaryDay(
        @PathVariable UUID safariId,
        @RequestBody ItineraryUpdateRequest request) {
    safariServiceImpl.updateSafariItenaryDay(safariId, request);

    return ResponseEntity.ok(
            Map.of("message", "Successfully saved all days")
    );
}



@PutMapping("/{safariId}/itinerary-days/{dayId}")
@PreAuthorize("hasAnyRole('ADMIN','SALES_PERSON')")
public ResponseEntity<?> updateItineraryDay(
        @PathVariable UUID safariId,
        @PathVariable UUID dayId,
        @RequestBody ItineraryRequest request) {
    safariServiceImpl.updateItineraryDay(safariId, dayId,request);
    return ResponseEntity.ok(
            Map.of("message", "Itinerary day updated successfully")
    );
}




@DeleteMapping("/{safariId}/itinerary-days/{dayId}")
@PreAuthorize("hasAnyRole('ADMIN','SALES_PERSON')")
public ResponseEntity<?> deleteItineraryDay(
        @PathVariable UUID safariId,
        @PathVariable UUID dayId) {

    safariServiceImpl.deleteItineraryDay(safariId, dayId);
    return ResponseEntity.ok(
            Map.of("message", "Itinerary day deleted successfully")
    );
}


@GetMapping("/company")
@PreAuthorize("hasAnyRole('ADMIN','SALES_PERSON','RESERVATION_MANAGER')")
public ResponseEntity<?> getAllSafari(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "asc") String direction,
        @RequestParam(required = false) SafariStatus status,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate) {

    Page<Safari> safaris = safariServiceImpl.getAllSafari(
            page,
            size,
            sortBy,
            search,
            direction,
            status,
            startDate,
            endDate
    );

    return ResponseEntity.ok(
            Map.of(
                    "message", "Safaris successfully loaded",
                    "data", safaris
            )
    );
}


}
