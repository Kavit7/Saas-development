package com.saas.backend.AccessHelper;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.saas.backend.models.User;

@Component 
public class CurrentUserChecker {

    public User checkCurrentUser(){
        

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()){
            throw new RuntimeException("User is not found");
        }

        User currentUser= (User) authentication.getPrincipal();
        return  currentUser;
    }
    



}
