package com.smart.parking.qr;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VerifyRequest {

    @NotNull(message = "Reservation ID is required")
    private Long reservationId;

    @NotBlank(message = "QR content is required")
    private String qrContent;
}