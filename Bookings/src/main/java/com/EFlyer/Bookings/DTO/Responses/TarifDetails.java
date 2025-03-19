package com.EFlyer.Bookings.DTO.Responses;

import lombok.Data;

import java.util.List;

@Data
public class TarifDetails {
    private String tarifId;
    private String airways;
    private Boolean refundable;
    private Boolean taxIncluded;
    private TarifPriceDetails tarifPriceDetails;
    private List<FareDTO> fareDTOList;
}
