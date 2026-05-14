package com.smart.parking.reservation;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BookingRequest {
    private Long parkingSpaceId;
    private Integer slotCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}