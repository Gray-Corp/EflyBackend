package com.EFlyer.Bookings.DTO.Responses;

import lombok.Data;

@Data
public class LegDTO {
    private String legId;
    private String departure_airport_code;
    private String departure_date;
    private String departure_time;
    private String destination_airport_code;
    private String destination_date;
    private String destination_time;
    private String arrival_date;
    private String arrival_time;
    private String total_duration;
    private String flight_class;
    private String distance_miles;
    private Double distance_km;
    private String baggage_code;
    private String fno;
    private String stops;
    private String seats_available;
    private Boolean smoker;
}
