package com.saas.backend.AccessHelper;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.saas.backend.models.User;

@Component 
public class CompanyAccessValidator {
    
    public void validate(UUID resourceCompanyId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not found");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return;
        }

        User currentUser = (User) principal;

        // Super admin has global cross-company oversight
        if (currentUser.getRole() != null && 
            "SUPER_ADMIN".equalsIgnoreCase(currentUser.getRole().getName().replace("ROLE_", "").trim())) {
            return;
        }

        if (currentUser.getCompany() == null) {
            throw new RuntimeException("Current user is not associated with any company");
        }

        UUID currentCompanyId = currentUser.getCompany().getId();
        if (resourceCompanyId != null && !currentCompanyId.equals(resourceCompanyId)) {
            throw new RuntimeException("You are not authorized to access this resource");
        }
    }
}
