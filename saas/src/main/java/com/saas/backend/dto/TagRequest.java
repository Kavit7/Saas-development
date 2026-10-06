package com.saas.backend.dto;

import com.saas.backend.models.TagType;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagRequest {
    @NotBlank(message = "Tag name is required")
    private String name;
    private TagType tagType;
    private String description;
}
