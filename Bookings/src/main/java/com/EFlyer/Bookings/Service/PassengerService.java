package com.EFlyer.Bookings.Service;

import com.EFlyer.Bookings.DTO.Requests.PassengerRequests;
import com.EFlyer.Bookings.DTO.Responses.PassengerResponse;

import java.util.List;

public interface PassengerService {
    void savePassengerDetails(PassengerRequests passengerRequest);

    List<PassengerResponse> getPassengerDetailsList();
}
