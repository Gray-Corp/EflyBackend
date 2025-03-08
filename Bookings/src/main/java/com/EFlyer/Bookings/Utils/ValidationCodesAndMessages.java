package com.EFlyer.Bookings.Utils;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Data
@Component
@PropertySource("classpath:ValidationCodesAndMessages.properties")
public class ValidationCodesAndMessages {

    @Value("${code.success.common}")
    private String commonSuccessCode;
    @Value("${code.failure.common}")
    private String commonFailureCode;
    @Value("${code.nullValues.received}")
    private String nullValuesReceivedCode;

    @Value("${message.success.save}")
    private String saveSuccessMessage;
    @Value("${message.success.getList}")
    private String getListSuccessMessage;
    @Value("${message.failure.dateValFailed}")
    private String dateValFailedMessage;


}
