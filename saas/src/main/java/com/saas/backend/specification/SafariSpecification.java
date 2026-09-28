package com.saas.backend.specification;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.saas.backend.models.Safari;
import com.saas.backend.models.SafariStatus;

public class SafariSpecification {
    


    public static Specification<Safari> hasCompany(UUID companyId){
        return (root,query,cb)-> cb.equal(
            root.get("client").get("company").get("id"),companyId
        );   
    }

    public static Specification<Safari> hasSearch(String keyword){
        return (root,query,cb)->{
            String searchValue = "%"+keyword.toLowerCase()+"%";

          return  cb.or(
                cb.like(cb.lower(root.get("referenceNumber")),searchValue)
            );

        };

    }
    public static Specification<Safari> hasStatus(SafariStatus status){
        return (root,query,cb)-> cb.equal(root.get("status"),status);
    }
     public static Specification<Safari> hasStartDate(LocalDate date){
        return (root,query,cb)-> cb.equal(root.get("startDate"),date);
    }
     public static Specification<Safari> hasEndDate(LocalDate date){
        return (root,query,cb)-> cb.equal(root.get("endDate"),date);
    }
}
