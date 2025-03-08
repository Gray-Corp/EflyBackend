package com.EFlyer.Bookings.Service.impl;

import com.EFlyer.Bookings.DTO.Responses.TarifDetails;
import com.EFlyer.Bookings.Service.FlightApiService;
import com.EFlyer.Bookings.Service.FlightAvailabilityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class FlightAvailabilityImpl implements FlightAvailabilityService {

    @Autowired
    private FlightApiService flightApiService;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Map<String, Object> flightAvailability(String depApt, String desApt, String depDate,
                                                  String desDate, List<String> airlineCodes, String classType,
                                                  String passenger_type, String flight_class) {

        Map<String, Object> responses = new HashMap<>();

        try{
            Map<String, Object> flightAPI_responses = flightApiService.flightAvailablity(depApt,desApt,depDate,desDate,passenger_type,flight_class);

            List<TarifDetails> tarifDetailsList_fromAPI = (List<TarifDetails>)flightAPI_responses.get("body");

            tarifDetailsList_fromAPI.forEach(
                    tarifDetails -> {
                        AtomicInteger integer = new AtomicInteger(0);
                        AtomicReference<String> tempString = new AtomicReference<>();
                        tarifDetails.getFareDTOList().forEach(
                                fareDTO -> {
                    if (integer.getAndIncrement() % 2 ==1) {
                        tempString.set(fareDTO.getDeparture_airport_code());
                        fareDTO.setDeparture_airport_code(fareDTO.getDestination_airport_code());
                        fareDTO.setDestination_airport_code(tempString.get());
                    }
                    });
                    }
            );

            Map<String, List<TarifDetails>> grouped_tarifDetails = tarifDetailsList_fromAPI.stream()
                    .collect(Collectors.groupingBy(TarifDetails::getAirways));

            Map<String, List<TarifDetails>> grouped_tarifDetails_filtered = new HashMap<>();
            if (airlineCodes == null || airlineCodes.isEmpty()) {
                tarifDetailsList_fromAPI.sort(Comparator.comparing(TarifDetails::getTarifId));
                responses.put("Tarif_Details",tarifDetailsList_fromAPI);
            }
            else {
                airlineCodes.forEach(airlineCode -> grouped_tarifDetails_filtered.put(airlineCode, grouped_tarifDetails.get(airlineCode)));
                responses.put("Tarif_Details",grouped_tarifDetails_filtered);
            }

        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        return responses;
    }

    @Override
    public Boolean dateValidation(String depDate, String desDate) {

        return null;
    }
}
