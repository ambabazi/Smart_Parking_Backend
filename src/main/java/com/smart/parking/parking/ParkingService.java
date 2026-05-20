package com.smart.parking.parking;

import com.smart.parking.auth.User;
import com.smart.parking.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ParkingService {

    private final ParkingSpaceRepository spaceRepo;
    private final UserRepository userRepo;

    @Cacheable(cacheNames = "parkingSpacesNearby", key = "'nearby:' + #lat + ':' + #lng + ':' + #radiusMetres + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort")
    public Page<ParkingDTO> findNearby(Double lat, Double lng, Double radiusMetres, Pageable pageable) {
        Page<ParkingSpace> spaces = spaceRepo.findWithinRadius(lat, lng, radiusMetres, pageable);

        if (spaces.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No parking spaces found within " + radiusMetres + " metres");
        }
        return spaces.map(this::toDTO);
    }

    @Cacheable(cacheNames = "parkingSpaces", key = "'id:' + #id")
    public ParkingDTO getById(Long id) {
        return spaceRepo.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parking space not found"));
    }

    @Cacheable(cacheNames = "parkingSpaces", key = "'all:' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort")
    public Page<ParkingDTO> getAll(Pageable pageable) {
        return spaceRepo.findAll(pageable).map(this::toDTO);
    }

    @Cacheable(cacheNames = "parkingSpacesByEvent", key = "'event:' + #eventId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort")
    public Page<ParkingDTO> getSpacesByEvent(Long eventId, Pageable pageable) {
        return spaceRepo.findByCurrentEventId(eventId, pageable).map(this::toDTO);
    }

    @Cacheable(cacheNames = "parkingSpacesByOwner", key = "'owner:' + #ownerEmail + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort")
    public Page<ParkingSpace> getMyParkingSpaces(String ownerEmail, Pageable pageable) {
        return spaceRepo.findByOwnerEmail(ownerEmail, pageable);
    }

    @CacheEvict(cacheNames = {"parkingSpaces", "parkingSpacesNearby", "parkingSpacesByEvent", "parkingSpacesByOwner", "dashboardStats"}, allEntries = true)
    @Transactional
    public ParkingSpace createParkingSpace(ParkingSpaceCreateDTO dto, String ownerEmail) {
        User owner = userRepo.findByEmail(ownerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Owner not found"));

        ParkingSpace space = ParkingSpace.builder()
                .owner(owner)
                .name(dto.getName())
                .address(dto.getAddress())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .totalSlots(dto.getTotalSlots())
                .availableSlots(dto.getTotalSlots())
                .pricePerSlot(dto.getPricePerSlot())
                .eventEnabled(false)
                .build();

        return spaceRepo.save(space);
    }

    @CacheEvict(cacheNames = {"parkingSpaces", "parkingSpacesNearby", "parkingSpacesByEvent", "parkingSpacesByOwner", "dashboardStats"}, allEntries = true)
    @Transactional
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

    @CacheEvict(cacheNames = {"parkingSpaces", "parkingSpacesNearby", "parkingSpacesByEvent", "parkingSpacesByOwner", "dashboardStats"}, allEntries = true)
    @Transactional
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
}
