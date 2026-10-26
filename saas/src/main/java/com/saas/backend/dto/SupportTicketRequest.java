package com.saas.backend.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.saas.backend.models.Priority;
import com.saas.backend.models.SupportCategory;
import com.saas.backend.models.TicketStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SupportTicketRequest {
    private UUID companyId;
    private String subject;
    private String description;
    private SupportCategory category;
    private Priority priority;
    private TicketStatus status;
    private String resolutionNotes;
    private String relatedEntityType;
    private UUID relatedEntityId;
    private UUID assignedToId;
}
