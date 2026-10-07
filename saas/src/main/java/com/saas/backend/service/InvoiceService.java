package com.saas.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.saas.backend.dto.InvoiceManualUpdateRequest;
import com.saas.backend.dto.InvoiceVerificationRequest;
import com.saas.backend.models.AccommodationBooking;
import com.saas.backend.models.InvoiceStatus;
import com.saas.backend.response.InvoiceResponse;

/**
 * Service managing accommodation supplier vendor invoices (Accounts Payable).
 * Coordinates AI document extraction, attachment storage, manual fallbacks,
 * and authoritative financial verification.
 */
public interface InvoiceService {

    /**
     * Automatically processes an invoice attachment received via inbound property email.
     */
    InvoiceResponse processInboundInvoiceAttachment(
            AccommodationBooking booking,
            byte[] fileBytes,
            String fileName,
            String contentType
    );

    /**
     * Processes a manually uploaded PDF/Excel invoice file for an accommodation booking.
     */
    InvoiceResponse uploadAndDetectInvoice(
            UUID bookingId,
            MultipartFile file
    );

    /**
     * Retrieves the invoice associated with a booking.
     */
    InvoiceResponse getInvoiceByBookingId(UUID bookingId);

    /**
     * Retrieves an invoice by primary ID.
     */
    InvoiceResponse getInvoiceById(UUID id);

    /**
     * Lists invoices with optional status, booking, review, and verification filters.
     */
    List<InvoiceResponse> getAllInvoices(
            InvoiceStatus status,
            UUID bookingId,
            Boolean needsReview,
            Boolean verified
    );

    /**
     * Updates invoice banking coordinates and payment schedules manually
     * (used when AI detection encounters an unreadable document or user makes adjustments).
     */
    InvoiceResponse updateInvoiceManually(
            UUID id,
            InvoiceManualUpdateRequest request
    );

    /**
     * Authoritative financial approval of invoice banking details by finance or management.
     */
    InvoiceResponse verifyInvoice(
            UUID id,
            InvoiceVerificationRequest request
    );

    /**
     * Retrieves the file bytes and metadata for downloading the original invoice document.
     */
    byte[] getInvoiceFileBytes(UUID id);

    String getInvoiceFileName(UUID id);

    String getInvoiceFileType(UUID id);
}
