package com.saas.backend.controllers;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.saas.backend.dto.PropertyRequest;
import com.saas.backend.models.VerificationStatus;
import com.saas.backend.response.PropertyResponse;
import com.saas.backend.service.PropertyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping({"/properties", "/api/v1/properties"})
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Properties", description = "Manage properties")
public class PropertyController {

    private final PropertyService propertyService;

    @PostMapping
    @Operation(summary = "Create property")
    @PreAuthorize("hasAnyRole('ADMIN','RESERVATION_MANAGER')")
    public ResponseEntity<PropertyResponse> createProperty(
            @Valid @RequestBody PropertyRequest request) {

        PropertyResponse response =
                propertyService.createProperty(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    @Operation(summary = "Get all properties")
    public ResponseEntity<Page<PropertyResponse>> getAllProperty(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "name")
            String sortBy,

            @RequestParam(defaultValue = "asc")
            String direction,

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            VerificationStatus status) {

        Page<PropertyResponse> properties =
                propertyService.getAllProperty(
                        page,
                        size,
                        sortBy,
                        direction,
                        search,
                        status
                );

        return ResponseEntity.ok(properties);
    }

    @GetMapping("/{propertyId}")
    @Operation(summary = "Get property by ID")
    public ResponseEntity<PropertyResponse> getPropertyById(
            @PathVariable UUID propertyId) {

        PropertyResponse property =
                propertyService.getPropertyById(propertyId);

        return ResponseEntity.ok(property);
    }

    @PutMapping("/{propertyId}")
    @Operation(summary = "Update property")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','RESERVATION_MANAGER')")
    public ResponseEntity<PropertyResponse> updateProperty(
            @PathVariable UUID propertyId,
            @Valid @RequestBody PropertyRequest request) {

        PropertyResponse property =
                propertyService.UpdateProperty(propertyId, request);

        return ResponseEntity.ok(property);
    }

    @PatchMapping("/{propertyId}/verify")
    @Operation(summary = "Verify property")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','RESERVATION_MANAGER')")
    public ResponseEntity<Void> verifyProperty(
            @PathVariable UUID propertyId) {

        propertyService.PropertyVerification(propertyId);

        return ResponseEntity.noContent().build();
    }
}