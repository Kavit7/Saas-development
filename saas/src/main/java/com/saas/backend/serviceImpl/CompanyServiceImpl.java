package com.saas.backend.serviceImpl;



import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.saas.backend.dto.CompanyRequest;
import com.saas.backend.dto.CompanyUpdate;
import com.saas.backend.models.Company;
import com.saas.backend.models.SubscriptionPlan;
import com.saas.backend.models.User;
import com.saas.backend.repositories.CompanyRepository;
import com.saas.backend.repositories.SubscriptionPlanRepository;
import com.saas.backend.repositories.UserRepository;
import com.saas.backend.response.CompanyResponse;
import com.saas.backend.service.CompanyService;

import lombok.RequiredArgsConstructor;


@Service 
@RequiredArgsConstructor

public class CompanyServiceImpl implements CompanyService {
     
       private final CompanyRepository companyRepository;
       private final UserRepository userRepository;
       private final SubscriptionPlanRepository subscriptionPlanRepository;

   public  CompanyResponse createCompany(CompanyRequest companyRequest){
       
    try{

        if (companyRepository.findByNameIgnoreCase(companyRequest.getName()).isPresent()){
            throw new RuntimeException("Error obtained:-"+companyRequest.getName()+"is already Present" );
        }
    String slug = generateSlug(companyRequest.getName());
    SubscriptionPlan sbp= subscriptionPlanRepository.findSubscriptionPlanById(companyRequest.getSubscription_plan_id());
    Company company = new Company();

      company.setEmail(companyRequest.getEmail());
      company.setCountry(companyRequest.getCountry());
      company.setSlug(slug);
      company.setName(companyRequest.getName());
      company.setPhone(companyRequest.getPhone());
      company.setTimezone(companyRequest.getTimezone());
      company.setStatus(companyRequest.getStatus().ACTIVE);
      company.setSubscriptionPlan(sbp);
      // save 
      companyRepository.save(company);

      return new CompanyResponse(company.getId().toString(),company.getSlug(),company.getCreatedAt());

    }
    catch(Exception e){
         throw new RuntimeException(e.getMessage()); 
    }

    }
  


    public Page<Company> getAllCompany(int page, int size,String sortBy,String direction){

        try{

            Sort sort =direction.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

         Pageable pageable = PageRequest.of(page,size,sort);   
        return companyRepository.findAll(pageable);
        
        }
        catch(Exception e){
           throw new RuntimeException(e.getMessage());
        }
       
    }




    public Company getCompanyDetailsByIdOrName(UUID id, String Name,Authentication auth){
    try{
        Company company =companyRepository.findByIdOrName(id, Name).orElseThrow(()-> new RuntimeException("No company found"));
        // Lets check if your the Owner or your super admin or reservation Manager
        boolean isAdmin= getUserAuthority(auth);
        UUID currentUserId= userRepository.findByEmail(auth.getName()).orElseThrow().getId();

        //check your the owner
        User user= userRepository.findUserById(currentUserId);
        boolean isOwner= user.getId().equals(company.getId());
        if (!isAdmin && !isOwner){
            throw new AccessDeniedException("Dear user with Email: " +user.getEmail()+" You can't view Details with different Company");
        }
        return  company;
    }
    catch(Exception e){
        throw new RuntimeException(e.getMessage());
    }
    }




    public Company updateCompany (UUID id,CompanyUpdate companyUpdate){
        try{
            Company existing =companyRepository.findById(id). orElseThrow(()-> new RuntimeException("No Company with such Id"));
          
        
             if (companyUpdate.getName() != null) {
            String slug = generateSlug(companyUpdate.getName());
            existing.setName(companyUpdate.getName());
            existing.setSlug(slug);
            }
            if (companyUpdate.getCountry() != null) existing.setCountry(companyUpdate.getCountry());
            if (companyUpdate.getPhone()!=null) existing.setPhone(companyUpdate.getPhone());
            if (companyUpdate.getEmail()!=null) existing.setEmail(companyUpdate.getEmail());
            if (companyUpdate.getTimezone() !=null) existing.setTimezone(companyUpdate.getTimezone());
            // save to database
            companyRepository.save(existing);
            return existing;
        }
        catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }
    private String generateSlug(String name) {
    return name
            .toLowerCase()
            .trim()
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-|-$", "");
    }
    
    private boolean getUserAuthority(Authentication auth){
  boolean isAdmin = auth.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_SUPER_ADMIN"));
  return isAdmin;
    }
}
