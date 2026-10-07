package com.saas.backend.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saas.backend.models.IncomingEmail;

public interface IncomingEmailRepository extends JpaRepository<IncomingEmail,UUID> {

    boolean existsByMessageId(String messageId);

    java.util.Optional<IncomingEmail> findByMessageId(String messageId);

    java.util.List<IncomingEmail> findByProcessedFalse();

    java.util.List<IncomingEmail> findAllByOrderByReceivedAtDesc();

    java.util.List<IncomingEmail> findByBookingIdOrderByReceivedAtDesc(UUID bookingId);

    @org.springframework.data.jpa.repository.Query("SELECT e FROM IncomingEmail e " +
           "WHERE e.booking.accommodationRequirement.safari.client.company.id = :companyId " +
           "ORDER BY e.receivedAt DESC")
    java.util.List<IncomingEmail> findByCompanyIdOrderByReceivedAtDesc(@org.springframework.data.repository.query.Param("companyId") UUID companyId);

    @org.springframework.data.jpa.repository.Query("SELECT e FROM IncomingEmail e " +
           "WHERE e.booking.id = :bookingId AND e.booking.accommodationRequirement.safari.client.company.id = :companyId " +
           "ORDER BY e.receivedAt DESC")
    java.util.List<IncomingEmail> findByBookingIdAndCompanyIdOrderByReceivedAtDesc(
           @org.springframework.data.repository.query.Param("bookingId") UUID bookingId,
           @org.springframework.data.repository.query.Param("companyId") UUID companyId);
}
