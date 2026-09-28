package com.saas.backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.backend.serviceImpl.EmailTesting;


import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping("/api/v1/auth")

@RequiredArgsConstructor 

public class EmailSenderTestController {

    private final EmailTesting emailTesting;
    @PostMapping("/email")

    public ResponseEntity<?> testEmail(@RequestBody String email){
        emailTesting.sendEmail(email);
        return  ResponseEntity.ok("email sent successfully");
    }


    
}
