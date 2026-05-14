package com.smart.parking.parking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor  // Lombok: injects all final fields via constructor
public class ParkingService {

    private final ParkingSpaceRepository spaceRepo;

    public List<ParkingDTO> findNearby(Double lat, Double lng, Double radiusMetres) {
        return spaceRepo
                .findWithinRadius(lat, lng, radiusMetres)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private ParkingDTO toDTO(ParkingSpace p) {
        return new ParkingDTO(
                p.getId(),
                p.getName(),
                p.getLatitude(),
                p.getLongitude(),
                p.getAvailableSlots(),
                p.getPricePerSlot(),
                p.getEventEnabled()
        );
    }
}
