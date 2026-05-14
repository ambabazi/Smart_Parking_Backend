package com.smart.parking.parking;

import com.smart.parking.auth.User;
import com.smart.parking.event.Event;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "parking_spaces")
@Data                    // Lombok: generates getters, setters, toString
@NoArgsConstructor       // Lombok: generates empty constructor (JPA needs this)
@AllArgsConstructor      // Lombok: generates full constructor
@Builder                 // Lombok: lets you do ParkingSpace.builder().name("X").build()
public class ParkingSpace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;            // The HOST or owner of this parking space

    @Column(nullable = false)
    private String name;          // e.g. "Kacyiru Parking"

    private String address;        // Human-readable address

    @Column(nullable = false)
    private Double latitude;       // GPS lat — used in Haversine formula

    @Column(nullable = false)
    private Double longitude;      // GPS lng — used in Haversine formula

    @Column(nullable = false)
    private Integer totalSlots;    // Max capacity

    @Column(nullable = false)
    private Integer availableSlots; // Decreases on booking, increases on cancellation

    @Column(nullable = false)
    private Double pricePerSlot;   // Price in RWF per slot per hour

    @Column(nullable = false)
    private Boolean eventEnabled = false; // BE2-06 flips this to true

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_event_id")
    private Event currentEvent;    // Set when eventEnabled = true

    // Compatibility: tests expect price per hour as BigDecimal
    public void setPricePerHour(java.math.BigDecimal pricePerHour) {
        this.pricePerSlot = pricePerHour == null ? null : pricePerHour.doubleValue();
    }

    public java.math.BigDecimal getPricePerHour() {
        return this.pricePerSlot == null ? null : java.math.BigDecimal.valueOf(this.pricePerSlot);
    }
}
