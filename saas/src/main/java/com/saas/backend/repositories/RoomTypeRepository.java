package com.saas.backend.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.saas.backend.models.RoomType;


@Repository 
public interface RoomTypeRepository extends JpaRepository<RoomType,UUID> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
    Optional<RoomType> findByNameIgnoreCase(String name);
}
