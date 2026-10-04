package com.saas.backend.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.saas.backend.models.Invoice;
import com.saas.backend.models.InvoiceStatus;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    Optional<Invoice> findByAccommodationBookingId(UUID accommodationBookingId);
    boolean existsByAccommodationBookingId(UUID accommodationBookingId);
    boolean existsByInvoiceNumber(String invoiceNumber);
    List<Invoice> findAllByOrderByIssuedAtDesc();
    List<Invoice> findByStatusOrderByIssuedAtDesc(InvoiceStatus status);
}
