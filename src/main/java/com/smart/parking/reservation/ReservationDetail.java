package com.smart.parking.reservation;

import java.time.LocalDateTime;
import java.math.BigDecimal;

public class ReservationDetail {
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String parkingSpaceName;
    private BigDecimal totalPrice;

    public ReservationDetail(Long id, LocalDateTime startTime, LocalDateTime endTime, String parkingSpaceName, BigDecimal totalPrice) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.parkingSpaceName = parkingSpaceName;
        this.totalPrice = totalPrice;
    }

    public Long getId() { return id; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public String getParkingSpaceName() { return parkingSpaceName; }
    public BigDecimal getTotalPrice() { return totalPrice; }
}
