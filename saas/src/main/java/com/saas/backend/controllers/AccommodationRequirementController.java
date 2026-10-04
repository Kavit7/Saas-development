package com.saas.backend.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.backend.dto.RequirementRequest;
import com.saas.backend.response.AccommodationRequirementResponse;
import com.saas.backend.service.AccommodationRequirementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/accommodation-requirements")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "Accommodation Requirements",
        description = "Manage accommodation requirements"
)
public class AccommodationRequirementController {

    private final AccommodationRequirementService accommodationRequirementService;


    @PostMapping("/itinerary-day/{itineraryId}")
    @Operation(summary = "Create accommodation requirement")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_PERSON','RESERVATION_MANAGER')")
    public ResponseEntity<AccommodationRequirementResponse> createAccommodationRequirement(
            @PathVariable UUID itineraryId,
            @Valid @RequestBody RequirementRequest request) {

        AccommodationRequirementResponse accommodationRequirement =
                accommodationRequirementService
                        .createAccommodationRequirement(
                                itineraryId,
                                request
                        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(accommodationRequirement);
    }

    @GetMapping("/safari/{safariId}")
    @Operation(summary = "Get accommodation requirements by safari")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_PERSON','RESERVATION_MANAGER')")
    public ResponseEntity<List<AccommodationRequirementResponse>> getAccommodationRequirementsBySafari(
            @PathVariable UUID safariId) {
        return ResponseEntity.ok(
                accommodationRequirementService.getAccommodationRequirementsBySafari(safariId)
        );
    }

    @GetMapping("/itinerary-day/{itineraryId}")
    @Operation(summary = "Get accommodation requirement by itinerary day")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_PERSON','RESERVATION_MANAGER')")
    public ResponseEntity<?> getAccommodationRequirementByItineraryDay(
            @PathVariable UUID itineraryId) {
        AccommodationRequirementResponse response =
                accommodationRequirementService.getAccommodationRequirementByItineraryDay(itineraryId);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{requirementId}")
    @Operation(summary = "Get accommodation requirement")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_PERSON','RESERVATION_MANAGER')")
    public ResponseEntity<AccommodationRequirementResponse> getAccommodationRequirement(
            @PathVariable UUID requirementId) {

        AccommodationRequirementResponse accommodationRequirement =
                accommodationRequirementService
                        .getAccommodationRequirementById(
                                requirementId
                        );

        return ResponseEntity.ok(accommodationRequirement);
    }


    @PutMapping("/{requirementId}")
    @Operation(summary = "Update accommodation requirement")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_PERSON','RESERVATION_MANAGER')")
    public ResponseEntity<AccommodationRequirementResponse> updateAccommodationRequirement(
            @PathVariable UUID requirementId,
            @Valid @RequestBody RequirementRequest request) {

       AccommodationRequirementResponse accommodationRequirement =
                accommodationRequirementService
                        .updateAccommodationRequirement(
                                requirementId,
                                request
                        );

        return ResponseEntity.ok(accommodationRequirement);
    }


    @DeleteMapping("/{requirementId}")
    @Operation(summary = "Delete accommodation requirement")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_PERSON')")
    public ResponseEntity<Void> deleteAccommodationRequirement(
            @PathVariable UUID requirementId) {

        accommodationRequirementService
                .deleteAccommodationRequirement(requirementId);

        return ResponseEntity.noContent().build();
    }
}