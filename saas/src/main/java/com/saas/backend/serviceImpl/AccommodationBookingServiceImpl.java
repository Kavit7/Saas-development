package com.saas.backend.serviceImpl;

import java.time.OffsetDateTime;
import java.time.Year;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.saas.backend.repositories.AccommodationBookingRepository;
import com.saas.backend.repositories.AccommodationRequirementRepository;
import com.saas.backend.repositories.PropertyRepository;
import com.saas.backend.response.AccommodationBookingResponse;
import com.saas.backend.service.AccommodationBookingService;
import com.saas.backend.service.BookingFollowUpService;
import com.saas.backend.service.EmailService;
import com.saas.backend.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 

public class AccommodationBookingServiceImpl implements AccommodationBookingService {
    private final AccommodationBookingRepository accommodationBookingRepository;
    private final AccommodationRequirementRepository accommodationRequirementRepository;
    private final PropertyRepository propertyRepository;
    private final CurrentUserChecker checker;
    private final BookingFollowUpService  bookingFollowUpService;
    private final EmailService emailService;

    private final NotificationService
            notificationService;


    @Override
    @Transactional 
    public AccommodationBookingResponse createBooking(AccommodationBookingRequest request) {
       AccommodationRequirement requirement = accommodationRequirementRepository.findById(request.getAccomodationRequirementId()).orElseThrow(() -> new ResourceNotFoundException("Accommodation Requirement Not found"));
       
       
       Property property =propertyRepository.findById(request.getPropertyId()).orElseThrow(() -> new ResourceNotFoundException("Property Not found"));



       //validate Date
       if(!request.getCheckOut().isAfter(request.getCheckIn())){
        throw new IllegalArgumentException("checkout must be after check in");
       }


       User reservationManager= checker.checkCurrentUser();

       String referenceNumber=generateReferenceNumber();


       AccommodationBooking booking= AccommodationBooking.builder()
       .accommodationRequirement(requirement)
       .property(property)
       .reservationManager(reservationManager)
       .checkIn(request.getCheckIn())
       .checkOut(request.getCheckOut())
       .status(BookingStatus.DRAFT)
       .referenceNumber(referenceNumber)
       .notes(request.getNotes())
       .build();

       booking=accommodationBookingRepository.save(booking);
       return  mapToResponse(booking);
    }




    @Override 
    @Transactional 
    public AccommodationBookingResponse sendBookingRequest(UUID bookingId){
            
        // find Booking 
        AccommodationBooking booking = accommodationBookingRepository.findById(bookingId).orElseThrow(()-> new ResourceNotFoundException("Booking Not found"));

        // make sure if booking is still Draft;
        if (booking.getStatus() !=BookingStatus.DRAFT){
            throw new IllegalStateException("Only Draft bookings can be sent");

        }
        Property property= booking.getProperty();
        if (property.getContactEmail() == null || property.getContactEmail().isBlank()){
            throw new IllegalStateException("Property does not have valid contact email");
        }
        User reservationManager = checker.checkCurrentUser();
          
        // make sure the curent user ons this booking

        if (!booking.getReservationManager().getId().equals(reservationManager.getId())){
            throw new IllegalStateException("You are not allowed to send this booking request");
        }
       
        /* 
        * EMAIL WILL BE SENT HERE LATER
        *
        * 
        */
       emailService.sendBookingRequest(booking);
        
        // mark booking as awaiting provisonal

        booking.setStatus(BookingStatus.PROVISIONAL);
        booking.setRequestedAt(OffsetDateTime.now());
        booking =accommodationBookingRepository.save(booking);
     /*
     UPDATE REQUIREMENT
     * SALES PERSON REQUIREMENT CHANGE PENDING TO INPROGRESS
      
     
     
     */

        AccommodationRequirement requirement= booking.getAccommodationRequirement();
        requirement.setAccomodationRequirmentStatus(AccomodationRequirmentStatus.AWAITING_RESPONSE);
        accommodationRequirementRepository.save(requirement);


        /*
        * 
        create initial-request followup here 
        and notification
        */

        // update
         bookingFollowUpService
            .createInitialFollowUp(booking);

         notificationService
            .notifyBookingSent(booking);

        return  mapToResponse(booking);

    }


    @Override
@Transactional
public AccommodationBookingResponse declineBooking(
        UUID bookingId) {

    AccommodationBooking booking =
            accommodationBookingRepository
                    .findById(bookingId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Accommodation booking not found"
                            ));

    if (booking.getStatus()
            != BookingStatus.PROVISIONAL) {

        throw new IllegalStateException(
                "Booking is not awaiting response"
        );
    }

    booking.setStatus(
            BookingStatus.CANCELLED
    );

    booking.setRespondedAt(
            OffsetDateTime.now()
    );

    booking =
            accommodationBookingRepository.save(booking);

    /*
     * Stop reminders for this property
     */
    bookingFollowUpService
            .cancelPendingFollowUps(
                    booking.getId()
            );

