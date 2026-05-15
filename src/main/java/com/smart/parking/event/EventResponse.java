package com.smart.parking.event;

import java.time.LocalDateTime;

public record EventResponse(
        Long id,
        String name,
        Double latitude,
        Double longitude,
        Double radiusMetres,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer activatedSpacesCount
) {}
