package com.saas.backend.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.saas.backend.dto.InvoiceManualUpdateRequest;
import com.saas.backend.dto.InvoiceVerificationRequest;
import com.saas.backend.models.InvoiceStatus;
import com.saas.backend.response.InvoiceResponse;
import com.saas.backend.service.InvoiceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping({"/invoices", "/api/v1/invoices"})
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Invoices", description = "Accommodation supplier bills, banking coordinates, and payment verification")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    @Operation(summary = "Get list of invoices with optional filters")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESERVATION_MANAGER', 'SALES_PERSON', 'SUPER_ADMIN')")
    public ResponseEntity<List<InvoiceResponse>> getInvoices(
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) UUID bookingId,
            @RequestParam(required = false) Boolean needsReview,
            @RequestParam(required = false) Boolean verified
    ) {
        List<InvoiceResponse> list = invoiceService.getAllInvoices(status, bookingId, needsReview, verified);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get invoice by ID with parsed banking details")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESERVATION_MANAGER', 'SALES_PERSON', 'SUPER_ADMIN')")
    public ResponseEntity<InvoiceResponse> getInvoiceById(@PathVariable UUID id) {
        InvoiceResponse response = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get invoice for specific accommodation booking")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESERVATION_MANAGER', 'SALES_PERSON', 'SUPER_ADMIN')")
    public ResponseEntity<InvoiceResponse> getInvoiceByBookingId(@PathVariable UUID bookingId) {
        InvoiceResponse response = invoiceService.getInvoiceByBookingId(bookingId);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/booking/{bookingId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload lodge invoice (PDF/Excel) and auto-detect banking details with AI")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESERVATION_MANAGER', 'SUPER_ADMIN')")
    public ResponseEntity<InvoiceResponse> uploadAndDetectInvoice(
            @PathVariable UUID bookingId,
            @RequestParam("file") MultipartFile file
    ) {
        InvoiceResponse response = invoiceService.uploadAndDetectInvoice(bookingId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/manual-entry")
    @Operation(summary = "Manually enter or adjust banking coordinates and payment schedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESERVATION_MANAGER', 'SUPER_ADMIN')")
    public ResponseEntity<InvoiceResponse> updateInvoiceManually(
            @PathVariable UUID id,
            @Valid @RequestBody InvoiceManualUpdateRequest request
    ) {
        InvoiceResponse response = invoiceService.updateInvoiceManually(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/verify")
    @Operation(summary = "Authoritatively verify and approve invoice payment coordinates")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESERVATION_MANAGER', 'SUPER_ADMIN')")
    public ResponseEntity<InvoiceResponse> verifyInvoice(
            @PathVariable UUID id,
            @Valid @RequestBody InvoiceVerificationRequest request
    ) {
        InvoiceResponse response = invoiceService.verifyInvoice(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download original uploaded PDF or Excel invoice document")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESERVATION_MANAGER', 'SALES_PERSON', 'SUPER_ADMIN')")
    public ResponseEntity<byte[]> downloadInvoiceFile(@PathVariable UUID id) {
        byte[] data = invoiceService.getInvoiceFileBytes(id);
        String fileName = invoiceService.getInvoiceFileName(id);
        String fileType = invoiceService.getInvoiceFileType(id);

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(fileType);
        } catch (Exception ex) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(data);
    }
}
