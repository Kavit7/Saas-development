package com.saas.backend.serviceImpl;

import org.springframework.stereotype.Service;

import com.saas.backend.dto.CompanyRequest;
import com.saas.backend.models.Company;
import com.saas.backend.models.SubscriptionPlan;
import com.saas.backend.repositories.CompanyRepository;
import com.saas.backend.repositories.SubscriptionPlanRepository;
import com.saas.backend.response.CompanyResponse;
import com.saas.backend.service.CompanyService;

import lombok.RequiredArgsConstructor;


@Service 
@RequiredArgsConstructor

public class CompanyServiceImpl implements CompanyService {

       private final CompanyRepository companyRepository;
       private final SubscriptionPlanRepository subscriptionPlanRepository;

   public  CompanyResponse createCompany(CompanyRequest companyRequest){
       
    try{

        if (companyRepository.findByNameIgnoreCase(companyRequest.getName()).isPresent()){
            throw new RuntimeException("Error obtained:-"+companyRequest.getName()+"is already Present" );
        }
    String slug = generateSlug(companyRequest.getName());
    SubscriptionPlan sbp= subscriptionPlanRepository.findSubscriptionPlanById(companyRequest.getSubscription_plan_id());
    Company company = new Company();

      company.setEmail(companyRequest.getEmail());
      company.setCountry(companyRequest.getCountry());
      company.setSlug(slug);
      company.setName(companyRequest.getName());
      company.setPhone(companyRequest.getPhone());
      company.setTimezone(companyRequest.getTimezone());
      company.setStatus(companyRequest.getStatus().ACTIVE);
      company.setSubscriptionPlan(sbp);
      

      // save 
      companyRepository.save(company);

      return new CompanyResponse(company.getId().toString(),company.getSlug(),company.getCreatedAt());

    }
    catch(Exception e){
         throw new RuntimeException(e.getMessage()); 
    }

    }


    private String generateSlug(String name) {

    return name
            .toLowerCase()
            .trim()
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-|-$", "");
}
    
}
