package com.smart.parking.reservation;

import java.time.LocalDateTime;

public class AvailabilityResponse {
    private boolean available;
    private int availableSlots;
    private double estimatedPrice;
    private double pricePerHour;
    private long estimatedHours;

    public AvailabilityResponse(boolean available, int availableSlots, double estimatedPrice, double pricePerHour, long estimatedHours) {
        this.available = available;
        this.availableSlots = availableSlots;
        this.estimatedPrice = estimatedPrice;
        this.pricePerHour = pricePerHour;
        this.estimatedHours = estimatedHours;
    }

    public boolean isAvailable() { return available; }
    public int getAvailableSlots() { return availableSlots; }
    public double getEstimatedPrice() { return estimatedPrice; }
    public double getPricePerHour() { return pricePerHour; }
    public long getEstimatedHours() { return estimatedHours; }
}
