package com.smart.parking.payment;

import com.smart.parking.reservation.Reservation;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payments_reservation_id", columnList = "reservation_id"),
        @Index(name = "idx_payments_flutterwave_id", columnList = "flutterwave_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    private BigDecimal amount;
    private String status;
    @Column(name = "flutterwave_id")
    private String transactionId;
}
