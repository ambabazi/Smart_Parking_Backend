package com.smart.parking.reservation;

import com.smart.parking.auth.User;
import com.smart.parking.parking.ParkingSpace;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String licensePlate;   // e.g. "RAD 123 A"

    // Which user made this booking
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Which parking space was booked
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_space_id", nullable = false)
    private ParkingSpace parkingSpace;

    // How many slots (1–5)
    @Column(nullable = false)
    private Integer slotCount;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    // RWF total = slotCount × pricePerSlot × hours
    @Column(nullable = false)
    private Double totalAmount;

    // UUID token — shown as QR code to the driver
    @Column(nullable = false, unique = true)
    private String qrCode;

    // Set to true after Flutterwave webhook fires (BE1-08)
    private Boolean paid = false;

    // Set to true after attendant scans QR (BE1-05)
    private Boolean verified = false;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status = ReservationStatus.PENDING_PAYMENT;

    @CreatedDate
    private LocalDateTime createdAt;
}

// Separate enum file — or put inside the entity class
public enum ReservationStatus {
    PENDING_PAYMENT, PAID, VERIFIED, CANCELLED
}
