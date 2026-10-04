package com.saas.backend.serviceImpl;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.saas.backend.Exception.DuplicateException;
import com.saas.backend.Exception.ResourceNotFoundException;
import com.saas.backend.dto.PropertyRequest;
import com.saas.backend.models.PriceTier;
import com.saas.backend.models.Property;
import com.saas.backend.models.PropertyCategory;
import com.saas.backend.models.User;
import com.saas.backend.models.VerificationStatus;
import com.saas.backend.repositories.PriceTierRepository;
import com.saas.backend.repositories.PropertyCategoryRepository;
import com.saas.backend.repositories.PropertyRepository;
import com.saas.backend.repositories.UserRepository;
import com.saas.backend.response.PropertyResponse;
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

    private PropertyResponse mapToResponse(Property property) {
        if (property == null) return null;
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
                .build();
    }

    @Override
    public PropertyResponse createProperty(PropertyRequest request) {
        boolean exists = propertyRepository.existsByName(request.getName());
        if (exists) {
            throw new DuplicateException("The Property Already Exist");
        }

        PriceTier priceTier = priceTierRepository.findById(request.getPriceId())
                .orElseThrow(() -> new ResourceNotFoundException("The Price Tier does not exist"));

        PropertyCategory propertyCategory = propertyCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("No property Category found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User createdBy = (User) authentication.getPrincipal();
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

        propertyRepository.save(property);
        return mapToResponse(property);
    }

    @Override
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
    public PropertyResponse getPropertyById(UUID propId) {
        Property property = propertyRepository.findById(propId)
                .orElseThrow(() -> new ResourceNotFoundException("No Property found"));
        return mapToResponse(property);
    }

    @Override
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

        propertyRepository.save(property);
        return mapToResponse(property);
    }

    @Override
    public void PropertyVerification(UUID propId) {
        Property property = propertyRepository.findById(propId)
                .orElseThrow(() -> new ResourceNotFoundException("No Property found"));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User verifiedBy = null;
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof User) {
                verifiedBy = (User) authentication.getPrincipal();
            } else if (authentication.getName() != null) {
                verifiedBy = userRepository.findByEmail(authentication.getName()).orElse(null);
            }
        }
        OffsetDateTime verifiedAt = OffsetDateTime.now();
        property.setVerificationStatus(VerificationStatus.VERIFIED);   
        property.setVerifiedBy(verifiedBy);
        property.setVerifiedAt(verifiedAt);
        propertyRepository.save(property);
    }

    private String generateSlug(String name) {
        return name.toLowerCase().replace(" ", "-");
    }
}
