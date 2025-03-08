package com.EFlyer.Bookings.Service;

import java.util.Map;

public interface FlightApiService {

    Map<String,Object> flightAvailablity(String dep_apt, String des_apt,
                                         String dep_date, String des_date, String passenger_type,String flight_class);
}
