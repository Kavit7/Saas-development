package com.saas.backend.serviceImpl;


import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.saas.backend.dto.UserRequest;
import com.saas.backend.dto.UserStatusRequest;
import com.saas.backend.models.Company;
import com.saas.backend.models.Role;
import com.saas.backend.models.SubscriptionStatus;
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
       
     Long totalUsers= userRepository.countByCompany(company);

     if ( totalUsers >= company.getSubscriptionPlan().getMaxUsers()){
        throw new RuntimeException("You can't exceed maximum user plan");
     } 
     
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


    public Page<User> getAllUsers(int page , int size, String sortBy,String direction){



      try{
            Sort sort=direction.equalsIgnoreCase("desc")
              ? Sort.by(sortBy).descending(): Sort.by(sortBy).ascending();

              Pageable pageable= PageRequest.of(page,size,sort);

              return userRepository.findAll(pageable);
      }

      catch(Exception e){
        throw new RuntimeException("Error: "+ e.getMessage());
      }
    }

    public User getUserById(UUID id,Authentication auth){
          try{

           boolean isAdmin = getUserAuthority(auth);
           boolean isOwner= userRepository.findByEmail(auth.getName()).orElseThrow().getId().equals(id);
           if (!isAdmin && !isOwner){
            throw new AccessDeniedException("You have no Permision to view this Resource");
           }
            User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found with that id"));
            return user;

          }
          catch(Exception e)
          {
            throw new RuntimeException(e.getMessage());
          }

    }


    public  User updateUserStatus(UUID id ,UserStatusRequest status,Authentication auth){
      try{
        
      User user = userRepository.findById(id).orElseThrow(()-> new RuntimeException("User not found"));
       // prevent admin to deactivate / suspend it own account
      boolean isMe=userRepository.findByEmail(auth.getName()).orElseThrow().getId().equals(user.getId());
      if (isMe){
        throw new AccessDeniedException("You Forbidden To Change Your Own status");
      }
      user.setStatus(status.getStatus());
      userRepository.save(user);
      return user;
      }
          
      catch(Exception e){
        throw new RuntimeException("Error: " + e.getMessage());
      }
    }
  private boolean getUserAuthority(Authentication auth){
  boolean isAdmin = auth.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_SUPER_ADMIN"));
  return isAdmin;
    }
}
