package com.smart.parking.reservation;

import javax.persistence.*;

@Entity
public class Reservation { @Id @GeneratedValue Long id; public Long userId; public Long parkingSpaceId; }
