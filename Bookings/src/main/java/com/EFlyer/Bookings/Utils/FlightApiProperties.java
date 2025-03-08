package com.EFlyer.Bookings.Utils;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:FlightBookingAPI.properties")
@Data
public class FlightApiProperties {

    @Value("${api.base.url}")
    private String apiBaseUrl;

    @Value("${api.key}")
    private String apiKey;


}
