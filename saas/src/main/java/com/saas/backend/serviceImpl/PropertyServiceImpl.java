package com.saas.backend.serviceImpl;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.backend.Exception.DuplicateException;
import com.saas.backend.Exception.ResourceNotFoundException;
import com.saas.backend.dto.PropertyRequest;
import com.saas.backend.models.Amenity;
import com.saas.backend.models.PriceTier;
import com.saas.backend.models.Property;
import com.saas.backend.models.PropertyAmenity;
import com.saas.backend.models.PropertyCategory;
import com.saas.backend.models.PropertyTag;
import com.saas.backend.models.Tag;
import com.saas.backend.models.User;
import com.saas.backend.models.VerificationStatus;
import com.saas.backend.repositories.AmenityRepository;
import com.saas.backend.repositories.PriceTierRepository;
import com.saas.backend.repositories.PropertyAmenityRepository;
import com.saas.backend.repositories.PropertyCategoryRepository;
import com.saas.backend.repositories.PropertyRepository;
import com.saas.backend.repositories.PropertyTagRepository;
import com.saas.backend.repositories.TagRepository;
import com.saas.backend.repositories.UserRepository;
import com.saas.backend.response.AmenityResponse;
import com.saas.backend.response.PropertyResponse;
import com.saas.backend.response.TagResponse;
import com.saas.backend.service.PropertyService;
import com.saas.backend.specification.PropertySpecification;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class PropertyServiceImpl implements PropertyService {
    private final PropertyRepository propertyRepository;
    private final PropertyCategoryRepository propertyCategoryRepository;
    private final PriceTierRepository priceTierRepository;
    private final UserRepository userRepository;
    private final AmenityRepository amenityRepository;
    private final PropertyAmenityRepository propertyAmenityRepository;
    private final TagRepository tagRepository;
    private final PropertyTagRepository propertyTagRepository;

    private AmenityResponse mapAmenityToResponse(Amenity amenity) {
        if (amenity == null) return null;
        return AmenityResponse.builder()
                .id(amenity.getId())
                .name(amenity.getName())
                .description(amenity.getDescription())
                .createdAt(amenity.getCreatedAt())
                .updatedAt(amenity.getUpdatedAt())
                .build();
    }

    private TagResponse mapTagToResponse(Tag tag) {
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof User) {
                return (User) authentication.getPrincipal();
            } else if (authentication.getName() != null) {
                return userRepository.findByEmail(authentication.getName()).orElse(null);
            }
        }
        return null;
    }

    private PropertyResponse mapToResponse(Property property) {
        if (property == null) return null;

        List<AmenityResponse> amenityResponses = Collections.emptyList();
        try {
            List<PropertyAmenity> propertyAmenities = propertyAmenityRepository.findByProperty(property);
            if (propertyAmenities != null) {
                amenityResponses = propertyAmenities.stream()
                        .map(PropertyAmenity::getAmenity)
                        .filter(a -> a != null)
                        .map(this::mapAmenityToResponse)
                        .toList();
            }
        } catch (Exception ignored) {}

        List<TagResponse> tagResponses = Collections.emptyList();
        try {
            List<PropertyTag> propertyTags = propertyTagRepository.findByProperty(property);
            if (propertyTags != null) {
                tagResponses = propertyTags.stream()
                        .map(PropertyTag::getTag)
                        .filter(t -> t != null)
                        .map(this::mapTagToResponse)
                        .toList();
            }
        } catch (Exception ignored) {}

        return PropertyResponse.builder()
                .id(property.getId())
                .name(property.getName())
                .slug(property.getSlug())
                .location(property.getLocation())
                .region(property.getRegion())
                .country(property.getCountry())
                .categoryId(property.getCategory() != null ? property.getCategory().getId() : null)
                .categoryName(property.getCategory() != null ? property.getCategory().getName() : null)
                .priceTierId(property.getPriceTier() != null ? property.getPriceTier().getId() : null)
                .priceTierName(property.getPriceTier() != null ? property.getPriceTier().getName() : null)
                .description(property.getDescription())
                .contactName(property.getContactName())
                .contactEmail(property.getContactEmail())
                .contactPhone(property.getContactPhone())
                .website(property.getWebsite())
                .verificationStatus(property.getVerificationStatus())
                .createdBy(property.getCreatedBy() != null ? property.getCreatedBy().getEmail() : null)
                .verifiedAt(property.getVerifiedAt())
                .verifiedBy(property.getVerifiedBy() != null ? property.getVerifiedBy().getEmail() : null)
                .createdAt(property.getCreatedAt())
                .updatedAt(property.getUpdatedAt())
                .amenities(amenityResponses)
                .tags(tagResponses)
                .build();
    }

    @Override
    @Transactional
    public PropertyResponse createProperty(PropertyRequest request) {
        boolean exists = propertyRepository.existsByName(request.getName());
        if (exists) {
            throw new DuplicateException("The Property Already Exist");
        }

        PriceTier priceTier = priceTierRepository.findById(request.getPriceId())
                .orElseThrow(() -> new ResourceNotFoundException("The Price Tier does not exist"));

        PropertyCategory propertyCategory = propertyCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("No property Category found"));

        User createdBy = getCurrentUser();
        String slug = generateSlug(request.getName());

        Property property = new Property();
        property.setName(request.getName());
        property.setSlug(slug);
        property.setCategory(propertyCategory);
        property.setPriceTier(priceTier);
        property.setContactEmail(request.getContactEmail());
        property.setContactName(request.getContactName());
        property.setContactPhone(request.getContactPhone());
        property.setCountry(request.getCountry());
        property.setDescription(request.getDescription());
        property.setLocation(request.getLocation());
        property.setRegion(request.getRegion());
        property.setWebsite(request.getWebsite());
        property.setVerificationStatus(VerificationStatus.PENDING);
        property.setCreatedBy(createdBy);

        Property savedProperty = propertyRepository.save(property);

        // Associate Amenities if provided
        if (request.getAmenityIds() != null && !request.getAmenityIds().isEmpty()) {
            for (UUID amenityId : request.getAmenityIds()) {
                amenityRepository.findById(amenityId).ifPresent(amenity -> {
                    PropertyAmenity pa = PropertyAmenity.builder()
                            .property(savedProperty)
                            .amenity(amenity)
                            .build();
                    propertyAmenityRepository.save(pa);
                });
            }
        }

        // Associate Tags if provided
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            for (UUID tagId : request.getTagIds()) {
                tagRepository.findById(tagId).ifPresent(tag -> {
                    PropertyTag pt = PropertyTag.builder()
                            .property(savedProperty)
                            .tag(tag)
                            .createdBy(createdBy)
                            .build();
                    propertyTagRepository.save(pt);
                });
            }
        }

        return mapToResponse(savedProperty);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PropertyResponse> getAllProperty(int page, int size, String sortBy, String direction, String search, VerificationStatus status) {
        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
         
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Property> specification = (root, query, cb) -> cb.conjunction();
        
        if (status != null) {
            specification = specification.and(PropertySpecification.hasStatus(status));
        }
        
        if (search != null && !search.isBlank()) {
            specification = specification.and(PropertySpecification.hasSearch(search));
        }
        return propertyRepository.findAll(specification, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PropertyResponse getPropertyById(UUID propId) {
        Property property = propertyRepository.findById(propId)
                .orElseThrow(() -> new ResourceNotFoundException("No Property found"));
        return mapToResponse(property);
    }

    @Override
    @Transactional
    public PropertyResponse UpdateProperty(UUID propId, PropertyRequest request) {
        Property property = propertyRepository.findById(propId)
                .orElseThrow(() -> new ResourceNotFoundException("No Property found"));

        boolean exists = propertyRepository.existsByNameAndIdNot(request.getName(), propId);
        if (exists) {
            throw new DuplicateException("Property name already exists");
        }

        PriceTier priceTier = priceTierRepository.findById(request.getPriceId())
                .orElseThrow(() -> new ResourceNotFoundException("The Price Tier does not exist"));

        PropertyCategory propertyCategory = propertyCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("No Property Category found"));

        property.setName(request.getName());
        property.setSlug(generateSlug(request.getName()));
        property.setCategory(propertyCategory);
        property.setPriceTier(priceTier);
        property.setContactEmail(request.getContactEmail());
        property.setContactName(request.getContactName());
        property.setContactPhone(request.getContactPhone());
        property.setCountry(request.getCountry());
        property.setDescription(request.getDescription());
        property.setLocation(request.getLocation());
        property.setRegion(request.getRegion());
        property.setWebsite(request.getWebsite());

        Property savedProperty = propertyRepository.save(property);

        // Sync Amenities if provided
        if (request.getAmenityIds() != null) {
            propertyAmenityRepository.deleteByPropertyId(propId);
            for (UUID amenityId : request.getAmenityIds()) {
                amenityRepository.findById(amenityId).ifPresent(amenity -> {
                    PropertyAmenity pa = PropertyAmenity.builder()
                            .property(savedProperty)
                            .amenity(amenity)
                            .build();
                    propertyAmenityRepository.save(pa);
                });
            }
        }

        // Sync Tags if provided
        if (request.getTagIds() != null) {
            propertyTagRepository.deleteByPropertyId(propId);
            User currentUser = getCurrentUser();
            for (UUID tagId : request.getTagIds()) {
                tagRepository.findById(tagId).ifPresent(tag -> {
                    PropertyTag pt = PropertyTag.builder()
                            .property(savedProperty)
                            .tag(tag)
                            .createdBy(currentUser)
                            .build();
                    propertyTagRepository.save(pt);
                });
            }
        }

        return mapToResponse(savedProperty);
    }

    @Override
    @Transactional
    public void PropertyVerification(UUID propId) {
        Property property = propertyRepository.findById(propId)
                .orElseThrow(() -> new ResourceNotFoundException("No Property found"));
        User verifiedBy = getCurrentUser();
        OffsetDateTime verifiedAt = OffsetDateTime.now();
        property.setVerificationStatus(VerificationStatus.VERIFIED);   
        property.setVerifiedBy(verifiedBy);
        property.setVerifiedAt(verifiedAt);
        propertyRepository.save(property);
    }

    @Override
    @Transactional
    public PropertyResponse addAmenitiesToProperty(UUID propId, List<UUID> amenityIds) {
        Property property = propertyRepository.findById(propId)
                .orElseThrow(() -> new ResourceNotFoundException("No Property found"));

        if (amenityIds != null) {
            for (UUID amenityId : amenityIds) {
                if (!propertyAmenityRepository.existsByPropertyIdAndAmenityId(propId, amenityId)) {
                    amenityRepository.findById(amenityId).ifPresent(amenity -> {
                        PropertyAmenity pa = PropertyAmenity.builder()
                                .property(property)
                                .amenity(amenity)
                                .build();
                        propertyAmenityRepository.save(pa);
                    });
                }
            }
        }
        return mapToResponse(property);
    }

    @Override
    @Transactional
    public PropertyResponse removeAmenityFromProperty(UUID propId, UUID amenityId) {
        Property property = propertyRepository.findById(propId)
                .orElseThrow(() -> new ResourceNotFoundException("No Property found"));
        propertyAmenityRepository.deleteByPropertyIdAndAmenityId(propId, amenityId);
        return mapToResponse(property);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AmenityResponse> getPropertyAmenities(UUID propId) {
        Property property = propertyRepository.findById(propId)
                .orElseThrow(() -> new ResourceNotFoundException("No Property found"));
        return propertyAmenityRepository.findByProperty(property).stream()
                .map(PropertyAmenity::getAmenity)
                .filter(a -> a != null)
                .map(this::mapAmenityToResponse)
                .toList();
    }

    @Override
    @Transactional
    public PropertyResponse addTagsToProperty(UUID propId, List<UUID> tagIds) {
        Property property = propertyRepository.findById(propId)
                .orElseThrow(() -> new ResourceNotFoundException("No Property found"));

        User currentUser = getCurrentUser();
        if (tagIds != null) {
            for (UUID tagId : tagIds) {
                if (!propertyTagRepository.existsByPropertyIdAndTagId(propId, tagId)) {
                    tagRepository.findById(tagId).ifPresent(tag -> {
                        PropertyTag pt = PropertyTag.builder()
                                .property(property)
                                .tag(tag)
                                .createdBy(currentUser)
                                .build();
                        propertyTagRepository.save(pt);
                    });
                }
            }
        }
        return mapToResponse(property);
    }

    @Override
    @Transactional
    public PropertyResponse removeTagFromProperty(UUID propId, UUID tagId) {
        Property property = propertyRepository.findById(propId)
                .orElseThrow(() -> new ResourceNotFoundException("No Property found"));
        propertyTagRepository.deleteByPropertyIdAndTagId(propId, tagId);
        return mapToResponse(property);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> getPropertyTags(UUID propId) {
        Property property = propertyRepository.findById(propId)
                .orElseThrow(() -> new ResourceNotFoundException("No Property found"));
        return propertyTagRepository.findByProperty(property).stream()
                .map(PropertyTag::getTag)
                .filter(t -> t != null)
                .map(this::mapTagToResponse)
                .toList();
    }

    private String generateSlug(String name) {
        return name.toLowerCase().replace(" ", "-");
    }
}
