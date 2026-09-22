package com.saas.backend.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.saas.backend.models.PlatformAdmin;

@Repository 
public interface PlatformAdminRepository extends JpaRepository<PlatformAdmin,UUID> {
Optional<PlatformAdmin> findByEmail(String email);
    
} 
