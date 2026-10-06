package com.saas.backend.serviceImpl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.backend.Exception.DuplicateException;
import com.saas.backend.Exception.ResourceNotFoundException;
import com.saas.backend.dto.AmenityRequest;
import com.saas.backend.models.Amenity;
import com.saas.backend.repositories.AmenityRepository;
import com.saas.backend.response.AmenityResponse;
import com.saas.backend.service.AmenityService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AmenityServiceImpl implements AmenityService {

    private final AmenityRepository amenityRepository;

    private AmenityResponse mapToResponse(Amenity amenity) {
        if (amenity == null) return null;
        return AmenityResponse.builder()
                .id(amenity.getId())
                .name(amenity.getName())
                .description(amenity.getDescription())
                .createdAt(amenity.getCreatedAt())
                .updatedAt(amenity.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public AmenityResponse createAmenity(AmenityRequest request) {
        if (amenityRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateException("Amenity with name '" + request.getName() + "' already exists");
        }

        Amenity amenity = Amenity.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .build();

        Amenity saved = amenityRepository.save(amenity);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AmenityResponse> getAllAmenities() {
        return amenityRepository.findAllByOrderByNameAsc().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AmenityResponse getAmenityById(UUID id) {
        Amenity amenity = amenityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Amenity not found with id: " + id));
        return mapToResponse(amenity);
    }

    @Override
    @Transactional
    public AmenityResponse updateAmenity(UUID id, AmenityRequest request) {
        Amenity amenity = amenityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Amenity not found with id: " + id));

        if (amenityRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
            throw new DuplicateException("Amenity with name '" + request.getName() + "' already exists");
        }

        amenity.setName(request.getName().trim());
        amenity.setDescription(request.getDescription());

        Amenity updated = amenityRepository.save(amenity);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteAmenity(UUID id) {
        Amenity amenity = amenityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Amenity not found with id: " + id));
        amenityRepository.delete(amenity);
    }
}
