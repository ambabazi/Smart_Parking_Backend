package com.smart.parking.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerDashboardDTO {
    private Long totalParkingSpaces;
    private Long totalSlots;
    private Long availableSlots;
    private Long occupiedSlots;
    private Double occupancyPercentage;
    private Long activeReservations;
    private Long totalReservations;
    private BigDecimal totalRevenue;
    private BigDecimal revenueToday;
    private LocalDateTime memberSince;
}
