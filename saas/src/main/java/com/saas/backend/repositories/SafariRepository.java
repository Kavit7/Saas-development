package com.saas.backend.repositories;



import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.saas.backend.models.Safari;

@Repository 
public interface SafariRepository extends JpaRepository<Safari,UUID>, JpaSpecificationExecutor<Safari>{
    Page<Safari> findAll(Specification<Safari> specification, Pageable pageable);
    java.util.List<Safari> findAllByClient_Company_Id(UUID companyId);
    long countByClient_Company_Id(UUID companyId);
    java.util.List<Safari> findAllBySalesPerson_Id(UUID salesPersonId);
    boolean existsByReferenceNumber(String referenceNumber);
}
