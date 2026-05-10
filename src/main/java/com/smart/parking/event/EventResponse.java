package com.smart.parking.event;

import java.time.LocalDateTime;

// What we send back — includes activatedSpacesCount so the
// admin can confirm how many spaces were switched on
public record EventResponse(
        Long id,
        String name,
        Double latitude,
        Double longitude,
        Double radiusMetres,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer activatedSpacesCount   // e.g. 4 — tells admin the radius worked
) {}
