package com.saas.backend.serviceImpl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.saas.backend.Exception.DuplicateException;
import com.saas.backend.Exception.ResourceNotFoundException;
import com.saas.backend.dto.PropertyCategoryRequest;
import com.saas.backend.models.PropertyCategory;
import com.saas.backend.repositories.PropertyCategoryRepository;
import com.saas.backend.response.PropertyCategoryResponse;
import com.saas.backend.service.PropertyCategoryService;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class PropertyCategoryServiceImpl implements PropertyCategoryService {
      
    private final PropertyCategoryRepository propertyCategoryRepository;

    private PropertyCategoryResponse mapToResponse(PropertyCategory cat) {
        if (cat == null) return null;
        return PropertyCategoryResponse.builder()
                .id(cat.getId())
                .name(cat.getName())
                .description(cat.getDescription())
                .createdAt(cat.getCreatedAt())
                .updatedAt(cat.getUpdatedAt())
                .build();
    }

    @Override
    public PropertyCategoryResponse createCategory(PropertyCategoryRequest request) {
        boolean exists = propertyCategoryRepository.existsByNameIgnoreCase(request.getName());
        if (exists) throw new DuplicateException("Category Already exist");

        PropertyCategory propertyCategory = new PropertyCategory();
        propertyCategory.setName(request.getName());
        propertyCategory.setDescription(request.getDescription());
        propertyCategoryRepository.save(propertyCategory);
        return mapToResponse(propertyCategory);
    }

    @Override
    public PropertyCategoryResponse editCategory(UUID categoryId, PropertyCategoryRequest request) {
        PropertyCategory propertyCategory = propertyCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("No property Category found"));
        if (request.getName() != null) propertyCategory.setName(request.getName());
        if (request.getDescription() != null) propertyCategory.setDescription(request.getDescription());
        propertyCategoryRepository.save(propertyCategory);

        return mapToResponse(propertyCategory);
    }

    @Override
    public List<PropertyCategoryResponse> viewAllCategory() {
        List<PropertyCategory> propertyCategories = propertyCategoryRepository.findAll();
        return propertyCategories.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public void deletePropertyCategory(UUID categoryId) {
        PropertyCategory propertyCategory = propertyCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("No property Category found"));
        propertyCategoryRepository.delete(propertyCategory);
    }
}
