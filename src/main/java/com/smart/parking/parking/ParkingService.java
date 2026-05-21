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

    /**
     * Get parking space by ID, UUID, or reference code (hybrid approach)
     * Users can retrieve by any identifier
     */
    @Cacheable(cacheNames = "parkingSpaces", key = "'lookup:' + #identifier")
    public ParkingDTO getByIdOrUuidOrCode(String identifier) {
        // Try by UUID first
        return spaceRepo.findByUuid(identifier)
                .map(this::toDTO)
                // Try by reference code
                .orElseGet(() -> spaceRepo.findByReferenceCode(identifier)
                        .map(this::toDTO)
                        // Try by numeric ID
                        .orElseGet(() -> {
                            try {
                                Long id = Long.parseLong(identifier);
                                return spaceRepo.findById(id)
                                        .map(this::toDTO)
                                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Parking space not found with ID: " + identifier));
                            } catch (NumberFormatException e) {
                                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                                        "Parking space not found with identifier: " + identifier);
                            }
                        }));
    }

    /**
     * Get parking space by exact name (case-insensitive)
     * Useful for drivers looking for a specific named parking area
     */
    @Cacheable(cacheNames = "parkingSpaces", key = "'name:' + #name")
    public ParkingDTO getByName(String name) {
        return spaceRepo.findByNameIgnoreCase(name)
                .map(this::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Parking space not found with name: " + name));
    }

    /**
     * Search parking spaces by name (partial match, case-insensitive)
     * Helps drivers find parking spaces by searching for keywords
     */
    @Cacheable(cacheNames = "parkingSpaces", key = "'search:' + #nameSearchTerm + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<ParkingDTO> searchByName(String nameSearchTerm, Pageable pageable) {
        Page<ParkingSpace> spaces = spaceRepo.findByNameContainingIgnoreCase(nameSearchTerm, pageable);
        if (spaces.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No parking spaces found matching: " + nameSearchTerm);
        }
        return spaces.map(this::toDTO);
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
                p.getUuid(),
                p.getReferenceCode(),
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
