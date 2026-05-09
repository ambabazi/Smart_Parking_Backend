package com.kigaliparking.model;

import javax.persistence.*;

@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long parkingSpaceId;
    private Integer slotCount;
    private String qrCode;
    // getters/setters
}
