package com.saas.backend.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saas.backend.models.Amenity;
import com.saas.backend.models.Property;
import com.saas.backend.models.PropertyAmenity;

@Repository
public interface PropertyAmenityRepository extends JpaRepository<PropertyAmenity, UUID> {
    List<PropertyAmenity> findByProperty(Property property);
    List<PropertyAmenity> findByPropertyId(UUID propertyId);
    Optional<PropertyAmenity> findByPropertyAndAmenity(Property property, Amenity amenity);
    Optional<PropertyAmenity> findByPropertyIdAndAmenityId(UUID propertyId, UUID amenityId);
    boolean existsByPropertyAndAmenity(Property property, Amenity amenity);
    boolean existsByPropertyIdAndAmenityId(UUID propertyId, UUID amenityId);

    @Modifying
    @Query("DELETE FROM PropertyAmenity pa WHERE pa.property.id = :propertyId")
    void deleteByPropertyId(@Param("propertyId") UUID propertyId);

    @Modifying
    @Query("DELETE FROM PropertyAmenity pa WHERE pa.property.id = :propertyId AND pa.amenity.id = :amenityId")
    void deleteByPropertyIdAndAmenityId(@Param("propertyId") UUID propertyId, @Param("amenityId") UUID amenityId);
}
