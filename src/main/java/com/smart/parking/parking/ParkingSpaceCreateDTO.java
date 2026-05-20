package com.smart.parking.parking;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParkingSpaceCreateDTO {
    @NotBlank(message = "Parking space name is required")
    private String name;

    private String address;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    @NotNull(message = "Total slots is required")
    @Positive(message = "Total slots must be greater than 0")
    private Integer totalSlots;

    @NotNull(message = "Price per slot is required")
    @Positive(message = "Price per slot must be greater than 0")
    private Double pricePerSlot;
}
