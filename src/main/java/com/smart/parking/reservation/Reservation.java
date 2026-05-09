package com.smart.parking.reservation;

import jakarta.persistence.*;

@Entity
public class Reservation {
    @Id
    @GeneratedValue
    Long id;
    public Long userId;
    public Long parkingSpaceId;
}
