package com.smart.parking.parking;

import com.smart.parking.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/parking-spaces")
@RequiredArgsConstructor
@Validated
public class ParkingController {

    private final ParkingService parkingService;

    // Public endpoints for searching and viewing parking spaces
    @GetMapping("/nearby")
    public ResponseEntity<Page<ParkingDTO>> getNearby(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "2000") Double radius,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(parkingService.findNearby(lat, lng, radius, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParkingDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(parkingService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<ParkingDTO>> getAll(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(parkingService.getAll(pageable));
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<Page<ParkingDTO>> getByEvent(@PathVariable Long eventId,
                                                       @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(parkingService.getSpacesByEvent(eventId, pageable));
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
    public ResponseEntity<ApiResponse<Page<ParkingSpaceDetailDTO>>> getMyParkingSpaces(
            Authentication authentication,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ParkingSpaceDetailDTO> dtos = parkingService.getMyParkingSpaces(authentication.getName(), pageable)
                .map(this::toDetailDTO);
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
