package com.EFlyer.Bookings.DTO.Requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FareRequestFilterDTO {
    private List<String> airline_codes;
    private String flight_class;
    private Map<String,Integer> passenger_type;
    private Boolean ticket_refundable;
    private String fare_type;
    private Double minPrice;
    private Double maxPrice;
    private String minDepTime;
    private String maxDepTime;
    private String minRtnTime;
    private String maxRtnTime;
    private String duration;
    private String baggage_code;
}
