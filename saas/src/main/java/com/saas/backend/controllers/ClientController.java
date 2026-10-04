package com.saas.backend.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.saas.backend.dto.ClientRequest;
import com.saas.backend.dto.ClientUpdate;
import com.saas.backend.models.ClientStatus;
import com.saas.backend.response.ClientResponse;
import com.saas.backend.response.GuestResponse;
import com.saas.backend.serviceImpl.ClientServiceImpl;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController 
@RequiredArgsConstructor 
@SecurityRequirement(name="bearerAuth")
public class ClientController {

    private final ClientServiceImpl clientService;
    
    @PostMapping("/clients/{salesPersonId}")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_PERSON')")
    public ResponseEntity<?> createClient(@PathVariable UUID salesPersonId, @RequestBody ClientRequest request) {
        try {
            ClientResponse client = clientService.createClient(salesPersonId, request);
            return ResponseEntity.ok(Map.of(
                    "message", "Client added successfully",
                    "data", client,
                    "clientId", client.getId(),
                    "companyName", client.getCompanyName() != null ? client.getCompanyName() : "",
                    "salePerson", client.getSalePeson() != null ? client.getSalePeson() : "",
                    "createdAt", client.getCreatedAt()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
   
    @GetMapping("/clients")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_PERSON')")
    public ResponseEntity<Page<ClientResponse>> getClients(
            Authentication auth,
            @RequestParam (defaultValue = "0") int page,
            @RequestParam (defaultValue = "20") int size,
            @RequestParam (defaultValue = "createdAt") String sortBy,
            @RequestParam (defaultValue = "") String search,
            @RequestParam (defaultValue="asc") String direction,
            @RequestParam(defaultValue = "ACTIVE") ClientStatus status
    ) {
        Page<ClientResponse> client = clientService.getClients(auth, page, size, sortBy, search, direction, status);
        return ResponseEntity.ok(client);
    }

    @GetMapping("/client/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_PERSON')")
    public ResponseEntity<?> getClientsById(@PathVariable UUID id) {
        try {
            ClientResponse client = clientService.getClientById(id);
            return ResponseEntity.ok(Map.of("data", client));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("error" + e.getMessage());
        }
    }

    @PutMapping("/client/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_PERSON')")
    public ResponseEntity<?> editClient(@PathVariable UUID id, @RequestBody ClientUpdate request) {
        try {
            ClientResponse client = clientService.editClient(id, request);
            return ResponseEntity.ok(Map.of("Message", "updated successfully", "data", client));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/client/{id}/guests")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_PERSON')")
    public ResponseEntity<?> getClientGuests(@PathVariable UUID id) {
        try {
            List<GuestResponse> guests = clientService.getClientGuest(id);
            return ResponseEntity.ok(Map.of(
                    "Message", guests.isEmpty() ? "Data loaded successfully No guest found" : "Data loaded successfully",
                    "data", guests
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
