package com.saas.backend.serviceImpl;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.saas.backend.AccessHelper.CompanyAccessValidator;
import com.saas.backend.dto.ClientRequest;
import com.saas.backend.dto.ClientUpdate;
import com.saas.backend.models.Client;
import com.saas.backend.models.ClientStatus;
import com.saas.backend.models.Company;
import com.saas.backend.models.Guest;
import com.saas.backend.models.User;
import com.saas.backend.repositories.ClientRepository;
import com.saas.backend.repositories.CompanyRepository;
import com.saas.backend.repositories.GuestRepository;
import com.saas.backend.repositories.UserRepository;
import com.saas.backend.response.ClientResponse;
import com.saas.backend.service.ClientService;
import com.saas.backend.specification.ClientSpecification;

import lombok.RequiredArgsConstructor;



@Service 
@RequiredArgsConstructor 
public class ClientServiceImpl implements ClientService{


    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final ClientRepository clientRepository;
    private final GuestRepository guestRepository;
    private final CompanyAccessValidator companyAccessValidator;

    public ClientResponse createClient(ClientRequest request){
        try{

    // logic to get the company id and verify sales person with valid id 

    User user = userRepository.findById(request.getSaleId()).orElseThrow(()-> new RuntimeException("The id is not valid for the sale person"));


    //block admin to add client to its own id 
    if (user.getRole().getName().toUpperCase().equals("ADMIN")){
        throw new RuntimeException("Admin can't add Client to its id please select Sales person");
    }

    Company company= companyRepository.findById(user.getCompany().getId()).orElseThrow(()-> new RuntimeException("Company not found"));



    if (clientRepository.existsByCompanyIdAndEmail(company.getId(),request.getEmail())){
        throw new RuntimeException("Clients with this email already exists");
    }

    Client client = new Client();
    client.setCompany(company);
    client.setSalesPerson(user);
    client.setCountryOfResidence(request.getCountryOfResidence());
    client.setEmail(request.getEmail());
    client.setPhone(request.getPhone());
    client.setPreferredLanguage(request.getPreferredLanguage());
    client.setFirstName(request.getFirstName());
    client.setNotes(request.getNotes());
    client.setLastName(request.getLastName());
    client.setNationality(request.getNationality());
    client.setStatus(request.getStatus().ACTIVE);

    // save to database 
    clientRepository.save(client);
    return new ClientResponse(client.getCompany().getName(),client.getSalesPerson().getEmail(),client.getId(),client.getCreatedAt());  
    } catch(Exception e){
         throw new RuntimeException(e.getMessage());
    }
}

  

    @Override
    public Page<Client> getClients(Authentication auth,int page,int size,String sortBy,String search,String direction,ClientStatus status) {
        try {
            
               Sort sort =direction.equalsIgnoreCase("desc") ?
               Sort.by(sortBy).descending():Sort.by(sortBy).ascending();
               
               Pageable pageable=PageRequest.of(page,size,sort);

               Specification specification;
             
              
            User user = userRepository.findByEmail(auth.getName()).orElseThrow(()-> new RuntimeException("No user found with this id"));
            if (user.getRole().getName().toUpperCase().replace(" ", "_").equals("SALES_PERSON")){
                    specification= ClientSpecification.hasSalePerson(user.getId());
                                   
            }
            else{
                specification=ClientSpecification.hasCompany(user.getCompany().getId());
                
            }

            if (status != null){
                specification=specification.and(ClientSpecification.hasStatus(status));
            }
            if (search != null && !search.isBlank()){
                specification=specification.and(ClientSpecification.hasSearch(search));
            }
            return clientRepository.findAll(specification, pageable);
            
        } catch (Exception e) {
          throw new RuntimeException("Error: " + e.getMessage());
        }
    }



    @Override
    public Client getClientById(UUID id) {
        try{
            Client client = clientRepository.findById(id).orElseThrow(()-> new RuntimeException("No client found"));
            companyAccessValidator.validate(client.getCompany().getId());
            return client;
        }
        catch(Exception e){
                    throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Client editClient(UUID id,ClientUpdate request) {
     try{
        Client client = clientRepository.findById(id).orElseThrow(()-> new RuntimeException("Client not found"));
        companyAccessValidator.validate(client.getCompany().getId());
        if (request.getFirstName() != null) client.setFirstName(request.getFirstName());
        if (request.getLastName()!= null) client.setLastName(request.getLastName());
        if (request.getEmail()!=null) client.setEmail(request.getEmail());
        if (request.getNationality()!=null) client.setNationality(request.getNationality());
        if (request.getCountryOfResidence()  !=null) client.setCountryOfResidence(request.getCountryOfResidence());
        if (request.getPreferredLanguage() !=null) client.setPreferredLanguage(request.getPreferredLanguage());
        if (request.getPhone() != null)  client.setPhone(client.getPhone());
        if (request.getNotes() != null) client.setNotes(request.getNotes());
        clientRepository.save(client);
        return client;
         }
        catch(Exception e){
            throw new RuntimeException("Error"+ e.getMessage());
        }
    }



    @Override
    public List<Guest> getClientGuest(UUID id) {
       try{
        Client client = clientRepository.findById(id).orElseThrow(()-> new RuntimeException("Client not found"));

        companyAccessValidator.validate(client.getCompany().getId());
         // find by guests by client id 
         List<Guest> guests= guestRepository.findAllByClientId(client.getId());
        //  if (guests.isEmpty()){
        //     throw new RuntimeException("No guest found");
        //  }
         return guests;
       }catch(Exception e){
        throw new RuntimeException(e.getMessage());
       }
    
    }




    // delete client logic later
}
