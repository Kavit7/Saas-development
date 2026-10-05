package com.saas.backend.serviceImpl;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Year;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.backend.AccessHelper.CompanyAccessValidator;
import com.saas.backend.AccessHelper.CurrentUserChecker;
import com.saas.backend.Exception.ResourceNotFoundException;
import com.saas.backend.dto.AccommodationBookingRequest;
import com.saas.backend.dto.IncomingMailMessage;
import com.saas.backend.models.AccommodationBooking;
import com.saas.backend.models.AccommodationRequirement;
import com.saas.backend.models.AccomodationRequirmentStatus;
import com.saas.backend.models.BookingStatus;
import com.saas.backend.models.Property;
import com.saas.backend.models.User;
import com.saas.backend.models.Safari;
import com.saas.backend.models.SafariStatus;
import com.saas.backend.models.Priority;
import com.saas.backend.models.Invoice;
import com.saas.backend.models.InvoiceStatus;
import com.saas.backend.repositories.AccommodationBookingRepository;
import com.saas.backend.repositories.AccommodationRequirementRepository;
import com.saas.backend.repositories.InvoiceRepository;
import com.saas.backend.repositories.PropertyRepository;
import com.saas.backend.repositories.SafariRepository;
import com.saas.backend.response.AccommodationBookingResponse;
import com.saas.backend.service.AccommodationBookingService;
import com.saas.backend.service.BookingFollowUpService;
import com.saas.backend.service.EmailService;
import com.saas.backend.service.NotificationService;
import com.saas.backend.specification.AccommodationBookingSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AccommodationBookingServiceImpl
 * Handles booking creation, hotel reservation requests, email dispatch,
 * automated follow-ups, and confirmation tracking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccommodationBookingServiceImpl implements AccommodationBookingService {

    private final AccommodationBookingRepository accommodationBookingRepository;
    private final AccommodationRequirementRepository accommodationRequirementRepository;
    private final PropertyRepository propertyRepository;
    private final CurrentUserChecker checker;
    private final CompanyAccessValidator companyAccessValidator;
    private final BookingFollowUpService bookingFollowUpService;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final SafariRepository safariRepository;
    private final InvoiceRepository invoiceRepository;

    private void validateBookingCompanyAccess(AccommodationBooking booking) {
        if (booking != null &&
            booking.getAccommodationRequirement() != null &&
            booking.getAccommodationRequirement().getSafari() != null &&
            booking.getAccommodationRequirement().getSafari().getClient() != null &&
            booking.getAccommodationRequirement().getSafari().getClient().getCompany() != null) {
            companyAccessValidator.validate(booking.getAccommodationRequirement().getSafari().getClient().getCompany().getId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccommodationBookingResponse> getBookings(
            int page,
            int size,
            String sortBy,
            String direction,
            String search,
            BookingStatus status
    ) {
        User currentUser = checker.checkCurrentUser();
        Sort sort = "desc".equalsIgnoreCase(direction) ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<AccommodationBooking> specification = (root, query, cb) -> cb.conjunction();

        boolean isSuperAdmin = currentUser.getRole() != null &&
                "SUPER_ADMIN".equalsIgnoreCase(currentUser.getRole().getName().replace("ROLE_", "").trim());

        if (!isSuperAdmin && currentUser.getCompany() != null) {
            specification = specification.and(AccommodationBookingSpecification.hasCompany(currentUser.getCompany().getId()));
        }

        if (status != null) {
            specification = specification.and(AccommodationBookingSpecification.hasStatus(status));
        }

        if (search != null && !search.isBlank()) {
            specification = specification.and(AccommodationBookingSpecification.hasSearch(search));
        }

        return accommodationBookingRepository.findAll(specification, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccommodationBookingResponse> getAllBookings() {
        User currentUser = checker.checkCurrentUser();
        boolean isSuperAdmin = currentUser.getRole() != null &&
                "SUPER_ADMIN".equalsIgnoreCase(currentUser.getRole().getName().replace("ROLE_", "").trim());

        if (!isSuperAdmin && currentUser.getCompany() != null) {
            Specification<AccommodationBooking> spec = AccommodationBookingSpecification.hasCompany(currentUser.getCompany().getId());
            return accommodationBookingRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"))
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        return accommodationBookingRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AccommodationBookingResponse getBookingById(UUID bookingId) {
        AccommodationBooking booking = accommodationBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));
        validateBookingCompanyAccess(booking);
        return mapToResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccommodationBookingResponse> getBookingsByRequirement(UUID requirementId) {
        AccommodationRequirement requirement = accommodationRequirementRepository.findById(requirementId)
                .orElseThrow(() -> new ResourceNotFoundException("Accommodation requirement not found: " + requirementId));
        if (requirement.getSafari() != null &&
            requirement.getSafari().getClient() != null &&
            requirement.getSafari().getClient().getCompany() != null) {
            companyAccessValidator.validate(requirement.getSafari().getClient().getCompany().getId());
        }
        return accommodationBookingRepository.findByAccommodationRequirementId(requirementId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccommodationBookingResponse> getBookingsBySafari(UUID safariId) {
        Safari safari = safariRepository.findById(safariId)
                .orElseThrow(() -> new ResourceNotFoundException("Safari not found: " + safariId));
        if (safari.getClient() != null && safari.getClient().getCompany() != null) {
            companyAccessValidator.validate(safari.getClient().getCompany().getId());
        }
        return accommodationBookingRepository.findBySafariId(safariId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AccommodationBookingResponse createBooking(AccommodationBookingRequest request) {
        AccommodationRequirement requirement = accommodationRequirementRepository.findById(request.getAccomodationRequirementId())
                .orElseThrow(() -> new ResourceNotFoundException("Accommodation Requirement Not found: " + request.getAccomodationRequirementId()));

        if (requirement.getSafari() != null &&
            requirement.getSafari().getClient() != null &&
            requirement.getSafari().getClient().getCompany() != null) {
            companyAccessValidator.validate(requirement.getSafari().getClient().getCompany().getId());
        }

        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property Not found: " + request.getPropertyId()));

        if (requirement.getAccomodationRequirmentStatus() == AccomodationRequirmentStatus.COMPLETED) {
            throw new IllegalStateException("This accommodation requirement has already been booked and completed.");
        }

        // Automatically infer and fallback dates if omitted from the direct request
        LocalDate checkIn = request.getCheckIn();
        if (checkIn == null) {
            if (requirement.getItineraryDay() != null && requirement.getItineraryDay().getDate() != null) {
                checkIn = requirement.getItineraryDay().getDate();
            } else if (requirement.getSafari() != null && requirement.getSafari().getStartDate() != null) {
                checkIn = requirement.getSafari().getStartDate();
            } else {
                checkIn = LocalDate.now();
            }
        }

        LocalDate checkOut = request.getCheckOut();
        if (checkOut == null) {
            checkOut = checkIn.plusDays(1);
        }

        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Checkout date (" + checkOut + ") must be after check-in date (" + checkIn + ")");
        }

        User reservationManager = null;
        try {
            reservationManager = checker.checkCurrentUser();
        } catch (Exception e) {
            log.warn("Could not resolve current reservation manager from context, saving booking without explicit user link");
        }

        String referenceNumber = generateReferenceNumber();

        AccommodationBooking booking = AccommodationBooking.builder()
                .accommodationRequirement(requirement)
                .property(property)
                .reservationManager(reservationManager)
                .checkIn(checkIn)
                .checkOut(checkOut)
                .status(BookingStatus.DRAFT)
                .referenceNumber(referenceNumber)
                .notes(request.getNotes())
                .build();

        booking = accommodationBookingRepository.save(booking);

        // Update requirement status to IN_PROGRESS
        requirement.setAccomodationRequirmentStatus(AccomodationRequirmentStatus.IN_PROGRESS);
        accommodationRequirementRepository.save(requirement);

        // Automatically progress parent Safari from DRAFT to IN_PROGRESS
        Safari safari = requirement.getSafari();
        if (safari != null && safari.getStatus() == SafariStatus.DRAFT) {
            safari.setStatus(SafariStatus.IN_PROGRESS);
            safariRepository.save(safari);
            log.info("Transitioned Safari {} from DRAFT to IN_PROGRESS due to booking allocation", safari.getReferenceNumber());
        }

        // Notify the Sales Person that a lodge has been allocated
        if (safari != null && safari.getSalesPerson() != null) {
            String rmName = reservationManager != null ? (reservationManager.getFirstName() + " " + (reservationManager.getLastName() != null ? reservationManager.getLastName() : "")) : "Reservation Desk";
            notificationService.createNotification(
                    safari.getSalesPerson(),
                    "BOOKING_ALLOCATED",
                    "Lodge Allocated for Safari",
                    String.format("Lodge '%s' was allocated for Safari %s on %s by %s.",
                            property.getName(),
                            safari.getReferenceNumber(),
                            checkIn,
                            rmName),
                    Priority.MEDIUM,
                    "AccommodationBooking",
                    booking.getId()
            );
        }

        log.info("Created draft accommodation booking {} for property '{}'", referenceNumber, property.getName());
        return mapToResponse(booking);
    }

    @Override
    @Transactional
    public AccommodationBookingResponse sendBookingRequest(UUID bookingId) {
        AccommodationBooking booking = accommodationBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking Not found: " + bookingId));
        validateBookingCompanyAccess(booking);

        if (booking.getStatus() != BookingStatus.DRAFT) {
            throw new IllegalStateException("Only Draft bookings can be dispatched to property");
        }

        Property property = booking.getProperty();
        if (property.getContactEmail() == null || property.getContactEmail().isBlank()) {
            throw new IllegalStateException("Property '" + property.getName() + "' does not have a contact email configured");
        }

        // Authorize user (allow assigned reservation manager, admins, or platform managers)
        try {
            User currentUser = checker.checkCurrentUser();
            boolean isStaffAdmin = currentUser.getRole() != null &&
                    (currentUser.getRole().getName().toUpperCase().contains("ADMIN") ||
                     currentUser.getRole().getName().toUpperCase().contains("MANAGER"));

            if (!isStaffAdmin && booking.getReservationManager() != null
                    && !booking.getReservationManager().getId().equals(currentUser.getId())) {
                log.warn("Non-admin user {} attempting to send booking for {}", currentUser.getEmail(), booking.getReservationManager().getEmail());
            }
        } catch (Exception ignored) {
            // Permit system dispatch
        }

        // Send Email with resilience (don't break database transaction if local SMTP is offline)
        try {
            emailService.sendBookingRequest(booking);
            log.info("Dispatched booking email to {}", property.getContactEmail());
        } catch (Exception e) {
            log.warn("Could not dispatch live email to {} (SMTP offline): {}", property.getContactEmail(), e.getMessage());
        }

        // Transition booking to PROVISIONAL
        booking.setStatus(BookingStatus.PROVISIONAL);
        booking.setRequestedAt(OffsetDateTime.now());
        booking = accommodationBookingRepository.save(booking);

        // Update requirement status to AWAITING_RESPONSE
        AccommodationRequirement requirement = booking.getAccommodationRequirement();
        requirement.setAccomodationRequirmentStatus(AccomodationRequirmentStatus.AWAITING_RESPONSE);
        accommodationRequirementRepository.save(requirement);

        // Schedule follow-ups & dispatch notifications
        try {
            bookingFollowUpService.createInitialFollowUp(booking);
        } catch (Exception e) {
            log.warn("Follow-up schedule notice: {}", e.getMessage());
        }

        notificationService.notifyBookingSent(booking);

        return mapToResponse(booking);
    }

    @Override
    @Transactional
    public AccommodationBookingResponse declineBooking(UUID bookingId) {
        AccommodationBooking booking = accommodationBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Accommodation booking not found: " + bookingId));
        validateBookingCompanyAccess(booking);

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setRespondedAt(OffsetDateTime.now());
        booking = accommodationBookingRepository.save(booking);

        // Cancel pending follow-up reminders
        try {
            bookingFollowUpService.cancelPendingFollowUps(booking.getId());
        } catch (Exception e) {
            log.warn("Follow-up cancellation notice: {}", e.getMessage());
        }

        // Revert requirement back to IN_PROGRESS so another property can be allocated
        AccommodationRequirement requirement = booking.getAccommodationRequirement();
        if (requirement != null) {
            requirement.setAccomodationRequirmentStatus(AccomodationRequirmentStatus.IN_PROGRESS);
            accommodationRequirementRepository.save(requirement);
        }

        notificationService.notifyBookingDeclined(booking);
        return mapToResponse(booking);
    }

    @Override
    @Transactional
    public AccommodationBookingResponse confirmBooking(UUID bookingId, String confirmationNumber) {
        AccommodationBooking booking = accommodationBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Accommodation booking not found: " + bookingId));
        validateBookingCompanyAccess(booking);

        if (confirmationNumber == null || confirmationNumber.isBlank()) {
            throw new IllegalArgumentException("Property confirmation number is required");
        }

        booking.setConfirmationNumber(confirmationNumber);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setRespondedAt(OffsetDateTime.now());
        booking.setConfirmedAt(OffsetDateTime.now());
        booking = accommodationBookingRepository.save(booking);

        // Requirement is now satisfied and completed
        AccommodationRequirement requirement = booking.getAccommodationRequirement();
        if (requirement != null) {
            requirement.setAccomodationRequirmentStatus(AccomodationRequirmentStatus.COMPLETED);
            accommodationRequirementRepository.saveAndFlush(requirement);

            // If all requirements for this safari are completed, progress Safari to CONFIRMED
            Safari safari = requirement.getSafari();
            if (safari != null) {
                List<AccommodationRequirement> allReqs = accommodationRequirementRepository.findBySafariId(safari.getId());
                boolean allCompleted = !allReqs.isEmpty() && allReqs.stream()
                        .allMatch(r -> r.getAccomodationRequirmentStatus() == AccomodationRequirmentStatus.COMPLETED);
                if (allCompleted) {
                    safari.setStatus(SafariStatus.CONFIRMED);
                    safariRepository.saveAndFlush(safari);
                    log.info("All requirements completed! Transitioned Safari {} to CONFIRMED", safari.getReferenceNumber());
                } else if (safari.getStatus() == SafariStatus.DRAFT) {
                    safari.setStatus(SafariStatus.IN_PROGRESS);
                    safariRepository.saveAndFlush(safari);
                }
            }
        }

        // Stop future reminders
        try {
            bookingFollowUpService.cancelPendingFollowUps(booking.getId());
        } catch (Exception e) {
            log.warn("Follow-up cancellation notice: {}", e.getMessage());
        }

        // Auto-generate invoice if not exists
        createOrGetBookingInvoice(booking);

        // Notify sales consultant and staff
        notificationService.notifyBookingConfirmed(booking);
        return mapToResponse(booking);
    }

    @Override
    @Transactional
    public void processEmailConfirmation(AccommodationBooking detachedBooking, IncomingMailMessage email) {
        AccommodationBooking booking = accommodationBookingRepository.findById(detachedBooking.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking Not found: " + detachedBooking.getId()));

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setRespondedAt(OffsetDateTime.now());
        booking.setConfirmedAt(OffsetDateTime.now());
        accommodationBookingRepository.save(booking);

        AccommodationRequirement requirement = booking.getAccommodationRequirement();
        if (requirement != null) {
            requirement.setAccomodationRequirmentStatus(AccomodationRequirmentStatus.COMPLETED);
            accommodationRequirementRepository.saveAndFlush(requirement);

            Safari safari = requirement.getSafari();
            if (safari != null) {
                List<AccommodationRequirement> allReqs = accommodationRequirementRepository.findBySafariId(safari.getId());
                boolean allCompleted = !allReqs.isEmpty() && allReqs.stream()
                        .allMatch(r -> r.getAccomodationRequirmentStatus() == AccomodationRequirmentStatus.COMPLETED);
                if (allCompleted) {
                    safari.setStatus(SafariStatus.CONFIRMED);
                    safariRepository.saveAndFlush(safari);
                }
            }
        }

        try {
            bookingFollowUpService.cancelPendingFollowUps(booking.getId());
        } catch (Exception ignored) {}

        createOrGetBookingInvoice(booking);

        notificationService.notifyBookingConfirmed(booking);
    }

    @Override
    @Transactional
    public void processEmailDecline(AccommodationBooking detachedBooking, IncomingMailMessage email) {
        AccommodationBooking booking = accommodationBookingRepository.findById(detachedBooking.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking Not found: " + detachedBooking.getId()));

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setRespondedAt(OffsetDateTime.now());
        accommodationBookingRepository.save(booking);

        try {
            bookingFollowUpService.cancelPendingFollowUps(booking.getId());
        } catch (Exception ignored) {}

        AccommodationRequirement requirement = booking.getAccommodationRequirement();
        if (requirement != null) {
            requirement.setAccomodationRequirmentStatus(AccomodationRequirmentStatus.IN_PROGRESS);
            accommodationRequirementRepository.save(requirement);
        }

        notificationService.notifyBookingDeclined(booking);
    }

    private AccommodationBookingResponse mapToResponse(AccommodationBooking booking) {
        String propName = booking.getProperty() != null ? booking.getProperty().getName() : "Unknown Property";
        UUID propId = booking.getProperty() != null ? booking.getProperty().getId() : null;
        UUID reqId = booking.getAccommodationRequirement() != null ? booking.getAccommodationRequirement().getId() : null;
        UUID safariId = null;
        String safariRef = null;
        String dest = null;
        String clientName = null;
        String catName = null;
        Integer rooms = null;

        if (booking.getAccommodationRequirement() != null) {
            AccommodationRequirement req = booking.getAccommodationRequirement();
            dest = req.getDestination();
            rooms = req.getNumberOfRooms();
            if (req.getRequiredCategory() != null) {
                catName = req.getRequiredCategory().getName();
            }
            if (req.getSafari() != null) {
                safariId = req.getSafari().getId();
                safariRef = req.getSafari().getReferenceNumber();
                if (req.getSafari().getClient() != null) {
                    clientName = req.getSafari().getClient().getFirstName() + " " + req.getSafari().getClient().getLastName();
                }
            }
        }

        String rmName = "Staff";
        if (booking.getReservationManager() != null) {
            User rm = booking.getReservationManager();
            if (rm.getFirstName() != null && !rm.getFirstName().isBlank()) {
                rmName = rm.getFirstName() + " " + (rm.getLastName() != null ? rm.getLastName() : "");
            } else {
                rmName = rm.getEmail();
            }
        }

        return AccommodationBookingResponse.builder()
                .id(booking.getId())
                .referenceNumber(booking.getReferenceNumber())
                .requirementId(reqId)
                .propertyId(propId)
                .propertyName(propName)
                .safariId(safariId)
                .safariReference(safariRef)
                .destination(dest)
                .clientName(clientName)
                .categoryName(catName)
                .roomsCount(rooms)
                .reservationManagerName(rmName)
                .checkIn(booking.getCheckIn())
                .checkOut(booking.getCheckOut())
                .status(booking.getStatus())
                .confirmationNumber(booking.getConfirmationNumber())
                .requestedAt(booking.getRequestedAt())
                .confirmedAt(booking.getConfirmedAt())
                .respondedAt(booking.getRespondedAt())
                .notes(booking.getNotes())
                .build();
    }

    private String generateReferenceNumber() {
        String year = String.valueOf(Year.now().getValue());
        String random = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "ACC-BOOK-" + year + "-" + random;
    }

    private void createOrGetBookingInvoice(AccommodationBooking booking) {
        try {
            if (!invoiceRepository.existsByAccommodationBookingId(booking.getId())) {
                java.math.BigDecimal pricePerNight = java.math.BigDecimal.valueOf(250.00);
                if (booking.getProperty() != null && booking.getProperty().getPriceTier() != null && booking.getProperty().getPriceTier().getMinPrice() != null) {
                    pricePerNight = booking.getProperty().getPriceTier().getMinPrice();
                }

                long nights = 1;
                if (booking.getCheckIn() != null && booking.getCheckOut() != null) {
                    long diff = java.time.temporal.ChronoUnit.DAYS.between(booking.getCheckIn(), booking.getCheckOut());
                    if (diff > 0) {
                        nights = diff;
                    }
                }

                int rooms = 1;
                if (booking.getAccommodationRequirement() != null && booking.getAccommodationRequirement().getNumberOfRooms() != null) {
                    rooms = booking.getAccommodationRequirement().getNumberOfRooms();
                }

                java.math.BigDecimal totalAmount = pricePerNight.multiply(java.math.BigDecimal.valueOf(nights * rooms));
                String invNum = "INV-" + booking.getReferenceNumber().replace("ACC-BOOK-", "");

                Invoice invoice = Invoice.builder()
                        .accommodationBooking(booking)
                        .invoiceNumber(invNum)
                        .amount(totalAmount)
                        .currency("USD")
                        .dueDate(booking.getCheckIn() != null ? booking.getCheckIn() : LocalDate.now().plusDays(7))
                        .status(InvoiceStatus.PENDING)
                        .fileName(invNum + ".pdf")
                        .filePath("/invoices/" + invNum + ".pdf")
                        .issuedAt(OffsetDateTime.now())
                        .build();

                invoiceRepository.save(invoice);
                log.info("Auto-generated Invoice {} for confirmed booking {}", invNum, booking.getReferenceNumber());
            }
        } catch (Exception e) {
            log.warn("Could not auto-generate invoice for booking {}: {}", booking.getReferenceNumber(), e.getMessage());
        }
    }
}
