package com.smart.parking.reservation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutResponse {
    private String reservationReferenceCode;
    private LocalDateTime checkedOutAt;
    private LocalDateTime bookedUntil;
    @Builder.Default
    private Boolean hasOvertime = false;
    @Builder.Default
    private Long overtimeMinutes = 0L;
    private BigDecimal overtimeAmount;
    private String message;
}
