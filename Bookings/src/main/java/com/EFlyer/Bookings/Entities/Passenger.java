package com.EFlyer.Bookings.Entities;

import com.EFlyer.Bookings.Utils.Date_Time_Utils;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDate;

@Getter
@Setter
@Entity
public class Passenger extends Date_Time_Utils {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String full_name;
    private String first_name;
    private String last_name;
    private String gender;
    private String title;
    private Timestamp dob;
    private String country_code;
    private String zip_code;
    private String house_number;
    private String street;
    private String city;
    private String email_address;
    private String mobile_number;
}
