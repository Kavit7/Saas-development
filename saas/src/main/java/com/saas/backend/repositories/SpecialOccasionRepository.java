package com.saas.backend.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saas.backend.models.SpecialOccasion;

public interface SpecialOccasionRepository extends JpaRepository<SpecialOccasion,UUID> {

        SpecialOccasion findByGuestId(UUID id);
        List<SpecialOccasion> findAllByGuestId(UUID id);
} 
