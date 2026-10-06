package com.saas.backend.serviceImpl;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.backend.Exception.DuplicateException;
import com.saas.backend.Exception.ResourceNotFoundException;
import com.saas.backend.dto.TagRequest;
import com.saas.backend.models.Tag;
import com.saas.backend.models.TagType;
import com.saas.backend.models.User;
import com.saas.backend.repositories.TagRepository;
import com.saas.backend.repositories.UserRepository;
import com.saas.backend.response.TagResponse;
import com.saas.backend.service.TagService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    private TagResponse mapToResponse(Tag tag) {
        if (tag == null) return null;
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .tagType(tag.getTagType())
                .description(tag.getDescription())
                .createdBy(tag.getCreatedBy() != null ? tag.getCreatedBy().getEmail() : null)
                .createdAt(tag.getCreatedAt())
                .updatedAt(tag.getUpdatedAt())
                .build();
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            if (auth.getPrincipal() instanceof User) {
                return (User) auth.getPrincipal();
            } else if (auth.getName() != null) {
                return userRepository.findByEmail(auth.getName()).orElse(null);
            }
        }
        return null;
    }

    @Override
    @Transactional
    public TagResponse createTag(TagRequest request) {
        if (tagRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateException("Tag with name '" + request.getName() + "' already exists");
        }

        Tag tag = Tag.builder()
                .name(request.getName().trim())
                .tagType(request.getTagType() != null ? request.getTagType() : TagType.PROPERTY)
                .description(request.getDescription())
                .createdBy(getCurrentUser())
                .build();

        Tag saved = tagRepository.save(tag);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> getAllTags(TagType type) {
        List<Tag> list;
        if (type != null) {
            list = tagRepository.findByTagType(type);
        } else {
            list = tagRepository.findAllByOrderByNameAsc();
        }
        return list.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TagResponse getTagById(UUID id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));
        return mapToResponse(tag);
    }

    @Override
    @Transactional
    public TagResponse updateTag(UUID id, TagRequest request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));

        if (tagRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
            throw new DuplicateException("Tag with name '" + request.getName() + "' already exists");
        }

        tag.setName(request.getName().trim());
        if (request.getTagType() != null) {
            tag.setTagType(request.getTagType());
        }
        tag.setDescription(request.getDescription());

        Tag updated = tagRepository.save(tag);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteTag(UUID id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));
        tagRepository.delete(tag);
    }
}
