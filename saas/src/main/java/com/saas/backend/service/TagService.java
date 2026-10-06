package com.saas.backend.service;

import java.util.List;
import java.util.UUID;

import com.saas.backend.dto.TagRequest;
import com.saas.backend.models.TagType;
import com.saas.backend.response.TagResponse;

public interface TagService {
    TagResponse createTag(TagRequest request);
    List<TagResponse> getAllTags(TagType type);
    TagResponse getTagById(UUID id);
    TagResponse updateTag(UUID id, TagRequest request);
    void deleteTag(UUID id);
}
