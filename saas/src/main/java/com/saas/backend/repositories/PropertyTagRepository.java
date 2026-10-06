package com.saas.backend.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saas.backend.models.Property;
import com.saas.backend.models.PropertyTag;
import com.saas.backend.models.Tag;

@Repository
public interface PropertyTagRepository extends JpaRepository<PropertyTag, UUID> {
    List<PropertyTag> findByProperty(Property property);
    List<PropertyTag> findByPropertyId(UUID propertyId);
    Optional<PropertyTag> findByPropertyAndTag(Property property, Tag tag);
    Optional<PropertyTag> findByPropertyIdAndTagId(UUID propertyId, UUID tagId);
    boolean existsByPropertyAndTag(Property property, Tag tag);
    boolean existsByPropertyIdAndTagId(UUID propertyId, UUID tagId);

    @Modifying
    @Query("DELETE FROM PropertyTag pt WHERE pt.property.id = :propertyId")
    void deleteByPropertyId(@Param("propertyId") UUID propertyId);

    @Modifying
    @Query("DELETE FROM PropertyTag pt WHERE pt.property.id = :propertyId AND pt.tag.id = :tagId")
    void deleteByPropertyIdAndTagId(@Param("propertyId") UUID propertyId, @Param("tagId") UUID tagId);
}
