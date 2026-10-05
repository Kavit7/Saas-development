package com.saas.backend.dto;

import java.util.UUID;

import com.saas.backend.models.Priority;
import com.saas.backend.models.SupportCategory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicketRequest {
    private String subject;
    private String description;
    private SupportCategory category;
    private Priority priority;
    private String relatedEntityType;
    private UUID relatedEntityId;
    private UUID assignedToId;
}
