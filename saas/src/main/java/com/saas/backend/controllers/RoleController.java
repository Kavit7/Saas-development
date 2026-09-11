package com.saas.backend.controllers;


import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.backend.dto.RoleRequest;
import com.saas.backend.serviceImpl.RoleServiceImpl;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor 
@SecurityRequirement(name="bearerAuth")
@RequestMapping("/api/platform-admin")
public class RoleController {
    
    private final RoleServiceImpl roleService;

    @PreAuthorize ("hasRole('super admin')")
    @PostMapping("/roles")
    public ResponseEntity<?> createRole(@RequestBody RoleRequest roleRequest){
 
        try{
            roleService.createRole(roleRequest);
            return ResponseEntity.ok("Role created successfully");
        }catch(Exception e){
            return ResponseEntity.badRequest().body( e.getMessage());
        }
        
    }
}
