package com.saas.backend.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.saas.backend.dto.AccommodationInquiryRequest;
import com.saas.backend.response.AccommodationInquiryResponse;
import com.saas.backend.service.AccommodationInquiryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/accommodation-inquiries")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Accommodation Inquiries", description = "Operational inquiries between Sales and Reservation Desk")
public class AccommodationInquiryController {

    private final AccommodationInquiryService inquiryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SALES_PERSON', 'RESERVATION_MANAGER')")
    @Operation(summary = "Send an inquiry regarding an accommodation booking")
    public ResponseEntity<AccommodationInquiryResponse> createInquiry(
            @RequestBody AccommodationInquiryRequest request,
            Authentication auth) {
        AccommodationInquiryResponse response = inquiryService.createInquiry(request, auth);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SALES_PERSON', 'RESERVATION_MANAGER')")
    @Operation(summary = "List accommodation inquiries for company or specific booking")
    public ResponseEntity<List<AccommodationInquiryResponse>> getInquiries(
            @RequestParam(required = false) UUID bookingId,
            Authentication auth) {
        List<AccommodationInquiryResponse> response = inquiryService.getInquiries(auth, bookingId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'RESERVATION_MANAGER')")
    @Operation(summary = "Update status of an inquiry")
    public ResponseEntity<AccommodationInquiryResponse> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        String status = body.getOrDefault("status", "RESOLVED");
        AccommodationInquiryResponse response = inquiryService.updateInquiryStatus(id, status, auth);
        return ResponseEntity.ok(response);
    }
}
