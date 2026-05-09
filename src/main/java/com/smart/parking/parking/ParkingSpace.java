package com.smart.parking.parking;

import javax.persistence.*;

@Entity
public class ParkingSpace { @Id @GeneratedValue Long id; public String name; }
