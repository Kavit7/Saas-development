package com.saas.backend.response;

import java.time.OffsetDateTime;
import java.util.UUID;

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
public class SupportTicketResponse {
    private UUID id;
    private UUID companyId;
    private String companyName;
    private UUID raisedById;
    private String raisedByName;
    private String raisedByEmail;
    private UUID assignedToId;
    private String assignedToName;
    private String assignedToEmail;
    private SupportCategory category;
    private String subject;
    private String description;
    private String relatedEntityType;
    private UUID relatedEntityId;
    private Priority priority;
    private TicketStatus status;
    private String resolutionNotes;
    private Integer version;
    private OffsetDateTime resolvedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
