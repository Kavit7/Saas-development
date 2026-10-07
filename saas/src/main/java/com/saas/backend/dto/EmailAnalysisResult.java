package com.saas.backend.dto;

import com.saas.backend.models.EmailResponseType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result model representing the outcome of AI / heuristic analysis of an inbound email.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailAnalysisResult {

    /**
     * Classified intent: CONFIRMED, DECLINED, NEED_MORE_INFORMATION, or UNKNOWN.
     */
    private EmailResponseType responseType;

    /**
     * Property's confirmation number or voucher reference, if extracted from the email.
     */
    private String confirmationNumber;

    /**
     * Any system booking reference (e.g. ACC-BOOK-2026-XXXX) extracted from text.
     */
    private String bookingReference;

    /**
     * Concise explanation or summary of the lodge's response.
     */
    private String notes;

    /**
     * True if successfully processed by Gemini AI.
     */
    private boolean aiProcessed;

    /**
     * Error message if AI processing encountered an issue before fallback.
     */
    private String aiError;

    /**
     * True if human manual review is needed because automated matching was ambiguous.
     */
    private boolean needsManualReview;
}
