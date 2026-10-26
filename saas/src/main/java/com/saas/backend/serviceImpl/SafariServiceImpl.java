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
import com.saas.backend.dto.SafariRescheduleRequest;
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

import com.saas.backend.models.AccomodationRequirmentStatus;
import com.saas.backend.models.AccommodationBooking;
import com.saas.backend.models.AccommodationRequirement;
import com.saas.backend.models.BookingStatus;
import com.saas.backend.repositories.AccommodationBookingRepository;
import com.saas.backend.repositories.AccommodationRequirementRepository;

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
    private final AccommodationRequirementRepository accommodationRequirementRepository;
    private final AccommodationBookingRepository accommodationBookingRepository;

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

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            User currentUser = userRepository.findByEmail(auth.getName()).orElse(null);
            if (currentUser != null && currentUser.getRole() != null) {
                String roleName = currentUser.getRole().getName().toUpperCase().replace(" ", "_");
                if (roleName.contains("SALES")) {
                    if (client.getSalesPerson() != null && !client.getSalesPerson().getId().equals(currentUser.getId())) {
                        throw new SecurityException("Security & Access Policy: Sales consultants may only create safaris for their own clients.");
                    }
                }
            }
        }

        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("Safari start date and end date are required");
        }
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("Safari end date cannot be before start date");
        }

        // Auto-seed lead guest if client has no guests yet so operations can proceed immediately
        Long count = guestRepository.countByClientId(client.getId());
        if (count == null || count == 0) {
            com.saas.backend.models.Guest primaryGuest = com.saas.backend.models.Guest.builder()
                    .client(client)
                    .firstName(client.getFirstName())
                    .lastName(client.getLastName())
                    .gender(client.getGender())
                    .nationality(client.getNationality())
                    .build();
            guestRepository.save(primaryGuest);
        }

        int passengers = (request.getNumberOfPassengers() != null && request.getNumberOfPassengers() > 0)
                ? request.getNumberOfPassengers() : 1;
         
        Safari safari = new Safari();
        safari.setClient(client);

        User salesPerson = client.getSalesPerson();
        if (salesPerson == null) {
             auth = SecurityContextHolder.getContext().getAuthentication();
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
        safari.setNumberOfPassengers(passengers);
        safari.setStatus(SafariStatus.DRAFT);
        safari.setNotes(request.getNotes());

        safariRepository.save(safari);
        generateItineraryDay(safari);

        return mapToSafariResponse(safari);
    }

    private void generateItineraryDay(Safari safari) {
        if (safari.getStartDate() == null || safari.getEndDate() == null) return;

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

    private void validateSalesPersonSafariAccess(Safari safari) {
        if (safari == null) return;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return;
        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) return;

        String roleName = user.getRole() != null ? user.getRole().getName().toUpperCase().replace(" ", "_") : "";
        boolean isSalesPerson = roleName.contains("SALES");
        if (isSalesPerson) {
            if (safari.getSalesPerson() == null || !safari.getSalesPerson().getId().equals(user.getId())) {
                throw new SecurityException("Security & Access Policy: Sales consultants may only view or manage their own safaris.");
            }
        }
    }

    @Override
    public List<ItineraryDayResponse> getItineraryDaySafari(UUID safariId) {
        Safari safari = safariRepository.findById(safariId).orElseThrow(() -> new ResourceNotFoundException("Safari not found"));
        if (safari.getClient() != null && safari.getClient().getCompany() != null) {
            companyAccessValidator.validate(safari.getClient().getCompany().getId());
        }
        validateSalesPersonSafariAccess(safari);

        List<ItineraryDay> itineraryDays = itineraryDayRepository.findBySafariIdOrderByDayNumberAsc(safari.getId());

        // Self-heal: If days were never generated (e.g. legacy/seeded safaris), generate them automatically now
        if (itineraryDays.isEmpty() && safari.getStartDate() != null && safari.getEndDate() != null) {
            generateItineraryDay(safari);
            itineraryDays = itineraryDayRepository.findBySafariIdOrderByDayNumberAsc(safari.getId());
        }

        return itineraryDays.stream().map(this::mapToItineraryDayResponse).collect(Collectors.toList());
    }

    @Transactional 
    @Override
    public void updateSafariItenaryDay(UUID safariId, ItineraryUpdateRequest request) {
        Safari safari = safariRepository.findById(safariId).orElseThrow(() -> new ResourceNotFoundException("Safari not found"));
        if (safari.getClient() != null && safari.getClient().getCompany() != null) {
            companyAccessValidator.validate(safari.getClient().getCompany().getId());
        }
        validateSalesPersonSafariAccess(safari);

        for (ItineraryDayUpdateRequest dayRequest : request.getDays()) {
            ItineraryDay day = itineraryDayRepository.findByIdAndSafariId(dayRequest.getDayId(), safariId)
                    .orElseThrow(() -> new ResourceNotFoundException("Day not found"));

            day.setDestination(dayRequest.getDestination());
            day.setNotes(dayRequest.getNotes());
            itineraryDayRepository.save(day);
        }
        recalculateAndSaveSafariStatus(safari);
    } 

    @Transactional
    @Override
    public void deleteItineraryDay(UUID safariId, UUID dayId) {
        Safari safari = safariRepository.findById(safariId)
                .orElseThrow(() -> new ResourceNotFoundException("Safari not found"));
        if (safari.getClient() != null && safari.getClient().getCompany() != null) {
            companyAccessValidator.validate(safari.getClient().getCompany().getId());
        }
        validateSalesPersonSafariAccess(safari);

        ItineraryDay day = itineraryDayRepository
                .findByIdAndSafariId(dayId, safariId)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerary day not found"));

        List<AccommodationBooking> activeBookings = accommodationBookingRepository.findBySafariId(safariId)
                .stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .toList();

        // 1. Guard against deleting if a CONFIRMED booking is directly on this day
        boolean directConfirmed = activeBookings.stream().anyMatch(b ->
                b.getStatus() == BookingStatus.CONFIRMED &&
                b.getAccommodationRequirement() != null &&
                b.getAccommodationRequirement().getItineraryDay() != null &&
                b.getAccommodationRequirement().getItineraryDay().getId().equals(dayId)
        );
        if (directConfirmed) {
            throw new IllegalStateException("Cannot delete Day " + day.getDayNumber() +
                    ": A confirmed lodge booking exists on this day. Please cancel or reallocate the lodge booking first.");
        }

        // 2. Cleanly cancel draft/provisional bookings directly on this day
        List<AccommodationBooking> directDayBookings = activeBookings.stream()
                .filter(b -> b.getAccommodationRequirement() != null &&
                             b.getAccommodationRequirement().getItineraryDay() != null &&
                             b.getAccommodationRequirement().getItineraryDay().getId().equals(dayId))
                .toList();
        for (AccommodationBooking b : directDayBookings) {
            b.setStatus(BookingStatus.CANCELLED);
            b.setNotes((b.getNotes() != null ? b.getNotes() + " " : "") + "[Auto-cancelled due to Itinerary Day deletion]");
            accommodationBookingRepository.save(b);
        }

        // 3. Delete any requirement directly on this day
        accommodationRequirementRepository.findByItineraryDayId(dayId)
                .ifPresent(accommodationRequirementRepository::delete);

        // 4. Remove the day
        itineraryDayRepository.delete(day);

        // 5. Renumber remaining days sequentially and smoothly shift their dates
        List<ItineraryDay> days = itineraryDayRepository.findBySafariIdOrderByDayNumberAsc(safariId);
        int dayNumber = 1;
        LocalDate date = safari.getStartDate();

        for (ItineraryDay itineraryDay : days) {
            LocalDate oldDate = itineraryDay.getDate();
            itineraryDay.setDayNumber(dayNumber);
            itineraryDay.setDate(date);

            // Resynchronize checkIn/checkOut for requirements on shifted days
            if (oldDate != null && !oldDate.equals(date)) {
                long diff = java.time.temporal.ChronoUnit.DAYS.between(oldDate, date);
                accommodationRequirementRepository.findByItineraryDayId(itineraryDay.getId()).ifPresent(req -> {
                    List<AccommodationBooking> reqBookings = accommodationBookingRepository.findByAccommodationRequirementId(req.getId());
                    for (AccommodationBooking b : reqBookings) {
                        if (b.getStatus() != BookingStatus.CANCELLED) {
                            if (b.getCheckIn() != null) b.setCheckIn(b.getCheckIn().plusDays(diff));
                            if (b.getCheckOut() != null) b.setCheckOut(b.getCheckOut().plusDays(diff));
                            accommodationBookingRepository.save(b);
                        }
                    }
                });
            }

            dayNumber++;
            date = date.plusDays(1);
        }

        itineraryDayRepository.saveAll(days);

        if (!days.isEmpty()) {
            safari.setEndDate(date.minusDays(1));
        }
        safariRepository.save(safari);
        recalculateAndSaveSafariStatus(safari);
    }

    @Transactional
    @Override
    public SafariResponse rescheduleSafari(UUID safariId, SafariRescheduleRequest request) {
        if (request == null || request.getNewStartDate() == null) {
            throw new IllegalArgumentException("New safari start date is required");
        }

        Safari safari = safariRepository.findById(safariId)
                .orElseThrow(() -> new ResourceNotFoundException("Safari not found: " + safariId));

        if (safari.getClient() != null && safari.getClient().getCompany() != null) {
            companyAccessValidator.validate(safari.getClient().getCompany().getId());
        }
        validateSalesPersonSafariAccess(safari);

        LocalDate oldStart = safari.getStartDate();
        if (oldStart == null) {
            safari.setStartDate(request.getNewStartDate());
            if (safari.getEndDate() == null) {
                safari.setEndDate(request.getNewStartDate());
            }
            safari = safariRepository.save(safari);
            generateItineraryDay(safari);
            return mapToSafariResponse(safari);
        }

        long daysOffset = java.time.temporal.ChronoUnit.DAYS.between(oldStart, request.getNewStartDate());
        if (daysOffset == 0) {
            return mapToSafariResponse(safari);
        }

        // 1. Shift Safari start and end dates
        safari.setStartDate(request.getNewStartDate());
        if (safari.getEndDate() != null) {
            safari.setEndDate(safari.getEndDate().plusDays(daysOffset));
        }
        safari = safariRepository.save(safari);

        // 2. Shift all Itinerary Days
        List<ItineraryDay> days = itineraryDayRepository.findBySafariIdOrderByDayNumberAsc(safariId);
        for (ItineraryDay d : days) {
            if (d.getDate() != null) {
                d.setDate(d.getDate().plusDays(daysOffset));
            }
        }
        itineraryDayRepository.saveAll(days);

        // 3. Shift all Active Accommodation Bookings Check-In and Check-Out
        List<AccommodationBooking> bookings = accommodationBookingRepository.findBySafariId(safariId);
        for (AccommodationBooking b : bookings) {
            if (b.getStatus() == BookingStatus.CANCELLED) {
                continue;
            }
            if (b.getCheckIn() != null) {
                b.setCheckIn(b.getCheckIn().plusDays(daysOffset));
            }
            if (b.getCheckOut() != null) {
                b.setCheckOut(b.getCheckOut().plusDays(daysOffset));
            }
            String reasonText = (request.getReason() != null && !request.getReason().isBlank())
                    ? " Reason: " + request.getReason().trim() : "";
            String auditNote = String.format("[RESCHEDULED on %s by %d day(s).%s]",
                    LocalDate.now(), daysOffset, reasonText);
            b.setNotes(b.getNotes() != null && !b.getNotes().isBlank() ? b.getNotes() + " " + auditNote : auditNote);
            accommodationBookingRepository.save(b);
        }

        recalculateAndSaveSafariStatus(safari);
        return mapToSafariResponse(safari);
    }

    @Transactional
    @Override
    public void updateItineraryDay(UUID safariId, UUID dayId, ItineraryRequest request) {
        Safari safari = safariRepository.findById(safariId)
                .orElseThrow(() -> new ResourceNotFoundException("Safari not found"));
        if (safari.getClient() != null && safari.getClient().getCompany() != null) {
            companyAccessValidator.validate(safari.getClient().getCompany().getId());
        }
        validateSalesPersonSafariAccess(safari);

        ItineraryDay day = itineraryDayRepository
                .findByIdAndSafariId(dayId, safariId)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerary day not found"));

        // Data Consistency & ACID Protection:
        // 1. Prevent changing day date if active bookings exist for this day
        if (request.getDate() != null && !request.getDate().equals(day.getDate())) {
            boolean hasBooking = accommodationBookingRepository.findBySafariId(safariId).stream()
                    .filter(b -> b.getStatus() != com.saas.backend.models.BookingStatus.CANCELLED)
                    .anyMatch(b -> (b.getAccommodationRequirement() != null && b.getAccommodationRequirement().getItineraryDay() != null && b.getAccommodationRequirement().getItineraryDay().getId().equals(dayId))
                            || (b.getCheckIn() != null && b.getCheckIn().equals(day.getDate())));
            if (hasBooking) {
                throw new IllegalStateException("Security & Booking Policy: Cannot modify date for Day " + day.getDayNumber() +
                        ". An active lodge booking is already registered for this date. The booking must be rescheduled or cancelled first.");
            }
            day.setDate(request.getDate());
        }

        // 2. Sequential Route Policy: Cannot configure Day N destination if Day N-1 is not configured
        if (day.getDayNumber() > 1 && request.getDestination() != null && !request.getDestination().trim().isEmpty()) {
            ItineraryDay prevDay = itineraryDayRepository.findBySafariIdAndDayNumber(safariId, day.getDayNumber() - 1).orElse(null);
            if (prevDay != null && (prevDay.getDestination() == null || prevDay.getDestination().trim().isEmpty() || prevDay.getDestination().toLowerCase().contains("pending"))) {
                throw new IllegalStateException("Sequential Route Policy: Please configure Day " + prevDay.getDayNumber() +
                        " destination before configuring Day " + day.getDayNumber() + ".");
            }
        }

        if (request.getDestination() != null) {
            day.setDestination(request.getDestination());
        }
        if (request.getNotes() != null) {
            day.setNotes(request.getNotes());
        }

        itineraryDayRepository.save(day);
        recalculateAndSaveSafariStatus(safari);
    }

    @Transactional
    @Override
    public ItineraryDayResponse addItineraryDay(UUID safariId) {
        Safari safari = safariRepository.findById(safariId)
                .orElseThrow(() -> new ResourceNotFoundException("Safari not found"));
        if (safari.getClient() != null && safari.getClient().getCompany() != null) {
            companyAccessValidator.validate(safari.getClient().getCompany().getId());
        }
        validateSalesPersonSafariAccess(safari);

        if (safari.getStartDate() == null) {
            throw new IllegalArgumentException("Cannot add day: Safari start date is missing.");
        }

        List<ItineraryDay> existingDays = itineraryDayRepository.findBySafariIdOrderByDayNumberAsc(safariId);
        int nextDayNum = existingDays.size() + 1;
        LocalDate nextDate = existingDays.isEmpty()
                ? safari.getStartDate()
                : existingDays.get(existingDays.size() - 1).getDate().plusDays(1);

        ItineraryDay newDay = ItineraryDay.builder()
                .safari(safari)
                .dayNumber(nextDayNum)
                .date(nextDate)
                .destination(null)
                .build();

        newDay = itineraryDayRepository.save(newDay);

        // Safely extend safari end date to match the newly appended day
        safari.setEndDate(nextDate);
        safariRepository.save(safari);
        recalculateAndSaveSafariStatus(safari);

        return mapToItineraryDayResponse(newDay);
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

    @Transactional
    @Override
    public SafariStatus recalculateAndSaveSafariStatus(Safari safari) {
        if (safari == null) return null;
        if (safari.getStatus() == SafariStatus.CANCELLED) {
            return SafariStatus.CANCELLED;
        }

        List<ItineraryDay> days = itineraryDayRepository.findBySafariIdOrderByDayNumberAsc(safari.getId());
        if (days.isEmpty()) {
            safari.setStatus(SafariStatus.DRAFT);
            safariRepository.save(safari);
            return SafariStatus.DRAFT;
        }

        boolean allDestinationsSet = days.stream().allMatch(d -> 
            d.getDestination() != null 
            && !d.getDestination().trim().isEmpty() 
            && !d.getDestination().trim().equalsIgnoreCase("Destination not set")
            && !d.getDestination().trim().equalsIgnoreCase("Destination pending configuration")
        );

        if (!allDestinationsSet) {
            safari.setStatus(SafariStatus.DRAFT);
            safariRepository.save(safari);
            return SafariStatus.DRAFT;
        }

        // Check if all itinerary days have completed bookings
        List<AccommodationRequirement> requirements = accommodationRequirementRepository.findBySafariId(safari.getId());
        List<AccommodationBooking> bookings = accommodationBookingRepository.findBySafariId(safari.getId());

        boolean allDaysBooked = days.stream().allMatch(day -> {
            if (day.getDate() == null) return false;

            boolean reqCompleted = requirements.stream().anyMatch(r -> {
                if (r.getAccomodationRequirmentStatus() != AccomodationRequirmentStatus.COMPLETED) return false;
                if (r.getItineraryDay() != null && r.getItineraryDay().getId().equals(day.getId())) return true;
                if (r.getItineraryDay() != null && r.getItineraryDay().getDate() != null) {
                    int n = r.getNumberOfNights() != null && r.getNumberOfNights() > 0 ? r.getNumberOfNights() : 1;
                    LocalDate start = r.getItineraryDay().getDate();
                    LocalDate end = start.plusDays(n);
                    return !day.getDate().isBefore(start) && day.getDate().isBefore(end);
                }
                return false;
            });

            boolean bookingConfirmed = bookings.stream().anyMatch(b -> {
                if (b.getStatus() != BookingStatus.CONFIRMED) return false;
                if (b.getCheckIn() != null && b.getCheckOut() != null) {
                    return !day.getDate().isBefore(b.getCheckIn()) && day.getDate().isBefore(b.getCheckOut());
                }
                if (b.getAccommodationRequirement() != null && b.getAccommodationRequirement().getItineraryDay() != null) {
                    return b.getAccommodationRequirement().getItineraryDay().getId().equals(day.getId());
                }
                return b.getCheckIn() != null && b.getCheckIn().equals(day.getDate());
            });

            return reqCompleted || bookingConfirmed;
        });

        SafariStatus newStatus;
        if (allDaysBooked) {
            newStatus = SafariStatus.COMPLETED;
        } else {
            newStatus = SafariStatus.CONFIRMED;
        }

        safari.setStatus(newStatus);
        safariRepository.save(safari);
        return newStatus;
    }

    @Transactional
    @Override
    public List<ItineraryDayResponse> regenerateItineraryDays(UUID safariId) {
        Safari safari = safariRepository.findById(safariId)
                .orElseThrow(() -> new ResourceNotFoundException("Safari not found"));
        if (safari.getClient() != null && safari.getClient().getCompany() != null) {
            companyAccessValidator.validate(safari.getClient().getCompany().getId());
        }
        validateSalesPersonSafariAccess(safari);

        if (safari.getStartDate() == null || safari.getEndDate() == null) {
            throw new IllegalArgumentException("Cannot generate itinerary days: Safari start date and end date are required");
        }

        List<ItineraryDay> existingDays = itineraryDayRepository.findBySafariIdOrderByDayNumberAsc(safari.getId());
        if (existingDays.isEmpty()) {
            generateItineraryDay(safari);
        } else {
            // Fill in any missing dates from start to end date
            LocalDate cur = safari.getStartDate();
            int expectedDayNum = 1;
            while (!cur.isAfter(safari.getEndDate())) {
                final LocalDate dayDate = cur;
                boolean exists = existingDays.stream().anyMatch(d -> d.getDate() != null && d.getDate().equals(dayDate));
                if (!exists) {
                    ItineraryDay newDay = ItineraryDay.builder()
                            .safari(safari)
                            .dayNumber(expectedDayNum)
                            .date(dayDate)
                            .destination(null)
                            .build();
                    itineraryDayRepository.save(newDay);
                }
                cur = cur.plusDays(1);
                expectedDayNum++;
            }

            // Renumber all days sequentially
            List<ItineraryDay> allDays = itineraryDayRepository.findBySafariIdOrderByDayNumberAsc(safari.getId());
            int num = 1;
            for (ItineraryDay d : allDays) {
                d.setDayNumber(num++);
                itineraryDayRepository.save(d);
            }
        }

        recalculateAndSaveSafariStatus(safari);
        List<ItineraryDay> finalDays = itineraryDayRepository.findBySafariIdOrderByDayNumberAsc(safari.getId());
        return finalDays.stream().map(this::mapToItineraryDayResponse).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public SafariResponse updateSafariStatus(UUID safariId, SafariStatus requestedStatus) {
        Safari safari = safariRepository.findById(safariId)
                .orElseThrow(() -> new ResourceNotFoundException("Safari not found"));
        if (safari.getClient() != null && safari.getClient().getCompany() != null) {
            companyAccessValidator.validate(safari.getClient().getCompany().getId());
        }
        validateSalesPersonSafariAccess(safari);

        List<ItineraryDay> days = itineraryDayRepository.findBySafariIdOrderByDayNumberAsc(safari.getId());

        if (requestedStatus == SafariStatus.CONFIRMED) {
            if (days.isEmpty()) {
                throw new IllegalArgumentException("Cannot confirm safari: No itinerary days exist.");
            }
            boolean allDestinationsSet = days.stream().allMatch(d -> 
                d.getDestination() != null 
                && !d.getDestination().trim().isEmpty() 
                && !d.getDestination().trim().equalsIgnoreCase("Destination not set")
                && !d.getDestination().trim().equalsIgnoreCase("Destination pending configuration")
            );
            if (!allDestinationsSet) {
                throw new IllegalArgumentException("Cannot confirm safari: All itinerary days must have destinations configured.");
            }
            safari.setStatus(SafariStatus.CONFIRMED);
        } else if (requestedStatus == SafariStatus.COMPLETED) {
            if (days.isEmpty()) {
                throw new IllegalArgumentException("Cannot complete safari: No itinerary days exist.");
            }
            boolean allDestinationsSet = days.stream().allMatch(d -> 
                d.getDestination() != null 
                && !d.getDestination().trim().isEmpty() 
                && !d.getDestination().trim().equalsIgnoreCase("Destination not set")
                && !d.getDestination().trim().equalsIgnoreCase("Destination pending configuration")
            );
            if (!allDestinationsSet) {
                throw new IllegalArgumentException("Cannot complete safari: All itinerary days must have destinations configured.");
            }

            List<AccommodationRequirement> requirements = accommodationRequirementRepository.findBySafariId(safari.getId());
            List<AccommodationBooking> bookings = accommodationBookingRepository.findBySafariId(safari.getId());

            boolean allDaysBooked = days.stream().allMatch(day -> {
                if (day.getDate() == null) return false;

                boolean reqCompleted = requirements.stream().anyMatch(r -> {
                    if (r.getAccomodationRequirmentStatus() != AccomodationRequirmentStatus.COMPLETED) return false;
                    if (r.getItineraryDay() != null && r.getItineraryDay().getId().equals(day.getId())) return true;
                    if (r.getItineraryDay() != null && r.getItineraryDay().getDate() != null) {
                        int n = r.getNumberOfNights() != null && r.getNumberOfNights() > 0 ? r.getNumberOfNights() : 1;
                        LocalDate start = r.getItineraryDay().getDate();
                        LocalDate end = start.plusDays(n);
                        return !day.getDate().isBefore(start) && day.getDate().isBefore(end);
                    }
                    return false;
                });

                boolean bookingConfirmed = bookings.stream().anyMatch(b -> {
                    if (b.getStatus() != BookingStatus.CONFIRMED) return false;
                    if (b.getCheckIn() != null && b.getCheckOut() != null) {
                        return !day.getDate().isBefore(b.getCheckIn()) && day.getDate().isBefore(b.getCheckOut());
                    }
                    if (b.getAccommodationRequirement() != null && b.getAccommodationRequirement().getItineraryDay() != null) {
                        return b.getAccommodationRequirement().getItineraryDay().getId().equals(day.getId());
                    }
                    return b.getCheckIn() != null && b.getCheckIn().equals(day.getDate());
                });

                return reqCompleted || bookingConfirmed;
            });

            if (!allDaysBooked) {
                throw new IllegalArgumentException("Cannot complete safari: All itinerary days must have confirmed accommodation bookings.");
            }
            safari.setStatus(SafariStatus.COMPLETED);
        } else {
            safari.setStatus(requestedStatus);
        }

        safariRepository.save(safari);
        return mapToSafariResponse(safari);
    }
}
