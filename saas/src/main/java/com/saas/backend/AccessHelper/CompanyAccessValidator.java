package com.saas.backend.AccessHelper;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.saas.backend.models.User;

@Component 
public class CompanyAccessValidator {
    
    public void validate(UUID resourceCompanyId){

        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();


        if (authentication == null || !authentication.isAuthenticated()){
            throw new RuntimeException("User is not found");
        }

        User currentUser=(User) authentication.getPrincipal();

        UUID currentCompanyId= currentUser.getCompany().getId();
        if (!currentCompanyId.equals(resourceCompanyId)){
            throw new RuntimeException("You are not authorized to access this resource");
        }

    }
}
