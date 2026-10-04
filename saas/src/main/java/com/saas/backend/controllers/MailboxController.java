package com.saas.backend.controllers;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.saas.backend.dto.InvoiceCreateRequest;
import com.saas.backend.dto.InvoiceStatusUpdateRequest;
import com.saas.backend.models.InvoiceStatus;
import com.saas.backend.response.InvoiceResponse;
import com.saas.backend.response.MailboxEmailResponse;
import com.saas.backend.service.MailboxService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * MailboxController
 * Provides endpoints for inspecting lodge email inquiries/replies and accommodation booking invoices.
 */
@RestController
@RequestMapping({"/api/v1/mailbox", "/mailbox"})
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Mailbox & Invoices", description = "Lodge email communication and booking invoice tracking")
@RequiredArgsConstructor
public class MailboxController {

    private final MailboxService mailboxService;

    @GetMapping("/emails")
    @Operation(summary = "Get incoming lodge emails")
    public ResponseEntity<List<MailboxEmailResponse>> getEmails(
            @RequestParam(required = false) UUID bookingId
    ) {
        List<MailboxEmailResponse> emails = mailboxService.getAllEmails(bookingId);
        return ResponseEntity.ok(emails);
    }

    @GetMapping("/emails/{id}")
    @Operation(summary = "Get single incoming lodge email")
    public ResponseEntity<MailboxEmailResponse> getEmailById(@PathVariable UUID id) {
        MailboxEmailResponse email = mailboxService.getEmailById(id);
        return ResponseEntity.ok(email);
    }

    @PostMapping("/sync")
    @Operation(summary = "Trigger manual email inbox synchronization")
    public ResponseEntity<Map<String, String>> syncInbox() {
        mailboxService.triggerSync();
        return ResponseEntity.ok(Collections.singletonMap("message", "Mailbox synchronized successfully"));
    }

    @GetMapping("/invoices")
    @Operation(summary = "Get accommodation booking invoices")
    public ResponseEntity<List<InvoiceResponse>> getInvoices(
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) UUID bookingId
    ) {
        List<InvoiceResponse> invoices = mailboxService.getAllInvoices(status, bookingId);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/invoices/{id}")
    @Operation(summary = "Get single invoice by ID")
    public ResponseEntity<InvoiceResponse> getInvoiceById(@PathVariable UUID id) {
        InvoiceResponse invoice = mailboxService.getInvoiceById(id);
        return ResponseEntity.ok(invoice);
    }

    @PostMapping("/invoices")
    @Operation(summary = "Record or generate a booking invoice")
    public ResponseEntity<InvoiceResponse> createInvoice(@Valid @RequestBody InvoiceCreateRequest request) {
        InvoiceResponse response = mailboxService.createInvoice(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/invoices/{id}/status")
    @Operation(summary = "Update booking invoice status (PAID, PENDING, CANCELLED)")
    public ResponseEntity<InvoiceResponse> updateInvoiceStatus(
            @PathVariable UUID id,
            @Valid @RequestBody InvoiceStatusUpdateRequest request
    ) {
        InvoiceResponse response = mailboxService.updateInvoiceStatus(id, request.getStatus());
        return ResponseEntity.ok(response);
    }
}
