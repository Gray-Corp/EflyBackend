package com.EFlyer.Bookings.DTO.Requests;

import lombok.Data;

@Data
public class Passenger {
    private String firstName;
    private String lastName;
    private String dob;
    private String gender;
    private String phone;
    private String cellPhone;
    private String businessPhone;
    private String fax;
    private String email;
    private String company;
    private String houseNumber;
    private String street;
    private String location;
    private String country;
    private String countryCode;
    private String zipCode;
    private CreditCardDetails creditCardDetails;
}
