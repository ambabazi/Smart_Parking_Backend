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
public class ReservationResponseDTO {
    private Long id;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private Long parkingSpaceId;
    private String parkingSpaceName;
    private Integer slotCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal totalAmount;
    private Boolean paid;
    private Boolean verified;
    private String qrCode;
    private LocalDateTime createdAt;
}
