package com.saas.backend.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saas.backend.models.IncomingEmail;

public interface IncomingEmailRepository extends JpaRepository<IncomingEmail,UUID> {

    boolean existsByMessageId(String messageId);
} 
