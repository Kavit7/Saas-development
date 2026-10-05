package com.saas.backend.repositories;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.saas.backend.models.Property;



@Repository 
public interface PropertyRepository extends JpaRepository<Property,UUID> ,JpaSpecificationExecutor<Property> {
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, UUID id);
    long countByVerificationStatus(com.saas.backend.models.VerificationStatus verificationStatus);
    Page<Property> findAll(Specification<Property> specification, Pageable pageable);
}
