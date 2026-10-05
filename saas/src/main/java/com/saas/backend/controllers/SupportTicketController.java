package com.saas.backend.controllers;


import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.saas.backend.dto.SupportTicketRequest;
import com.saas.backend.dto.SupportTicketUpdateRequest;
import com.saas.backend.response.SupportTicketResponse;
import com.saas.backend.service.SupportTicketService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/support-tickets")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Support Tickets", description = "Operational issues and internal company support escalation")
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SALES_PERSON', 'RESERVATION_MANAGER', 'GUIDE')")
    @Operation(summary = "Raise a new support ticket")
    public ResponseEntity<SupportTicketResponse> createTicket(
            @RequestBody SupportTicketRequest request,
            Authentication auth) {
        SupportTicketResponse response = supportTicketService.createTicket(request, auth);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SALES_PERSON', 'RESERVATION_MANAGER', 'GUIDE')")
    @Operation(summary = "Get list of support tickets for company")
    public ResponseEntity<Page<SupportTicketResponse>> getTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String status,
            Authentication auth) {
        Page<SupportTicketResponse> response = supportTicketService.getTickets(auth, page, size, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SALES_PERSON', 'RESERVATION_MANAGER', 'GUIDE')")
    @Operation(summary = "Get details of a specific support ticket")
    public ResponseEntity<SupportTicketResponse> getTicketById(
            @PathVariable UUID id,
            Authentication auth) {
        SupportTicketResponse response = supportTicketService.getTicketById(id, auth);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'RESERVATION_MANAGER')")
    @Operation(summary = "Update, assign, or resolve support ticket")
    public ResponseEntity<SupportTicketResponse> updateTicket(
            @PathVariable UUID id,
            @RequestBody SupportTicketUpdateRequest request,
            Authentication auth) {
        SupportTicketResponse response = supportTicketService.updateTicket(id, request, auth);
        return ResponseEntity.ok(response);
    }
}
