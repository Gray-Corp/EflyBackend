package com.EFlyer.Bookings.YpsilonApiDocs;

import lombok.Data;

import java.util.Map;


@Data
public class FlightApiConfig {

    public static Map<String,String> Ypsilon_headers = Map.of(
            "Content-Type","application/xml",
            "Accept","application/xml",
            "Accept-Encoding", "gzip, deflate",
            "api-version", "3.92",
            "accessmode", "agency",
            "accessid", "eflycha eflycha",
            "authmode", "pwd",
            "Connection", "close",
            "Authorization", "Basic ZWZseWNoOlFSWlRDdWM5OExfSUpDVmRZRGxrZnVXQVEyd0pXbSFE");

}
