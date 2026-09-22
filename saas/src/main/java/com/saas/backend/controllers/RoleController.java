package com.saas.backend.controllers;


import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.backend.dto.RoleRequest;
import com.saas.backend.models.Role;
import com.saas.backend.serviceImpl.RoleServiceImpl;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor 
@SecurityRequirement(name="bearerAuth")
@RequestMapping("/api/platform-admin")
public class RoleController {
    
    private final RoleServiceImpl roleService;

    
    @PostMapping("/roles")
     @PreAuthorize ("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createRole(@RequestBody RoleRequest roleRequest){
 
        try{
            roleService.createRole(roleRequest);
            return ResponseEntity.ok("Role created successfully");
        }catch(Exception e){
            return ResponseEntity.badRequest().body( e.getMessage());
        }
        
    }

    @GetMapping ("get-roles")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<?> getAllRoles(){
        try {
        List<Role> role = roleService.getRoles();
        return  ResponseEntity.ok(Map.of("message", "Roles loaded successfully","data",role));
        }
        catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
