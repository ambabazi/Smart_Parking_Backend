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

    @PostMapping
    @PreAuthorize("hasAuthority('HOST')")
    public ResponseEntity<ApiResponse<ParkingSpaceDetailDTO>> registerParkingSpace(
            @Valid @RequestBody ParkingSpaceCreateDTO request,
            Authentication authentication) {
        try {
            ParkingSpace space = parkingService.createParkingSpace(request, authentication.getName());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Parking space registered", toDetailDTO(space)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("We couldn’t register the parking space. Please check your details and try again."));
        }
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAuthority('HOST')")
    public ResponseEntity<ApiResponse<List<ParkingSpaceDetailDTO>>> getMyParkingSpaces(
            Authentication authentication) {
        List<ParkingSpace> spaces = parkingSpaceRepository.findByOwnerEmail(authentication.getName());
        List<ParkingSpaceDetailDTO> dtos = spaces.stream()
                .map(this::toDetailDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Your parking spaces", dtos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ParkingSpaceDetailDTO>> getParkingSpace(@PathVariable Long id) {
        return parkingSpaceRepository.findById(id)
                .map(space -> ResponseEntity.ok(ApiResponse.success("Parking space found", toDetailDTO(space))))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('HOST')")
    public ResponseEntity<ApiResponse<ParkingSpaceDetailDTO>> updateParkingSpace(
            @PathVariable Long id,
            @Valid @RequestBody ParkingSpaceCreateDTO request,
            Authentication authentication) {
        try {
            ParkingSpace space = parkingService.updateParkingSpace(id, request, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Parking space updated", toDetailDTO(space)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('HOST')")
    public ResponseEntity<ApiResponse<Void>> deleteParkingSpace(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            parkingService.deleteParkingSpace(id, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Parking space deleted"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
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
