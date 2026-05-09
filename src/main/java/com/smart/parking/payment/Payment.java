package com.smart.parking.payment;

import javax.persistence.*;

@Entity
public class Payment { @Id @GeneratedValue Long id; public String transactionId; }
