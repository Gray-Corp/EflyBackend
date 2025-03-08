package com.EFlyer.Bookings.APIValidations;

import lombok.*;

@Data
@AllArgsConstructor
public class ApiBaseResponses {
    private String validation_status;
    private String validation_Code;
    private String validation_message;
}
