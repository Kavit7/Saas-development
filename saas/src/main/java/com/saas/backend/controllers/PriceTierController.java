package com.saas.backend.controllers;

import java.util.List;
import java.util.Map;
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

import com.saas.backend.dto.PriceTierRequest;
import com.saas.backend.response.PriceTierResponse;
import com.saas.backend.serviceImpl.PriceTierServiceImpl;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/price-tier")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PriceTierController {

    private final PriceTierServiceImpl priceTierService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RESERVATION_MANAGER')")
    public ResponseEntity<PriceTierResponse> createPriceTier(
            @RequestBody PriceTierRequest request) {

        PriceTierResponse priceTier =
                priceTierService.createPriceTier(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(priceTier);
    }

    @PutMapping("/{priceId}")
    @PreAuthorize("hasAnyRole('ADMIN','RESERVATION_MANAGER')")
    public ResponseEntity<PriceTierResponse> updatePriceTier(
            @PathVariable UUID priceId,
            @RequestBody PriceTierRequest request) {
        PriceTierResponse priceTier =
                priceTierService.updatPriceTier(priceId, request);

        return ResponseEntity.ok(priceTier);
    }

    @GetMapping
    public ResponseEntity<List<PriceTierResponse>> getAllPriceTier() {
        List<PriceTierResponse> priceTiers =
                priceTierService.getAllPriceTier();
        return ResponseEntity.ok(priceTiers);
    }

    @DeleteMapping("/{priceId}")
    @PreAuthorize("hasAnyRole('ADMIN','RESERVATION_MANAGER')")
    public ResponseEntity<?> deletePriceTier(
            @PathVariable UUID priceId) {
        priceTierService.deletePriceTier(priceId);
        return ResponseEntity.ok(Map.of("message", "Deleted successfully"));
    }
}