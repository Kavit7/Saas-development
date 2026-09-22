package com.saas.backend.service;



import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

import com.saas.backend.dto.ClientRequest;
import com.saas.backend.dto.ClientUpdate;
import com.saas.backend.models.Client;
import com.saas.backend.models.Guest;
import com.saas.backend.models.ClientStatus;
import com.saas.backend.response.ClientResponse;

public interface ClientService {
        ClientResponse createClient(ClientRequest request);
        Page<Client> getClients(Authentication auth,int page,int size,String sortBy,String direction,String search,ClientStatus status);
        Client getClientById(UUID id);
        Client editClient(UUID id,ClientUpdate request);
        List<Guest> getClientGuest(UUID id);
}
