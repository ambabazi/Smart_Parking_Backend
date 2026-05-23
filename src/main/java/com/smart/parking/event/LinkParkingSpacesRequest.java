package com.smart.parking.event;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class LinkParkingSpacesRequest {
    @NotEmpty(message = "At least one parking space id is required")
    private List<Long> parkingSpaceIds;
}