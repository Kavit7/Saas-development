package com.saas.backend.serviceImpl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.saas.backend.dto.PlatformAdminRequest;
import com.saas.backend.models.PlatformAdmin;
import com.saas.backend.repositories.PlatformAdminRepository;
import com.saas.backend.response.PlatformAdminResponse;
import com.saas.backend.service.PlatformAdminService;

import lombok.RequiredArgsConstructor;


@Service 
@RequiredArgsConstructor 
public class PlatformAdminServiceImpl implements PlatformAdminService{
       private final PlatformAdminRepository platformAdminRepository;
       private final PasswordEncoder passwordEncoder;
    @Override
    public PlatformAdminResponse createPlatformAdmin(PlatformAdminRequest request) {
         try{
           // check if admin is present
         if (platformAdminRepository.findByEmail(request.getEmail()).isPresent()){
            throw new RuntimeException("The Admin with this Email Already exist");

         }

        String passwordHash= passwordEncoder.encode(request.getPassword());

        PlatformAdmin admin= new PlatformAdmin();
        admin.setFirstName(request.getFirstName());
        admin.setLastName(request.getLastName());
        admin.setEmail(request.getEmail());
        admin.setPasswordHash(passwordHash);
        admin.setPhone(request.getPhone());
        admin.setPlatformRole(request.getRole());
        admin.setStatus(request.getStatus().ACTIVE);


        platformAdminRepository.save(admin);


        return new PlatformAdminResponse(admin.getId(),admin.getFirstName(),admin.getLastName(),admin.getPlatformRole(),admin.getStatus());

         }

         catch(Exception e){
             throw new RuntimeException(e.getMessage());
         }

        
        



    }


    
}
