package com.saas.backend.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.saas.backend.models.Amenity;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, UUID> {
    Optional<Amenity> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
    List<Amenity> findAllByOrderByNameAsc();
}
