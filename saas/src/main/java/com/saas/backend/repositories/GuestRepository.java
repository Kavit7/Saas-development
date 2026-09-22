package com.saas.backend.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.saas.backend.models.Guest;


@Repository 
public interface GuestRepository extends JpaRepository<Guest,UUID>,JpaSpecificationExecutor<Guest> {
    boolean existsByClient_IdAndFirstNameAndLastName(UUID id,String fname,String lastName);
    List<Guest> findAllByClientId(UUID id);
    Long countByClientId(UUID clientId);

}
