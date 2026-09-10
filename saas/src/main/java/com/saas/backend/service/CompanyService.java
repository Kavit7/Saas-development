package com.saas.backend.service;

import org.springframework.stereotype.Repository;

import com.saas.backend.dto.CompanyRequest;
import com.saas.backend.response.CompanyResponse;




@Repository
public interface CompanyService {
    


    public CompanyResponse createCompany(CompanyRequest companyRequest);
}
