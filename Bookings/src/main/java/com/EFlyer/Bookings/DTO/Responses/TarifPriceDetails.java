package com.EFlyer.Bookings.DTO.Responses;

import lombok.Data;

@Data
public class TarifPriceDetails {
    private double price_per_adtSell;
    private double price_per_adtBuy;
    private double tax_per_adt;
    private double taxAndSell_adt;
    private double price_per_chdBuy;
    private double price_per_chdSell;
    private double tax_per_chd;
    private double taxAndSell_chd;
    private double price_per_infBuy;
    private double price_per_infSell;
    private double tax_per_inf;
    private double taxAndSell_inf;
    private double total_buy_Price;
    private double total_sell_Price = 0;
}
