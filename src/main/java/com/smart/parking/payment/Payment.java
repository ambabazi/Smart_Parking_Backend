package com.smart.parking.payment;

import com.smart.parking.reservation.Reservation;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    private BigDecimal amount;
    private String status;
    private String transactionId;
}
