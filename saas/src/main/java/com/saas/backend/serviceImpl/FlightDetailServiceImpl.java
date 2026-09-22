package com.saas.backend.serviceImpl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.saas.backend.AccessHelper.CompanyAccessValidator;
import com.saas.backend.Exception.DuplicateException;
import com.saas.backend.Exception.ResourceNotFoundException;
import com.saas.backend.dto.FlightRequest;
import com.saas.backend.models.Client;
import com.saas.backend.models.FlightDetail;
import com.saas.backend.repositories.ClientRepository;
import com.saas.backend.repositories.FlightDetailsRepository;
import com.saas.backend.service.FlightDetailsService;

import lombok.RequiredArgsConstructor;


@Service 
@RequiredArgsConstructor 
public class FlightDetailServiceImpl implements FlightDetailsService {


    private final ClientRepository clientRepository;
    private final CompanyAccessValidator companyAccessValidator;
    private final FlightDetailsRepository flightDetailsRepository;
    @Override
    public FlightDetail createClientFlightDetails(UUID clientId, FlightRequest request) {
    
        // check the clientId
        Client client = clientRepository.findById(clientId).orElseThrow(()-> new ResourceNotFoundException("Client not found"));
        

        companyAccessValidator.validate(client.getCompany().getId());

        boolean exists= flightDetailsRepository.existsByClientIdAndFlightType(client.getId(),request.getFlightType());


        if (exists){
            throw new DuplicateException("The details with Flight Type "+request.getFlightType() + " to client " + client.getFirstName()+ "-"+client.getLastName()+" Already exist");
        }
       
        FlightDetail flightDetail= new FlightDetail();
        flightDetail.setClient(client);
        flightDetail.setAirline(request.getAirline());
        flightDetail.setAirport(request.getAirport());
        flightDetail.setArrivalDatetime(request.getArrivalDatetime());
        flightDetail.setDepartureDatetime(request.getDepartureDatetime());
        flightDetail.setFlightNumber(request.getFlightNumber());
        flightDetail.setFlightType(request.getFlightType());

        // save to database

          flightDetailsRepository.save(flightDetail);
        
          return flightDetail;        
    }
    @Override
    public List<FlightDetail> getClientFlightDetails(UUID clientId) {
        Client client=clientRepository.findById(clientId).orElseThrow(()-> new ResourceNotFoundException("Client not found"));
        companyAccessValidator.validate(client.getCompany().getId());
        List<FlightDetail> details=flightDetailsRepository.findAllByClientId(client.getId());
        return details;
    }
    
}
