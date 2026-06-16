package com.smart.parking.reservation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingRequest {
    @NotNull(message = "Parking space identifier is required")
    private String parkingSpaceId;

    @NotNull(message = "Slot count is required")
    @Positive(message = "Slot count must be greater than 0")
    private Integer slotCount;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    /** One plate per slot when booking multiple cars (e.g. ["RAB123A","RAB456B"]). */
    private java.util.List<String> licensePlates;
}