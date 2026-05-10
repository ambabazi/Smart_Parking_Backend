package com.smart.parking.event;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.smart.parking.parking.ParkingSpace;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;            // e.g. "Kigali Arena Concert"

    @Column(nullable = false)
    private Double latitude;         // venue GPS lat

    @Column(nullable = false)
    private Double longitude;        // venue GPS lng

    @Column(nullable = false)
    private Double radiusMetres;     // activation zone e.g. 500.0

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    // All parking spaces activated by this event.
    // mappedBy = "currentEvent" means ParkingSpace.currentEvent
    // is the owning side of the relationship.
    // cascade MERGE — when we save the event, JPA also updates
    // the related parking spaces automatically.
    @OneToMany(mappedBy = "currentEvent", cascade = CascadeType.MERGE)
    private List<ParkingSpace> activatedSpaces = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;
}
