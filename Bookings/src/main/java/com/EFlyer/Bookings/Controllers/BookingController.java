package com.EFlyer.Bookings.Controllers;

import com.EFlyer.Bookings.APIValidations.APIContentResponse;
import com.EFlyer.Bookings.APIValidations.ApiBaseResponses;
import com.EFlyer.Bookings.Service.FlightApiService;
import com.EFlyer.Bookings.Service.FlightAvailabilityService;
import com.EFlyer.Bookings.Service.PassengerService;
import com.EFlyer.Bookings.Service.impl.HtmlXmlConverter;
import com.EFlyer.Bookings.Utils.EndPointURI;
import com.EFlyer.Bookings.Utils.ValidationCodesAndMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping(EndPointURI.BasePathApi)
public class BookingController {

    @Autowired
    private PassengerService passengerService;
    @Autowired
    private FlightApiService flightApiService;
    @Autowired
    private FlightAvailabilityService flightAvailabilityService;
    @Autowired
    private HtmlXmlConverter htmlXmlConverter;
    @Autowired
    private ValidationCodesAndMessages validationCodesAndMessages;

    @GetMapping(value = "/getFlights")
    public ResponseEntity<Object> checkAvailability(
            @RequestParam(name = "dep_date") String dep_date,
            @RequestParam(name = "des_date") String des_date,
            @RequestParam(name = "dep_apt") String dep_apt,
            @RequestParam(name = "des_apt") String des_apt,
            @RequestParam(name = "airline_codes",required = false) List<String> airline_codes,
            @RequestParam(name = "class_type",required = false) String class_type,
            @RequestParam(name = "passenger_type",required = false) String passenger_type,
            @RequestParam(name = "flight_class",required = false) String flight_class
            ){

        if (flight_class == null) flight_class = "E";
        Map<String,Object> responseMap = flightAvailabilityService.flightAvailability(
                dep_apt,des_apt,dep_date,des_date,airline_codes,class_type,passenger_type,flight_class);
        return ResponseEntity.ok(new APIContentResponse<>(
                "Success",
                validationCodesAndMessages.getCommonSuccessCode(),
                validationCodesAndMessages.getGetListSuccessMessage(),
                "flight_Availability_Details",
                responseMap.get("Tarif_Details")));
    }

}
