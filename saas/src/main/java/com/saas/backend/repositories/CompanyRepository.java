package com.saas.backend.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.saas.backend.models.Company;


@Repository 
public interface CompanyRepository extends JpaRepository<Company,UUID> {

    Optional<Company> findByNameIgnoreCase(String name);

    
}