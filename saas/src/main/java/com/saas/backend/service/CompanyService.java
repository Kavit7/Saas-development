package com.saas.backend.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

import com.saas.backend.dto.CompanyRequest;
import com.saas.backend.dto.CompanyUpdate;
import com.saas.backend.response.CompanyResponse;

public interface CompanyService {

    public CompanyResponse createCompany(CompanyRequest companyRequest);

    public Page<CompanyResponse> getAllCompany(int page, int size, String sortBy, String direction);

    public CompanyResponse getCompanyDetailsByIdOrName(UUID id, String name, Authentication authentication);

    public CompanyResponse updateCompany(UUID id, CompanyUpdate companyUpdate);

}
