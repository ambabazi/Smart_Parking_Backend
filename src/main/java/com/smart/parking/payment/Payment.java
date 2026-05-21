package com.smart.parking.payment;

import com.smart.parking.common.ReferenceCodeGenerator;
import com.smart.parking.reservation.Reservation;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

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

    @Column(nullable = false, unique = true, length = 36)
    private String uuid;

    @Column(nullable = false, unique = true, length = 50)
    private String referenceCode;

    @ManyToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    private BigDecimal amount;
    private String status;
    @Column(name = "flutterwave_id")
    private String transactionId;

    @PrePersist
    void assignPublicIdentifiers() {
        if (uuid == null || uuid.isBlank()) {
            uuid = UUID.randomUUID().toString();
        }
        if (referenceCode == null || referenceCode.isBlank()) {
            referenceCode = ReferenceCodeGenerator.paymentCode();
        }
    }
}
