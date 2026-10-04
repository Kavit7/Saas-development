package com.saas.backend.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailboxEmailResponse {
    private UUID id;
    private String messageId;
    private String fromEmail;
    private String toEmail;
    private String subject;
    private String body;
    private OffsetDateTime receivedAt;
    private boolean processed;
    private UUID bookingId;
    private String bookingReference;
    private String propertyName;
    private String safariReference;
    private String clientName;
}