    /*
     * Requirement itself is NOT necessarily
     * DECLINED.
     *
     * We still need accommodation.
     */
    notificationService
            .notifyBookingDeclined(booking);

    return mapToResponse(booking);
}
@Override
@Transactional
public AccommodationBookingResponse confirmBooking(
        UUID bookingId,
        String confirmationNumber) {

    AccommodationBooking booking =
            accommodationBookingRepository
                    .findById(bookingId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Accommodation booking not found"
                            ));

    if (booking.getStatus()
            != BookingStatus.PROVISIONAL) {

        throw new IllegalStateException(
                "Booking is not awaiting response"
        );
    }

    if (confirmationNumber == null ||
            confirmationNumber.isBlank()) {

        throw new IllegalArgumentException(
                "Confirmation number is required"
        );
    }

    booking.setConfirmationNumber(
            confirmationNumber
    );

    booking.setStatus(
            BookingStatus.CONFIRMED
    );

    booking.setRespondedAt(
            OffsetDateTime.now()
    );

    booking.setConfirmedAt(
            OffsetDateTime.now()
    );

    booking =
            accommodationBookingRepository.save(booking);

    /*
     * Update requirement
     */
    AccommodationRequirement requirement =
            booking.getAccommodationRequirement();

    requirement.setAccomodationRequirmentStatus(
            AccomodationRequirmentStatus.COMPLETED
    );

    accommodationRequirementRepository.save(requirement);

    /*
     * Stop future reminders
     */
    bookingFollowUpService
            .cancelPendingFollowUps(
                    booking.getId()
            );

    /*
     * Notify Sales Person
     * + Reservation Manager
     */
    notificationService
            .notifyBookingConfirmed(booking);

    return mapToResponse(booking);
}

    private AccommodationBookingResponse mapToResponse(AccommodationBooking booking) {
        AccommodationBookingResponse response =AccommodationBookingResponse.builder()
        .id(booking.getId())
        .propertyName(booking.getProperty().getName())
        .requirementId(booking.getAccommodationRequirement().getId())
        .reservationManagerName(booking.getReservationManager().getEmail())
        .checkIn(booking.getCheckIn())
        .checkOut(booking.getCheckOut())
         .referenceNumber(booking.getReferenceNumber())
         .confirmationNumber(booking.getConfirmationNumber())
         .requestedAt(booking.getRequestedAt())
         .confirmedAt(booking.getConfirmedAt())
         .respondedAt(booking.getRespondedAt())
         .notes(booking.getNotes())
         .status(booking.getStatus())
         
         .build();

         return response;
            }

@Override
@Transactional
public void processEmailConfirmation(
        AccommodationBooking detachedBooking,
        IncomingMailMessage email) {

    // Reload inside this transaction so lazy relations work
    AccommodationBooking booking =
            accommodationBookingRepository
                    .findById(detachedBooking.getId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Booking Not found"
                            ));

    booking.setStatus(
            BookingStatus.CONFIRMED
    );

    booking.setRespondedAt(
            OffsetDateTime.now()
    );

    accommodationBookingRepository.save(booking);

    AccommodationRequirement requirement =
            booking.getAccommodationRequirement();

    requirement.setAccomodationRequirmentStatus(
            AccomodationRequirmentStatus.COMPLETED
    );

    accommodationRequirementRepository.save(requirement);

    bookingFollowUpService
            .cancelPendingFollowUps(
                    booking.getId()
            );

    notificationService
            .notifyBookingConfirmed(booking);
}

@Override
@Transactional
public void processEmailDecline(
        AccommodationBooking detachedBooking,
        IncomingMailMessage email) {

    // Reload inside this transaction so lazy relations work
    AccommodationBooking booking =
            accommodationBookingRepository
                    .findById(detachedBooking.getId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Booking Not found"
                            ));

    // 1. Update booking status
    booking.setStatus(BookingStatus.CANCELLED);

    booking.setRespondedAt(
            OffsetDateTime.now()
    );

    accommodationBookingRepository.save(booking);

    // 2. Stop follow-ups for this booking
    bookingFollowUpService
            .cancelPendingFollowUps(
                    booking.getId()
            );

    // 3. Requirement is still in progress
    AccommodationRequirement requirement =
            booking.getAccommodationRequirement();

    requirement.setAccomodationRequirmentStatus(
            AccomodationRequirmentStatus.IN_PROGRESS
    );

    accommodationRequirementRepository.save(
            requirement
    );

    // 4. Notify Reservation Manager
    notificationService.notifyBookingDeclined(
            booking
    );

    // 5. Notify Sales Person
    // notificationService.notify(
    //         requirement
    // );
}
    private String generateReferenceNumber() {
        String year= String.valueOf(Year.now().getValue());

        String random =UUID.randomUUID().toString().substring(0,6).toUpperCase();

        return "ACC-BOOK-"+year+"-"+random;
    }
    
}
