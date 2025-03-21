package com.EFlyer.Bookings.YpsilonApiDocs;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;


@Data
public class FlightApiConfig {

    public Map<String,String> Ypsilon_headers = new HashMap<>();

   public FlightApiConfig(String session_id){

            Ypsilon_headers.put("Content-Type","application/xml");
            Ypsilon_headers.put("Accept","application/xml");
            Ypsilon_headers.put("Accept-Encoding", "gzip");
            Ypsilon_headers.put("api-version", "3.92");
            Ypsilon_headers.put("accessmode", "agency");
            Ypsilon_headers.put("accessid", "eflycha eflycha");
            Ypsilon_headers.put("authmode", "pwd");
            Ypsilon_headers.put("Authorization", "Basic ZWZseWNoOlFSWlRDdWM5OExfSUpDVmRZRGxrZnVXQVEyd0pXbSFE");
            Ypsilon_headers.put("session",session_id);
            Ypsilon_headers.put("Connection", "close");
    }

}
