package com.EFlyer.Bookings.DTO.Requests;

import lombok.Data;

import java.util.List;

@Data
public class PricingRequest {
    private String tarif_id;
    private List<String> flightIds;
}
