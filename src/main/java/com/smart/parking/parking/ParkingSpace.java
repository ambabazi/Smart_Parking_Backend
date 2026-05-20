package com.smart.parking.parking;

import com.smart.parking.auth.User;
import com.smart.parking.event.Event;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "parking_spaces", indexes = {
        @Index(name = "idx_parking_spaces_owner_id", columnList = "owner_id"),
        @Index(name = "idx_parking_spaces_current_event_id", columnList = "current_event_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingSpace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String name;

    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Integer totalSlots;

    @Column(nullable = false)
    private Integer availableSlots;

    @Column(nullable = false)
    private Double pricePerSlot;

    @Column(nullable = false)
    @Builder.Default
    private Boolean eventEnabled = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_event_id")
    private Event currentEvent;

    public void setPricePerHour(java.math.BigDecimal pricePerHour) {
        this.pricePerSlot = pricePerHour == null ? null : pricePerHour.doubleValue();
    }

    public java.math.BigDecimal getPricePerHour() {
        return this.pricePerSlot == null ? null : java.math.BigDecimal.valueOf(this.pricePerSlot);
    }
}
