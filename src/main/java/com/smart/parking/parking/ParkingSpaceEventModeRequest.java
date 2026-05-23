package com.smart.parking.parking;

import lombok.Data;

@Data
public class ParkingSpaceEventModeRequest {
    private Boolean eventEnabled;
    private String linkedEventIdentifier;
}