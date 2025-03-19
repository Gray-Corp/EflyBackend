package com.EFlyer.Bookings.Service;

import com.EFlyer.Bookings.DTO.Requests.FareRequestFilterDTO;

import java.util.List;
import java.util.Map;

public interface FlightAvailabilityService {

    Map<String, Object> flightAvailability(String depApt, String desApt, String depDate, String desDate, FareRequestFilterDTO fareRequestFilterDTO);

    Boolean dateValidation(String depDate, String desDate);
}
