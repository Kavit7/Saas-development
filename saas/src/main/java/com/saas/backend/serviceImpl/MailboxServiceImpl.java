package com.saas.backend.serviceImpl;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.backend.AccessHelper.CurrentUserChecker;
import com.saas.backend.dto.InvoiceCreateRequest;
import com.saas.backend.Exception.ResourceNotFoundException;
import com.saas.backend.models.AccommodationBooking;
import com.saas.backend.models.IncomingEmail;
import com.saas.backend.models.Invoice;
import com.saas.backend.models.InvoiceStatus;
import com.saas.backend.models.Safari;
import com.saas.backend.models.User;
import com.saas.backend.repositories.AccommodationBookingRepository;
import com.saas.backend.repositories.IncomingEmailRepository;
import com.saas.backend.repositories.InvoiceRepository;
import com.saas.backend.response.InvoiceResponse;
import com.saas.backend.response.MailboxEmailResponse;
import com.saas.backend.service.IncomingEmailService;
import com.saas.backend.service.MailboxService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailboxServiceImpl implements MailboxService {

    private final IncomingEmailRepository incomingEmailRepository;
    private final InvoiceRepository invoiceRepository;
    private final AccommodationBookingRepository bookingRepository;
    private final IncomingEmailService incomingEmailService;
    private final CurrentUserChecker currentUserChecker;

    @Override
    @Transactional(readOnly = true)
    public List<MailboxEmailResponse> getAllEmails(UUID bookingId) {
        User currentUser = currentUserChecker.checkCurrentUser();
        boolean isSuperAdmin = currentUser != null && currentUser.getRole() != null &&
                "SUPER_ADMIN".equalsIgnoreCase(currentUser.getRole().getName().replace("ROLE_", "").trim());

        List<IncomingEmail> emails;
        if (!isSuperAdmin && currentUser != null && currentUser.getCompany() != null) {
            UUID companyId = currentUser.getCompany().getId();
            if (bookingId != null) {
                emails = incomingEmailRepository.findByBookingIdAndCompanyIdOrderByReceivedAtDesc(bookingId, companyId);
            } else {
                emails = incomingEmailRepository.findByCompanyIdOrderByReceivedAtDesc(companyId);
            }
        } else {
            if (bookingId != null) {
                emails = incomingEmailRepository.findByBookingIdOrderByReceivedAtDesc(bookingId);
            } else {
                emails = incomingEmailRepository.findAllByOrderByReceivedAtDesc();
            }
        }
        return emails.stream().map(this::mapToEmailResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MailboxEmailResponse getEmailById(UUID id) {
        IncomingEmail email = incomingEmailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Email message not found with ID: " + id));
        return mapToEmailResponse(email);
    }

    @Override
    public void triggerSync() {
        log.info("Triggering manual mailbox check & email synchronization...");
        try {
            incomingEmailService.processNewEmails();
        } catch (Exception e) {
            log.error("Mailbox sync encountered an error: {}", e.getMessage());
            throw new RuntimeException("Mailbox sync error: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAllInvoices(InvoiceStatus status, UUID bookingId) {
        User currentUser = currentUserChecker.checkCurrentUser();
        boolean isSuperAdmin = currentUser != null && currentUser.getRole() != null &&
                "SUPER_ADMIN".equalsIgnoreCase(currentUser.getRole().getName().replace("ROLE_", "").trim());

        List<Invoice> invoices;
        if (!isSuperAdmin && currentUser != null && currentUser.getCompany() != null) {
            UUID companyId = currentUser.getCompany().getId();
            if (bookingId != null) {
                invoices = invoiceRepository.findByAccommodationBookingId(bookingId)
                        .filter(inv -> matchesCompany(inv, companyId))
                        .map(List::of)
                        .orElseGet(List::of);
            } else if (status != null) {
                invoices = invoiceRepository.findByCompanyIdAndStatusOrderByIssuedAtDesc(companyId, status);
            } else {
                invoices = invoiceRepository.findByCompanyIdOrderByIssuedAtDesc(companyId);
            }
        } else {
            if (bookingId != null) {
                invoices = invoiceRepository.findByAccommodationBookingId(bookingId)
                        .map(List::of)
                        .orElseGet(List::of);
            } else if (status != null) {
                invoices = invoiceRepository.findByStatusOrderByIssuedAtDesc(status);
            } else {
                invoices = invoiceRepository.findAllByOrderByIssuedAtDesc();
            }
        }
        return invoices.stream().map(this::mapToInvoiceResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(UUID id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + id));
        return mapToInvoiceResponse(invoice);
    }

    @Override
    @Transactional
    public InvoiceResponse createInvoice(InvoiceCreateRequest request) {
        AccommodationBooking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + request.getBookingId()));

        User currentUser = currentUserChecker.checkCurrentUser();
        boolean isSuperAdmin = currentUser != null && currentUser.getRole() != null &&
                "SUPER_ADMIN".equalsIgnoreCase(currentUser.getRole().getName().replace("ROLE_", "").trim());

        if (!isSuperAdmin && currentUser != null && currentUser.getCompany() != null) {
            if (!matchesCompanyBooking(booking, currentUser.getCompany().getId())) {
                throw new SecurityException("You do not have permission to create an invoice for a booking outside your company.");
            }
        }

        if (invoiceRepository.existsByAccommodationBookingId(booking.getId())) {
            throw new IllegalArgumentException("An invoice already exists for this booking");
        }

        String invNumber = request.getInvoiceNumber();
        if (invNumber == null || invNumber.isBlank()) {
            invNumber = "INV-" + booking.getReferenceNumber().replace("ACC-BOOK-", "");
        }

        Invoice invoice = Invoice.builder()
                .accommodationBooking(booking)
                .invoiceNumber(invNumber)
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .dueDate(request.getDueDate() != null ? request.getDueDate() : (booking.getCheckIn() != null ? booking.getCheckIn() : LocalDate.now().plusDays(14)))
                .status(request.getStatus() != null ? request.getStatus() : InvoiceStatus.PENDING)
                .fileName(request.getFileName() != null ? request.getFileName() : (invNumber + ".pdf"))
                .filePath(request.getFilePath() != null ? request.getFilePath() : ("/invoices/" + invNumber + ".pdf"))
                .issuedAt(OffsetDateTime.now())
                .build();

        invoice = invoiceRepository.save(invoice);
        log.info("Recorded invoice {} for booking {}", invoice.getInvoiceNumber(), booking.getReferenceNumber());
        return mapToInvoiceResponse(invoice);
    }

    private boolean matchesCompany(Invoice inv, UUID companyId) {
        if (inv == null || inv.getAccommodationBooking() == null) return false;
        return matchesCompanyBooking(inv.getAccommodationBooking(), companyId);
    }

    private boolean matchesCompanyBooking(AccommodationBooking b, UUID companyId) {
        if (b == null || b.getAccommodationRequirement() == null || b.getAccommodationRequirement().getSafari() == null) return false;
        Safari s = b.getAccommodationRequirement().getSafari();
        if (s.getClient() == null || s.getClient().getCompany() == null) return false;
        return companyId.equals(s.getClient().getCompany().getId());
    }

    @Override
    @Transactional
    public InvoiceResponse updateInvoiceStatus(UUID id, InvoiceStatus status) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));

        invoice.setStatus(status);
        invoice = invoiceRepository.save(invoice);
        log.info("Updated invoice {} status to {}", invoice.getInvoiceNumber(), status);
        return mapToInvoiceResponse(invoice);
    }

    private MailboxEmailResponse mapToEmailResponse(IncomingEmail email) {
        UUID bookingId = null;
        String bookingRef = null;
        String bookingStatus = null;
        String confirmationNumber = null;
        String propName = null;
        String safariRef = null;
        String clientName = null;

        if (email.getBooking() != null) {
            AccommodationBooking b = email.getBooking();
            bookingId = b.getId();
            bookingRef = b.getReferenceNumber();
            bookingStatus = b.getStatus() != null ? b.getStatus().name() : null;
            confirmationNumber = b.getConfirmationNumber();
            if (b.getProperty() != null) {
                propName = b.getProperty().getName();
            }
            if (b.getAccommodationRequirement() != null && b.getAccommodationRequirement().getSafari() != null) {
                safariRef = b.getAccommodationRequirement().getSafari().getReferenceNumber();
                if (b.getAccommodationRequirement().getSafari().getClient() != null) {
                    clientName = b.getAccommodationRequirement().getSafari().getClient().getFirstName() + " "
                            + b.getAccommodationRequirement().getSafari().getClient().getLastName();
                }
            }
        }

        return MailboxEmailResponse.builder()
                .id(email.getId())
                .messageId(email.getMessageId())
                .fromEmail(email.getFromEmail())
                .toEmail(email.getToEmail())
                .subject(email.getSubject())
                .body(email.getBody())
                .receivedAt(email.getReceivedAt())
                .processed(email.isProcessed())
                .bookingId(bookingId)
                .bookingReference(bookingRef)
                .bookingStatus(bookingStatus)
                .confirmationNumber(confirmationNumber)
                .propertyName(propName)
                .safariReference(safariRef)
                .clientName(clientName)
                .build();
    }

    private InvoiceResponse mapToInvoiceResponse(Invoice inv) {
        UUID bookingId = null;
        String bookingRef = null;
        String propName = null;
        String safariRef = null;
        String clientName = null;

        if (inv.getAccommodationBooking() != null) {
            AccommodationBooking b = inv.getAccommodationBooking();
            bookingId = b.getId();
            bookingRef = b.getReferenceNumber();
            if (b.getProperty() != null) {
                propName = b.getProperty().getName();
            }
            if (b.getAccommodationRequirement() != null && b.getAccommodationRequirement().getSafari() != null) {
                safariRef = b.getAccommodationRequirement().getSafari().getReferenceNumber();
                if (b.getAccommodationRequirement().getSafari().getClient() != null) {
                    clientName = b.getAccommodationRequirement().getSafari().getClient().getFirstName() + " "
                            + b.getAccommodationRequirement().getSafari().getClient().getLastName();
                }
            }
        }

        return InvoiceResponse.builder()
                .id(inv.getId())
                .bookingId(bookingId)
                .bookingReference(bookingRef)
                .safariReference(safariRef)
                .propertyName(propName)
                .clientName(clientName)
                .invoiceNumber(inv.getInvoiceNumber())
                .amount(inv.getAmount())
                .currency(inv.getCurrency())
                .dueDate(inv.getDueDate())
                .status(inv.getStatus())
                .fileName(inv.getFileName())
                .filePath(inv.getFilePath())
                .issuedAt(inv.getIssuedAt())
                .build();
    }
}
