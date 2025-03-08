package com.EFlyer.Bookings.Entities;

import com.EFlyer.Bookings.Utils.Date_Time_Utils;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Getter
@Setter
public class Bookings extends Date_Time_Utils {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String travel_type;
    private Timestamp departure_dateTime;
    private Timestamp return_dateTime;
    private Integer passenger_count;
    private String service_class;
    private String baggage_preference;

}
