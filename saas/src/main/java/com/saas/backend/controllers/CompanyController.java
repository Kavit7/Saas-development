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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.saas.backend.dto.CompanyRequest;
import com.saas.backend.dto.CompanyUpdate;
import com.saas.backend.models.Company;
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
       ResponseEntity<?> createCompany(CompanyRequest companyRequest){
          try{
           CompanyResponse response= companyService.createCompany(companyRequest);

          return  ResponseEntity.ok(Map.of("message","company created successfully","id",response.getId(),"slug",response.getSlug(),"createdAt",response.getCreatedAt()));
          }

          catch(Exception e){
              return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()));
          }
         
      }

       @GetMapping("/companies")
       @PreAuthorize("hasRole('SUPER_ADMIN')")
       ResponseEntity<?> getCompanies(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue="100") int size,
        @RequestParam (defaultValue="name") String sortBy,
        @RequestParam (defaultValue="asc") String direction
       ){
          try{    
            Page<Company> company = companyService.getAllCompany(page,size,sortBy,direction);
            return ResponseEntity.ok(Map.of("message","companies loaded successfully","data",company));
          }
        catch(Exception e){
        return ResponseEntity.badRequest().body(e.getMessage());
        }
       }   
       
       @GetMapping("/companies/{id}/{name}")
       @PreAuthorize("hasAnyRole('SUPER_ADMIN','RESERVATION_MANAGER','SALES_PERSON')")
       ResponseEntity<?> getCompanyDetails(@RequestParam(required = false) @PathVariable UUID id, @RequestParam(required = false)  @PathVariable String name,Authentication auth){
         try {
            Company company = companyService.getCompanyDetailsByIdOrName(id, name,auth);
            
            return ResponseEntity.ok(Map.of("message","Company details loaded","data",company));
         }
         catch (Exception e){
            return  ResponseEntity.badRequest().body(e.getMessage());
         }
  
       }

        @PutMapping("/company/edit")
         @PreAuthorize("hasRole('SUPER_ADMIN')")
         ResponseEntity<?> editCompany(UUID id, CompanyUpdate update){
            try {
                Company upd = companyService.updateCompany(id, update);
                return ResponseEntity.ok(Map.of("message","Updated Successfully","data",upd));
            }
            catch (Exception e){
            return  ResponseEntity.badRequest().body(e.getMessage());
         }
         }

       
}
