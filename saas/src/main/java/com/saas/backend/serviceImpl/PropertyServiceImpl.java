package com.saas.backend.serviceImpl;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.saas.backend.Exception.DuplicateException;
import com.saas.backend.Exception.ResourceNotFoundException;
import com.saas.backend.dto.PropertyRequest;
import com.saas.backend.models.PriceTier;
import com.saas.backend.models.Property;
import com.saas.backend.models.PropertyCategory;
import com.saas.backend.models.User;
import com.saas.backend.models.VerificationStatus;
import com.saas.backend.repositories.PriceTierRepository;
import com.saas.backend.repositories.PropertyCategoryRepository;
import com.saas.backend.repositories.PropertyRepository;
import com.saas.backend.response.PropertyResponse;
import com.saas.backend.service.PropertyService;
import com.saas.backend.specification.PropertySpecification;

import lombok.RequiredArgsConstructor;



@Service 
@RequiredArgsConstructor 

public class PropertyServiceImpl implements PropertyService {
    private final PropertyRepository propertyRepository;
    private final PropertyCategoryRepository propertyCategoryRepository;
    private final PriceTierRepository priceTierRepository;


    @Override
    public PropertyResponse createProperty(PropertyRequest request) {
        
        // check if propertyExists
        boolean exists = propertyRepository.existsByName(request.getName());
        if (exists){
            throw new DuplicateException("The Property AlreadyExist");
        }



        // check price tier if exists
        PriceTier priceTier = priceTierRepository.findById(request.getPriceId()).orElseThrow(()-> new ResourceNotFoundException("The Price Tier Not exists"));

        // check property category if exists
        PropertyCategory propertyCategory = propertyCategoryRepository.findById(request.getCategoryId()).orElseThrow(()-> new ResourceNotFoundException("No property Catgory found "));
        // Verified Status
       
        
        //check creator
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        User createdBy= (User) authentication.getPrincipal();
        // set slug
        String slug = generateSlug(request.getName());
         Property property = new Property();
         property.setName(request.getName());
         property.setSlug(slug);
         property.setCategory(propertyCategory);
         property.setPriceTier(priceTier);
         property.setContactEmail(request.getContactEmail());
         property.setContactName(request.getContactName());
         property.setContactPhone(request.getContactPhone());
         property.setCountry(request.getCountry());
         property.setDescription(request.getDescription());
         property.setLocation(request.getLocation());
         property.setRegion(request.getRegion());
         property.setWebsite(request.getWebsite());
         property.setVerificationStatus(VerificationStatus.PENDING);
         property.setCreatedBy(createdBy);
         // save Property


         propertyRepository.save(property);

         return new PropertyResponse(property.getId(), property.getName(), property.getCreatedBy().getEmail());



     
    }

    @Override
    public Page<Property> getAllProperty(int page ,int size,String sortBy,String direction, String search, VerificationStatus status) {
         
        Sort sort =direction.equalsIgnoreCase("desc") ?
        Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
         
        Pageable pageable= PageRequest.of(page,size,sort);

        Specification specification;
        specification= PropertySpecification.hasStatus(status);
        
        if( search != null && !search.isBlank()){
          specification= specification.and(PropertySpecification.hasSearch(search));
        }
        return propertyRepository.findAll(specification, pageable);
    }

    @Override
    public Property getPropertyById(UUID PropId) {
        Property property =propertyRepository.findById(PropId).orElseThrow(()-> new ResourceNotFoundException("No Property found"));
        return property;
    }

   
   @Override
    public Property UpdateProperty(UUID propId, PropertyRequest request) {

    // 1. Find property
    Property property = propertyRepository.findById(propId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("No Property found"));

    // 2. Check duplicate property name
    boolean exists = propertyRepository.existsByNameAndIdNot(
            request.getName(),
            propId
    );

    if (exists) {
        throw new DuplicateException("Property name already exists");
    }

    // 3. Find Price Tier
    PriceTier priceTier = priceTierRepository.findById(request.getPriceId())
            .orElseThrow(() ->
                    new ResourceNotFoundException("The Price Tier does not exist"));

    // 4. Find Property Category
    PropertyCategory propertyCategory =
            propertyCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("No Property Category found"));

    // 5. Update fields
    property.setName(request.getName());
    property.setSlug(generateSlug(request.getName()));
    property.setCategory(propertyCategory);
    property.setPriceTier(priceTier);
    property.setContactEmail(request.getContactEmail());
    property.setContactName(request.getContactName());
    property.setContactPhone(request.getContactPhone());
    property.setCountry(request.getCountry());
    property.setDescription(request.getDescription());
    property.setLocation(request.getLocation());
    property.setRegion(request.getRegion());
    property.setWebsite(request.getWebsite());

    // 6. Save updated property
    return propertyRepository.save(property);
}
    @Override
    public void PropertyVerification(UUID propId) {
          Property property =propertyRepository.findById(propId).orElseThrow(()-> new ResourceNotFoundException("No Property found"));
          //get User
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        User verifiedBy= (User) authentication.getPrincipal();
        OffsetDateTime verifiedAt=OffsetDateTime.now();
          property.setVerificationStatus(VerificationStatus.VERIFIED);   
          property.setVerifiedBy(verifiedBy);
          property.setVerifiedAt(verifiedAt);
          propertyRepository.save(property);
    }
    private String generateSlug(String name){
        String slug= name.toLowerCase().replace(" ", "-");
        return slug;
    }
     
}
