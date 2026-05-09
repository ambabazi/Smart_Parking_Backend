package com.smart.parking.payment;

import jakarta.persistence.*;

@Entity
public class Payment {
    @Id
    @GeneratedValue
    Long id;
    public String transactionId;
}
