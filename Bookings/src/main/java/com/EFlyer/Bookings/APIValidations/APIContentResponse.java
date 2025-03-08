package com.EFlyer.Bookings.APIValidations;

import lombok.*;

import java.util.HashMap;
import java.util.Map;


@Getter
@Setter
public class APIContentResponse <T> extends ApiBaseResponses{

    private Map<String,T> contents = new HashMap<>();

    public APIContentResponse(String validation_status, String validation_Code, String validation_message,
    String title,T content) {
        super(validation_status, validation_Code, validation_message);
        contents.put(title,content);
    }
}
