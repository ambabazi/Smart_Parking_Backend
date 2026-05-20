package com.smart.parking.notification;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SmsRequest {

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Message is required")
    private String message;
}
