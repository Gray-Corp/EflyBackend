package com.EFlyer.Bookings.Service.impl;

import com.EFlyer.Bookings.DTO.Responses.FareDTO;
import com.EFlyer.Bookings.DTO.Responses.FlightDTO;
import com.EFlyer.Bookings.DTO.Responses.LegDTO;
import com.EFlyer.Bookings.DTO.Responses.TarifDetails;
import com.EFlyer.Bookings.Service.FlightApiService;
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
import java.util.concurrent.atomic.AtomicReference;

@Service
public class FlightApiServiceImpl implements FlightApiService {

    @Override
    public Map<String, Object> flightAvailablity(String dep_apt, String des_apt, String dep_date, String des_date,
                                                 String passenger_type, String flight_class) {

        Map<String,Object> responseMap = new HashMap<>();
        Map<String,String> xmlResponseFromAPi  = flight_availability_xmlResponse(dep_apt,des_apt,dep_date,des_date,passenger_type,flight_class);
        // Parse XML response
        Document doc = Jsoup.parse(xmlResponseFromAPi.get("response"), "", Parser.xmlParser());
        Elements tarif_Elements = doc.getElementsByTag("tarif");
        List<TarifDetails> tarifDetailsList = new ArrayList<>();

        tarif_Elements.forEach(tarif_Element->{
            TarifDetails individual_tarifDetails = new TarifDetails();
            List<FareDTO> individual_fareDTOList = new ArrayList<>();
            List<String> fareIdList = new ArrayList<>();
            individual_tarifDetails.setTarifId(tarif_Element.attr("tarifId"));
            individual_tarifDetails.setRefundable(Boolean.valueOf(tarif_Element.attr("refundable")));
            individual_tarifDetails.setTaxIncluded(
                    !(tarif_Element.attr("taxMode").equals("EXCL"))
            );
            individual_tarifDetails.setPrice_per_chdBuy(tarif_Element.attr("chdBuy"));
            individual_tarifDetails.setPrice_per_adtBuy(tarif_Element.attr("adtBuy"));
            individual_tarifDetails.setPrice_per_infBuy(tarif_Element.attr("infBuy"));
            individual_tarifDetails.setTotal_buy_Price(
                    String.valueOf(
                            Double.parseDouble(tarif_Element.attr("chdBuy")) +
                                    Double.parseDouble(tarif_Element.attr("adtBuy")) +
                                    Double.parseDouble(tarif_Element.attr("infBuy"))
                    )
            );
            individual_tarifDetails.setPrice_per_chdSell(tarif_Element.attr("chdSell"));
            individual_tarifDetails.setPrice_per_adtSell(tarif_Element.attr("adtSell"));
            individual_tarifDetails.setPrice_per_infSell(tarif_Element.attr("infSell"));
            individual_tarifDetails.setTotal_sell_Price(
                    String.valueOf(
                            Double.parseDouble(tarif_Element.attr("chdSell")) +
                                    Double.parseDouble(tarif_Element.attr("adtSell")) +
                                    Double.parseDouble(tarif_Element.attr("infSell"))
            ));

            Elements fare_elements_forTarif = tarif_Element.getElementsByTag("fareXRef");
            fare_elements_forTarif.forEach(fareElement->{
                FareDTO fareDTO = new FareDTO();
                fareDTO.setFareId(fareElement.attr("fareId"));
                individual_fareDTOList.add(fareDTO);
            });
                individual_tarifDetails.setFareDTOList(individual_fareDTOList);
                tarifDetailsList.add(individual_tarifDetails);
        });

        tarifDetailsList.forEach(tarifDetails -> {
            tarifDetails.getFareDTOList().forEach(fareDTO -> {
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
                    }
            );
        });

       tarifDetailsList.forEach(tarifDetails -> {
           tarifDetails.getFareDTOList().forEach(fareDTO -> {
               Elements fareElements_byId = doc.getElementsByAttributeValue("fareId", fareDTO.getFareId());
               fareElements_byId.forEach(each_fareElement -> {
                   List<FlightDTO> flightDTOList = new ArrayList<>();
                   Elements flight_elements = each_fareElement.select("flight");
                   flight_elements.forEach(flight_element -> {
                       FlightDTO flightDTO = new FlightDTO();
                       flightDTO.setFlightId(flight_element.attr("flightId"));
                       flightDTOList.add(flightDTO);
                   });
                   fareDTO.setFlightDTOS(flightDTOList);
               });
           });

           tarifDetails.getFareDTOList().forEach(fareDTO -> {
               fareDTO.getFlightDTOS().forEach(flightDTO -> {
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
               });

           });

           tarifDetails.getFareDTOList().forEach(fareDTO -> {
               fareDTO.getFlightDTOS().forEach(flightDTO -> {
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
               });
           });
       });
        responseMap.put("Session",xmlResponseFromAPi.get("Session"));
        responseMap.put("body",tarifDetailsList);
        return responseMap;
    }

