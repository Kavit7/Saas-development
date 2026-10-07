package com.saas.backend.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_booking_id", nullable = false, unique = true)
    private AccommodationBooking accommodationBooking;

    @Column(nullable = false, unique = true)
    private String invoiceNumber;

    private BigDecimal amount;

    private String currency;

    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    private String fileName;

    private String filePath;

    private String fileType;

    private OffsetDateTime issuedAt;

    /**
     * Flexible JSONB storage for property banking coordinates,
     * installment / milestone schedules, and raw extraction metadata.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payment_details", columnDefinition = "jsonb")
    private String paymentDetails;

    /**
     * Whether banking details and totals have been manually or authoritatively verified.
     */
    @Builder.Default
    @Column(name = "verified", nullable = false, columnDefinition = "boolean default false")
    private Boolean verified = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_id")
    private User verifiedBy;

    @Column(name = "verified_at")
    private OffsetDateTime verifiedAt;

    @Column(name = "verification_notes", length = 1000)
    private String verificationNotes;

    /**
     * Flags whether this invoice data was automatically detected by AI (e.g. Gemini).
     */
    @Builder.Default
    @Column(name = "ai_extracted")
    private Boolean aiExtracted = false;

    /**
     * If AI detection failed, timed out, or had low confidence, this flag prompts
     * the user to manually review and complete banking details without data loss.
     */
    @Builder.Default
    @Column(name = "needs_manual_review")
    private Boolean needsManualReview = false;

    @Column(name = "extraction_error", length = 1000)
    private String extractionError;

    @Version
    private Integer version;
}
