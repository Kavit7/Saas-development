package com.saas.backend.service;

import com.saas.backend.dto.InvoiceExtractionResult;

/**
 * Pluggable contract for extracting banking and payment information
 * from accommodation vendor invoice documents.
 * 
 * Allows seamless switching between Gemini AI, OCR, or other parsing strategies
 * without altering the core invoice or mailbox business logic.
 */
public interface InvoiceDocumentParser {

    /**
     * Parses an accommodation invoice document (PDF, Excel, etc.) and extracts
     * invoice number, total amount, currency, due date, banking coordinates,
     * and installment payment schedules.
     */
    InvoiceExtractionResult parseInvoice(byte[] fileBytes, String fileName, String mimeType);

    /**
     * Checks if this parser engine is properly configured and operational.
     */
    boolean isAvailable();

    /**
     * Identifier for the active parsing engine (e.g. "GEMINI_AI").
     */
    String getParserName();
}
