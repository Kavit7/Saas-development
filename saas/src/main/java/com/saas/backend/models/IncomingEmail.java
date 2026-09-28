package com.saas.backend.models;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "incoming_emails")
@Getter
@Setter
@Builder
@AllArgsConstructor 
@NoArgsConstructor 
public class IncomingEmail extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String messageId;

    private String fromEmail;

    private String toEmail;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    private OffsetDateTime receivedAt;

    private boolean processed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private AccommodationBooking booking;
}