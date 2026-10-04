package com.saas.backend.service;

import java.util.List;
import java.util.UUID;

import com.saas.backend.dto.InvoiceCreateRequest;
import com.saas.backend.models.InvoiceStatus;
import com.saas.backend.response.InvoiceResponse;
import com.saas.backend.response.MailboxEmailResponse;

public interface MailboxService {
    List<MailboxEmailResponse> getAllEmails(UUID bookingId);
    MailboxEmailResponse getEmailById(UUID id);
    void triggerSync();
    List<InvoiceResponse> getAllInvoices(InvoiceStatus status, UUID bookingId);
    InvoiceResponse getInvoiceById(UUID id);
    InvoiceResponse createInvoice(InvoiceCreateRequest request);
    InvoiceResponse updateInvoiceStatus(UUID id, InvoiceStatus status);
}
