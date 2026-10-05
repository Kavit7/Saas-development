package com.saas.backend.dto;

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
public class AccommodationInquiryRequest {
    private UUID accommodationBookingId;
    private UUID receiverId;
    private String subject;
    private String message;
    private Priority priority;
    private TicketStatus status;
}
