package com.saas.backend.controllers;


import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.backend.dto.CompanyRequest;
import com.saas.backend.response.CompanyResponse;
import com.saas.backend.serviceImpl.CompanyServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController 

@RequiredArgsConstructor 
@RequestMapping ("/api/v1/company")
public class CompanyController {



     private final CompanyServiceImpl companyService;
      @PostMapping("/create")
       ResponseEntity<?> createCompany(CompanyRequest companyRequest){

          try{
           CompanyResponse response= companyService.createCompany(companyRequest);

          return  ResponseEntity.ok(Map.of("message","company created successfully","id",response.getId(),"slug",response.getSlug(),"createdAt",response.getCreatedAt()));
          }

          catch(Exception e){
              return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()));
          }

      }
    
}
