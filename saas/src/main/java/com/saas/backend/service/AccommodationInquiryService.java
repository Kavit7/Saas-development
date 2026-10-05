package com.saas.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;

import com.saas.backend.dto.AccommodationInquiryRequest;
import com.saas.backend.response.AccommodationInquiryResponse;

public interface AccommodationInquiryService {

    AccommodationInquiryResponse createInquiry(AccommodationInquiryRequest request, Authentication auth);

    List<AccommodationInquiryResponse> getInquiries(Authentication auth, UUID bookingId);

    AccommodationInquiryResponse updateInquiryStatus(UUID id, String status, Authentication auth);
}
