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
public class SupportTicketUpdateRequest {
    private TicketStatus status;
    private Priority priority;
    private UUID assignedToId;
    private String resolutionNotes;
}
