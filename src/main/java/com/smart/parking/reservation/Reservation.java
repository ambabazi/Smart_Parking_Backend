package com.smart.parking.reservation;

import com.smart.parking.auth.User;
import com.smart.parking.parking.ParkingSpace;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "parking_space_id")
    private ParkingSpace parkingSpace;

    private int slotCount;
    private String qrCode;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean paid;
    private boolean verified;
}
