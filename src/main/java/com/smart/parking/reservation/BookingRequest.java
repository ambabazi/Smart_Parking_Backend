package com.smart.parking.reservation;


import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

// What the frontend sends in the POST /reservations body
public record BookingRequest(



        @NotNull
        Long parkingSpaceId,

        @NotNull @Min(1) @Max(5)   // enforce 1–5 at the API layer
        Integer slotCount,

        @NotNull
        LocalDateTime startTime,

        @NotNull
        LocalDateTime endTime,

        // @NotBlank = must not be null AND must not be empty/spaces
        // @Pattern  = Rwandan plate format: RAD 123 A
        //             [A-Z]{2,3} = 2-3 letters
        //             \s\d{3}\s  = space, 3 digits, space
        //             [A-Z]      = 1 final letter
        // This rejects "123", "hello", "" before it even hits the service
        @NotBlank(message = "License plate is required")
        @Pattern(
                regexp = "^[A-Z]{2,3}\\s\\d{3}\\s[A-Z]$",
                message = "Invalid plate format. Example: RAD 123 A"
        )
        String licensePlate

) {}
