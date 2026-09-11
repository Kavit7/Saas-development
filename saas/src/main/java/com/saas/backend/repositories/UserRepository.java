package com.saas.backend.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saas.backend.models.User;
import java.util.UUID;

@Repository 
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
     @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.email = :email")
    Optional<User> findByEmailWithRole(@Param("email") String email);
  
}
