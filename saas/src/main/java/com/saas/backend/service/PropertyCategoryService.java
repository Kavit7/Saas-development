package com.saas.backend.service;

import java.util.List;
import java.util.UUID;

import com.saas.backend.dto.PropertyCategoryRequest;
import com.saas.backend.models.PropertyCategory;

public interface PropertyCategoryService {
    PropertyCategory createCategory(PropertyCategoryRequest request);

    PropertyCategory editCategory(UUID categoryId,PropertyCategoryRequest request);

    List<PropertyCategory> viewAllCategory();

    void deletePropertyCategory(UUID propertyCat);
    
}
