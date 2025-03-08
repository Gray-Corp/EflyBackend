package com.EFlyer.Bookings.Service;

import java.util.List;
import java.util.Map;

public interface FlightAvailabilityService {

    Map<String, Object> flightAvailability(String depApt, String desApt, String depDate,
                                           String desDate, List<String> airlineCodes, String classType,
                                           String passenger_type, String flight_class);

    Boolean dateValidation(String depDate, String desDate);
}
