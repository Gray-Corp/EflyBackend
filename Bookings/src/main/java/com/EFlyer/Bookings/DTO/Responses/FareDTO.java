package com.EFlyer.Bookings.DTO.Responses;

import lombok.Data;
import java.util.List;

@Data
public class FareDTO {

    private String fareId;
    private String fareType;
    private String airline;
    private String flightClass;
    private String departure_airport_code;
    private String destination_airport_code;
    private String passengerType;
    private String date;
    private String ticket_deadLine_date;
    private String fareBaseCode;
    private String currency;
    private String price;
    private List<FlightDTO> flightDTOS;
}
