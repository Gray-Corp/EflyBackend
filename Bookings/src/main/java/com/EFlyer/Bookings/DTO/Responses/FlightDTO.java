package com.EFlyer.Bookings.DTO.Responses;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FlightDTO {

    private String flightId;
    private String legXRefId;
    private String airways;
    private String legId;
    private String fareBaseAdt;
    private Integer seats;
    private String first_departure_airport;
    private String first_departure_date;
    private String first_departure_time;
    private String final_destination_airport;
    private String final_destination_date;
    private String final_destination_time;
    private Double total_distance_km;
    private String stops;
    private Map<String,String> baggage;
    private List<LegDTO> legDTOS;

}
