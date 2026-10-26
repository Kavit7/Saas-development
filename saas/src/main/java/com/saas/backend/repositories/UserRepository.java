package com.saas.backend.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saas.backend.models.Company;
import com.saas.backend.models.User;
import java.util.UUID;

@Repository 
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
     @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.email = :email")
    Optional<User> findByEmailWithRole(@Param("email") String email);

    User findUserById(UUID id);
    Page<User> findAll(Pageable pageable);

    Long countByCompany(Company company);
    Page<User> findByCompany(Company company, Pageable pageable);
    java.util.List<User> findByCompany(Company company);
    java.util.List<User> findByCompanyId(UUID companyId);

    @Query("SELECT u FROM User u WHERE u.company.id = :companyId AND (UPPER(u.role.name) LIKE '%RESERVATION%' OR UPPER(u.role.name) = 'RM')")
    java.util.List<User> findReservationManagersByCompanyId(@Param("companyId") UUID companyId);
}
