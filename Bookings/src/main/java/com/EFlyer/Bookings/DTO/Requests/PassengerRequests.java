package com.EFlyer.Bookings.DTO.Requests;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PassengerRequests{

    private Long id;
    private String full_name;
    private String address;
    private String mobile;
    private String Identification;
    private String passport;
    private LocalDate dob;
    private String gender;
    private String passengerType;

}