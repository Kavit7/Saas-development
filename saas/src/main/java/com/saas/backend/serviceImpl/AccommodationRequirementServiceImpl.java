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

import lombok.RequiredArgsConstructor;



@Service 
@RequiredArgsConstructor 
public class AccommodationRequirementServiceImpl implements AccommodationRequirementService {
    private final ItineraryDayRepository itineraryDayRepository;
    private final PropertyCategoryRepository propertyCategoryRepository;
    private final PriceTierRepository priceTierRepository;
    private final AccommodationRequirementRepository accommodationRequirementRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final CompanyAccessValidator companyAccessValidator;

    @Override
    public AccommodationRequirementResponse createAccommodationRequirement(UUID itineraryId,RequirementRequest request) {
     ItineraryDay itineraryDay= itineraryDayRepository.findById(itineraryId).orElseThrow(()-> new ResourceNotFoundException("Day Not found"));

     boolean exists= accommodationRequirementRepository.existsByItineraryDay(itineraryDay);
     if (exists){
        throw new DuplicateException("requirement fot this day already exists");
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
     accommodationRequirementRepository.save(accommodationRequirement);

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
public AccommodationRequirementResponse getAccommodationRequirementById(
        UUID requirementId) {

  AccommodationRequirement requirement=  accommodationRequirementRepository.findById(requirementId)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Accommodation requirement not found"));

        companyAccessValidator.validate(requirement.getSafari().getSalesPerson().getCompany().getId());


return mapToResponse(requirement);
}


// this will list all requirement for a certain company only not for all
// @Override 
// public AccommodationRequirementResponse getAllAccommodationRequirement(){

// }




@Override
public void deleteAccommodationRequirement(UUID requirementId) {

    AccommodationRequirement accommodationRequirement =
            accommodationRequirementRepository.findById(requirementId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Accommodation requirement not found"));

    accommodationRequirementRepository.delete(
            accommodationRequirement
    );
}


private AccommodationRequirementResponse mapToResponse(AccommodationRequirement requirement){

 List<RoomRequirementResponse> rooms= requirement.getRooms().stream().map(room-> new RoomRequirementResponse(room.getRoomType().getName(),room.getQuantity())).toList();

 return new AccommodationRequirementResponse(requirement.getRequiredCategory().getId(),requirement.getRequiredCategory().getName(),requirement.getRequiredPriceTier().getId() , requirement.getRequiredPriceTier().getName(), requirement.getNumberOfRooms(), rooms, requirement.getRoomPreferences(), requirement.getSpecialRequests(),requirement.getAccomodationRequirmentStatus());

}
    
}
