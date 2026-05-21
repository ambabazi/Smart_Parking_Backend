package com.smart.parking.common;

import com.smart.parking.event.Event;
import com.smart.parking.event.EventRepository;
import com.smart.parking.parking.ParkingSpace;
import com.smart.parking.parking.ParkingSpaceRepository;
import com.smart.parking.reservation.Reservation;
import com.smart.parking.reservation.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EntityIdentifierResolver {

    private final ParkingSpaceRepository parkingSpaceRepository;
    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;

    public ParkingSpace resolveParkingSpace(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Parking space identifier is required");
        }
        String trimmed = identifier.trim();
        return parkingSpaceRepository.findByReferenceCode(trimmed)
                .or(() -> parkingSpaceRepository.findByUuid(trimmed))
                .or(() -> parkingSpaceRepository.findByNameIgnoreCase(trimmed))
                .or(() -> parseLong(trimmed).flatMap(parkingSpaceRepository::findById))
                .orElseThrow(() -> new ResourceNotFoundException("Parking space not found: " + trimmed));
    }

    public Reservation resolveReservation(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Reservation identifier is required");
        }
        String trimmed = identifier.trim();
        return reservationRepository.findByReferenceCode(trimmed)
                .or(() -> reservationRepository.findByUuid(trimmed))
                .or(() -> parseLong(trimmed).flatMap(reservationRepository::findById))
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + trimmed));
    }

    public Event resolveEvent(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Event identifier is required");
        }
        String trimmed = identifier.trim();
        return eventRepository.findByNameIgnoreCase(trimmed)
                .or(() -> parseLong(trimmed).flatMap(eventRepository::findById))
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + trimmed));
    }

    private java.util.Optional<Long> parseLong(String value) {
        try {
            return java.util.Optional.of(Long.parseLong(value));
        } catch (NumberFormatException ex) {
            return java.util.Optional.empty();
        }
    }
}
