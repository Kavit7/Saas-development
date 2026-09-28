package com.saas.backend.serviceImpl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.saas.backend.Exception.DuplicateException;
import com.saas.backend.Exception.ResourceNotFoundException;
import com.saas.backend.dto.PriceTierRequest;
import com.saas.backend.models.PriceTier;
import com.saas.backend.repositories.PriceTierRepository;
import com.saas.backend.service.PriceTierService;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class PriceTierServiceImpl implements PriceTierService {
    private final PriceTierRepository priceTierRepository;
    @Override
    public PriceTier createPriceTier(PriceTierRequest request) {
            
        boolean exists= priceTierRepository.existsByNameAndCurrencyIgnoreCase(request.getName(),request.getCurrency());
        if(exists) throw new DuplicateException("Price Tier already exists");

        PriceTier priceTier = new PriceTier();

        priceTier.setName(request.getName());
        priceTier.setCurrency(request.getCurrency());
        priceTier.setMinPrice(request.getMinPrice());
        priceTier.setMaxPrice(request.getMaxPrice());
        priceTierRepository.save(priceTier);
        return priceTier;

    }
    

      public PriceTier updatPriceTier(UUID priceId,PriceTierRequest request){


        PriceTier priceTier= priceTierRepository.findById(priceId).orElseThrow(()-> new ResourceNotFoundException("Price not found") );

        if(request.getCurrency() != null ) priceTier.setCurrency(request.getCurrency());
        if(request.getMaxPrice() != null ) priceTier.setMaxPrice(request.getMaxPrice());
        if(request.getMinPrice() != null) priceTier.setMinPrice(request.getMinPrice());
        if(request.getName() != null) priceTier.setName(request.getName());

        //save 
        priceTierRepository.save(priceTier);
        return priceTier;

      }
      public List<PriceTier> getAllPriceTier(){
        List<PriceTier> priceTiers= priceTierRepository.findAll();
        return priceTiers;
        
      }
   public void deletePriceTier(UUID priceId){
       PriceTier priceTier= priceTierRepository.findById(priceId).orElseThrow(()-> new ResourceNotFoundException("Price not found") );
       priceTierRepository.delete(priceTier);
   }





}
