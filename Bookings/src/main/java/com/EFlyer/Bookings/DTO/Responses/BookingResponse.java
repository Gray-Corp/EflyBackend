package com.EFlyer.Bookings.DTO.Responses;

import lombok.Data;

import java.util.List;

@Data
public class BookingResponse {
    private String bookingResponseFromAPI;
    private Boolean success_status;
    private String fileKey;
    private String bookingStatus;
    private String tarifId;
    private String tiketPrintedDate;
    private String ticketPrintedTime;
    private Boolean e_ticket_able;
    private String powerPricerDisplay;
    private String ypsilon_PNR;
    private List<Itinerary> itineraries;
}
