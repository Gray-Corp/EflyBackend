package com.EFlyer.Bookings.DTO.Requests;

import lombok.Data;

@Data
public class CreditCardDetails {
    private String ccNo;
    private String ccCvcCode;
    private String ccOwner;
    private String ccVlDate;
    private String ccType;
}
