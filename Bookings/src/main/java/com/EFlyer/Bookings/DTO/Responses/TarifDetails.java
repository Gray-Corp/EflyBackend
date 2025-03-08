package com.EFlyer.Bookings.DTO.Responses;

import lombok.Data;

import java.util.List;

@Data
public class TarifDetails {
    private String tarifId;
    private String airways;
    private String price_per_adtSell;
    private String price_per_adtBuy;
    private String price_per_chdBuy;
    private String price_per_chdSell;
    private String price_per_infBuy;
    private String price_per_infSell;
    private String total_buy_Price;
    private String total_sell_Price;
    private Boolean refundable;
    private Boolean taxIncluded;
    private List<FareDTO> fareDTOList;
}
