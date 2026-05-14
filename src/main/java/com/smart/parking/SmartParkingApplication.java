package com.smart.parking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling   // Needed for event auto-deactivation (BE2)
public class SmartParkingApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartParkingApplication.class, args);
    }
}
