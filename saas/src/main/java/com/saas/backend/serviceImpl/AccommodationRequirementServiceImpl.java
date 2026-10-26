package com.saas.backend.serviceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.backend.AccessHelper.CompanyAccessValidator;
import com.saas.backend.Exception.DuplicateException;
import com.saas.backend.Exception.ResourceNotFoundException;
import com.saas.backend.dto.RequirementRequest;
import com.saas.backend.dto.RoomRequirementRequest;
import com.saas.backend.models.AccommodationBooking;
import com.saas.backend.models.AccommodationRequirement;
import com.saas.backend.models.AccomodationRequirmentStatus;
import com.saas.backend.models.BookingStatus;
import com.saas.backend.models.Company;
import com.saas.backend.models.ItineraryDay;
import com.saas.backend.models.PriceTier;
import com.saas.backend.models.PropertyCategory;
import com.saas.backend.models.RoomRequirement;
import com.saas.backend.models.RoomType;
import com.saas.backend.models.Safari;
import com.saas.backend.repositories.AccommodationBookingRepository;
import com.saas.backend.repositories.AccommodationRequirementRepository;
import com.saas.backend.repositories.ItineraryDayRepository;
import com.saas.backend.repositories.PriceTierRepository;
import com.saas.backend.repositories.PropertyCategoryRepository;
import com.saas.backend.repositories.RoomTypeRepository;
import com.saas.backend.repositories.SafariRepository;
import com.saas.backend.repositories.UserRepository;
import com.saas.backend.response.AccommodationRequirementResponse;
import com.saas.backend.response.RoomRequirementResponse;
import com.saas.backend.service.AccommodationRequirementService;

import com.saas.backend.models.User;
import com.saas.backend.repositories.UserRepository;
import com.saas.backend.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service 
@RequiredArgsConstructor 
@Slf4j
public class AccommodationRequirementServiceImpl implements AccommodationRequirementService {
    private final ItineraryDayRepository itineraryDayRepository;
    private final PropertyCategoryRepository propertyCategoryRepository;
    private final PriceTierRepository priceTierRepository;
    private final AccommodationRequirementRepository accommodationRequirementRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final CompanyAccessValidator companyAccessValidator;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final AccommodationBookingRepository accommodationBookingRepository;
    private final SafariRepository safariRepository;

