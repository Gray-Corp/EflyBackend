package com.EFlyer.Bookings.Service.impl;

import com.EFlyer.Bookings.DTO.Requests.PassengerRequests;
import com.EFlyer.Bookings.DTO.Responses.PassengerResponse;
import com.EFlyer.Bookings.Entities.Passenger;
import com.EFlyer.Bookings.Repository.PassengerRepository;
import com.EFlyer.Bookings.Service.PassengerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PassengerServiceImpl implements PassengerService {

    @Autowired
    private PassengerRepository passengerRepository;
    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public void savePassengerDetails(PassengerRequests passengerRequest) {
        Passenger passenger = objectMapper.convertValue(passengerRequest, Passenger.class);
        passengerRepository.save(passenger);
    }

    @Override
    public List<PassengerResponse> getPassengerDetailsList() {

        return passengerRepository.findAll().stream().map(
                response->{
                    PassengerResponse passengerResponse = new PassengerResponse();
                    BeanUtils.copyProperties(response,passengerResponse);
                    return passengerResponse;
                }
        ).collect(Collectors.toList());
    }
}
