package com.saas.backend.serviceImpl;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.saas.backend.dto.CompanyRequest;
import com.saas.backend.dto.CompanyUpdate;
import com.saas.backend.models.Company;
import com.saas.backend.models.SubscriptionPlan;
import com.saas.backend.models.SubscriptionStatus;
import com.saas.backend.repositories.CompanyRepository;
import com.saas.backend.repositories.PlatformAdminRepository;
import com.saas.backend.repositories.SubscriptionPlanRepository;
import com.saas.backend.repositories.UserRepository;
import com.saas.backend.response.CompanyResponse;
import com.saas.backend.service.CompanyService;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final PlatformAdminRepository adminRepository;

    public CompanyResponse mapToResponse(Company company) {
        if (company == null) return null;
        return CompanyResponse.builder()
                .id(company.getId() != null ? company.getId().toString() : null)
                .name(company.getName())
                .slug(company.getSlug())
                .email(company.getEmail())
                .phone(company.getPhone())
                .country(company.getCountry())
                .timezone(company.getTimezone())
                .subscriptionPlanId(company.getSubscriptionPlan() != null ? company.getSubscriptionPlan().getId() : null)
                .subscriptionPlanName(company.getSubscriptionPlan() != null ? company.getSubscriptionPlan().getName() : null)
                .status(company.getStatus())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }

    @Override
    public CompanyResponse createCompany(CompanyRequest companyRequest) {
        try {
            if (companyRepository.findByNameIgnoreCase(companyRequest.getName()).isPresent()) {
                throw new RuntimeException("Error obtained:-" + companyRequest.getName() + " is already Present");
            }
            String slug = generateSlug(companyRequest.getName());
            SubscriptionPlan sbp = subscriptionPlanRepository.findByNameIgnoreCase(companyRequest.getSubscription_plan())
                    .orElseThrow(() -> new RuntimeException("No plan found change the plan"));
            Company company = new Company();

            company.setEmail(companyRequest.getEmail());
            company.setCountry(companyRequest.getCountry());
            company.setSlug(slug);
            company.setName(companyRequest.getName());
            company.setPhone(companyRequest.getPhone());
            company.setTimezone(companyRequest.getTimezone());
            company.setStatus(SubscriptionStatus.ACTIVE);
            company.setSubscriptionPlan(sbp);
            // save 
            companyRepository.save(company);

            return mapToResponse(company);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage()); 
        }
    }

    @Override
    public Page<CompanyResponse> getAllCompany(int page, int size, String sortBy, String direction) {
        try {
            Sort sort = direction.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);   
            return companyRepository.findAll(pageable).map(this::mapToResponse);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public CompanyResponse getCompanyDetailsByIdOrName(UUID id, String name, Authentication auth) {
        try {
            Company company = companyRepository.findByIdOrName(id, name)
                    .orElseThrow(() -> new RuntimeException("No company found"));

            return mapToResponse(company);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public CompanyResponse updateCompany(UUID id, CompanyUpdate companyUpdate) {
        try {
            Company existing = companyRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("No Company with such Id"));

            if (companyUpdate.getName() != null) {
                String slug = generateSlug(companyUpdate.getName());
                existing.setName(companyUpdate.getName());
                existing.setSlug(slug);
            }
            if (companyUpdate.getCountry() != null) existing.setCountry(companyUpdate.getCountry());
            if (companyUpdate.getPhone() != null) existing.setPhone(companyUpdate.getPhone());
            if (companyUpdate.getEmail() != null) existing.setEmail(companyUpdate.getEmail());
            if (companyUpdate.getTimezone() != null) existing.setTimezone(companyUpdate.getTimezone());
            // save to database
            companyRepository.save(existing);
            return mapToResponse(existing);
        } catch (Exception e) {
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
