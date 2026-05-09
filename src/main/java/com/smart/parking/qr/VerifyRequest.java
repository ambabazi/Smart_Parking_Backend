package com.smart.parking.qr;

import lombok.Data;

@Data
public class VerifyRequest {
    private String token; // The UUID scanned from the QR code
}