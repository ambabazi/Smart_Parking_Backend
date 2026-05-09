package com.smart.parking.event;

import jakarta.persistence.*;

@Entity
public class Event {
    @Id
    @GeneratedValue
    Long id;
    public String name;
}
