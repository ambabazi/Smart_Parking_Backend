package com.smart.parking.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardDTO {
    private Long totalParkingSpaces;
    private Long totalReservationSlots;
    private Long activeReservations;
    private Long bookingsToday;
    private BigDecimal revenueToday;
    private Double occupancyPercentage;
}
