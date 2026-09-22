package com.saas.backend.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.backend.dto.GuestRequest;
import com.saas.backend.dto.GuestRequirmentRequest;
import com.saas.backend.models.Guest;
import com.saas.backend.models.GuestRequirement;
import com.saas.backend.response.GuestResponse;
import com.saas.backend.serviceImpl.GuestServiceImpl;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController 
@SecurityRequirement(name="bearerAuth")
@RequestMapping("/guest") 
@RequiredArgsConstructor 

public class GuestController {
    
  private final GuestServiceImpl guestService;



  @PostMapping ("/{id}/client")
  @PreAuthorize("hasAnyRole('SALES_PERSON','ADMIN')")
  ResponseEntity<?> createGuest( @PathVariable UUID id,@RequestBody GuestRequest request){
    try{
        GuestResponse guestResponse= guestService.createGuest(id, request);
        return ResponseEntity.ok(Map.of("message","Guest created Successfully","data",guestResponse));
    }
    catch(Exception e){
        return ResponseEntity.badRequest().body(e.getMessage());
    }
  }
@PutMapping ("/{id}")
  @PreAuthorize("hasAnyRole('SALES_PERSON','ADMIN')")
  ResponseEntity<?> editGuest( @PathVariable UUID id, @RequestBody GuestRequest request){
    try{
        Guest guest= guestService.editGuest(id, request);
        return ResponseEntity.ok(Map.of("message","Guest Updated Successfully","data",guest));
    }
    catch(Exception e){
        return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @DeleteMapping ("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  ResponseEntity<?> deleteGuest( @PathVariable UUID id){
    try{
        guestService.deleteGuest(id);
        return ResponseEntity.ok(Map.of("message","Guest deleted Successfully"));
    }
    catch(Exception e){
        return ResponseEntity.badRequest().body(e.getMessage());
    }
  }
  @PostMapping ("/{id}/requirements")
  @PreAuthorize("hasAnyRole('SALES_PERSON','ADMIN')")
  ResponseEntity<?> createGuestRequirement( @PathVariable UUID id,@RequestBody GuestRequirmentRequest request,Authentication auth){
    try{
        GuestRequirement guestRequirement =guestService.createGuestRequirment(id,request, auth);
        return ResponseEntity.ok(Map.of("message","Guest Requirement added Successfully","data",guestRequirement));
    }
    catch(Exception e){
        return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

 @GetMapping("guest-requirements/{guestId}")
 @PreAuthorize("hasAnyRole('SALES_PERSON','ADMIN')")
 ResponseEntity<?> getGuestRequirement(@PathVariable UUID guestId){
          try{
           List<GuestRequirement> requirement= guestService.getGuestRequirement(guestId);
           return ResponseEntity.ok(Map.of("message","Guest Requirement Loaded","data",requirement));
    }
    catch(Exception e){
        return ResponseEntity.badRequest().body(e.getMessage());
    }
 }
 //Update Guest Requirement
 
 @PutMapping ("guest-requirements/{reqId}")
  @PreAuthorize("hasAnyRole('SALES_PERSON','ADMIN')")
  ResponseEntity<?> updateGuestRequirement(@PathVariable UUID reqId,@RequestBody GuestRequirmentRequest request){
    try{
        GuestRequirement guestRequirement= guestService.updateGuestRequirement(reqId, request);
        return ResponseEntity.ok(Map.of("message","GuestRequirement Updated Successfully","data",guestRequirement));
    }
    catch(Exception e){
        return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @DeleteMapping("guest-requirements/{reqId}")
  @PreAuthorize("hasAnyRole('SALES_PERSON','ADMIN')")
ResponseEntity<?> deleteGuestRequirement( @PathVariable UUID reqId){
    try{
        guestService.deleteGuestRequirement(reqId);
        return ResponseEntity.ok(Map.of("message","Guest Requirement deleted Successfully"));
    }
    catch(Exception e){
        return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

}
