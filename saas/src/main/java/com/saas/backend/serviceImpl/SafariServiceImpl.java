package com.saas.backend.serviceImpl;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.backend.AccessHelper.CompanyAccessValidator;
import com.saas.backend.Exception.ResourceNotFoundException;
import com.saas.backend.dto.ItineraryDayUpdateRequest;
import com.saas.backend.dto.ItineraryRequest;
import com.saas.backend.dto.ItineraryUpdateRequest;
import com.saas.backend.dto.SafariRequest;
import com.saas.backend.models.Client;
import com.saas.backend.models.ItineraryDay;
import com.saas.backend.models.Safari;
import com.saas.backend.models.SafariStatus;
import com.saas.backend.models.User;
import com.saas.backend.repositories.ClientRepository;
import com.saas.backend.repositories.GuestRepository;
import com.saas.backend.repositories.ItineraryDayRepository;
import com.saas.backend.repositories.SafariRepository;
import com.saas.backend.repositories.UserRepository;
import com.saas.backend.response.ItineraryDayResponse;
import com.saas.backend.response.SafariResponse;
import com.saas.backend.service.SafariService;
import com.saas.backend.specification.SafariSpecification;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class SafariServiceImpl implements SafariService {
    private final ClientRepository clientRepository;
    private final SafariRepository safariRepository;
    private final CompanyAccessValidator companyAccessValidator;
    private final ItineraryDayRepository itineraryDayRepository;
    private final GuestRepository guestRepository;
    private final UserRepository userRepository;

    private SafariResponse mapToSafariResponse(Safari safari) {
        if (safari == null) return null;
        return SafariResponse.builder()
                .id(safari.getId())
                .clientId(safari.getClient() != null ? safari.getClient().getId() : null)
                .clientName(safari.getClient() != null ? (safari.getClient().getFirstName() + " " + safari.getClient().getLastName()) : null)
                .salesPersonId(safari.getSalesPerson() != null ? safari.getSalesPerson().getId() : null)
                .salesPersonName(safari.getSalesPerson() != null ? (safari.getSalesPerson().getFirstName() + " " + safari.getSalesPerson().getLastName()) : null)
                .referenceNumber(safari.getReferenceNumber())
                .startDate(safari.getStartDate())
                .endDate(safari.getEndDate())
                .numberOfPassengers(safari.getNumberOfPassengers())
                .status(safari.getStatus())
                .notes(safari.getNotes())
                .createdAt(safari.getCreatedAt())
                .updatedAt(safari.getUpdatedAt())
                .build();
    }

    private ItineraryDayResponse mapToItineraryDayResponse(ItineraryDay day) {
        if (day == null) return null;
        return ItineraryDayResponse.builder()
                .id(day.getId())
                .safariId(day.getSafari() != null ? day.getSafari().getId() : null)
                .dayNumber(day.getDayNumber())
                .date(day.getDate())
                .destination(day.getDestination())
                .notes(day.getNotes())
                .createdAt(day.getCreatedAt())
                .updatedAt(day.getUpdatedAt())
                .build();
    }

    @Transactional 
    @Override
    public SafariResponse createClientSafari(UUID clientId, SafariRequest request) {
        Client client = clientRepository.findById(clientId).orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        companyAccessValidator.validate(client.getCompany().getId());
          
        Long count = guestRepository.countByClientId(client.getId());

        if (count == 0) {
            throw new ResourceNotFoundException("this client has no guests");
        }
        if (request.getNumberOfPassengers() < 1 || request.getNumberOfPassengers() > count) {
            throw new ResourceNotFoundException("Number of passenger must range between 1 and " + count);
        }
         
        Safari safari = new Safari();
        safari.setClient(client);

        User salesPerson = client.getSalesPerson();
        if (salesPerson == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getName().equalsIgnoreCase("anonymousUser")) {
                salesPerson = userRepository.findByEmail(auth.getName()).orElse(null);
                if (salesPerson != null && client.getSalesPerson() == null) {
                    client.setSalesPerson(salesPerson);
                    clientRepository.save(client);
                }
            }
        }
        safari.setSalesPerson(salesPerson);
        safari.setStartDate(request.getStartDate());
        safari.setEndDate(request.getEndDate());
        safari.setReferenceNumber(generateReference());
        safari.setNumberOfPassengers(request.getNumberOfPassengers());
        safari.setStatus(SafariStatus.DRAFT);
        safari.setNotes(request.getNotes());

        safariRepository.save(safari);
        generateItineraryDay(safari);

        return mapToSafariResponse(safari);
    }

    private void generateItineraryDay(Safari safari) {
        int dayNumber = 1;
        LocalDate date = safari.getStartDate();

        while (!date.isAfter(safari.getEndDate())) {
            ItineraryDay day = ItineraryDay.builder()
                    .safari(safari)
                    .dayNumber(dayNumber)
                    .date(date)
                    .destination(null)
                    .build();
            itineraryDayRepository.save(day);
            date = date.plusDays(1);
            dayNumber++;
        }
    }

    private String generateReference() {
        String characters = "ABCDEFGHIJKLMNPQRSTUVWXYZ123456789";
        StringBuilder code = new StringBuilder("SAF-");
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 7; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        return code.toString();
    }

    @Override
    public List<ItineraryDayResponse> getItineraryDaySafari(UUID safariId) {
        Safari safari = safariRepository.findById(safariId).orElseThrow(() -> new ResourceNotFoundException("Safari not found"));
        List<ItineraryDay> itineraryDay = itineraryDayRepository.findBySafariId(safari.getId());
        return itineraryDay.stream().map(this::mapToItineraryDayResponse).collect(Collectors.toList());
    }

    @Transactional 
    @Override
    public void updateSafariItenaryDay(UUID safariId, ItineraryUpdateRequest request) {
        safariRepository.findById(safariId).orElseThrow(() -> new ResourceNotFoundException("Safari not found"));

        for (ItineraryDayUpdateRequest dayRequest : request.getDays()) {
            ItineraryDay day = itineraryDayRepository.findByIdAndSafariId(dayRequest.getDayId(), safariId)
                    .orElseThrow(() -> new ResourceNotFoundException("Day not found"));

            day.setDestination(dayRequest.getDestination());
            day.setNotes(dayRequest.getNotes());
            itineraryDayRepository.save(day);
        }
    } 

    @Transactional
    @Override
    public void deleteItineraryDay(UUID safariId, UUID dayId) {
        Safari safari = safariRepository.findById(safariId)
                .orElseThrow(() -> new ResourceNotFoundException("Safari not found"));

        ItineraryDay day = itineraryDayRepository
                .findByIdAndSafariId(dayId, safariId)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerary day not found"));

        itineraryDayRepository.delete(day);

        List<ItineraryDay> days =
                itineraryDayRepository.findBySafariIdOrderByDayNumberAsc(safariId);

        int dayNumber = 1;
        LocalDate date = safari.getStartDate();

        for (ItineraryDay itineraryDay : days) {
            itineraryDay.setDayNumber(dayNumber);
            itineraryDay.setDate(date);

            dayNumber++;
            date = date.plusDays(1);
        }

        itineraryDayRepository.saveAll(days);

        safari.setEndDate(date.minusDays(1));
        safariRepository.save(safari);
    }

    @Transactional
    @Override
    public void updateItineraryDay(UUID safariId, UUID dayId, ItineraryRequest request) {
        safariRepository.findById(safariId)
                .orElseThrow(() -> new ResourceNotFoundException("Safari not found"));

        ItineraryDay day = itineraryDayRepository
                .findByIdAndSafariId(dayId, safariId)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerary day not found"));

        if (request.getDestination() != null) {
            day.setDestination(request.getDestination());
        }
        if (request.getNotes() != null) {
            day.setNotes(request.getNotes());
        }
        if (request.getDate() != null) {
            day.setDate(request.getDate());
        }

        itineraryDayRepository.save(day);
    }

    @Override
    public Page<SafariResponse> getAllSafari(int page, int size, String sortBy, String search, String direction, SafariStatus status, LocalDate startDate, LocalDate endDate) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResourceNotFoundException("User not authenticated");
        }

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending(); 

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Safari> specification;
        boolean isSalesPerson = user.getRole() != null &&
                user.getRole().getName().toUpperCase().replace(" ", "_").contains("SALES_PERSON");

        if (isSalesPerson) {
            specification = SafariSpecification.hasSalesPerson(user.getId());
        } else if (user.getCompany() != null) {
            specification = SafariSpecification.hasCompany(user.getCompany().getId());
        } else {
            specification = (root, query, cb) -> cb.conjunction();
        }

        if (search != null && !search.isBlank()) {
            specification = specification.and(SafariSpecification.hasSearch(search));
        }

        if (status != null) {
            specification = specification.and(SafariSpecification.hasStatus(status));
        }

        if (startDate != null) {
            specification = specification.and(SafariSpecification.hasStartDate(startDate));
        }
         
        if (endDate != null) {
            specification = specification.and(SafariSpecification.hasEndDate(endDate));
        }

        return safariRepository.findAll(specification, pageable).map(this::mapToSafariResponse);
    }
}
