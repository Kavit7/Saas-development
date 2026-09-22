package com.saas.backend.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.backend.dto.OccasionRequest;
import com.saas.backend.models.SpecialOccasion;
import com.saas.backend.serviceImpl.GuestServiceImpl;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController 
@SecurityRequirement(name="bearerAuth")
@RequestMapping("/occasion/") 
@RequiredArgsConstructor
public class GuestSpecialOccasionController {
   private final GuestServiceImpl guestService;


@PostMapping("guest-occasions/{guestId}")
@PreAuthorize("hasAnyRole('SALES_PERSON','ADMIN')")
public ResponseEntity<?> createGuestOccasion(
        @PathVariable UUID guestId,
        @RequestBody OccasionRequest request) {

    try {

        SpecialOccasion specialOccasion =
                guestService.createGuestOccassion(guestId, request);

        return ResponseEntity.ok(specialOccasion);

    } catch (Exception e) {

        return ResponseEntity.badRequest().body(e.getMessage());
    }
}


@PutMapping("guest-occasions/{guestId}")
@PreAuthorize("hasAnyRole('SALES_PERSON','ADMIN')")
public ResponseEntity<?> updateGuestOccasion(
        @PathVariable UUID guestId,
        @RequestBody OccasionRequest request) {

    try {

        SpecialOccasion specialOccasion =
                guestService.updateGuestOccasion(guestId, request);

        return ResponseEntity.ok(specialOccasion);

    } catch (Exception e) {

        return ResponseEntity.badRequest().body(e.getMessage());
    }
}


@GetMapping("guest-occasions/{guestId}")
@PreAuthorize("hasAnyRole('SALES_PERSON','ADMIN')")
public ResponseEntity<?> getGuestSpecialOccasion(
        @PathVariable UUID guestId) {

    try {

        List<SpecialOccasion> occasions =
                guestService.getGuestSpecialOcassion(guestId);

        return ResponseEntity.ok(occasions);

    } catch (Exception e) {

        return ResponseEntity.badRequest().body(e.getMessage());
    }
}


@DeleteMapping("guest-occasions/{occasionId}")
@PreAuthorize("hasAnyRole('SALES_PERSON','ADMIN')")
public ResponseEntity<?> deleteGuestOccasion(
        @PathVariable UUID occasionId) {

    try {
        guestService.deleteGuestOccassion(occasionId);

        return ResponseEntity.ok(
                Map.of("message", "Guest Occasion deleted Successfully")
        );

    } catch (Exception e) {

        return ResponseEntity.badRequest().body(e.getMessage());
    }
}




    
}
