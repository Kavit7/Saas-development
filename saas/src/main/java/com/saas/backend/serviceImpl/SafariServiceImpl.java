package com.saas.backend.serviceImpl;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.backend.AccessHelper.CompanyAccessValidator;
import com.saas.backend.Exception.ResourceNotFoundException;
import com.saas.backend.dto.ItineraryDayUpdateRequest;
import com.saas.backend.dto.ItineraryUpdateRequest;
import com.saas.backend.dto.SafariRequest;
import com.saas.backend.models.Client;
import com.saas.backend.models.ItineraryDay;
import com.saas.backend.models.Safari;
import com.saas.backend.models.SafariStatus;
import com.saas.backend.repositories.ClientRepository;
import com.saas.backend.repositories.GuestRepository;
import com.saas.backend.repositories.ItineraryDayRepository;
import com.saas.backend.repositories.SafariRepository;
import com.saas.backend.service.SafariService;

import lombok.RequiredArgsConstructor;


@Service 
@RequiredArgsConstructor 
public class SafariServiceImpl implements SafariService{
      private final ClientRepository clientRepository;
      private final SafariRepository safariRepository;
      private final CompanyAccessValidator companyAccessValidator;
      private final ItineraryDayRepository itineraryDayRepository;
      private final GuestRepository guestRepository;
      

    @Transactional 
    @Override
    public Safari createClientSafari(UUID clientId,SafariRequest request) {

        
         Client client = clientRepository.findById(clientId).orElseThrow(()-> new ResourceNotFoundException("Client not found"));
         companyAccessValidator.validate(client.getCompany().getId());
          
         // count guest
         Long count =guestRepository.countByClientId(client.getId());

         
        if (count ==0 ){
            throw new ResourceNotFoundException("this client has no guests");
        }
        if (request.getNumberOfPassengers()< 1 || request.getNumberOfPassengers() > count) {
            throw new ResourceNotFoundException("Number of pasenger must range between 1 and " +count);

        }
         
         Safari safari = new Safari();
         safari.setClient(client);
         safari.setSalesPerson(client.getSalesPerson());
         safari.setStartDate(request.getStartDate());
         safari.setEndDate(request.getEndDate());
         safari.setReferenceNumber(generateReference());
         safari.setNumberOfPassengers(request.getNumberOfPassengers());
         safari.setStatus(SafariStatus.DRAFT);
         safari.setNotes(request.getNotes());

        safariRepository.save(safari);
         //generate itenary days
         generateItineraryDay(safari);

         // save safari
        
         return safari;
         }
    




    private void generateItineraryDay(Safari safari){

           int dayNumber=1;
         LocalDate date = safari.getStartDate();


         while (!date.isAfter(safari.getEndDate())){
           

            ItineraryDay day = ItineraryDay.builder()
            .safari(safari)
            .dayNumber(dayNumber)
            .date(date)
            .destination(null)
            .build();
            itineraryDayRepository.save(day);
            date=date.plusDays(1);
            dayNumber++;
         }
    }


    private String generateReference(){
String characters = "ABCDEFGHIJKLMNPQRSTUVWXYZ123456789";


StringBuilder code= new StringBuilder("SAF-");

SecureRandom random= new SecureRandom();
for (int i=0; i<7; i++){
      code.append(characters.charAt(random.nextInt(characters.length())));
}
        return code.toString();
    }


    @Override
    public List<ItineraryDay> getItineraryDaySafari(UUID safariId) {
          Safari safari = safariRepository.findById(safariId).orElseThrow(()-> new ResourceNotFoundException("Safari not found"));
          List<ItineraryDay> itineraryDay= itineraryDayRepository.findBySafariId(safari.getId());
          return  itineraryDay;
    }




    @Transactional 
    @Override
    public void updateSafariItenaryDay(UUID safariId, ItineraryUpdateRequest request) {
         Safari safari = safariRepository.findById(safariId).orElseThrow(()-> new ResourceNotFoundException("Safari not found"));

         for(ItineraryDayUpdateRequest dayRequest: request.getDays()){

            ItineraryDay day= itineraryDayRepository.findByIdAndSafariId(dayRequest.getDayId(),safariId).orElseThrow(()-> new ResourceNotFoundException("Day not found"));

            day.setDestination(dayRequest.getDestination());
            day.setNotes(dayRequest.getNotes());
            itineraryDayRepository.save(day);
         }

    }  

}
