package com.EFlyer.Bookings.Service.impl;

import com.EFlyer.Bookings.DTO.Requests.BookingDTO;
import com.EFlyer.Bookings.DTO.Requests.Passenger;
import com.EFlyer.Bookings.DTO.Requests.PricingRequest;
import com.EFlyer.Bookings.DTO.Responses.*;
import com.EFlyer.Bookings.Service.FlightApiService;
import com.EFlyer.Bookings.YpsilonApiDocs.FlightApiConfig;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class FlightApiServiceImpl implements FlightApiService {

    @Override
    public Map<String, Object> flightAvailablity(String dep_apt, String des_apt, String dep_date, String des_date,
                                                 Map<String, Integer> passenger_type, String flight_class) {

        Map<String, Object> responseMap = new HashMap<>();
        String fareRequestXml = fareRequest_xml_body(dep_apt, des_apt, dep_date, des_date, passenger_type, flight_class);
        Map<String, String> xmlResponseFromAPi = flight_availability_xmlResponse(fareRequestXml, "", "https://stagingxml.ypsilon.net:11024/");

        Document doc = Jsoup.parse(xmlResponseFromAPi.get("response"), "", Parser.xmlParser());
        Elements tarif_Elements = doc.getElementsByTag("tarif");
        List<TarifDetails> tarifDetailsList = getTarifDetails_fromAPI(tarif_Elements);

        getFareDetails_fromAPI(tarifDetailsList, doc);
        getFlightAndLegDetails_fromAPI(tarifDetailsList, doc);
        getBaggageDetails_fromAPI(tarifDetailsList,doc);
        responseMap.put("Session", xmlResponseFromAPi.get("Session"));
        responseMap.put("xml_request",fareRequestXml);
        responseMap.put("body", tarifDetailsList);
        return responseMap;
    }

    @Override
    public Object bookFlight(BookingDTO bookingDTO, String session_id) {

        BookingResponse bookingResponse = new BookingResponse();
        Map<String,Object> response = new HashMap<>();

        Map<String, String> xml_response = flight_availability_xmlResponse(build_FlightBooking_xml_body(bookingDTO), session_id, "https://norristest.ypsilon.net:11025");
        Document bookingResponse_doc = Jsoup.parse(xml_response.get("response"), "", Parser.xmlParser());
        Elements bookingResponse_elements = bookingResponse_doc.getElementsByTag("bookingResponse");

        System.out.println();
        bookingResponse_elements.forEach(element -> {
            bookingResponse.setSuccess_status(Boolean.valueOf(element.attr("success")));
            bookingResponse.setFileKey(element.attr("fileKey"));
            bookingResponse.setBookingStatus(element.attr("bookingStatus"));
            bookingResponse.setTarifId(element.attr("tarifId"));
            bookingResponse.setTiketPrintedDate(element.attr("ticketPrintDate"));
            bookingResponse.setTicketPrintedTime(element.attr("ticketPrintTime"));
            bookingResponse.setE_ticket_able(Boolean.valueOf(element.attr("eticketable")));
            bookingResponse.setPowerPricerDisplay(element.attr("powerPricerDisplay"));
        });
        Document itinerary_doc = Jsoup.parse(xml_response.get("response"), "", Parser.xmlParser());
        Elements itinerary_elements = itinerary_doc.getElementsByTag("itinerary");
        List<Itinerary> itineraryList = new ArrayList<>();
        itinerary_elements.forEach(itinerary_element -> {
            Itinerary itinerary = new Itinerary();
            itinerary.setDeparture_airport_code(itinerary_element.attr("depApt"));
            itinerary.setDestination_airport_code(itinerary_element.attr("dstApt"));
            itinerary.setFlight_class(itinerary_element.attr("cosDescription"));
            itineraryList.add(itinerary);
        });
        bookingResponse.setItineraries(itineraryList);
       if (bookingResponse.getSuccess_status() == null)  response.put("Error_message",bookingResponse_doc.selectFirst("errorResponse").text()) ;

    response.put("booking_xml_request",build_FlightBooking_xml_body(bookingDTO));
    response.put("booking_response",bookingResponse);
    return response;
    //flight_availability_xmlResponse(build_FlightBooking_xml_body(bookingDTO),session_id,"https://norristest.ypsilon.net:11025");

}

    @Override
    public Object pricingRequest(String sessionId, PricingRequest pricingRequest) {
        Map<String,Object> response = new HashMap<>();
        response.put("flight_availability_xmlResponse",flight_availability_xmlResponse(build_pricingRequest_xml_body(pricingRequest),sessionId,"https://stagingxml.ypsilon.net:11024/"));
        response.put("build_pricingRequest_xml_body",build_pricingRequest_xml_body(pricingRequest));
        return response;
    }

    public Map<String,String> flight_availability_xmlResponse(String fareRequestXml,String session_id,String url) {

        System.out.println(fareRequestXml);
        System.out.println(session_id);

        Map<String,String> responseMap = new HashMap<>();
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            HttpPost httpPost = new HttpPost(url);

            // Set request headers
            FlightApiConfig flightApiConfig = new FlightApiConfig(session_id);
            flightApiConfig.Ypsilon_headers.forEach(httpPost::setHeader);


            // Attach compressed entity
            HttpEntity entity = new StringEntity(fareRequestXml);
            httpPost.setEntity(entity);

            // Execute request
            HttpResponse response = httpClient.execute(httpPost);
            responseMap.put("Session",response.getFirstHeader("Session").getElements()[0].toString());
            responseMap.put("response",EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
            responseMap.put("Error Msg",e.getMessage());
        }
        return responseMap;
    }

    private List<TarifDetails> getTarifDetails_fromAPI(Elements tarifElements){
        List<TarifDetails> tarifDetailsList = new ArrayList<>();

        tarifElements.forEach(tarif_Element->{
            TarifDetails individual_tarifDetails = new TarifDetails();
            List<FareDTO> individual_fareDTOList = new ArrayList<>();
            TarifPriceDetails tarifPriceDetails = new TarifPriceDetails();

            individual_tarifDetails.setTarifId(tarif_Element.attr("tarifId"));
            individual_tarifDetails.setRefundable(Boolean.valueOf(tarif_Element.attr("refundable")));
            individual_tarifDetails.setTaxIncluded(!(tarif_Element.attr("taxMode").equals("EXCL")));

            tarifPriceDetails.setPrice_per_chdBuy(Double.parseDouble(tarif_Element.attr("chdBuy")));
            tarifPriceDetails.setPrice_per_adtBuy(Double.parseDouble(tarif_Element.attr("adtBuy")));
            tarifPriceDetails.setPrice_per_infBuy(Double.parseDouble(tarif_Element.attr("infBuy")));

            tarifPriceDetails.setPrice_per_chdSell(Double.parseDouble(tarif_Element.attr("chdSell")));
            tarifPriceDetails.setPrice_per_adtSell(Double.parseDouble(tarif_Element.attr("adtSell")));
            tarifPriceDetails.setPrice_per_infSell(Double.parseDouble(tarif_Element.attr("infSell")));

            tarifPriceDetails.setTax_per_chd(Double.parseDouble(tarif_Element.attr("chdTax")));
            tarifPriceDetails.setTax_per_adt(Double.parseDouble(tarif_Element.attr("adtTax")));
            tarifPriceDetails.setTax_per_inf(Double.parseDouble(tarif_Element.attr("infTax")));

            individual_tarifDetails.setTarifPriceDetails(tarifPriceDetails);

            Elements fare_elements_forTarif = tarif_Element.getElementsByTag("fareXRef");
            fare_elements_forTarif.forEach(fareElement->{
                FareDTO fareDTO = new FareDTO();
                fareDTO.setFareId(fareElement.attr("fareId"));
                individual_fareDTOList.add(fareDTO);
            });
            individual_tarifDetails.setFareDTOList(individual_fareDTOList);
            tarifDetailsList.add(individual_tarifDetails);
        });

        return tarifDetailsList;
    }

    private void getFareDetails_fromAPI(List<TarifDetails> tarifDetailsList, Document doc){

        tarifDetailsList.forEach(tarifDetails -> tarifDetails.getFareDTOList().forEach(fareDTO -> {
            Elements fareElements_byId = doc.getElementsByAttributeValue("fareId", fareDTO.getFareId());
            tarifDetails.setAirways(fareElements_byId.attr("shared:vcr"));
            fareDTO.setAirline(fareElements_byId.attr("shared:vcr"));
            if (fareElements_byId.attr("cos").equals("E")){
                fareDTO.setFlightClass("Economy");
            } else if (fareElements_byId.attr("cos").equals("B")) {
                fareDTO.setFlightClass("Business");
            }
            else fareDTO.setFlightClass("Other");
            fareDTO.setFareType(fareElements_byId.attr("shared:fareType"));
            fareDTO.setDeparture_airport_code(fareElements_byId.attr("depApt"));
            fareDTO.setDestination_airport_code(fareElements_byId.attr("dstApt"));
            fareDTO.setDate(fareElements_byId.attr("date"));
            fareDTO.setTicket_deadLine_date(fareElements_byId.attr("ticketTimelimit"));
            fareDTO.setPassengerType(fareElements_byId.attr("paxType"));
        }));
    }

    private void getFlightAndLegDetails_fromAPI(List<TarifDetails> tarifDetailsList, Document doc){
        tarifDetailsList.forEach(tarifDetails -> {
            tarifDetails.getFareDTOList().forEach(fareDTO -> {
                Elements fareElements_byId = doc.getElementsByAttributeValue("fareId", fareDTO.getFareId());
                fareElements_byId.forEach(each_fareElement -> {
                    List<FlightDTO> flightDTOList = new ArrayList<>();
                    Elements flight_elements = each_fareElement.select("flight");
                    flight_elements.forEach(flight_element -> {
                        FlightDTO flightDTO = new FlightDTO();
                        flightDTO.setFlightId(flight_element.attr("flightId"));
                        flightDTO.setAirways(tarifDetails.getAirways());
                        flightDTOList.add(flightDTO);
                    });
                    fareDTO.setFlightDTOS(flightDTOList);
                });
            });

            tarifDetails.getFareDTOList().forEach(fareDTO -> fareDTO.getFlightDTOS().forEach(flightDTO -> {
                Elements flightElements_byId = doc.getElementsByAttributeValue("flightId", flightDTO.getFlightId());
                flightElements_byId.forEach(each_flightElement_byID->{
                    List<LegDTO> legDTOList = new ArrayList<>();
                    Elements leg_elements_flight = each_flightElement_byID.select("legXRef");
                    leg_elements_flight.forEach(leg_element_flight->{
                        LegDTO legDTO = new LegDTO();
                        legDTO.setLegId(leg_element_flight.attr("legId"));
                        legDTO.setSeats_available(leg_element_flight.attr("seats"));
                        legDTO.setFlight_class(leg_element_flight.attr("cosDescription"));

                        legDTOList.add(legDTO);
                    });
                    flightDTO.setLegDTOS(legDTOList);
                });
            }));

            tarifDetails.getFareDTOList().forEach(fareDTO -> fareDTO.getFlightDTOS().forEach(flightDTO -> {
                flightDTO.getLegDTOS().forEach(legDTO -> {
                    Elements leg_elements = doc.getElementsByAttributeValue("legId", legDTO.getLegId());
                    leg_elements.forEach(leg_element->{
                        legDTO.setDeparture_airport_code(leg_element.attr("depApt"));
                        legDTO.setDeparture_date(leg_element.attr("depDate"));
                        legDTO.setDeparture_time(leg_element.attr("depTime"));
                        legDTO.setArrival_date(leg_element.attr("arrDate"));
                        legDTO.setArrival_time(leg_element.attr("arrTime"));
                        legDTO.setDestination_airport_code(leg_element.attr("dstApt"));
                        legDTO.setSmoker(false);
                        legDTO.setDistance_miles(leg_element.attr("miles"));
                        legDTO.setFno(leg_element.attr("fno"));
                        legDTO.setStops(leg_element.attr("stops"));
                    });
                    legDTO.setDistance_km((Double.parseDouble(legDTO.getDistance_miles()) / 1.6));
                });
                LegDTO first_legDTO = flightDTO.getLegDTOS().getFirst();
                LegDTO final_legDTO = flightDTO.getLegDTOS().getLast();
                AtomicReference<Double> total_distance = new AtomicReference<>(0.0);
                flightDTO.setFirst_departure_airport(first_legDTO.getDeparture_airport_code());
                flightDTO.setFirst_departure_date(first_legDTO.getDeparture_date());
                flightDTO.setFirst_departure_time(first_legDTO.getDeparture_time());
                flightDTO.setFinal_destination_airport(final_legDTO.getDestination_airport_code());
                flightDTO.setFinal_destination_date(final_legDTO.getArrival_date());
                flightDTO.setFinal_destination_time(final_legDTO.getArrival_time());
                flightDTO.setStops(
                        flightDTO.getLegDTOS().size() < 2 ? "Direct Flight" : String.valueOf(flightDTO.getLegDTOS().size()-1)
                );
                flightDTO.getLegDTOS().forEach(legDTO -> total_distance.updateAndGet(v -> v + legDTO.getDistance_km()));
                flightDTO.setTotal_distance_km(Double.parseDouble(total_distance.toString()));
            }));
        });
    }

    private void getBaggageDetails_fromAPI(List<TarifDetails> tarifDetailsList,Document doc) {
        tarifDetailsList.forEach(tarifDetails -> tarifDetails.getFareDTOList().forEach(
                fareDTO -> fareDTO.getFlightDTOS().forEach(flightDTO -> {
                    AtomicInteger flightDTO_fba = new AtomicInteger(1);
                    flightDTO.getLegDTOS().forEach(legDTO -> {
                        String legXRefId = doc.getElementsByAttributeValue("legId", legDTO.getLegId()).attr("legXRefId");
                        legDTO.setBaggage_code(doc.getElementsByAttributeValue("elemId", legXRefId).attr("serviceID"));
                        int legDTO_fba = Integer.parseInt(legDTO.getBaggage_code().substring(4));

                        List<Map<String, String>> baggageOptions = List.of(
                                Map.of("FBA_1", "2 * 23 kg luggage & Hand luggage"),
                                Map.of("FBA_2", "1 * 20 kg luggage & Hand luggage"),
                                Map.of("FBA_3", "1 or 2 * 23 kg luggage & Hand luggage"),
                                Map.of("FBA_4", "2 * 20 kg luggage & Hand luggage"),
                                Map.of("FBA_5", "7kg Hand luggage only"),
                                Map.of("FBA_6", "Custom luggage")
                        );

                        if (flightDTO_fba.get() < legDTO_fba) {
                            flightDTO_fba.set(legDTO_fba);
                            flightDTO.setBaggage(baggageOptions.get(legDTO_fba-1));
                            System.out.println("TarifId :- "+tarifDetails.getTarifId()+" "+legDTO.getBaggage_code());
                        } else {
                            flightDTO.setBaggage(baggageOptions.get(flightDTO_fba.get()-1));
                        }
                    });
                })));
    }

    private String build_FlightBooking_xml_body(BookingDTO bookingDTO){
        String passenger_xml_body = build_passengerXmlBody_flightBooking(bookingDTO.getCo_passengers());

        Passenger passenger = bookingDTO.getBilling_passenger();

        return String.format(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<bookingRequest xmlns:shared=\"http://ypsilon.net/shared\" tarifId=\"%s\" allTermsAccepted=\"true\">\n" +
                        "    <billing businessPhone=\"%s\" cellPhone=\"%s\"\n" +
                        "    company=\"%s\" country=\"%s\" email=\"%s\"\n" +
                        "    fax=\"%s\" surname=\"%s\" firstname=\"%s\" gender=\"%s\"\n" +
                        "    dob=\"%s\" location=\"%s\" phone=\"%s\"\n" +
                        "    street=\"%s\" houseNo=\"%s\" zipcode=\"%s\"/>\n" +
                        "    <delivery type=\"NORMAL\"/>\n" +
                        "    <shared:flightIds>\n" +
                        "        <flightId>"+bookingDTO.getFlightIds().get(0)+"</flightId>\n" +
                        "        <flightId>"+bookingDTO.getFlightIds().get(1)+"</flightId>\n" +
                        "    </shared:flightIds>\n" +
                        "    <paxes> \n" +
                        passenger_xml_body +
                        "  </paxes> \n" +
                        "    <payment>\n" +
                        "    <onacc/> \n" +
                        "    </payment>\n" +
                        "    <remarks/>\n" +
                        "    <osiRemarks/>\n" +
                        "    <addServices/>\n" +
                        "    <owOptions/>\n" +
                        "</bookingRequest>",bookingDTO.getTarifId(),passenger.getBusinessPhone(),
                passenger.getCellPhone(),passenger.getCompany(),passenger.getCountryCode(),passenger.getEmail(),
                passenger.getFax(),passenger.getLastName(),passenger.getFirstName(),passenger.getGender(),passenger.getDob(),
                passenger.getLocation(),passenger.getPhone(),passenger.getStreet(),passenger.getHouseNumber(),passenger.getZipCode()

        );
    }

    private String build_passengerXmlBody_flightBooking(List<Passenger> passengerList){
        StringBuilder stringBuilder = new StringBuilder();
        passengerList.forEach(passenger -> {
            String co_passenger = String.format("<pax surname=\"%s\" firstname=\"%s\" dob=\"%s\" gender=\"%s\"/> \n",
                    passenger.getLastName(),passenger.getFirstName(),passenger.getDob(),passenger.getGender());
            stringBuilder.append(co_passenger);
        });

        String xml_body = "<pax surname=\"Tester\" firstname=\"John\" dob=\"1985-09-12\" gender=\"M\"/> \n";

        return stringBuilder.toString();

    }

    private String fareRequest_xml_body(String dep_apt,String des_apt, String dep_date,
                                        String des_date, Map<String,Integer> passenger_type, String flight_class){
        String passenger_body = build_passengerXmlBody_fareRequest(passenger_type);

        String xml_body = String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<fareRequest xmlns:shared=\"http://ypsilon.net/shared\" da=\"true\">\n" +
                        "  <vcrs>\n" +
                        "  <vcr>BA</vcr> <vcr>AC</vcr> <vcr>AF</vcr> <vcr>AI</vcr> <vcr>AM</vcr> <vcr>BR</vcr>\n" +
                        "  <vcr>CA</vcr> <vcr>CI</vcr> <vcr>CX</vcr> <vcr>DL</vcr> <vcr>EK</vcr> <vcr>ET</vcr>\n" +
                        "  <vcr>EY</vcr> <vcr>GA</vcr> <vcr>JL</vcr> <vcr>KLM</vcr> <vcr>KE</vcr> <vcr>LH</vcr>\n"+
                        "  <vcr>MH</vcr> <vcr>NZ</vcr> <vcr>PR</vcr> <vcr>QR</vcr> <vcr>QF</vcr> <vcr>SQ</vcr>\n" +
                        "  <vcr>SU</vcr> <vcr>SV</vcr> <vcr>TK</vcr> <vcr>UA</vcr> <vcr>VS</vcr> <vcr>WN</vcr>\n" +
                        "  <vcr>A3</vcr> <vcr>AD</vcr> <vcr>AK</vcr> <vcr>AS</vcr> <vcr>B6</vcr> <vcr>DN</vcr>\n" +
                        "  <vcr>FZ</vcr> <vcr>G3</vcr> <vcr>HA</vcr> <vcr>IB</vcr> <vcr>LS</vcr> <vcr>MS</vcr>\n" +
                        "  <vcr>NK</vcr> <vcr>PC</vcr> <vcr>PE</vcr> <vcr>PW</vcr> <vcr>SN</vcr> <vcr>TZ</vcr>\n" +
                        "  <vcr>U2</vcr> <vcr>VY</vcr> <vcr>W6</vcr> <vcr>5X</vcr> <vcr>7L</vcr> <vcr>CV</vcr>\n" +
                        "  <vcr>FX</vcr> <vcr>LD</vcr> <vcr>QY</vcr> <vcr>RU</vcr> " +
                        "  </vcrs>\n" +
                        "  <shared:fareTypes/>\n" +
                        "  <tourOps/>\n" +
                        "  <flights>\n" +
                        "  <flight depApt=\"%s\" dstApt=\"%s\" depDate=\"%s\"/>\n" +
                        "  <flight depApt=\"%s\" dstApt=\"%s\" depDate=\"%s\"/>\n" +
                        "  </flights>\n" +
                        "  <paxes>\n" +
                        passenger_body +
                        "  </paxes>\n" +
                        "  <paxTypes/>\n" +
                        "  <options>\n" +
                        "  <limit>25</limit>\n" +
                        "  <offset>1</offset>\n" +
                        "  <waitOnList>\n" +
                        "  <waitOn>ALL</waitOn>\n" +
                        "  </waitOnList>\n" +
                        "  </options>\n" +
                        "  <coses>\n" +
                        "  <cos>"+flight_class+"</cos> \n" +
                        "  </coses>\n" +
                        "</fareRequest>",dep_apt,des_apt,dep_date,des_apt,dep_apt,des_date);
        return xml_body;
    }

    private String build_passengerXmlBody_fareRequest(Map<String,Integer> passengerType_map) {

        StringBuilder stringBuilder = new StringBuilder(
                "<pax surname=\"Tester\" firstname=\"John\" dob=\"1985-09-12\" gender=\"M\"/>\n" +
                        "<pax surname=\"Tester\" firstname=\"Jane\" dob=\"1985-12-12\" gender=\"F\"/>\n");

        if (passengerType_map != null) {

            stringBuilder = new StringBuilder();

            StringBuilder finalStringBuilder = stringBuilder;
            passengerType_map.forEach((key, value) -> {
                if (key.equals("Adt")) finalStringBuilder.append(
                        "<pax surname=\"Tester\" firstname=\"Jane\" dob=\"1985-12-12\" gender=\"F\"/>\n".repeat(Math.max(0, value)));

                else if (key.equals("Chd")) {
                    finalStringBuilder.append(
                            "<pax surname=\"Tester\" firstname=\"Jane\" dob=\"2009-12-12\" gender=\"F\"/>\n".repeat(Math.max(0, value)));

                }
                else if (key.equals("Inf")) {
                    finalStringBuilder.append(
                            "<pax surname=\"Tester\" firstname=\"Jane\" dob=\"2023-12-12\" gender=\"F\"/>\n".repeat(Math.max(0, value)));

                }

            });

            return finalStringBuilder.toString();

        }

        return stringBuilder.toString();
    }

    private String build_pricingRequest_xml_body(PricingRequest pricingRequest){

        return String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<pricingRequest xmlns:shared=\"http://ypsilon.net/shared\" tarifId=\"%s\" ptl=\"false\">\n" +
                "<shared:flightIds>\n" +
                "    <flightId>"+pricingRequest.getFlightIds().get(0)+"</flightId>\n" +
                "    <flightId>"+pricingRequest.getFlightIds().get(1)+"</flightId>\n" +
                "  </shared:flightIds>\n" +
                "</pricingRequest>",pricingRequest.getTarif_id());

    }
}


