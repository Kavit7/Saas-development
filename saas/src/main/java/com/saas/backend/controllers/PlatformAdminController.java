package com.saas.backend.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.backend.dto.PlatformAdminRequest;
import com.saas.backend.response.PlatformAdminResponse;
import com.saas.backend.serviceImpl.PlatformAdminServiceImpl;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController 
@RequiredArgsConstructor 
@RequestMapping("/api/platform-admin")
@SecurityRequirement(name="bearerAuth")
public class PlatformAdminController {
private final PlatformAdminServiceImpl adminServiceImpl;


    @PostMapping ("/create")
    @PreAuthorize ("hasRole('SUPER_ADMIN')")
    ResponseEntity<?> createPlatformAdmin(@RequestBody PlatformAdminRequest request){

        try{
            PlatformAdminResponse response= adminServiceImpl.createPlatformAdmin(request);
            return ResponseEntity.ok(Map.of("Message", "platform admin created successfully", "id",response.getId(),"role",response.getRole()));
        }
        catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
}
