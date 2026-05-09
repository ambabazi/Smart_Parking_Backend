package com.smart.parking.parking;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ParkingSpace {
    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private int availableSlots;
    private java.math.BigDecimal pricePerHour;
}
