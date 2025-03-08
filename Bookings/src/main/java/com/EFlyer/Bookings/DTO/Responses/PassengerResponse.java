package com.EFlyer.Bookings.DTO.Responses;

import com.EFlyer.Bookings.Utils.Date_Time_Utils;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PassengerResponse{

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