package com.saas.backend.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.backend.dto.UserRequest;
import com.saas.backend.response.UserResponse;
import com.saas.backend.serviceImpl.UserServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController 
@RequiredArgsConstructor 
@RequestMapping("/api/users")
public class UserController {
    
    private final UserServiceImpl userServiceImpl;

    @PostMapping("/create")
    ResponseEntity<?> createAdmin(UserRequest userRequest){
        try{
             UserResponse response= userServiceImpl.createAdmin(userRequest);
           return ResponseEntity.ok(Map.of("message","Admin Added successfull ","id",response.getId(),"role",response.getRole(),"status",response.getStatus(),"created_at",response.getCreatedAt()));
        } catch(Exception e){
           return  ResponseEntity.badRequest().body(e.getMessage());
        }

    }
    
}
