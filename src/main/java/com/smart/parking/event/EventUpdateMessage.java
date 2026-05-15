package com.smart.parking.event;

public record EventUpdateMessage(
        Long eventId,
        String eventName,
        Double latitude,
        Double longitude,
        Double radiusMetres,
        Boolean active
) {}
