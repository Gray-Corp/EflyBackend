package com.EFlyer.Bookings.Service.impl;

import com.EFlyer.Bookings.DTO.Requests.FareRequestFilterDTO;
import com.EFlyer.Bookings.DTO.Responses.TarifDetails;
import com.EFlyer.Bookings.Service.FlightApiService;
import com.EFlyer.Bookings.Service.FlightAvailabilityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class FlightAvailabilityImpl implements FlightAvailabilityService {

    @Autowired
    private FlightApiService flightApiService;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Map<String, Object> flightAvailability(String depApt, String desApt, String depDate, String desDate,
                                                  FareRequestFilterDTO fareRequestFilterDTO) {

        List<String> airlineCodes = fareRequestFilterDTO.getAirline_codes();
        String flight_class = fareRequestFilterDTO.getFlight_class();
        Map<String,Integer> passengerType_map = fareRequestFilterDTO.getPassenger_type();
        Boolean ticketRefundable = fareRequestFilterDTO.getTicket_refundable();
        String fare_type = fareRequestFilterDTO.getFare_type();
        Double minPrice = fareRequestFilterDTO.getMinPrice();
        Double maxPrice = fareRequestFilterDTO.getMaxPrice();
        String minDepTime = fareRequestFilterDTO.getMinDepTime();
        String maxDepTime = fareRequestFilterDTO.getMaxDepTime();
        String minRtnTime = fareRequestFilterDTO.getMinRtnTime();
        String maxRtnTime = fareRequestFilterDTO.getMaxRtnTime();
        String duration = fareRequestFilterDTO.getDuration();
        String baggage_code = fareRequestFilterDTO.getBaggage_code();

        Map<String, Object> responses = new HashMap<>();

        try{
            Map<String, Object> flightAPI_responses = flightApiService.flightAvailablity(depApt,desApt,depDate,desDate,
                    passengerType_map,flight_class);

            List<TarifDetails> tarifDetailsList_fromAPI = (List<TarifDetails>)flightAPI_responses.get("body");

            tarifDetailsList_fromAPI.forEach(tarifDetails -> {
                        AtomicInteger integer = new AtomicInteger(0);
                        AtomicReference<String> tempString = new AtomicReference<>();
                        tarifDetails = price_calculation(passengerType_map,tarifDetails);
                        tarifDetails.getFareDTOList().forEach(fareDTO -> {
                                if (integer.getAndIncrement() % 2 ==1) {
                                    tempString.set(fareDTO.getDeparture_airport_code());
                                    fareDTO.setDeparture_airport_code(fareDTO.getDestination_airport_code());
                                    fareDTO.setDestination_airport_code(tempString.get());
                                }});
                    }
            );

            if (baggage_code != null) baggageFiltering(tarifDetailsList_fromAPI,baggage_code);

            if (minPrice != null && maxPrice != null){
                tarifDetailsList_fromAPI = tarifDetailsList_fromAPI.stream().filter(tarifDetails ->
                        tarifDetails.getTarifPriceDetails().getTaxAndSell_adt() >= minPrice &&
                                maxPrice >= tarifDetails.getTarifPriceDetails().getTaxAndSell_adt()
                ).collect(Collectors.toList());
            }

            if (ticketRefundable != null) tarifDetailsList_fromAPI = tarifDetailsList_fromAPI.stream().filter(
                    tarifDetails -> tarifDetails.getRefundable().equals(ticketRefundable)).collect(Collectors.toList());

            responses = tarifDetails_filterByAirways(tarifDetailsList_fromAPI,airlineCodes);
            responses.put("Session",flightAPI_responses.get("Session"));

        }catch (Exception e){
            e.printStackTrace();
        }

        return responses;
    }

    void baggageFiltering(List<TarifDetails> tarifDetailsListFromAPI, String baggageCode) {

        tarifDetailsListFromAPI.forEach(tarifDetails -> tarifDetails.setFareDTOList(
                tarifDetails.getFareDTOList().stream().peek(fareDTO -> fareDTO.setFlightDTOS(
                                fareDTO.getFlightDTOS().stream()
                                        .filter(flightDTO->flightDTO.getBaggage().containsKey(baggageCode))
                                        .collect(Collectors.toList())
                        ))
                        .filter(fareDTO -> !fareDTO.getFlightDTOS().isEmpty()).collect(Collectors.toList())
        ));

        tarifDetailsListFromAPI.removeIf(tarifDetails -> tarifDetails.getFareDTOList().isEmpty());



    }

    TarifDetails price_calculation(Map<String,Integer> passengerType_map, TarifDetails tarifDetails){
        passengerType_map.forEach((key,val)->{
            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            if (key.equals("Adt")) {
                double price_per_passenger = tarifDetails.getTarifPriceDetails().getPrice_per_adtSell();
                double tax_per_passenger = tarifDetails.getTarifPriceDetails().getTax_per_adt();
                double total_sell_price = tarifDetails.getTarifPriceDetails().getTotal_sell_Price();
                tarifDetails.getTarifPriceDetails().setTaxAndSell_adt(price_per_passenger+tax_per_passenger);
                double total_sell_price_withTax = total_sell_price + ((price_per_passenger + tax_per_passenger) * val);
                tarifDetails.getTarifPriceDetails().setTotal_sell_Price(Double.parseDouble(decimalFormat.format(total_sell_price_withTax)));
            }
            if (key.equals("Chd")) {
                Double price_per_passenger = tarifDetails.getTarifPriceDetails().getPrice_per_chdSell();
                Double tax_per_passenger = tarifDetails.getTarifPriceDetails().getTax_per_chd();
                double total_sell_price = tarifDetails.getTarifPriceDetails().getTotal_sell_Price();
                tarifDetails.getTarifPriceDetails().setTaxAndSell_chd(price_per_passenger+tax_per_passenger);
                double total_sell_price_withTax = total_sell_price + ((price_per_passenger + tax_per_passenger) * val);
                tarifDetails.getTarifPriceDetails().setTotal_sell_Price(Double.parseDouble(decimalFormat.format(total_sell_price_withTax)));
            }
            if (key.equals("Inf")) {
                Double price_per_passenger = tarifDetails.getTarifPriceDetails().getPrice_per_infSell();
                Double tax_per_passenger = tarifDetails.getTarifPriceDetails().getTax_per_inf();
                double total_sell_price = tarifDetails.getTarifPriceDetails().getTotal_sell_Price();
                tarifDetails.getTarifPriceDetails().setTaxAndSell_inf(price_per_passenger+tax_per_passenger);
                double total_sell_price_withTax = total_sell_price + ((price_per_passenger + tax_per_passenger) * val);
                tarifDetails.getTarifPriceDetails().setTotal_sell_Price(Double.parseDouble(decimalFormat.format(total_sell_price_withTax)));
            }
        });
        return tarifDetails;
    }

    Map<String,Object> tarifDetails_filterByAirways( List<TarifDetails> tarifDetailsList_fromAPI, List<String> airlineCodes ){
        Map<String,Object> responses = new HashMap<>();

        Map<String, List<TarifDetails>> grouped_tarifDetails_filtered = new HashMap<>();
        if (airlineCodes == null || airlineCodes.isEmpty()) {
            tarifDetailsList_fromAPI.sort(Comparator.comparing(TarifDetails::getTarifId));
            responses.put("Tarif_Details",tarifDetailsList_fromAPI);
            responses.put("total_tarifs",tarifDetailsList_fromAPI.size());
        }
        else {
            Map<String, List<TarifDetails>> grouped_tarifDetails = tarifDetailsList_fromAPI.stream()
                    .collect(Collectors.groupingBy(TarifDetails::getAirways));
            airlineCodes.forEach(airlineCode -> grouped_tarifDetails_filtered.put(airlineCode, grouped_tarifDetails.get(airlineCode)));
            responses.put("Tarif_Details",grouped_tarifDetails_filtered);
            responses.put("total_tarifs",grouped_tarifDetails_filtered.values().size());
        }
        return responses;
    }

    @Override
    public Boolean dateValidation(String depDate, String desDate) {

        return null;
    }
}
