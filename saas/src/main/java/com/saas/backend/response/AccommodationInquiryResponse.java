package com.saas.backend.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.saas.backend.models.Priority;
import com.saas.backend.models.TicketStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccommodationInquiryResponse {
    private UUID id;
    private UUID bookingId;
    private String bookingReference;
    private String propertyName;
    private UUID senderId;
    private String senderName;
    private String senderEmail;
    private UUID receiverId;
    private String receiverName;
    private String receiverEmail;
    private String subject;
    private String message;
    private Priority priority;
    private TicketStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
