package com.saas.backend.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.saas.backend.models.TagType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagResponse {
    private UUID id;
    private String name;
    private TagType tagType;
    private String description;
    private String createdBy;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