    public Map<String,String> flight_availability_xmlResponse(String dep_apt,String des_apt, String dep_date,
                                                              String des_date, String passenger_type, String flight_class) {

        String passenger_body = passenger_type_xml_body(passenger_type);
        String bod =  "<pax surname=\"Tester\" firstname=\"John\" dob=\"1985-09-12\" gender=\"M\"/>\n"+
                                 "<pax surname=\"Tester\" firstname=\"Jane\" dob=\"1985-12-12\" gender=\"F\"/>\n"+
                                 "<pax surname=\"Tester\" firstname=\"Jane\" dob=\"1985-12-12\" gender=\"F\"/>\n";

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
                "  <limit>20</limit>\n" +
                "  <offset>0</offset>\n" +
                "  <waitOnList>\n" +
                "  <waitOn>ALL</waitOn>\n" +
                "  </waitOnList>\n" +
                "  </options>\n" +
                "  <coses>\n" +
                "  <cos>"+flight_class+"</cos> \n" +
                "  </coses>\n" +
                "</fareRequest>",dep_apt,des_apt,dep_date,des_apt,dep_apt,des_date);

        Map<String,String> responseMap = new HashMap<>();
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // API Endpoint
            String url = "https://stagingxml.ypsilon.net:11024/";
            HttpPost httpPost = new HttpPost(url);
            // Set request headers
            httpPost.setHeader("Content-Type", "application/xml");
            httpPost.setHeader("Accept", "application/xml");
            httpPost.setHeader("Accept-Encoding", "gzip, deflate");
            httpPost.setHeader("api-version", "3.92");
            httpPost.setHeader("accessmode", "agency");
            httpPost.setHeader("accessid", "eflycha eflycha");
            httpPost.setHeader("authmode", "pwd");
            httpPost.setHeader("session", "");
            httpPost.setHeader("Connection", "close");
            httpPost.setHeader("Authorization", "Basic ZWZseWNoOlFSWlRDdWM5OExfSUpDVmRZRGxrZnVXQVEyd0pXbSFE");

            // Attach compressed entity
            HttpEntity entity = new StringEntity(xml_body);
            httpPost.setEntity(entity);

            // Execute request
            HttpResponse response = httpClient.execute(httpPost);
            System.out.println();
            responseMap.put("Session",response.getFirstHeader("Session").getElements()[0].toString());
            responseMap.put("response",EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8));

        } catch (Exception e) {
            e.printStackTrace();
            responseMap.put("Error Msg",e.getMessage());
        }
        return responseMap;
    }

    private String passenger_type_xml_body(String passengerType) {

        StringBuilder stringBuilder = new StringBuilder("<pax surname=\"Tester\" firstname=\"John\" dob=\"1985-09-12\" gender=\"M\"/>\n" +
                "<pax surname=\"Tester\" firstname=\"Jane\" dob=\"1985-12-12\" gender=\"F\"/>\n");



                if (passengerType != null) {
                    stringBuilder = new StringBuilder();
                    String[] key_value_pairs = passengerType.split(",");
                    Map<String,String> passengerType_map = new HashMap<>();

                    for (String type : key_value_pairs){
                        String[] key_value = type.split(":");
                        passengerType_map.put(key_value[0],key_value[1]);
                    }

                    for (String passenger_count: passengerType_map.values())
                    {
                        int passenger_count_num = Integer.parseInt(passenger_count);
                        stringBuilder.append("<pax surname=\"Tester\" firstname=\"Jane\" dob=\"1985-12-12\" gender=\"F\"/>\n".repeat(Math.max(0, passenger_count_num)));
                    }
                }

                return stringBuilder.toString();
    }
}


