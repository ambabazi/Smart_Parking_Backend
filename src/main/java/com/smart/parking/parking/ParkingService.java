package com.smart.parking.parking;

import com.smart.parking.auth.User;
import com.smart.parking.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkingService {

    private final ParkingSpaceRepository spaceRepo;
    private final UserRepository userRepo;

    public List<ParkingDTO> findNearby(Double lat, Double lng, Double radiusMetres) {
        return spaceRepo
                .findWithinRadius(lat, lng, radiusMetres)
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
                p.getLatitude(),
                p.getLongitude(),
                p.getAvailableSlots(),
                p.getPricePerSlot(),
                p.getEventEnabled()
        );
    }
}
