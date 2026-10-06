package com.saas.backend.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.saas.backend.dto.TagRequest;
import com.saas.backend.models.TagType;
import com.saas.backend.response.TagResponse;
import com.saas.backend.service.TagService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping({"/tags", "/api/v1/tags"})
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Tags", description = "Manage system tags")
public class TagController {

    private final TagService tagService;

    @PostMapping
    @Operation(summary = "Create a tag")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','RESERVATION_MANAGER')")
    public ResponseEntity<TagResponse> createTag(@Valid @RequestBody TagRequest request) {
        TagResponse response = tagService.createTag(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all tags")
    public ResponseEntity<List<TagResponse>> getAllTags(
            @RequestParam(required = false) TagType type) {
        List<TagResponse> list = tagService.getAllTags(type);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tag by ID")
    public ResponseEntity<TagResponse> getTagById(@PathVariable UUID id) {
        TagResponse response = tagService.getTagById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a tag")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','RESERVATION_MANAGER')")
    public ResponseEntity<TagResponse> updateTag(
            @PathVariable UUID id,
            @Valid @RequestBody TagRequest request) {
        TagResponse response = tagService.updateTag(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a tag")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}
