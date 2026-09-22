package com.saas.backend.repositories;



import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.saas.backend.models.Safari;

@Repository 
public interface SafariRepository extends  JpaRepository<Safari,UUID>{
    
}
