package com.saas.backend.serviceImpl;

import org.springframework.stereotype.Service;

import com.saas.backend.dto.RoleRequest;
import com.saas.backend.models.Role;
import com.saas.backend.repositories.RoleRepository;
import com.saas.backend.service.RoleService;

import lombok.RequiredArgsConstructor;




@Service 

@RequiredArgsConstructor 
public class RoleServiceImpl implements RoleService {


   private final  RoleRepository roleRepository;
    @Override
    public void createRole(RoleRequest roleRequest) {
        // this role later should be restricted to platform admin only, for now we will allow any user to create a role
        try{
   // check if role exists

            if (roleRepository.findByNameIgnoreCase(roleRequest.getName()).isPresent()) {
                throw new RuntimeException("Role with name " + roleRequest.getName() + " already exists");
            }   

       Role role =new Role();
       role.setName(roleRequest.getName());
       role.setDescription(roleRequest.getDescription()); 
       roleRepository.save(role);
        }catch(Exception e){
            throw new RuntimeException("Error creating role: " + e.getMessage());
        }
    }
    
}
