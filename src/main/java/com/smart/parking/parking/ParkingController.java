package com.smart.parking.parking;

import com.smart.parking.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/parking")
@RequiredArgsConstructor
public class ParkingController {

    private final ParkingSpaceRepository parkingSpaceRepository;
    private final ParkingService parkingService;

    // Public endpoints for searching and viewing parking spaces
    @GetMapping("/nearby")
    public ResponseEntity<List<ParkingDTO>> getNearby(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "2000") Double radius
    ) {
        return ResponseEntity.ok(parkingService.findNearby(lat, lng, radius));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParkingDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(parkingService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ParkingDTO>> getAll() {
        return ResponseEntity.ok(parkingService.getAll());
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<ParkingDTO>> getByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(parkingService.getSpacesByEvent(eventId));
    }

    // Owner endpoints
    @PostMapping
    @PreAuthorize("hasAuthority('HOST')")
    public ResponseEntity<ApiResponse<ParkingSpaceDetailDTO>> registerParkingSpace(
            @Valid @RequestBody ParkingSpaceCreateDTO request,
            Authentication authentication) {
        ParkingSpace space = parkingService.createParkingSpace(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Parking space registered", toDetailDTO(space)));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAuthority('HOST')")
    public ResponseEntity<ApiResponse<List<ParkingSpaceDetailDTO>>> getMyParkingSpaces(Authentication authentication) {
        List<ParkingSpace> spaces = parkingSpaceRepository.findByOwnerEmail(authentication.getName());
        List<ParkingSpaceDetailDTO> dtos = spaces.stream().map(this::toDetailDTO).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Your parking spaces", dtos));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('HOST')")
    public ResponseEntity<ApiResponse<ParkingSpaceDetailDTO>> updateParkingSpace(
            @PathVariable Long id,
            @Valid @RequestBody ParkingSpaceCreateDTO request,
            Authentication authentication) {
        ParkingSpace space = parkingService.updateParkingSpace(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Parking space updated", toDetailDTO(space)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('HOST')")
    public ResponseEntity<ApiResponse<Void>> deleteParkingSpace(@PathVariable Long id, Authentication authentication) {
        parkingService.deleteParkingSpace(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Parking space deleted"));
    }

    private ParkingSpaceDetailDTO toDetailDTO(ParkingSpace space) {
        return ParkingSpaceDetailDTO.builder()
                .id(space.getId())
                .name(space.getName())
                .address(space.getAddress())
                .latitude(space.getLatitude())
                .longitude(space.getLongitude())
                .totalSlots(space.getTotalSlots())
                .availableSlots(space.getAvailableSlots())
                .pricePerSlot(space.getPricePerSlot())
                .eventEnabled(space.getEventEnabled())
                .ownerId(space.getOwner().getId())
                .ownerName(space.getOwner().getFullName())
                .build();
    }
}
