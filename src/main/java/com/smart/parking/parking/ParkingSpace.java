package com.smart.parking.parking;

import com.smart.parking.event.Event;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "parking_spaces")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingSpace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    // Event relationship — null until event mode activates
    // Event entity does not need to exist yet to compile this
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_event_id")
    private Event currentEvent;
}