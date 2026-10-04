package com.saas.backend.controllers;

import java.util.List;
import java.util.Map;
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
import org.springframework.web.bind.annotation.RestController;

import com.saas.backend.dto.PropertyCategoryRequest;
import com.saas.backend.response.PropertyCategoryResponse;
import com.saas.backend.service.PropertyCategoryService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/property-category")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PropertyCategoryController {

    private final PropertyCategoryService propertyCategoryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RESERVATION_MANAGER')")
    public ResponseEntity<PropertyCategoryResponse> createCategory(
            @RequestBody PropertyCategoryRequest request) {

        PropertyCategoryResponse propertyCategory =
                propertyCategoryService.createCategory(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(propertyCategory);
    }
     
    @PutMapping("/{categoryId}")
    @PreAuthorize("hasAnyRole('ADMIN','RESERVATION_MANAGER')")
    public ResponseEntity<PropertyCategoryResponse> editCategory(
            @PathVariable UUID categoryId,
            @RequestBody PropertyCategoryRequest request) {

        PropertyCategoryResponse propertyCategory =
                propertyCategoryService.editCategory(categoryId, request);

        return ResponseEntity.ok(propertyCategory);
    }

    @GetMapping
    public ResponseEntity<List<PropertyCategoryResponse>> viewAllCategory() {
        List<PropertyCategoryResponse> propertyCategories =
                propertyCategoryService.viewAllCategory();

        return ResponseEntity.ok(propertyCategories);
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasAnyRole('ADMIN','RESERVATION_MANAGER')")
    public ResponseEntity<?> deletePropertyCategory(
            @PathVariable UUID categoryId) {

        propertyCategoryService.deletePropertyCategory(categoryId);

        return ResponseEntity.ok(Map.of("message","Category Deleted successfully"));
    }
}