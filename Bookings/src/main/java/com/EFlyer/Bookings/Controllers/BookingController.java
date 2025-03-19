package com.EFlyer.Bookings.Controllers;

import com.EFlyer.Bookings.APIValidations.APIContentResponse;
import com.EFlyer.Bookings.DTO.Requests.BookingDTO;
import com.EFlyer.Bookings.DTO.Requests.FareRequestFilterDTO;
import com.EFlyer.Bookings.DTO.Requests.PricingRequest;
import com.EFlyer.Bookings.Service.FlightApiService;
import com.EFlyer.Bookings.Service.FlightAvailabilityService;
import com.EFlyer.Bookings.Service.PassengerService;
import com.EFlyer.Bookings.Utils.EndPointURI;
import com.EFlyer.Bookings.Utils.ValidationCodesAndMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(EndPointURI.BasePathApi)
public class BookingController {

    @Autowired
    private PassengerService passengerService;
    @Autowired
    private FlightApiService flightApiService;
    @Autowired
    private FlightAvailabilityService flightAvailabilityService;

    @Autowired
    private ValidationCodesAndMessages validationCodesAndMessages;

    @GetMapping(value = "/getFlights")
    public ResponseEntity<Object> checkAvailability(
            @RequestParam(name = "dep_date") String dep_date,
            @RequestParam(name = "des_date") String des_date,
            @RequestParam(name = "dep_apt") String dep_apt,
            @RequestParam(name = "des_apt") String des_apt,
            @RequestParam(name = "airline_codes",required = false) List<String> airline_codes,
            @RequestParam(name = "passenger_type",required = false) String passenger_type,
            @RequestParam(name = "flight_class",required = false) String flight_class,
            @RequestParam(name = "ticket_refundable",required = false) Boolean ticket_refundable
            ){

        Map<String,Integer> passengerType_map = new HashMap<>();

        if (passenger_type != null) {

            String[] key_value_pairs = passenger_type.split(",");
            for (String type : key_value_pairs){
                String[] key_value = type.split(":");
                passengerType_map.put(key_value[0],Integer.parseInt(key_value[1]));
            }
        }
        else passengerType_map.put("Adt",1);

        if (flight_class == null) flight_class = "E";

        FareRequestFilterDTO fareRequestFilterDTO = new FareRequestFilterDTO();
        fareRequestFilterDTO.setAirline_codes(airline_codes);
        fareRequestFilterDTO.setFlight_class(flight_class);
        fareRequestFilterDTO.setPassenger_type(passengerType_map);
        fareRequestFilterDTO.setTicket_refundable(ticket_refundable);

        Map<String,Object> responseMap = flightAvailabilityService.flightAvailability(
                dep_apt,des_apt,dep_date,des_date,fareRequestFilterDTO
                );

//        Map<String,Object> responses = new HashMap<>();
//        responses.put("Total Tarifs",responseMap.get("total_tarifs"));
//        responses.put("available_Flights",responseMap.get("Tarif_Details"));

        return ResponseEntity.ok()
                .header("Session_Id",responseMap.get("Session").toString())
                .body(new APIContentResponse<>(
                "Success",
                validationCodesAndMessages.getCommonSuccessCode(),
                validationCodesAndMessages.getGetListSuccessMessage(),
                "flight_Availability_Details",
                responseMap.get("Tarif_Details")));
    }

    @PostMapping("/getFlights")
    public ResponseEntity<Object> checkAvailability(
            @RequestParam(name = "dep_date") String dep_date,
            @RequestParam(name = "des_date") String des_date,
            @RequestParam(name = "dep_apt") String dep_apt,
            @RequestParam(name = "des_apt") String des_apt,
            @RequestBody FareRequestFilterDTO fareRequestFilterDTO
    ){
        if (fareRequestFilterDTO.getFlight_class() == null) fareRequestFilterDTO.setFlight_class("E");
        if (fareRequestFilterDTO.getPassenger_type() == null)
            fareRequestFilterDTO.setPassenger_type(new HashMap<>(Map.of("Adt",1)));
        if (fareRequestFilterDTO.getMinPrice() == null) fareRequestFilterDTO.setMinPrice(0.00);
        if (fareRequestFilterDTO.getMaxPrice() == null) fareRequestFilterDTO.setMaxPrice(99999999999999.00);

        Map<String,Object> responseMap = flightAvailabilityService.flightAvailability(dep_apt,des_apt,dep_date,des_date,fareRequestFilterDTO);
        return ResponseEntity.ok().header("Session_Id",responseMap.get("Session").toString()).body(new APIContentResponse<>(
                "Success",
                validationCodesAndMessages.getCommonSuccessCode(),
                validationCodesAndMessages.getGetListSuccessMessage(),
                "flight_Availability_Details",
                responseMap.get("Tarif_Details")));
    }

    @PostMapping("/bookFlight")
    public ResponseEntity<Object> bookFlight(
            @RequestHeader("session_id") String session_id,
            @RequestBody BookingDTO bookingDTO){
        flightApiService.bookFlight(bookingDTO,session_id);
        return ResponseEntity.ok().body( new APIContentResponse<>(
                        "Success",
                        "40000",
                        "Booking Details Received Successfully",
                        "Results",
                flightApiService.bookFlight(bookingDTO,session_id)
                )
        );
    }

    @PostMapping("/pricing")
    public ResponseEntity<Object> priceRequest(
            @RequestHeader String session_id,
            @RequestBody PricingRequest pricingRequest
    ){
        return ResponseEntity.ok().body(flightApiService.pricingRequest(session_id,pricingRequest));
    }

}
