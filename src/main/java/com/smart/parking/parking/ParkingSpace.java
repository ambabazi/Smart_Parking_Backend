package com.smart.parking.parking;

import jakarta.persistence.*;

@Entity
public class ParkingSpace {
    @Id
    @GeneratedValue
    Long id;
    public String name;
}
