package com.saas.backend.specification;

import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.saas.backend.models.Client;
import com.saas.backend.models.ClientStatus;

public class ClientSpecification {

    public static Specification<Client> hasCompany(UUID companyId){
        return (root,query,cb) ->  cb.equal(
         root.get("company").get("id"),companyId);
    
   }
   public static Specification<Client> hasSalePerson(UUID saleId){
    return (root,query,cb)-> cb.equal(
        root.get("salesPerson").get("id"),saleId
    );
   }



   public static Specification<Client> hasStatus(ClientStatus status){
    return (root,query,cb)->cb.equal(root.get("status"), status);
   }


   public static  Specification<Client> hasSearch(String keyword){
    return (root,query,cb)->{

        String searchValue = "%"+keyword.toLowerCase()+"%";
        return cb.or(
            cb.like(
                cb.lower(root.get("firstName")),searchValue),
                cb.like( cb.lower(root.get("lastName")),searchValue),
                cb.like( cb.lower(root.get("email")),searchValue)
            );
    };
   }
}
