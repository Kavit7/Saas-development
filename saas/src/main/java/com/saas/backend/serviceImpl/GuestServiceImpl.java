package com.saas.backend.serviceImpl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.saas.backend.AccessHelper.CompanyAccessValidator;
import com.saas.backend.dto.GuestRequest;
import com.saas.backend.dto.GuestRequirmentRequest;
import com.saas.backend.dto.OccasionRequest;
import com.saas.backend.models.Client;
import com.saas.backend.models.Guest;
import com.saas.backend.models.GuestRequirement;
import com.saas.backend.models.SpecialOccasion;
import com.saas.backend.models.User;
import com.saas.backend.repositories.ClientRepository;
import com.saas.backend.repositories.GuestRepository;
import com.saas.backend.repositories.GuestRequirementRepository;
import com.saas.backend.repositories.SpecialOccasionRepository;
import com.saas.backend.repositories.UserRepository;
import com.saas.backend.response.GuestRequirementResponse;
import com.saas.backend.response.GuestResponse;
import com.saas.backend.response.SpecialOccasionResponse;
import com.saas.backend.service.GuestService;
import com.saas.backend.service.OccasionService;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class GuestServiceImpl implements GuestService, OccasionService {
    private final ClientRepository clientRepository;
    private final GuestRepository guestRepository;
    private final GuestRequirementRepository guestRequirementRepository;
    private final UserRepository userRepository;
    private final SpecialOccasionRepository specialOccasionRepository;
    private final CompanyAccessValidator companyAccessValidator;

    private GuestResponse mapToGuestResponse(Guest guest) {
        if (guest == null) return null;
        return GuestResponse.builder()
                .id(guest.getId())
                .clientId(guest.getClient() != null ? guest.getClient().getId() : null)
                .firstName(guest.getFirstName())
                .lastName(guest.getLastName())
                .dateOfBirth(guest.getDateOfBirth())
                .nationality(guest.getNationality())
                .passportNumber(guest.getPassportNumber())
                .passportExpiry(guest.getPassportExpiry())
                .gender(guest.getGender())
                .createdAt(guest.getCreatedAt())
                .updatedAt(guest.getUpdatedAt())
                .build();
    }

    private GuestRequirementResponse mapToGuestRequirementResponse(GuestRequirement req) {
        if (req == null) return null;
        return GuestRequirementResponse.builder()
                .id(req.getId())
                .guestId(req.getGuest() != null ? req.getGuest().getId() : null)
                .guestName(req.getGuest() != null ? (req.getGuest().getFirstName() + " " + req.getGuest().getLastName()) : null)
                .requirementType(req.getRequirementType())
                .requirementValue(req.getRequirementValue())
                .severity(req.getSeverity())
                .notes(req.getNotes())
                .createdBy(req.getCreatedBy() != null ? req.getCreatedBy().getEmail() : null)
                .createdAt(req.getCreatedAt())
                .updatedAt(req.getUpdatedAt())
                .build();
    }

    private SpecialOccasionResponse mapToSpecialOccasionResponse(SpecialOccasion occasion) {
        if (occasion == null) return null;
        return SpecialOccasionResponse.builder()
                .id(occasion.getId())
                .clientId(occasion.getClient() != null ? occasion.getClient().getId() : null)
                .guestId(occasion.getGuest() != null ? occasion.getGuest().getId() : null)
                .guestName(occasion.getGuest() != null ? (occasion.getGuest().getFirstName() + " " + occasion.getGuest().getLastName()) : null)
                .occasionType(occasion.getOccasionType())
                .occasionDate(occasion.getOccasionDate())
                .notes(occasion.getNotes())
                .createdAt(occasion.getCreatedAt())
                .updatedAt(occasion.getUpdatedAt())
                .build();
    }

    @Override
    public GuestResponse createGuest(UUID id, GuestRequest request) {
        try {
            Client client = clientRepository.findById(id).orElseThrow(() -> new RuntimeException("Client Not Found"));
            companyAccessValidator.validate(client.getCompany().getId());
            if (guestRepository.existsByClient_IdAndFirstNameAndLastName(client.getId(), request.getFirstName(), request.getLastName())) {
                throw new RuntimeException("Sorry we can't duplicate the same guest to the same client");
            }
            Guest guest = new Guest();
            guest.setClient(client);
            guest.setFirstName(request.getFirstName());
            guest.setLastName(request.getLastName());
            guest.setDateOfBirth(request.getDateOfBirth());
            guest.setGender(request.getGender());
            guest.setNationality(request.getNationality());
            guest.setPassportExpiry(request.getPassportExpiry());
            guest.setPassportNumber(request.getPassportNumber());
            guestRepository.save(guest);
            return mapToGuestResponse(guest);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public GuestResponse editGuest(UUID id, GuestRequest request) {
        try {
            Guest guest = guestRepository.findById(id).orElseThrow(() -> new RuntimeException("No guest found")); 
            companyAccessValidator.validate(guest.getClient().getCompany().getId());       
            if (request.getFirstName() != null) guest.setFirstName(request.getFirstName());
            if (request.getLastName() != null) guest.setLastName(request.getLastName());
            if (request.getDateOfBirth() != null) guest.setDateOfBirth(request.getDateOfBirth());
            if (request.getGender() != null) guest.setGender(request.getGender());
            if (request.getNationality() != null) guest.setNationality(request.getNationality());
            if (request.getPassportExpiry() != null) guest.setPassportExpiry(request.getPassportExpiry());
            if (request.getPassportNumber() != null) guest.setPassportNumber(request.getPassportNumber());
            guestRepository.save(guest);
            return mapToGuestResponse(guest);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void deleteGuest(UUID id) {
        Guest guest = guestRepository.findById(id).orElseThrow(() -> new RuntimeException("No guest found")); 
        companyAccessValidator.validate(guest.getClient().getCompany().getId());
        guestRepository.delete(guest);
    }

    @Override
    public GuestRequirementResponse createGuestRequirment(UUID guestId, GuestRequirmentRequest request, Authentication auth) {
        try {
            Guest guest = guestRepository.findById(guestId).orElseThrow(() -> new RuntimeException("No guest found"));
            companyAccessValidator.validate(guest.getClient().getCompany().getId());
            User user = userRepository.findByEmail(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
            boolean exists = guestRequirementRepository.existsByGuestIdAndRequirementType(guestId, request.getRequirementType());
            if (exists) {
                throw new RuntimeException("This RequirementType already exist to this Guest");
            }
            GuestRequirement guestRequirement = new GuestRequirement();
            guestRequirement.setGuest(guest);
            guestRequirement.setRequirementType(request.getRequirementType());
            guestRequirement.setRequirementValue(request.getRequirementValue());
            guestRequirement.setSeverity(request.getSeverity());
            guestRequirement.setCreatedBy(user);
            guestRequirement.setNotes(request.getNotes());

            guestRequirementRepository.save(guestRequirement);
            return mapToGuestRequirementResponse(guestRequirement);
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    public List<GuestRequirementResponse> getGuestRequirement(UUID guestId) {
        try {
            Guest guest = guestRepository.findById(guestId).orElseThrow(() -> new RuntimeException("No guest found"));
            companyAccessValidator.validate(guest.getClient().getCompany().getId());
            List<GuestRequirement> guestRequirement = guestRequirementRepository.findAllByGuestId(guestId);

            if (guestRequirement.isEmpty()) {
                throw new RuntimeException("No requirment found to this guest");
            }

            return guestRequirement.stream().map(this::mapToGuestRequirementResponse).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    public GuestRequirementResponse updateGuestRequirement(UUID reqId, GuestRequirmentRequest request) {
        try {
            GuestRequirement guestRequirement = guestRequirementRepository.findById(reqId).orElseThrow(() -> new RuntimeException("No requirment found with this id"));
            companyAccessValidator.validate(guestRequirement.getGuest().getClient().getCompany().getId());
            if (request.getRequirementType() != null) {
                if (!(guestRequirement.getRequirementType().equals(request.getRequirementType()))) {
                    throw new RuntimeException("You can't change the Requirement Type please do not Update it!!");
                }
                guestRequirement.setRequirementType(request.getRequirementType());
            }
            if (request.getRequirementValue() != null) guestRequirement.setRequirementValue(request.getRequirementValue());
            if (request.getSeverity() != null) guestRequirement.setSeverity(request.getSeverity());
            if (request.getNotes() != null) guestRequirement.setNotes(request.getNotes());
            guestRequirementRepository.save(guestRequirement);
            return mapToGuestRequirementResponse(guestRequirement);
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    public void deleteGuestRequirement(UUID reqId) {
        try {
            GuestRequirement guestRequirement = guestRequirementRepository.findById(reqId).orElseThrow(() -> new RuntimeException("No requirment found with this id"));
            companyAccessValidator.validate(guestRequirement.getGuest().getClient().getCompany().getId());
            guestRequirementRepository.delete(guestRequirement);
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    public SpecialOccasionResponse createGuestOccassion(UUID guestId, OccasionRequest request) {
        try {
            Guest guest = guestRepository.findById(guestId).orElseThrow(() -> new RuntimeException("No guest found"));
            companyAccessValidator.validate(guest.getClient().getCompany().getId());
            SpecialOccasion specialOccasion = new SpecialOccasion();
            specialOccasion.setClient(guest.getClient());
            specialOccasion.setGuest(guest);
            specialOccasion.setOccasionType(request.getOccasionType());
            specialOccasion.setOccasionDate(request.getOccassionDate());
            specialOccasion.setNotes(request.getNotes());

            specialOccasionRepository.save(specialOccasion);
            return mapToSpecialOccasionResponse(specialOccasion);
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    public SpecialOccasionResponse updateGuestOccasion(UUID guestId, OccasionRequest request) {
        try {
            Guest guest = guestRepository.findById(guestId).orElseThrow(() -> new RuntimeException("No guest found"));
            companyAccessValidator.validate(guest.getClient().getCompany().getId());
            SpecialOccasion specialOccasion = specialOccasionRepository.findByGuestId(guestId);
            if (request.getOccasionType() != null) specialOccasion.setOccasionType(request.getOccasionType());
            if (request.getOccassionDate() != null) specialOccasion.setOccasionDate(request.getOccassionDate());
            if (request.getNotes() != null) specialOccasion.setNotes(request.getNotes());

            specialOccasionRepository.save(specialOccasion);
            return mapToSpecialOccasionResponse(specialOccasion);
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    public List<SpecialOccasionResponse> getGuestSpecialOcassion(UUID guestId) {
        try {    
            Guest guest = guestRepository.findById(guestId).orElseThrow(() -> new RuntimeException("No guest found"));
            companyAccessValidator.validate(guest.getClient().getCompany().getId());
            List<SpecialOccasion> occasions = specialOccasionRepository.findAllByGuestId(guestId);
            if (occasions.isEmpty()) {
                throw new RuntimeException("No occassion found");
            }
            return occasions.stream().map(this::mapToSpecialOccasionResponse).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    public void deleteGuestOccassion(UUID occassionId) {
        try {
            SpecialOccasion specialOccasion = specialOccasionRepository.findById(occassionId).orElseThrow(() -> new RuntimeException("Occassion not found"));
            companyAccessValidator.validate(specialOccasion.getClient().getCompany().getId());
            specialOccasionRepository.delete(specialOccasion);
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }
}