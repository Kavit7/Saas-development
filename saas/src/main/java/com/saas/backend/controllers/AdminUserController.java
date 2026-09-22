package com.saas.backend.controllers;

import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.saas.backend.dto.UserRequest;
import com.saas.backend.dto.UserStatusRequest;
import com.saas.backend.models.User;
import com.saas.backend.response.UserResponse;
import com.saas.backend.serviceImpl.UserServiceImpl;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController 
@RequiredArgsConstructor 
@SecurityRequirement(name="bearerAuth")
@RequestMapping("/api/users")
public class AdminUserController {
    
    private final UserServiceImpl userServiceImpl;
    
    @PostMapping("/create_user")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    ResponseEntity<?> createAdmin(@RequestBody UserRequest userRequest){
        try{
             UserResponse response= userServiceImpl.createAdmin(userRequest);
           return ResponseEntity.ok(Map.of("message","User Added successfull ","id",response.getId(),"role",response.getRole(),"status",response.getStatus(),"created_at",response.getCreatedAt()));
        } catch(Exception e){
           return  ResponseEntity.badRequest().body(e.getMessage());
        }
    }   



    @GetMapping("/get-users") 
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    ResponseEntity<?> getAllUsers(
        @RequestParam (defaultValue = "0") int page,
        @RequestParam (defaultValue = "100") int size,
        @RequestParam (defaultValue = "email") String sortBy,
        @RequestParam (defaultValue = "asc") String direction
    ){
   try{
       Page pag = userServiceImpl.getAllUsers(page, size, sortBy, direction);
       return  ResponseEntity.ok(Map.of("message","User Loaded Successfully", "data",pag));   
   }
   catch(Exception e){
   return  ResponseEntity.badRequest().body(e.getMessage());
          
   }
    }

    @GetMapping("get-user/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SALES_PERSON', 'RESERVATION_MANAGER')")
    ResponseEntity<?> getUserById( @PathVariable  UUID id,Authentication auth){
        try {
            User user = userServiceImpl.getUserById(id, auth);

            return ResponseEntity.ok(Map.of("Message", "Loaded Successfully", "data", user));
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PatchMapping("/user/{id}/status")
    @PreAuthorize ("hasRole('SUPER_ADMIN')")
    ResponseEntity<?> updateStatus(@PathVariable UUID id ,@RequestBody UserStatusRequest request,Authentication auth){
        try{
            User user = userServiceImpl.updateUserStatus(id, request, auth);
            return ResponseEntity.ok(Map.of("message","Status updated","data",user));
        }
        catch(Exception e){
             return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