    @Override
    public AccommodationRequirementResponse createAccommodationRequirement(UUID itineraryId, RequirementRequest request) {
        ItineraryDay itineraryDay = itineraryDayRepository.findById(itineraryId)
                .orElseThrow(() -> new ResourceNotFoundException("Day Not found"));

        if (itineraryDay.getSafari() != null && itineraryDay.getSafari().getClient() != null && itineraryDay.getSafari().getClient().getCompany() != null) {
            companyAccessValidator.validate(itineraryDay.getSafari().getClient().getCompany().getId());
        }

        boolean exists = accommodationRequirementRepository.existsByItineraryDay(itineraryDay);
        if (exists) {
            throw new DuplicateException("Requirement for this day already exists");
        }

        // Multi-night continuity protection: check if this day is already covered by a previous multi-night stay
        if (itineraryDay.getSafari() != null && itineraryDay.getDayNumber() != null) {
            List<AccommodationRequirement> existingSafariReqs = accommodationRequirementRepository.findBySafariId(itineraryDay.getSafari().getId());
            AccommodationRequirement coveringReq = existingSafariReqs.stream()
                    .filter(r -> r.getItineraryDay() != null && r.getItineraryDay().getDayNumber() != null)
                    .filter(r -> {
                        int startDay = r.getItineraryDay().getDayNumber();
                        int nights = (r.getNumberOfNights() != null && r.getNumberOfNights() > 0) ? r.getNumberOfNights() : 1;
                        return itineraryDay.getDayNumber() > startDay && itineraryDay.getDayNumber() < (startDay + nights);
                    })
                    .findFirst()
                    .orElse(null);

            if (coveringReq != null) {
                throw new IllegalStateException("Day " + itineraryDay.getDayNumber() + " is already covered by the " +
                        (coveringReq.getNumberOfNights() != null ? coveringReq.getNumberOfNights() : 1) +
                        "-night stay starting on Day " + coveringReq.getItineraryDay().getDayNumber() +
                        " (" + (coveringReq.getDestination() != null ? coveringReq.getDestination() : "Circuit") + "). " +
                        "No separate accommodation requirement is needed.");
            }
        }

        // Sequential Policy & Destination Check
        if (itineraryDay.getDestination() == null || itineraryDay.getDestination().trim().isEmpty() || itineraryDay.getDestination().toLowerCase().contains("pending")) {
            throw new IllegalStateException("Policy Guard: Day " + itineraryDay.getDayNumber() + " destination must be configured before creating lodging requirements.");
        }

        if (itineraryDay.getDayNumber() != null && itineraryDay.getDayNumber() > 1 && itineraryDay.getSafari() != null) {
            ItineraryDay prevDay = itineraryDayRepository.findBySafariIdAndDayNumber(itineraryDay.getSafari().getId(), itineraryDay.getDayNumber() - 1).orElse(null);
            if (prevDay != null && (prevDay.getDestination() == null || prevDay.getDestination().trim().isEmpty() || prevDay.getDestination().toLowerCase().contains("pending"))) {
                throw new IllegalStateException("Sequential Route Policy: Please configure Day " + prevDay.getDayNumber() + " destination before configuring Day " + itineraryDay.getDayNumber() + ".");
            }
        }
    
// ill implement the duplication
      PriceTier priceTier= priceTierRepository.findById(request.getPricetierId()).orElseThrow(()-> new ResourceNotFoundException("Price not found") );

       PropertyCategory propertyCategory = propertyCategoryRepository.findById(request.getCategoryId()).orElseThrow(()-> new ResourceNotFoundException("No property Category found"));

     
     AccommodationRequirement accommodationRequirement = new AccommodationRequirement();

     accommodationRequirement.setSafari(itineraryDay.getSafari());
     accommodationRequirement.setItineraryDay(itineraryDay);
     accommodationRequirement.setDestination(itineraryDay.getDestination());
     accommodationRequirement.setAccomodationRequirmentStatus(AccomodationRequirmentStatus.PENDING);
     accommodationRequirement.setNumberOfRooms(request.getNumberOfrooms());
     accommodationRequirement.setRequiredCategory(propertyCategory);
     accommodationRequirement.setRequiredPriceTier(priceTier);
     accommodationRequirement.setRoomPreferences(request.getRoomPreferences());
     
     int nights = (request.getNumberOfNights() != null && request.getNumberOfNights() > 0) ? request.getNumberOfNights() : 1;
     if (itineraryDay.getDate() != null && itineraryDay.getSafari() != null && itineraryDay.getSafari().getEndDate() != null) {
         java.time.LocalDate stayEnd = itineraryDay.getDate().plusDays(nights);
         if (stayEnd.isAfter(itineraryDay.getSafari().getEndDate().plusDays(1))) {
             throw new IllegalArgumentException("Stay duration (" + nights + " nights) exceeds the remaining safari duration.");
         }
     }
     accommodationRequirement.setNumberOfNights(nights);
     
     List<RoomRequirement> rooms = new ArrayList<>();
     for (RoomRequirementRequest roomRequest: request.getRoomRequirements()){
             
          RoomRequirement room = new RoomRequirement();

          RoomType roomType= roomTypeRepository.findByNameIgnoreCase(roomRequest.getRoomType()) .orElseThrow(() ->
                        new ResourceNotFoundException("Room type not found"));
          room.setRoomType(roomType);
          room.setQuantity(roomRequest.getQuantity());
          room.setAccommodationRequirement(accommodationRequirement);
          rooms.add(room);
     }

    
     int totalRooms = request.getRoomRequirements()
        .stream()
        .mapToInt(RoomRequirementRequest::getQuantity)
        .sum();

if (totalRooms != request.getNumberOfrooms()) {
    throw new IllegalArgumentException(
            "Total room quantities must equal number of rooms"
    );
}
     accommodationRequirement.setRooms(rooms);
     accommodationRequirement = accommodationRequirementRepository.save(accommodationRequirement);

     // Auto-propagate destination to covered continuing stay days
     if (request.getNumberOfNights() != null && request.getNumberOfNights() > 1 && itineraryDay.getSafari() != null) {
         int currentDayNum = itineraryDay.getDayNumber();
         for (int offset = 1; offset < request.getNumberOfNights(); offset++) {
             int nextDayNum = currentDayNum + offset;
             itineraryDayRepository.findBySafariIdAndDayNumber(itineraryDay.getSafari().getId(), nextDayNum)
                     .ifPresent(nextDay -> {
                         if (nextDay.getDestination() == null || nextDay.getDestination().isBlank()
                                 || nextDay.getDestination().equalsIgnoreCase("Destination not set")
                                 || nextDay.getDestination().toLowerCase().contains("pending")) {
                             nextDay.setDestination(itineraryDay.getDestination());
                             itineraryDayRepository.save(nextDay);
                             log.info("Auto-propagated destination '{}' from Day {} to continuing stay Day {}",
                                     itineraryDay.getDestination(), currentDayNum, nextDayNum);
                         }
                     });
         }
     }

     // Notify reservation managers about this pending requirement
     try {
         User salesPerson = itineraryDay.getSafari() != null ? itineraryDay.getSafari().getSalesPerson() : null;
         String spName = salesPerson != null ? (salesPerson.getFirstName() + " " + (salesPerson.getLastName() != null ? salesPerson.getLastName() : "")) : "Sales Consultant";
         String safRef = itineraryDay.getSafari() != null ? itineraryDay.getSafari().getReferenceNumber() : "Safari File";

         Company safariCompany = itineraryDay.getSafari() != null ? itineraryDay.getSafari().getClient().getCompany() : null;
         if (safariCompany != null) {
             List<User> targetStaff = userRepository.findByCompanyId(safariCompany.getId()).stream()
                     .filter(u -> u.getRole() != null && u.getRole().getName() != null &&
                             !u.getRole().getName().toUpperCase().contains("SUPER_ADMIN") &&
                             !u.getRole().getName().toUpperCase().contains("PLATFORM_ADMIN") &&
                             (u.getRole().getName().toUpperCase().contains("RESERVATION") ||
                              u.getRole().getName().toUpperCase().contains("ADMIN")))
                     .toList();

             for (User staff : targetStaff) {
                 notificationService.createNotification(
                         staff,
                         "REQUIREMENT_CREATED",
                         "New Lodging Allocation Needed",
                         String.format("Sales Consultant %s submitted lodging requirement for Safari %s on Day %d (%s, %d room(s)).",
                                 spName,
                                 safRef,
                                 itineraryDay.getDayNumber(),
                                 itineraryDay.getDestination() != null ? itineraryDay.getDestination() : "Circuit",
                                 request.getNumberOfrooms()),
                         com.saas.backend.models.Priority.HIGH,
                         "AccommodationRequirement",
                         accommodationRequirement.getId()
                 );
             }
         }
     } catch (Exception e) {
         log.warn("Could not dispatch requirement notification: {}", e.getMessage());
     }

     return  mapToResponse(accommodationRequirement);

        
    }


