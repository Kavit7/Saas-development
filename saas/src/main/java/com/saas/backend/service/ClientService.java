package com.saas.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

import com.saas.backend.dto.ClientRequest;
import com.saas.backend.dto.ClientUpdate;
import com.saas.backend.models.ClientStatus;
import com.saas.backend.response.ClientResponse;
import com.saas.backend.response.GuestResponse;

public interface ClientService {
    ClientResponse createClient(UUID salesPersonId, ClientRequest request);
    Page<ClientResponse> getClients(Authentication auth, int page, int size, String sortBy, String search, String direction, ClientStatus status);
    ClientResponse getClientById(UUID id);
    ClientResponse editClient(UUID id, ClientUpdate request);
    List<GuestResponse> getClientGuest(UUID id);
}
