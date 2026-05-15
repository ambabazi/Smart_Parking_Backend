package com.smart.parking.parking;

<<<<<<< HEAD
=======
import com.smart.parking.auth.User;
import com.smart.parking.auth.UserRepository;
>>>>>>> origin/main
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;





import org.springframework.http.HttpStatus;

import org.springframework.web.server.ResponseStatusException;



@Service
@RequiredArgsConstructor
public class ParkingService {

    private final ParkingSpaceRepository spaceRepo;
    private final UserRepository userRepo;

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

    public ParkingSpace createParkingSpace(ParkingSpaceCreateDTO dto, String ownerEmail) {
        User owner = userRepo.findByEmail(ownerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Owner not found"));

        ParkingSpace space = ParkingSpace.builder()
                .name(dto.getName())
                .address(dto.getAddress())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .totalSlots(dto.getTotalSlots())
                .availableSlots(dto.getTotalSlots())
                .pricePerSlot(dto.getPricePerSlot())
                .eventEnabled(false)
                .owner(owner)
                .build();

        return spaceRepo.save(space);
    }

    public ParkingSpace updateParkingSpace(Long id, ParkingSpaceCreateDTO dto, String ownerEmail) {
        ParkingSpace space = spaceRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Parking space not found"));

        if (!space.getOwner().getEmail().equals(ownerEmail)) {
            throw new IllegalArgumentException("You can only update your own parking spaces");
        }

        space.setName(dto.getName());
        space.setAddress(dto.getAddress());
        space.setLatitude(dto.getLatitude());
        space.setLongitude(dto.getLongitude());
        space.setTotalSlots(dto.getTotalSlots());
        space.setPricePerSlot(dto.getPricePerSlot());

        return spaceRepo.save(space);
    }

    public void deleteParkingSpace(Long id, String ownerEmail) {
        ParkingSpace space = spaceRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Parking space not found"));

        if (!space.getOwner().getEmail().equals(ownerEmail)) {
            throw new IllegalArgumentException("You can only delete your own parking spaces");
        }

        spaceRepo.delete(space);
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
<<<<<<< HEAD
}
=======
}
>>>>>>> origin/main
