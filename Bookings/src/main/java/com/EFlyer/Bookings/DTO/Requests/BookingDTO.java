package com.EFlyer.Bookings.DTO.Requests;

import lombok.Data;

import java.util.List;

@Data
public class BookingDTO {
    private Passenger billing_passenger;
    private List<Passenger> co_passengers;
    private String deliveryType;
    private List<String> flightIds;
    private String tarifId;
}
