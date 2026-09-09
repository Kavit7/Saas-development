package com.saas.backend.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.saas.backend.models.Role;


@Repository 
public interface RoleRepository  extends JpaRepository<Role,UUID> {    
    Optional<Role> findByNameIgnoreCase(String name);
}
