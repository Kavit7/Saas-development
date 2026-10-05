package com.saas.backend.serviceImpl;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.backend.dto.AccommodationInquiryRequest;
import com.saas.backend.models.AccommodationBooking;
import com.saas.backend.models.AccommodationInquiry;
import com.saas.backend.models.Priority;
import com.saas.backend.models.TicketStatus;
import com.saas.backend.models.User;
import com.saas.backend.repositories.AccommodationBookingRepository;
import com.saas.backend.repositories.AccommodationInquiryRepository;
import com.saas.backend.repositories.UserRepository;
import com.saas.backend.response.AccommodationInquiryResponse;
import com.saas.backend.service.AccommodationInquiryService;
import com.saas.backend.service.AuditLogService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccommodationInquiryServiceImpl implements AccommodationInquiryService {

    private final AccommodationInquiryRepository inquiryRepository;
    private final AccommodationBookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    private AccommodationInquiryResponse mapToResponse(AccommodationInquiry inquiry) {
        if (inquiry == null) return null;

        String bookingRef = inquiry.getAccommodationBooking() != null
                ? inquiry.getAccommodationBooking().getReferenceNumber()
                : null;
        String propName = (inquiry.getAccommodationBooking() != null && inquiry.getAccommodationBooking().getProperty() != null)
                ? inquiry.getAccommodationBooking().getProperty().getName()
                : null;

        String senderName = inquiry.getSender() != null
                ? (inquiry.getSender().getFirstName() + " " + inquiry.getSender().getLastName()).trim()
                : null;
        String senderEmail = inquiry.getSender() != null ? inquiry.getSender().getEmail() : null;

        String receiverName = inquiry.getReceiver() != null
                ? (inquiry.getReceiver().getFirstName() + " " + inquiry.getReceiver().getLastName()).trim()
                : null;
        String receiverEmail = inquiry.getReceiver() != null ? inquiry.getReceiver().getEmail() : null;

        return AccommodationInquiryResponse.builder()
                .id(inquiry.getId())
                .bookingId(inquiry.getAccommodationBooking() != null ? inquiry.getAccommodationBooking().getId() : null)
                .bookingReference(bookingRef)
                .propertyName(propName)
                .senderId(inquiry.getSender() != null ? inquiry.getSender().getId() : null)
                .senderName(senderName)
                .senderEmail(senderEmail)
                .receiverId(inquiry.getReceiver() != null ? inquiry.getReceiver().getId() : null)
                .receiverName(receiverName)
                .receiverEmail(receiverEmail)
                .subject(inquiry.getSubject())
                .message(inquiry.getMessage())
                .priority(inquiry.getPriority())
                .status(inquiry.getStatus())
                .createdAt(inquiry.getCreatedAt())
                .updatedAt(inquiry.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public AccommodationInquiryResponse createInquiry(AccommodationInquiryRequest request, Authentication auth) {
        User sender = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("User not authenticated"));

        AccommodationBooking booking = bookingRepository.findById(request.getAccommodationBookingId())
                .orElseThrow(() -> new RuntimeException("Accommodation booking not found"));

        User receiver = null;
        if (request.getReceiverId() != null) {
            receiver = userRepository.findById(request.getReceiverId()).orElse(null);
        }
        if (receiver == null && booking.getReservationManager() != null) {
            receiver = booking.getReservationManager();
        }

        AccommodationInquiry inquiry = AccommodationInquiry.builder()
                .accommodationBooking(booking)
                .sender(sender)
                .receiver(receiver)
                .subject(request.getSubject())
                .message(request.getMessage())
                .priority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM)
                .status(TicketStatus.OPEN)
                .build();

        AccommodationInquiry saved = inquiryRepository.save(inquiry);

        if (sender.getCompany() != null) {
            auditLogService.recordAuditLog(
                    sender.getCompany(),
                    sender,
                    "CREATE_ACCOMMODATION_INQUIRY",
                    "accommodation_inquiry",
                    saved.getId(),
                    null,
                    "{\"subject\":\"" + saved.getSubject() + "\"}",
                    "0.0.0.0",
                    "Internal Web Client"
            );
        }

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccommodationInquiryResponse> getInquiries(Authentication auth, UUID bookingId) {
        if (bookingId != null) {
            return inquiryRepository.findByAccommodationBookingIdOrderByCreatedAtDesc(bookingId)
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        User currentUser = userRepository.findByEmail(auth.getName()).orElse(null);
        if (currentUser != null && currentUser.getCompany() != null) {
            return inquiryRepository.findByCompanyId(currentUser.getCompany().getId())
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @Override
    @Transactional
    public AccommodationInquiryResponse updateInquiryStatus(UUID id, String status, Authentication auth) {
        AccommodationInquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inquiry not found"));

        TicketStatus newStatus = TicketStatus.valueOf(status.toUpperCase().trim());
        inquiry.setStatus(newStatus);
        AccommodationInquiry saved = inquiryRepository.save(inquiry);
        return mapToResponse(saved);
    }
}
