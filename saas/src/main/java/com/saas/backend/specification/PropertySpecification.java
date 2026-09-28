package com.saas.backend.specification;

import org.springframework.data.jpa.domain.Specification;

import com.saas.backend.models.Property;
import com.saas.backend.models.VerificationStatus;

public class PropertySpecification {


    public static Specification<Property> hasSearch(String search){
        return (root,query,cb)->{
            String keyword = "%"+search+"%";

            return cb.or(
                cb.like(cb.lower(root.get("name")),keyword),
                cb.like(cb.lower(root.get("website")),keyword)

            );

        };
        
    }

    public static Specification<Property> hasStatus(VerificationStatus status){
        return (root,query,cb)-> cb.equal(root.get("verificationStatus"),status);
    }
    
}
