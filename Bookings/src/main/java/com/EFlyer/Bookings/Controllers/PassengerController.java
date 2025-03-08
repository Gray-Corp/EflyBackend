package com.EFlyer.Bookings.Controllers;

import com.EFlyer.Bookings.APIValidations.APIContentResponse;
import com.EFlyer.Bookings.APIValidations.ApiBaseResponses;
import com.EFlyer.Bookings.DTO.Requests.PassengerRequests;
import com.EFlyer.Bookings.Service.PassengerService;
import com.EFlyer.Bookings.Utils.EndPointURI;
import com.EFlyer.Bookings.Utils.ValidationCodesAndMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping(EndPointURI.BasePathApi)
public class PassengerController {

    @Autowired
    private PassengerService passengerService;
    @Autowired
    private ValidationCodesAndMessages validationCodesAndMessages;

    @PostMapping(EndPointURI.SavePassengerDetails)
    public ResponseEntity<Object> savePassengerDetails(@RequestBody PassengerRequests passengerRequest){
        passengerService.savePassengerDetails(passengerRequest);
        return ResponseEntity.ok(new ApiBaseResponses(
                "Success",
                validationCodesAndMessages.getCommonSuccessCode(),
                validationCodesAndMessages.getSaveSuccessMessage()
        ));
    }

    @GetMapping(EndPointURI.GetPassengerDetailsList)
    public ResponseEntity<Object> getPassengerDetailsList(){
        return ResponseEntity.ok(new APIContentResponse<>(
                "PassengerDetailsList",
                validationCodesAndMessages.getCommonSuccessCode(),
                validationCodesAndMessages.getSaveSuccessMessage(),
                "PassengerDetails",
                passengerService.getPassengerDetailsList()
        ));
    }
}
