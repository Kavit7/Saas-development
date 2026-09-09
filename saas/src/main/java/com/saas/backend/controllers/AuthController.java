package com.saas.backend.controllers;


import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.backend.dto.LoginRequest;
import com.saas.backend.response.AuthResponse;
import com.saas.backend.serviceImpl.AuthServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping ("/api/v1/auth")


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
                "message", "Login successful",
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

}
