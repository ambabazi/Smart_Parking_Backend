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
public class DriverDashboardDTO {
    private Long totalReservations;
    private Long completedReservations;
    private Long activeReservations;
    private Long cancelledReservations;
    private Long upcomingReservations;
    private BigDecimal totalSpent;
    private LocalDateTime membersince;
}
