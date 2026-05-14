package com.smart.parking.parking;

// DTO = Data Transfer Object
// Never return your Entity directly — it exposes internal fields.
// Return only what the frontend needs.
public record ParkingDTO(
        Long id,
        String name,
        String address,
        Double latitude,
        Double longitude,
        Integer totalSlots,
        Integer availableSlots,
        Double pricePerSlot,
        Boolean eventEnabled
) {}
