package com.smart.parking.parking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;





import org.springframework.http.HttpStatus;

import org.springframework.web.server.ResponseStatusException;



@Service
@RequiredArgsConstructor
public class ParkingService {

    private final ParkingSpaceRepository spaceRepo;

    // GET /parking-spaces/nearby
    public List<ParkingDTO> findNearby(Double lat,
                                            Double lng,
                                            Double radiusMetres) {
        List<ParkingSpace> spaces =
                spaceRepo.findWithinRadius(lat, lng, radiusMetres);

        if (spaces.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No parking spaces found within " + radiusMetres + " metres");
        }
        return spaces.stream().map(this::toDTO).toList();
    }

    // GET /parking-spaces/{id}
    public ParkingDTO getById(Long id) {
        return spaceRepo.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Parking space not found"));
    }

    // GET /parking-spaces — all spaces (admin/host use)
    public List<ParkingDTO> getAll() {
        return spaceRepo.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // GET /parking-spaces/event/{eventId}
    public List<ParkingDTO> getSpacesByEvent(Long eventId) {
        return spaceRepo.findByCurrentEventId(eventId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private ParkingDTO toDTO(ParkingSpace p) {
        return new ParkingDTO(
                p.getId(),
                p.getName(),
                p.getAddress(),
                p.getLatitude(),
                p.getLongitude(),
                p.getTotalSlots(),
                p.getAvailableSlots(),
                p.getPricePerSlot(),
                p.getEventEnabled()
        );
    }
}