package com.saas.backend.serviceImpl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.saas.backend.AccessHelper.CompanyAccessValidator;
import com.saas.backend.Exception.DuplicateException;
import com.saas.backend.Exception.ResourceNotFoundException;
import com.saas.backend.dto.FlightRequest;
import com.saas.backend.models.Client;
import com.saas.backend.models.FlightDetail;
import com.saas.backend.repositories.ClientRepository;
import com.saas.backend.repositories.FlightDetailsRepository;
import com.saas.backend.response.FlightDetailResponse;
import com.saas.backend.service.FlightDetailsService;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class FlightDetailServiceImpl implements FlightDetailsService {

    private final ClientRepository clientRepository;
    private final CompanyAccessValidator companyAccessValidator;
    private final FlightDetailsRepository flightDetailsRepository;

    private FlightDetailResponse mapToResponse(FlightDetail flightDetail) {
        if (flightDetail == null) return null;
        return FlightDetailResponse.builder()
                .id(flightDetail.getId())
                .clientId(flightDetail.getClient() != null ? flightDetail.getClient().getId() : null)
                .clientName(flightDetail.getClient() != null ? (flightDetail.getClient().getFirstName() + " " + flightDetail.getClient().getLastName()) : null)
                .flightType(flightDetail.getFlightType())
                .airline(flightDetail.getAirline())
                .flightNumber(flightDetail.getFlightNumber())
                .airport(flightDetail.getAirport())
                .arrivalDatetime(flightDetail.getArrivalDatetime())
                .departureDatetime(flightDetail.getDepartureDatetime())
                .createdAt(flightDetail.getCreatedAt())
                .updatedAt(flightDetail.getUpdatedAt())
                .build();
    }

    @Override
    public FlightDetailResponse createClientFlightDetails(UUID clientId, FlightRequest request) {
        Client client = clientRepository.findById(clientId).orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        companyAccessValidator.validate(client.getCompany().getId());

        boolean exists = flightDetailsRepository.existsByClientIdAndFlightType(client.getId(), request.getFlightType());

        if (exists) {
            throw new DuplicateException("The details with Flight Type " + request.getFlightType() + " to client " + client.getFirstName() + "-" + client.getLastName() + " Already exist");
        }
       
        FlightDetail flightDetail = new FlightDetail();
        flightDetail.setClient(client);
        flightDetail.setAirline(request.getAirline());
        flightDetail.setAirport(request.getAirport());
        flightDetail.setArrivalDatetime(request.getArrivalDatetime());
        flightDetail.setDepartureDatetime(request.getDepartureDatetime());
        flightDetail.setFlightNumber(request.getFlightNumber());
        flightDetail.setFlightType(request.getFlightType());

        
        flightDetailsRepository.save(flightDetail);
        
        return mapToResponse(flightDetail);        
    }

    @Override
    public List<FlightDetailResponse> getClientFlightDetails(UUID clientId) {
        Client client = clientRepository.findById(clientId).orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        companyAccessValidator.validate(client.getCompany().getId());
        List<FlightDetail> details = flightDetailsRepository.findAllByClientId(client.getId());
        return details.stream().map(this::mapToResponse).collect(Collectors.toList());
    }
}
