package com.smart.parking.event;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

// What the admin sends to POST /events
public record EventRequest(

        @NotBlank(message = "Event name is required")
        String name,

        @NotNull
        Double latitude,

        @NotNull
        Double longitude,

        @NotNull @Positive
        Double radiusMetres,      // must be > 0

        @NotNull
        LocalDateTime startTime,

        @NotNull
        LocalDateTime endTime

) {}
