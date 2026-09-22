package com.saas.backend.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.saas.backend.models.GuestRequirement;
import com.saas.backend.models.RequirementType;


@Repository 
public interface GuestRequirementRepository extends JpaRepository<GuestRequirement,UUID> {
    boolean existsByGuestIdAndRequirementType(UUID id,RequirementType requirementType);

    List<GuestRequirement> findAllByGuestId(UUID guestId);
}
