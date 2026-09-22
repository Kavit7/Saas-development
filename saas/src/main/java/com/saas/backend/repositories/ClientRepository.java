package com.saas.backend.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.saas.backend.models.Client;




@Repository 
public interface ClientRepository extends JpaRepository<Client,UUID>,JpaSpecificationExecutor<Client> {
 boolean existsByCompanyIdAndEmail(UUID id,String email);
 List<Client> findAllBySalesPersonId(UUID userId);
 List<Client> findAllByCompany_Id(UUID companyId);

 Page<Client> findAll(Specification specification,Pageable pageable);
} 
