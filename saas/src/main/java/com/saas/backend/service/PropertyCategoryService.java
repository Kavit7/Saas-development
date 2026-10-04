package com.saas.backend.service;

import java.util.List;
import java.util.UUID;

import com.saas.backend.dto.PropertyCategoryRequest;
import com.saas.backend.response.PropertyCategoryResponse;

public interface PropertyCategoryService {
    PropertyCategoryResponse createCategory(PropertyCategoryRequest request);

    PropertyCategoryResponse editCategory(UUID categoryId, PropertyCategoryRequest request);

    List<PropertyCategoryResponse> viewAllCategory();

    void deletePropertyCategory(UUID propertyCat);
}
