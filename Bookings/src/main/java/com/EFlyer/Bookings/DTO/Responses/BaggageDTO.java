package com.EFlyer.Bookings.DTO.Responses;

import lombok.Data;

@Data
public class BaggageDTO {
    private String baggage_code;
    private Integer baggage_weight;
    private String baggage_description;
}