    @Override
    @Transactional
    public AccommodationRequirementResponse updateAccommodationRequirement(
            UUID requirementId,
            RequirementRequest request) {

        // 1. Find existing accommodation requirement
        AccommodationRequirement accommodationRequirement =
                accommodationRequirementRepository.findById(requirementId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Accommodation requirement not found"));

        // Tenant Security Validation
        if (accommodationRequirement.getSafari() != null &&
            accommodationRequirement.getSafari().getClient() != null &&
            accommodationRequirement.getSafari().getClient().getCompany() != null) {
            companyAccessValidator.validate(accommodationRequirement.getSafari().getClient().getCompany().getId());
        }

        // 2. Find price tier
        PriceTier priceTier =
                priceTierRepository.findById(request.getPricetierId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Price tier not found"));

        // 3. Find property category
        PropertyCategory propertyCategory =
                propertyCategoryRepository.findById(request.getCategoryId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Property category not found"));

        // 4. Update accommodation requirement
        accommodationRequirement.setNumberOfRooms(
                request.getNumberOfrooms()
        );

        accommodationRequirement.setRequiredCategory(
                propertyCategory
        );

        accommodationRequirement.setRequiredPriceTier(
                priceTier
        );

        accommodationRequirement.setRoomPreferences(
                request.getRoomPreferences()
        );

        int oldNights = accommodationRequirement.getNumberOfNights() != null ? accommodationRequirement.getNumberOfNights() : 1;
        if (request.getNumberOfNights() != null && request.getNumberOfNights() > 0) {
            accommodationRequirement.setNumberOfNights(request.getNumberOfNights());

            // Auto-synchronize Check-out date on any existing active booking for this requirement
            if (oldNights != request.getNumberOfNights()) {
                List<AccommodationBooking> bookings = accommodationBookingRepository.findByAccommodationRequirementId(requirementId);
                for (AccommodationBooking b : bookings) {
                    if (b.getStatus() != BookingStatus.CANCELLED && b.getCheckIn() != null) {
                        b.setCheckOut(b.getCheckIn().plusDays(request.getNumberOfNights()));
                        accommodationBookingRepository.save(b);
                    }
                }
            }
        }

        // 5. Remove old room requirements
        accommodationRequirement.getRooms().clear();

        // 6. Create updated room requirements
        for (RoomRequirementRequest roomRequest :
                request.getRoomRequirements()) {

            RoomType roomType =
                    roomTypeRepository
                            .findByNameIgnoreCase(
                                    roomRequest.getRoomType()
                            )
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Room type not found"));

            RoomRequirement room = new RoomRequirement();

            room.setRoomType(roomType);
            room.setQuantity(roomRequest.getQuantity());

            // Important: maintain both sides of relationship
            room.setAccommodationRequirement(
                    accommodationRequirement
            );

            accommodationRequirement.getRooms().add(room);
        }

        accommodationRequirementRepository.save(
                accommodationRequirement
        );
        return mapToResponse(accommodationRequirement);
    }



    @Override
    public AccommodationRequirementResponse getAccommodationRequirementById(UUID requirementId) {
        AccommodationRequirement requirement = accommodationRequirementRepository.findById(requirementId)
                .orElseThrow(() -> new ResourceNotFoundException("Accommodation requirement not found"));

        if (requirement.getSafari() != null) {
            UUID companyId = null;
            if (requirement.getSafari().getClient() != null && requirement.getSafari().getClient().getCompany() != null) {
                companyId = requirement.getSafari().getClient().getCompany().getId();
            } else if (requirement.getSafari().getSalesPerson() != null && requirement.getSafari().getSalesPerson().getCompany() != null) {
                companyId = requirement.getSafari().getSalesPerson().getCompany().getId();
            }
            if (companyId != null) {
                companyAccessValidator.validate(companyId);
            }
        }

        return mapToResponse(requirement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccommodationRequirementResponse> getAccommodationRequirementsBySafari(UUID safariId) {
        Safari safari = safariRepository.findById(safariId).orElse(null);
        if (safari != null && safari.getClient() != null && safari.getClient().getCompany() != null) {
            companyAccessValidator.validate(safari.getClient().getCompany().getId());
        }
        List<AccommodationRequirement> requirements = accommodationRequirementRepository.findBySafariId(safariId);
        return requirements.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AccommodationRequirementResponse getAccommodationRequirementByItineraryDay(UUID itineraryId) {
        AccommodationRequirement requirement = accommodationRequirementRepository.findByItineraryDayId(itineraryId)
                .orElse(null);
        return requirement != null ? mapToResponse(requirement) : null;
    }

    @Override
    @Transactional
    public void deleteAccommodationRequirement(UUID requirementId) {
        AccommodationRequirement accommodationRequirement = accommodationRequirementRepository.findById(requirementId)
                .orElseThrow(() -> new ResourceNotFoundException("Accommodation requirement not found"));

        if (accommodationRequirement.getSafari() != null && accommodationRequirement.getSafari().getClient() != null && accommodationRequirement.getSafari().getClient().getCompany() != null) {
            companyAccessValidator.validate(accommodationRequirement.getSafari().getClient().getCompany().getId());
        }

        List<com.saas.backend.models.AccommodationBooking> bookings = accommodationBookingRepository.findByAccommodationRequirementId(requirementId);
        com.saas.backend.models.AccommodationBooking active = bookings.stream()
                .filter(b -> b.getStatus() != com.saas.backend.models.BookingStatus.CANCELLED)
                .findFirst()
                .orElse(null);

        if (active != null) {
            throw new IllegalStateException("Security & Booking Policy: Cannot delete accommodation requirement with an active lodge booking (" + active.getReferenceNumber() + "). Please cancel the booking first.");
        }

        accommodationRequirementRepository.delete(accommodationRequirement);
    }

    private AccommodationRequirementResponse mapToResponse(AccommodationRequirement requirement) {
        if (requirement == null) return null;

        List<RoomRequirementResponse> rooms = requirement.getRooms() != null
                ? requirement.getRooms().stream()
                        .map(room -> new RoomRequirementResponse(
                                room.getRoomType() != null ? room.getRoomType().getName() : null,
                                room.getQuantity()))
                        .toList()
                : new ArrayList<>();

        return AccommodationRequirementResponse.builder()
                .id(requirement.getId())
                .safariId(requirement.getSafari() != null ? requirement.getSafari().getId() : null)
                .itineraryDayId(requirement.getItineraryDay() != null ? requirement.getItineraryDay().getId() : null)
                .itineraryDayDate(requirement.getItineraryDay() != null ? requirement.getItineraryDay().getDate() : null)
                .dayNumber(requirement.getItineraryDay() != null ? requirement.getItineraryDay().getDayNumber() : null)
                .destination(requirement.getDestination())
                .categoryId(requirement.getRequiredCategory() != null ? requirement.getRequiredCategory().getId() : null)
                .categoryName(requirement.getRequiredCategory() != null ? requirement.getRequiredCategory().getName() : null)
                .pricetierId(requirement.getRequiredPriceTier() != null ? requirement.getRequiredPriceTier().getId() : null)
                .priceTierName(requirement.getRequiredPriceTier() != null ? requirement.getRequiredPriceTier().getName() : null)
                .numberOfrooms(requirement.getNumberOfRooms())
                .numberOfNights(requirement.getNumberOfNights() != null ? requirement.getNumberOfNights() : 1)
                .roomRequirements(rooms)
                .roomPreferences(requirement.getRoomPreferences())
                .specialRequests(requirement.getSpecialRequests())
                .status(requirement.getAccomodationRequirmentStatus())
                .build();
    }
    
}
