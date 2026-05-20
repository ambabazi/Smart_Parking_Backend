package com.smart.parking.reservation;

import com.smart.parking.auth.User;
import com.smart.parking.parking.ParkingSpace;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations", indexes = {
        @Index(name = "idx_reservations_user_id", columnList = "user_id"),
        @Index(name = "idx_reservations_parking_space_id", columnList = "parking_space_id"),
        @Index(name = "idx_reservations_status", columnList = "status"),
        @Index(name = "idx_reservations_paid", columnList = "paid"),
        @Index(name = "idx_reservations_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_space_id", nullable = false)
    private ParkingSpace parkingSpace;

    @Column(name = "slot_count", nullable = false)
    @Builder.Default
    private Integer slotCount = 1;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    @Builder.Default
    private Boolean paid = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean verified = false;

    @Column(name = "qr_code", nullable = false, unique = true, length = 100)
    private String qrCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // Check-in/Check-out tracking
    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;

    @Column(name = "checked_out_at")
    private LocalDateTime checkedOutAt;

    @Column(name = "overtime_amount")
    @Builder.Default
    private BigDecimal overtimeAmount = BigDecimal.ZERO;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, CHECKED_IN, CHECKED_OUT, COMPLETED

    // Compatibility: provide boolean-style accessor used by legacy tests
    public boolean isPaid() {
        return Boolean.TRUE.equals(this.paid);
    }
}
