package com.saas.backend.controllers;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.saas.backend.dto.CompanyRequest;
import com.saas.backend.dto.CompanyUpdate;
import com.saas.backend.response.CompanyResponse;
import com.saas.backend.serviceImpl.CompanyServiceImpl;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController 
@SecurityRequirement(name="bearerAuth")
@RequiredArgsConstructor 
@RequestMapping ("/api/company")
public class CompanyController {

    private final CompanyServiceImpl companyService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createCompany(@RequestBody CompanyRequest companyRequest) {
        try {
            CompanyResponse response = companyService.createCompany(companyRequest);
            return ResponseEntity.ok(Map.of("message", "company created successfully", "data", response, "id", response.getId(), "slug", response.getSlug(), "createdAt", response.getCreatedAt()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/companies")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        try {    
            Page<CompanyResponse> company = companyService.getAllCompany(page, size, sortBy, direction);
            return ResponseEntity.ok(Map.of("message", "companies loaded successfully", "data", company));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }   

    @GetMapping("/companies/{id}/{name}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','RESERVATION_MANAGER','SALES_PERSON')")
    public ResponseEntity<?> getCompanyDetails(
            @RequestParam(required = false) @PathVariable UUID id,
            @RequestParam(required = false) @PathVariable String name,
            Authentication auth) {
        try {
            CompanyResponse company = companyService.getCompanyDetailsByIdOrName(id, name, auth);
            return ResponseEntity.ok(Map.of("message", "Company details loaded", "data", company));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/company/edit")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> editCompany(@RequestParam UUID id, @RequestBody CompanyUpdate update) {
        try {
            CompanyResponse upd = companyService.updateCompany(id, update);
            return ResponseEntity.ok(Map.of("message", "Updated Successfully", "data", upd));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/my-company")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'RESERVATION_MANAGER', 'SALES_PERSON', 'GUIDE')")
    public ResponseEntity<?> getMyCompany(Authentication auth) {
        try {
            CompanyResponse company = companyService.getMyCompany(auth);
            return ResponseEntity.ok(Map.of("message", "Company details loaded", "data", company));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/my-company/change-plan")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> changeMyCompanyPlan(@RequestBody Map<String, String> body, Authentication auth) {
        try {
            String planIdentifier = body.getOrDefault("plan", body.get("planId"));
            if (planIdentifier == null || planIdentifier.trim().isEmpty()) {
                planIdentifier = body.get("subscription_plan");
            }
            if (planIdentifier == null || planIdentifier.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Please select a plan to switch to."));
            }
            CompanyResponse updated = companyService.changeCompanyPlan(null, planIdentifier, auth);
            return ResponseEntity.ok(Map.of("message", "Company plan updated successfully", "data", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{companyId}/change-plan")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> changeCompanyPlanBySuperAdmin(
            @PathVariable UUID companyId,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        try {
            String planIdentifier = body.getOrDefault("plan", body.get("planId"));
            if (planIdentifier == null || planIdentifier.trim().isEmpty()) {
                planIdentifier = body.get("subscription_plan");
            }
            if (planIdentifier == null || planIdentifier.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Please select a plan to switch to."));
            }
            CompanyResponse updated = companyService.changeCompanyPlan(companyId, planIdentifier, auth);
            return ResponseEntity.ok(Map.of("message", "Company plan updated successfully", "data", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
