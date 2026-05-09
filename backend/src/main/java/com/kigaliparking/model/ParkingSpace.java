package com.kigaliparking.model;

import javax.persistence.*;

@Entity
public class ParkingSpace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Double latitude;
    private Double longitude;
    private Integer availableSlots;
    // getters/setters
}
