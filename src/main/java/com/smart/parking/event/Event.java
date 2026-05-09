package com.smart.parking.event;

import javax.persistence.*;

@Entity
public class Event { @Id @GeneratedValue Long id; public String name; }
