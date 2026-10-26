package com.saas.backend.controllers;


import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.backend.dto.ChangePasswordRequest;
import com.saas.backend.dto.LoginRequest;
import com.saas.backend.response.AuthResponse;
import com.saas.backend.response.UserResponse;
import com.saas.backend.serviceImpl.AuthServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping({"/api/v1/auth", "/auth"})
@Tag(name = "Authentication", description = "User login, profile, and password management")
@RequiredArgsConstructor 
public class AuthController {
    
  private final AuthServiceImpl authService;


   @PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request) {

    try {

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(
            Map.of(
                "error", false,
                "message", "Login Successfully",
                "token", response.getToken()
            )
        );

    } catch (UsernameNotFoundException e) {

        return ResponseEntity.status(401).body(
            Map.of(
                "error", true,
                "message", "Invalid email or password"
            )
        );

    } catch (BadCredentialsException e) {

        return ResponseEntity.status(401).body(
            Map.of(
                "error", true,
                "message", "Invalid email or password"
            )
        );
    }
}



   @PostMapping("/platform-admin/login")
public ResponseEntity<?> platformLogin(@RequestBody LoginRequest request) {

    try {

        AuthResponse response = authService.platformAdminlogin(request);

        return ResponseEntity.ok(
            Map.of(
                "error", false,
                "message", "Login Successfully",
                "token", response.getToken()
            )
        );

    } catch (UsernameNotFoundException e) {

        return ResponseEntity.status(401).body(
            Map.of(
                "error", true,
                "message", "Invalid email or password"
            )
        );

    } catch (BadCredentialsException e) {

        return ResponseEntity.status(401).body(
            Map.of(
                "error", true,
                "message", "Invalid email or password"
            )
        );
    }
}

    @GetMapping({"/me", "/profile"})
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<?> getMe(Authentication auth) {
        try {
            UserResponse profile = authService.getCurrentUserProfile(auth);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", true, "message", e.getMessage()));
        }
    }

    @PostMapping("/change-password")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Change own password (clears 3-day temporary default password, setting permanent password)")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request, Authentication auth) {
        try {
            Map<String, Object> res = authService.changePassword(request, auth);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", true, "message", e.getMessage()));
        }
    }

}
