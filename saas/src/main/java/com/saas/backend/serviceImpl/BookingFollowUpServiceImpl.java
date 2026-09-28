package com.saas.backend.serviceImpl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.backend.models.AccommodationBooking;
import com.saas.backend.models.BookingFollowUp;
import com.saas.backend.models.FollowUpStatus;
import com.saas.backend.models.FollowUpType;
import com.saas.backend.repositories.BookingFollowUpRepository;
import com.saas.backend.service.BookingFollowUpService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingFollowUpServiceImpl
        implements BookingFollowUpService {

    private final BookingFollowUpRepository followUpRepository;

    @Override
    @Transactional
    public void createInitialFollowUp(
            AccommodationBooking booking) {

        BookingFollowUp followUp =
                BookingFollowUp.builder()
                        .booking(booking)
                        .type(FollowUpType.INITIAL_REQUEST)
                        .status(FollowUpStatus.SENT)
                        .scheduledAt(OffsetDateTime.now())
                        .sentAt(OffsetDateTime.now())
                        .message(
                                "Initial accommodation reservation request sent to property."
                        )
                        .createdBy(
                                booking.getReservationManager()
                        )
                        .build();

        followUpRepository.save(followUp);
    }

    @Override
    @Transactional
    public void createReminder(
            AccommodationBooking booking) {

        BookingFollowUp followUp =
                BookingFollowUp.builder()
                        .booking(booking)
                        .type(FollowUpType.REMINDER)
                        .status(FollowUpStatus.SENT)
                        .scheduledAt(OffsetDateTime.now())
                        .sentAt(OffsetDateTime.now())
                        .message(
                                "Accommodation reservation reminder sent to property."
                        )
                        .createdBy(
                                booking.getReservationManager()
                        )
                        .build();

        followUpRepository.save(followUp);
    }

    @Override
    @Transactional
    public void cancelPendingFollowUps(UUID bookingId){
        List<BookingFollowUp> pendingFollowUps =
            followUpRepository.findByBookingIdAndStatus(
                    bookingId,
                    FollowUpStatus.SCHEDULED);

    for (BookingFollowUp followUp : pendingFollowUps) {
        followUp.setStatus(FollowUpStatus.CANCELLED);
    }

    followUpRepository.saveAll(pendingFollowUps);
}
        }