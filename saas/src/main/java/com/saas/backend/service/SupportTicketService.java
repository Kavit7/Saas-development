package com.saas.backend.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

import com.saas.backend.dto.SupportTicketRequest;
import com.saas.backend.dto.SupportTicketUpdateRequest;
import com.saas.backend.response.SupportTicketResponse;

public interface SupportTicketService {

    SupportTicketResponse createTicket(SupportTicketRequest request, Authentication auth);

    Page<SupportTicketResponse> getTickets(Authentication auth, int page, int size, String status);

    SupportTicketResponse getTicketById(UUID id, Authentication auth);

    SupportTicketResponse updateTicket(UUID id, SupportTicketUpdateRequest request, Authentication auth);
}
