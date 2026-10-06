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

import com.saas.backend.dto.AmenityRequest;
import com.saas.backend.response.AmenityResponse;
import com.saas.backend.service.AmenityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping({"/amenities", "/api/v1/amenities"})
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Amenities", description = "Manage property amenities")
public class AmenityController {

    private final AmenityService amenityService;

    @PostMapping
    @Operation(summary = "Create an amenity")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','RESERVATION_MANAGER')")
    public ResponseEntity<AmenityResponse> createAmenity(@Valid @RequestBody AmenityRequest request) {
        AmenityResponse response = amenityService.createAmenity(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all amenities")
    public ResponseEntity<List<AmenityResponse>> getAllAmenities() {
        List<AmenityResponse> list = amenityService.getAllAmenities();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get amenity by ID")
    public ResponseEntity<AmenityResponse> getAmenityById(@PathVariable UUID id) {
        AmenityResponse response = amenityService.getAmenityById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an amenity")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','RESERVATION_MANAGER')")
    public ResponseEntity<AmenityResponse> updateAmenity(
            @PathVariable UUID id,
            @Valid @RequestBody AmenityRequest request) {
        AmenityResponse response = amenityService.updateAmenity(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an amenity")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Void> deleteAmenity(@PathVariable UUID id) {
        amenityService.deleteAmenity(id);
        return ResponseEntity.noContent().build();
    }
}
