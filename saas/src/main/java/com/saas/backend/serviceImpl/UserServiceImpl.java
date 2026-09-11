package com.saas.backend.serviceImpl;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.saas.backend.dto.UserRequest;
import com.saas.backend.models.Company;
import com.saas.backend.models.Role;
import com.saas.backend.models.User;
import com.saas.backend.repositories.CompanyRepository;
import com.saas.backend.repositories.RoleRepository;
import com.saas.backend.repositories.UserRepository;
import com.saas.backend.response.UserResponse;
import com.saas.backend.service.UserService;

import lombok.RequiredArgsConstructor;




@Service 
@RequiredArgsConstructor 
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final CompanyRepository companyRepository;
    public UserResponse createAdmin(UserRequest userRequest){
              
      try{
          if (userRepository.findByEmail(userRequest.getEmail()).isPresent()){
            throw new RuntimeException("User with that tha email"+ userRequest.getEmail()+" Already exists");
          }


    String hashPassword = passwordEncoder.encode(userRequest.getPassword());
    Role role = roleRepository.findByNameIgnoreCase(userRequest.getRoleName()).orElseThrow(()-> new RuntimeException("Role Not found"));
     Company company = companyRepository.findByNameIgnoreCase(userRequest.getCompanyName()).orElseThrow(()-> new RuntimeException("Company Not found"));
      User user = new User();
      user.setCompany(company);
      user.setEmail(userRequest.getEmail());
      user.setFirstName(userRequest.getFirstName());
      user.setLastName(userRequest.getLastName());
      user.setPhone(userRequest.getPhone());
      user.setStatus(userRequest.getStatus().ACTIVE);
      user.setRole(role);
      user.setPasswordHash(hashPassword);
     userRepository.save(user);


       return new UserResponse(user.getId(),user.getRole().getName(),user.getStatus(),user.getCreatedAt());
        
      }
      catch(Exception e){
        throw new RuntimeException(e.getMessage());
      }  
    }
}
