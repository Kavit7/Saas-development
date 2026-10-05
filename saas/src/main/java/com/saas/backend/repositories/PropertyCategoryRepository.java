package com.saas.backend.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.saas.backend.models.PropertyCategory;


@Repository 
public interface PropertyCategoryRepository extends JpaRepository<PropertyCategory,UUID> {
    boolean existsByNameIgnoreCase(String name);
    java.util.Optional<PropertyCategory> findByNameIgnoreCase(String name);
}
