package com.saas.backend.serviceImpl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.saas.backend.AccessHelper.CompanyAccessValidator;
import com.saas.backend.dto.ClientRequest;
import com.saas.backend.dto.ClientUpdate;
import com.saas.backend.models.Client;
import com.saas.backend.models.ClientStatus;
import com.saas.backend.models.Company;
import com.saas.backend.models.Guest;
import com.saas.backend.models.User;
import com.saas.backend.repositories.ClientRepository;
import com.saas.backend.repositories.CompanyRepository;
import com.saas.backend.repositories.GuestRepository;
import com.saas.backend.repositories.UserRepository;
import com.saas.backend.response.ClientResponse;
import com.saas.backend.response.GuestResponse;
import com.saas.backend.service.ClientService;
import com.saas.backend.specification.ClientSpecification;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class ClientServiceImpl implements ClientService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final ClientRepository clientRepository;
    private final GuestRepository guestRepository;
    private final CompanyAccessValidator companyAccessValidator;

    public ClientResponse mapToResponse(Client client) {
        if (client == null) return null;
        return ClientResponse.builder()
                .id(client.getId())
                .firstName(client.getFirstName())
                .lastName(client.getLastName())
                .gender(client.getGender())
                .email(client.getEmail())
                .phone(client.getPhone())
                .countryOfResidence(client.getCountryOfResidence())
                .nationality(client.getNationality())
                .preferredLanguage(client.getPreferredLanguage())
                .notes(client.getNotes())
                .status(client.getStatus())
                .salesPersonId(client.getSalesPerson() != null ? client.getSalesPerson().getId() : null)
                .salePeson(client.getSalesPerson() != null ? client.getSalesPerson().getEmail() : null)
                .companyId(client.getCompany() != null ? client.getCompany().getId() : null)
                .companyName(client.getCompany() != null ? client.getCompany().getName() : null)
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt())
                .build();
    }

    public GuestResponse mapGuestToResponse(Guest guest) {
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

    @Override
    public ClientResponse createClient(UUID salesPersonId, ClientRequest request) {
        try {
            User user = userRepository.findById(salesPersonId)
                    .orElseThrow(() -> new RuntimeException("The id is not valid for the sale person"));

            if (user.getRole().getName().equalsIgnoreCase("ADMIN")) {
                throw new RuntimeException("Admin can't add Client to its id please select Sales person");
            }

            Company company = companyRepository.findById(user.getCompany().getId())
                    .orElseThrow(() -> new RuntimeException("Company not found"));

            if (clientRepository.existsByCompanyIdAndEmail(company.getId(), request.getEmail())) {
                throw new RuntimeException("Clients with this email already exists");
            }

            Client client = new Client();
            client.setCompany(company);
            client.setSalesPerson(user);
            client.setCountryOfResidence(request.getCountryOfResidence());
            client.setEmail(request.getEmail());
            client.setPhone(request.getPhone());
            client.setPreferredLanguage(request.getPreferredLanguage());
            client.setFirstName(request.getFirstName());
            client.setNotes(request.getNotes());
            client.setLastName(request.getLastName());
            client.setGender(request.getGender());
            client.setNationality(request.getNationality());
            client.setStatus(request.getStatus());

            clientRepository.save(client);
            return mapToResponse(client);  
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Page<ClientResponse> getClients(Authentication auth, int page, int size, String sortBy, String search, String direction, ClientStatus status) {
        try {
            Sort sort = direction.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
               
            Pageable pageable = PageRequest.of(page, size, sort);
            Specification<Client> specification;
              
            User user = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("No user found with this email"));

            String roleName = user.getRole() != null ? user.getRole().getName().toUpperCase().replace(" ", "_") : "";
            boolean isSuperAdmin = roleName.contains("SUPER_ADMIN");
            boolean isSalesPerson = roleName.contains("SALES_PERSON");

            if (isSuperAdmin) {
                specification = (root, query, cb) -> cb.conjunction();
            } else if (isSalesPerson && user.getCompany() != null) {
                specification = ClientSpecification.hasCompany(user.getCompany().getId())
                        .and(ClientSpecification.hasSalePerson(user.getId()));
            } else if (user.getCompany() != null) {
                specification = ClientSpecification.hasCompany(user.getCompany().getId());
            } else {
                specification = (root, query, cb) -> cb.conjunction();
            }

            if (status != null) {
                specification = specification.and(ClientSpecification.hasStatus(status));
            }
            if (search != null && !search.isBlank()) {
                specification = specification.and(ClientSpecification.hasSearch(search.trim()));
            }

            Page<Client> clientPage = clientRepository.findAll(specification, pageable);
            return clientPage.map(this::mapToResponse);
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    public ClientResponse getClientById(UUID id) {
        try {
            Client client = clientRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("No client found"));
            companyAccessValidator.validate(client.getCompany().getId());
            return mapToResponse(client);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public ClientResponse editClient(UUID id, ClientUpdate request) {
        try {
            Client client = clientRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Client not found"));
            companyAccessValidator.validate(client.getCompany().getId());

            if (request.getFirstName() != null) client.setFirstName(request.getFirstName());
            if (request.getLastName() != null) client.setLastName(request.getLastName());
            if (request.getGender() != null) client.setGender(request.getGender());
            if (request.getEmail() != null) client.setEmail(request.getEmail());
            if (request.getNationality() != null) client.setNationality(request.getNationality());
            if (request.getCountryOfResidence() != null) client.setCountryOfResidence(request.getCountryOfResidence());
            if (request.getPreferredLanguage() != null) client.setPreferredLanguage(request.getPreferredLanguage());
            if (request.getPhone() != null) client.setPhone(request.getPhone());
            if (request.getNotes() != null) client.setNotes(request.getNotes());

            clientRepository.save(client);
            return mapToResponse(client);
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    public List<GuestResponse> getClientGuest(UUID id) {
        try {
            Client client = clientRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Client not found"));

            companyAccessValidator.validate(client.getCompany().getId());
            List<Guest> guests = guestRepository.findAllByClientId(client.getId());
            return guests.stream().map(this::mapGuestToResponse).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
