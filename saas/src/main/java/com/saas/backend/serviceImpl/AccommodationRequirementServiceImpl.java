package com.saas.backend.serviceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.saas.backend.AccessHelper.CompanyAccessValidator;
import com.saas.backend.Exception.DuplicateException;
import com.saas.backend.Exception.ResourceNotFoundException;
import com.saas.backend.dto.RequirementRequest;
import com.saas.backend.dto.RoomRequirementRequest;
import com.saas.backend.models.AccommodationRequirement;
import com.saas.backend.models.AccomodationRequirmentStatus;
import com.saas.backend.models.ItineraryDay;
import com.saas.backend.models.PriceTier;
import com.saas.backend.models.PropertyCategory;
import com.saas.backend.models.RoomRequirement;
import com.saas.backend.models.RoomType;
import com.saas.backend.repositories.AccommodationRequirementRepository;
import com.saas.backend.repositories.ItineraryDayRepository;
import com.saas.backend.repositories.PriceTierRepository;
import com.saas.backend.repositories.PropertyCategoryRepository;
import com.saas.backend.repositories.RoomTypeRepository;
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

     // Notify reservation managers about this pending requirement
     try {
         User salesPerson = itineraryDay.getSafari() != null ? itineraryDay.getSafari().getSalesPerson() : null;
         String spName = salesPerson != null ? (salesPerson.getFirstName() + " " + (salesPerson.getLastName() != null ? salesPerson.getLastName() : "")) : "Sales Consultant";
         String safRef = itineraryDay.getSafari() != null ? itineraryDay.getSafari().getReferenceNumber() : "Safari File";

         List<User> reservationManagers = userRepository.findAll().stream()
                 .filter(u -> u.getRole() != null && u.getRole().getName() != null &&
                         (u.getRole().getName().toUpperCase().contains("RESERVATION") ||
                          u.getRole().getName().toUpperCase().contains("ADMIN")))
                 .toList();

         for (User rm : reservationManagers) {
             notificationService.createNotification(
                     rm,
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
     } catch (Exception e) {
         log.warn("Could not dispatch requirement notification: {}", e.getMessage());
     }

     return  mapToResponse(accommodationRequirement);

        
    }


    @Override
public AccommodationRequirementResponse updateAccommodationRequirement(
        UUID requirementId,
        RequirementRequest request) {

    // 1. Find existing accommodation requirement
    AccommodationRequirement accommodationRequirement =
            accommodationRequirementRepository.findById(requirementId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Accommodation requirement not found"));

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
    public List<AccommodationRequirementResponse> getAccommodationRequirementsBySafari(UUID safariId) {
        List<AccommodationRequirement> requirements = accommodationRequirementRepository.findBySafariId(safariId);
        return requirements.stream().map(this::mapToResponse).toList();
    }

    @Override
    public AccommodationRequirementResponse getAccommodationRequirementByItineraryDay(UUID itineraryId) {
        AccommodationRequirement requirement = accommodationRequirementRepository.findByItineraryDayId(itineraryId)
                .orElse(null);
        return requirement != null ? mapToResponse(requirement) : null;
    }

    @Override
    public void deleteAccommodationRequirement(UUID requirementId) {
        AccommodationRequirement accommodationRequirement = accommodationRequirementRepository.findById(requirementId)
                .orElseThrow(() -> new ResourceNotFoundException("Accommodation requirement not found"));

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
                .destination(requirement.getDestination())
                .categoryId(requirement.getRequiredCategory() != null ? requirement.getRequiredCategory().getId() : null)
                .categoryName(requirement.getRequiredCategory() != null ? requirement.getRequiredCategory().getName() : null)
                .pricetierId(requirement.getRequiredPriceTier() != null ? requirement.getRequiredPriceTier().getId() : null)
                .priceTierName(requirement.getRequiredPriceTier() != null ? requirement.getRequiredPriceTier().getName() : null)
                .numberOfrooms(requirement.getNumberOfRooms())
                .roomRequirements(rooms)
                .roomPreferences(requirement.getRoomPreferences())
                .specialRequests(requirement.getSpecialRequests())
                .status(requirement.getAccomodationRequirmentStatus())
                .build();
    }
    
}
