package com.saas.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.backend.dto.EmailAnalysisResult;
import com.saas.backend.models.EmailResponseType;
import com.saas.backend.serviceImpl.EmailResponseDetectionServiceImpl;

class EmailResponseDetectionServiceTest {

    private EmailResponseDetectionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EmailResponseDetectionServiceImpl(new ObjectMapper());
    }

    @Test
    void testAcceptadeMessageHeuristic() {
        String subject = "Re: Accommodation Reservation Request - ACC-BOOK-2026-091D26";
        String body = "Dear property we kindly inform you that booking is acceptade refer the invoice to the attachment";

        EmailAnalysisResult result = service.analyzeEmailResponse(subject, body);

        assertNotNull(result);
        assertEquals(EmailResponseType.CONFIRMED, result.getResponseType());
    }

    @Test
    void testLodgeConfirmationExtraction() {
        String subject = "Re: Accommodation Reservation Request - ACC-BOOK-2026-091D26";
        String body = "Dear David,\n\nThank you for your reservation request ACC-BOOK-2026-091D26.\n\n" +
                "We are pleased to confirm availability for the following:\n\n" +
                "- Check-in: Sun, 08 Nov 2026\n" +
                "- Check-out: Mon, 09 Nov 2026\n\n" +
                "Lodge confirmation number: UL-CONF-TEST-48213\n\n" +
                "Please note the rooms will be held until 14 Oct 2026.";

        EmailAnalysisResult result = service.analyzeEmailResponse(subject, body);

        assertNotNull(result);
        assertEquals(EmailResponseType.CONFIRMED, result.getResponseType());
        assertEquals("UL-CONF-TEST-48213", result.getConfirmationNumber());
    }
}
