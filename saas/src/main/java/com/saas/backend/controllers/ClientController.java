package com.saas.backend.controllers;


import java.util.List;
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

import com.saas.backend.dto.ClientRequest;
import com.saas.backend.dto.ClientUpdate;
import com.saas.backend.models.Client;
import com.saas.backend.models.ClientStatus;
import com.saas.backend.models.Guest;
import com.saas.backend.response.ClientResponse;
import com.saas.backend.serviceImpl.ClientServiceImpl;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping("/api")
@RequiredArgsConstructor 
@SecurityRequirement(name="bearerAuth")
public class ClientController {


    private final ClientServiceImpl clientService;
    

    @PostMapping("/client")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_PERSON')")
    ResponseEntity<?> createClient( @RequestBody ClientRequest request){
        try{
             ClientResponse client = clientService.createClient(request);
             return ResponseEntity.ok(Map.of("message","Client added successfully", "clientId",client.getId(),"companyName",client.getCompanyName(),"salePerson",client.getSalePeson(),"createdAt",client.getCreatedAt()
            ));
        }
        catch(Exception e){
          return  ResponseEntity.badRequest().body(e.getMessage());
        }
    }
   

        @GetMapping("/clients")
        @PreAuthorize("hasAnyRole('ADMIN','SALES_PERSON')")
         ResponseEntity<?> getClients(
            Authentication auth,
            @RequestParam (defaultValue = "0") int page,
            @RequestParam (defaultValue = "20") int size,
            @RequestParam (defaultValue = "createdAt") String sortBy,
            @RequestParam (defaultValue = "") String search,
            @RequestParam (defaultValue="asc") String direction,
            @RequestParam(defaultValue = "ACTIVE") ClientStatus status
         ){

              try {
             Page client = clientService.getClients(auth, page, size, sortBy, search, direction, status);
             return ResponseEntity.ok(Map.of("message","Loaded successfully", "data",client));
        } catch (Exception e) {
           return ResponseEntity.badRequest().body(e.getMessage());
        }
         }




         @GetMapping("/client/{id}")
         @PreAuthorize("hasAnyRole('ADMIN','SALES_PERSON')")
         ResponseEntity<?> getClientsById(@PathVariable UUID id){
            try {
                Client client = clientService.getClientById(id);
                return ResponseEntity.ok(Map.of("data",client));
                
            }
            catch(Exception e){
                return ResponseEntity.badRequest().body("error"+ e.getMessage());
            }
         }

         @PutMapping("/client/{id}")
         @PreAuthorize("hasAnyRole('ADMIN','SALES_PERSON')")
         ResponseEntity<?> editClient( @PathVariable UUID id ,@RequestBody ClientUpdate request){
            try {
                Client client =clientService.editClient(id, request);
                return ResponseEntity.ok(Map.of("Message","updated succesfully", "data",client));
            } catch (Exception e) {
                return  ResponseEntity.badRequest().body(e.getMessage());
            }
         }
  

         @GetMapping("/client/{id}/guests")
         @PreAuthorize("hasAnyRole('ADMIN','SALES_PERSON')")
         ResponseEntity<?> getClientGuests(@PathVariable UUID id){
            try {
                List<Guest> guests= clientService.getClientGuest(id);
                if (guests.isEmpty()){
                    return ResponseEntity.ok(Map.of("Message","Data loaded succesfully No guest found","data",guests));
                }
                 return ResponseEntity.ok(Map.of("Message","Data loaded succesfully", "data",guests));
            } catch (Exception e) {
                  return  ResponseEntity.badRequest().body(e.getMessage());
            }
         }
}
