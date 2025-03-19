package com.EFlyer.Bookings.Service;

import com.EFlyer.Bookings.DTO.Requests.BookingDTO;
import com.EFlyer.Bookings.DTO.Requests.PricingRequest;

import java.util.Map;

public interface FlightApiService {

    Map<String,Object> flightAvailablity(String dep_apt, String des_apt,
                                         String dep_date, String des_date, Map<String,Integer> passenger_type,String flight_class);

    Object bookFlight(BookingDTO bookingDTO, String session_id);

    Object pricingRequest(String sessionId, PricingRequest pricingRequest);
}
